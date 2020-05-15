package message;

import message.queue.PracticeQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.CreateTournamentQueueMessage;

import java.io.Serializable;

public class DefaultKafkaMessage implements Serializable {
    private PracticeQueueMessage practiceQueueMessage;
    private JoinTournamentQueueMessage joinTournamentQueueMessage;
    private CreateTournamentQueueMessage createTournamentQueueMessage;

    public DefaultKafkaMessage() {
    }

    public DefaultKafkaMessage(PracticeQueueMessage practiceQueueMessage) {
        this.practiceQueueMessage = practiceQueueMessage;
        this.joinTournamentQueueMessage = null;
        this.createTournamentQueueMessage = null;
    }

    public DefaultKafkaMessage(JoinTournamentQueueMessage joinTournamentQueueMessage) {
        this.practiceQueueMessage = null;
        this.joinTournamentQueueMessage = joinTournamentQueueMessage;
        this.createTournamentQueueMessage = null;
    }

    public DefaultKafkaMessage(CreateTournamentQueueMessage createTournamentQueueMessage) {
        this.practiceQueueMessage = null;
        this.joinTournamentQueueMessage = null;
        this.createTournamentQueueMessage = createTournamentQueueMessage;
    }

    public boolean isPracticeMessage() {
        return this.practiceQueueMessage != null;
    }

    public boolean isJoinTournamentMessage() {
        return this.joinTournamentQueueMessage != null;
    }

    public boolean isCreateTournamentMessage() {
        return this.createTournamentQueueMessage != null;
    }

    public PracticeQueueMessage getPracticeQueueMessage() {
        return practiceQueueMessage;
    }

    public void setPracticeQueueMessage(PracticeQueueMessage practiceQueueMessage) {
        this.practiceQueueMessage = practiceQueueMessage;
    }

    public JoinTournamentQueueMessage getJoinTournamentQueueMessage() {
        return joinTournamentQueueMessage;
    }

    public void setJoinTournamentQueueMessage(JoinTournamentQueueMessage joinTournamentQueueMessage) {
        this.joinTournamentQueueMessage = joinTournamentQueueMessage;
    }

    public CreateTournamentQueueMessage getCreateTournamentQueueMessage() {
        return createTournamentQueueMessage;
    }

    public void setCreateTournamentQueueMessage(CreateTournamentQueueMessage createTournamentQueueMessage) {
        this.createTournamentQueueMessage = createTournamentQueueMessage;
    }
}
