package message.queue;

import model.PlayTypeEnum;
import message.DefaultPlayMessage;
import message.requests.RequestPracticeMessage;

public class PracticeQueueMessage extends DefaultPlayMessage {
    public PracticeQueueMessage(String username, RequestPracticeMessage requestMessage) {
        super(requestMessage.getGameType(), username);
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.PRACTICE;
    }
}
