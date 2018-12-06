let axios = require('axios')
exports.install = function (Vue, options) {

  //获取环境变量
  Vue.prototype.initEnv = function () {
    let _this = this
    axios
      .get('/i/system/env')
      .then(function (response) {
        let envDto = response.data
        _this.$store.state.envs = envDto.envs

        var env = window.localStorage.getItem('env')
        let currentEnv = env && env != 'undefined' ? env : envDto.currentEnv

        for (let i = 0; i < envDto.envs.length; ++i) {
          if (currentEnv === envDto.envs[i]) {
            _this.$store.state.currentEnv = envDto.envs[i]
            break
          }
        }
        _this.$store.state.init = true
      })
      .catch(function (e) {
        console.log(e)
        _this.$Notice.error({title: '初始化环境信息失败', desc: e.message})
      })
  }



  Vue.prototype.validateParams = function (refName) {
    if (refName == null || refName.trim() == '') {
      return false
    }

    let _this = this
    //validate
    let flag = true
    this.$refs[refName].validate(valid => {
      if (!valid) {
        flag = false
        _this.$Notice.error({
          title: '验证失败'
        })
      }
    })
    return flag
  }

  Vue.prototype.trim = function (str) {
    return str.replace(/(^\s*)|(\s*$)/g, '')
  }
}
