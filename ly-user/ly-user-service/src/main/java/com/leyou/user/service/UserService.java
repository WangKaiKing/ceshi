package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Boolean check(Integer type, String data) {

        User record = new User();

        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
        }

        return this.userMapper.selectCount(record) == 0;
    }

    public void sendCode(String phone) {
        //发什么、发到哪里去？？？？

        Map<String, String> msg = new HashMap<>();

        String code = NumberUtils.generateCode(4);

        msg.put("phone", phone);
        msg.put("code", code);

//        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
        System.out.println("msg = " + msg);
        redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
    }

    public Boolean register(User user, String code) {
        //TODO 先要校验验证码，校验成功后要删除验证吗
        String storeCode = redisTemplate.opsForValue().get(user.getPhone());

        if (code.equals(storeCode)) {

            user.setCreated(new Date());

            // 生成盐
            String salt = CodecUtils.generateSalt();
            user.setSalt(salt);
            // 对密码进行加密
            user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

            this.userMapper.insertSelective(user);


            // 如果注册成功，删除redis中的code

            try {
                this.redisTemplate.delete(user.getPhone());
            } catch (Exception e) {
                logger.error("删除缓存验证码失败，code：{}", code, e);
            }
            return true;
        }
        return null;
    }

    public User login(String username, String password) {

        User record = new User();
        record.setUsername(username);

        User user = this.userMapper.selectOne(record);

        if (null==user){
            return null;
        }

        String saltPass = CodecUtils.md5Hex(password, user.getSalt());

        if (user.getPassword().equals(saltPass)){
            return user;
        }

        return null;
    }
}
