package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("执行了秒杀商品增量更新任务调度"+new Date());
        List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsIdList);
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过的商品
        criteria.andStockCountGreaterThan(0);//库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());//结束时间大于等于当前时间
        //有个bug，没有考虑缓存中存在但数据库中已被删掉的情况
        if (goodsIdList!=null&&goodsIdList.size()>0) {
            criteria.andIdNotIn(goodsIdList);//查询缓存中不存在的商品id集合
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //将列表数据装入缓存
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            System.out.println("增量更新秒杀商品id:"+seckillGoods.getId());
        }
        System.out.println("---end---");
    }


    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        //查询出缓存中的数据，判断每条数据的过期日期与当前日期的大小
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        System.out.println("执行了清除秒杀商品的任务"+new Date());
        for (TbSeckillGoods seckillGood : seckillGoods) {
            if (seckillGood.getEndTime().getTime()<System.currentTimeMillis()) {
                //同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                //清除缓存
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("秒杀商品"+seckillGood.getId()+"已过期");
            }
        }
        System.out.println("执行了清除过期秒杀商品的任务---end---");
    }

}
