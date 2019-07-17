 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){

	$scope.register=function () {
	//判断两次输入的密码是否一致
		if ($scope.password != $scope.entity.password) {
			alert("两次输入的密码不一致，请重新输入");
			$scope.password="";
			$scope.entity.password="";
			return;
		}
		//新增
		userService.add($scope.entity,$scope.smsCode).success(
			function (response) {
				alert(response.message)
			}
		)
	};


	$scope.sendCode=function () {
		var reg_telephone = new RegExp("^(13[09]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$");
		if ($scope.entity.phone==null||$scope.entity.phone==""){
			alert("手机号码不能为空，请输入您的手机号码");
			return;
		}
		//防止通过前端抓包，直接获得页面地址
		/*if (!reg_telephone.test($scope.entity.phone)){
			alert("您输入的手机格式有误，请重新输入");
			return;
		}*/
		userService.sendCode($scope.entity.phone).success(
			function (response) {
				alert(response.message);
			}
		)
	}
});	
