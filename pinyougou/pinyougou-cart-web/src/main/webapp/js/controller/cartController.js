//购物车控制层
app.controller('cartController',function ($scope, cartService) {

    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList=response;
                //sum();
                $scope.totalValue=cartService.sum($scope.cartList);
            }
        )
    }

    //数量增加
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();//刷新列表
                } else {
                    alert(response.message)
                }
            }
        )
    }

    //求合计
    /*sum=function () {
        $scope.totalNum=0;
        $scope.totalMoney=0;
        for (var i = 0; i < $scope.cartList.length; i++) {
            var cart = $scope.cartList[i];
            for (var j = 0; j < cart.orderItemList.length; j++) {
                var orderItem = cart.orderItemList[j];
                $scope.totalNum+=orderItem.num;//累计数量
                $scope.totalMoney+=orderItem.totalFee;//累计金额
            }
        }
    }*/

})