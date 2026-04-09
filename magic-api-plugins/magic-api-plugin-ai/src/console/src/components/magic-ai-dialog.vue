<template>
    <teleport to=".magic-editor">
        <div v-if="visible" class="magic-ai-panel">
            <div class="magic-ai-panel-header">
                <span>AI Coding 助手</span>
                <span class="magic-ai-close" @click="onClose"><magic-icon icon="close"/></span>
            </div>
            <div class="magic-ai-container">
                <!-- 对话消息区域 -->
                <div class="magic-ai-messages" ref="messagesContainer">
                    <div v-if="messages.length === 0" class="magic-ai-welcome">
                        <div class="magic-ai-welcome-icon">✨</div>
                        <div class="magic-ai-welcome-title">AI Coding 助手</div>
                        <div class="magic-ai-welcome-desc">我可以帮你按照 magic-api 脚本语法生成代码，请描述你的需求。</div>
                        <div class="magic-ai-suggestions">
                            <div class="magic-ai-suggestion" v-for="s in suggestions" :key="s" @click="sendSuggestion(s)">
                                {{ s }}
                            </div>
                        </div>
                    </div>
                    <template v-else>
                        <div
                            v-for="(msg, index) in messages"
                            :key="index"
                            :class="['magic-ai-message', msg.role === 'user' ? 'magic-ai-message--user' : 'magic-ai-message--assistant']"
                        >
                            <div class="magic-ai-message-avatar">
                                <span v-if="msg.role === 'user'">我</span>
                                <span v-else>AI</span>
                            </div>
                            <div class="magic-ai-message-content">
                                <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
                                <div v-else>{{ msg.content }}</div>
                                <!-- 代码操作按钮 -->
                                <div class="magic-ai-code-actions" v-if="msg.role === 'assistant' && extractCode(msg.content)">
                                    <button class="magic-ai-btn magic-ai-btn--insert" @click="insertCode(extractCode(msg.content))">
                                        插入到编辑器
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--replace" @click="replaceCode(extractCode(msg.content))">
                                        替换编辑器内容
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--copy" @click="copyCode(extractCode(msg.content))">
                                        复制代码
                                    </button>
                                </div>
                            </div>
                        </div>
                        <!-- 正在生成 -->
                        <div v-if="loading" class="magic-ai-message magic-ai-message--assistant">
                            <div class="magic-ai-message-avatar"><span>AI</span></div>
                            <div class="magic-ai-message-content">
                                <div class="magic-ai-loading">
                                    <span></span><span></span><span></span>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>

                <!-- 输入区域 -->
                <div class="magic-ai-input-area">
                    <div class="magic-ai-input-toolbar">
                        <label class="magic-ai-context-toggle">
                            <input type="checkbox" v-model="useCurrentCode" />
                            携带当前代码作为上下文
                        </label>
                        <button class="magic-ai-clear-btn" @click="clearMessages" :disabled="messages.length === 0">
                            清空对话
                        </button>
                    </div>
                    <div class="magic-ai-input-wrapper">
                        <textarea
                            ref="inputRef"
                            v-model="inputText"
                            class="magic-ai-textarea"
                            placeholder="描述你需要实现的功能，按 Enter 发送，Shift+Enter 换行..."
                            @keydown="onKeydown"
                            :disabled="loading"
                        ></textarea>
                        <button class="magic-ai-send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">
                            <magic-icon icon="run" />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </teleport>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
    bus: Object,
    request: Object,
    modal: Object
})

const visible = ref(false)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const useCurrentCode = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)

const suggestions = [
    '查询用户列表，支持按名称模糊搜索和分页',
    '根据ID查询用户详情并返回',
    '新增用户，接收POST请求体参数',
    '更新用户信息，根据ID修改',
    '删除用户，根据ID删除',
]

// 从bus事件打开对话框
props.bus.$on('ai-dialog-open', () => {
    visible.value = true
    nextTick(() => inputRef.value && inputRef.value.focus())
})

const onClose = () => {
    visible.value = false
}

const sendSuggestion = (text) => {
    inputText.value = text
    sendMessage()
}

const sendMessage = async () => {
    const text = inputText.value.trim()
    if (!text || loading.value) return

    let currentCode = ''
    if (useCurrentCode.value) {
        props.bus.$emit('ai-get-current-code', (code) => {
            currentCode = code || ''
        })
        await nextTick()
    }

    messages.value.push({ role: 'user', content: text })
    inputText.value = ''
    loading.value = true
    scrollToBottom()

    const history = messages.value.slice(0, -1).slice(-10).map(m => ({
        role: m.role,
        content: m.content
    }))

    props.request.sendJson('/ai/chat', {
        message: text,
        history,
        currentCode: useCurrentCode.value ? currentCode : ''
    }).success(response => {
        messages.value.push({ role: 'assistant', content: response.message })
        scrollToBottom()
    }).error(() => {
        messages.value.push({ role: 'assistant', content: '❌ 请求失败，请检查AI配置是否正确。' })
        scrollToBottom()
    }).end(() => {
        loading.value = false
    })
}

const onKeydown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        sendMessage()
    }
}

const clearMessages = () => {
    messages.value = []
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
    })
}

const extractCode = (content) => {
    if (!content) return null
    const match = content.match(/```(?:magicscript|ms)?\s*\n?([\s\S]*?)```/)
    return match ? match[1].trim() : null
}

const insertCode = (code) => {
    props.bus.$emit('ai-insert-code', code)
    props.modal.message('代码已插入到编辑器')
}

const replaceCode = (code) => {
    props.modal.confirm('提示', '确定要替换当前编辑器中的全部内容吗？', () => {
        props.bus.$emit('ai-replace-code', code)
        props.modal.message('代码已替换')
    })
}

const copyCode = (code) => {
    navigator.clipboard.writeText(code).then(() => {
        props.modal.message('代码已复制到剪贴板')
    }).catch(() => {
        props.modal.alert('复制失败，请手动复制')
    })
}

const renderMarkdown = (content) => {
    if (!content) return ''
    let html = content
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')

    html = html.replace(/```(?:magicscript|ms|java|sql|json|javascript|js)?\s*\n?([\s\S]*?)```/g, (match, code) => {
        return `<div class="magic-ai-code-block"><pre><code>${code.trim()}</code></pre></div>`
    })

    html = html.replace(/`([^`\n]+)`/g, '<code class="magic-ai-inline-code">$1</code>')
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
    html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
    html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')
    html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
    html = html.replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>')
    html = html.replace(/<\/ul>\s*<ul>/g, '')
    html = html.replace(/\n/g, '<br/>')

    return html
}

defineExpose({ visible })
</script>

<style scoped>
.magic-ai-panel {
    position: absolute;
    top: var(--magic-header-height);
    right: 0;
    bottom: 0;
    width: 380px;
    display: flex;
    flex-direction: column;
    background: var(--main-background-color);
    border-left: 1px solid var(--main-border-color);
    z-index: 100;
}
.magic-ai-panel-header {
    height: var(--magic-header-height);
    line-height: var(--magic-header-height);
    padding: 0 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--main-border-color);
    font-size: 13px;
    font-weight: bold;
    color: var(--main-color);
    flex-shrink: 0;
}
.magic-ai-close {
    cursor: pointer;
    display: flex;
    align-items: center;
    opacity: 0.6;
}
.magic-ai-close:hover { opacity: 1; }
.magic-ai-close :deep(.magic-icon) {
    width: 14px;
    height: 14px;
}
.magic-ai-container {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    background: var(--main-background-color);
}
.magic-ai-messages {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 0;
}
.magic-ai-welcome {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--main-color);
    opacity: 0.8;
}
.magic-ai-welcome-icon { font-size: 48px; margin-bottom: 10px; }
.magic-ai-welcome-title { font-size: 18px; font-weight: bold; margin-bottom: 8px; }
.magic-ai-welcome-desc {
    font-size: 13px;
    color: var(--empty-color);
    margin-bottom: 20px;
    text-align: center;
}
.magic-ai-suggestions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    max-width: 480px;
}
.magic-ai-suggestion {
    padding: 8px 14px;
    border: 1px solid var(--main-border-color);
    border-radius: 6px;
    cursor: pointer;
    font-size: 12px;
    color: var(--main-color);
    transition: background 0.15s;
    text-align: left;
}
.magic-ai-suggestion:hover {
    background: var(--select-option-hover-background-color);
    color: var(--select-option-hover-color);
}
.magic-ai-message {
    display: flex;
    gap: 10px;
    align-items: flex-start;
}
.magic-ai-message--user { flex-direction: row-reverse; }
.magic-ai-message-avatar {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: bold;
    flex-shrink: 0;
}
.magic-ai-message--user .magic-ai-message-avatar { background: #1976d2; color: #fff; }
.magic-ai-message--assistant .magic-ai-message-avatar { background: #43a047; color: #fff; }
.magic-ai-message-content {
    max-width: 85%;
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--main-color);
    background: var(--select-option-hover-background-color);
    word-break: break-word;
}
.magic-ai-message--user .magic-ai-message-content {
    background: #1565c0;
    color: #fff;
    border-radius: 8px 2px 8px 8px;
}
.magic-ai-message--assistant .magic-ai-message-content { border-radius: 2px 8px 8px 8px; }
.magic-ai-code-actions {
    display: flex;
    gap: 6px;
    margin-top: 8px;
    flex-wrap: wrap;
}
.magic-ai-btn {
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 11px;
    cursor: pointer;
    border: 1px solid;
    transition: all 0.15s;
}
.magic-ai-btn--insert { background: #43a047; color: #fff; border-color: #43a047; }
.magic-ai-btn--insert:hover { background: #388e3c; }
.magic-ai-btn--replace { background: #1976d2; color: #fff; border-color: #1976d2; }
.magic-ai-btn--replace:hover { background: #1565c0; }
.magic-ai-btn--copy { background: transparent; color: var(--main-color); border-color: var(--main-border-color); }
.magic-ai-btn--copy:hover { background: var(--select-option-hover-background-color); }
.magic-ai-loading {
    display: flex;
    gap: 4px;
    align-items: center;
    padding: 4px 0;
}
.magic-ai-loading span {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--main-icon-color);
    animation: aiDot 1.2s infinite;
}
.magic-ai-loading span:nth-child(2) { animation-delay: 0.2s; }
.magic-ai-loading span:nth-child(3) { animation-delay: 0.4s; }
@keyframes aiDot {
    0%, 60%, 100% { transform: scale(0.6); opacity: 0.4; }
    30% { transform: scale(1); opacity: 1; }
}
.magic-ai-input-area {
    border-top: 1px solid var(--main-border-color);
    padding: 8px 12px 10px;
    background: var(--main-background-color);
    flex-shrink: 0;
}
.magic-ai-input-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 12px;
    color: var(--empty-color);
}
.magic-ai-context-toggle {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    user-select: none;
}
.magic-ai-context-toggle input { cursor: pointer; accent-color: #1976d2; }
.magic-ai-clear-btn {
    padding: 2px 8px;
    background: transparent;
    border: 1px solid var(--main-border-color);
    border-radius: 3px;
    font-size: 11px;
    cursor: pointer;
    color: var(--empty-color);
}
.magic-ai-clear-btn:hover:not(:disabled) {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
}
.magic-ai-clear-btn:disabled { opacity: 0.4; cursor: default; }
.magic-ai-input-wrapper {
    display: flex;
    gap: 8px;
    align-items: flex-end;
}
.magic-ai-textarea {
    flex: 1;
    min-height: 60px;
    max-height: 120px;
    padding: 8px 10px;
    border: 1px solid var(--main-border-color);
    border-radius: 6px;
    resize: none;
    font-size: 13px;
    background: var(--main-background-color);
    color: var(--main-color);
    font-family: inherit;
    line-height: 1.5;
    outline: none;
    transition: border-color 0.15s;
}
.magic-ai-textarea:focus { border-color: #1976d2; }
.magic-ai-textarea:disabled { opacity: 0.6; cursor: not-allowed; }
.magic-ai-send-btn {
    width: 38px;
    height: 38px;
    border-radius: 6px;
    background: #43a047;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: background 0.15s;
}
.magic-ai-send-btn:hover:not(:disabled) { background: #388e3c; }
.magic-ai-send-btn:disabled {
    background: var(--main-border-color);
    cursor: not-allowed;
    opacity: 0.6;
}
.magic-ai-send-btn :deep(.magic-icon) { fill: #fff; }
</style>

<style>
.magic-ai-message-content .magic-ai-code-block {
    background: #1e1e1e;
    border-radius: 6px;
    margin: 8px 0;
    overflow: hidden;
}
.magic-ai-message-content .magic-ai-code-block pre {
    margin: 0;
    padding: 12px;
    overflow-x: auto;
}
.magic-ai-message-content .magic-ai-code-block code {
    color: #d4d4d4;
    font-family: 'JetBrainsMono', Consolas, 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre;
}
.magic-ai-message-content .magic-ai-inline-code {
    background: rgba(0, 0, 0, 0.15);
    padding: 1px 5px;
    border-radius: 3px;
    font-family: monospace;
    font-size: 12px;
}
.magic-ai-message--user .magic-ai-message-content .magic-ai-inline-code {
    background: rgba(255, 255, 255, 0.2);
}
.magic-ai-message-content h2,
.magic-ai-message-content h3,
.magic-ai-message-content h4 {
    margin: 8px 0 4px;
    font-weight: bold;
}
.magic-ai-message-content ul {
    margin: 4px 0;
    padding-left: 20px;
}
.magic-ai-message-content li { margin: 2px 0; }
</style>
