package message.requests;

import game.GameType;
import game.PlayType;
import message.DefaultPlayMessage;

public class RequestPracticeMessage extends DefaultPlayMessage {
    public RequestPracticeMessage(String username, GameType gameType) {
        super(gameType, username);
    }

    @Override
    public PlayType playType() {
        return PlayType.PRACTICE;
    }
}

