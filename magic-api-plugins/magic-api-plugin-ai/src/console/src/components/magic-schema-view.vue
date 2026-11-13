<template>
	<div class="magic-schema">
		<div class="magic-schema__head">
			<div class="title">{{ source.name || '数据源' }}</div>
			<div class="meta">
				<span class="tag">{{ kindLabel }}</span>
				<span v-if="source.database">库 {{ source.database }}</span>
				<span v-if="source.host">{{ source.host }}</span>
				<span v-if="source.keyCount !== undefined && source.keyCount !== null">键 {{ source.keyCount }}</span>
			</div>
		</div>
		<p v-if="source.note" class="note">{{ source.note }}</p>
		<p v-if="source.error" class="note warn">{{ source.error }}</p>
		<section v-if="!table">
			<h3>怎么查</h3>
			<pre v-if="source.example">{{ source.example }}</pre>
			<pre v-else-if="source.kind === 'redis'">Redis 没有表。脚本里使用 redis 模块读写键。</pre>
			<p class="hint">支持 MySQL、ClickHouse、Redis、Doris。Doris 使用 MySQL 协议时，会按连接地址识别。</p>
			<p v-if="tables.length" class="hint">左侧展开后点表名，可查看字段。</p>
		</section>
		<section v-else>
			<h3>{{ table.name }} <em v-if="table.comment">{{ table.comment }}</em></h3>
			<pre>{{ queryExample }}</pre>
			<table>
				<thead>
					<tr>
						<th>字段</th>
						<th>类型</th>
						<th>主键</th>
						<th>说明</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="column in table.columns || []" :key="column.name">
						<td>{{ column.name }}</td>
						<td>{{ column.type }}</td>
						<td>{{ column.primary ? '是' : '' }}</td>
						<td>{{ column.comment }}</td>
					</tr>
				</tbody>
			</table>
		</section>
	</div>
</template>
<script setup>
import { computed } from 'vue'

const props = defineProps({
	selectTab: Object
})

const item = computed(() => (props.selectTab && props.selectTab.item) || {})
const source = computed(() => item.value.source || {})
const table = computed(() => item.value.table || null)
const tables = computed(() => source.value.tables || [])
const kindLabel = computed(() => {
	const kind = source.value.kind
	if (kind === 'mysql') return 'MySQL'
	if (kind === 'clickhouse') return 'ClickHouse'
	if (kind === 'redis') return 'Redis'
	if (kind === 'doris') return 'Doris'
	return kind || 'JDBC'
})
const queryExample = computed(() => {
	if (!table.value) return ''
	const script = source.value.script || 'db.select'
	return `return ${script}('select * from ${table.value.name} limit 20');`
})
</script>
<style scoped>
.magic-schema {
	height: 100%;
	overflow: auto;
	padding: 16px 20px 32px;
	box-sizing: border-box;
	background: var(--main-background-color, #fff);
	color: var(--main-color, #1f1f1f);
}
.title {
	font-size: 18px;
	font-weight: 600;
}
.meta {
	display: flex;
	gap: 10px;
	align-items: center;
	margin-top: 8px;
	font-size: 13px;
	opacity: 0.85;
}
.tag {
	padding: 1px 8px;
	border: 1px solid var(--main-border-color, #d9d9d9);
	border-radius: 10px;
}
.note, .hint, h3 {
	margin: 14px 0 8px;
	font-size: 13px;
	font-weight: 500;
}
.note.warn { color: #d46b08; }
h3 em {
	margin-left: 8px;
	font-style: normal;
	font-weight: 400;
	opacity: 0.7;
}
pre {
	margin: 0 0 12px;
	padding: 10px 12px;
	white-space: pre-wrap;
	background: var(--navbar-body-background-color, #f7f8fa);
	border: 1px solid var(--main-border-color, #eee);
	font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
	font-size: 12px;
}
table {
	width: 100%;
	border-collapse: collapse;
	font-size: 13px;
}
th, td {
	border-bottom: 1px solid var(--main-border-color, #eee);
	padding: 6px 8px;
	text-align: left;
	vertical-align: top;
}
th { font-weight: 600; }
</style>
