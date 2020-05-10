package gm.transformer;

import message.created.PlayMessage;
import message.created.TournamentPlayMessage;
import message.requests.RequestCreateTournamentMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

@SuppressWarnings("unchecked")
public class CreateTournamentTransformer implements Transformer<String, RequestCreateTournamentMessage, KeyValue<String, String>> {
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
    public KeyValue<String, String> transform(String key, RequestCreateTournamentMessage msg) {
        TournamentPlayMessage newPlay = new TournamentPlayMessage(msg);
        pairTournamentPlayersKVStore.put(newPlay.getTournamentID(), newPlay);
        return null;
    }

    @Override
    public void close() {

    }
}
