package authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Order(Ordered.HIGHEST_PRECEDENCE)
@Service("redisService")
@Slf4j
public class RedisService {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> template;

    @Value("${DEFAULT_EXPIRATION_TIME_MIN}")
    private int defaultExpirationTimeMin;


    public RedisService(ObjectMapper mapper, RedisTemplate<String, Object> template) {
        this.mapper = mapper;
        this.template = template;
    }

    public synchronized List<String> getKeys(final String pattern) {
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        Set<String> redisKeys = template.keys(pattern);
        if (redisKeys != null) {
            return new ArrayList<>(redisKeys);
        } else {
            return new ArrayList<>();
        }
    }

    public synchronized Object getValue(final String key, Class clazz) {
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        Object obj = template.opsForValue().get(key);
        return mapper.convertValue(obj, clazz);
    }

    public void setValue(final String key, final Object value) {
        setValue(key, value, TimeUnit.MINUTES, defaultExpirationTimeMin, false);
    }

    public void setValue(final String key, final Object value, boolean marshal) {
        setValue(key, value, TimeUnit.MINUTES, defaultExpirationTimeMin, marshal);
    }


    public void setValue(final String key, final Object value, TimeUnit unit, long timeout, boolean marshal) {
        if (marshal) {
            template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
            template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        } else {
            template.setHashValueSerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
        }
        template.opsForValue().set(key, value);
        // set a expire for a message
        template.expire(key, timeout, unit);
    }

}
