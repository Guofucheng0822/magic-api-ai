# magic-api-plugin-ai

在 Magic-API 控制台里用对话查询已接入的数据库，并把确认过的查询保存成接口。

完整改动见仓库根目录 [docs/本仓库改动说明.md](../../../docs/本仓库改动说明.md)。

## 引入

```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-plugin-ai</artifactId>
    <version>2.2.2</version>
</dependency>
```

需要同时引入 `magic-api-spring-boot-starter`，并开启 `magic-api.web`。ClickHouse、Redis、Doris、SelectDB 按项目需要另加对应插件。

## 配置

密钥放在环境变量或业务工程配置里，不要写进本仓库。

```yaml
magic-api:
  web: /magic/web
  ai:
    enabled: true
    provider: deepseek
    api-url: https://api.deepseek.com/chat/completions
    api-key: ${MAGIC_API_AI_API_KEY:}
    model: deepseek-chat
```

`provider` 还可以是 `openai`、`claude`、`dashscope`、`zhipu`、`minimax`。属性名是 `enabled`，不是 `enable`。

## 使用

1. 打开 Magic 控制台，用 `Ctrl+Shift+A` 或数据源里的 AI 入口打开对话。
2. 用业务说法描述要查的数据。助手按已加载的库表生成只读脚本并执行。
3. 查询结果可以导出 CSV。回复内容是接口文档时，可以导出 Markdown。
4. 需要留成接口时，在面板里填写名称、路径、方法和参数后保存。保存的是新接口，不覆盖当前打开的接口。

会话历史保存在当前浏览器，换浏览器或清除站点数据后不再保留。
