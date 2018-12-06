<template>
    <Row>
        <Col span="22" offset="1">
            <Col>
                <div class="ivu-card" style="margin-bottom: 20px">
                    <div class="ivu-card-body" style="height: 500px; width: 100%; background-image: url(/static/image/zebra-head.png); background-repeat:no-repeat; background-size:100% 100%;-moz-background-size:100% 100%;">

                    </div>
                </div>
            </Col>

            <Col span="24">
                <Col span="4">
                    <div class="ivu-card">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto">
                                <i class="ivu-icon ivu-icon-refresh"></i></span>
                            <br>
                            <br>
                            <h2 style="text-align: center">动态刷新</h2>
                        </div>
                    </div>
                </Col>

                <Col span="4" offset="1">
                    <div class="ivu-card">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto">
                                <i class="ivu-icon ivu-icon-merge"></i></span>
                            <br>
                            <br>
                            <h2 style="text-align: center">读写分离</h2>
                        </div>
                    </div>
                </Col>

                <Col span="4" offset="1">
                    <div class="ivu-card">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto; padding: 0">
                                <i class="fa fa-sitemap"></i></span>
                            <br>
                            <br>
                            <h2 style="text-align: center">分库分表</h2>
                        </div>
                    </div>
                </Col>

                <Col span="4" offset="1">
                    <div class="ivu-card">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto;padding: 0">
                                <i class="ivu-icon ivu-icon-ios-pulse-strong"></i></span>
                            <br>
                            <br>
                            <h2 style="text-align: center">监控打点</h2>
                        </div>
                    </div>
                </Col>

                <Col span="4" offset="1">
                    <div class="ivu-card">
                        <div class="ivu-card-body">
                            <span class="ivu-avatar ivu-avatar-circle avatar-title avatar-icon"
                                  style="background-color: rgb(58, 128, 194); display: block; margin: 0 auto; padding: 0">
                                <i class="ivu-icon ivu-icon-ios-color-filter-outline"></i></span>
                            <br>
                            <br>
                            <h2 style="text-align: center">多数据源</h2>
                        </div>
                    </div>
                </Col>
            </Col>

            <Col span="16">
                <div class="ivu-card" style="padding: 10px; margin-top: 20px; height: 500px">
                    <div class="ivu-card-head">
                        <b>Zebra介绍</b>
                    </div>
                    <div class="ivu-card-body">
                        <span><b>Zebra</b>是一款基于JDBC API协议上开发出的<b>高可用、高性能</b>的数据库访问层 (Data Access Layer) 解决方案，提供了诸如<b>动态配置、监控、读写分离、分库分表等</b>功能，他是一个smart客户端， 通过jar包形式引入， 直接与mysql建立连接。</span>
                        <br>
                        <span>
                        <b>项目地址 : </b>
                            <a href="https://github.com/dianping/zebra" target="view_window">https://github.com/dianping/zebra</a>
                        </span>
                        <br>
                        <span>
                            <b> wiki地址 : </b> <a href="https://github.com/dianping/zebra/wiki" target="view_window">https://github.com/dianping/zebra/wiki</a></span>
                        <br>
                        <br>
                        <img style="padding: 8px; width: auto; height: auto;max-width: 100%;max-height: 260px;"
                             src="/static/image/zebra-dal.png"/>
                    </div>
                </div>
            </Col>

            <Col span="8">

                <div class="ivu-card" style="padding: 10px;margin-top: 20px; margin-left: 20px; height: 500px">
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
