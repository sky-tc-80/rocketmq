package tech.sky.dev.mq;

import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 消息列表分割
 * 当发送大批量时才会增长，可能不确定它是否超过了大小限制（4MB）。最好把你的消息列表分割一下
 */
public class ListSplitter implements Iterator<List<Message>> {
    private final List<Message> messages;
    private final int size_limit = 1024 * 1024 * 4;
    private int currIndex;

    public ListSplitter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            Message message = messages.get(nextIndex);
            int tmpSize = message.getTopic().length() + message.getBody().length;
            Map<String, String> properties = message.getProperties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                tmpSize += entry.getKey().length() + entry.getValue().length();
            }
            // 增加日志的开销20字节
            tmpSize += tmpSize + 20;
            if (tmpSize > size_limit) {
                // 单个消息超过了最大的限制
                // 忽略,否则会阻塞分裂的进程
                if (nextIndex - currIndex == 0) {
                    //假如下一个子列表没有元素,则添加这个子列表然后退出循环,否则只是退出循环
                    nextIndex++;
                }
                break;
            }

            if (tmpSize + totalSize > size_limit) {
                break;
            } else {
                totalSize += tmpSize;
            }
        }
        List<Message> sub = this.messages.subList(currIndex, nextIndex);
        currIndex=nextIndex;
        return sub;
    }
}
