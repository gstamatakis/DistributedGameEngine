package message.queue;

import message.DefaultPlayMessage;
import message.requests.RequestPracticeMessage;
import model.PlayTypeEnum;

public class PracticeQueueMessage extends DefaultPlayMessage {
    public PracticeQueueMessage(String username, RequestPracticeMessage requestMessage) {
        super(requestMessage.getGameType(), username);
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.PRACTICE;
    }
}
