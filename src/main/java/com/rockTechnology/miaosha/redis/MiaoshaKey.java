package com.rockTechnology.miaosha.redis;

public class MiaoshaKey extends BasePrefix{


	public MiaoshaKey(String prefix) {
		super(prefix);
	}

	public MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static MiaoshaKey isGoodsOver = new MiaoshaKey("igo");
	public static MiaoshaKey getMiaoshaPath = new MiaoshaKey( 60, "mp");


}
