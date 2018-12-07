<template>
    <Form ref="dsConfig" :model="dsConfig" label-position="left" :label-width="100">
        <FormItem prop="jdbcref">
            <span slot="label" style="font-size: 14px;color: #464c5b">jdbcref值</span>
            <Input v-model="dsConfig.jdbcref" placeholder="jdbcref" style="width:285px"/>
        </FormItem>
        <FormItem prop="env">
            <span slot="label" style="font-size: 14px;color: #464c5b">环境</span>
            <Select v-model="dsConfig.env" placeholder="env" :transfer="true" style="width:285px">
                <Option v-for="value in envs" :value="value" :key="value">{{ value }}</Option>
            </Select>
        </FormItem>
        <div v-for="(v, index) in dsConfig.DBAddresses">
        <FormItem prop="DBAddress">
            <span slot="label" style="font-size: 14px;color: #464c5b">数据库地址{{ index }}</span>
            <Input v-model="dsConfig.DBAddresses[index].DBAddress" placeholder="ip:port" style="width: 200px"/>
            <RadioGroup v-model="dsConfig.DBAddresses[index].readOrWrite" type="button" style="margin-left: 4px">
                <Radio label="读" style="padding-left: 8px"></Radio>
                <Radio label="写" style="padding-left: 8px"></Radio>
            </RadioGroup>
            <Tooltip content="流量占比">
            <InputNumber :min="0" v-model="dsConfig.DBAddresses[index].weight" style="margin-left: 4px;width: 40px"
                         :disabled="dsConfig.DBAddresses[index].readOrWrite === '写'">
            </InputNumber>
            </Tooltip>
            <Span v-if="index == 0">
                        <Button type="primary" @click="addDB" shape="circle" icon="plus-round"
                                size="small" style="padding-top: 1px;margin-left: 6px;">
                        </Button>
                    </Span>
            <Span v-else>
                        <Button type="dashed" @click="delDB" shape="circle" icon="minus-round"
                                size="small" style="padding-top: 1px;margin-left: 6px;">
                        </Button>
            </Span>
            <div style="margin-top: 7px">
            <Input v-model="dsConfig.DBAddresses[index].userName" placeholder="userName" style="width:166px"/>
            <Input v-model="dsConfig.DBAddresses[index].password" placeholder="password" type="password" style="width:166px"/>
            </div>
        </FormItem>
        </div>
        <FormItem prop="dbName">
            <span slot="label" style="font-size: 14px;color: #464c5b">数据库名</span>
            <Input v-model="dsConfig.dbName" placeholder="dbName" style="width:285px"/>
        </FormItem>
        <!--<FormItem prop="user">-->
            <!--<span slot="label" style="font-size: 14px;color: #464c5b">用户名和密码</span>-->
            <!--<Input v-model="dsConfig.userName" placeholder="userName" style="width:175px"/>-->
            <!--<Input v-model="dsConfig.password" placeholder="password" type="password" style="width:175px"/>-->
        <!--</FormItem>-->
        <FormItem prop="owner">
            <span slot="label" style="font-size: 14px;color: #464c5b">负责人</span>
            <Input v-model="dsConfig.owner" placeholder="owner" style="width:285px"/>
        </FormItem>
        <FormItem prop="description">
            <span slot="label" style="font-size: 14px;color: #464c5b">描述</span>
            <Input v-model="dsConfig.description" type="textarea" :rows="4" placeholder="description" style="width:285px"/>
        </FormItem>
    </Form>

</template>

<script>
    const axios = require('axios');
    export default {
        name: "jdbcrefConfigForm",
        props: {
            dsConfig:{
                env: null,
                owner: null,
                jdbcref: null,
                dbName: null,
                description: null,
                DBAddresses: []
            }
        },
        watch: {
            init: function () {
                if (this.$store.state.init && !this.dataInit) {
                    this.envs = this.$store.state.envs;
                    this.env = this.env ? _this.env : _this.$store.state.currentEnv;
                    this.dataInit = true;
                }
            },
            env: function () {
               this.changeEnv(this.env);
            }
        },
        data: function () {
            return {
                dataInit : false,
                env : this.$store.state.currentEnv,
                envs: this.$store.state.envs,
            }
        },
        methods: {
            addDB() {
                this.dsConfig.DBAddresses.push({DBAddress: "", readOrWrite: "读", weight: 0});
            },
            delDB() {
                this.dsConfig.DBAddresses.pop();
            }
        }
    }
</script>

<style scoped>

</style>
