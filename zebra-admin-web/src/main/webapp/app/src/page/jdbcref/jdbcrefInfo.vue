<template>
    <Row>
        <Col>
        <div class="ivu-card">

            <div class="ivu-card-body">
                <!--<div class="zebra-control-group">-->
                    <!--<span>当前环境：</span>-->

                    <!--<RadioGroup v-model="currentEnv" type="button">-->
                        <!--<Radio style="padding-left: 6px">{{ currentEnv }}</Radio>-->
                    <!--</RadioGroup>-->
                <!--</div>-->
                <Table width="auto" size="large" border :columns="columns" :data="dsList"></Table>
                <Page :total="total" size="small" style="padding-top: 10px;" :page-size="pageSize" @on-change="searchData"></Page>
            </div>
        </div>

            <Modal v-model="dsconfigModel" title="配置设置" width="600" :mask-closable="false">
            <p slot="header">
                配置设置({{ jdbcref }})
            </p>
            <jdbcrefConfigForm ref="jdbcrefConfigForm" :dsConfig="dsConfig" style="margin-left: 10px"></jdbcrefConfigForm>
            <div slot="footer">
                <Button type="text" size="large" @click="cancel">取消</Button>
                <Button type="primary" size="large" @click="save">保存</Button>
            </div>
        </Modal>
            <Modal v-model="propertiesModel" title="连接池默认参数" width="500" >
                <Table width="auto" size="large" border :columns="propertyColumns" :data="propertyList">
                </Table>
                <div slot="footer">
                    <Button type="primary" size="large" @click="exit">我知道了</Button>
                </div>
            </Modal>
        </Col>
    </Row>
</template>

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
                let _this = this;
                if (_this.$store.state.init && !_this.dataInit) {
                    _this.envs = _this.$store.state.envs;
                    _this.env = _this.env ? _this.env : _this.$store.state.currentEnv;
                    _this.dataInit = true;
                }
            },
            env: function () {
                let _this = this;
                _this.loadData();
            },
            modelLoading: function () {
                if (this.modelLoading) {
                    this.$Spin.show();
                } else {
                    this.$Spin.hide();
                }
            },
            '$route'(to, from) {
                let newValue = to.params.jdbcRef;
                let oldValue = from.params.jdbcRef;
                if (oldValue != newValue) {
                    this.jdbcref = newValue;
                    this.loadData();
                }
            }
        },
        created() {
            if (this.$store.state.init && !this.dataInit) {
                let _this = this;
                _this.env = _this.$store.state.currentEnv;
                axios.get('/i/jdbcref/findJdbcrefDetil', {
                    params: {
                        jdbcref: _this.jdbcref,
                        env:  _this.env
                    }
                }).then(function (response) {
                    if (response.status == 200) {
                        _this.dsList = response.data;
                    } else {
                        _this.$Notice.error({
                            title: '加载ds列表失败'
                        });
                    }
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '加载ds列表失败',
                        desc: e.message
                    });
                });
                _this.dataInit = true;
            }
        },
        methods: {
            loadData: function () {
                let _this = this;
                axios.get('/i/jdbcref/findJdbcrefDetil', {
                    params: {
                        jdbcref: _this.jdbcref,
                        env: _this.env
                    }
                }).then(function (response) {
                    if (response.status == 200) {
                        _this.dsList = response.data;
                    } else {
                        _this.$Notice.error({
                            title: '加载ds列表失败',
                        });
                    }
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '加载ds列表失败',
                        desc: e.message
                    });
                })
            },
            editJdbcRef(params) {
                this.dsconfigModel = true;
            },
            cancel() {
                this.dsconfigModel = false;
            },
            save() {

                this.dsconfigModel = false;
            },
            viewProperties(properties) {
                this.propertyList = [];
                this.propertiesModel = true;

                var propertyArray = [];
                propertyArray = properties.split('&');

                for (var i = 0; i < propertyArray.length; ++i) {
                    var array = [];
                    array = propertyArray[i].split('=');
                    this.propertyList.push({key: array[0],value: array[1]});
                }
            },
            exit() {
                this.propertiesModel = false;
            },
            offLine(row) {
                let _this = this;
                axios.get('/i/jdbcref/dsOffLine', {
                    params: {
                        dsName: row.dsName,
                        env: _this.env
                    }
                }).then(function (response) {
                    _this.loadData();
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '下线' + row.dsName + '失败！',
                        desc: e.message
                    });
                });
            },
            onLine(row) {
                let _this = this;
                axios.get('/i/jdbcref/dsOnLine', {
                    params: {
                        dsName: row.dsName,
                        env: _this.env
                    }
                }).then(function (response) {
                    _this.loadData();
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '上线' + row.dsName + '失败！',
                        desc: e.message
                    });
                });
            }
        },
        data: function () {
            return {
                stared: false,
                theme: 'light',
                jdbcref: this.$route.params.jdbcRef,
                dataInit: false,
                showDetilTable: false,
                dsconfigModel: false,
                propertiesModel: false,
                env : this.$store.state.currentEnv,
                dsConfig:{
                    jdbcref: null,
                    key: null,
                    value: null
                },
                groupdsMapping:[],
                columns: [
                    {
                        title: 'dsName',
                        key: 'dsName',
                        align: 'center',
                        width : 260,
                        render: (h, params) => {
                            return h('span', {
                                style: {fontSize: '14px'}
                            }, params.row.dsName)
                        }
                    },
                    {
                        title: 'IP',
                        key: 'ip',
                        align: 'center',
                        width: 120,
                        render: (h, params) => {
                            return h('span', {
                                style: {fontSize: '14px'}
                            }, params.row.ip)
                        }
                    },
                    {
                        title: '端口',
                        key: 'port',
                        align: 'center',
                        width : 100,
                        render: (h, params) => {
                            return h('span', {
                                style: {fontSize: '14px'}
                            }, params.row.port)
                        }
                    },
                    {
                        title: '用户名',
                        key: 'username',
                        align: 'center',
                        width : 100
                    },
                    {
                        title: '密码',
                        key: 'password',
                        align: 'center',
                        width : 100
                    },
                    {
                        title: 'JdbcUrl',
                        key: 'jdbcUrl',
                        align: 'center',
                        width: 260,
                        render: (h, params) => {
                            var value = params.row.jdbcUrl.substr(0, 27) + '...';
                            return h('Tooltip', {
                                props: {placement: 'top', content: params.row.jdbcUrl, transfer: true},
                            },[h('span', {
                                style: {fontSize: '14px'}
                            }, value), h('div',{
                                slot: 'content',
                                style: {
                                    whiteSpace: 'normal',
                                    wordBreak: 'break-all'
                                }
                            }, params.row.jdbcUrl)]);
                        }
                    },
                    {
                        title: '读/写',
                        key: 'readOrWrite',
                        align: 'center',
                        width: 80,
                        render: (h, params) => {
                            var value;
                            if (params.row.weight !== 0) {
                                value = '读';
                            } else {
                                value = '写';
                            }
                            return h('span', {
                                style: {fontSize: '14px'}
                            }, value);
                        }
                    },
                    {
                        title: '流量占比',
                        key: 'weight',
                        align: 'center',
                        width: 100,
                        render: (h, params) => {
                            return h('span', {
                                style: {fontSize: '14px'}
                            }, params.row.weight)
                        }
                    },
                    {
                        title: '是否在线',
                        key: 'active',
                        align: 'center',
                        width: 100,
                        render: (h, params) => {
                            var value;
                            if (params.row.active === true) {
                                value = '在线';
                                return h('span', {
                                    style: {color: '#19be6b', fontSize: '14px'}
                                }, value);
                            }
                            else {
                                value = '下线';
                                return h('span', {
                                    style: {color: '#ff9900', fontSize: '14px'}
                                }, value);
                            }

                        }
                    },
                    {
                        title: '连接池默认参数',
                        key: 'properties',
                        align: 'center',
                        width: 150,
                        render: (h, params) => {
                            return h('Button', {
                                props: {
                                    type: 'primary',
                                    size: 'small'
                                },
                                style: {fontSize: '14px'},
                                on: {
                                    click: () => {
                                        this.viewProperties(params.row.properties);
                                    }
                                }
                            }, '查看');
                        }
                    },
                    {
                        title: '操作',
                        key: 'operate',
                        width: 80,
                        align: 'center',
                        render: (h, params) => {
                            if (params.row.active === true) {
                                return h('div', [
                                    h('Button', {
                                        props: {type: 'warning', size: 'small'},
                                        on: {
                                            click: () => {
                                                this.offLine(params.row);
                                            }
                                        }
                                    }, '下线')
                                ]);
                            }
                            else {
                                return h('div', [
                                    h('Button', {
                                        props: {type: 'primary', size: 'small'},
                                        on: {
                                            click: () => {
                                                this.onLine(params.row);
                                            }
                                        }
                                    }, '上线')
                                ]);
                            }
                        }
                    }
                ],
                dsList: [],
                propertyColumns: [
                    {
                        title: 'key',
                        key: 'key',
                        align: 'center'
                    },
                    {
                        title: 'value',
                        key: 'value',
                        align: 'center'
                    }
                ],
                propertyList: []
            }
        }
    }
</script>

<style scoped>
    .zebra-form-header {
        font-size: 13px;
        font-weight: 500;
        margin: 0 0 10px 0;
        position: relative;
    }

    .zebra-form-body {
        font-size: 12px;
        font-weight: 500;
        padding: 0 10px;
        margin-bottom: 10px;
        color: #80848f;
    }
</style>
