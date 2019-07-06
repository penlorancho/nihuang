app.controller('brandController',function($scope,$controller,brandService){

    $controller('baseController',{$scope:$scope});

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    /*//分页控件配置 currentPage:当前页 totalItems:总记录数 itemsPerPage:当前页记录数 perPageOptions:分页选项
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
    };*/

    //分页方法
    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        )
    };

    //新增品牌(add-->save)
    $scope.save = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object = brandService.update($scope.entity);
        } else {
            object = brandService.add($scope.entity)
        }
        object.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                } else {
                    alert(response.message)
                }
            }
        )
    };

    /*$scope.save = function () {
        var methodName = 'add';
        if ($scope.entity.id != null) {
            methodName = 'update';
        }
        $http.post('../brand/'+methodName+'.do',$scope.entity).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新列表
                    } else {
                        alert(response.message)
                    }
                }
        )
    };*/

    //查询实体
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    };
/*
    //用户勾选的品牌id集合(复选框)
    $scope.selectIds=[];

    $scope.updateSelection = function ($event,id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);//push向集合中添加元素
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);//参数1:移除的位数  参数2：移除的个数
        }
    }*/

    //删除品牌
    $scope.dele=function () {
        if (confirm('确定要删除吗？')) {

            brandService.dele($scope.selectIds).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新列表
                    } else {
                        alert(response.message)
                    }
                }
            )
        }
    };

    //防止searchEntity刚开始的时候为null
    $scope.searchEntity={};
    //条件查询
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        )
    };

    //防止searchEntity刚开始的时候为null
    /*$scope.searchEntity={};
    //条件查询
    $scope.search=function (page,size) {
        $http.post('../brand/search.do?page='+page+'&size='+size,$scope.searchEntity).success(
                function (response) {
                    $scope.list=response.rows;//显示当前页数据
                    $scope.paginationConf.totalItems=response.total;//更新总记录数
                }
        )
    };*/

});