package gm.transformer;

import message.created.PlayMessage;
import message.created.TournamentPlayMessage;
import message.queue.JoinTournamentQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class JoinTournamentTransformer implements Transformer<String, JoinTournamentQueueMessage, KeyValue<String, PlayMessage>> {
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
    public KeyValue<String, PlayMessage> transform(String key, JoinTournamentQueueMessage newPlayer) {
        TournamentPlayMessage tournament = pairTournamentPlayersKVStore.get(newPlayer.getTournamentID());
        if (tournament == null) {
            logger.error("Tried to join a non existing tournament: " + newPlayer.toString());
        } else {
            if (tournament.addPlayer(newPlayer)) {
                logger.info("Added " + newPlayer.toString() + " to tournament " + tournament.getTournamentID());
                if (tournament.isFull()) {
                    List<String> players = new ArrayList<>();
                    tournament.getPlayerUsernames().iterator().forEachRemaining(players::add);
                    Iterator<String> playerIter = players.iterator();
                    int div = players.size() / 4;
                    int rem = players.size() % 4;
                    while (playerIter.hasNext()) {
                        int rounds = div - 1;
                        if (rem > 0) {
                            rounds += 1;
                            rem -= 1;
                        }
                        String p1 = playerIter.next();
                        String p2 = playerIter.next();
                        PlayMessage newPlay = new PlayMessage(tournament, p1, p2, rounds);
                        userToGameKVStore.put(p1, newPlay);
                        userToGameKVStore.put(p2, newPlay);
                        gameIDToGameKVStore.put(newPlay.getID(), newPlay);
                        this.ctx.forward(newPlay.getID(), newPlay);
                    }

                    //If the tournament is over just delete the tournament message
                    //Otherwise, create a new smaller tournament with the same tournamentID and 0 participants
                    if (div == 0) {
                        pairTournamentPlayersKVStore.delete(tournament.getTournamentID());
                    } else {
                        tournament.progressTournament();
                        pairTournamentPlayersKVStore.put(tournament.getTournamentID(), tournament);
                    }
                } else {
                    //Put the tournament message back into the store
                    pairTournamentPlayersKVStore.put(tournament.getTournamentID(), tournament);
                }
            } else {
                logger.error("Failed to add " + newPlayer.getCreatedBy() + " to tournament " + tournament.getTournamentID());
            }
        }
        return null;    //Null values are dropped from stream by default
    }

    @Override
    public void close() {

    }
}
