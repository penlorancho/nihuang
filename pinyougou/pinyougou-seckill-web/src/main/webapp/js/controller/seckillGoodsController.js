app.controller('seckillGoodsController',function ($scope,seckillGoodsService,$location,$interval) {

    //读取列表数据绑定到表单中
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list=response;
            }
        )
    }

    //查询商品
    $scope.findOneFromRedis=function () {
        //获取参数id（跳转页面时传递）
        var id = $location.search()['id'];
        seckillGoodsService.findOneFromRedis(id).success(
            function (response) {
                $scope.entity=response;
                //倒计时开始
                //获取从截止日期到当前日期的时间
                allSecond=Math.floor((new Date($scope.entity.endTime).getTime()-(new Date()).getTime())/1000);
                time=$interval(function () {
                    allSecond=allSecond-1;
                    $scope.timeString=convertTimeString(allSecond);
                    if (allSecond <= 0) {
                        $interval.cancel(time);
                    }
                },1000);
            }
        )
    }


    //转换秒为天小时分钟秒的格式
    convertTimeString=function (allSecond) {
        var days = Math.floor(allSecond/(60*60*24));//天数
        var hours = Math.floor((allSecond-days*60*60*24)/(60*60));//小时数
        var minutes = Math.floor((allSecond-days*60*60*24-hours*60*60)/60);//分钟数
        var seconds = allSecond-days*60*60*24-hours*60*60-minutes*60;
        var timeString = "";
        if (days > 0) {
            timeString = days + "天";
        }
        return timeString + hours +"时:" + minutes + "分:" + seconds +"秒";
    }


    //提交订单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success) {
                    alert("抢购成功，请在五分钟内完成下单");
                    location.href="pay.html";//跳转到支付页面
                } else {
                    alert(response.message);
                }
            }
        )
    }

})