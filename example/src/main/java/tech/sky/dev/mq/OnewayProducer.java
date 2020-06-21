package tech.sky.dev.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

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
    }
}
