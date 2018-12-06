//import 页面地址
import home from '../page/home/home.vue'
import jdbcrefOverview from '../page/jdbcref/jdbcrefOverview.vue'
import jdbcrefInfo from '../page/jdbcref/jdbcrefInfo.vue'
import shardOverview from '../page/shard/shardOverview.vue'
import shard from '../page/shard/shard.vue'
import shardInfo from '../page/shard/shardInfo.vue'

import envConfig from '../page/zebraconfig/envConfig.vue'
import jdbcRefMenu from '../page/jdbcref/jdbcrefMenu.vue'
// 配置路由
export default [
  {
    path: '*',
    redirect: '/'
  },
  {
    path: '/',
    redirect: '/home'
  },

  {
    path: '/home',
    component: home
  },
  {
    path: '/jdbcrefOverview',
    component: jdbcrefOverview
  },
  // {
  //   name : 'jdbcref',
  //   path : '/jdbcref/:jdbcref',
  //   component: jdbcref,
  //   children : [
  //     {path: '/' , redirect : '/jdbcref/:jdbcref/info'},
  //     {path: '/jdbcref/:jdbcref/info', component : jdbcrefInfo}
  //   ]
  // },
  {
    path: '/shardOverview',
    component: shardOverview
  },
  {
    name: 'shard',
    path: '/shard/:ruleName',
    component: shard,
    children: [
      { path: '/', redirect: '/shard/:ruleName/info' },
      { path: '/shard/:ruleName/info', component: shardInfo }
    ]
  },
  {
    name: 'jdbcref',
    path: '/jdbcref/:jdbcRef',
    component: jdbcRefMenu,
    children: [
      { path: '/', redirect: '/jdbcref/info/:jdbcRef' },
      { path: '/jdbcref/info/:jdbcRef', component: jdbcrefInfo }
    ]
  },

  {
    path: '/envConfig',
    component: envConfig
  }
  // more path
]
