package sg.edu.nus.iss.ThemeParkQueueTime.repo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ListRepo {

    @Autowired
    private RedisTemplate<String, Object> template;

    public void setList(String key, Object value, int index) {
        template.opsForList().set(key, index, value);
    }

    public void addToBackList(String key, Object value) {
        template.opsForList().rightPush(key, value);
    }

    public void addToFrontList(String key, Object value) {
        template.opsForList().leftPush(key, value);
    }

    public void addToBackListAll(String key, Object value) {
        template.opsForList().rightPushAll(key, value);
    }

    public List<Object> getList(String key) {
        return template.opsForList().range(key, 0, -1); //key,start, end
    }

    public void removeFromList(String key, Object value) {
        template.opsForList().remove(key, 0, value);
    }

    public Long sizeOfList(String key) {
        return template.opsForList().size(key);
    }

    public Long indexOfList(String key, Object value) {
        return template.opsForList().indexOf(key, value);
    }

    public Object getValueAtIndex(String key, int index) {
        return template.opsForList().index(key, index);
    }

    public void deleteList(String key) {
        template.delete(key);
    }
}
