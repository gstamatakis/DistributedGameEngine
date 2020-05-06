package ui.service;

import message.UserJoinQueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, UserJoinQueueMessage> kafkaJoinQueueTemplate;

    //Listeners
//    @KafkaListener(topics = "topicName")
//    public void listenWithHeaders(
//            @Payload String message,
//            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
//        System.out.println("Received Message: " + message + "from partition: " + partition);
//    }
//
//    @KafkaListener(topicPartitions = @TopicPartition(topic = "topicName",
//            partitionOffsets = {
//                    @PartitionOffset(partition = "0", initialOffset = "0"),
//                    @PartitionOffset(partition = "3", initialOffset = "0")
//            }))
//    public void listenToParition(
//            @Payload String message,
//            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
//        System.out.println("Received Message: " + message + "from partition: " + partition);
//    }

    //Public methods
    public void enqueuePractice(UserJoinQueueMessage joinQueueMessage) {

    }

    //COM methods
    private void sendMessage(String message, String topicName) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

    private void sendMessage(UserJoinQueueMessage message, String topicName) {
        ListenableFuture<SendResult<String, UserJoinQueueMessage>> future = kafkaJoinQueueTemplate.send(topicName, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, UserJoinQueueMessage>>() {
            @Override
            public void onSuccess(SendResult<String, UserJoinQueueMessage> result) {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

}
