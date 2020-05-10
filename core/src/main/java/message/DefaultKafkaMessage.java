package message;

import message.queue.PracticeQueueMessage;
import message.queue.TournamentQueueMessage;
import message.requests.RequestCreateTournamentMessage;

import java.io.Serializable;

public class DefaultKafkaMessage implements Serializable {
    private PracticeQueueMessage practiceQueueMessage;
    private TournamentQueueMessage tournamentQueueMessage;
    private RequestCreateTournamentMessage requestCreateTournamentMessage;

    public DefaultKafkaMessage() {
    }

    public DefaultKafkaMessage(PracticeQueueMessage practiceQueueMessage) {
        this.practiceQueueMessage = practiceQueueMessage;
        this.tournamentQueueMessage = null;
        this.requestCreateTournamentMessage = null;
    }

    public DefaultKafkaMessage(TournamentQueueMessage tournamentQueueMessage) {
        this.practiceQueueMessage = null;
        this.tournamentQueueMessage = tournamentQueueMessage;
        this.requestCreateTournamentMessage = null;
    }

    public DefaultKafkaMessage(RequestCreateTournamentMessage requestCreateTournamentMessage) {
        this.practiceQueueMessage = null;
        this.tournamentQueueMessage = null;
        this.requestCreateTournamentMessage = requestCreateTournamentMessage;
    }

    public boolean isPracticeMessage() {
        return this.practiceQueueMessage != null;
    }

    public boolean isJoinTournamentMessage() {
        return this.tournamentQueueMessage != null;
    }

    public boolean isCreateTournamentMessage() {
        return this.requestCreateTournamentMessage != null;
    }

    public PracticeQueueMessage getPracticeQueueMessage() {
        return practiceQueueMessage;
    }

    public void setPracticeQueueMessage(PracticeQueueMessage practiceQueueMessage) {
        this.practiceQueueMessage = practiceQueueMessage;
    }

    public TournamentQueueMessage getTournamentQueueMessage() {
        return tournamentQueueMessage;
    }

    public void setTournamentQueueMessage(TournamentQueueMessage tournamentQueueMessage) {
        this.tournamentQueueMessage = tournamentQueueMessage;
    }

    public RequestCreateTournamentMessage getRequestCreateTournamentMessage() {
        return requestCreateTournamentMessage;
    }

    public void setRequestCreateTournamentMessage(RequestCreateTournamentMessage requestCreateTournamentMessage) {
        this.requestCreateTournamentMessage = requestCreateTournamentMessage;
    }
}
