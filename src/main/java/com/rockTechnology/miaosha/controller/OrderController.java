package com.rockTechnology.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rockTechnology.miaosha.domain.MiaoshaUser;
import com.rockTechnology.miaosha.domain.OrderInfo;
import com.rockTechnology.miaosha.redis.RedisService;
import com.rockTechnology.miaosha.result.CodeMsg;
import com.rockTechnology.miaosha.result.Result;
import com.rockTechnology.miaosha.service.GoodsService;
import com.rockTechnology.miaosha.service.MiaoshaUserService;
import com.rockTechnology.miaosha.service.OrderService;
import com.rockTechnology.miaosha.vo.GoodsVo;
import com.rockTechnology.miaosha.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;
	
    @RequestMapping("/detail")
    @ResponseBody
	//todo user判断未空的拦截器
    public Result<OrderDetailVo> info(Model model,MiaoshaUser user,
    		@RequestParam("orderId") long orderId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	OrderInfo order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return Result.error(CodeMsg.ORDER_NOT_EXIST);
    	}
    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);
    	return Result.success(vo);
    }
    
}
