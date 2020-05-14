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

import java.util.Iterator;
import java.util.List;

public class JoinTournamentTransformer implements Transformer<String, TournamentQueueMessage, KeyValue<String, PlayMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(JoinTournamentTransformer.class);

    private final String pairTournamentPlayersStore;
    private final String userToGameIDStore;
    private final String gameIDToGameStore;
    private KeyValueStore<String, TournamentPlayMessage> pairTournamentPlayersKVStore;
    private KeyValueStore<String, PlayMessage> userToGameKVStore;
    private KeyValueStore<String, PlayMessage> gameIDToGameKVStore;
    private ProcessorContext ctx;

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
        this.ctx = context;
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String key, TournamentQueueMessage newPlayer) {
        TournamentPlayMessage tournamentPlay = pairTournamentPlayersKVStore.get(newPlayer.getTournamentID());
        if (tournamentPlay == null) {
            logger.error("Tried to join a non existing tournament: " + newPlayer.toString());
        } else {
            if (tournamentPlay.addPlayer(newPlayer)) {
                logger.info("Added " + newPlayer.getCreatedBy() + " to tournament " + tournamentPlay.getTournamentID());
                if (tournamentPlay.isFull()) {
                    Iterator<String> playerIter = tournamentPlay.getPlayerUsernames().iterator();
                    while (playerIter.hasNext()) {
                        String p1 = playerIter.next();
                        String p2 = playerIter.next();
                        PlayMessage newPlay = new PlayMessage(tournamentPlay, p1, p2);
                        userToGameKVStore.put(p1, newPlay);
                        userToGameKVStore.put(p2, newPlay);
                        gameIDToGameKVStore.put(newPlay.getID(), newPlay);
                        pairTournamentPlayersKVStore.delete(newPlay.getID());
                        this.ctx.forward(newPlay.getID(), newPlay);
                    }
                } else {
                    pairTournamentPlayersKVStore.put(tournamentPlay.getTournamentID(), tournamentPlay);
                }
            } else {
                logger.error("Failed to add " + newPlayer.getCreatedBy() + " to tournament " + tournamentPlay.getTournamentID());
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
