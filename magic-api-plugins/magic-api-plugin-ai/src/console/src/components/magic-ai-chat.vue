<template>
	<div class="magic-ai-chat">
		<div class="magic-ai-chat__status">
			<span :class="['dot', statusState === 'ok' ? 'ok' : (statusState === 'missing' ? 'warn' : '')]"></span>
			<span>{{ statusText }}</span>
			<span v-if="currentApi" class="api-tag">{{ currentApi.method }} {{ currentApi.path }}</span>
			<button type="button" class="link" :disabled="!currentApi || sending" @click="unlockApi(currentApi)">{{ $i('ai.unlock') }}</button>
			<button type="button" class="link" @click="showHistory = !showHistory">{{ $i('ai.history') }}</button>
			<button type="button" class="link" :disabled="sending" @click="newChat">{{ $i('ai.newChat') }}</button>
		</div>
		<div v-if="showHistory" class="magic-ai-chat__history">
			<div v-if="sessions.length === 0" class="magic-ai-chat__empty">{{ $i('ai.historyEmpty') }}</div>
			<div v-for="item in sessions" :key="item.id" :class="['history-item', item.id === sessionId ? 'active' : '']">
				<button type="button" class="history-open" @click="loadSession(item.id)">
					<span class="history-title">{{ item.title }}</span>
					<small>{{ formatTime(item.updatedAt) }}</small>
				</button>
				<button type="button" class="history-delete" @click="removeSession(item.id)">{{ $i('ai.historyDelete') }}</button>
			</div>
		</div>
		<div class="magic-ai-chat__messages" ref="listRef">
			<div v-if="messages.length === 0" class="magic-ai-chat__empty">{{ $i('ai.empty') }}</div>
			<div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
				<div class="bubble">
					<pre>{{ msg.content }}</pre>
					<div v-if="(msg.proposal || (msg.actions && msg.actions.length)) && !msg.accepted && !msg.rejected" class="msg-actions">
						<button type="button" class="primary" :disabled="sending" @click="acceptFix(msg)">
							{{ msg.actions && msg.actions.length ? $i('ai.acceptAction') : $i('ai.acceptFix') }}
						</button>
						<button type="button" :disabled="sending" @click="rejectFix(msg)">
							{{ msg.actions && msg.actions.length ? $i('ai.rejectAction') : $i('ai.rejectFix') }}
						</button>
					</div>
					<div v-else-if="msg.api" class="msg-actions">
						<button type="button" :disabled="sending" @click="openApi(msg.api)">{{ $i('ai.openApi') }}</button>
						<button type="button" :disabled="sending" @click="unlockApi(msg.api)">{{ $i('ai.unlock') }}</button>
						<button type="button" :disabled="sending" @click="runTest(msg.api)">{{ $i('ai.testing') }}</button>
						<button type="button" :disabled="sending" @click="runPublish(msg.api)">{{ $i('ai.publishing') }}</button>
					</div>
				</div>
			</div>
		</div>
		<div class="magic-ai-chat__composer">
			<textarea
				v-model="input"
				:placeholder="$i('ai.placeholder')"
				:disabled="sending"
				@keydown.enter.exact.prevent="send"
			/>
			<div class="actions">
				<button type="button" class="primary" :disabled="sending || !input.trim()" @click="send">
					{{ sending ? $i('ai.sending') : $i('ai.send') }}
				</button>
				<button type="button" :disabled="!currentApi || sending" @click="runTest(currentApi)">{{ $i('ai.testing') }}</button>
				<button type="button" :disabled="!currentApi || sending" @click="runPublish(currentApi)">{{ $i('ai.publishing') }}</button>
				<button type="button" :disabled="!currentApi || sending" @click="unlockApi(currentApi)">{{ $i('ai.unlock') }}</button>
			</div>
		</div>
	</div>
</template>
<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'

const $i = inject('i18n.format')
const info = inject('info', ref({}))
const opened = inject('opened', ref(null))
const bus = inject('bus')
const constants = inject('constants')
const Message = inject('Message')
const request = inject('request')

if (!request || !constants || !bus || !Message) {
	console.warn('[magic-ai] missing editor runtime injects')
}

const STORAGE_KEY = 'magic-ai-chat-history'
const MAX_SESSIONS = 30
const input = ref('')
const sending = ref(false)
const messages = ref([])
const currentApi = ref(null)
const sessionId = ref('')
const sessions = ref([])
const showHistory = ref(false)
let persistReady = false
let pendingOpenId = null
const apiKeyConfigured = ref(false)
const statusState = ref('checking')
const listRef = ref(null)

const statusText = computed(() => {
	if (statusState.value === 'ok') return $i('ai.statusOk')
	if (statusState.value === 'missing') return $i('ai.statusMissingKey')
	if (statusState.value === 'pendingLogin') return $i('ai.statusNeedLogin')
	return $i('ai.statusChecking')
})

const historyPayload = () => messages.value
	.filter(m => m.role === 'user' || m.role === 'assistant')
	.map(m => ({ role: m.role, content: m.content }))

const scrollBottom = async () => {
	await nextTick()
	if (listRef.value) {
		listRef.value.scrollTop = listRef.value.scrollHeight
	}
}

const refreshStatus = () => {
	if (!request) return
	request.sendGet('/ai/status').success(res => {
		const data = res && res.apiKeyConfigured === undefined && res.data ? res.data : res
		const configured = !!(data && data.apiKeyConfigured)
		apiKeyConfigured.value = configured
		statusState.value = configured ? 'ok' : 'missing'
	}).exception((code) => {
		if (code === 401) {
			statusState.value = 'pendingLogin'
		}
	})
}

const newId = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 8)

const readStore = () => {
	try {
		const raw = localStorage.getItem(STORAGE_KEY)
		if (!raw) return { currentId: '', sessions: [] }
		const data = JSON.parse(raw)
		return {
			currentId: data.currentId || '',
			sessions: Array.isArray(data.sessions) ? data.sessions : []
		}
	} catch (e) {
		return { currentId: '', sessions: [] }
	}
}

const writeStore = () => {
	try {
		localStorage.setItem(STORAGE_KEY, JSON.stringify({
			currentId: sessionId.value,
			sessions: sessions.value.slice(0, MAX_SESSIONS)
		}))
	} catch (e) {
		// 浏览器配额不足时保留内存中的当前会话
	}
}

const sessionTitle = (list) => {
	const first = (list || []).find(item => item.role === 'user' && item.content)
	const text = first ? String(first.content).replace(/\s+/g, ' ').trim() : ''
	return text ? text.slice(0, 28) : $i('ai.historyUntitled')
}

const slimMessage = (msg) => ({
	role: msg.role,
	content: msg.content,
	api: msg.api && msg.api.id ? {
		id: msg.api.id,
		method: msg.api.method,
		path: msg.api.path,
		name: msg.api.name
	} : null,
	proposal: msg.proposal || null,
	actions: msg.actions && msg.actions.length ? msg.actions : null,
	accepted: !!msg.accepted,
	rejected: !!msg.rejected
})

const persistSession = () => {
	if (!persistReady || !sessionId.value) return
	const list = messages.value.map(slimMessage)
	const index = sessions.value.findIndex(item => item.id === sessionId.value)
	if (!list.length) {
		if (index >= 0) sessions.value.splice(index, 1)
		writeStore()
		return
	}
	const item = {
		id: sessionId.value,
		title: sessionTitle(list),
		updatedAt: Date.now(),
		messages: list
	}
	if (index >= 0) sessions.value.splice(index, 1, item)
	else sessions.value.unshift(item)
	sessions.value.sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
	if (sessions.value.length > MAX_SESSIONS) sessions.value.length = MAX_SESSIONS
	writeStore()
}

const applySession = (session) => {
	sessionId.value = session.id
	messages.value = (session.messages || []).map(item => ({ ...item }))
	const lastApi = [...messages.value].reverse().find(item => item.api && item.api.id)
	if (lastApi) currentApi.value = lastApi.api
	writeStore()
	scrollBottom()
}

const restoreHistory = () => {
	const store = readStore()
	sessions.value = store.sessions
	const current = store.sessions.find(item => item.id === store.currentId) || store.sessions[0]
	if (current) {
		sessionId.value = current.id
		messages.value = (current.messages || []).map(item => ({ ...item }))
		const lastApi = [...messages.value].reverse().find(item => item.api && item.api.id)
		if (lastApi) currentApi.value = lastApi.api
	} else {
		sessionId.value = newId()
	}
	persistReady = true
}

const newChat = () => {
	if (sending.value) return
	persistSession()
	sessionId.value = newId()
	messages.value = []
	showHistory.value = false
	writeStore()
}

const loadSession = (id) => {
	if (sending.value || id === sessionId.value) {
		showHistory.value = false
		return
	}
	persistSession()
	const found = sessions.value.find(item => item.id === id)
	if (!found) return
	showHistory.value = false
	applySession(found)
}

const removeSession = (id) => {
	sessions.value = sessions.value.filter(item => item.id !== id)
	if (id === sessionId.value) {
		const next = sessions.value[0]
		if (next) applySession(next)
		else {
			sessionId.value = newId()
			messages.value = []
			writeStore()
		}
	} else {
		writeStore()
	}
}

const formatTime = (time) => {
	if (!time) return ''
	const date = new Date(time)
	const pad = (value) => String(value).padStart(2, '0')
	return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const openApi = (api) => {
	if (!api || !api.id || !bus || !Message) return
	currentApi.value = api
	pendingOpenId = api.id
	bus.$emit(Message.OPEN_WITH_ID, api.id)
	if (Message.RELOAD_RESOURCES) {
		bus.$emit(Message.RELOAD_RESOURCES)
	}
}

const unlockApi = (api) => {
	const target = api && api.id ? api : currentApi.value
	if (!target || !target.id) {
		bus.status($i('ai.needApi'))
		return
	}
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/resource/unlock', { id: target.id }, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		const ok = res === true || res === 'true'
		const text = ok ? $i('ai.unlockSuccess') : $i('ai.unlockFailed')
		bus.status(text)
		messages.value.push({ role: 'assistant', content: text, api: target })
		if (ok && Message && Message.RELOAD_RESOURCES) {
			bus.$emit(Message.RELOAD_RESOURCES)
		}
		scrollBottom()
	}).exception((code, message) => {
		messages.value.push({
			role: 'assistant',
			content: message || $i('ai.unlockFailed'),
			api: target
		})
		scrollBottom()
	}).error(err => {
		messages.value.push({
			role: 'assistant',
			content: `${$i('ai.unlockFailed')}: ${(err && (err.message || err.msg)) || err}`,
			api: target
		})
		scrollBottom()
	})
}

const editingCurrent = (text) => {
	const value = (text || '').trim()
	if (/当前接口|这个接口|改这个|修改当前|把当前|沿用当前/.test(value)) return true
	return /^(保存|确认|是的|同意|好的|可以)[。.!！\s]*$/.test(value)
}

const send = () => {
	const text = (input.value || '').trim()
	if (!text || sending.value) return
	sending.value = true
	messages.value.push({ role: 'user', content: text })
	input.value = ''
	scrollBottom()

	const body = {
		message: text,
		history: historyPayload().slice(0, -1),
		autoSave: true
	}
	if (editingCurrent(text)) {
		const openedInfo = (opened && opened.value && opened.value.item) || (info && info.value)
		if (openedInfo && openedInfo.id && (openedInfo.method || openedInfo.script)) {
			body.apiId = openedInfo.id
		} else if (currentApi.value && currentApi.value.id) {
			body.apiId = currentApi.value.id
		}
	}

	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/ai/chat', body, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		const reply = (res && res.reply) || ''
		const definition = res && res.definition
		const actions = res && res.actions
		if (actions && actions.length) {
			messages.value.push({
				role: 'assistant',
				content: reply || $i('ai.actionReady'),
				actions,
				proposal: definition && definition.script ? definition : null
			})
			bus.status($i('ai.actionReady'))
		} else if (res && res.locked && definition && definition.script) {
			messages.value.push({
				role: 'assistant',
				content: reply || $i('ai.lockedHint'),
				proposal: definition,
				api: {
					id: body.apiId,
					method: definition.method,
					path: definition.path,
					name: definition.name
				}
			})
			bus.status($i('ai.lockedHint'))
		} else {
			const api = res && res.api
			messages.value.push({
				role: 'assistant',
				content: reply || (api ? $i('ai.generated') : '(empty)'),
				api: api || null
			})
			if (api) {
				currentApi.value = api
				bus.status($i('ai.generated') + `: ${api.method} ${api.path}`)
				bus.$emit(Message.RELOAD_RESOURCES)
				bus.$emit(Message.OPEN_WITH_ID, api.id)
			}
		}
	}).exception((code, message) => {
		messages.value.push({
			role: 'assistant',
			content: message || $i('ai.chatFailed')
		})
	}).error(err => {
		messages.value.push({
			role: 'assistant',
			content: (err && (err.message || err.msg)) || String(err)
		})
	}).end(() => {
		sending.value = false
		scrollBottom()
	})
}

const runTest = (api) => {
	if (!api || !api.id) {
		bus.status($i('ai.needApi'))
		return
	}
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	headers[constants.HEADER_REQUEST_CLIENT_ID] = constants.CLIENT_ID
	headers[constants.HEADER_REQUEST_SCRIPT_ID] = api.id
	bus.$emit(Message.SWITCH_TOOLBAR, 'log')
	bus.status($i('ai.testing') + ` ${api.method || ''} ${api.path || ''}`)
	request.sendPost('/ai/test', { apiId: api.id, context: {} }, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		messages.value.push({
			role: 'assistant',
			content: `${$i('ai.testSuccess')}\n` + JSON.stringify(res, null, 2),
			api
		})
		scrollBottom()
	}).exception((code, message) => {
		const errorText = message || $i('ai.testFailed')
		bus.status($i('ai.testFailed'))
		messages.value.push({
			role: 'assistant',
			content: errorText,
			api
		})
		scrollBottom()
		proposeRepair(api, errorText)
	}).error(err => {
		const errorText = `${$i('ai.testFailed')}: ${(err && (err.message || err.msg)) || err}`
		bus.status($i('ai.testFailed'))
		messages.value.push({
			role: 'assistant',
			content: errorText,
			api
		})
		scrollBottom()
		proposeRepair(api, errorText)
	})
}

const postChat = (body, onSuccess) => {
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/ai/chat', body, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(onSuccess).exception((code, message) => {
		messages.value.push({
			role: 'assistant',
			content: message || $i('ai.fixMissing')
		})
	}).error(err => {
		messages.value.push({
			role: 'assistant',
			content: (err && (err.message || err.msg)) || String(err)
		})
	}).end(() => {
		sending.value = false
		scrollBottom()
	})
}

const proposeRepair = (api, errorText) => {
	if (!api || !api.id || sending.value) return
	const text = '在线测试失败，请根据报错修改当前接口脚本。先说明原因并给出完整 JSON，先不要保存。\n' + (errorText || '')
	sending.value = true
	messages.value.push({ role: 'user', content: text })
	bus.status($i('ai.repairing'))
	scrollBottom()
	postChat({
		message: text,
		history: historyPayload().slice(0, -1),
		autoSave: false,
		apiId: api.id
	}, res => {
		const definition = res && (res.definition || (res.data && res.data.definition))
		const reply = (res && (res.reply || (res.data && res.data.reply))) || ''
		if (definition && definition.script) {
			messages.value.push({
				role: 'assistant',
				content: (reply ? reply + '\n\n' : '') + $i('ai.fixReady'),
				proposal: definition,
				api
			})
		} else {
			messages.value.push({
				role: 'assistant',
				content: reply || $i('ai.fixMissing'),
				api
			})
		}
	})
}

const acceptFix = (msg) => {
	if (!msg || sending.value) return
	if (msg.actions && msg.actions.length) {
		acceptActions(msg)
		return
	}
	if (!msg.proposal) return
	const apiId = (msg.api && msg.api.id) || (currentApi.value && currentApi.value.id)
	if (!apiId) return
	sending.value = true
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/ai/apply', {
		apiId,
		definition: JSON.stringify(msg.proposal)
	}, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(api => {
		msg.accepted = true
		const saved = api && api.id ? api : msg.api
		currentApi.value = saved
		messages.value.push({
			role: 'assistant',
			content: $i('ai.fixApplied'),
			api: saved
		})
		bus.status($i('ai.fixApplied'))
		if (saved && saved.id) {
			openApi(saved)
		}
	}).exception((code, message) => {
		messages.value.push({
			role: 'assistant',
			content: message || $i('ai.fixMissing'),
			api: msg.api
		})
	}).error(err => {
		messages.value.push({
			role: 'assistant',
			content: (err && (err.message || err.msg)) || String(err),
			api: msg.api
		})
	}).end(() => {
		sending.value = false
		scrollBottom()
	})
}

const acceptActions = (msg) => {
	sending.value = true
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	const payload = { actions: JSON.stringify(msg.actions) }
	if (msg.proposal && msg.proposal.script) {
		payload.definition = JSON.stringify(msg.proposal)
	}
	request.sendPost('/ai/action', payload, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		msg.accepted = true
		const api = res && res.api
		const lines = ((res && res.results) || []).map(item => item && item.message).filter(Boolean)
		if (api && api.id) {
			currentApi.value = api
			openApi(api)
		} else if (Message && Message.RELOAD_RESOURCES) {
			bus.$emit(Message.RELOAD_RESOURCES)
		}
		messages.value.push({
			role: 'assistant',
			content: lines.length ? lines.join('\n') : $i('ai.actionApplied'),
			api: api && api.id ? api : null
		})
		bus.status($i('ai.actionApplied'))
	}).exception((code, message) => {
		messages.value.push({
			role: 'assistant',
			content: message || $i('ai.actionFailed')
		})
	}).error(err => {
		messages.value.push({
			role: 'assistant',
			content: (err && (err.message || err.msg)) || String(err)
		})
	}).end(() => {
		sending.value = false
		scrollBottom()
	})
}

const rejectFix = (msg) => {
	if (!msg || msg.accepted || msg.rejected) return
	msg.rejected = true
	messages.value.push({
		role: 'assistant',
		content: msg.actions && msg.actions.length ? $i('ai.actionRejected') : $i('ai.fixRejected'),
		api: msg.api
	})
	scrollBottom()
}

const runPublish = (api) => {
	if (!api || !api.id) {
		bus.status($i('ai.needApi'))
		return
	}
	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/ai/publish', { apiId: api.id }, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		const ok = res && (res.success || res.locked)
		bus.status(ok ? $i('ai.publishSuccess') : $i('ai.publishFailed'))
		messages.value.push({
			role: 'assistant',
			content: ok ? $i('ai.publishSuccess') : $i('ai.publishFailed'),
			api
		})
		if (ok) {
			bus.$emit(Message.RELOAD_RESOURCES)
		}
		scrollBottom()
	}).error(err => {
		bus.status($i('ai.publishFailed'))
		messages.value.push({
			role: 'assistant',
			content: `${$i('ai.publishFailed')}: ${(err && (err.message || err.msg)) || err}`,
			api
		})
		scrollBottom()
	})
}

watch(messages, () => persistSession(), { deep: true })

onMounted(() => {
	restoreHistory()
	refreshStatus()
	if (bus && Message && typeof bus.$on === 'function') {
		if (Message.LOGINED) bus.$on(Message.LOGINED, refreshStatus)
		if (Message.RELOAD_RESOURCES_FINISH) {
			bus.$on(Message.RELOAD_RESOURCES_FINISH, () => {
				if (!pendingOpenId) return
				const id = pendingOpenId
				pendingOpenId = null
				setTimeout(() => bus.$emit(Message.OPEN_WITH_ID, id), 60)
			})
		}
	}
	const openedInfo = (opened && opened.value && opened.value.item) || (info && info.value)
	if (openedInfo && openedInfo.id && openedInfo.method) {
		currentApi.value = openedInfo
	}
})
</script>
<style scoped>
.magic-ai-chat {
	display: flex;
	flex-direction: column;
	flex: 1;
	min-height: 220px;
	height: 100%;
	padding: 6px 8px;
	box-sizing: border-box;
	background: linear-gradient(180deg, rgba(16, 42, 66, 0.04), transparent 48%);
}
.magic-ai-chat__status {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 12px;
	opacity: 0.85;
	margin-bottom: 6px;
}
.magic-ai-chat__status .dot {
	width: 8px;
	height: 8px;
	border-radius: 50%;
	background: #c0c4cc;
}
.magic-ai-chat__status .dot.ok { background: #2ba471; }
.magic-ai-chat__status .dot.warn { background: #e37318; }
.api-tag {
	margin-left: auto;
	padding: 1px 8px;
	border: 1px solid var(--main-border-color, #ddd);
	font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
	font-size: 11px;
}
.magic-ai-chat__status .link {
	border: 0;
	background: transparent;
	padding: 0 4px;
	cursor: pointer;
	color: #1677ff;
	font-size: 12px;
}
.magic-ai-chat__history {
	max-height: 168px;
	overflow: auto;
	margin-bottom: 6px;
	border: 1px solid var(--main-border-color, #e5e5e5);
	background: var(--main-background-color, #fff);
}
.history-item {
	display: flex;
	align-items: center;
	border-bottom: 1px solid var(--main-border-color, #eee);
}
.history-item.active {
	background: #e8f3ff;
}
.history-open {
	flex: 1;
	display: flex;
	justify-content: space-between;
	gap: 8px;
	border: 0;
	background: transparent;
	text-align: left;
	padding: 6px 8px;
	cursor: pointer;
	font-size: 12px;
}
.history-title {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.history-delete {
	border: 0;
	background: transparent;
	color: #c45656;
	cursor: pointer;
	font-size: 12px;
	padding-right: 8px;
}
.magic-ai-chat__messages {
	flex: 1;
	overflow: auto;
	padding: 4px;
	border: 1px solid var(--main-border-color, #e5e5e5);
	background: var(--main-background-color, #fff);
}
.magic-ai-chat__empty {
	opacity: 0.65;
	padding: 16px 8px;
	font-size: 13px;
	line-height: 1.5;
}
.msg {
	display: flex;
	margin: 8px 0;
}
.msg.user { justify-content: flex-end; }
.msg.assistant { justify-content: flex-start; }
.bubble {
	max-width: 86%;
	padding: 8px 10px;
	border-radius: 8px;
	background: #f3f6f9;
}
.msg.user .bubble {
	background: #e8f3ff;
}
.bubble pre {
	margin: 0;
	white-space: pre-wrap;
	word-break: break-word;
	font-family: inherit;
	font-size: 13px;
	line-height: 1.45;
}
.msg-actions {
	display: flex;
	gap: 6px;
	margin-top: 8px;
}
.msg-actions button,
.actions button {
	cursor: pointer;
	border: 1px solid var(--main-border-color, #d0d5dd);
	background: #fff;
	padding: 4px 10px;
	font-size: 12px;
}
.msg-actions button.primary,
.actions button.primary {
	background: #1677ff;
	border-color: #1677ff;
	color: #fff;
}
.actions button:disabled,
.msg-actions button:disabled {
	opacity: 0.5;
	cursor: not-allowed;
}
.magic-ai-chat__composer {
	margin-top: 8px;
	display: flex;
	flex-direction: column;
	gap: 6px;
}
.magic-ai-chat__composer textarea {
	width: 100%;
	min-height: 64px;
	resize: vertical;
	box-sizing: border-box;
	padding: 8px;
	border: 1px solid var(--main-border-color, #d0d5dd);
	font-family: inherit;
	font-size: 13px;
}
.actions {
	display: flex;
	gap: 8px;
	justify-content: flex-end;
}
</style>
