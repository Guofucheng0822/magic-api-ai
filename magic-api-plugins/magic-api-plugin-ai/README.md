# magic-api-plugin-ai

在 Magic-API 编辑器中通过自然语言对话生成接口，并支持在线测试与一键发布（锁定）。

默认对接 **阿里云百炼 DashScope** 的 **通义千问（Qwen）** OpenAI 兼容接口；也可改配置对接任意 OpenAI 兼容服务。

## 引入依赖

```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-plugin-ai</artifactId>
    <version>2.2.2</version>
</dependency>
```

同时需要已引入 `magic-api-spring-boot-starter`，并开启 `magic-api.web` 控制台。

## 配置（DashScope / 千问）

1. 在 [阿里云百炼控制台](https://bailian.console.aliyun.com/) 创建 API Key。
2. 通过环境变量或配置文件注入（**不要把 Key 写进仓库**）：

```bash
export MAGIC_API_AI_API_KEY=sk-xxxxxxxx
```

```yaml
magic-api:
  web: /magic/web
  ai:
    enable: true
    # DashScope OpenAI 兼容模式（默认）
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: ${MAGIC_API_AI_API_KEY:}
    # 可选：qwen-plus / qwen-turbo / qwen-max 等
    model: qwen-plus
    temperature: 0.2
    timeout: 60000
    default-group-name: AI生成
```

### 切换其他 OpenAI 兼容服务

只需改 `base-url`、`api-key`、`model`，例如：

```yaml
magic-api:
  ai:
    base-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY:}
    model: gpt-4o-mini
```

## 编辑器使用

1. 启动应用后打开 Magic 控制台。
2. 底部工具条出现 **AI 助手**。
3. 用自然语言描述接口，例如：`写一个 GET /hello 返回欢迎语`。
4. 生成成功后会保存到「AI生成」分组，并可 **打开接口 / 在线测试 / 一键发布**。
5. **一键发布** 调用平台已有的资源锁定（`/resource/lock`），发布后接口不可编辑；需要修改时请先解锁。

## HTTP API（插件提供）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `{web}/ai/status` | 查看是否配置了 api-key、当前 model/base-url |
| POST | `{web}/ai/chat` | 对话生成；默认自动保存接口 |
| POST | `{web}/ai/apply` | 将定义写入/更新接口 |
| POST | `{web}/ai/test` | 在线测试（复用 debug/websocket 机制） |
| POST | `{web}/ai/publish` | 一键发布（锁定） |

### `/ai/chat` 请求示例

```json
{
  "message": "创建一个 GET /hello 接口，返回 {message:'hello magic-api'}",
  "history": [],
  "autoSave": true
}
```

## 验证步骤

1. 配置 `MAGIC_API_AI_API_KEY` 并启动应用。
2. 访问控制台，确认插件加载成功（底部出现 AI 助手；或 `GET {web}/ai/status` 中 `apiKeyConfigured=true`）。
3. 发送一句生成请求，确认「AI生成」分组下出现新接口。
4. 点击 **在线测试**，在运行日志中看到执行结果。
5. 点击 **一键发布**，接口变为锁定状态；`GET` 业务路径可正常访问。

## 说明

- LLM 客户端为 OpenAI Chat Completions 兼容协议（`POST {base-url}/chat/completions`）。
- 默认模型与 Endpoint 面向千问 / DashScope；不绑定单一厂商 SDK。
- 本插件为可选模块，不修改 Java 工具链相关父 POM 属性。
