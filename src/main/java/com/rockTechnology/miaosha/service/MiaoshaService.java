package com.rockTechnology.miaosha.service;

import com.rockTechnology.miaosha.domain.MiaoshaOrder;
import com.rockTechnology.miaosha.domain.MiaoshaUser;
import com.rockTechnology.miaosha.domain.OrderInfo;
import com.rockTechnology.miaosha.redis.MiaoshaKey;
import com.rockTechnology.miaosha.redis.RedisService;
import com.rockTechnology.miaosha.util.MD5Util;
import com.rockTechnology.miaosha.util.UUIDUtil;
import com.rockTechnology.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.util.HashMap;

@Service
public class MiaoshaService {
    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    public long getMiaoshaResult(Long id, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(id, goodsId);
        if (order != null){
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1;
            }else {
                return 0;
            }
        }
    }

    @Transactional //用于标记事务
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        if (!success){
            setGoodsOver(goods.getId());
            return null;
        }
        //order_info maiosha_order
        return orderService.createOrder(user, goods);
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(Long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
    }

    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || path == null){
            return false;
        }
        String oldPath = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);

        return path.equals(oldPath);
    }

    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

}
