package com.rockTechnology.miaosha.controller;

import com.rockTechnology.miaosha.rabbitmq.MQSender;
import com.rockTechnology.miaosha.rabbitmq.MiaoshaMessage;
import com.rockTechnology.miaosha.redis.GoodsKey;
import com.rockTechnology.miaosha.redis.MiaoshaKey;
import com.rockTechnology.miaosha.util.MD5Util;
import com.rockTechnology.miaosha.util.UUIDUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.rockTechnology.miaosha.domain.MiaoshaOrder;
import com.rockTechnology.miaosha.domain.MiaoshaUser;
import com.rockTechnology.miaosha.redis.RedisService;
import com.rockTechnology.miaosha.result.CodeMsg;
import com.rockTechnology.miaosha.result.Result;
import com.rockTechnology.miaosha.service.GoodsService;
import com.rockTechnology.miaosha.service.MiaoshaService;
import com.rockTechnology.miaosha.service.MiaoshaUserService;
import com.rockTechnology.miaosha.service.OrderService;
import com.rockTechnology.miaosha.vo.GoodsVo;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	MQSender mqSender;

	private HashMap<Long, Boolean> localOverMap = new HashMap<>();

	/**
	 * 系统初始化时候做的事情
	 * 将库存预加载到缓存中
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
		if (goodsVoList == null){
			return;
		}
		for (GoodsVo goodsVo : goodsVoList){
			redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
			localOverMap.put(goodsVo.getId(), false);
		}
	}

	/**
	 * QPS:1306
	 * 5000 * 10
	 * */
	/**
	 *  GET POST有什么区别？
	 * */
	@RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> miaosha(Model model,MiaoshaUser user,
									 @RequestParam("goodsId")long goodsId, @PathVariable("path")String path) {
		//判断用户是否登录
		model.addAttribute("user", user);
		if(user == null) { //todo:多个线程会不会有问题，不会的，因为访问的是不同的资源，而且没有写操作
			return Result.error(CodeMsg.SESSION_ERROR);
		}

		//验证path
		boolean check = miaoshaService.checkPath(user, goodsId, path);
		if (!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}


		//判断是否还有库存
		//收到请求，先减少库存
		boolean localOver = localOverMap.get(goodsId);
		if (localOver){
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
		if (stock <= 0){
			localOverMap.put(goodsId, true);//todo:为什么会多减，不会少减
		}

		//判断是否已经秒杀到了
		//来自缓存
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}

		//入队
		MiaoshaMessage message = new MiaoshaMessage();
		message.setUser(user);
		message.setGoodsId(goodsId);
		mqSender.sendMiaoshaMessage(message);
		return Result.success(0);



//		//判断库存
//		//来自数据库
//		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
//		int stock = goods.getStockCount();
//		if(stock <= 0) {
//			return Result.error(CodeMsg.MIAO_SHA_OVER);
//		}
//		//判断是否已经秒杀到了
//		//来自缓存
//		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//		if(order != null) {
//			return Result.error(CodeMsg.REPEATE_MIAOSHA);
//		}
//		//减库存 下订单 写入秒杀订单
//		//来自数据库
//		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//		return Result.success(orderInfo);
	}



	@RequestMapping(value = "/path", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(Model model,MiaoshaUser user,
								   @RequestParam("goodsId")long goodsId) {
		//判断用户是否登录
		model.addAttribute("user", user);
		if (user == null) { //todo:多个线程会不会有问题，不会的，因为访问的是不同的资源，而且没有写操作
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		String path = miaoshaService.createMiaoshaPath(user, goodsId);
		return Result.success(path);
	}

	@RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCode(Model model,MiaoshaUser user,
										 @RequestParam("goodsId")long goodsId) {
		//判断用户是否登录
		if (user == null) { //todo:多个线程会不会有问题，不会的，因为访问的是不同的资源，而且没有写操作
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		BufferedImage image = miaoshaService.createMiaoshaVerifyCode(user, goodsId);
		return null;
	}


	/**
	 * orderId:成功
	 * -1：秒杀失败
	 * 0：排队中
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
								   @RequestParam("goodsId")long goodsId) {
		//判断用户是否登录
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}

		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}


}