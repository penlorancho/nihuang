package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 80000)
    private CartService cartService;

    /**
     * 从cookie中提取购物车
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);

        //不管有没有登录，都从cookie中获取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString==null||cartListString.equals("")) {
            cartListString="[]";
        }
        List<Cart> cartListCookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")) {
            //如果未登录
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");
            /*String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartListString==null||cartListString.equals("")) {
                cartListString="[]";
            }
            List<Cart> cartListCookie = JSON.parseArray(cartListString, Cart.class);*/
            return cartListCookie;
        } else {
            //如果已登录
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if (cartListCookie.size()>0) {
                //判断本地购物车中存在数据
                //得到合并后的购物车
                List<Cart> cartList = cartService.mergeCartList(cartListCookie, cartListFromRedis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username,cartList);
                //本地购物车清除
                CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车");
                return cartListFromRedis;
            }
            return cartListFromRedis;
        }

    }

    /**
     * 添加商品到购物车列表
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    //@CrossOrigin(origins="http://localhost:9105",allowCredentials ="true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //可以访问的域(当此方法不需要操作cookie)
        response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
        //如果方法中要操作cookie，必须加上这句
        response.setHeader("Access-Control-Allow-Credentials", "true");
        //当前登录人账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+name);
        try {
            //从cookie中提取购物车
            List<Cart> cartList = findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (name.equals("anonymousUser")) {
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
                System.out.println("向cookie存购物车");
            } else {
                cartService.saveCartListToRedis(name,cartList);
            }
            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }
    }

}
