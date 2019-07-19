package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(timeout = 80000)
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuID查询商品明细SKU对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null) {
            throw new RuntimeException("商品不存在");
        }
        //操作时间差导致的数据该改变
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态不合法");
        }

        //2.根据SKU对象查询商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart==null) {
            //4.如果购物车不存在该商家的id
            //4.1创建一个新的购物车对象
            cart=new Cart();
            cart.setSellerId(sellerId);//商家id
            cart.setSellerName(item.getSeller());//商家名称
            List<TbOrderItem> orderItemList = new ArrayList<>();//创建购物车明细列表
            //创建新的购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将新的购物车对象添加到购物车列表中
            cartList.add(cart);
        } else {
            //5.如果购物车中存在该商家的id
            //判断该商品是否在购物车的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem==null) {
                //5.1如果不存在，创建新的购物车明细对象，并把它添加到该购物车的明细列表中
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2如果存在，在原有的数量上增加数量，并更新金额
                orderItem.setNum(orderItem.getNum()+num);//修改数量
                //金额new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum())
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
                //当购物车中明细的数量小于等于0，移除此明细
                if (orderItem.getNum()<=0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //当购物车的明细数量为0，在购物车中移除此明细
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }

        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车："+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null) {
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中保存购物车："+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem oderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, oderItem.getItemId(), oderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家ID在购物车列表中查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }


    /**
     * 创建购物车明细对象
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        //创建新的购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        //new BigDecimal(item.getPrice().doubleValue()*num)
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }


    /**
     * 根据skuID在购物车明细列表查询购物车明细对象
     * @param orderItemList
     * @param itemId
     * @return
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

}
