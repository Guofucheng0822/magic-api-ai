import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicAiDialog from './components/magic-ai-dialog.vue'
import MagicSchemaPanel from './components/magic-schema-panel.vue'
import 'vite-plugin-svg-icons/register'

export default (opt) => {
    const { i18n, bus, request, modal } = opt

    // 注册 i18n
    i18n.add('zh-cn', localZhCN)
    i18n.add('en', localEn)

    // 将 bus 挂到 window，以便 panel 组件使用
    window.magicEditorBus = bus

    // 注册快捷键 Ctrl+Shift+A
    document.addEventListener('keydown', (e) => {
        if (e.ctrlKey && e.shiftKey && e.key === 'A') {
            e.preventDefault()
            bus.$emit('ai-dialog-open')
        }
    })

    return {
        datasources: [{
            type: 'ai',
            icon: 'ai',
            title: i18n.format('ai.title'),
            name: i18n.format('ai.name'),
            onClick: () => bus.$emit('ai-dialog-open'),
            component: MagicAiDialog
        }, {
            type: 'schema',
            embed: true,
            component: MagicSchemaPanel
        }]
    }
}
