<template>
    <Row>
        <Col span="24">
        <Menu style="width: 100%;">
            <router-link to="/" style="color: white">
                <MenuItem name="home">
                    <Icon type="ios-home"></Icon>
                    主页
                </MenuItem>
            </router-link>

            <router-link to="/jdbcrefOverview" style="color: white">
                <MenuItem name="jdbcrefView">
                <Icon type="social-buffer"></Icon>
                读写分离
                </MenuItem>
            </router-link>

            <router-link to="/shardOverview" style="color: white">
                <MenuItem name="shardView">
                <Icon type="android-share-alt"></Icon>
                分库分表
                </MenuItem>
            </router-link>

            <router-link to="/envConfig" style="color: white;text-decoration:none">
                <MenuItem name="envView">
                    <Icon type="ios-paper"></Icon>
                    环境管理
                </MenuItem>
            </router-link>

            </Menu>
        </Col>
    </Row>
</template>

<style scoped>
    .ivu-menu {
        display: block;
        margin: 0;
        padding: 0;
        outline: 0;
        top: 0;
        list-style: none;
        font-size: 14px;
        width: 100%;
        z-index: 999;
        color: #fff;
        background: inherit;
    }

    .ivu-menu-item-group .ivu-menu-item-group-title {
        font-size: 12px;
    }

    .ivu-menu-vertical.ivu-menu-light:after {
        content: '';
        display: block;
        width: 0px;
        height: 100%;
        background: #22E4F9;
        position: absolute;
        top: 0;
        bottom: 0;
        right: 0;
        z-index: 1
    }

    .ivu-menu-light.ivu-menu-vertical .ivu-menu-item-active:not(.ivu-menu-submenu) {
        color: #22E4F9;
        background : rgba(19, 19, 19, 0.33);
        border-right: 2px solid #22E4F9;
    }
</style>

<script>
    import ZSubmenu from '../components/submenu/submenu'
    export default {
        components: {
            "ZSubmenu": ZSubmenu
        },
        data: function () {
            return {
                res: null,
                corps: []
            }
        },
        methods: {
        }
    }
</script>
