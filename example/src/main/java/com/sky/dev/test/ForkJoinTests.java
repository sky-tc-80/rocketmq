package com.sky.dev.test;

import com.sky.dev.util.concurrent.CompletableFuture;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * 内存交互操作
 *
 *  　　内存交互操作有8种，虚拟机实现必须保证每一个操作都是原子的，不可在分的（对于double和long类型的变量来说，
 *  load、store、read和write操作在某些平台上允许例外）
 *
 *         lock     （锁定）：作用于主内存的变量，把一个变量标识为线程独占状态
 *         unlock （解锁）：作用于主内存的变量，它把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定
 *         read    （读取）：作用于主内存变量，它把一个变量的值从主内存传输到线程的工作内存中，以便随后的load动作使用
 *         load     （载入）：作用于工作内存的变量，它把read操作从主存中变量放入工作内存中
 *         use      （使用）：作用于工作内存中的变量，它把工作内存中的变量传输给执行引擎，每当虚拟机遇到一个需要使用到变量的值，就会使用到这个指令
 *         assign  （赋值）：作用于工作内存中的变量，它把一个从执行引擎中接受到的值放入工作内存的变量副本中
 *         store    （存储）：作用于主内存中的变量，它把一个从工作内存中一个变量的值传送到主内存中，以便后续的write使用
 *         write 　（写入）：作用于主内存中的变量，它把store操作从工作内存中得到的变量的值放入主内存的变量中
 *
 * 　　JMM对这八种指令的使用，制定了如下规则：
 *
 *         不允许read和load、store和write操作之一单独出现。即使用了read必须load，使用了store必须write
 *         不允许线程丢弃他最近的assign操作，即工作变量的数据改变了之后，必须告知主存
 *         不允许一个线程将没有assign的数据从工作内存同步回主内存
 *         一个新的变量必须在主内存中诞生，不允许工作内存直接使用一个未被初始化的变量。就是怼变量实施use、store操作之前，必须经过assign和load操作
 *         一个变量同一时间只有一个线程能对其进行lock。多次lock后，必须执行相同次数的unlock才能解锁
 *         如果对一个变量进行lock操作，会清空所有工作内存中此变量的值，在执行引擎使用这个变量前，必须重新load或assign操作初始化变量的值
 *         如果一个变量没有被lock，就不能对其进行unlock操作。也不能unlock一个被其他线程锁住的变量
 *         对一个变量进行unlock操作之前，必须把此变量同步回主内存
 *
 * 程序次序法则：线程中的每个动作A都happends-before于该线程中的每一个动作B,其中，在程序中，所有的动作B都出现在动作A之后。
 * 监视器锁法则：对一个监视器锁的解锁happens-before于每一个后续对同一个监视器锁的加锁。
 * volatile变量法则：对volatile域的写入操作happends-before于每一个后续对同一域的读操作。
 * 线程启动法则：在一个线程里，对Thread.start的调用会happends-before于每一个启动线程中的动作。
 * 线程终结法则：线程中的任何动作都happends-before于其他线程检测到这个线程已经终结，或者从Thread.join 调用中成功返回，或者Thread.isAlive返回false.
 * 中断法则：一个线程调用另一个线程的interrupt.happens-before于被中断的线程发现中断。
 * （通过跑出interruptedException,或者调用isInterrupted和interrupted）.
 * 终结法则：一个对象的构造函数的结束happends-before于这个对象finalizer的开始。
 * 传递性：如果A happens-before于B, 且B happends-before 于C, 则A happens-before 于C。
 */
public class ForkJoinTests extends RecursiveTask<Long> {
    @Override
    protected Long compute() {
        return null;
    }

    private static void testParallel() {
        long r = LongStream
                .rangeClosed(0L, 10_0000_0000)
                .parallel()
                .reduce(0L, Long::sum);
        System.out.println(r);

    }

    private static void testCompletableFutureNoReturn() throws Exception {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " supplyAsync => ending");
        });
        System.out.println("main thread running...");
        System.out.println(task.get());
    }

    private static void testCompletableFutureWithReturn() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " supplyAsync => ending");
            int i = 10/0;
            return 11L;
        });
        System.out.println("main thread running...");

        System.out.println(future.whenComplete((t, e) -> {
            System.out.println("the success value is => " + t);
            System.out.println("the error is => " + e);
        }).exceptionally(e -> {
            System.out.println(e.getMessage());
            return 22L;
        }).get());
        //System.out.println(future.get());
    }

    public static void main(String[] args) {
        testParallel();
        try {
            //testCompletableFutureNoReturn();
            testCompletableFutureWithReturn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
