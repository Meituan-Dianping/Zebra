<template>
    <Row>


        <div class="ivu-card" style="padding: 8px">
            <div class="ivu-card-body">
                <div v-if="!shardRule || shardRule.length == 0">
                    配置不存在
                    <a @click="createConfig">点此创建</a>配置
                </div>
                <div v-else-if="shardRule && shardRule.length > 0">
                    <Tabs type="card" closable style="border:rgb(221, 217, 217) solid 1px" @on-tab-remove="handleTabRemove">
                        <TabPane v-for="(tb,index) in shardRule" :key="tb.tableName" :label="tb.tableName" :name="index">
                            <div class="zebra-form-body">
                                <span style="padding-right:4px">表名:
                                    <Input v-model="tb.tableName" placeholder="table name" :autofocus="true" style="width: 250px"></Input>
                                </span>

                            </div>
                            <div class="zebra-form-body" style="padding-top:4px" v-if="tb.dimensionConfigs && tb.dimensionConfigs.length > 0">
                                <span style="display:block">多维度配置:</span>
                                <div v-for="dimensionConfig in tb.dimensionConfigs" class="ivu-card-body" style="background-color:rgb(212, 229, 245) ; margin-top:8px">
                                    <Checkbox  v-model="dimensionConfig.isMaster" style="padding-left:8px;margin-left: 4px">主维度</Checkbox>

                                    <div class="zebra-form-body" style="padding-top:4px">
                                        <span style="padding-right:4px">库路由规则:
                                            <Input  v-model="dimensionConfig.dbRule" placeholder="dbRule" style="width: 250px"></Input>
                                        </span>
                                        <span style="padding-right:4px">库索引:
                                            <Input  v-model="dimensionConfig.dbIndexes" placeholder="dbIndexes" style="width: 200px"></Input>
                                        </span>
                                        <br>
                                        <br>
                                        <span style="padding-right:4px">表路由规则:
                                            <Input  v-model="dimensionConfig.tbRule" placeholder="tbRule" style="width: 250px"></Input>
                                        </span>
                                        <span style="padding-right:4px">表后缀:
                                            <Input  v-model="dimensionConfig.tbSuffix" placeholder="tbSuffix" style="width: 200px"></Input>
                                        </span>
                                        <Checkbox v-model="dimensionConfig.tbSuffixZeroPadding" style="padding-right:4px">ZeroPadding</Checkbox>
                                    </div>
                                </div>
                            </div>
                        </TabPane>
                        <Button type="ghost" @click="handleTabsAdd"  slot="extra">
                            <Icon type="plus"></Icon>
                        </Button>
                    </Tabs>
                    <Button type="primary" @click="saveRule" style="margin-left: 93%;margin-top: 5px">保存</Button>
                </div>
            </div>
        </div>

    </Row>
</template>

<script>
    import Icon from "../../components/icon/icon";
    let axios = require('axios');
    export default {
        components: {Icon},
        computed: {
            staredShards() {
              return this.$store.state.staredShards;
            },
            init: function () {
                return this.$store.state.init
            },
            // tableName: function () {
            //     return this.shardRule[index].tableName;
            // }
        },
        watch: {
            staredShards() {
                this.isStaredShard();
            },
            '$route'(to, from) {
                this.ruleName = to.params.ruleName;
                this.isStaredShard();
            },
            init: function () {
                if (this.$store.state.init && !this.dataInit) {
                    this.envs = this.$store.state.envs;
                    this.env = this.env ? this.env : this.$store.state.currentEnv;
                    this.dataInit = true;
                }
            },
            env: function () {
                this.changeEnv(this.env);
                this.loadData();
            }
        },
        methods: {
            loadData: function () {
                let _this = this;
                axios.get('/i/shard/findRuleName', {
                    params: {
                        env: _this.env,
                        ruleName: _this.ruleName
                    }
                }).then(function (response) {
                    if (response.status == 200) {
                        _this.shardRule = response.data.tableShardConfigs;
                        if (_this.shardRule != null) {
                            for (var i = 0; i < _this.shardRule.length; ++i) {
                                _this.shardRuleTab.push(true);
                            }
                        }
                    } else {
                        _this.$Notice.error({
                            title: '查询ruleName失败',
                        });
                    }
                }).catch(function (e) {
                    console.error(e.message);
                    _this.$Notice.error({
                        title: '查询ruleName失败',
                        desc: e.message
                    });
                })
            },
            createConfig: function () {
                this.shardRule = [{
                    tableName: 'table_new',
                    dimensionConfigs: [{
                        isMaster: false,
                        dbRule: '',
                        dbIndexes: '',
                        tbRule: '',
                        tbSuffix: '',
                        tbSuffixZeroPadding: ''
                    }]
                }];
                this.shardRuleTab[0] = true;
            },
            isStaredShard() {
                this.stared = false;
                if (this.staredShards != 'undefined' && this.staredShards != null && this.staredShards.length > 0) {
                    if (this.staredShards.indexOf(this.ruleName) >= 0) {
                        this.stared = true;
                    }
                }
            },
            handleTabRemove(index) {
                 //this.shardRuleTab[index] = false;
                 this.shardRule.splice(index, 1);
            },
            handleTabsAdd() {
                var rule = {
                    tableName: 'table_new',
                    dimensionConfigs: [{
                        isMaster: false,
                        dbRule: '',
                        dbIndexes: '',
                        tbRule: '',
                        tbSuffix: '',
                        tbSuffixZeroPadding: ''
                    }]
                };
                this.shardRule.push(rule);
            },
            saveRule() {
                let _this = this;
                var ruleNameConfig = {
                    ruleName: _this.ruleName,
                    env: _this.env,
                    tableShardConfigs: _this.shardRule
                };
                axios.post('/i/shard/saveRuleNameConfig', ruleNameConfig).then(function (response) {
                    if (response.data.status === 'success') {
                        _this.$Notice.info({
                            title: '保存成功!'
                        });
                    } else {
                        _this.$Notice.error({
                            title: '保存失败!'
                        });
                    }
                }).catch(function (e) {
                    _this.$Notice.error({
                        title: '保存失败!',
                        desc: e.message
                    });
                });
            }
        },
        created() {
            if (this.$store.state.init && !this.dataInit) {
                this.loadData();
                this.dataInit = true;
            }
        },
        data: function () {
            return {
                theme: 'light',
                env: this.$store.state.currentEnv,
                envs : this.$store.state.envs,
                ruleName: this.$route.params.ruleName,
                stared: false,
                dataInit: false,
                shard: {},
                shardRule: [],
                shardRuleTab:[]
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
