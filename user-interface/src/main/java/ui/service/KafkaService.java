package ui.service;

import com.google.gson.Gson;
import game.GameType;
import game.PlayType;
import message.JoinPlayMessage;
import message.PlayMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaService {
    private final String joinPlayTopic = "join-play";
    private final String playsTopic = "plays";
    private final Gson gson = new Gson();
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, JoinPlayMessage> kafkaJoinQueueTemplate;

    public void enqueuePractice(String username, GameType gt) throws InterruptedException, ExecutionException, TimeoutException {
        //Create the message
        JoinPlayMessage message = new JoinPlayMessage(username, PlayType.PRACTICE, gt, "");
        //Send the message
        kafkaJoinQueueTemplate.send(joinPlayTopic, username, message).get(timeout, TimeUnit.SECONDS);
    }

    //Listeners
    @KafkaListener(topics = playsTopic, groupId = "observer")
    public void listenForNewPlays(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        PlayMessage newPlay = gson.fromJson(message, PlayMessage.class);
        System.out.println("Received Message: " + newPlay.toString() + "from partition: " + partition);
    }

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

//    public void sendMessage(String message, String topicName) {
//        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);
//        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
//
//            @Override
//            public void onSuccess(SendResult<String, String> result) {
//                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
//            }
//
//            @Override
//            public void onFailure(Throwable ex) {
//                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
//            }
//        });
//    }
}
