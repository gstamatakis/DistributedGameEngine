package kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.BiFunction;


@SpringBootApplication
public class KafkaStreamsGlobalKTableJoin {

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsGlobalKTableJoin.class, args);
    }

    public static class KStreamToTableJoinApplication {
        @Bean
        public BiFunction<KStream<String, Long>, KTable<String, String>, KStream<String, Long>> process() {
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

    private static final class RegionWithClicks {

        private final String region;
        private final long clicks;

        public RegionWithClicks(String region, long clicks) {
            if (region == null || region.isEmpty()) {
                throw new IllegalArgumentException("region must be set");
            }
            if (clicks < 0) {
                throw new IllegalArgumentException("clicks must not be negative");
            }
            this.region = region;
            this.clicks = clicks;
        }

        public String getRegion() {
            return region;
        }

        public long getClicks() {
            return clicks;
        }

    }
}
