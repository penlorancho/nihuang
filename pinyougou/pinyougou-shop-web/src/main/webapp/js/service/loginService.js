app.service('loginService',function ($http) {

    //显示当前用户名
    this.loginName=function () {
        return $http.get('../login/name.do')
    }
})