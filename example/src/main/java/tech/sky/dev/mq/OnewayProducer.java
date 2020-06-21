package tech.sky.dev.mq;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class OnewayProducer {
    public static void main(String[] args) throws MQClientException,
            UnsupportedEncodingException,
            RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer("oneway_test_group");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();

        int messageCount = 100;
        for (int i = 0; i < messageCount; i++) {
            Message msg = new Message("TopicTest",
                    "TagA",
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            producer.sendOneway(msg);
        }

        Thread.sleep(5000);
        producer.shutdown();

        // =========
        // 批量发送消息能显著提高传递小消息的性能。限制是这些批量消息应该有相同的topic，
        // 相同的waitStoreMsgOK，而且不能是延时消息。此外，这一批消息的总大小不应超过4MB
        /*String topic = "BatchTest";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(topic, "TagA", "OrderID001", "Hello world 0".getBytes()));
        messages.add(new Message(topic, "TagA", "OrderID002", "Hello world 1".getBytes()));
        messages.add(new Message(topic, "TagA", "OrderID003", "Hello world 2".getBytes()));
        try {
            producer.send(messages);
        } catch (Exception e) {
            e.printStackTrace();
            //处理error
        }

        ListSplitter splitter = new ListSplitter(messages);
        while (splitter.hasNext()){
            try {
                List<Message> listItem = splitter.next();
                producer.send(listItem);
            } catch (MQBrokerException e) {
                e.printStackTrace();
            }
        }*/


    }
}
