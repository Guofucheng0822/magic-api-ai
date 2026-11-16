<template>
    <teleport :to="popupHost || '.magic-editor'">
        <div v-if="visible" class="magic-ai-panel" :class="{ 'magic-ai-panel--window': popupHost }" :style="panelStyle">
            <div v-if="!popupHost" class="magic-ai-resize" @mousedown.prevent="startResize"></div>
            <div class="magic-ai-panel-header">
                <div class="magic-ai-header-left">
                    <span class="magic-ai-header-icon">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M12 2a4 4 0 0 1 4 4v1a3 3 0 0 1 3 3v1a2 2 0 0 1-2 2h-1l-2 5H10l-2-5H7a2 2 0 0 1-2-2v-1a3 3 0 0 1 3-3V6a4 4 0 0 1 4-4z"/>
                            <circle cx="9" cy="9" r="1"/><circle cx="15" cy="9" r="1"/>
                        </svg>
                    </span>
                    <span class="magic-ai-header-title">AI Coding 助手</span>
                </div>
                <div class="magic-ai-header-actions">
                    <button class="magic-ai-header-btn" @click="newChat">新对话</button>
                    <button class="magic-ai-header-btn" @click="showHistory = !showHistory">历史</button>
                    <button class="magic-ai-header-btn" @click="diagnoseError">诊断</button>
                    <button class="magic-ai-header-btn" @click="toggleWindow">{{ popupHost ? '收回' : '独立窗口' }}</button>
                    <span class="magic-ai-close" @click="onClose">
                        <magic-icon icon="close"/>
                    </span>
                </div>
            </div>
            <div v-if="createForm" class="magic-ai-create-mask">
                <div class="magic-ai-create">
                    <div class="magic-ai-create-title">新建接口</div>
                    <label>名称</label>
                    <input v-model="createForm.name" />
                    <label>路径</label>
                    <input v-model="createForm.path" />
                    <label>方法</label>
                    <select v-model="createForm.method">
                        <option>GET</option>
                        <option>POST</option>
                        <option>PUT</option>
                        <option>DELETE</option>
                    </select>
                    <label>参数</label>
                    <input v-model="createForm.parameters" placeholder="多个参数用逗号分隔，例如 dnum" />
                    <div class="magic-ai-create-hint">保存为本地新文件，不会覆盖当前打开的接口。脚本里的 #{参数} 会自动填到这里。</div>
                    <div class="magic-ai-create-actions">
                        <button type="button" class="magic-ai-create-save" @click="submitCreate" :disabled="createForm.saving">保存</button>
                        <button type="button" class="magic-ai-create-cancel" @click="createForm = null">取消</button>
                    </div>
                </div>
            </div>
            <div v-if="showHistory" class="magic-ai-history" @scroll="historyMenu = null">
                <div v-if="sessions.length === 0" class="magic-ai-history-empty">还没有历史会话</div>
                <div v-for="session in sessions" :key="session.id" class="magic-ai-history-item" @contextmenu.prevent="openHistoryMenu($event, session)">
                    <input
                        v-if="editingId === session.id"
                        ref="renameInput"
                        class="magic-ai-history-input"
                        v-model="editingTitle"
                        maxlength="40"
                        @click.stop
                        @keydown.enter.prevent="saveTitle(session)"
                        @keydown.esc.prevent="editingId = null"
                        @blur="saveTitle(session)"
                    />
                    <button v-else type="button" class="magic-ai-history-title" @click="openSession(session)">{{ session.title }}</button>
                </div>
            </div>
            <div v-if="historyMenu" class="magic-ai-history-menu" :style="{ left: historyMenu.x + 'px', top: historyMenu.y + 'px' }" @click.stop @contextmenu.prevent>
                <button type="button" @click="renameFromMenu">修改名称</button>
                <button type="button" class="magic-ai-history-menu-danger" @click="deleteFromMenu">删除</button>
            </div>
            <div class="magic-ai-container">
                <!-- 对话消息区域 -->
                <div class="magic-ai-messages" ref="messagesContainer">
                    <div v-if="messages.length === 0" class="magic-ai-welcome">
                        <div class="magic-ai-welcome-icon-wrap">
                            <svg class="magic-ai-welcome-svg" viewBox="0 0 64 64" width="52" height="52" fill="none">
                                <defs>
                                    <linearGradient id="aiGrad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
                                        <stop offset="0%" stop-color="#5b7ee5"/>
                                        <stop offset="100%" stop-color="#4da6e8"/>
                                    </linearGradient>
                                </defs>
                                <rect rx="16" width="64" height="64" fill="url(#aiGrad)" opacity="0.12"/>
                                <path d="M32 14a8 8 0 0 1 8 8v2a6 6 0 0 1 6 6v2a4 4 0 0 1-4 4h-2l-4 10h-8l-4-10h-2a4 4 0 0 1-4-4v-2a6 6 0 0 1 6-6v-2a8 8 0 0 1 8-8z" stroke="url(#aiGrad)" stroke-width="2.5" fill="none"/>
                                <circle cx="26" cy="26" r="2" fill="#5b7ee5"/>
                                <circle cx="38" cy="26" r="2" fill="#4da6e8"/>
                                <path d="M28 31c1.5 2 6.5 2 8 0" stroke="url(#aiGrad)" stroke-width="2" stroke-linecap="round" fill="none"/>
                            </svg>
                        </div>
                        <div class="magic-ai-welcome-title">AI Coding 助手</div>
                        <div class="magic-ai-welcome-desc">我可以帮你按照 magic-api 脚本语法生成代码，请描述你的需求。</div>
                        <div class="magic-ai-suggestions">
                            <div class="magic-ai-suggestion" v-for="s in suggestions" :key="s" @click="sendSuggestion(s)">
                                <span class="magic-ai-suggestion-icon">
                                    <svg viewBox="0 0 16 16" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><path d="M6 3l5 5-5 5"/></svg>
                                </span>
                                <span>{{ s }}</span>
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
                                <span v-if="msg.role === 'user'">
                                    <svg viewBox="0 0 20 20" width="16" height="16" fill="currentColor"><path d="M10 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm-7 8a7 7 0 0 1 14 0H3z"/></svg>
                                </span>
                                <span v-else>
                                    <svg viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10 3a3.5 3.5 0 0 1 3.5 3.5V8A2.5 2.5 0 0 1 16 10.5V12a2 2 0 0 1-2 2h-.5L12 17H8l-1.5-3H6a2 2 0 0 1-2-2v-1.5A2.5 2.5 0 0 1 6.5 8v-1.5A3.5 3.5 0 0 1 10 3z"/><circle cx="8" cy="9" r="0.8" fill="currentColor"/><circle cx="12" cy="9" r="0.8" fill="currentColor"/></svg>
                                </span>
                            </div>
                            <div :class="['magic-ai-message-body', msg.queryResult ? 'magic-ai-message-body--result' : '']">
                                <div class="magic-ai-message-content">
                                    <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
                                    <div v-else>{{ msg.content }}</div>
                                    <span v-if="streaming && msg.role === 'assistant' && index === messages.length - 1" class="magic-ai-cursor">▍</span>
                                </div>
                                <!-- 代码操作按钮 -->
                                <div class="magic-ai-code-actions" v-if="msg.role === 'assistant' && !streaming && (extractCode(msg.content) || isApiDoc(msg.content))">
                                    <template v-if="extractCode(msg.content)">
                                    <button class="magic-ai-btn magic-ai-btn--insert" @click="insertCode(extractCode(msg.content))">
                                        <svg viewBox="0 0 16 16" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M8 3v10M3 8h10"/></svg>
                                        插入代码
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--replace" @click="replaceCode(extractCode(msg.content))">
                                        <svg viewBox="0 0 16 16" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M2 8h12M12 4l4 4-4 4"/></svg>
                                        替换代码
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--copy" @click="copyCode(extractCode(msg.content))">
                                        <svg viewBox="0 0 16 16" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="5" y="5" width="9" height="9" rx="1.5"/><path d="M5 11H3.5A1.5 1.5 0 0 1 2 9.5v-7A1.5 1.5 0 0 1 3.5 1h7A1.5 1.5 0 0 1 12 2.5V5"/></svg>
                                        复制
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--create" @click="createApi(extractCode(msg.content))">
                                        新建接口
                                    </button>
                                    <button class="magic-ai-btn magic-ai-btn--run" @click="runQuery(msg, extractCode(msg.content))" :disabled="msg.querying">
                                        执行查询
                                    </button>
                                    <button v-if="isReadQuery(extractCode(msg.content))" class="magic-ai-btn magic-ai-btn--copy" @click="exportQuery(msg, extractCode(msg.content))" :disabled="msg.exporting">
                                        导出数据
                                    </button>
                                    </template>
                                    <button v-if="isApiDoc(msg.content)" class="magic-ai-btn magic-ai-btn--copy" @click="exportDoc(msg.content)">
                                        导出文档
                                    </button>
                                </div>
                                <div v-if="msg.querying" class="magic-ai-query-status">正在查询...</div>
                                <div v-if="msg.exporting" class="magic-ai-query-status">正在导出...</div>
                                <div v-if="msg.queryError" class="magic-ai-query-status">{{ msg.queryError }}</div>
                                <div v-if="msg.exportError" class="magic-ai-query-status">{{ msg.exportError }}</div>
                                <div v-if="msg.queryResult" class="magic-ai-query-result">
                                    <div v-for="(section, sectionIndex) in querySections(msg.queryResult)" :key="sectionIndex" class="magic-ai-query-section">
                                        <div v-if="sectionKind(section) === 'empty'" class="magic-ai-query-summary">{{ section.name || '结果' }}：没有数据</div>
                                        <div v-else-if="sectionKind(section) === 'scalar'" class="magic-ai-query-summary">{{ section.name || '结果' }}：{{ section.rows[0][0] }}</div>
                                        <template v-else>
                                            <div class="magic-ai-query-summary">{{ section.name || '结果' }}：{{ section.total }} 条<span v-if="section.truncated">，这里显示前 {{ section.rows.length }} 条</span></div>
                                            <div class="magic-ai-query-table-wrap">
                                                <table>
                                                    <thead>
                                                        <tr><th v-for="col in section.columns" :key="col">{{ col }}</th></tr>
                                                    </thead>
                                                    <tbody>
                                                        <tr v-for="(row, rowIndex) in section.rows" :key="rowIndex">
                                                            <td v-for="(cell, cellIndex) in row" :key="cellIndex" :title="cell">{{ cell }}</td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- 正在等待（非流式阶段才显示loading动画） -->
                        <div v-if="loading && !streaming" class="magic-ai-message magic-ai-message--assistant">
                            <div class="magic-ai-message-avatar">
                                <span>
                                    <svg viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10 3a3.5 3.5 0 0 1 3.5 3.5V8A2.5 2.5 0 0 1 16 10.5V12a2 2 0 0 1-2 2h-.5L12 17H8l-1.5-3H6a2 2 0 0 1-2-2v-1.5A2.5 2.5 0 0 1 6.5 8v-1.5A3.5 3.5 0 0 1 10 3z"/><circle cx="8" cy="9" r="0.8" fill="currentColor"/><circle cx="12" cy="9" r="0.8" fill="currentColor"/></svg>
                                </span>
                            </div>
                            <div class="magic-ai-message-body">
                                <div class="magic-ai-message-content">
                                    <div class="magic-ai-loading">
                                        <span></span><span></span><span></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>

                <div v-if="lastError" class="magic-ai-run-error">
                    <div class="magic-ai-run-error-text">{{ lastError.title || '运行日志' }}：{{ lastError.message }}</div>
                    <div class="magic-ai-run-error-actions">
                        <button type="button" @click="diagnoseError">诊断</button>
                        <button type="button" @click="lastError = null">忽略</button>
                    </div>
                </div>
                <!-- 输入区域 -->
                <div class="magic-ai-input-area">
                    <!-- 模型选择器 -->
                    <div class="magic-ai-model-selector" v-if="configLoaded && selectableProviders.length > 0">
                        <div class="magic-ai-select-wrap">
                            <select class="magic-ai-select" :value="activeProvider" @change="onProviderChange">
                                <option v-for="p in selectableProviders" :key="p.key" :value="p.key">{{ p.label }}</option>
                            </select>
                            <svg class="magic-ai-select-arrow" viewBox="0 0 12 12" width="10" height="10"><path d="M3 5l3 3 3-3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        </div>
                        <div class="magic-ai-select-wrap magic-ai-select-wrap--model">
                            <select class="magic-ai-select" :value="activeModel" @change="onModelChange">
                                <option v-for="m in availableModels" :key="m" :value="m">{{ m }}</option>
                            </select>
                            <svg class="magic-ai-select-arrow" viewBox="0 0 12 12" width="10" height="10"><path d="M3 5l3 3 3-3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        </div>
                    </div>
                    <div v-if="focusTable" class="magic-ai-focus">
                        <span>下一句带上表 {{ focusTable }}</span>
                        <button type="button" @click="focusTable = ''">取消</button>
                    </div>
                    <div class="magic-ai-input-toolbar">
                        <label class="magic-ai-context-toggle">
                            <input type="checkbox" v-model="useCurrentCode" />
                            <span class="magic-ai-toggle-text">携带当前代码上下文</span>
                        </label>
                        <button class="magic-ai-clear-btn" @click="clearMessages" :disabled="messages.length === 0">
                            <svg viewBox="0 0 16 16" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><path d="M2 4h12M5 4V3a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v1M6 7v5M10 7v5M4 4l1 10h6l1-10"/></svg>
                            清空
                        </button>
                    </div>
                    <div class="magic-ai-input-wrapper">
                        <textarea
                            ref="inputRef"
                            v-model="inputText"
                            class="magic-ai-textarea"
                            placeholder="描述你需要实现的功能... Enter 发送，Shift+Enter 换行"
                            @keydown="onKeydown"
                            :disabled="loading"
                            rows="3"
                        ></textarea>
                        <button class="magic-ai-send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">
                            <svg viewBox="0 0 20 20" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 10l6-6 6 6M10 4v12"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </teleport>
</template>

<script setup>
import { ref, nextTick, computed, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
    bus: Object,
    request: Object,
    modal: Object
})

const HISTORY_KEY = 'magic-ai-sessions'
const CURRENT_KEY = 'magic-ai-current'
const WIDTH_KEY = 'magic-ai-panel-width'

const readJson = (key, fallback) => {
    try {
        const text = localStorage.getItem(key)
        return text ? JSON.parse(text) : fallback
    } catch (e) {
        return fallback
    }
}

const visible = ref(false)
const panelWidth = ref(Number(localStorage.getItem(WIDTH_KEY)) || 420)
const popupHost = ref(null)
let popupWindow = null
const lastError = ref(null)
const recentSql = ref('')
const focusTable = ref('')
const createForm = ref(null)
const messages = ref(readJson(CURRENT_KEY, []))
const sessions = ref(readJson(HISTORY_KEY, []))
const showHistory = ref(false)
const editingId = ref(null)
const editingTitle = ref('')
const renameInput = ref(null)
const historyMenu = ref(null)
const inputText = ref('')
const loading = ref(false)
const streaming = ref(false)  // 流式输出中
const useCurrentCode = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)

// 模型选择相关状态
const activeProvider = ref('')
const activeModel = ref('')
const configuredProviders = ref([]) // [{key, label, models}]
const configLoaded = ref(false)

const STORAGE_KEY_PROVIDER = 'magic-ai-provider'
const STORAGE_KEY_MODEL = 'magic-ai-model'

// 从 localStorage 读取上次选择
const loadLocalSelection = () => {
    try {
        const p = localStorage.getItem(STORAGE_KEY_PROVIDER)
        const m = localStorage.getItem(STORAGE_KEY_MODEL)
        if (p) activeProvider.value = p
        if (m) activeModel.value = m
    } catch (e) { /* ignore */ }
}

// 保存选择到 localStorage
const saveLocalSelection = () => {
    try {
        localStorage.setItem(STORAGE_KEY_PROVIDER, activeProvider.value)
        localStorage.setItem(STORAGE_KEY_MODEL, activeModel.value)
    } catch (e) { /* ignore */ }
}

// 过滤出后端已配置的提供商
const selectableProviders = computed(() => {
    return configuredProviders.value.map(p => ({ key: p.key, label: p.label || p.key }))
})

// 当前提供商下的可用模型
const availableModels = computed(() => {
    const provider = activeProvider.value
    const found = configuredProviders.value.find(p => p.key === provider)
    return found && found.models ? found.models : []
})

// 从后端加载已配置的提供商列表，并恢复本地选择
const loadAiConfig = () => {
    props.request.sendJson('/ai/config/get', {}).success(data => {
        if (Array.isArray(data)) {
            configuredProviders.value = data
        }
        // 恢复 localStorage 中的选择
        loadLocalSelection()
        // 如果本地没有选择或选择的提供商已不存在，使用第一个提供商
        const providerKeys = configuredProviders.value.map(p => p.key)
        if (!activeProvider.value || !providerKeys.includes(activeProvider.value)) {
            if (configuredProviders.value.length > 0) {
                activeProvider.value = configuredProviders.value[0].key
                const models = configuredProviders.value[0].models || []
                activeModel.value = models[0] || ''
                saveLocalSelection()
            }
        } else {
            // 验证当前模型是否仍在可用列表中
            const found = configuredProviders.value.find(p => p.key === activeProvider.value)
            const models = found && found.models ? found.models : []
            if (activeModel.value && !models.includes(activeModel.value)) {
                activeModel.value = models[0] || ''
                saveLocalSelection()
            }
        }
        configLoaded.value = true
    }).error(() => {
        configLoaded.value = true
    })
}

const onProviderChange = (e) => {
    const newProvider = e.target.value
    activeProvider.value = newProvider
    // 自动选择新提供商的第一个模型
    const found = configuredProviders.value.find(p => p.key === newProvider)
    const models = found && found.models ? found.models : []
    activeModel.value = models[0] || ''
    saveLocalSelection()
}

const onModelChange = (e) => {
    activeModel.value = e.target.value
    saveLocalSelection()
}

const suggestions = [
    '按 dnum 查询 ClickHouse operate 最近 100 条，并新建接口',
    '按 dnum 查询 ClickHouse response，再关联 MySQL 设备信息',
    '用 redis.scan 和 redis.get 查询一个 key，只读',
    '写一个 GET 接口，返回固定 JSON',
]

watch(messages, (value) => {
    try {
        localStorage.setItem(CURRENT_KEY, JSON.stringify(value))
    } catch (e) { /* ignore */ }
}, { deep: true })

const persistSessions = () => {
    try {
        localStorage.setItem(HISTORY_KEY, JSON.stringify(sessions.value.slice(0, 30)))
    } catch (e) { /* ignore */ }
}

const archiveCurrent = () => {
    const list = messages.value.filter(item => item && item.content)
    if (!list.length) return
    const firstUser = list.find(item => item.role === 'user')
    const title = ((firstUser && firstUser.content) || list[0].content).slice(0, 28)
    sessions.value.unshift({
        id: Date.now(),
        title,
        messages: list.map(item => ({ role: item.role, content: item.content }))
    })
    sessions.value = sessions.value.slice(0, 30)
    persistSessions()
}

const newChat = () => {
    archiveCurrent()
    messages.value = []
    showHistory.value = false
    historyMenu.value = null
}

const openSession = (session) => {
    archiveCurrent()
    messages.value = (session.messages || []).map(item => ({ role: item.role, content: item.content }))
    showHistory.value = false
    scrollToBottom()
}

const openHistoryMenu = (event, session) => {
    historyMenu.value = { session, x: event.clientX, y: event.clientY }
}

const closeHistoryMenu = () => {
    historyMenu.value = null
}

onMounted(() => document.addEventListener('click', closeHistoryMenu))
onBeforeUnmount(() => {
    document.removeEventListener('click', closeHistoryMenu)
    closePopup()
})

const renameFromMenu = () => {
    const session = historyMenu.value && historyMenu.value.session
    historyMenu.value = null
    if (session) {
        startRename(session)
    }
}

const deleteFromMenu = () => {
    const session = historyMenu.value && historyMenu.value.session
    historyMenu.value = null
    if (!session) {
        return
    }
    props.modal.confirm('删除会话', '删除「' + (session.title || '未命名') + '」？', () => {
        sessions.value = sessions.value.filter(item => item.id !== session.id)
        if (editingId.value === session.id) {
            editingId.value = null
        }
        persistSessions()
    })
}

const startRename = (session) => {
    editingId.value = session.id
    editingTitle.value = session.title || ''
    nextTick(() => {
        const input = Array.isArray(renameInput.value) ? renameInput.value[0] : renameInput.value
        if (input && input.focus) {
            input.focus()
            input.select()
        }
    })
}

const saveTitle = (session) => {
    if (editingId.value !== session.id) {
        return
    }
    const title = editingTitle.value.trim().slice(0, 40)
    editingId.value = null
    if (!title || title === session.title) {
        return
    }
    session.title = title
    persistSessions()
}

const currentScript = () => {
    let code = ''
    props.bus.$emit('ai-get-current-code', (current) => {
        code = current || ''
    })
    return code
}

const compactLog = (text) => {
    const lines = String(text || '').split(/\r?\n/)
    const kept = []
    let frames = 0
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i]
        if (/^\s+at /.test(line)) {
            frames++
            if (frames > 3) {
                continue
            }
        }
        kept.push(line)
        if (kept.length >= 24) {
            break
        }
    }
    return kept.join('\n').trim()
}

const showRunNotice = () => {
    visible.value = true
    if (!configLoaded.value) {
        loadAiConfig()
    }
}

const rememberRunLog = (lines) => {
    const list = Array.isArray(lines) ? lines : []
    for (let i = 0; i < list.length; i++) {
        const text = String(list[i] || '')
        if (/执行SQL|Execute SQL/.test(text)) {
            recentSql.value = compactLog(text)
        }
        if (!/\sERROR\s/.test(text)) {
            continue
        }
        const message = compactLog(text)
        if (!message) {
            continue
        }
        const sql = recentSql.value ? ('执行的 SQL：\n' + recentSql.value + '\n\n') : ''
        lastError.value = { title: '运行日志有错误', message: sql + message }
        showRunNotice()
    }
}

const diagnoseError = () => {
    if (!lastError.value || !lastError.value.message) {
        props.modal.alert('先在控制台运行一次，等运行日志里出现 ERROR 后再诊断', '诊断')
        return
    }
    visible.value = true
    if (!configLoaded.value) {
        loadAiConfig()
    }
    useCurrentCode.value = true
    inputText.value = '请根据下面的运行情况诊断。接口返回给调用方的失败信息仍然是「系统内部出现错误」。说明原因，并给出一份可直接替换的完整脚本。先不要说已经改好，我确认后再点「替换代码」。\n\n'
        + lastError.value.message
    sendMessage()
}

if (props.bus && props.bus.$event) {
    props.bus.$event('log', ([lines]) => rememberRunLog(lines))
    props.bus.$event('logs', ([lines]) => rememberRunLog(lines))
}

props.bus.$on('ai-focus-table', (table) => {
    const name = table && table.name ? String(table.name) : ''
    if (!name) {
        return
    }
    focusTable.value = name
    notify('下一句会带上表 ' + name)
})

props.bus.$on('ai-run-result', (body) => {
    const text = typeof body === 'string' ? body : ''
    let parsed = null
    try {
        parsed = JSON.parse(text)
    } catch (e) {
        return
    }
    if (!parsed || parsed.code !== 1 || !Array.isArray(parsed.data) || parsed.data.length > 0) {
        return
    }
    const sql = recentSql.value ? ('\n执行的 SQL：\n' + recentSql.value) : ''
    lastError.value = {
        title: '运行结果是空列表',
        message: '接口返回成功，data 是空列表。请判断是查询条件没有命中数据，还是 SQL 写错了。' + sql
    }
    showRunNotice()
})

// 从bus事件打开对话框
props.bus.$on('ai-dialog-open', () => {
    visible.value = true
    if (!configLoaded.value) {
        loadAiConfig()
    }
    nextTick(() => inputRef.value && inputRef.value.focus())
})

const panelStyle = computed(() => popupHost.value ? null : { width: clampWidth(panelWidth.value) + 'px' })

const clampWidth = (width) => Math.min(Math.max(width || 420, 360), Math.max(360, window.innerWidth - 80))

const startResize = (event) => {
    const startX = event.clientX
    const startWidth = clampWidth(panelWidth.value)
    const move = (e) => {
        panelWidth.value = clampWidth(startWidth + startX - e.clientX)
    }
    const up = () => {
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', up)
        localStorage.setItem(WIDTH_KEY, String(panelWidth.value))
    }
    document.addEventListener('mousemove', move)
    document.addEventListener('mouseup', up)
}

const closePopup = () => {
    popupHost.value = null
    if (popupWindow && !popupWindow.closed) {
        const win = popupWindow
        popupWindow = null
        win.close()
    }
    popupWindow = null
}

const toggleWindow = () => {
    if (popupHost.value) {
        closePopup()
        return
    }
    const win = window.open('', 'magic-ai-window', 'width=1040,height=760,resizable=yes')
    if (!win) {
        notify('浏览器拦截了独立窗口，请允许弹出窗口')
        return
    }
    win.document.open()
    win.document.write('<!DOCTYPE html><html><head><meta charset="utf-8"><title>AI Coding 助手</title></head><body class="magic-editor"><div id="magic-ai-host"></div></body></html>')
    win.document.close()
    document.querySelectorAll('link[rel="stylesheet"]').forEach((node) => {
        const link = win.document.createElement('link')
        link.rel = 'stylesheet'
        link.href = node.href
        win.document.head.appendChild(link)
    })
    document.querySelectorAll('style').forEach((node) => {
        win.document.head.appendChild(node.cloneNode(true))
    })
    const style = win.document.createElement('style')
    style.textContent = 'html,body{margin:0;height:100%;overflow:hidden;background:var(--main-background-color,#fff)}body{position:relative}'
    win.document.head.appendChild(style)
    popupWindow = win
    popupHost.value = win.document.getElementById('magic-ai-host')
    win.addEventListener('pagehide', () => {
        popupHost.value = null
        popupWindow = null
    })
}

const onClose = () => {
    visible.value = false
    closePopup()
}

const sendSuggestion = (text) => {
    inputText.value = text
    sendMessage()
}

// 获取请求的 baseURL（从当前页面路径推导）
const getBaseURL = () => {
    const path = window.location.pathname
    // 页面路径如 /magic/web/index.html 或 /magic/web/，取目录部分
    const dir = path.endsWith('/') ? path.slice(0, -1) : path.substring(0, path.lastIndexOf('/'))
    return window.location.origin + dir
}

const scriptIssues = (code) => {
    const issues = []
    if (code.indexOf('"""') >= 0) {
        issues.push('不要使用三引号，SQL 用普通引号字符串')
    }
    if (/&gt;|&lt;|&amp;gt;|&amp;lt;/.test(code)) {
        issues.push('比较符直接写 >=、<=、>、<')
    }
    if (/\b(let|const)\b/.test(code)) {
        issues.push('不要使用 let 或 const，改成 var')
    }
    if (/===|!==/.test(code)) {
        issues.push('不要使用 === 或 !==')
    }
    if (/=>/.test(code)) {
        issues.push('不要使用箭头函数')
    }
    if (/for\s*\(\s*var\s+\w+\s*=/.test(code)) {
        issues.push('不要写 for (var i = 0; ...)，改成 for (item in list)')
    }
    if (/\.map\s*\(/.test(code) || /\.length\b/.test(code)) {
        issues.push('不要使用 .map() 或 .length')
    }
    return issues
}

const sendMessage = async (options) => {
    const rewriting = !!(options && options.rewrite)
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
    streaming.value = false
    scrollToBottom()

    const history = messages.value.slice(0, -1).slice(-10).map(m => ({
        role: m.role,
        content: m.content
    }))

    // 添加空的 assistant 消息占位，用于流式追加
    messages.value.push({ role: 'assistant', content: '' })
    const msgIndex = messages.value.length - 1
    scrollToBottom()

    const token = localStorage.getItem('magic-token') || 'unauthorization'
    const baseURL = getBaseURL()

    try {
        const resp = await fetch(baseURL + '/ai/chat/stream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'magic-token': token
            },
            body: JSON.stringify({
                message: text,
                history,
                currentCode: useCurrentCode.value ? currentCode : '',
                provider: activeProvider.value,
                model: activeModel.value,
                focusTable: focusTable.value || ''
            })
        })

        if (!resp.ok) {
            messages.value[msgIndex].content = '请求失败，状态码: ' + resp.status
            scrollToBottom()
            return
        }

        streaming.value = true
        const reader = resp.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
            const { done, value } = await reader.read()
            if (done) break
            buffer += decoder.decode(value, { stream: true })

            // 按行解析 SSE 数据
            const lines = buffer.split('\n')
            buffer = lines.pop() // 保留不完整的最后一行

            for (const line of lines) {
                const trimmed = line.trim()
                if (!trimmed || trimmed.startsWith(':')) continue
                if (trimmed.startsWith('event:done') || trimmed.startsWith('event: done')) {
                    // 流结束
                    continue
                }
                if (trimmed.startsWith('data:')) {
                    const data = trimmed.substring(5)
                    if (data.trim() === '[DONE]') continue
                    // SSE data 是 JSON 编码的字符串，parse 还原换行等特殊字符
                    try {
                        const text = JSON.parse(data)
                        messages.value[msgIndex].content += text
                    } catch (e) {
                        // 解析失败则直接追加原始文本
                        messages.value[msgIndex].content += data
                    }
                    scrollToBottom()
                }
            }
        }

        // 如果最终内容为空，显示提示
        if (!messages.value[msgIndex].content.trim()) {
            messages.value[msgIndex].content = 'AI返回了空内容，请检查配置。'
        }
    } catch (e) {
        if (!messages.value[msgIndex].content) {
            messages.value[msgIndex].content = '请求失败，请检查AI配置是否正确。'
        } else {
            messages.value[msgIndex].content += '\n\n连接中断'
        }
        scrollToBottom()
    } finally {
        loading.value = false
        streaming.value = false
        const content = messages.value[msgIndex] && messages.value[msgIndex].content
        const code = extractCode(content)
        const issues = code ? scriptIssues(code) : []
        if (rewriting && issues.length) {
            messages.value[msgIndex].content += '\n\n仍需手改：\n- ' + issues.join('\n- ')
        } else if (!rewriting && issues.length) {
            inputText.value = '刚才的脚本不能直接在 magic-script 里运行，请按这些问题重写一份完整脚本，只保留一份代码：\n- ' + issues.join('\n- ')
            await sendMessage({ rewrite: true })
        } else if (code) {
            await runQuery(messages.value[msgIndex], code)
        }
    }
}

const onKeydown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        sendMessage()
    }
}

const clearMessages = () => {
    messages.value = []
    showHistory.value = false
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
    })
}

const querySections = (result) => {
    if (result && Array.isArray(result.sections) && result.sections.length) {
        return result.sections
    }
    return [result]
}

const sectionKind = (section) => {
    const rows = section && Array.isArray(section.rows) ? section.rows : []
    if (!rows.length || Number(section.total) === 0) {
        return 'empty'
    }
    const columns = section && Array.isArray(section.columns) ? section.columns : []
    if (columns.length === 1 && columns[0] === 'value' && rows.length === 1) {
        return 'scalar'
    }
    return 'table'
}

const isReadQuery = (code) => {
    return /\b(select|selectOne|selectValue|selectInt|page|count)\s*\(/i.test(code || '')
        || /\bredis\s*\.\s*(get|hget|hgetall|hkeys|lrange|llen|smembers|scard|zrange|type|ttl|exists|scan|keys)\s*\(/i.test(code || '')
}

const isApiDoc = (content) => {
    return /接口说明|请求路径|返回参数|返回示例/.test(content || '')
}

const downloadBlob = (blob, filename) => {
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
    URL.revokeObjectURL(link.href)
}

const exportDoc = (content) => {
    const title = (String(content || '').match(/^#\s+(.+)$/m) || [, '接口文档'])[1].replace(/[\\/:*?"<>|]/g, '').trim().slice(0, 40) || '接口文档'
    downloadBlob(new Blob([content || ''], { type: 'text/markdown;charset=utf-8' }), title + '.md')
}

const exportQuery = async (message, code) => {
    if (!message || !code || message.exporting || !isReadQuery(code)) {
        return
    }
    message.exporting = true
    message.exportError = ''
    const token = localStorage.getItem('magic-token') || 'unauthorization'
    try {
        const resp = await fetch(getBaseURL() + '/ai/export', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'magic-token': token
            },
            body: JSON.stringify({ script: code })
        })
        if (!resp.ok) {
            message.exportError = await resp.text() || '导出失败'
            return
        }
        downloadBlob(await resp.blob(), 'query.csv')
    } catch (e) {
        message.exportError = '导出失败'
    } finally {
        message.exporting = false
    }
}

const runQuery = async (message, code) => {
    if (!message || !code || message.querying || !isReadQuery(code)) {
        return
    }
    message.querying = true
    message.queryError = ''
    message.queryResult = null
    const token = localStorage.getItem('magic-token') || 'unauthorization'
    try {
        const resp = await fetch(getBaseURL() + '/ai/query', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'magic-token': token
            },
            body: JSON.stringify({ script: code })
        })
        const body = await resp.json()
        if (!body || body.code !== 1) {
            message.queryError = (body && body.message) || '查询失败'
        } else {
            message.queryResult = body.data
        }
    } catch (e) {
        message.queryError = '查询失败'
    } finally {
        message.querying = false
        scrollToBottom()
    }
}

const extractCode = (content) => {
    if (!content) return null
    const match = content.match(/```[a-zA-Z0-9_-]*\s*\n?([\s\S]*?)```/)
    return match ? match[1].trim() : null
}

const insertCode = (code) => {
    props.bus.$emit('ai-insert-code', code)
    notify('代码已插入到编辑器')
}

const replaceCode = (code) => {
    props.modal.confirm('提示', '确定要替换当前编辑器中的全部内容吗？', () => {
        props.bus.$emit('ai-replace-code', code)
        notify('代码已替换')
    })
}

const copyWithTextarea = (code) => {
    const area = document.createElement('textarea')
    area.value = code
    area.setAttribute('readonly', 'readonly')
    area.style.position = 'fixed'
    area.style.top = '0'
    area.style.left = '-9999px'
    document.body.appendChild(area)
    area.focus()
    area.select()
    area.setSelectionRange(0, area.value.length)
    let copied = false
    try {
        copied = document.execCommand('copy')
    } catch (e) {
        copied = false
    }
    document.body.removeChild(area)
    return copied
}

const copyCode = (code) => {
    const done = () => notify('代码已复制到剪贴板')
    const fail = () => props.modal.alert('复制失败，请手动复制')
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(code).then(done).catch(() => copyWithTextarea(code) ? done() : fail())
        return
    }
    copyWithTextarea(code) ? done() : fail()
}

const notify = (text, ok = true) => {
    try {
        if (props.bus && typeof props.bus.status === 'function') {
            props.bus.status(text, ok !== false)
            return
        }
    } catch (e) { /* 状态栏不可用时改用弹窗 */ }
    props.modal.alert(text, '提示')
}

const suggestParams = (code) => {
    const names = []
    const pattern = /#\{([A-Za-z_][\w]*)\}/g
    let found = pattern.exec(code || '')
    while (found) {
        if (names.indexOf(found[1]) < 0) {
            names.push(found[1])
        }
        found = pattern.exec(code || '')
    }
    return names
}

const suggestName = (code) => {
    const table = (code || '').match(/\bfrom\s+([A-Za-z_][\w]*)/i)
    if (table) {
        const raw = table[1]
        return 'query' + raw.charAt(0).toUpperCase() + raw.slice(1)
    }
    return 'newApi'
}

const createApi = (code) => {
    const name = suggestName(code)
    createForm.value = { name, path: '/' + name, method: 'GET', parameters: suggestParams(code).join(','), script: code, saving: false }
}

const submitCreate = async () => {
    const form = createForm.value
    if (!form || form.saving) return
    if (!form.name || !String(form.name).trim() || !form.path || !String(form.path).trim()) {
        props.modal.alert('请填写名称和路径')
        return
    }
    form.saving = true
    const token = localStorage.getItem('magic-token') || 'unauthorization'
    try {
        const resp = await fetch(getBaseURL() + '/ai/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'magic-token': token
            },
            body: JSON.stringify({
                name: form.name,
                path: form.path,
                method: form.method,
                parameters: form.parameters,
                script: form.script
            })
        })
        const body = await resp.json()
        if (body && body.code === 1 && body.data) {
            const saved = body.data
            createForm.value = null
            props.bus.$emit('load-resources')
            props.modal.alert('已新建 ' + saved.groupName + '/' + saved.name + '  ' + saved.method + ' ' + saved.path, '新建接口')
        } else {
            form.saving = false
            props.modal.alert((body && body.message) || '新建接口失败', '新建接口')
        }
    } catch (e) {
        form.saving = false
        props.modal.alert((e && e.message) || '新建接口失败', '新建接口')
    }
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
    width: 420px;
    display: flex;
    flex-direction: column;
    background: var(--main-background-color);
    border-left: 1px solid var(--main-border-color);
    z-index: 100;
    box-shadow: -4px 0 16px rgba(0, 0, 0, 0.08);
    animation: slideInRight 0.2s ease-out;
}
.magic-ai-panel--window {
    top: 0;
    right: 0;
    left: 0;
    width: auto;
    border-left: none;
    box-shadow: none;
    animation: none;
}
.magic-ai-resize {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    cursor: ew-resize;
    z-index: 6;
}
.magic-ai-resize:hover {
    background: rgba(91, 126, 229, 0.45);
}
@keyframes slideInRight {
    from { transform: translateX(20px); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
}

/* Header */
.magic-ai-panel-header {
    height: 44px;
    padding: 0 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--main-border-color);
    background: var(--main-background-color);
    flex-shrink: 0;
}
.magic-ai-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
}
.magic-ai-header-actions {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-shrink: 0;
}
.magic-ai-header-btn {
    border: 1px solid var(--main-border-color);
    background: transparent;
    color: var(--main-color);
    border-radius: 6px;
    padding: 2px 8px;
    font-size: 12px;
    cursor: pointer;
    font-family: inherit;
}
.magic-ai-header-btn:hover {
    background: var(--select-option-hover-background-color);
}
.magic-ai-create-mask {
    position: absolute;
    inset: 44px 0 0 0;
    background: rgba(0, 0, 0, 0.35);
    z-index: 5;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    padding-top: 36px;
}
.magic-ai-create {
    width: 300px;
    background: var(--main-background-color);
    color: var(--main-color);
    border: 1px solid var(--main-border-color);
    border-radius: 8px;
    padding: 14px;
    display: flex;
    flex-direction: column;
    gap: 6px;
}
.magic-ai-create-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 4px;
}
.magic-ai-create label {
    font-size: 12px;
}
.magic-ai-create input,
.magic-ai-create select {
    height: 30px;
    border: 1px solid var(--main-border-color);
    border-radius: 6px;
    background: var(--main-background-color);
    color: var(--main-color);
    padding: 0 8px;
    font-size: 12px;
    font-family: inherit;
}
.magic-ai-create-hint {
    font-size: 11px;
    line-height: 1.5;
    opacity: 0.75;
}
.magic-ai-create-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 6px;
}
.magic-ai-create-save,
.magic-ai-create-cancel {
    border-radius: 6px;
    padding: 4px 12px;
    font-size: 12px;
    cursor: pointer;
    font-family: inherit;
}
.magic-ai-create-save {
    border: none;
    background: #5b7ee5;
    color: #fff;
}
.magic-ai-create-save:disabled {
    opacity: 0.6;
    cursor: default;
}
.magic-ai-create-cancel {
    border: 1px solid var(--main-border-color);
    background: transparent;
    color: var(--main-color);
}
.magic-ai-history {
    max-height: 180px;
    overflow: auto;
    border-bottom: 1px solid var(--main-border-color);
    background: var(--main-background-color);
}
.magic-ai-history-empty {
    padding: 8px 14px;
    color: var(--main-color);
    font-size: 12px;
}
.magic-ai-history-item {
    display: flex;
    align-items: center;
    padding: 2px 8px;
}
.magic-ai-history-item:hover {
    background: var(--select-option-hover-background-color);
}
.magic-ai-history-title,
.magic-ai-history-input {
    flex: 1;
    min-width: 0;
    text-align: left;
    padding: 4px 8px;
    border: none;
    background: transparent;
    color: var(--main-color);
    font-size: 12px;
    font-family: inherit;
}
.magic-ai-history-title {
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.magic-ai-history-input {
    border: 1px solid var(--main-border-color);
    border-radius: 4px;
    background: var(--input-background-color, #fff);
}
.magic-ai-history-menu {
    position: fixed;
    z-index: 300;
    min-width: 96px;
    padding: 4px 0;
    border: 1px solid var(--main-border-color);
    border-radius: 4px;
    background: var(--main-background-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.magic-ai-history-menu button {
    display: block;
    width: 100%;
    padding: 6px 12px;
    border: none;
    background: transparent;
    color: var(--main-color);
    font-size: 12px;
    text-align: left;
    cursor: pointer;
    font-family: inherit;
}
.magic-ai-history-menu button:hover {
    background: var(--select-option-hover-background-color);
}
.magic-ai-history-menu-danger {
    color: #d64545;
}
.magic-ai-header-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);
    color: #fff;
}
.magic-ai-header-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--main-color);
    letter-spacing: 0.3px;
}
.magic-ai-close {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 6px;
    opacity: 0.5;
    transition: all 0.15s;
}
.magic-ai-close:hover {
    opacity: 1;
    background: var(--select-option-hover-background-color);
}
.magic-ai-close :deep(.magic-icon) {
    width: 14px;
    height: 14px;
}

/* Container */
.magic-ai-container {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
}

/* Messages area */
.magic-ai-messages {
    flex: 1;
    overflow-x: hidden;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 0;
}
.magic-ai-messages::-webkit-scrollbar {
    width: 5px;
}
.magic-ai-messages::-webkit-scrollbar-thumb {
    background: var(--main-border-color);
    border-radius: 3px;
}
.magic-ai-messages::-webkit-scrollbar-track {
    background: transparent;
}

/* Welcome */
.magic-ai-welcome {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 20px;
}
.magic-ai-welcome-icon-wrap {
    margin-bottom: 16px;
}
.magic-ai-welcome-title {
    font-size: 17px;
    font-weight: 600;
    color: var(--main-color);
    margin-bottom: 6px;
}
.magic-ai-welcome-desc {
    font-size: 12px;
    color: var(--empty-color);
    margin-bottom: 24px;
    text-align: center;
    line-height: 1.5;
    max-width: 280px;
}
.magic-ai-suggestions {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;
    max-width: 360px;
}
.magic-ai-suggestion {
    padding: 10px 14px;
    border: 1px solid var(--main-border-color);
    border-radius: 8px;
    cursor: pointer;
    font-size: 12px;
    color: var(--main-color);
    transition: all 0.15s ease;
    text-align: left;
    display: flex;
    align-items: center;
    gap: 8px;
    line-height: 1.4;
}
.magic-ai-suggestion-icon {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    opacity: 0.4;
    transition: all 0.15s;
}
.magic-ai-suggestion:hover {
    background: var(--select-option-hover-background-color);
    border-color: #5b7ee5;
    transform: translateX(2px);
}
.magic-ai-suggestion:hover .magic-ai-suggestion-icon {
    opacity: 0.8;
    color: #5b7ee5;
}

/* Messages */
.magic-ai-message {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    max-width: 100%;
    min-width: 0;
    animation: fadeInMsg 0.2s ease-out;
}
@keyframes fadeInMsg {
    from { opacity: 0; transform: translateY(6px); }
    to { opacity: 1; transform: translateY(0); }
}
.magic-ai-message--user {
    flex-direction: row-reverse;
}
.magic-ai-message-avatar {
    width: 30px;
    height: 30px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: bold;
    flex-shrink: 0;
}
.magic-ai-message--user .magic-ai-message-avatar {
    background: #5b7ee5;
    color: #fff;
}
.magic-ai-message--assistant .magic-ai-message-avatar {
    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);
    color: #fff;
}
.magic-ai-message-body {
    max-width: 82%;
    min-width: 0;
}
.magic-ai-message-body--result {
    flex: 1;
    max-width: calc(100% - 36px);
}
.magic-ai-message-content {
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.65;
    color: var(--main-color);
    background: var(--select-option-hover-background-color);
    word-break: break-word;
}
.magic-ai-message--user .magic-ai-message-content {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
    border-radius: 12px 4px 12px 12px;
}
.magic-ai-message--assistant .magic-ai-message-content {
    border-radius: 4px 12px 12px 12px;
}

/* Code action buttons */
.magic-ai-code-actions {
    display: flex;
    gap: 6px;
    margin-top: 8px;
    flex-wrap: wrap;
}
.magic-ai-btn {
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 11px;
    cursor: pointer;
    border: none;
    transition: all 0.15s;
    display: flex;
    align-items: center;
    gap: 4px;
    font-family: inherit;
}
.magic-ai-btn--insert {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
    border: 1px solid var(--main-border-color);
}
.magic-ai-btn--insert:hover,
.magic-ai-btn--create:hover {
    background: #5b7ee5;
    color: #fff;
    border-color: #5b7ee5;
}
.magic-ai-btn--create,
.magic-ai-btn--run {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
    border: 1px solid var(--main-border-color);
}
.magic-ai-btn--run:disabled {
    opacity: 0.5;
    cursor: default;
}
.magic-ai-query-status,
.magic-ai-query-summary {
    margin-top: 8px;
    font-size: 12px;
    color: var(--main-color);
}
.magic-ai-query-section + .magic-ai-query-section {
    margin-top: 10px;
}
.magic-ai-query-result {
    min-width: 0;
    max-width: 100%;
    word-break: normal;
}
.magic-ai-query-table-wrap {
    margin-top: 6px;
    width: 100%;
    max-width: 100%;
    max-height: 280px;
    overflow: auto;
    border: 1px solid var(--main-border-color);
    border-radius: 4px;
}
.magic-ai-query-table-wrap::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}
.magic-ai-query-table-wrap::-webkit-scrollbar-thumb {
    background: var(--main-border-color);
    border-radius: 4px;
}
.magic-ai-query-table-wrap table {
    width: max-content;
    min-width: 100%;
    border-collapse: separate;
    border-spacing: 0;
    font-size: 12px;
}
.magic-ai-query-table-wrap th,
.magic-ai-query-table-wrap td {
    padding: 4px 8px;
    border-bottom: 1px solid var(--main-border-color);
    text-align: left;
    white-space: nowrap;
    word-break: normal;
}
.magic-ai-query-table-wrap th {
    position: sticky;
    top: 0;
    z-index: 1;
    background: var(--main-background-color);
}
.magic-ai-query-table-wrap th:first-child,
.magic-ai-query-table-wrap td:first-child {
    position: sticky;
    left: 0;
    z-index: 1;
    background: var(--main-background-color);
}
.magic-ai-query-table-wrap th:first-child {
    z-index: 2;
}
.magic-ai-btn--replace {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
    border: 1px solid var(--main-border-color);
}
.magic-ai-btn--replace:hover {
    background: #5b7ee5;
    color: #fff;
    border-color: #5b7ee5;
}
.magic-ai-btn--copy {
    background: transparent;
    color: var(--main-color);
    border: 1px solid var(--main-border-color);
}
.magic-ai-btn--copy:hover {
    background: var(--select-option-hover-background-color);
}

/* Loading dots */
.magic-ai-loading {
    display: flex;
    gap: 5px;
    align-items: center;
    padding: 4px 0;
}
.magic-ai-loading span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--main-icon-color, #999);
    animation: aiDot 1.4s infinite ease-in-out;
}
.magic-ai-loading span:nth-child(2) { animation-delay: 0.2s; }
.magic-ai-loading span:nth-child(3) { animation-delay: 0.4s; }
@keyframes aiDot {
    0%, 80%, 100% { transform: scale(0.5); opacity: 0.3; }
    40% { transform: scale(1); opacity: 1; }
}

/* Input area */
.magic-ai-run-error {
    margin: 0 12px 8px;
    padding: 8px 10px;
    border: 1px solid #f0c36d;
    background: #fff8e8;
    color: #6b4e16;
    border-radius: 6px;
    font-size: 12px;
    line-height: 1.5;
}
.magic-ai-run-error-text {
    max-height: 72px;
    overflow: auto;
    word-break: break-all;
}
.magic-ai-run-error-actions {
    display: flex;
    gap: 8px;
    margin-top: 6px;
}
.magic-ai-run-error-actions button {
    border: 1px solid #d7b56a;
    background: #fff;
    color: #6b4e16;
    border-radius: 4px;
    padding: 2px 8px;
    cursor: pointer;
}
.magic-ai-focus {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 0 8px;
    padding: 4px 8px;
    border-radius: 4px;
    background: var(--main-hover-background-color, rgba(91, 126, 229, 0.12));
    color: var(--main-color);
    font-size: 12px;
}
.magic-ai-focus button {
    border: none;
    background: transparent;
    color: inherit;
    cursor: pointer;
}
.magic-ai-input-area {
    border-top: 1px solid var(--main-border-color);
    padding: 10px 14px 12px;
    background: var(--main-background-color);
    flex-shrink: 0;
}

/* Model selector */
.magic-ai-model-selector {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
}
.magic-ai-select-wrap {
    flex: 1;
    position: relative;
    display: flex;
    align-items: center;
}
.magic-ai-select-wrap--model {
    flex: 1.5;
}
.magic-ai-select {
    width: 100%;
    height: 30px;
    padding: 0 22px 0 8px;
    border: 1px solid var(--main-border-color);
    border-radius: 6px;
    background: var(--main-background-color);
    color: var(--main-color);
    font-size: 12px;
    outline: none;
    cursor: pointer;
    appearance: none;
    -webkit-appearance: none;
    transition: border-color 0.15s;
}
.magic-ai-select:focus {
    border-color: #5b7ee5;
}
.magic-ai-select-arrow {
    position: absolute;
    right: 8px;
    pointer-events: none;
    opacity: 0.5;
    color: var(--main-color);
}

/* Toolbar */
.magic-ai-input-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
}
.magic-ai-context-toggle {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    user-select: none;
}
.magic-ai-context-toggle input {
    cursor: pointer;
    accent-color: #5b7ee5;
    width: 14px;
    height: 14px;
}
.magic-ai-toggle-text {
    font-size: 11px;
    color: var(--empty-color);
}
.magic-ai-clear-btn {
    padding: 3px 8px;
    background: transparent;
    border: 1px solid var(--main-border-color);
    border-radius: 5px;
    font-size: 11px;
    cursor: pointer;
    color: var(--empty-color);
    display: flex;
    align-items: center;
    gap: 3px;
    transition: all 0.15s;
    font-family: inherit;
}
.magic-ai-clear-btn:hover:not(:disabled) {
    background: var(--select-option-hover-background-color);
    color: var(--main-color);
    border-color: var(--main-color);
}
.magic-ai-clear-btn:disabled { opacity: 0.3; cursor: default; }

/* Input wrapper */
.magic-ai-input-wrapper {
    display: flex;
    gap: 8px;
    align-items: flex-end;
}
.magic-ai-textarea {
    flex: 1;
    min-height: 56px;
    max-height: 120px;
    padding: 8px 12px;
    border: 1px solid var(--main-border-color);
    border-radius: 10px;
    resize: none;
    font-size: 13px;
    background: var(--main-background-color);
    color: var(--main-color);
    font-family: inherit;
    line-height: 1.5;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
}
.magic-ai-textarea:focus {
    border-color: #5b7ee5;
    box-shadow: 0 0 0 2px rgba(91, 126, 229, 0.12);
}
.magic-ai-textarea:disabled { opacity: 0.5; cursor: not-allowed; }
.magic-ai-textarea::placeholder { color: var(--empty-color); font-size: 12px; }

/* Send button */
.magic-ai-send-btn {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.2s;
    color: #fff;
}
.magic-ai-send-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(91, 126, 229, 0.3);
}
.magic-ai-send-btn:active:not(:disabled) {
    transform: translateY(0);
}
.magic-ai-send-btn:disabled {
    background: var(--main-border-color);
    cursor: not-allowed;
    opacity: 0.5;
}

/* Streaming cursor */
.magic-ai-cursor {
    display: inline;
    animation: blink 1s step-end infinite;
    color: var(--main-color);
    font-weight: bold;
}
@keyframes blink {
    0%, 50% { opacity: 1; }
    51%, 100% { opacity: 0; }
}
</style>

<style>
/* Code block global styles */
.magic-ai-message-content .magic-ai-code-block {
    background: #1a1b26;
    border-radius: 8px;
    margin: 8px 0;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.06);
}
.magic-ai-message-content .magic-ai-code-block pre {
    margin: 0;
    padding: 14px 16px;
    overflow-x: auto;
}
.magic-ai-message-content .magic-ai-code-block pre::-webkit-scrollbar {
    height: 4px;
}
.magic-ai-message-content .magic-ai-code-block pre::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 2px;
}
.magic-ai-message-content .magic-ai-code-block code {
    color: #c0caf5;
    font-family: 'JetBrainsMono', 'Fira Code', Consolas, 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre;
}
.magic-ai-message-content .magic-ai-inline-code {
    background: var(--select-option-hover-background-color);
    padding: 1px 6px;
    border-radius: 4px;
    font-family: 'JetBrainsMono', Consolas, monospace;
    font-size: 12px;
    color: var(--main-color);
}
.magic-ai-message-content h2,
.magic-ai-message-content h3,
.magic-ai-message-content h4 {
    margin: 10px 0 4px;
    font-weight: 600;
    line-height: 1.4;
}
.magic-ai-message-content h2 { font-size: 15px; }
.magic-ai-message-content h3 { font-size: 14px; }
.magic-ai-message-content h4 { font-size: 13px; }
.magic-ai-message-content ul {
    margin: 4px 0;
    padding-left: 18px;
}
.magic-ai-message-content li {
    margin: 3px 0;
    line-height: 1.5;
}
.magic-ai-message-content strong {
    font-weight: 600;
}
</style>
