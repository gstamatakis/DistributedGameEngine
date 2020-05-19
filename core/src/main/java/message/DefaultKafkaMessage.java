package message;

import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.MoveMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;

import java.io.Serializable;

public class DefaultKafkaMessage implements Serializable {
    private PracticeQueueMessage practiceQueueMessage;
    private JoinTournamentQueueMessage joinTournamentQueueMessage;
    private CreateTournamentQueueMessage createTournamentQueueMessage;
    private MoveMessage playerMoveMessage;
    private CompletedMoveMessage completedMoveMessage;
    private CompletedPlayMessage completedPlayMessage;

    public DefaultKafkaMessage() {
        this.practiceQueueMessage = null;
        this.joinTournamentQueueMessage = null;
        this.createTournamentQueueMessage = null;
        this.playerMoveMessage = null;
        this.completedMoveMessage = null;
        this.completedMoveMessage = null;
    }

    public DefaultKafkaMessage(PracticeQueueMessage practiceQueueMessage) {
        this();
        this.practiceQueueMessage = practiceQueueMessage;
    }

    public DefaultKafkaMessage(JoinTournamentQueueMessage joinTournamentQueueMessage) {
        this();
        this.joinTournamentQueueMessage = joinTournamentQueueMessage;
    }

    public DefaultKafkaMessage(CreateTournamentQueueMessage createTournamentQueueMessage) {
        this();
        this.createTournamentQueueMessage = createTournamentQueueMessage;
    }

    public DefaultKafkaMessage(MoveMessage playerMoveMessage) {
        this();
        this.playerMoveMessage = playerMoveMessage;
    }

    public DefaultKafkaMessage(CompletedMoveMessage completedMoveMessage) {
        this();
        this.completedMoveMessage = completedMoveMessage;
    }

    public DefaultKafkaMessage(CompletedPlayMessage completedPlayMessage) {
        this();
        this.completedPlayMessage = completedPlayMessage;
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

    public boolean isPlayerMoveMessage() {
        return this.playerMoveMessage != null;
    }

    public boolean isCompletedMoveMessage() {
        return this.completedMoveMessage != null;
    }

    public boolean isCompletedPlayMessage() {
        return this.completedPlayMessage != null;
    }


    public boolean getPlayerMoveMessage() {
        return this.playerMoveMessage != null;
    }

    public void setPlayerMoveMessage(MoveMessage MoveMessage) {
        this.playerMoveMessage = MoveMessage;
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

    public CompletedMoveMessage getCompletedMoveMessage() {
        return completedMoveMessage;
    }

    public CompletedPlayMessage getCompletedPlayMessage() {
        return completedPlayMessage;
    }

    public MoveMessage getMoveMessage() {
        return playerMoveMessage;
    }
}
