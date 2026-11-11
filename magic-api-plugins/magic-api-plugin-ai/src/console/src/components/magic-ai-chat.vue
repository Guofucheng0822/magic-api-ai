<template>
	<div class="magic-ai-chat">
		<div class="magic-ai-chat__status">
			<span :class="['dot', apiKeyConfigured ? 'ok' : 'warn']"></span>
			<span>{{ statusText }}</span>
			<span v-if="currentApi" class="api-tag">{{ currentApi.method }} {{ currentApi.path }}</span>
		</div>
		<div class="magic-ai-chat__messages" ref="listRef">
			<div v-if="messages.length === 0" class="magic-ai-chat__empty">{{ $i('ai.empty') }}</div>
			<div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
				<div class="bubble">
					<pre>{{ msg.content }}</pre>
					<div v-if="msg.api" class="msg-actions">
						<button type="button" @click="openApi(msg.api)">{{ $i('ai.openApi') }}</button>
						<button type="button" @click="runTest(msg.api)">{{ $i('ai.testing') }}</button>
						<button type="button" @click="runPublish(msg.api)">{{ $i('ai.publishing') }}</button>
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
			</div>
		</div>
	</div>
</template>
<script setup>
import { computed, inject, nextTick, onMounted, ref } from 'vue'

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

const input = ref('')
const sending = ref(false)
const messages = ref([])
const currentApi = ref(null)
const apiKeyConfigured = ref(false)
const listRef = ref(null)

const statusText = computed(() => apiKeyConfigured.value ? $i('ai.statusOk') : $i('ai.statusMissingKey'))

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
	request.sendGet('/ai/status').success(res => {
		apiKeyConfigured.value = !!(res && res.apiKeyConfigured)
	})
}

const openApi = (api) => {
	if (!api || !api.id) return
	currentApi.value = api
	bus.$emit(Message.OPEN_WITH_ID, api.id)
	bus.$emit(Message.RELOAD_RESOURCES)
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
	const openedInfo = (opened && opened.value && opened.value.item) || (info && info.value)
	if (openedInfo && openedInfo.id && (openedInfo.method || openedInfo.script)) {
		body.apiId = openedInfo.id
	} else if (currentApi.value && currentApi.value.id) {
		body.apiId = currentApi.value.id
	}

	const headers = {}
	headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
	request.sendPost('/ai/chat', body, {
		baseURL: constants.SERVER_URL,
		headers,
		withCredentials: true
	}).success(res => {
		const reply = (res && res.reply) || ''
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
	}).error(err => {
		bus.status($i('ai.testFailed'))
		messages.value.push({
			role: 'assistant',
			content: `${$i('ai.testFailed')}: ${(err && (err.message || err.msg)) || err}`,
			api
		})
		scrollBottom()
	})
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

onMounted(() => {
	refreshStatus()
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
