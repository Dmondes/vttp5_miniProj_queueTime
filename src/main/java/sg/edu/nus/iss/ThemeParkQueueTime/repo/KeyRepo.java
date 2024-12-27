package sg.edu.nus.iss.ThemeParkQueueTime.repo;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KeyRepo {

    @Autowired
    private RedisTemplate<String, Object> template;

    public void setKey(String key, Object value) {
        template.opsForValue().set(key, value);
    }

    public Object getKey(String key) {
        return template.opsForValue().get(key);
    }

    public void incrementKey(String key) {
        template.opsForValue().increment(key);
    }

    public void incrementKeyBy(String key, int amount) {
        template.opsForValue().increment(key, amount);
    }

    public void decrementKey(String key) {
        template.opsForValue().decrement(key);
    }

    public void decrementKeyBy(String key, int amount) {
        template.opsForValue().decrement(key, amount);
    }

    public void deleteKey(String key) {
        template.delete(key);
    }

    public Set<String> keys(String pattern) {
        return template.keys(pattern);
    }

    public void deleteKeys(Set<String> keys) {
        template.delete(keys);
    }

    public boolean hasKey(String key) {
        return template.hasKey(key);
    }
}
