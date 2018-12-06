<template>
    <Row>
        <Col span="24">
            <Col span="24">
                <div class="ivu-card-body"
                     style="height: 400px; width: 100%; margin-bottom: 10px; background-image: url(/static/image/zebra-nav.png); background-repeat:no-repeat; background-size:100% 100%;-moz-background-size:100% 100%;">
                    <span style="display : block; font-size:32px; font-weight: bold; color: white">
                        分布式数据库中间件Zebra
                    </span>
                    <br>
                    <span style="color:#fff; font-size: 16px">
                            <b>Zebra</b>是一款基于JDBC API协议上开发出的的数据库访问层 (Data Access Layer) 解决方案，具备<b>轻量灵活、<br>高可用、高性能等特性</b>。
                            专注于解决传统关系型数据库的扩展性问题，在分布式场景下充分利用关系型数据<br>库的能力，提供诸如<b>动态数据源、读写分离、分库分表、数据库请求监控、等</b>功能。Zebra由美团点评
                            基础<br>架构团队自主研发, 目前应用于公司内部外卖、配送、酒旅等各大业务线，经过丰富场景验证，品质值得信赖。
                        </span>
                </div>
            </Col>

            <Col span="24">
                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto">
                                <i class="ivu-icon ivu-icon-refresh"></i></span>
                            <br>
                            <h2 style="text-align: center">动态数据源</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                提供动态的数据源刷新能力，实时响应数据源变更，支撑底层存储的平滑扩展。
                            </span>
                        </div>
                    </div>
                </Col>

                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto">
                                <i class="ivu-icon ivu-icon-merge"></i></span>
                            <br>
                            <h2 style="text-align: center">读写分离</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                提供对底层存储的读写分离能力，支持单库事务，强制走主/从库，从库流量调节。
                            </span>
                        </div>
                    </div>
                </Col>

                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto; padding: 0">
                                <i class="fa fa-sitemap"></i></span>
                            <br>
                            <h2 style="text-align: center">分库分表</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                提供hash和range 2种方式进行数据分片, sql支持度丰富, 解决大表大库的烦恼。
                            </span>
                        </div>
                    </div>
                </Col>

                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto;padding: 0">
                                <i class="ivu-icon ivu-icon-ios-pulse-strong"></i></span>
                            <br>
                            <h2 style="text-align: center">监控打点</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                提供数据库请求的监控，丰富的监控打点使调用链路清晰可见，方便业务排查问题。
                            </span>
                        </div>
                    </div>
                </Col>

                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto; padding: 0">
                                <i class="ivu-icon ivu-icon-ios-shuffle-strong"></i></span>
                            <br>
                            <h2 style="text-align: center">异步化请求</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                提供异步化的数据库访问能力，在数据库密级型业务中有效提升系统吞吐量。
                            </span>
                        </div>
                    </div>
                </Col>

                <Col span="8">
                    <div class="ivu-card home-bordered">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto; padding: 0">
                                <i class="ivu-icon ivu-icon-ios-color-filter-outline"></i></span>
                            <br>
                            <h2 style="text-align: center">多数据源</h2>
                            <br>
                            <span style="display: block; padding: 0 50px 0 50px">
                                内置多数据源配置能力，解决同时访问多个数据库场景下复杂配置的难题。
                            </span>
                        </div>
                    </div>
                </Col>
            </Col>

            <Col span="16">
                <div class="ivu-card" style="padding: 10px; margin-top: 20px; height: 500px">
                    <div class="ivu-card-head">
                        <b>问题与文档</b>
                    </div>
                    <div class="ivu-card-body">
                        <span style="font-weight: bold; font-size: 24px">快速开始</span>
                        <br>
                        <span>快速搭建项目</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra" target="view_window">https://github.com/Meituan-Dianping/zebra</a>

                        <br>
                        <br>

                        <span style="font-weight: bold; font-size: 24px">设计说明</span>
                        <br>
                        <span>总体设计</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra" target="view_window">https://github.com/Meituan-Dianping/zebra</a>
                        <br>
                        <span>路由设计</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra" target="view_window">https://github.com/Meituan-Dianping/zebra</a>
                        <br>
                        <br>

                        <span style="font-weight: bold; font-size: 24px">关于我们</span>
                        <br>
                        <span>项目地址</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra" target="view_window">https://github.com/Meituan-Dianping/zebra</a>
                        <br>
                        <span>wiki地址</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra/wiki" target="view_window">https://github.com/Meituan-Dianping/zebra/wiki</a>
                        <br>
                        <br>

                        <span style="font-weight: bold; font-size: 24px">FAQ</span>
                        <br>
                        <a href="https://github.com/Meituan-Dianping/zebra" target="view_window">https://github.com/Meituan-Dianping/zebra</a>
                    </div>
                </div>
            </Col>

            <Col span="8">

                <div class="ivu-card" style="padding: 10px;margin-top: 20px; margin-left: 10px; height: 500px">
                    <div class="ivu-card-head">
                        <b>路线图</b>
                    </div>
                    <div class="ivu-card-body" style="padding: 10px 0 0 0">
                        <Timeline>
                            <TimelineItem>
                                <p class="time">2.x 版本</p>
                                <p class="content">
                                    <Tag color="green">读写分离</Tag>
                                    <br>
                                    <Tag color="green">分库分表</Tag>
                                    <br>
                                    <Tag color="green">监控</Tag>
                                    <br>
                                    <Tag color="green">异步化dao</Tag>
                                </p>
                            </TimelineItem>
                            <TimelineItem>
                                <p class="time">3.x 版本</p>
                                <p class="content">
                                    <Tag color="yellow">分布式sql引擎</Tag>
                                    <br>
                                    <Tag color="yellow">分布式事务</Tag>
                                    <br>
                                    <Tag color="grey">Proxy</Tag>
                                </p>
                            </TimelineItem>
                            <TimelineItem>
                                <p class="time">未来</p>
                                <p class="content">
                                    <Tag color="blue">欢迎共建</Tag>
                                </p>
                            </TimelineItem>
                        </Timeline>
                    </div>
                </div>
            </Col>
        </Col>
    </Row>

</template>
<script>
    import Avatar from "iview/src/components/avatar/avatar";

    export default {
        components: {Avatar},
        computed: {
            envs() {
                return this.$store.state.envs;
            },
            init() {
                return this.$store.state.init;
            }
        },
        watch: {
            init(to, from) {
                if (to) {
                    this.selectedEnv = this.$store.state.currentEnv;
                }
            }
        }
    }
</script>

<style scoped>

    .home-bordered {
        border: 1px solid rgba(183, 183, 183, 0.23);
    }
    .avatar-title {
        width:60px;
        height:60px;
        line-height:60px;
        border-radius:30px;
        text-align: center;
    }

    .avatar-icon {
        font-size: 36px;
        padding-left: 6px;
    }

    .time {
        font-size: 16px;
        font-weight: bold;
    }

    .content {
        font-size: 14px;
        padding-left: 5px;
    }
</style>
