 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService,$location,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//查询实体 
	$scope.findOne=function(){
		//引入$location用于查找属性字符串所拼成的地址信息，然后从地址中获取到id
		var id = $location.search()['id'];
		if (id == null) {
			return;
		}

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//富文本框内容显示，商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//商品图片显示
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//商品扩展属性显示
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//商品规格选项
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换SKU列表中的规格对象
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.status=['未审核','已审核','审核未通过','已关闭'];

	$scope.itemCatList=[];//商品分类列表
	//查询商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0; i < response.length; i++) {
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		)
	}

    //查询一级分类列表
    $scope.selectItemCat1List=function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List=response;
            }
        )
    }

    //查询二级商品分类列表
    $scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
        if (newValue != undefined) {

            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List=response;
                }
            )
        }
    })

    //查询三级商品分类列表
    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
        if (newValue != undefined) {

            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List=response;
                }
            )
        }
    })

    //读取模板id
    $scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
        if (newValue != undefined) {

            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId=response.typeId;
                }
            )
        }
    })

    //读取模板id后读取品牌列表 扩展属性列表 规格列表
    $scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        if (newValue != undefined) {

            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate=response;//模板对象
                    $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);//品牌列表类型转换
                    //扩展属性
                    if ($location.search()['id'] == null) {
                        //修改商品列表时不执行这段代码，新增时执行
                        $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                }
            )
            //获取规格列表
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList=response;
                }
            )
        }
    })


    //判断规格与规格选项是否应该被勾选
    $scope.checkAttributeValue=function (specName,optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,'attributeName',specName);
        if (object != null) {
            if (object.attributeValue.indexOf(optionName)>=0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }


    $scope.updateStatus=function (status) {
		goodsService.updateStatus($scope.selectIds,status).success(
			function (response) {
				if (response.success) {
					$scope.reloadList();//刷新页面
					$scope.selectIds=[];
				} else {
					alert(response.message);
				}
			}
		)
	}

});	
