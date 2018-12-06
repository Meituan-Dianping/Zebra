<template>

    <Menu mode="horizontal" :theme='theme'>
        <div class="layout-menu">
            <Button type="ghost" icon="navicon-round" style="color: white;z-index: 1" @click="showSide()"></Button>
        </div>
        <div class="layout-logo">
            <a href="/" class="logo">
                Zebra 管理平台
            </a>
        </div>
        <div class="layout-nav">
            <div class="header-nav-list">
                <a href="https://github.com/Meituan-Dianping/zebra/wiki" class="ivu-menu-item" target="view_window" style="text-align: center;font-size: 14px;">
                    文档
                </a>
                <a href="https://github.com/Meituan-Dianping/zebra" class="ivu-menu-item" target="view_window" style="text-align: center;font-size: 14px;">
                    帮助&支持
                </a>
                <!--<Submenu name="3">-->
                    <!--<template slot="title">-->
                        <!--<Avatar :src="headUrl" /> {{name}}-->
                    <!--</template>-->
                    <!--<a href="/logout" class="ivu-menu-item" style="text-align: center">-->
                        <!--<Icon type="log-out"></Icon>-->
                        <!--登出-->
                    <!--</a>-->
                <!--</Submenu>-->
            </div>
        </div>
    </Menu>
</template>

<script>
    let axios = require('axios');
    export default {
        props: ['message'],

        //data加载于created之前，只执行一次
        data: function () {
            return {
                theme: 'light',
                name: '',
                headUrl: null,
                value: null,
                menuFocus: false,
                keyWord: null,
                searchLoading: false,
                selectGroups: null
            }
        },
        methods: {
            showSide: function () {
                var show = this.message.showSide;
                this.message.showSide = !show;
            }
        }
    }
</script>

<style scoped>
    .wrapper-header-nav-logo {
        height: 50px;
        margin-top: 8px;
        float: left;
    }

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

    .ivu-menu-horizontal:hover {
        background-color: #2b3239;
        opacity: 0.9;
    }

    .ivu-menu-horizontal.ivu-menu-light:after {
        content: '';
        display: block;
        width: 100%;
        height: 0px;
        background: #dddee1;
        position: absolute;
        bottom: 0;
        left: 0
    }

    /deep/ .ivu-select-dropdown {
        width: inherit;
        max-height: 200px;
        overflow: auto;
        margin: 5px 0;
        padding: 5px 0;
        background-color: #3C4650;
        box-sizing: border-box;
        border-radius: 4px;
        box-shadow: 0 1px 6px rgba(0, 0, 0, .2);
        position: absolute;
        z-index: 900
    }

    .ivu-select-dropdown {
        width: inherit;
        max-height: 200px;
        overflow: auto;
        margin: 5px 0;
        padding: 5px 0;
        background-color: #3C4650;
        box-sizing: border-box;
        border-radius: 4px;
        box-shadow: 0 1px 6px rgba(0, 0, 0, .2);
        position: absolute;
        z-index: 900
    }

    .ivu-select-item {
        color: #fff;
    }

    .ivu-select-item:hover {
        color: #22E4F9;
        border-right: 2px solid #22E4F9;
        background-color: #515f6c
    }

    .ivu-select-item-focus {
        color: #22E4F9;
        border-right: 2px solid #22E4F9;
        background-color: #515f6c
    }

    .ivu-menu-horizontal {
        height: 58px;
        line-height: 58px;
    }

    .layout-menu {
        float: left;
        position: relative;
        padding-left: 5px;
        margin-right: 30px;
        height: inherit;
        z-index: 1;
    }

    .ivu-menu-horizontal .layout-logo {
        float: left;
        position: relative;
        width: 16%;
        height: inherit;
        min-width: 100px;
        z-index: 2;
    }

    .logo {
        color: #fff;
        font-size: 1.2em;
        font-weight: 500;
    }

    .layout-nav {
        /* float: right; */
        position: relative;
        /*opacity: 0.8;*/
        display: inline;
    }

    .header-nav-list {
        /*opacity: 0.9;*/
        float: right;
    }

    .ivu-input {
        display: inline-block;
        width: 100%;
        height: 32px;
        line-height: 1.5;
        padding: 4px 7px;
        font-size: 12px;
        border: 2px solid #dddee1;
        border-radius: 4px;
        color: #000;
        background-color: #fff;
        position: relative;
        cursor: text;
        transition: border .2s ease-in-out, background .2s ease-in-out, box-shadow .2s ease-in-out
    }

    .auto-complete-item {
        padding: 4px 0;
        border-bottom: 1px solid #F6F6F6;
    }

    .auto-complete-group {
        font-size: 12px;
        padding: 4px 6px;
    }

    .auto-complete-group span {
        color: #cbcbcb;
        font-weight: bold;
    }
</style>
