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
                        <BreadcrumbItem>读写分离</BreadcrumbItem>
                    </Breadcrumb>
                </div>
            </div>


            <Col span="24">
                <div class="ivu-card">
                    <div class="ivu-card-body" style="padding: 12px;margin-top: 10px">
                        <div class="zebra-control-group">
                            <span>环境:</span>
                            <Select v-model="env" :transfer="true" style="width: 200px">
                                <Option v-for="value in envs" :value="value" :key="value">{{ value }}</Option>
                            </Select>
                        </div>
                        <div class="zebra-control-group">
                            <span>检索:</span>
                            <Input v-model="jdbcref" icon="ios-search-outline" placeholder="jdbcref 输入完成后回车搜索"
                                   @on-enter="searchData(0)" style="width: 200px"
                            />
                        </div>
                        <div class="zebra-control-group" style="float: right">
                            <Button type="primary" @click="registerJdbcRef" style="margin-left: 10px">注册JdbcRef</Button>
                        </div>
                    </div>
                </div>
                <div class="ivu-card" style="padding: 10px;margin-top: 10px">
                    <div class="ivu-card-body" style="padding: 10px 0 0 0;width: 100%">
                        <Table width="auto" border :columns="columns" :data="jdbcrefList" size="large"></Table>
                        <Page :total="total" size="small" style="padding-top: 10px;" :page-size="pageSize"
                              @on-change="searchData"></Page>
                    </div>
                </div>
                <Modal v-model="editJdbcRefConfigModal" :mask-closable="false" width="530">
                    <p slot="header">
                        <span>创建配置项</span>
                    </p>
                    <jdbcrefConfigForm ref="jdbcrefConfigForm" :dsConfig="dsConfig"
                                       style="margin-left: 10px"></jdbcrefConfigForm>
                    <div slot="footer">
                        <Button type="text" size="large" @click="cancel">取消</Button>
                        <Button type="primary" size="large" @click="save">保存</Button>
                    </div>
                </Modal>
            </Col>
        </Col>
    </Row>
</template>

<style scoped>
</style>

<script>
    let axios = require('axios');
    import jdbcrefConfigForm from "./jdbcrefConfigForm.vue"

    export default {
        components: {
            jdbcrefConfigForm
        },
        computed: {
            init: function () {
                return this.$store.state.init
            }
        },
        watch: {
            init: function () {
                if (this.$store.state.init && !this.dataInit) {
                    this.envs = this.$store.state.envs;
                    this.env =  this.$store.state.currentEnv;
                    this.loadJdbcConfig();
                    this.dataInit = true;
                }
            },
            env: function () {
                this.currentPage = 1;
                this.searchData(this.currentPage);
                this.changeEnv(this.env);
            }
        },
        methods: {
            initFormatter() {
                Date.prototype.Format = function (fmt) { //
                    let o = {
                        "M+": this.getMonth() + 1,                 //月份
                        "d+": this.getDate(),                    //日
                        "h+": this.getHours(),                   //小时
                        "m+": this.getMinutes(),                 //分
                        "s+": this.getSeconds(),                 //秒
                        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
                        "S": this.getMilliseconds()             //毫秒
                    };
                    if (/(y+)/.test(fmt))
                        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
                    for (var k in o)
                        if (new RegExp("(" + k + ")").test(fmt))
                            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
                    return fmt;
                }
            },
            searchData(page) {
                let _this = this;
                axios.get('/i/jdbcref/findJbdcrefByEnv', {
                    params: {
                        env: _this.env,
                        page: page ? page : _this.currentPage,
                        size: _this.pageSize,
                        jdbcrefFilter: _this.jdbcref
                    }
                }).then(function (response) {
                    _this.jdbcrefList = response.data.jdbcrefList;
                    _this.total = response.data.total;
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '获取jdbcref列表失败',
                        desc: e.message
                    });
                });
                _this.currentPage = page;
            },
            registerJdbcRef() {
                this.dsConfig.DBAddresses = [];
                this.editJdbcRefConfigModal = true;
                this.dsConfig.DBAddresses.push({DBAddress: "", readOrWrite: "读", weight: 0});
            },
            removeJdbcRef(jdbcRef) {
                let _this = this;
                _this.$Modal.confirm({
                    title: '提示',
                    content: '是否删除' + jdbcRef,
                    onOk: () => {
                        axios.get('/i/jdbcref/removeJdbcRef', {
                            params: {
                                jdbcref: jdbcRef,
                                env: _this.env
                            }
                        }).then(function (response) {
                            if (response.data.status === 'success') {
                                _this.$Notice.info({
                                    title: response.data.message
                                });
                            }
                            else if (response.data.status === 'fail') {
                                _this.$Notice.error({
                                    title: "失败",
                                    desc: response.data.message
                                });
                            }
                            _this.loadJdbcConfig();
                        })
                    }
                });
            },
            save() {
                let _this = this;
                var singleConfigs = [];

                for (var i = 0; i < _this.dsConfig.DBAddresses.length; ++i) {
                    if (_this.dsConfig.DBAddresses[i].readOrWrite === '写') {
                        singleConfigs.push({
                            address: _this.dsConfig.DBAddresses[i].DBAddress,
                            writeWeight: 1,
                            readWeight: 0,
                            userName: _this.dsConfig.DBAddresses[i].userName,
                            password: _this.dsConfig.DBAddresses[i].password
                        });
                    } else {
                        singleConfigs.push({
                            address: _this.dsConfig.DBAddresses[i].DBAddress,
                            writeWeight: 0,
                            readWeight: _this.dsConfig.DBAddresses[i].weight,
                            userName: _this.dsConfig.DBAddresses[i].userName,
                            password: _this.dsConfig.DBAddresses[i].password
                        });
                    }
                }
                var jdbcrefConfig = {
                    jdbcref: _this.dsConfig.jdbcref,
                    env: _this.dsConfig.env,
                    owner: _this.dsConfig.owner,
                    dbName: _this.dsConfig.dbName,
                    description: _this.dsConfig.description,
                    dbAddresses: singleConfigs
                }
                axios.post('/i/jdbcref/saveJdbcrefConfig', jdbcrefConfig).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: "成功",
                            desc: response.data.message
                        });
                    } else {
                        _this.$Notice.error({
                            title: "失败",
                            desc: response.data.message
                        });
                    }
                    _this.loadJdbcConfig();
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: "失败",
                        desc: e.message
                    });
                });
                this.editJdbcRefConfigModal = false;

            },
            cancel() {
                this.editJdbcRefConfigModal = false;
            },
            loadJdbcConfig() {
                let _this = this;
                axios.get('/i/jdbcref/findJbdcrefByEnv', {
                    params: {
                        env: _this.env,
                        page: _this.currentPage,
                        size: _this.pageSize
                    }
                }).then(function (response) {
                    _this.jdbcrefList = response.data.jdbcrefList;
                    _this.total = response.data.total;
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '获取jdbcref列表失败',
                        desc: e.message
                    });
                });
            }
        },
        created() {
            if (this.$store.state.init && !this.dataInit) {
                this.loadJdbcConfig();
                this.dataInit = true;
            }
            this.initFormatter();
        },
        data: function () {
            return {
                dataInit: false,
                total: 0,
                currentPage: 1,
                pageSize: 15,
                envs: this.$store.state.envs,
                env: this.$store.state.currentEnv,
                jdbcref: null,
                jdbcrefList: [],
                dsConfig: {
                    env: null,
                    owner: null,
                    jdbcref: null,
                    dbName: null,
                    description: null,
                    DBAddresses: []
                },
                editJdbcRefConfigModal: false,
                columns: [
                    {
                        title: 'JdbcRef',
                        key: 'jdbcref',
                        align: 'center',
                    },
                    {
                        title: '数据库配置',
                        key: 'groupConfig',
                        align: 'center',
                        render: (h, params) => {
                            var config = '写：' + params.row.groupConfig.writeDBNum + ' / 读：' + params.row.groupConfig.readDBNum
                            return h('span', {
                                style: {color: '#19be6b', fontSize: '14px'}
                            }, config)
                        }
                    },
                    {
                        title: '负责人',
                        key: 'owner',
                        align: 'center',
                    },
                    {
                        title: '更新时间',
                        key: 'updateTime',
                        align: 'center',
                        render: function (h, params) {
                            if (params.row.updateTime) {
                                return h('div',
                                    new Date(params.row.updateTime).Format('yyyy-MM-dd hh:mm:ss'));
                            }
                            return h('div', params.row.updateTime);
                        }
                    },
                    {
                        title: '操作',
                        key: 'operate',
                        width: 150,
                        align: 'center',
                        fixed: 'right',
                        render: (h, params) => {
                            return h('div', [h('Button', {
                                props: {type: 'primary', size: 'small'},
                                on: {
                                    click: () => {
                                        this.$router.push('jdbcref/info/' + params.row.jdbcref)
                                    }
                                }
                            }, '详情'),
                                h('Button', {
                                    props: {
                                        type: 'error',
                                        size: 'small'
                                    },
                                    style: {
                                        marginLeft: '5px'
                                    },
                                    on: {
                                        click: () => {
                                            this.removeJdbcRef(params.row.jdbcref)
                                        }
                                    }
                                }, '删除')]);
                        }
                    }
                ]
            }
        }
    }
</script>
