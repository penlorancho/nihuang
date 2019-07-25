package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 600000)
    private WeixinPayService weixinPayService;

    @Reference(timeout = 600000)
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/creatNative")
    public Map creatNative(){
        //IdWorker idWorker = new IdWorker();
        //return weixinPayService.creatNative(idWorker.nextId()+"","1");
        //1.获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.从redis中获取支付日志
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        if (seckillOrder!=null){
            //3.调用微信支付接口
            return weixinPayService.creatNative(seckillOrder.getId()+"",(long)(seckillOrder.getMoney().doubleValue()*100)+"");
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int x = 0;
        while(true){
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if (map==null) {
                result = new Result(false,"支付发生错误");
                break;
            }
            if (map.get("trade_state") != null && map.get("trade_state").equals("SUCCESS")) {
                result = new Result(true,"支付成功");
                //orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            System.err.println("计时器:"+x);
            if (x>=100) {
                result = new Result(false,"二维码超时");
                Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
                //关闭支付
                if (payResult==null&&"FAIL".equals(payResult.get("result_code"))) {
                    if ("ORDERPAID".equals(payResult.get("err_code"))) {
                        //保存订单
                        result = new Result(true,"支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                    }
                }
                //删除订单
                if (result.isSuccess()==false) {
                    seckillOrderService.deleteOrderRromRedis(username,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }

}
