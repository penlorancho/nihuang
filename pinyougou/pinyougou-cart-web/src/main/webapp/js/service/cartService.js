//购物车服务层
app.service('cartService',function ($http) {
    //购物车列表
    this.findCartList=function () {
        return $http.get('../cart/findCartList.do');
    }
    
    //添加商品到购物车
    this.addGoodsToCartList=function (itemId, num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求合计数
    this.sum=function (cartList) {
        var totalValue={totalNum:0,totalMoney:0}
        for (var i = 0; i < cartList.length; i++) {
            var cart = cartList[i];
            for (var j = 0; j < cart.orderItemList.length; j++) {
                var orderItem = cart.orderItemList[j];
                totalValue.totalNum+=orderItem.num;//累计数量
                totalValue.totalMoney+=orderItem.totalFee;//累计金额
            }
        }
        return totalValue;
    }
})