package gm.transformer;

import message.created.TournamentPlayMessage;
import message.queue.CreateTournamentQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

@SuppressWarnings("unchecked")
public class CreateTournamentTransformer implements Transformer<String, CreateTournamentQueueMessage, KeyValue<String, String>> {
    private final String pairTournamentPlayersStore;
    private KeyValueStore<String, TournamentPlayMessage> pairTournamentPlayersKVStore;

    public CreateTournamentTransformer(String pairTournamentPlayersStore) {
        this.pairTournamentPlayersStore = pairTournamentPlayersStore;
    }

    @Override
    public void init(ProcessorContext context) {
        this.pairTournamentPlayersKVStore = (KeyValueStore<String, TournamentPlayMessage>) context.getStateStore(pairTournamentPlayersStore);
    }

    @Override
    public KeyValue<String, String> transform(String key, CreateTournamentQueueMessage msg) {
        TournamentPlayMessage newTournament = new TournamentPlayMessage(msg);
        pairTournamentPlayersKVStore.put(newTournament.getTournamentID(), newTournament);
        return null;
    }

    @Override
    public void close() {

    }
}
