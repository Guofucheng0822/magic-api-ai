<template>
    <span class="magic-schema-anchor"></span>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
    bus: Object,
    request: Object,
    modal: Object
})

const sources = ref([])
let loading = null

const normalizeKey = (key) => !key || key === 'default' ? 'default' : key

const escapeHtml = (value) => String(value ?? '').replace(/[&<>"]/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;'
}[char]))

const getBaseURL = () => {
    const path = window.location.pathname
    const dir = path.endsWith('/') ? path.slice(0, -1) : path.substring(0, path.lastIndexOf('/'))
    return window.location.origin + dir
}

const loadSources = async () => {
    if (sources.value.length) {
        return sources.value
    }
    if (!loading) {
        const token = localStorage.getItem('magic-token') || 'unauthorization'
        loading = fetch(getBaseURL() + '/ai/schema', {
            headers: { 'magic-token': token }
        }).then(resp => resp.json()).then(body => {
            sources.value = body && body.code === 1 ? (body.data || []) : []
            return sources.value
        }).catch(() => {
            sources.value = []
            return sources.value
        }).finally(() => {
            loading = null
        })
    }
    return loading
}

const findSource = (item) => sources.value.find(source => source.key === normalizeKey(item && item.key))

const renderTree = (source) => {
    if (!source) {
        return '<div class="magic-schema-empty">未配置这个数据源，或没有读到表</div>'
    }
    if (source.error) {
        return `<div class="magic-schema-empty">${escapeHtml(source.error)}</div>`
    }
    if (source.keys && source.keys.length && (!source.tables || source.tables.length === 0)) {
        return source.keys.map(key => `<div class="magic-schema-node magic-schema-leaf"><span class="magic-schema-arrow">•</span><span>${escapeHtml(key)}</span></div>`).join('')
    }
    if (!source.tables || source.tables.length === 0) {
        return '<div class="magic-schema-empty">没有表</div>'
    }
    return source.tables.map(table => {
        const columns = (table.columns || []).map(column =>
            `<div class="magic-schema-col"><span>${escapeHtml(column.name)}</span><em>${escapeHtml(column.type)}</em></div>`
        ).join('') || '<div class="magic-schema-empty">没有字段</div>'
        return `<div class="magic-schema-table">
            <div class="magic-schema-node" data-table>
                <span class="magic-schema-arrow">▶</span><span>${escapeHtml(table.name)}</span>
            </div>
            <div class="magic-schema-cols" style="display:none">${columns}</div>
        </div>`
    }).join('')
}

const bindTree = (box) => {
    box.addEventListener('click', (event) => {
        event.stopPropagation()
        const node = event.target.closest('[data-table]')
        if (!node || !box.contains(node)) {
            return
        }
        const nameNode = node.querySelector('span:last-child')
        const tableName = nameNode ? nameNode.textContent : ''
        if (tableName && props.bus) {
            props.bus.$emit('ai-focus-table', { key: box.dataset.key || 'default', name: tableName })
        }
        const columns = node.nextElementSibling
        if (!columns) {
            return
        }
        const open = columns.style.display === 'none'
        columns.style.display = open ? 'block' : 'none'
        const arrow = node.querySelector('.magic-schema-arrow')
        if (arrow) {
            arrow.textContent = open ? '▼' : '▶'
        }
    })
}

const setCaret = (li, open) => {
    let caret = li.querySelector(':scope > .magic-schema-caret')
    if (!caret) {
        caret = document.createElement('span')
        caret.className = 'magic-schema-caret'
        li.insertBefore(caret, li.firstChild)
    }
    caret.textContent = open ? '▼' : '▶'
}

props.bus.$on('datasource-schema-open', async (payload) => {
    const item = payload && payload.item ? payload.item : payload
    const event = payload && payload.event
    const target = event && (event.currentTarget || event.target)
    const li = target && target.closest ? target.closest('li') : null
    if (!li) {
        return
    }
    const existing = li.querySelector(':scope > .magic-schema-inline')
    if (existing) {
        existing.remove()
        setCaret(li, false)
        return
    }
    const box = document.createElement('div')
    box.className = 'magic-schema-inline'
    box.dataset.key = normalizeKey(item && item.key)
    box.innerHTML = '<div class="magic-schema-empty">正在读取表结构...</div>'
    li.appendChild(box)
    setCaret(li, true)
    bindTree(box)
    await loadSources()
    if (!li.isConnected) {
        return
    }
    box.innerHTML = renderTree(findSource(item))
})
</script>

<style>
.magic-schema-anchor {
    display: none;
}
.magic-data-resource > ul li .magic-schema-caret {
    display: inline-block;
    width: 12px;
    color: var(--resource-span-color);
}
.magic-data-resource > ul li .magic-schema-inline {
    display: block;
    white-space: normal;
    margin: 2px 0 6px 8px;
    padding-left: 8px;
    border-left: 1px solid var(--main-border-color);
}
.magic-data-resource > ul li .magic-schema-node,
.magic-data-resource > ul li .magic-schema-col,
.magic-data-resource > ul li .magic-schema-empty {
    display: flex;
    align-items: center;
    gap: 6px;
    line-height: 20px;
    min-height: 20px;
    padding: 0 4px;
    white-space: nowrap;
}
.magic-data-resource > ul li .magic-schema-node {
    cursor: pointer;
}
.magic-data-resource > ul li .magic-schema-node:hover {
    background: var(--tree-hover-background-color, var(--main-hover-background-color));
}
.magic-data-resource > ul li .magic-schema-arrow {
    width: 12px;
    flex: none;
    color: var(--resource-span-color);
}
.magic-data-resource > ul li .magic-schema-cols {
    margin-left: 14px;
}
.magic-data-resource > ul li .magic-schema-col {
    justify-content: space-between;
    color: var(--resource-span-color);
}
.magic-data-resource > ul li .magic-schema-col em,
.magic-data-resource > ul li .magic-schema-empty {
    font-style: normal;
    opacity: 0.75;
}
</style>
