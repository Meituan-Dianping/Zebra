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
          <BreadcrumbItem to="/jdbcrefOverview">
            jdbcref概览
          </BreadcrumbItem>
          <BreadcrumbItem>{{jdbcref}}</BreadcrumbItem>
        </Breadcrumb>
      </div>
    </div>

    <Menu mode="horizontal" :theme="theme" style="margin-top: 8px;margin-bottom: 8px">
      <MenuItem name="info">
        <Icon type="ios-paper"></Icon>
        基本信息
      </MenuItem>
    </Menu>

    <div class="router_container">
      <router-view></router-view>
    </div>

    </Col>
  </Row>
</template>

<script>
  export default {
    data: function () {
      return {
        theme: 'primary',
        jdbcref: this.$route.params.jdbcref
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
