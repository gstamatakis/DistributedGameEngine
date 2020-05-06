package ui.kafka;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import message.RegionWithClicks;

import java.util.function.BiFunction;


@SpringBootApplication
public class KafkaStreamsGlobalKTableJoin {
    @Autowired
    private InteractiveQueryService interactiveQueryService;

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsGlobalKTableJoin.class, args);
    }

    public static class KStreamToTableJoinApplication {
        @Bean
        public BiFunction<KStream<String, Long>, KTable<String, String>, KStream<String, Long>> processClicks() {
            return (userClicksStream, userRegionsTable) -> userClicksStream
                    .leftJoin(userRegionsTable,
                            (clicks, region) -> new RegionWithClicks(region == null ? "UNKNOWN" : region, clicks),
                            Joined.with(Serdes.String(), Serdes.Long(), null))
                    .map((user, regionWithClicks) -> new KeyValue<>(regionWithClicks.getRegion(), regionWithClicks.getClicks()))
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                    .reduce(Long::sum)
                    .toStream();
        }

    }
}
