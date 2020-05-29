package gm.transformer;

import message.DefaultKafkaMessage;
import message.completed.CompletedTournamentMessage;
import message.created.PlayMessage;
import message.created.TournamentPlayMessage;
import message.queue.JoinTournamentQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class JoinTournamentTransformer implements Transformer<String, JoinTournamentQueueMessage, KeyValue<String, DefaultKafkaMessage>> {
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
    public KeyValue<String, DefaultKafkaMessage> transform(String key, JoinTournamentQueueMessage newPlayer) {
        TournamentPlayMessage tournament = pairTournamentPlayersKVStore.get(newPlayer.getTournamentID());

        if (tournament == null) {
            logger.error("Tried to join a non existing tournament: " + newPlayer.toString());
        } else {
            if (tournament.addPlayer(newPlayer)) {
                logger.info("Added " + newPlayer.toString() + " to tournament " + tournament.getTournamentID());
                if (tournament.isFull()) {
                    List<String> players = new ArrayList<>(tournament.getPlayerUsernames());
                    players.sort(String::compareToIgnoreCase);
                    int rounds = players.size() / 4;

                    //If the tournament is over just delete the tournament message and update the completed plays topic
                    if (rounds == 1) {
                        CompletedTournamentMessage finalMsg = new CompletedTournamentMessage(tournament);
                        this.ctx.forward(finalMsg.getId(), new DefaultKafkaMessage(finalMsg, CompletedTournamentMessage.class.getCanonicalName()));
                        logger.info(String.format("transform: Finished tournament [%s].", finalMsg.toString()));
                        pairTournamentPlayersKVStore.delete(tournament.getTournamentID());
                        return null;
                    }

                    //Match players into pairs (similar to a practices play)
                    Iterator<String> playerIter = players.iterator();
                    while (playerIter.hasNext()) {
                        String p1 = playerIter.next();
                        String p2 = playerIter.next();
                        PlayMessage newPlay = new PlayMessage(tournament, p1, p2, rounds, p1, p2, String.valueOf(LocalDateTime.now()));
                        userToGameKVStore.put(p1, newPlay);
                        userToGameKVStore.put(p2, newPlay);
                        gameIDToGameKVStore.put(newPlay.getID(), newPlay);
                        logger.info(String.format("transform: Forwarding play [%s].", newPlay.toString()));
                        this.ctx.forward(newPlay.getID(), new DefaultKafkaMessage(newPlay, PlayMessage.class.getCanonicalName()));
                    }

                    //Create a new smaller tournament with the same tournamentID and half the participants
                    tournament.getPlayerUsernames().clear();
                    tournament.setRemainingSlots(players.size() / 2);
                    if (tournament.getAllPlayers() == null){
                        tournament.setAllPlayers(players);
                    }

                    pairTournamentPlayersKVStore.put(tournament.getTournamentID(), tournament);
                    logger.info(String.format("transform: Advancing tournament with id=[%s] and remaining slots=[%d].",
                            tournament.getTournamentID(), tournament.getRemainingSlots()));

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
