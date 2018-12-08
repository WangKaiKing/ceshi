package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptors.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "ly:cart:uid:";

    public void addCart(Cart cart) {



        UserInfo userInfo = LoginInterceptor.getUserInfo();

        cart.setUserId(userInfo.getId());
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> ops =
                redisTemplate.boundHashOps(KEY_PREFIX+userInfo.getId());
        System.out.println("ops = " + ops.toString());

        Object result = ops.get(cart.getSkuId() + "");

        if (null == result) {
            //没有存过值
            ops.put(cart.getSkuId() + "", JsonUtils.serialize(cart));
        } else {
            //已经存储过了,需要把原来的数量加上现在的数量
            Cart storeCart = JsonUtils.parse(result.toString(),Cart.class);

            storeCart.setNum(storeCart.getNum()+cart.getNum());

            ops.put(storeCart.getSkuId()+"",JsonUtils.serialize(cart));

            System.out.println(JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        BoundHashOperations<String, Object, Object> ops =
                redisTemplate.boundHashOps(KEY_PREFIX+userInfo.getId());

        List<Object> values = ops.values();

        List<Cart> carts = JsonUtils.parseList(values.toString(), Cart.class);

        return carts;
    }

    public void decrementNum(Long skuId) {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        BoundHashOperations<String, Object, Object> ops =
                redisTemplate.boundHashOps(KEY_PREFIX+userInfo.getId());

        Cart storeCart = JsonUtils.parse(ops.get(skuId + "").toString(), Cart.class);

        storeCart.setNum(storeCart.getNum()-1);

        ops.put((storeCart.getSkuId()+""),JsonUtils.serialize(storeCart));
    }

    public void incrementNum(Long skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        BoundHashOperations<String, Object, Object> ops =
                redisTemplate.boundHashOps(KEY_PREFIX+userInfo.getId());

        Cart storeCart = JsonUtils.parse(ops.get(skuId + "").toString(), Cart.class);

        storeCart.setNum(storeCart.getNum()+1);

        ops.put((storeCart.getSkuId()+""),JsonUtils.serialize(storeCart));
    }


    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getUserInfo();

        String key = KEY_PREFIX + user.getId();

        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(key);

        ops.delete(skuId);
    }
}
