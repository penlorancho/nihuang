package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 600000)
    private WeixinPayService weixinPayService;

    @RequestMapping("/creatNative")
    public Map creatNative(){
        IdWorker idWorker = new IdWorker();
        return weixinPayService.creatNative(idWorker.nextId()+"","1");
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
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
                break;
            }
        }
        return result;
    }

}
