package message.requests;

import message.DefaultPlayMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

public class RequestPracticeMessage extends DefaultPlayMessage {
    public RequestPracticeMessage(String username, GameTypeEnum gameType) {
        super(gameType, username);
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.PRACTICE;
    }
}

