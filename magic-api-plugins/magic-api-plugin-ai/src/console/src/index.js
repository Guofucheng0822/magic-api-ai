import { defineComponent, h, provide } from 'vue'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicAIChat from './components/magic-ai-chat.vue'
import 'vite-plugin-svg-icons/register'

function wrapChat(opt) {
    return defineComponent({
        name: 'MagicAIChatHost',
        setup() {
            provide('request', opt.request)
            provide('constants', opt.constants)
            provide('Message', opt.Message)
            provide('bus', opt.bus)
            provide('i18n.format', opt.i18n.format)
            return () => h(MagicAIChat)
        }
    })
}

export default (opt) => {
    const i18n = opt.i18n
    i18n.add('zh-cn', localZhCN)
    i18n.add('en', localEn)
    return {
        // type 省略 => 始终显示在底部工具条
        toolbars: [{
            id: 'magic-ai-chat',
            title: 'ai.title',
            icon: '#magic-ai-ai',
            component: wrapChat(opt)
        }]
    }
}
