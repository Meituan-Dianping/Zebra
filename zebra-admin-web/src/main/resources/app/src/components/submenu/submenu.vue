<template>
    <li :class="classes">
        <div :class="[baseClass + '-submenu-title']" ref="reference" @click.stop="handleClick" :style="titleStyle">
            <slot name="title"></slot>
            <Icon type="ios-arrow-down" class="ivu-menu-submenu-title-icon"></Icon>
        </div>
        <collapse-transition>
            <ul :class="[baseClass]" v-show="opened">
                <slot></slot>
            </ul>
        </collapse-transition>
    </li>
</template>
<script>
    import Icon from '../icon/icon.vue';
    import CollapseTransition from '../base/collapse-transition';
    import { getStyle, findComponentUpward, findComponentsDownward } from '../base/assist';
    import mixin from './mixin';

    const baseClass = 'ivu-menu';

    export default {
        name: 'Submenu',
        components: { Icon, CollapseTransition },
        mixins: [mixin],
        props: {
            name: {
                type: [String, Number],
                required: true
            }
        },
        data() {
            return {
                baseClass: baseClass,
                active: false,
                opened: false,
                dropWidth: parseFloat(getStyle(this.$el, 'width'))
            };
        },
        computed: {
            classes() {
                return [
                    `${baseClass}-submenu`,
                    {
                        [`${baseClass}-item-active`]: this.active && !this.hasParentSubmenu,
                        [`${baseClass}-opened`]: this.opened,
                        [`${baseClass}-submenu-has-parent-submenu`]: this.hasParentSubmenu,
                        [`${baseClass}-child-item-active`]: this.active
                    }
                ];
            },
            accordion() {
                return this.menu.accordion;
            },
            dropStyle() {
                let style = {};

                if (this.dropWidth) style.minWidth = `${this.dropWidth}px`;
                return style;
            },
            titleStyle() {
                return this.hasParentSubmenu ? {
                    paddingLeft: 43 + (this.parentSubmenuNum - 1) * 24 + 'px'
                } : {};
            }
        },
        methods: {
            handleClick() {
                if (this.mode === 'horizontal') return;
                const opened = this.opened;
                this.$emit('on-submenu-select', this.name);
                
                if (this.accordion) {
                    this.$parent.$children.forEach(item => {
                        if (item.$options.name === 'Submenu') item.opened = false;
                    });
                }
                this.opened = !opened;
                this.menu.updateOpenKeys(this.name);
            }
        },
        mounted() {
            this.$on('on-menu-item-select', (name) => {
                this.dispatch('Menu', 'on-menu-item-select', name);
                return true;
            });
            this.$on('on-update-active-name', (status) => {
                if (findComponentUpward(this, 'Submenu')) this.dispatch('Submenu', 'on-update-active-name', status);
                if (findComponentsDownward(this, 'Submenu')) findComponentsDownward(this, 'Submenu').forEach(item => {
                    item.active = false;
                });
                this.active = status;
            });
        }
    };
</script>