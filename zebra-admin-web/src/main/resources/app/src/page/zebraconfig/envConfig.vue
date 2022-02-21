<template>
    <Row>
        <Col>
            <div class="ivu-card">
                <div class="ivu-card-body" style="padding: 12px;width:100%;line-height: 12px;position:relative;">
                    <Breadcrumb>
                        <BreadcrumbItem to="/">
                            <Icon type="ios-home-outline"></Icon>
                            Home
                        </BreadcrumbItem>
                        <BreadcrumbItem>环境配置</BreadcrumbItem>
                    </Breadcrumb>
                </div>
            </div>
            <div class="ivu-card">
                <div class="ivu-card-body" style="padding: 12px;margin-top: 10px">
                    <div class="zebra-control-group">
                        <Button type="primary" @click="addZKAddr">添加环境</Button>
                    </div>
                </div>
            </div>

            <div class="ivu-card" style="padding: 10px;margin-top: 10px">
                <div class="ivu-card-body" style="padding: 10px 0 0 0;width: 100%">
                    <Table border :columns="ZKColumns" size="large" :data="ZKAddrList"></Table>
                </div>
            </div>
            <Modal v-model="ZKAddrModal" title="添加Zookeeper地址" @on-ok="saveZKAddr" width="450" :mask-closable="false">
                <Form :model="zkConfig" label-position="left" :label-width="120">
                    <FormItem prop="zkAddr">
                        <span slot="label" style="font-size: 14px;color: #464c5b">zookeeper地址</span>
                        <Input v-model="zkConfig.zkAddr" placeholder="zookeeper地址" style="width: 250px"/>
                    </FormItem>
                    <FormItem>
                        <span slot="label" style="font-size: 14px;color: #464c5b">环境</span>
                        <Input v-model="zkConfig.env" placeholder="环境" style="width: 250px"/>
                    </FormItem>
                    <FormItem>
                        <span slot="label" style="font-size: 14px;color: #464c5b">描述</span>
                        <Input v-model="zkConfig.description" placeholder="描述" style="width: 250px"/>
                    </FormItem>
                </Form>
            </Modal>
        </Col>
    </Row>
</template>

<script>
    let axios = require('axios');
    export default {
        name: "ZKConfig",
        created() {
            this.initData();
        },
        data: function () {
            return {
                ZKAddrList: [],
                ZKAddrModal: false,
                zkConfig: {
                    zkAddr: null,
                    env: null,
                    description: null
                },
                ZKColumns: [
                    {
                        title: '编号',
                        key: 'id',
                        width: 100,
                        align: 'center',

                        sortable: true,
                        sortType: 'desc'
                    },
                    {
                        title: '环境',
                        key: 'name',
                        width: 200,
                        align: 'center'
                    },
                    {
                        title: 'zookeeper地址',
                        key: 'host',
                        width: 250,
                        align: 'center'
                    },
                    {
                        title: '备注',
                        key: 'description',
                        align: 'center'
                    },
                    {
                        title: '操作',
                        align: 'center',
                        width: 100,
                        render: (h, params) => {
                            return h('Button', {
                                props: {type: 'error', size: 'small'},
                                style: {
                                    marginRight: '5px'
                                },
                                on: {
                                    click: () => {
                                        this.deleteZKConfig(params.row.id);
                                    }
                                }
                            }, '删除');
                        }
                    }
                ]
            }
        },
        methods: {
            addZKAddr() {
                this.ZKAddrModal = true;
            },
            saveZKAddr() {
                let _this = this;
                var zkConfigDto = {
                    host: _this.zkConfig.zkAddr,
                    name: _this.zkConfig.env,
                    description: _this.zkConfig.description,
                }
                axios.post('/i/zkConfig/addZKConfig', zkConfigDto).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: '增加zookeeper配置成功'
                        });
                    } else {
                        _this.$Notice.error({
                            title: '增加zookeeper配置失败'
                        });
                    }
                    _this.initData();
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '增加zookeeper配置失败',
                        desc: e.message
                    });
                })
            },
            initData() {
                let _this = this;
                axios.get('/i/zkConfig/findZKConfig').then(function (response) {
                    _this.ZKAddrList = response.data;
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '加载ZookeeperConfig列表失败',
                        desc: e.message
                    });
                })
            },
            deleteZKConfig(id) {
                let _this = this;
                axios.get('/i/zkConfig/deleteZKConfig', {
                    params: {
                        id: id
                    }
                }).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: '删除成功'
                        });
                    } else {
                        _this.$Notice.error({
                            title: '删除失败'
                        });
                    }
                    _this.initData();
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '删除失败',
                        desc: e.message
                    });
                })
            }
        }
    }
</script>

<style scoped>

</style>
