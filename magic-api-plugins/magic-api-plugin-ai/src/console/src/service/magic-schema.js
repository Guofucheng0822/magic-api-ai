export default function (bus, constants, $i, Message, request) {
    return {
        getIcon: item => {
            const kind = item && item.source && item.source.kind
            if (item && item.table) {
                return ['表', '#0cbb52']
            }
            if (kind === 'clickhouse') return ['CH', '#d48806']
            if (kind === 'redis') return ['RD', '#cf1322']
            if (kind === 'doris') return ['Do', '#0958d9']
            if (kind === 'mysql') return ['My', '#1677ff']
            return ['源', '#595959']
        },
        name: $i('schema.name'),
        language: 'magicscript',
        runnable: false,
        requirePath: false,
        merge: item => item,
        // 编辑器看到 script 才会跳过 /resource/file/{id}。这些节点不是接口文件，空响应会触发 Object.keys(null)。
        processSave: item => item,
        injectResources(getList) {
            const fill = () => {
                request.sendGet('/ai/schema').success(list => {
                    const bucket = getList('schema')
                    if (!Array.isArray(bucket)) {
                        return
                    }
                    const nodes = (list || []).map(source => ({
                        id: source.id,
                        name: source.name,
                        folder: true,
                        opened: true,
                        type: 'schema',
                        parentId: 'schema:0',
                        source,
                        script: describeSource(source),
                        children: sourceChildren(source)
                    }))
                    bucket.splice(0, bucket.length, ...nodes)
                }).exception(() => {})
            }
            if (bus && Message && Message.LOAD_RESOURCES_FINISH && typeof bus.$on === 'function') {
                bus.$on(Message.LOAD_RESOURCES_FINISH, fill)
            }
            if (constants && constants.LOGINED) {
                fill()
            }
        }
    }
}

function sourceChildren(source) {
    const children = [{
        id: source.id + ':info',
        name: '连接信息',
        folder: false,
        type: 'schema',
        groupId: source.id,
        source,
        info: true,
        script: describeSource(source)
    }]
    ;(source.tables || []).forEach(table => {
        const comment = table.comment ? '  ' + table.comment : ''
        children.push({
            id: source.id + ':' + table.name,
            name: table.name + comment,
            folder: false,
            type: 'schema',
            groupId: source.id,
            source,
            table,
            script: describeTable(source, table)
        })
    })
    return children
}

function describeSource(source) {
    const lines = ['/*', '数据源: ' + (source.name || '')]
    if (source.kind) lines.push('类型: ' + source.kind)
    if (source.database) lines.push('库: ' + source.database)
    if (source.host) lines.push('地址: ' + source.host)
    if (source.script) lines.push('脚本入口: ' + source.script)
    if (source.note) lines.push(source.note)
    if (source.keyCount !== undefined && source.keyCount !== null) lines.push('键数量: ' + source.keyCount)
    lines.push('*/')
    if (source.example) {
        lines.push('')
        lines.push(source.example)
    }
    return lines.join('\n')
}

function describeTable(source, table) {
    const entry = source.script || 'db.select'
    const lines = [
        '/*',
        '数据源: ' + (source.name || ''),
        '表: ' + table.name + (table.comment ? '  ' + table.comment : ''),
        '*/',
        '',
        `return ${entry}('select * from ${table.name} limit 20');`,
        '',
        '/*',
        '字段:'
    ]
    ;(table.columns || []).forEach(column => {
        lines.push([column.name, column.type, column.primary ? 'PK' : '', column.comment].filter(Boolean).join('  '))
    })
    lines.push('*/')
    return lines.join('\n')
}
