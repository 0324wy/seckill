package com.rockTechnology.miaosha.service;

import com.rockTechnology.miaosha.dao.GoodsDao;
import com.rockTechnology.miaosha.dao.UserDao;
import com.rockTechnology.miaosha.domain.MiaoshaGoods;
import com.rockTechnology.miaosha.domain.User;
import com.rockTechnology.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * goods的service层
 */
@Service //说明是Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao; //注解引入goodsDao

    public List<GoodsVo> listGoodsVo(){
        List<GoodsVo> goodsVos = goodsDao.listGoodsVo();
        return goodsVos;
    }


    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int s = goodsDao.reduceStock(g);
        return s > 0;//todo:>=0?
    }

}
