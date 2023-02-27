package com.kyxs.cloud.core.base.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author wangsf
 * @since 2023/2/25
 */
@Component
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String DELIMITER = "|";

    public boolean lock(String key,final String value,final TimeUnit timeUnit,int expire){
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis()+DELIMITER+value);
        //设置超时时间以防锁解不开
        if (success) {
            redisTemplate.expire(key,expire,timeUnit);
            return true;
        }
        String currValue = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(currValue)) {
            final String[] oldValues = currValue.split(Pattern.quote(DELIMITER));
            //缓存已经到过期时间，但是还没释放，避免造成死锁
            if (Long.parseLong(oldValues[0]) + timeUnit.toSeconds(1) <= System.currentTimeMillis()) {
                String oldValue = redisTemplate.opsForValue().getAndSet(key, System.currentTimeMillis() + DELIMITER + value);
                //这里类似CAS锁机制
                if (!StringUtils.isEmpty(oldValue)&&currValue.equals(oldValue)){
                    redisTemplate.expire(key, expire, timeUnit);
                    return true;
                }
            }
        }
        return false;
    }

    public void unlock(String key,String value) {
        String val = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(val)) {
            final String[] values = val.split(Pattern.quote(DELIMITER));
            if (values.length <= 1) {
                return;
            }
            if (value.equals(values[1])) {
                redisTemplate.delete(key);
            }
        }
    }
}
