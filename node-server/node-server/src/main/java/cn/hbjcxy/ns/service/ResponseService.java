package cn.hbjcxy.ns.service;

import cn.hbjcxy.ns.configurer.general.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class ResponseService extends BaseService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String getResponse(String tranceId) throws InterruptedException {
        for (int i = 0; i < 10 * (1000 / 200); i++) {
            String response = redisTemplate.opsForValue().get(tranceId);
            if (StringUtils.hasText(response)) return response;
            TimeUnit.MILLISECONDS.sleep(200);
        }
        return null;
    }

    public void putResponse(String tranceId, String response) {
        redisTemplate.opsForValue().set(tranceId, response, 15, TimeUnit.SECONDS);
    }

}
