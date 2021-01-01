/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.util;

import io.netty.util.internal.MathUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static io.netty.util.internal.StringUtil.NEWLINE;
import static io.netty.util.internal.StringUtil.simpleClassName;

/**
 * Netty 内存检测原理：利用虚引用跟踪 ByteBuf 的对象的回收，并在垃圾回收后检测 ByteBuf 的 release方法是否有执行过，
 * 其实就是 DefaultResoureLeak 的 close 方法是否执行过判断是否发生了内存泄漏。
 * 同时利用装饰模式，将用于跟踪垃圾回收的虚引用 DefaultRersourceLeak 与具体的 BtyeBuf 包装起来，
 * 装饰类：WrappedByteBuf进行包装。
 *
 * 内部维护一个双端链表，维护所有指向堆外内存的引用对象链
 * （ResourceLeak对象链，ResourceLeak的实现类，继承虚拟引用PhantomReference），head 与 tail 是虚拟节点
 * @param <T>
 */
public class ResourceLeakDetector<T> {

    private static final String PROP_LEVEL_OLD = "io.netty.leakDetectionLevel";
    private static final String PROP_LEVEL = "io.netty.leakDetection.level";
    private static final Level DEFAULT_LEVEL = Level.SIMPLE;

    private static final String PROP_MAX_RECORDS = "io.netty.leakDetection.maxRecords";
    private static final int DEFAULT_MAX_RECORDS = 4;
    private static final int MAX_RECORDS;

    /**
     * 内存检测级别
     * Represents the level of resource leak detection.
     */
    public enum Level {
        /**
         * 禁用内存泄露检测
         * Disables resource leak detection.
         */
        DISABLED,
        /**
         * 默认的内存检测级别，以一个时间间隔，默认是每创建113个直接内存（堆外内存）时检测一次
         * Enables simplistic sampling resource leak detection which reports there is a leak or not,
         * at the cost of small overhead (default).
         */
        SIMPLE,
        /**
         * 每次产生一个堆外内存，目前这两个在Netty的实现中等价，统一用ADVANCED来实现
         * Enables advanced sampling resource leak detection which reports where the leaked object was accessed
         * recently at the cost of high overhead.
         */
        ADVANCED,
        /**
         * Enables paranoid resource leak detection which reports where the leaked object was accessed recently,
         * at the cost of the highest possible overhead (for testing purposes only).
         */
        PARANOID
    }

    private static Level level;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetector.class);

    static {
        final boolean disabled;
        if (SystemPropertyUtil.get("io.netty.noResourceLeakDetection") != null) {
            disabled = SystemPropertyUtil.getBoolean("io.netty.noResourceLeakDetection", false);
            logger.debug("-Dio.netty.noResourceLeakDetection: {}", disabled);
            logger.warn(
                    "-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.",
                    PROP_LEVEL, DEFAULT_LEVEL.name().toLowerCase());
        } else {
            disabled = false;
        }

        Level defaultLevel = disabled? Level.DISABLED : DEFAULT_LEVEL;

        // First read old property name
        String levelStr = SystemPropertyUtil.get(PROP_LEVEL_OLD, defaultLevel.name()).trim().toUpperCase();

        // If new property name is present, use it
        levelStr = SystemPropertyUtil.get(PROP_LEVEL, levelStr).trim().toUpperCase();
        Level level = DEFAULT_LEVEL;
        for (Level l: EnumSet.allOf(Level.class)) {
            if (levelStr.equals(l.name()) || levelStr.equals(String.valueOf(l.ordinal()))) {
                level = l;
            }
        }

        MAX_RECORDS = SystemPropertyUtil.getInt(PROP_MAX_RECORDS, DEFAULT_MAX_RECORDS);

        ResourceLeakDetector.level = level;
        if (logger.isDebugEnabled()) {
            logger.debug("-D{}: {}", PROP_LEVEL, level.name().toLowerCase());
            logger.debug("-D{}: {}", PROP_MAX_RECORDS, MAX_RECORDS);
        }
    }

    // Should be power of two.
    static final int DEFAULT_SAMPLING_INTERVAL = 128;

    /**
     * @deprecated Use {@link #setLevel(Level)} instead.
     */
    @Deprecated
    public static void setEnabled(boolean enabled) {
        setLevel(enabled? Level.SIMPLE : Level.DISABLED);
    }

    /**
     * Returns {@code true} if resource leak detection is enabled.
     */
    public static boolean isEnabled() {
        return getLevel().ordinal() > Level.DISABLED.ordinal();
    }

    /**
     * Sets the resource leak detection level.
     */
    public static void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException("level");
        }
        ResourceLeakDetector.level = level;
    }

    /**
     * Returns the current resource leak detection level.
     */
    public static Level getLevel() {
        return level;
    }

    /** the linked list of active resources */
    private final DefaultResourceLeak head = new DefaultResourceLeak(null);
    private final DefaultResourceLeak tail = new DefaultResourceLeak(null);

    /**
     * 引用队列，该引用队列正存放的是 ResoureLeak 对象链，
     * 代表着这里面的引用对象所指向的对象已被垃圾回收。按照常理，如果该 ResourceLeak 对象，
     * 也同时存在于上面的双端链表中，说明发生了内存泄漏
     */
    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
    private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent.newConcurrentHashMap();

    /**
     * 检测对象的完全限定名称,主要用途用于报告内存泄漏时，相关的详细信息
     */
    private final String resourceType;
    /**
     * 内存泄漏检测级别是SIMPLE时，的检测频率，默认113，单位为个，而不是时间
     */
    private final int samplingInterval;
    private final int mask;
    // 该类对象的最大激活数量
    private final long maxActive;
    // 该类对象的最大激活数量
    private long active;
    // active * samplingInterval > maxActive 条件满足时，是否打印日志
    private final AtomicBoolean loggedTooManyActive = new AtomicBoolean();

    private long leakCheckCnt;

    /**
     * @deprecated use {@link ResourceLeakDetectorFactory#newResourceLeakDetector(Class, int, long)}.
     */
    @Deprecated
    public ResourceLeakDetector(Class<?> resourceType) {
        this(simpleClassName(resourceType));
    }

    /**
     * @deprecated use {@link ResourceLeakDetectorFactory#newResourceLeakDetector(Class, int, long)}.
     */
    @Deprecated
    public ResourceLeakDetector(String resourceType) {
        this(resourceType, DEFAULT_SAMPLING_INTERVAL, Long.MAX_VALUE);
    }

    /**
     * This should not be used directly by users of {@link ResourceLeakDetector}.
     * Please use {@link ResourceLeakDetectorFactory#newResourceLeakDetector(Class)}
     * or {@link ResourceLeakDetectorFactory#newResourceLeakDetector(Class, int, long)}
     */
    @SuppressWarnings("deprecation")
    public ResourceLeakDetector(Class<?> resourceType, int samplingInterval, long maxActive) {
        this(simpleClassName(resourceType), samplingInterval, maxActive);
    }

    /**
     * @deprecated use {@link ResourceLeakDetectorFactory#newResourceLeakDetector(Class, int, long)}.
     */
    @Deprecated
    public ResourceLeakDetector(String resourceType, int samplingInterval, long maxActive) {
        if (resourceType == null) {
            throw new NullPointerException("resourceType");
        }
        if (maxActive <= 0) {
            throw new IllegalArgumentException("maxActive: " + maxActive + " (expected: 1+)");
        }

        this.resourceType = resourceType;
        this.samplingInterval = MathUtil.safeFindNextPositivePowerOfTwo(samplingInterval);
        // samplingInterval is a power of two so we calculate a mask that we can use to
        // check if we need to do any leak detection or not.
        mask = this.samplingInterval - 1;
        this.maxActive = maxActive;

        head.next = tail;
        tail.prev = head;
    }

    /**
     * 如果返回一个 ResoureLeak，则用 ResourceLeak 与当前的 ByteBuf 放入一个包装类，
     * 跟踪该直接内存的回收情况，检测内存泄露。如果没有产生一个 ResourceLeak，则不会跟踪直接内存的泄露检测
     * Creates a new {@link ResourceLeak} which is expected to be closed via {@link ResourceLeak#close()} when the
     * related resource is deallocated.
     *
     * @return the {@link ResourceLeak} or {@code null}
     */
    public final ResourceLeak open(T obj) {
        Level level = ResourceLeakDetector.level;
        if (level == Level.DISABLED) {
            return null;
        }

        // 如果监控级别低于PARANOID,在一定的采样频率下报告内存泄露
        if (level.ordinal() < Level.PARANOID.ordinal()) {
            if ((++ leakCheckCnt & mask) == 0) {
                reportLeak(level);
                return new DefaultResourceLeak(obj);
            } else {
                return null;
            }
        } else {
            // 每次需要分配 ByteBuf 时,报告内存泄露情况
            reportLeak(level);
            return new DefaultResourceLeak(obj);
        }
    }

    private void reportLeak(Level level) {
        if (!logger.isErrorEnabled()) {
            for (;;) {
                @SuppressWarnings("unchecked")
                DefaultResourceLeak ref = (DefaultResourceLeak) refQueue.poll();
                if (ref == null) {
                    break;
                }
                ref.close();
            }
            return;
        }

        // Report too many instances.
        int samplingInterval = level == Level.PARANOID? 1 : this.samplingInterval;
        if (active * samplingInterval > maxActive && loggedTooManyActive.compareAndSet(false, true)) {
            reportInstancesLeak(resourceType);
        }

        // Detect and report previous leaks.
        for (;;) {
            // 从垃圾回收器通知的引用队列中找(位于该队列中的引用代表该引用执向的对象已经被垃圾回收器回收了），
            // 如果调用close 方法，返回false, 说明没有泄露，close 方法第一次调用时，会返回 true,
            // 将 DefaultResourceLeak 的 free设置为true,表示已经释放，所以在检测是否泄露的时候，
            // 只要内存泄露程序调用 close 不是第一次调用，就可以说明内存未泄露
            @SuppressWarnings("unchecked")
            DefaultResourceLeak ref = (DefaultResourceLeak) refQueue.poll();
            if (ref == null) {
                break;
            }

            // 清理引用
            ref.clear();
            // netty判断是否有内存泄露应该是根据if(ref.close())来实现的,如果正常release的话,
            // ref.close()返回false.也就不会继续下面的report了
            if (!ref.close()) {
                continue;
            }

            String records = ref.toString();
            if (reportedLeaks.putIfAbsent(records, Boolean.TRUE) == null) {
                if (records.isEmpty()) {
                    reportUntracedLeak(resourceType);
                } else {
                    reportTracedLeak(resourceType, records);
                }
            }
        }
    }

    /**
     * This method is called when a traced leak is detected. It can be overridden for tracking how many times leaks
     * have been detected.
     */
    protected void reportTracedLeak(String resourceType, String records) {
        logger.error(
                "LEAK: {}.release() was not called before it's garbage-collected. " +
                "See http://netty.io/wiki/reference-counted-objects.html for more information.{}",
                resourceType, records);
    }

    /**
     * This method is called when an untraced leak is detected. It can be overridden for tracking how many times leaks
     * have been detected.
     */
    protected void reportUntracedLeak(String resourceType) {
        logger.error("LEAK: {}.release() was not called before it's garbage-collected. " +
                "Enable advanced leak reporting to find out where the leak occurred. " +
                "To enable advanced leak reporting, " +
                "specify the JVM option '-D{}={}' or call {}.setLevel() " +
                "See http://netty.io/wiki/reference-counted-objects.html for more information.",
                resourceType, PROP_LEVEL, Level.ADVANCED.name().toLowerCase(), simpleClassName(this));
    }

    /**
     * This method is called when instance leaks are detected. It can be overridden for tracking how many times leaks
     * have been detected.
     */
    protected void reportInstancesLeak(String resourceType) {
        logger.error("LEAK: You are creating too many " + resourceType + " instances.  " +
                resourceType + " is a shared resource that must be reused across the JVM," +
                "so that only a few instances are created.");
    }

    /**
     * DefaultResourceLeak是个”虚”引用类型,有别于常见的普通的”强”引用,虚引用完全不影响目标对象的垃圾回收,但是会在目标对象被VM垃圾回收时被加入到引用队列中.
     * 在正常情况下ResourceLeak对象会所监控的资源的引用计数为0时被清理掉(不在被加入引用队列),所以一旦资源的引用计数失常,ResourceLeak对象会被加入到引用队列.例如没有成对调用ByteBuf的retain和relaease方法,导致ByteBuf没有被正常释放(对于DirectByteBuf没有及时释放内存,对于PooledByteBuf没有返回Pool),当引用队列中存在元素时意味着程序中有内存泄露发生.
     * ResourceLeakDetector通过检查引用队列来判断是否有内存泄露,并报告跟踪情况.
     *
     * 总结
     * Netty使用装饰器模式,为ByteBuf增加内存跟踪记录功能.利用虚引用跟踪资源被VM垃圾回收的情况,加上ByteBuf的引用计数特性,进而判断是否发生内存泄露
     */
    private final class DefaultResourceLeak extends PhantomReference<Object> implements ResourceLeak {
        private final String creationRecord;
        private final Deque<String> lastRecords = new ArrayDeque<String>();
        private final AtomicBoolean freed;
        private DefaultResourceLeak prev;
        private DefaultResourceLeak next;
        private int removedRecords;

        DefaultResourceLeak(Object referent) {
            super(referent, referent != null? refQueue : null);

            if (referent != null) {
                Level level = getLevel();
                if (level.ordinal() >= Level.ADVANCED.ordinal()) {
                    creationRecord = newRecord(3);
                } else {
                    creationRecord = null;
                }

                // TODO: Use CAS to update the list.
                synchronized (head) {
                    prev = head;
                    next = head.next;
                    head.next.prev = this;
                    head.next = this;
                    active ++;
                }
                freed = new AtomicBoolean();
            } else {
                creationRecord = null;
                freed = new AtomicBoolean(true);
            }
        }

        @Override
        public void record() {
            if (creationRecord != null) {
                String value = newRecord(2);

                synchronized (lastRecords) {
                    int size = lastRecords.size();
                    if (size == 0 || !lastRecords.getLast().equals(value)) {
                        lastRecords.add(value);
                    }
                    if (size > MAX_RECORDS) {
                        lastRecords.removeFirst();
                        ++removedRecords;
                    }
                }
            }
        }

        @Override
        public boolean close() {
            if (freed.compareAndSet(false, true)) {
                synchronized (head) {
                    active --;
                    prev.next = next;
                    next.prev = prev;
                    prev = null;
                    next = null;
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            if (creationRecord == null) {
                return EMPTY_STRING;
            }

            final Object[] array;
            final int removedRecords;
            synchronized (lastRecords) {
                array = lastRecords.toArray();
                removedRecords = this.removedRecords;
            }

            StringBuilder buf = new StringBuilder(16384).append(NEWLINE);
            if (removedRecords > 0) {
                buf.append("WARNING: ")
                .append(removedRecords)
                .append(" leak records were discarded because the leak record count is limited to ")
                .append(MAX_RECORDS)
                .append(". Use system property ")
                .append(PROP_MAX_RECORDS)
                .append(" to increase the limit.")
                .append(NEWLINE);
            }
            buf.append("Recent access records: ")
            .append(array.length)
            .append(NEWLINE);

            if (array.length > 0) {
                for (int i = array.length - 1; i >= 0; i --) {
                    buf.append('#')
                       .append(i + 1)
                       .append(':')
                       .append(NEWLINE)
                       .append(array[i]);
                }
            }

            buf.append("Created at:")
               .append(NEWLINE)
               .append(creationRecord);

            buf.setLength(buf.length() - NEWLINE.length());
            return buf.toString();
        }
    }

    private static final String[] STACK_TRACE_ELEMENT_EXCLUSIONS = {
            "io.netty.buffer.AbstractByteBufAllocator.toLeakAwareBuffer(",
            "io.netty.buffer.AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation("
    };

    static String newRecord(int recordsToSkip) {
        StringBuilder buf = new StringBuilder(4096);
        StackTraceElement[] array = new Throwable().getStackTrace();
        for (StackTraceElement e: array) {
            if (recordsToSkip > 0) {
                recordsToSkip --;
            } else {
                String estr = e.toString();

                // Strip the noisy stack trace elements.
                boolean excluded = false;
                for (String exclusion: STACK_TRACE_ELEMENT_EXCLUSIONS) {
                    if (estr.startsWith(exclusion)) {
                        excluded = true;
                        break;
                    }
                }

                if (!excluded) {
                    buf.append('\t');
                    buf.append(estr);
                    buf.append(NEWLINE);
                }
            }
        }

        return buf.toString();
    }
}
