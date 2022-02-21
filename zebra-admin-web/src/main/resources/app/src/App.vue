<template>
    <Layout>
        <Header>
            <top :message="parentData"></top>
        </Header>

        <Layout style="margin-top: 58px">
            <transition name="sidebar-transition">
                <Sider v-show="parentData.showSide" style="width: 220px;min-width: 200px;max-width: 260px;flex: 0 0 220px;">
                    <!--<sidebar></sidebar>-->
                    <side></side>
                </Sider>
            </transition>

            <Content>
                <!--<router-view></router-view>-->
                <router-view></router-view>
            </Content>
        </Layout>

    </Layout>
</template>

<script>
    import top from './page/top.vue'
    import side from './page/side.vue'
    export default {
        data: function () {
            return {
                parentData: {
                    showSide: true
                }
            }
        },
        components: {
            "top": top,
            "side": side
        },
      created() {

      }
    }
</script>
