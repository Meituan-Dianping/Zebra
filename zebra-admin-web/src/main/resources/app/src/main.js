// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
/*vue*/
import Vue from 'vue'
/* iview */
import iView from 'iview'
import 'iview/dist/styles/iview.css'
/*vue-router*/
import VueRouter from 'vue-router'
/*自己的文件*/
import App from './App.vue'
/*目录*/
import route from './router/route.js'

import Vuex from 'vuex'

import hljs from 'highlight.js'
import 'highlight.js/styles/googlecode.css'

/*自己改的一些css*/
import '../static/css/mystyle.css'

import Util from './js/util'

import echarts from 'echarts'

// 光引用不成，还得使用
Vue.use(VueRouter)
Vue.use(iView)
Vue.use(Vuex)
Vue.use(Util)
Vue.prototype.$echarts = echarts

Vue.prototype.makehl = function(str) {
    var value = hljs.highlightAuto(str);
  return value.value
}

const router = new VueRouter({
  routes: route
})

const store = new Vuex.Store({
  state:{
    init : false,
    currentEnv : null,
    envs : [],
    user:'',
    staredJdbcrefs:[],
    staredShards:[],
    staredJdbcrefDtos:[],
    staredShardDtos:[]
  },
  getters: {},
  mutations: {

  },
  actions: {}
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store: store,
  router,
  render: h => h(App)
})
