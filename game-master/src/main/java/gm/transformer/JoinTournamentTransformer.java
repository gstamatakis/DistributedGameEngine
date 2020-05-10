package gm.transformer;

import message.created.PlayMessage;
import message.created.TournamentPlayMessage;
import message.queue.TournamentQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinTournamentTransformer implements Transformer<String, TournamentQueueMessage, KeyValue<String, PlayMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(JoinTournamentTransformer.class);

    private final String pairTournamentPlayersStore;
    private final String userToGameIDStore;
    private final String gameIDToGameStore;
    private KeyValueStore<String, TournamentPlayMessage> pairTournamentPlayersKVStore;
    private KeyValueStore<String, PlayMessage> userToGameKVStore;
    private KeyValueStore<String, PlayMessage> gameIDToGameKVStore;

    public JoinTournamentTransformer(String pairTournamentPlayersStore, String userToGameIDStore, String gameIDToGameStore) {
        this.pairTournamentPlayersStore = pairTournamentPlayersStore;
        this.userToGameIDStore = userToGameIDStore;
        this.gameIDToGameStore = gameIDToGameStore;
    }

    @Override
    public void init(ProcessorContext context) {
        this.pairTournamentPlayersKVStore = (KeyValueStore<String, TournamentPlayMessage>) context.getStateStore(pairTournamentPlayersStore);
        this.userToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(userToGameIDStore);
        this.gameIDToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(gameIDToGameStore);
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String key, TournamentQueueMessage newPlayer) {
        TournamentPlayMessage play = pairTournamentPlayersKVStore.get(newPlayer.getTournamentID());
        if (play == null) {
            logger.error("Tried to join a non existing tournament: " + newPlayer.toString());
        } else {
            if (play.addPlayer(newPlayer)) {
                logger.info("Added " + newPlayer.getCreatedBy() + " to tournament " + play.getTournamentID());
                if (play.isFull()) {
                    PlayMessage playMessage = new PlayMessage(play);
                    for (String player : playMessage.getPlayerUsernames()) {
                        userToGameKVStore.put(player, playMessage);
                    }
                    gameIDToGameKVStore.put(playMessage.getID(), playMessage);
                    pairTournamentPlayersKVStore.delete(playMessage.getID());
                    return new KeyValue<>(playMessage.getID(), playMessage);
                } else {
                    pairTournamentPlayersKVStore.put(play.getTournamentID(), play);
                }
            } else {
                logger.error("Failed to add " + newPlayer.getCreatedBy() + " to tournament " + play.getTournamentID());
            }
        }
        return null;    //Null values are dropped from stream by default
    }

    @Override
    public void close() {
        if (pairTournamentPlayersKVStore != null) {
            if (pairTournamentPlayersKVStore.isOpen()) {
                pairTournamentPlayersKVStore.close();
            }
        }
        if (userToGameKVStore != null) {
            if (userToGameKVStore.isOpen()) {
                userToGameKVStore.close();
            }
        }
        if (gameIDToGameKVStore != null) {
            if (gameIDToGameKVStore.isOpen()) {
                gameIDToGameKVStore.close();
            }
        }
    }
}
