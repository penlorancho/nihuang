package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service(timeout = 600000)
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 生成支付二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map creatNative(String out_trade_no, String total_fee) {
        //1.封装参数
        Map<String,String> param = new HashMap();
        param.put("appid",appid);//公众账号id
        param.put("mch_id",partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");
        param.put("out_trade_no",out_trade_no);//交易订单号
        param.put("total_fee",total_fee);//金额（分）
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://www.itcast.com");
        param.put("trade_type","NATIVE");//交易类型
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数:"+paramXml);
            //2.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("微信返回结果:"+mapResult);
            Map map = new HashMap();
            map.put("code_url",mapResult.get("code_url"));//生成支付二维码的链接
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 检测支付状态（所使用的URL接口跟生成二维码的接口是不同的，注意区别）
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1.封装参数
        Map param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str",WXPayUtil.generateNonceStr());
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发出请求
            //检测支付状态时，所使用的URL接口跟生成二维码的接口是不同的，注意区别
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API查询结果:"+xmlResult);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 支付关闭
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {
        //1.封装参数
        Map param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str",WXPayUtil.generateNonceStr());
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发出请求
            //检测支付状态时，所使用的URL接口跟生成二维码的接口是不同的，注意区别
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API查询结果:"+xmlResult);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
