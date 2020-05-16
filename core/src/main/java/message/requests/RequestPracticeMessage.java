package message.requests;

import model.GameTypeEnum;
import model.PlayTypeEnum;
import message.DefaultPlayMessage;

public class RequestPracticeMessage extends DefaultPlayMessage {
    public RequestPracticeMessage(String username, GameTypeEnum gameType) {
        super(gameType, username);
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.PRACTICE;
    }
}

