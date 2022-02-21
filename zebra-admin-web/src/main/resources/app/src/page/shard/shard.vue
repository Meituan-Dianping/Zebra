<template>
  <Row>
    <Col span="24">
    <!-- title -->
    <div class="ivu-card">
      <div class="ivu-card-body" style="padding: 12px;width:100%;line-height: 12px;position:relative;">
        <Breadcrumb>
          <BreadcrumbItem to="/">
            <Icon type="ios-home-outline"></Icon>
            Home
          </BreadcrumbItem>
          <BreadcrumbItem to="/shardOverview">
            RuleName概览
          </BreadcrumbItem>
          <BreadcrumbItem>{{ruleName}}</BreadcrumbItem>
        </Breadcrumb>
      </div>
    </div>

    <div class="router_container" style="margin-top: 10px">
      <router-view></router-view>
    </div>

    </Col>
  </Row>
</template>

<script>
  export default {

    watch:{
      '$route' (to, from) {
        this.ruleName = to.params.ruleName;
      }
    },
    computed:{
      currentMenu() {
        let path = this.$route.path;
        let index = path.lastIndexOf("/");
        return path.substr(index + 1, path.length);
      }
    },
    data: function () {
      return {
        theme: 'primary',
        ruleName: this.$route.params.ruleName
      }
    },
    methods:{
      select(name) {
        this.currentMenu = name;
        let ruleName = this.$route.params.ruleName;
        this.$router.push("/shard/" + ruleName + "/" + name);
      }
    }
  }
</script>

<style scoped>
  .ivu-menu-horizontal {
    height: 36px;
    line-height: 36px;
  }

  .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-item-active, .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-item:hover, .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-submenu-active, .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-submenu:hover {
    color: #2b85e4;
    font-size: 13px;
    background : #fff;
  }
</style>
