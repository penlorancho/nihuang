 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
					itemCatService.findTypeTemplate($scope.entity.typeId).success(
						function (response) {
							$scope.typeEntity = response;
							$scope.typeEntity = JSON.parse($scope.typeEntity);
						}
					)
			}
		);				
	}

	/*$scope.findTypeTemplate=function(id) {
		itemCatService.findTypeTemplate(id).success(
			function (response) {
				$scope.typeEntity = response;
				$scope.typeEntity = JSON.parse($scope.typeEntity);
			}
		)
	}*/
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改
			$scope.entity.typeId=$scope.typeEntity.id;
		}else{
			$scope.entity.parentId=$scope.parentId;
			$scope.entity.typeId=$scope.typeEntity.id;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
					$scope.findByParentId($scope.parentId);//重新加载
		        	//$scope.reloadList();//重新加载
					alert(response.message);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	/*$scope.dele=function(){
		for (var i = 0; i <= $scope.selectIds.length; i++) {
			itemCatService.findByParentId($scope.selectIds[i]).success(
				function (response) {
					if (response == null) {
						itemCatService.dele( $scope.selectIds[i] ).success(
							function(response){
								if(response.success){
									//$scope.reloadList();//刷新列表
									$scope.findByParentId($scope.parentId);//重新加载
									$scope.selectIds=[];
								}
							}
						);
					} else {
						alert("分类ID"+response.id+"号有子类选项，请询问管理员后再进行操作");
					}
				}
			)
		}
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);
	}*/

	 //批量删除
	 $scope.dele=function(){
		 //获取选中的复选框
		 itemCatService.dele( $scope.selectIds ).success(
			 function(response){
				 if(response.success){
					 //$scope.reloadList();//刷新列表
					 $scope.findByParentId($scope.parentId);//重新加载
					 $scope.selectIds=[];
					 alert(response.message);
				 } else {
				 	 $scope.haveChild=response.haveChild;
				 	 if ($scope.haveChild!=null||$scope.haveChild.size()>0){
						 $scope.findByParentId($scope.parentId);//重新加载
						 $scope.selectIds=[];
						 alert("id为"+response.haveChild+"的"+response.message);
					 } else {
						 $scope.findByParentId($scope.parentId);
						 $scope.selectIds=[];
						 alert(response.message)
					 }
				 }
			 }
		 );
	 }
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.parentId=0;//上级Id
	//根据上级查询商品分类列表
	$scope.findByParentId=function (parentId) {
		$scope.parentId=parentId;//记录上级Id
		itemCatService.findByParentId(parentId).success(
			function (response) {
				$scope.list=response;
			}
		)
	}

	$scope.grade=1;//当前级别
	//设置级别
	$scope.setGrade=function (value) {
		$scope.grade=value;
	}

	$scope.selectList=function (p_entity) {
		if ($scope.grade == 1) {
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
		if ($scope.grade == 2) {
			$scope.entity_1=p_entity;
			$scope.entity_2=null;
		}
		if ($scope.grade == 3) {
			$scope.entity_2=p_entity;
		}
		$scope.findByParentId(p_entity.id);
	}

	$scope.typeList = {
		data: [],
		placeholder: '尚无数据'
	};

	//读取模板列表
	$scope.findTypeTemplateList=function () {
		itemCatService.selectOptionList().success(
			function (response) {
				$scope.typeList.data=response;
				//$scope.typeList.placeholder = '加载完毕'
			}
		)
	}

	/*$timeout(function () {
		//调用读取模板列表的方法获取列表数据
		findTypeTemplateList();
		$scope.typeList.placeholder = '加载完毕'
	}, 1000);*/

    
});	
