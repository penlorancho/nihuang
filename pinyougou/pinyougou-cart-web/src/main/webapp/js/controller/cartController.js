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


    //获取当前用户的地址列表
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault=='1') {
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }

    //选择地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }

    //判断某地址对象是否是选中的对象
    $scope.isSelected=function (address) {
        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order={paymentType:'1'};

    //选择支付类型
    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }

    //提交订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                //alert(response.message)
                if (response.success) {
                    //页面跳转
                    if ($scope.order.paymentType == '1') {
                        //如果是微信支付，跳转到支付页面
                        location.href="pay.html";
                    } else {
                        //如果是货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }
                } else {
                    //弹出失败信息，也可以跳转到提示页面
                    alert(response.message);
                }
            }
        )
    }

})