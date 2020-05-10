package message.queue;

import game.PlayType;
import message.DefaultPlayMessage;
import message.requests.RequestPracticeMessage;

public class PracticeQueueMessage extends DefaultPlayMessage {
    public PracticeQueueMessage(String username, RequestPracticeMessage requestMessage) {
        super(requestMessage.getGameType(), username);
    }

    @Override
    public PlayType playType() {
        return PlayType.PRACTICE;
    }
}
