var __vite_style__ = document.createElement("style");
__vite_style__.innerHTML = "\n.magic-ai-panel[data-v-483a8d2a] {\r\n    position: absolute;\r\n    top: var(--magic-header-height);\r\n    right: 0;\r\n    bottom: 0;\r\n    width: 380px;\r\n    display: flex;\r\n    flex-direction: column;\r\n    background: var(--main-background-color);\r\n    border-left: 1px solid var(--main-border-color);\r\n    z-index: 100;\n}\n.magic-ai-panel-header[data-v-483a8d2a] {\r\n    height: var(--magic-header-height);\r\n    line-height: var(--magic-header-height);\r\n    padding: 0 10px;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: space-between;\r\n    border-bottom: 1px solid var(--main-border-color);\r\n    font-size: 13px;\r\n    font-weight: bold;\r\n    color: var(--main-color);\r\n    flex-shrink: 0;\n}\n.magic-ai-close[data-v-483a8d2a] {\r\n    cursor: pointer;\r\n    display: flex;\r\n    align-items: center;\r\n    opacity: 0.6;\n}\n.magic-ai-close[data-v-483a8d2a]:hover { opacity: 1;\n}\n.magic-ai-close[data-v-483a8d2a] .magic-icon {\r\n    width: 14px;\r\n    height: 14px;\n}\n.magic-ai-container[data-v-483a8d2a] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    flex: 1;\r\n    min-height: 0;\r\n    background: var(--main-background-color);\n}\n.magic-ai-messages[data-v-483a8d2a] {\r\n    flex: 1;\r\n    overflow-y: auto;\r\n    padding: 12px;\r\n    display: flex;\r\n    flex-direction: column;\r\n    gap: 12px;\r\n    min-height: 0;\n}\n.magic-ai-welcome[data-v-483a8d2a] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    align-items: center;\r\n    justify-content: center;\r\n    height: 100%;\r\n    color: var(--main-color);\r\n    opacity: 0.8;\n}\n.magic-ai-welcome-icon[data-v-483a8d2a] { font-size: 48px; margin-bottom: 10px;\n}\n.magic-ai-welcome-title[data-v-483a8d2a] { font-size: 18px; font-weight: bold; margin-bottom: 8px;\n}\n.magic-ai-welcome-desc[data-v-483a8d2a] {\r\n    font-size: 13px;\r\n    color: var(--empty-color);\r\n    margin-bottom: 20px;\r\n    text-align: center;\n}\n.magic-ai-suggestions[data-v-483a8d2a] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    gap: 8px;\r\n    width: 100%;\r\n    max-width: 480px;\n}\n.magic-ai-suggestion[data-v-483a8d2a] {\r\n    padding: 8px 14px;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 6px;\r\n    cursor: pointer;\r\n    font-size: 12px;\r\n    color: var(--main-color);\r\n    transition: background 0.15s;\r\n    text-align: left;\n}\n.magic-ai-suggestion[data-v-483a8d2a]:hover {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--select-option-hover-color);\n}\n.magic-ai-message[data-v-483a8d2a] {\r\n    display: flex;\r\n    gap: 10px;\r\n    align-items: flex-start;\n}\n.magic-ai-message--user[data-v-483a8d2a] { flex-direction: row-reverse;\n}\n.magic-ai-message-avatar[data-v-483a8d2a] {\r\n    width: 30px;\r\n    height: 30px;\r\n    border-radius: 50%;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    font-size: 11px;\r\n    font-weight: bold;\r\n    flex-shrink: 0;\n}\n.magic-ai-message--user .magic-ai-message-avatar[data-v-483a8d2a] { background: #1976d2; color: #fff;\n}\n.magic-ai-message--assistant .magic-ai-message-avatar[data-v-483a8d2a] { background: #43a047; color: #fff;\n}\n.magic-ai-message-content[data-v-483a8d2a] {\r\n    max-width: 85%;\r\n    padding: 8px 12px;\r\n    border-radius: 8px;\r\n    font-size: 13px;\r\n    line-height: 1.6;\r\n    color: var(--main-color);\r\n    background: var(--select-option-hover-background-color);\r\n    word-break: break-word;\n}\n.magic-ai-message--user .magic-ai-message-content[data-v-483a8d2a] {\r\n    background: #1565c0;\r\n    color: #fff;\r\n    border-radius: 8px 2px 8px 8px;\n}\n.magic-ai-message--assistant .magic-ai-message-content[data-v-483a8d2a] { border-radius: 2px 8px 8px 8px;\n}\n.magic-ai-code-actions[data-v-483a8d2a] {\r\n    display: flex;\r\n    gap: 6px;\r\n    margin-top: 8px;\r\n    flex-wrap: wrap;\n}\n.magic-ai-btn[data-v-483a8d2a] {\r\n    padding: 3px 10px;\r\n    border-radius: 4px;\r\n    font-size: 11px;\r\n    cursor: pointer;\r\n    border: 1px solid;\r\n    transition: all 0.15s;\n}\n.magic-ai-btn--insert[data-v-483a8d2a] { background: #43a047; color: #fff; border-color: #43a047;\n}\n.magic-ai-btn--insert[data-v-483a8d2a]:hover { background: #388e3c;\n}\n.magic-ai-btn--replace[data-v-483a8d2a] { background: #1976d2; color: #fff; border-color: #1976d2;\n}\n.magic-ai-btn--replace[data-v-483a8d2a]:hover { background: #1565c0;\n}\n.magic-ai-btn--copy[data-v-483a8d2a] { background: transparent; color: var(--main-color); border-color: var(--main-border-color);\n}\n.magic-ai-btn--copy[data-v-483a8d2a]:hover { background: var(--select-option-hover-background-color);\n}\n.magic-ai-loading[data-v-483a8d2a] {\r\n    display: flex;\r\n    gap: 4px;\r\n    align-items: center;\r\n    padding: 4px 0;\n}\n.magic-ai-loading span[data-v-483a8d2a] {\r\n    width: 7px;\r\n    height: 7px;\r\n    border-radius: 50%;\r\n    background: var(--main-icon-color);\r\n    animation: aiDot-483a8d2a 1.2s infinite;\n}\n.magic-ai-loading span[data-v-483a8d2a]:nth-child(2) { animation-delay: 0.2s;\n}\n.magic-ai-loading span[data-v-483a8d2a]:nth-child(3) { animation-delay: 0.4s;\n}\n@keyframes aiDot-483a8d2a {\n0%, 60%, 100% { transform: scale(0.6); opacity: 0.4;\n}\n30% { transform: scale(1); opacity: 1;\n}\n}\n.magic-ai-input-area[data-v-483a8d2a] {\r\n    border-top: 1px solid var(--main-border-color);\r\n    padding: 8px 12px 10px;\r\n    background: var(--main-background-color);\r\n    flex-shrink: 0;\n}\n.magic-ai-input-toolbar[data-v-483a8d2a] {\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: space-between;\r\n    margin-bottom: 6px;\r\n    font-size: 12px;\r\n    color: var(--empty-color);\n}\n.magic-ai-context-toggle[data-v-483a8d2a] {\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 4px;\r\n    cursor: pointer;\r\n    user-select: none;\n}\n.magic-ai-context-toggle input[data-v-483a8d2a] { cursor: pointer; accent-color: #1976d2;\n}\n.magic-ai-clear-btn[data-v-483a8d2a] {\r\n    padding: 2px 8px;\r\n    background: transparent;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 3px;\r\n    font-size: 11px;\r\n    cursor: pointer;\r\n    color: var(--empty-color);\n}\n.magic-ai-clear-btn[data-v-483a8d2a]:hover:not(:disabled) {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--main-color);\n}\n.magic-ai-clear-btn[data-v-483a8d2a]:disabled { opacity: 0.4; cursor: default;\n}\n.magic-ai-input-wrapper[data-v-483a8d2a] {\r\n    display: flex;\r\n    gap: 8px;\r\n    align-items: flex-end;\n}\n.magic-ai-textarea[data-v-483a8d2a] {\r\n    flex: 1;\r\n    min-height: 60px;\r\n    max-height: 120px;\r\n    padding: 8px 10px;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 6px;\r\n    resize: none;\r\n    font-size: 13px;\r\n    background: var(--main-background-color);\r\n    color: var(--main-color);\r\n    font-family: inherit;\r\n    line-height: 1.5;\r\n    outline: none;\r\n    transition: border-color 0.15s;\n}\n.magic-ai-textarea[data-v-483a8d2a]:focus { border-color: #1976d2;\n}\n.magic-ai-textarea[data-v-483a8d2a]:disabled { opacity: 0.6; cursor: not-allowed;\n}\n.magic-ai-send-btn[data-v-483a8d2a] {\r\n    width: 38px;\r\n    height: 38px;\r\n    border-radius: 6px;\r\n    background: #43a047;\r\n    border: none;\r\n    cursor: pointer;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    flex-shrink: 0;\r\n    transition: background 0.15s;\n}\n.magic-ai-send-btn[data-v-483a8d2a]:hover:not(:disabled) { background: #388e3c;\n}\n.magic-ai-send-btn[data-v-483a8d2a]:disabled {\r\n    background: var(--main-border-color);\r\n    cursor: not-allowed;\r\n    opacity: 0.6;\n}\n.magic-ai-send-btn[data-v-483a8d2a] .magic-icon { fill: #fff;\n}\r\n\n.magic-ai-message-content .magic-ai-code-block {\r\n    background: #1e1e1e;\r\n    border-radius: 6px;\r\n    margin: 8px 0;\r\n    overflow: hidden;\n}\n.magic-ai-message-content .magic-ai-code-block pre {\r\n    margin: 0;\r\n    padding: 12px;\r\n    overflow-x: auto;\n}\n.magic-ai-message-content .magic-ai-code-block code {\r\n    color: #d4d4d4;\r\n    font-family: 'JetBrainsMono', Consolas, 'Courier New', monospace;\r\n    font-size: 12px;\r\n    line-height: 1.6;\r\n    white-space: pre;\n}\n.magic-ai-message-content .magic-ai-inline-code {\r\n    background: rgba(0, 0, 0, 0.15);\r\n    padding: 1px 5px;\r\n    border-radius: 3px;\r\n    font-family: monospace;\r\n    font-size: 12px;\n}\n.magic-ai-message--user .magic-ai-message-content .magic-ai-inline-code {\r\n    background: rgba(255, 255, 255, 0.2);\n}\n.magic-ai-message-content h2,\r\n.magic-ai-message-content h3,\r\n.magic-ai-message-content h4 {\r\n    margin: 8px 0 4px;\r\n    font-weight: bold;\n}\n.magic-ai-message-content ul {\r\n    margin: 4px 0;\r\n    padding-left: 20px;\n}\n.magic-ai-message-content li { margin: 2px 0;\n}\r\n";
document.head.appendChild(__vite_style__);
var MagicAI = function(vue) {
  "use strict";
  var localZhCN = {
    name: "\u7B80\u4F53\u4E2D\u6587",
    ai: {
      name: "AI Coding",
      title: "AI \u52A9\u624B",
      open: "\u6253\u5F00 AI \u5BF9\u8BDD",
      welcome: "AI Coding \u52A9\u624B",
      welcomeDesc: "\u6211\u53EF\u4EE5\u5E2E\u4F60\u6309\u7167 magic-api \u811A\u672C\u8BED\u6CD5\u751F\u6210\u4EE3\u7801\uFF0C\u8BF7\u63CF\u8FF0\u4F60\u7684\u9700\u6C42\u3002",
      placeholder: "\u63CF\u8FF0\u4F60\u9700\u8981\u5B9E\u73B0\u7684\u529F\u80FD\uFF0C\u6309 Enter \u53D1\u9001\uFF0CShift+Enter \u6362\u884C...",
      send: "\u53D1\u9001",
      clear: "\u6E05\u7A7A\u5BF9\u8BDD",
      useContext: "\u643A\u5E26\u5F53\u524D\u4EE3\u7801\u4F5C\u4E3A\u4E0A\u4E0B\u6587",
      insert: "\u63D2\u5165\u5230\u7F16\u8F91\u5668",
      replace: "\u66FF\u6362\u7F16\u8F91\u5668\u5185\u5BB9",
      copy: "\u590D\u5236\u4EE3\u7801",
      copySuccess: "\u4EE3\u7801\u5DF2\u590D\u5236\u5230\u526A\u8D34\u677F",
      user: "\u6211",
      assistant: "AI"
    }
  };
  var localEn = {
    name: "English",
    ai: {
      name: "AI Coding",
      title: "AI Assistant",
      open: "Open AI Dialog",
      welcome: "AI Coding Assistant",
      welcomeDesc: "I can help you generate code according to magic-api script syntax. Please describe your requirements.",
      placeholder: "Describe the function you need to implement, press Enter to send, Shift+Enter to wrap...",
      send: "Send",
      clear: "Clear Chat",
      useContext: "Include current code as context",
      insert: "Insert to Editor",
      replace: "Replace Editor Content",
      copy: "Copy Code",
      copySuccess: "Code copied to clipboard",
      user: "Me",
      assistant: "AI"
    }
  };
  var magicAiDialog_vue_vue_type_style_index_0_scoped_true_lang = "";
  var magicAiDialog_vue_vue_type_style_index_1_lang = "";
  var _export_sfc = (sfc, props) => {
    const target = sfc.__vccOpts || sfc;
    for (const [key, val] of props) {
      target[key] = val;
    }
    return target;
  };
  const _hoisted_1 = {
    key: 0,
    class: "magic-ai-panel"
  };
  const _hoisted_2 = { class: "magic-ai-panel-header" };
  const _hoisted_3 = { class: "magic-ai-container" };
  const _hoisted_4 = {
    key: 0,
    class: "magic-ai-welcome"
  };
  const _hoisted_5 = { class: "magic-ai-suggestions" };
  const _hoisted_6 = ["onClick"];
  const _hoisted_7 = { class: "magic-ai-message-avatar" };
  const _hoisted_8 = { key: 0 };
  const _hoisted_9 = { key: 1 };
  const _hoisted_10 = { class: "magic-ai-message-content" };
  const _hoisted_11 = ["innerHTML"];
  const _hoisted_12 = { key: 1 };
  const _hoisted_13 = {
    key: 2,
    class: "magic-ai-code-actions"
  };
  const _hoisted_14 = ["onClick"];
  const _hoisted_15 = ["onClick"];
  const _hoisted_16 = ["onClick"];
  const _hoisted_17 = {
    key: 0,
    class: "magic-ai-message magic-ai-message--assistant"
  };
  const _hoisted_18 = { class: "magic-ai-input-area" };
  const _hoisted_19 = { class: "magic-ai-input-toolbar" };
  const _hoisted_20 = { class: "magic-ai-context-toggle" };
  const _hoisted_21 = ["disabled"];
  const _hoisted_22 = { class: "magic-ai-input-wrapper" };
  const _hoisted_23 = ["disabled"];
  const _hoisted_24 = ["disabled"];
  const _sfc_main = {
    __name: "magic-ai-dialog",
    props: {
      bus: Object,
      request: Object,
      modal: Object
    },
    setup(__props, { expose: __expose }) {
      const props = __props;
      const visible = vue.ref(false);
      const messages = vue.ref([]);
      const inputText = vue.ref("");
      const loading = vue.ref(false);
      const useCurrentCode = vue.ref(false);
      const messagesContainer = vue.ref(null);
      const inputRef = vue.ref(null);
      const suggestions = [
        "\u67E5\u8BE2\u7528\u6237\u5217\u8868\uFF0C\u652F\u6301\u6309\u540D\u79F0\u6A21\u7CCA\u641C\u7D22\u548C\u5206\u9875",
        "\u6839\u636EID\u67E5\u8BE2\u7528\u6237\u8BE6\u60C5\u5E76\u8FD4\u56DE",
        "\u65B0\u589E\u7528\u6237\uFF0C\u63A5\u6536POST\u8BF7\u6C42\u4F53\u53C2\u6570",
        "\u66F4\u65B0\u7528\u6237\u4FE1\u606F\uFF0C\u6839\u636EID\u4FEE\u6539",
        "\u5220\u9664\u7528\u6237\uFF0C\u6839\u636EID\u5220\u9664"
      ];
      props.bus.$on("ai-dialog-open", () => {
        visible.value = true;
        vue.nextTick(() => inputRef.value && inputRef.value.focus());
      });
      const onClose = () => {
        visible.value = false;
      };
      const sendSuggestion = (text) => {
        inputText.value = text;
        sendMessage();
      };
      const sendMessage = async () => {
        const text = inputText.value.trim();
        if (!text || loading.value)
          return;
        let currentCode = "";
        if (useCurrentCode.value) {
          props.bus.$emit("ai-get-current-code", (code) => {
            currentCode = code || "";
          });
          await vue.nextTick();
        }
        messages.value.push({ role: "user", content: text });
        inputText.value = "";
        loading.value = true;
        scrollToBottom();
        const history = messages.value.slice(0, -1).slice(-10).map((m) => ({
          role: m.role,
          content: m.content
        }));
        props.request.sendJson("/ai/chat", {
          message: text,
          history,
          currentCode: useCurrentCode.value ? currentCode : ""
        }).success((response) => {
          messages.value.push({ role: "assistant", content: response.message });
          scrollToBottom();
        }).error(() => {
          messages.value.push({ role: "assistant", content: "\u274C \u8BF7\u6C42\u5931\u8D25\uFF0C\u8BF7\u68C0\u67E5AI\u914D\u7F6E\u662F\u5426\u6B63\u786E\u3002" });
          scrollToBottom();
        }).end(() => {
          loading.value = false;
        });
      };
      const onKeydown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          sendMessage();
        }
      };
      const clearMessages = () => {
        messages.value = [];
      };
      const scrollToBottom = () => {
        vue.nextTick(() => {
          if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
          }
        });
      };
      const extractCode = (content) => {
        if (!content)
          return null;
        const match = content.match(/```(?:magicscript|ms)?\s*\n?([\s\S]*?)```/);
        return match ? match[1].trim() : null;
      };
      const insertCode = (code) => {
        props.bus.$emit("ai-insert-code", code);
        props.modal.message("\u4EE3\u7801\u5DF2\u63D2\u5165\u5230\u7F16\u8F91\u5668");
      };
      const replaceCode = (code) => {
        props.modal.confirm("\u63D0\u793A", "\u786E\u5B9A\u8981\u66FF\u6362\u5F53\u524D\u7F16\u8F91\u5668\u4E2D\u7684\u5168\u90E8\u5185\u5BB9\u5417\uFF1F", () => {
          props.bus.$emit("ai-replace-code", code);
          props.modal.message("\u4EE3\u7801\u5DF2\u66FF\u6362");
        });
      };
      const copyCode = (code) => {
        navigator.clipboard.writeText(code).then(() => {
          props.modal.message("\u4EE3\u7801\u5DF2\u590D\u5236\u5230\u526A\u8D34\u677F");
        }).catch(() => {
          props.modal.alert("\u590D\u5236\u5931\u8D25\uFF0C\u8BF7\u624B\u52A8\u590D\u5236");
        });
      };
      const renderMarkdown = (content) => {
        if (!content)
          return "";
        let html = content.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        html = html.replace(/```(?:magicscript|ms|java|sql|json|javascript|js)?\s*\n?([\s\S]*?)```/g, (match, code) => {
          return `<div class="magic-ai-code-block"><pre><code>${code.trim()}</code></pre></div>`;
        });
        html = html.replace(/`([^`\n]+)`/g, '<code class="magic-ai-inline-code">$1</code>');
        html = html.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
        html = html.replace(/^### (.+)$/gm, "<h4>$1</h4>");
        html = html.replace(/^## (.+)$/gm, "<h3>$1</h3>");
        html = html.replace(/^# (.+)$/gm, "<h2>$1</h2>");
        html = html.replace(/^- (.+)$/gm, "<li>$1</li>");
        html = html.replace(/(<li>[\s\S]*?<\/li>)/g, "<ul>$1</ul>");
        html = html.replace(/<\/ul>\s*<ul>/g, "");
        html = html.replace(/\n/g, "<br/>");
        return html;
      };
      __expose({ visible });
      return (_ctx, _cache) => {
        const _component_magic_icon = vue.resolveComponent("magic-icon");
        return vue.openBlock(), vue.createBlock(vue.Teleport, { to: ".magic-editor" }, [
          visible.value ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_1, [
            vue.createElementVNode("div", _hoisted_2, [
              _cache[2] || (_cache[2] = vue.createElementVNode("span", null, "AI Coding \u52A9\u624B", -1)),
              vue.createElementVNode("span", {
                class: "magic-ai-close",
                onClick: onClose
              }, [
                vue.createVNode(_component_magic_icon, { icon: "close" })
              ])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              vue.createElementVNode("div", {
                class: "magic-ai-messages",
                ref_key: "messagesContainer",
                ref: messagesContainer
              }, [
                messages.value.length === 0 ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_4, [
                  _cache[3] || (_cache[3] = vue.createElementVNode("div", { class: "magic-ai-welcome-icon" }, "\u2728", -1)),
                  _cache[4] || (_cache[4] = vue.createElementVNode("div", { class: "magic-ai-welcome-title" }, "AI Coding \u52A9\u624B", -1)),
                  _cache[5] || (_cache[5] = vue.createElementVNode("div", { class: "magic-ai-welcome-desc" }, "\u6211\u53EF\u4EE5\u5E2E\u4F60\u6309\u7167 magic-api \u811A\u672C\u8BED\u6CD5\u751F\u6210\u4EE3\u7801\uFF0C\u8BF7\u63CF\u8FF0\u4F60\u7684\u9700\u6C42\u3002", -1)),
                  vue.createElementVNode("div", _hoisted_5, [
                    (vue.openBlock(), vue.createElementBlock(vue.Fragment, null, vue.renderList(suggestions, (s) => {
                      return vue.createElementVNode("div", {
                        class: "magic-ai-suggestion",
                        key: s,
                        onClick: ($event) => sendSuggestion(s)
                      }, vue.toDisplayString(s), 9, _hoisted_6);
                    }), 64))
                  ])
                ])) : (vue.openBlock(), vue.createElementBlock(vue.Fragment, { key: 1 }, [
                  (vue.openBlock(true), vue.createElementBlock(vue.Fragment, null, vue.renderList(messages.value, (msg, index2) => {
                    return vue.openBlock(), vue.createElementBlock("div", {
                      key: index2,
                      class: vue.normalizeClass(["magic-ai-message", msg.role === "user" ? "magic-ai-message--user" : "magic-ai-message--assistant"])
                    }, [
                      vue.createElementVNode("div", _hoisted_7, [
                        msg.role === "user" ? (vue.openBlock(), vue.createElementBlock("span", _hoisted_8, "\u6211")) : (vue.openBlock(), vue.createElementBlock("span", _hoisted_9, "AI"))
                      ]),
                      vue.createElementVNode("div", _hoisted_10, [
                        msg.role === "assistant" ? (vue.openBlock(), vue.createElementBlock("div", {
                          key: 0,
                          innerHTML: renderMarkdown(msg.content)
                        }, null, 8, _hoisted_11)) : (vue.openBlock(), vue.createElementBlock("div", _hoisted_12, vue.toDisplayString(msg.content), 1)),
                        msg.role === "assistant" && extractCode(msg.content) ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_13, [
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--insert",
                            onClick: ($event) => insertCode(extractCode(msg.content))
                          }, " \u63D2\u5165\u5230\u7F16\u8F91\u5668 ", 8, _hoisted_14),
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--replace",
                            onClick: ($event) => replaceCode(extractCode(msg.content))
                          }, " \u66FF\u6362\u7F16\u8F91\u5668\u5185\u5BB9 ", 8, _hoisted_15),
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--copy",
                            onClick: ($event) => copyCode(extractCode(msg.content))
                          }, " \u590D\u5236\u4EE3\u7801 ", 8, _hoisted_16)
                        ])) : vue.createCommentVNode("", true)
                      ])
                    ], 2);
                  }), 128)),
                  loading.value ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_17, [..._cache[6] || (_cache[6] = [
                    vue.createElementVNode("div", { class: "magic-ai-message-avatar" }, [
                      vue.createElementVNode("span", null, "AI")
                    ], -1),
                    vue.createElementVNode("div", { class: "magic-ai-message-content" }, [
                      vue.createElementVNode("div", { class: "magic-ai-loading" }, [
                        vue.createElementVNode("span"),
                        vue.createElementVNode("span"),
                        vue.createElementVNode("span")
                      ])
                    ], -1)
                  ])])) : vue.createCommentVNode("", true)
                ], 64))
              ], 512),
              vue.createElementVNode("div", _hoisted_18, [
                vue.createElementVNode("div", _hoisted_19, [
                  vue.createElementVNode("label", _hoisted_20, [
                    vue.withDirectives(vue.createElementVNode("input", {
                      type: "checkbox",
                      "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => useCurrentCode.value = $event)
                    }, null, 512), [
                      [vue.vModelCheckbox, useCurrentCode.value]
                    ]),
                    _cache[7] || (_cache[7] = vue.createTextVNode(" \u643A\u5E26\u5F53\u524D\u4EE3\u7801\u4F5C\u4E3A\u4E0A\u4E0B\u6587 ", -1))
                  ]),
                  vue.createElementVNode("button", {
                    class: "magic-ai-clear-btn",
                    onClick: clearMessages,
                    disabled: messages.value.length === 0
                  }, " \u6E05\u7A7A\u5BF9\u8BDD ", 8, _hoisted_21)
                ]),
                vue.createElementVNode("div", _hoisted_22, [
                  vue.withDirectives(vue.createElementVNode("textarea", {
                    ref_key: "inputRef",
                    ref: inputRef,
                    "onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => inputText.value = $event),
                    class: "magic-ai-textarea",
                    placeholder: "\u63CF\u8FF0\u4F60\u9700\u8981\u5B9E\u73B0\u7684\u529F\u80FD\uFF0C\u6309 Enter \u53D1\u9001\uFF0CShift+Enter \u6362\u884C...",
                    onKeydown,
                    disabled: loading.value
                  }, null, 40, _hoisted_23), [
                    [vue.vModelText, inputText.value]
                  ]),
                  vue.createElementVNode("button", {
                    class: "magic-ai-send-btn",
                    onClick: sendMessage,
                    disabled: loading.value || !inputText.value.trim()
                  }, [
                    vue.createVNode(_component_magic_icon, { icon: "run" })
                  ], 8, _hoisted_24)
                ])
              ])
            ])
          ])) : vue.createCommentVNode("", true)
        ]);
      };
    }
  };
  var MagicAiDialog = /* @__PURE__ */ _export_sfc(_sfc_main, [["__scopeId", "data-v-483a8d2a"]]);
  if (typeof window !== "undefined") {
    let loadSvg = function() {
      var body = document.body;
      var svgDom = document.getElementById("__svg__icons__dom__1775734172618__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1775734172618__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol class="icon" viewBox="0 0 1024 1024"  id="magic-ai-ai"><defs><linearGradient id="magic-ai-ai_a" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#83D3E7" /><stop offset="50%" stop-color="#6950BE" /><stop offset="100%" stop-color="#C53B77" /></linearGradient></defs><path d="M512 128c-211.68 0-384 164.32-384 368 0 123.36 62.88 233.44 159.84 297.28l31.52-36C239.04 702.56 192 606.24 192 496c0-168.48 139.04-304 320-304s320 135.52 320 304c0 110.24-47.04 206.56-127.36 261.28l31.52 36A355.565 355.565 0 0 0 896 496c0-203.68-172.32-368-384-368Zm0 128c-145.76 0-256 106.08-256 240 0 90.4 43.68 170.08 114.4 219.36l31.52-36C348.64 640.16 320 570.72 320 496c0-98.08 78.24-176 192-176s192 77.92 192 176c0 74.72-28.64 144.16-81.92 183.36l31.52 36C724.32 666.08 768 586.4 768 496c0-133.92-110.24-240-256-240Zm0 128c-79.36 0-128 52.64-128 112 0 43.36 19.36 84.32 58.24 109.28l31.52-36C446.56 551.2 448 523.04 448 496c0-26.72 25.76-48 64-48 38.24 0 64 21.28 64 48 0 27.04 1.44 55.2-25.76 73.28l31.52 36C620.64 580.32 640 539.36 640 496c0-59.36-48.64-112-128-112Z" fill="url(#magic-ai-ai_a)" /></symbol>';
      body.insertBefore(svgDom, body.firstChild);
    };
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", loadSvg);
    } else {
      loadSvg();
    }
  }
  var index = (opt) => {
    const { i18n, bus, request, modal } = opt;
    i18n.add("zh-cn", localZhCN);
    i18n.add("en", localEn);
    window.magicEditorBus = bus;
    document.addEventListener("keydown", (e) => {
      if (e.ctrlKey && e.shiftKey && e.key === "A") {
        e.preventDefault();
        bus.$emit("ai-dialog-open");
      }
    });
    return {
      datasources: [{
        type: "ai",
        icon: "ai",
        title: i18n.format("ai.title"),
        name: i18n.format("ai.name"),
        onClick: () => bus.$emit("ai-dialog-open"),
        component: MagicAiDialog
      }]
    };
  };
  return index;
}(Vue);
