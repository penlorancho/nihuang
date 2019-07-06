app.controller('baseController',function ($scope) {

    //分页控件配置 currentPage:当前页 totalItems:总记录数 itemsPerPage:当前页记录数 perPageOptions:分页选项
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    //刷新列表
    $scope.reloadList = function(){
        //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
    };


    //用户勾选的品牌id集合(复选框)
    $scope.selectIds=[];

    $scope.updateSelection = function ($event,id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);//push向集合中添加元素
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);//参数1:移除的位数  参数2：移除的个数
        }
    }

    $scope.jsonToString=function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i>0) {
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    }

    //在list集合中根据某key的值查询对象
    $scope.searchObjectByKey=function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }
});