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
                    <BreadcrumbItem>分库分表</BreadcrumbItem>
                </Breadcrumb>
            </div>
        </div>

        <div class="ivu-card">
            <div class="ivu-card-body" style="padding: 12px;margin-top: 10px">
                <div class="zebra-control-group">
                    <span>环境:</span>
                    <Select v-model="env" :transfer="true" style="width: 200px">
                        <Option v-for="value in envs" :value="value" :key="value">{{ value }}</Option>
                    </Select>
                </div>
                <!-- <div class="zebra-control-group">
                    <span>部门:</span>
                    <Select v-model="bu" :transfer="true" filterable style="width: 200px">
                        <Option v-for="item in cityList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                    </Select>
                </div> -->

                <div class="zebra-control-group">
                    <span>检索:</span>
                    <Input v-model="ruleName" icon="ios-search-outline" placeholder="ruleName 输入完成后回车搜索" @on-enter="searchData(0)" style="width: 200px"/>
                </div>
                <div class="zebra-control-group" style="float: right">
                    <Button type="primary" @click="registerRuleName">注册ruleName</Button>
                </div>
            </div>
        </div>

        <Col span="24">
        <div class="ivu-card" style="padding: 10px;margin-top: 10px">
            <div class="ivu-card-body" style="padding: 10px 0 0 0;width: 100%">
                <Table width="auto" border :columns="columns" size="large" :data="shardList"></Table>
                <Page :total="total" size="small" style="padding-top: 10px;" :page-size="pageSize" @on-change="searchData"></Page>
            </div>
        </div>
        </Col>

        </Col>
        <Modal v-model="registerRuleNameModal" title="注册ruleName" width="400"
               @on-ok="addRuleName" :mask-closable="false">
            <Form :model="formItem" :label-width="80" label-position="left">
                <FormItem label="ruleName">
                    <span slot="label" style="font-size: 14px;color: #464c5b">ruleName</span>
                    <Input v-model="formItem.ruleName" placeholder="ruleName" />
                </FormItem>
                <!--<FormItem label="physicalDbs">-->
                    <!--<Input v-model="formItem.physicalDbs" placeholder="填写分库,可以以db[0-9]标识,也可以db0,db1,db2...形式"/>-->
                <!--</FormItem>-->
                <FormItem label="负责人">
                    <span slot="label" style="font-size: 14px;color: #464c5b">负责人</span>
                    <Input v-model="formItem.owner" placeholder="owner" />
                </FormItem>
                <FormItem label="描述">
                    <span slot="label" style="font-size: 14px;color: #464c5b">描述</span>
                    <Input v-model="formItem.desc" placeholder="description"/>
                </FormItem>
            </Form>
        </Modal>
        <Modal
            v-model="removeRuleNameModal"
            @on-ok="ok"
            @on-cancel="cancel">
            <p>Content of dialog</p>
            <p>Content of dialog</p>
            <p>Content of dialog</p>
        </Modal>
    </Row>
</template>

<script>
    let axios = require('axios');
    export default {
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
                _this.currentPage = 1;
                _this.searchData(_this.currentPage);
                _this.$store.state.currentEnv = this.env;
            }
        },
        methods: {
            searchData(page) {
                let _this = this;
                axios.get('/i/shard/findRuleNameByEnv', {
                    params: {
                        env: _this.env,
                        page: page ? page : _this.currentPage,
                        size: _this.pageSize,
                        shardFilter: _this.ruleName
                    }
                }).then(function (response) {
                    _this.shardList = response.data.shardList;
                    _this.total = response.data.total;
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '获取ruleName列表失败',
                        desc: e.message
                    });
                });
                _this.currentPage = page;
            },
            removeRuleName(ruleName) {
                this.$Modal.confirm({
                    content: '确认删除' + ruleName + '?',
                    onOk: () => {
                    this.doRemoveRuleName(ruleName);
                    }
            })
            },
            doRemoveRuleName(ruleName) {
                let _this = this;
                axios.get('/i/shard/deleteRuleNameConfig', {
                    params: {
                        ruleName: ruleName,
                        env: _this.env
                    }
                }).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: response.data.message
                        });
                    } else {
                        _this.$Notice.error({
                            title: response.data.message
                        });
                    }
                }).cache(function (e) {
                    _this.$Notice.error({
                        title: '删除失败',
                        desc: e.message
                    });
                })
            },
            registerRuleName() {
                this.registerRuleNameModal = true;
            },
            addRuleName() {
                let _this = this;
                axios.get('/i/shard/addRuleName', {
                    params: {
                        ruleName: _this.formItem.ruleName,
                        env: _this.env,
                        // physicalDbs : _this.formItem.physicalDbs,
                        owner : _this.formItem.owner,
                        desc : _this.formItem.desc
                    }
                }).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: response.data.message
                        });
                        _this.$router.push('/shard/' + _this.formItem.ruleName);
                    } else if (response.data.status === 'fail') {
                        _this.$Notice.error({
                            title: response.data.message
                        });
                    }
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '注册ruleName失败',
                        desc: e.message
                    });
                })
            },
            loadData() {
                let _this = this;
                axios.get('/i/shard/findRuleNameByEnv', {
                    params: {
                        env: _this.env,
                        page: _this.currentPage,
                        size: _this.pageSize
                    }
                }).then(function (response) {
                    _this.shardList = response.data.shardList;
                    _this.total = response.data.total;
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '获取ruleName列表失败',
                        desc: e.message
                    });
                })
            }
        },
        created() {
            if (this.$store.state.init && !this.dataInit) {
                let _this = this;
                axios.get('/i/zkConfig/getEnv').then(function (response) {
                    _this.envs = response.data;
                    _this.env = _this.envs[0];
                    axios.get('/i/shard/findRuleNameByEnv', {
                        params: {
                            env: _this.env,
                            page: _this.currentPage,
                            size: _this.pageSize
                        }
                    }).then(function (response) {
                        _this.shardList = response.data.shardList;
                        _this.total = response.data.total;
                    }).catch(function (e) {
                        console.error(e.message);
                        _this.$Notice.error({
                            title: '获取ruleName列表失败',
                            desc: e.message
                        });
                    })
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '加载Zookeeper地址列表失败',
                        desc: e.message
                    });
                });
                _this.dataInit = true;
            }
        },
        data: function () {
            return {
                dataInit : false,
                total: 0,
                currentPage: 1,
                pageSize: 15,
                env: null,
                envs: [],
                ruleName: null,
                shardList: [],
                rowChosed: '',
                registerRuleNameModal: false,
                formItem : {
                    ruleName : '',
                    physicalDbs : '',
                    owner : '',
                    desc : ''
                },
                columns: [
                    {
                        title: 'ruleName',
                        key: 'ruleName',
                        width: 220
                    },
                    // {
                    //     title: '分库',
                    //     key: 'physicalDbs',
                    //     width: 280,
                    //     align: 'center'
                    // },
                    {
                        title: '负责人',
                        key: 'owner',
                        width: 160,
                        align: 'center'
                    },
                    {
                        title: '描述',
                        key: 'desc',
                        align: 'center'
                    },
                    {
                        title: '修改时间',
                        key: 'updateTime',
                        width: 280,
                        align: 'center'
                    },
                    {
                        title: '操作',
                        key: 'operate',
                        width: 160,
                        align: 'center',
                        fixed: 'right',
                        render: (h, params) => {
                            return h('div', [h('Button', {
                                props: { type: 'primary', size: 'small' },
                                style: {
                                    marginRight: '5px'
                                },
                                on: {
                                    click: () => {
                                        this.$router.push('/shard/' + params.row.ruleName)
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
                                            this.removeRuleName(params.row.ruleName)
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
