let axios = require('axios')
exports.install = function (Vue, options) {

  //获取环境变量
  Vue.prototype.initEnv = function () {
    let _this = this
    axios
      .get('/i/zkConfig/getEnv')
      .then(function (response) {
        _this.$store.state.envs = response.data

        var env = window.localStorage.getItem('env')
        let currentEnv = env && env != 'undefined' ? env : envDto.currentEnv

        for (let i = 0; i < _this.$store.state.envs.length; ++i) {
          if (currentEnv === _this.$store.state.envs[i]) {
            _this.$store.state.currentEnv = _this.$store.state.envs[i]
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

  Vue.prototype.changeEnv = function(env) {
      this.$store.state.env = env;
      window.localStorage.setItem('env', env);
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
