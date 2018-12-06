<template>
  <div>
    <div style="display: inline-block;width: 100%;">
      <Breadcrumb style="padding: 10px;background:#fff;margin-bottom: 10px;">
        <BreadcrumbItem to="/">
          <Icon type="ios-home-outline"></Icon>
          Home
        </BreadcrumbItem>
        <BreadcrumbItem>
          <Icon type="social-buffer-outline"></Icon>
          {{path}}
        </BreadcrumbItem>
      </Breadcrumb>
    </div>
      <router-view></router-view>
    </div>
</template>
<script>
  export default {
    data() {
      return {
        path: this.$route.params.jdbcRef,
        currentMenu: "/jdbcref/info",
      }
    },
    methods: {
      select(name) {
        this.currentMenu = name;
        let jdbcRef = this.$route.params.jdbcRef;
        this.$router.push(name + "/" + jdbcRef);
      },
      resetCurrentMenu(){
        this.currentMenu = "/jdbcref/info";
      }
    },
    watch: {
      '$route'(to, from) {
        let newValue = to.params.jdbcRef;
        let oldValue = from.params.jdbcRef;
        if (oldValue != newValue) {
          this.path = newValue;
          this.resetCurrentMenu();
        }
      }
    },
    created() {
      let jdbcRef = this.$route.params.jdbcRef;
      let path = this.$route.path;
      this.currentMenu = path.substr(0, path.length-jdbcRef.length-1);
    }
  }
</script>
<style scoped>
  .ivu-menu-dark {
    background: #495060
  }

  .ivu-menu-horizontal {
    height: 40px !important;
    line-height: 40px !important;
  }

  .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-item-active, .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-item:hover, .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu-active, .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu:hover, .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-item, .ivu-menu-primary.ivu-menu-horizontal .ivu-menu-submenu {
    color: #555 !important;
    background-color: #fff !important;
  }

  .ivu-menu {
    display: block ;
    margin: 0 ;
    padding: 0;
    outline: 0 ;
    top: 0 ;
    list-style: none ;
    font-size: 14px ;
    width: 100%;
    z-index: 1;
    color: #fff;
  }
</style>
