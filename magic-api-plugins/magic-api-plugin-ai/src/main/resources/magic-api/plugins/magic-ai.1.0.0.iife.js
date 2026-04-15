var __vite_style__ = document.createElement("style");
__vite_style__.innerHTML = "\n.magic-ai-panel[data-v-4bd039a3] {\r\n    position: absolute;\r\n    top: var(--magic-header-height);\r\n    right: 0;\r\n    bottom: 0;\r\n    width: 420px;\r\n    display: flex;\r\n    flex-direction: column;\r\n    background: var(--main-background-color);\r\n    border-left: 1px solid var(--main-border-color);\r\n    z-index: 100;\r\n    box-shadow: -4px 0 16px rgba(0, 0, 0, 0.08);\r\n    animation: slideInRight-4bd039a3 0.2s ease-out;\n}\n@keyframes slideInRight-4bd039a3 {\nfrom { transform: translateX(20px); opacity: 0;\n}\nto { transform: translateX(0); opacity: 1;\n}\n}\r\n\r\n/* Header */\n.magic-ai-panel-header[data-v-4bd039a3] {\r\n    height: 44px;\r\n    padding: 0 14px;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: space-between;\r\n    border-bottom: 1px solid var(--main-border-color);\r\n    background: var(--main-background-color);\r\n    flex-shrink: 0;\n}\n.magic-ai-header-left[data-v-4bd039a3] {\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 8px;\n}\n.magic-ai-header-icon[data-v-4bd039a3] {\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    width: 28px;\r\n    height: 28px;\r\n    border-radius: 8px;\r\n    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);\r\n    color: #fff;\n}\n.magic-ai-header-title[data-v-4bd039a3] {\r\n    font-size: 13px;\r\n    font-weight: 600;\r\n    color: var(--main-color);\r\n    letter-spacing: 0.3px;\n}\n.magic-ai-close[data-v-4bd039a3] {\r\n    cursor: pointer;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    width: 28px;\r\n    height: 28px;\r\n    border-radius: 6px;\r\n    opacity: 0.5;\r\n    transition: all 0.15s;\n}\n.magic-ai-close[data-v-4bd039a3]:hover {\r\n    opacity: 1;\r\n    background: var(--select-option-hover-background-color);\n}\n.magic-ai-close[data-v-4bd039a3] .magic-icon {\r\n    width: 14px;\r\n    height: 14px;\n}\r\n\r\n/* Container */\n.magic-ai-container[data-v-4bd039a3] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    flex: 1;\r\n    min-height: 0;\n}\r\n\r\n/* Messages area */\n.magic-ai-messages[data-v-4bd039a3] {\r\n    flex: 1;\r\n    overflow-y: auto;\r\n    padding: 16px;\r\n    display: flex;\r\n    flex-direction: column;\r\n    gap: 16px;\r\n    min-height: 0;\n}\n.magic-ai-messages[data-v-4bd039a3]::-webkit-scrollbar {\r\n    width: 5px;\n}\n.magic-ai-messages[data-v-4bd039a3]::-webkit-scrollbar-thumb {\r\n    background: var(--main-border-color);\r\n    border-radius: 3px;\n}\n.magic-ai-messages[data-v-4bd039a3]::-webkit-scrollbar-track {\r\n    background: transparent;\n}\r\n\r\n/* Welcome */\n.magic-ai-welcome[data-v-4bd039a3] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    align-items: center;\r\n    justify-content: center;\r\n    height: 100%;\r\n    padding: 20px;\n}\n.magic-ai-welcome-icon-wrap[data-v-4bd039a3] {\r\n    margin-bottom: 16px;\n}\n.magic-ai-welcome-title[data-v-4bd039a3] {\r\n    font-size: 17px;\r\n    font-weight: 600;\r\n    color: var(--main-color);\r\n    margin-bottom: 6px;\n}\n.magic-ai-welcome-desc[data-v-4bd039a3] {\r\n    font-size: 12px;\r\n    color: var(--empty-color);\r\n    margin-bottom: 24px;\r\n    text-align: center;\r\n    line-height: 1.5;\r\n    max-width: 280px;\n}\n.magic-ai-suggestions[data-v-4bd039a3] {\r\n    display: flex;\r\n    flex-direction: column;\r\n    gap: 6px;\r\n    width: 100%;\r\n    max-width: 360px;\n}\n.magic-ai-suggestion[data-v-4bd039a3] {\r\n    padding: 10px 14px;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 8px;\r\n    cursor: pointer;\r\n    font-size: 12px;\r\n    color: var(--main-color);\r\n    transition: all 0.15s ease;\r\n    text-align: left;\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 8px;\r\n    line-height: 1.4;\n}\n.magic-ai-suggestion-icon[data-v-4bd039a3] {\r\n    display: flex;\r\n    align-items: center;\r\n    flex-shrink: 0;\r\n    opacity: 0.4;\r\n    transition: all 0.15s;\n}\n.magic-ai-suggestion[data-v-4bd039a3]:hover {\r\n    background: var(--select-option-hover-background-color);\r\n    border-color: #5b7ee5;\r\n    transform: translateX(2px);\n}\n.magic-ai-suggestion:hover .magic-ai-suggestion-icon[data-v-4bd039a3] {\r\n    opacity: 0.8;\r\n    color: #5b7ee5;\n}\r\n\r\n/* Messages */\n.magic-ai-message[data-v-4bd039a3] {\r\n    display: flex;\r\n    gap: 10px;\r\n    align-items: flex-start;\r\n    animation: fadeInMsg-4bd039a3 0.2s ease-out;\n}\n@keyframes fadeInMsg-4bd039a3 {\nfrom { opacity: 0; transform: translateY(6px);\n}\nto { opacity: 1; transform: translateY(0);\n}\n}\n.magic-ai-message--user[data-v-4bd039a3] {\r\n    flex-direction: row-reverse;\n}\n.magic-ai-message-avatar[data-v-4bd039a3] {\r\n    width: 30px;\r\n    height: 30px;\r\n    border-radius: 10px;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    font-size: 11px;\r\n    font-weight: bold;\r\n    flex-shrink: 0;\n}\n.magic-ai-message--user .magic-ai-message-avatar[data-v-4bd039a3] {\r\n    background: #5b7ee5;\r\n    color: #fff;\n}\n.magic-ai-message--assistant .magic-ai-message-avatar[data-v-4bd039a3] {\r\n    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);\r\n    color: #fff;\n}\n.magic-ai-message-body[data-v-4bd039a3] {\r\n    max-width: 82%;\r\n    min-width: 0;\n}\n.magic-ai-message-content[data-v-4bd039a3] {\r\n    padding: 10px 14px;\r\n    border-radius: 12px;\r\n    font-size: 13px;\r\n    line-height: 1.65;\r\n    color: var(--main-color);\r\n    background: var(--select-option-hover-background-color);\r\n    word-break: break-word;\n}\n.magic-ai-message--user .magic-ai-message-content[data-v-4bd039a3] {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--main-color);\r\n    border-radius: 12px 4px 12px 12px;\n}\n.magic-ai-message--assistant .magic-ai-message-content[data-v-4bd039a3] {\r\n    border-radius: 4px 12px 12px 12px;\n}\r\n\r\n/* Code action buttons */\n.magic-ai-code-actions[data-v-4bd039a3] {\r\n    display: flex;\r\n    gap: 6px;\r\n    margin-top: 8px;\r\n    flex-wrap: wrap;\n}\n.magic-ai-btn[data-v-4bd039a3] {\r\n    padding: 4px 10px;\r\n    border-radius: 6px;\r\n    font-size: 11px;\r\n    cursor: pointer;\r\n    border: none;\r\n    transition: all 0.15s;\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 4px;\r\n    font-family: inherit;\n}\n.magic-ai-btn--insert[data-v-4bd039a3] {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--main-color);\r\n    border: 1px solid var(--main-border-color);\n}\n.magic-ai-btn--insert[data-v-4bd039a3]:hover {\r\n    background: #5b7ee5;\r\n    color: #fff;\r\n    border-color: #5b7ee5;\n}\n.magic-ai-btn--replace[data-v-4bd039a3] {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--main-color);\r\n    border: 1px solid var(--main-border-color);\n}\n.magic-ai-btn--replace[data-v-4bd039a3]:hover {\r\n    background: #5b7ee5;\r\n    color: #fff;\r\n    border-color: #5b7ee5;\n}\n.magic-ai-btn--copy[data-v-4bd039a3] {\r\n    background: transparent;\r\n    color: var(--main-color);\r\n    border: 1px solid var(--main-border-color);\n}\n.magic-ai-btn--copy[data-v-4bd039a3]:hover {\r\n    background: var(--select-option-hover-background-color);\n}\r\n\r\n/* Loading dots */\n.magic-ai-loading[data-v-4bd039a3] {\r\n    display: flex;\r\n    gap: 5px;\r\n    align-items: center;\r\n    padding: 4px 0;\n}\n.magic-ai-loading span[data-v-4bd039a3] {\r\n    width: 6px;\r\n    height: 6px;\r\n    border-radius: 50%;\r\n    background: var(--main-icon-color, #999);\r\n    animation: aiDot-4bd039a3 1.4s infinite ease-in-out;\n}\n.magic-ai-loading span[data-v-4bd039a3]:nth-child(2) { animation-delay: 0.2s;\n}\n.magic-ai-loading span[data-v-4bd039a3]:nth-child(3) { animation-delay: 0.4s;\n}\n@keyframes aiDot-4bd039a3 {\n0%, 80%, 100% { transform: scale(0.5); opacity: 0.3;\n}\n40% { transform: scale(1); opacity: 1;\n}\n}\r\n\r\n/* Input area */\n.magic-ai-input-area[data-v-4bd039a3] {\r\n    border-top: 1px solid var(--main-border-color);\r\n    padding: 10px 14px 12px;\r\n    background: var(--main-background-color);\r\n    flex-shrink: 0;\n}\r\n\r\n/* Model selector */\n.magic-ai-model-selector[data-v-4bd039a3] {\r\n    display: flex;\r\n    gap: 6px;\r\n    margin-bottom: 8px;\n}\n.magic-ai-select-wrap[data-v-4bd039a3] {\r\n    flex: 1;\r\n    position: relative;\r\n    display: flex;\r\n    align-items: center;\n}\n.magic-ai-select-wrap--model[data-v-4bd039a3] {\r\n    flex: 1.5;\n}\n.magic-ai-select[data-v-4bd039a3] {\r\n    width: 100%;\r\n    height: 30px;\r\n    padding: 0 22px 0 8px;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 6px;\r\n    background: var(--main-background-color);\r\n    color: var(--main-color);\r\n    font-size: 12px;\r\n    outline: none;\r\n    cursor: pointer;\r\n    appearance: none;\r\n    -webkit-appearance: none;\r\n    transition: border-color 0.15s;\n}\n.magic-ai-select[data-v-4bd039a3]:focus {\r\n    border-color: #5b7ee5;\n}\n.magic-ai-select-arrow[data-v-4bd039a3] {\r\n    position: absolute;\r\n    right: 8px;\r\n    pointer-events: none;\r\n    opacity: 0.5;\r\n    color: var(--main-color);\n}\r\n\r\n/* Toolbar */\n.magic-ai-input-toolbar[data-v-4bd039a3] {\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: space-between;\r\n    margin-bottom: 8px;\n}\n.magic-ai-context-toggle[data-v-4bd039a3] {\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 5px;\r\n    cursor: pointer;\r\n    user-select: none;\n}\n.magic-ai-context-toggle input[data-v-4bd039a3] {\r\n    cursor: pointer;\r\n    accent-color: #5b7ee5;\r\n    width: 14px;\r\n    height: 14px;\n}\n.magic-ai-toggle-text[data-v-4bd039a3] {\r\n    font-size: 11px;\r\n    color: var(--empty-color);\n}\n.magic-ai-clear-btn[data-v-4bd039a3] {\r\n    padding: 3px 8px;\r\n    background: transparent;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 5px;\r\n    font-size: 11px;\r\n    cursor: pointer;\r\n    color: var(--empty-color);\r\n    display: flex;\r\n    align-items: center;\r\n    gap: 3px;\r\n    transition: all 0.15s;\r\n    font-family: inherit;\n}\n.magic-ai-clear-btn[data-v-4bd039a3]:hover:not(:disabled) {\r\n    background: var(--select-option-hover-background-color);\r\n    color: var(--main-color);\r\n    border-color: var(--main-color);\n}\n.magic-ai-clear-btn[data-v-4bd039a3]:disabled { opacity: 0.3; cursor: default;\n}\r\n\r\n/* Input wrapper */\n.magic-ai-input-wrapper[data-v-4bd039a3] {\r\n    display: flex;\r\n    gap: 8px;\r\n    align-items: flex-end;\n}\n.magic-ai-textarea[data-v-4bd039a3] {\r\n    flex: 1;\r\n    min-height: 56px;\r\n    max-height: 120px;\r\n    padding: 8px 12px;\r\n    border: 1px solid var(--main-border-color);\r\n    border-radius: 10px;\r\n    resize: none;\r\n    font-size: 13px;\r\n    background: var(--main-background-color);\r\n    color: var(--main-color);\r\n    font-family: inherit;\r\n    line-height: 1.5;\r\n    outline: none;\r\n    transition: border-color 0.2s, box-shadow 0.2s;\n}\n.magic-ai-textarea[data-v-4bd039a3]:focus {\r\n    border-color: #5b7ee5;\r\n    box-shadow: 0 0 0 2px rgba(91, 126, 229, 0.12);\n}\n.magic-ai-textarea[data-v-4bd039a3]:disabled { opacity: 0.5; cursor: not-allowed;\n}\n.magic-ai-textarea[data-v-4bd039a3]::placeholder { color: var(--empty-color); font-size: 12px;\n}\r\n\r\n/* Send button */\n.magic-ai-send-btn[data-v-4bd039a3] {\r\n    width: 36px;\r\n    height: 36px;\r\n    border-radius: 10px;\r\n    background: linear-gradient(135deg, #5b7ee5 0%, #4da6e8 100%);\r\n    border: none;\r\n    cursor: pointer;\r\n    display: flex;\r\n    align-items: center;\r\n    justify-content: center;\r\n    flex-shrink: 0;\r\n    transition: all 0.2s;\r\n    color: #fff;\n}\n.magic-ai-send-btn[data-v-4bd039a3]:hover:not(:disabled) {\r\n    transform: translateY(-1px);\r\n    box-shadow: 0 3px 10px rgba(91, 126, 229, 0.3);\n}\n.magic-ai-send-btn[data-v-4bd039a3]:active:not(:disabled) {\r\n    transform: translateY(0);\n}\n.magic-ai-send-btn[data-v-4bd039a3]:disabled {\r\n    background: var(--main-border-color);\r\n    cursor: not-allowed;\r\n    opacity: 0.5;\n}\r\n\r\n/* Streaming cursor */\n.magic-ai-cursor[data-v-4bd039a3] {\r\n    display: inline;\r\n    animation: blink-4bd039a3 1s step-end infinite;\r\n    color: var(--main-color);\r\n    font-weight: bold;\n}\n@keyframes blink-4bd039a3 {\n0%, 50% { opacity: 1;\n}\n51%, 100% { opacity: 0;\n}\n}\r\n\r\n/* Code block global styles */\n.magic-ai-message-content .magic-ai-code-block {\r\n    background: #1a1b26;\r\n    border-radius: 8px;\r\n    margin: 8px 0;\r\n    overflow: hidden;\r\n    border: 1px solid rgba(255, 255, 255, 0.06);\n}\n.magic-ai-message-content .magic-ai-code-block pre {\r\n    margin: 0;\r\n    padding: 14px 16px;\r\n    overflow-x: auto;\n}\n.magic-ai-message-content .magic-ai-code-block pre::-webkit-scrollbar {\r\n    height: 4px;\n}\n.magic-ai-message-content .magic-ai-code-block pre::-webkit-scrollbar-thumb {\r\n    background: rgba(255, 255, 255, 0.15);\r\n    border-radius: 2px;\n}\n.magic-ai-message-content .magic-ai-code-block code {\r\n    color: #c0caf5;\r\n    font-family: 'JetBrainsMono', 'Fira Code', Consolas, 'Courier New', monospace;\r\n    font-size: 12px;\r\n    line-height: 1.6;\r\n    white-space: pre;\n}\n.magic-ai-message-content .magic-ai-inline-code {\r\n    background: var(--select-option-hover-background-color);\r\n    padding: 1px 6px;\r\n    border-radius: 4px;\r\n    font-family: 'JetBrainsMono', Consolas, monospace;\r\n    font-size: 12px;\r\n    color: var(--main-color);\n}\n.magic-ai-message-content h2,\r\n.magic-ai-message-content h3,\r\n.magic-ai-message-content h4 {\r\n    margin: 10px 0 4px;\r\n    font-weight: 600;\r\n    line-height: 1.4;\n}\n.magic-ai-message-content h2 { font-size: 15px;\n}\n.magic-ai-message-content h3 { font-size: 14px;\n}\n.magic-ai-message-content h4 { font-size: 13px;\n}\n.magic-ai-message-content ul {\r\n    margin: 4px 0;\r\n    padding-left: 18px;\n}\n.magic-ai-message-content li {\r\n    margin: 3px 0;\r\n    line-height: 1.5;\n}\n.magic-ai-message-content strong {\r\n    font-weight: 600;\n}\r\n";
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
      assistant: "AI",
      provider: "\u63D0\u4F9B\u5546",
      model: "\u6A21\u578B",
      switchFailed: "\u5207\u6362\u6A21\u578B\u5931\u8D25\uFF0C\u8BF7\u68C0\u67E5\u914D\u7F6E"
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
      assistant: "AI",
      provider: "Provider",
      model: "Model",
      switchFailed: "Failed to switch model, please check configuration"
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
  const _hoisted_10 = { class: "magic-ai-message-body" };
  const _hoisted_11 = { class: "magic-ai-message-content" };
  const _hoisted_12 = ["innerHTML"];
  const _hoisted_13 = { key: 1 };
  const _hoisted_14 = {
    key: 2,
    class: "magic-ai-cursor"
  };
  const _hoisted_15 = {
    key: 0,
    class: "magic-ai-code-actions"
  };
  const _hoisted_16 = ["onClick"];
  const _hoisted_17 = ["onClick"];
  const _hoisted_18 = ["onClick"];
  const _hoisted_19 = {
    key: 0,
    class: "magic-ai-message magic-ai-message--assistant"
  };
  const _hoisted_20 = { class: "magic-ai-input-area" };
  const _hoisted_21 = {
    key: 0,
    class: "magic-ai-model-selector"
  };
  const _hoisted_22 = { class: "magic-ai-select-wrap" };
  const _hoisted_23 = ["value"];
  const _hoisted_24 = ["value"];
  const _hoisted_25 = { class: "magic-ai-select-wrap magic-ai-select-wrap--model" };
  const _hoisted_26 = ["value"];
  const _hoisted_27 = ["value"];
  const _hoisted_28 = { class: "magic-ai-input-toolbar" };
  const _hoisted_29 = { class: "magic-ai-context-toggle" };
  const _hoisted_30 = ["disabled"];
  const _hoisted_31 = { class: "magic-ai-input-wrapper" };
  const _hoisted_32 = ["disabled"];
  const _hoisted_33 = ["disabled"];
  const STORAGE_KEY_PROVIDER = "magic-ai-provider";
  const STORAGE_KEY_MODEL = "magic-ai-model";
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
      const streaming = vue.ref(false);
      const useCurrentCode = vue.ref(false);
      const messagesContainer = vue.ref(null);
      const inputRef = vue.ref(null);
      const activeProvider = vue.ref("");
      const activeModel = vue.ref("");
      const configuredProviders = vue.ref([]);
      const configLoaded = vue.ref(false);
      const loadLocalSelection = () => {
        try {
          const p = localStorage.getItem(STORAGE_KEY_PROVIDER);
          const m = localStorage.getItem(STORAGE_KEY_MODEL);
          if (p)
            activeProvider.value = p;
          if (m)
            activeModel.value = m;
        } catch (e) {
        }
      };
      const saveLocalSelection = () => {
        try {
          localStorage.setItem(STORAGE_KEY_PROVIDER, activeProvider.value);
          localStorage.setItem(STORAGE_KEY_MODEL, activeModel.value);
        } catch (e) {
        }
      };
      const selectableProviders = vue.computed(() => {
        return configuredProviders.value.map((p) => ({ key: p.key, label: p.label || p.key }));
      });
      const availableModels = vue.computed(() => {
        const provider = activeProvider.value;
        const found = configuredProviders.value.find((p) => p.key === provider);
        return found && found.models ? found.models : [];
      });
      const loadAiConfig = () => {
        props.request.sendJson("/ai/config/get", {}).success((data) => {
          if (Array.isArray(data)) {
            configuredProviders.value = data;
          }
          loadLocalSelection();
          const providerKeys = configuredProviders.value.map((p) => p.key);
          if (!activeProvider.value || !providerKeys.includes(activeProvider.value)) {
            if (configuredProviders.value.length > 0) {
              activeProvider.value = configuredProviders.value[0].key;
              const models = configuredProviders.value[0].models || [];
              activeModel.value = models[0] || "";
              saveLocalSelection();
            }
          } else {
            const found = configuredProviders.value.find((p) => p.key === activeProvider.value);
            const models = found && found.models ? found.models : [];
            if (activeModel.value && !models.includes(activeModel.value)) {
              activeModel.value = models[0] || "";
              saveLocalSelection();
            }
          }
          configLoaded.value = true;
        }).error(() => {
          configLoaded.value = true;
        });
      };
      const onProviderChange = (e) => {
        const newProvider = e.target.value;
        activeProvider.value = newProvider;
        const found = configuredProviders.value.find((p) => p.key === newProvider);
        const models = found && found.models ? found.models : [];
        activeModel.value = models[0] || "";
        saveLocalSelection();
      };
      const onModelChange = (e) => {
        activeModel.value = e.target.value;
        saveLocalSelection();
      };
      const suggestions = [
        "\u67E5\u8BE2\u7528\u6237\u5217\u8868\uFF0C\u652F\u6301\u6309\u540D\u79F0\u6A21\u7CCA\u641C\u7D22\u548C\u5206\u9875",
        "\u6839\u636EID\u67E5\u8BE2\u7528\u6237\u8BE6\u60C5\u5E76\u8FD4\u56DE",
        "\u65B0\u589E\u7528\u6237\uFF0C\u63A5\u6536POST\u8BF7\u6C42\u4F53\u53C2\u6570",
        "\u66F4\u65B0\u7528\u6237\u4FE1\u606F\uFF0C\u6839\u636EID\u4FEE\u6539"
      ];
      props.bus.$on("ai-dialog-open", () => {
        visible.value = true;
        if (!configLoaded.value) {
          loadAiConfig();
        }
        vue.nextTick(() => inputRef.value && inputRef.value.focus());
      });
      const onClose = () => {
        visible.value = false;
      };
      const sendSuggestion = (text) => {
        inputText.value = text;
        sendMessage();
      };
      const getBaseURL = () => {
        const path = window.location.pathname;
        const dir = path.endsWith("/") ? path.slice(0, -1) : path.substring(0, path.lastIndexOf("/"));
        return window.location.origin + dir;
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
        streaming.value = false;
        scrollToBottom();
        const history = messages.value.slice(0, -1).slice(-10).map((m) => ({
          role: m.role,
          content: m.content
        }));
        messages.value.push({ role: "assistant", content: "" });
        const msgIndex = messages.value.length - 1;
        scrollToBottom();
        const token = localStorage.getItem("magic-token") || "unauthorization";
        const baseURL = getBaseURL();
        try {
          const resp = await fetch(baseURL + "/ai/chat/stream", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "magic-token": token
            },
            body: JSON.stringify({
              message: text,
              history,
              currentCode: useCurrentCode.value ? currentCode : "",
              provider: activeProvider.value,
              model: activeModel.value
            })
          });
          if (!resp.ok) {
            messages.value[msgIndex].content = "\u8BF7\u6C42\u5931\u8D25\uFF0C\u72B6\u6001\u7801: " + resp.status;
            scrollToBottom();
            return;
          }
          streaming.value = true;
          const reader = resp.body.getReader();
          const decoder = new TextDecoder();
          let buffer = "";
          while (true) {
            const { done, value } = await reader.read();
            if (done)
              break;
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split("\n");
            buffer = lines.pop();
            for (const line of lines) {
              const trimmed = line.trim();
              if (!trimmed || trimmed.startsWith(":"))
                continue;
              if (trimmed.startsWith("event:done") || trimmed.startsWith("event: done")) {
                continue;
              }
              if (trimmed.startsWith("data:")) {
                const data = trimmed.substring(5);
                if (data.trim() === "[DONE]")
                  continue;
                try {
                  const text2 = JSON.parse(data);
                  messages.value[msgIndex].content += text2;
                } catch (e) {
                  messages.value[msgIndex].content += data;
                }
                scrollToBottom();
              }
            }
          }
          if (!messages.value[msgIndex].content.trim()) {
            messages.value[msgIndex].content = "AI\u8FD4\u56DE\u4E86\u7A7A\u5185\u5BB9\uFF0C\u8BF7\u68C0\u67E5\u914D\u7F6E\u3002";
          }
        } catch (e) {
          if (!messages.value[msgIndex].content) {
            messages.value[msgIndex].content = "\u8BF7\u6C42\u5931\u8D25\uFF0C\u8BF7\u68C0\u67E5AI\u914D\u7F6E\u662F\u5426\u6B63\u786E\u3002";
          } else {
            messages.value[msgIndex].content += "\n\n\u8FDE\u63A5\u4E2D\u65AD";
          }
          scrollToBottom();
        } finally {
          loading.value = false;
          streaming.value = false;
        }
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
              _cache[2] || (_cache[2] = vue.createElementVNode("div", { class: "magic-ai-header-left" }, [
                vue.createElementVNode("span", { class: "magic-ai-header-icon" }, [
                  vue.createElementVNode("svg", {
                    viewBox: "0 0 24 24",
                    width: "16",
                    height: "16",
                    fill: "none",
                    stroke: "currentColor",
                    "stroke-width": "2",
                    "stroke-linecap": "round",
                    "stroke-linejoin": "round"
                  }, [
                    vue.createElementVNode("path", { d: "M12 2a4 4 0 0 1 4 4v1a3 3 0 0 1 3 3v1a2 2 0 0 1-2 2h-1l-2 5H10l-2-5H7a2 2 0 0 1-2-2v-1a3 3 0 0 1 3-3V6a4 4 0 0 1 4-4z" }),
                    vue.createElementVNode("circle", {
                      cx: "9",
                      cy: "9",
                      r: "1"
                    }),
                    vue.createElementVNode("circle", {
                      cx: "15",
                      cy: "9",
                      r: "1"
                    })
                  ])
                ]),
                vue.createElementVNode("span", { class: "magic-ai-header-title" }, "AI Coding \u52A9\u624B")
              ], -1)),
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
                  _cache[4] || (_cache[4] = vue.createElementVNode("div", { class: "magic-ai-welcome-icon-wrap" }, [
                    vue.createElementVNode("svg", {
                      class: "magic-ai-welcome-svg",
                      viewBox: "0 0 64 64",
                      width: "52",
                      height: "52",
                      fill: "none"
                    }, [
                      vue.createElementVNode("defs", null, [
                        vue.createElementVNode("linearGradient", {
                          id: "aiGrad",
                          x1: "0",
                          y1: "0",
                          x2: "64",
                          y2: "64",
                          gradientUnits: "userSpaceOnUse"
                        }, [
                          vue.createElementVNode("stop", {
                            offset: "0%",
                            "stop-color": "#5b7ee5"
                          }),
                          vue.createElementVNode("stop", {
                            offset: "100%",
                            "stop-color": "#4da6e8"
                          })
                        ])
                      ]),
                      vue.createElementVNode("rect", {
                        rx: "16",
                        width: "64",
                        height: "64",
                        fill: "url(#aiGrad)",
                        opacity: "0.12"
                      }),
                      vue.createElementVNode("path", {
                        d: "M32 14a8 8 0 0 1 8 8v2a6 6 0 0 1 6 6v2a4 4 0 0 1-4 4h-2l-4 10h-8l-4-10h-2a4 4 0 0 1-4-4v-2a6 6 0 0 1 6-6v-2a8 8 0 0 1 8-8z",
                        stroke: "url(#aiGrad)",
                        "stroke-width": "2.5",
                        fill: "none"
                      }),
                      vue.createElementVNode("circle", {
                        cx: "26",
                        cy: "26",
                        r: "2",
                        fill: "#5b7ee5"
                      }),
                      vue.createElementVNode("circle", {
                        cx: "38",
                        cy: "26",
                        r: "2",
                        fill: "#4da6e8"
                      }),
                      vue.createElementVNode("path", {
                        d: "M28 31c1.5 2 6.5 2 8 0",
                        stroke: "url(#aiGrad)",
                        "stroke-width": "2",
                        "stroke-linecap": "round",
                        fill: "none"
                      })
                    ])
                  ], -1)),
                  _cache[5] || (_cache[5] = vue.createElementVNode("div", { class: "magic-ai-welcome-title" }, "AI Coding \u52A9\u624B", -1)),
                  _cache[6] || (_cache[6] = vue.createElementVNode("div", { class: "magic-ai-welcome-desc" }, "\u6211\u53EF\u4EE5\u5E2E\u4F60\u6309\u7167 magic-api \u811A\u672C\u8BED\u6CD5\u751F\u6210\u4EE3\u7801\uFF0C\u8BF7\u63CF\u8FF0\u4F60\u7684\u9700\u6C42\u3002", -1)),
                  vue.createElementVNode("div", _hoisted_5, [
                    (vue.openBlock(), vue.createElementBlock(vue.Fragment, null, vue.renderList(suggestions, (s) => {
                      return vue.createElementVNode("div", {
                        class: "magic-ai-suggestion",
                        key: s,
                        onClick: ($event) => sendSuggestion(s)
                      }, [
                        _cache[3] || (_cache[3] = vue.createElementVNode("span", { class: "magic-ai-suggestion-icon" }, [
                          vue.createElementVNode("svg", {
                            viewBox: "0 0 16 16",
                            width: "14",
                            height: "14",
                            fill: "none",
                            stroke: "currentColor",
                            "stroke-width": "1.5",
                            "stroke-linecap": "round"
                          }, [
                            vue.createElementVNode("path", { d: "M6 3l5 5-5 5" })
                          ])
                        ], -1)),
                        vue.createElementVNode("span", null, vue.toDisplayString(s), 1)
                      ], 8, _hoisted_6);
                    }), 64))
                  ])
                ])) : (vue.openBlock(), vue.createElementBlock(vue.Fragment, { key: 1 }, [
                  (vue.openBlock(true), vue.createElementBlock(vue.Fragment, null, vue.renderList(messages.value, (msg, index2) => {
                    return vue.openBlock(), vue.createElementBlock("div", {
                      key: index2,
                      class: vue.normalizeClass(["magic-ai-message", msg.role === "user" ? "magic-ai-message--user" : "magic-ai-message--assistant"])
                    }, [
                      vue.createElementVNode("div", _hoisted_7, [
                        msg.role === "user" ? (vue.openBlock(), vue.createElementBlock("span", _hoisted_8, [..._cache[7] || (_cache[7] = [
                          vue.createElementVNode("svg", {
                            viewBox: "0 0 20 20",
                            width: "16",
                            height: "16",
                            fill: "currentColor"
                          }, [
                            vue.createElementVNode("path", { d: "M10 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm-7 8a7 7 0 0 1 14 0H3z" })
                          ], -1)
                        ])])) : (vue.openBlock(), vue.createElementBlock("span", _hoisted_9, [..._cache[8] || (_cache[8] = [
                          vue.createElementVNode("svg", {
                            viewBox: "0 0 20 20",
                            width: "16",
                            height: "16",
                            fill: "none",
                            stroke: "currentColor",
                            "stroke-width": "1.5",
                            "stroke-linecap": "round",
                            "stroke-linejoin": "round"
                          }, [
                            vue.createElementVNode("path", { d: "M10 3a3.5 3.5 0 0 1 3.5 3.5V8A2.5 2.5 0 0 1 16 10.5V12a2 2 0 0 1-2 2h-.5L12 17H8l-1.5-3H6a2 2 0 0 1-2-2v-1.5A2.5 2.5 0 0 1 6.5 8v-1.5A3.5 3.5 0 0 1 10 3z" }),
                            vue.createElementVNode("circle", {
                              cx: "8",
                              cy: "9",
                              r: "0.8",
                              fill: "currentColor"
                            }),
                            vue.createElementVNode("circle", {
                              cx: "12",
                              cy: "9",
                              r: "0.8",
                              fill: "currentColor"
                            })
                          ], -1)
                        ])]))
                      ]),
                      vue.createElementVNode("div", _hoisted_10, [
                        vue.createElementVNode("div", _hoisted_11, [
                          msg.role === "assistant" ? (vue.openBlock(), vue.createElementBlock("div", {
                            key: 0,
                            innerHTML: renderMarkdown(msg.content)
                          }, null, 8, _hoisted_12)) : (vue.openBlock(), vue.createElementBlock("div", _hoisted_13, vue.toDisplayString(msg.content), 1)),
                          streaming.value && msg.role === "assistant" && index2 === messages.value.length - 1 ? (vue.openBlock(), vue.createElementBlock("span", _hoisted_14, "\u258D")) : vue.createCommentVNode("", true)
                        ]),
                        msg.role === "assistant" && !streaming.value && extractCode(msg.content) ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_15, [
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--insert",
                            onClick: ($event) => insertCode(extractCode(msg.content))
                          }, [..._cache[9] || (_cache[9] = [
                            vue.createElementVNode("svg", {
                              viewBox: "0 0 16 16",
                              width: "12",
                              height: "12",
                              fill: "none",
                              stroke: "currentColor",
                              "stroke-width": "2",
                              "stroke-linecap": "round"
                            }, [
                              vue.createElementVNode("path", { d: "M8 3v10M3 8h10" })
                            ], -1),
                            vue.createTextVNode(" \u63D2\u5165\u4EE3\u7801 ", -1)
                          ])], 8, _hoisted_16),
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--replace",
                            onClick: ($event) => replaceCode(extractCode(msg.content))
                          }, [..._cache[10] || (_cache[10] = [
                            vue.createElementVNode("svg", {
                              viewBox: "0 0 16 16",
                              width: "12",
                              height: "12",
                              fill: "none",
                              stroke: "currentColor",
                              "stroke-width": "2",
                              "stroke-linecap": "round"
                            }, [
                              vue.createElementVNode("path", { d: "M2 8h12M12 4l4 4-4 4" })
                            ], -1),
                            vue.createTextVNode(" \u66FF\u6362\u4EE3\u7801 ", -1)
                          ])], 8, _hoisted_17),
                          vue.createElementVNode("button", {
                            class: "magic-ai-btn magic-ai-btn--copy",
                            onClick: ($event) => copyCode(extractCode(msg.content))
                          }, [..._cache[11] || (_cache[11] = [
                            vue.createElementVNode("svg", {
                              viewBox: "0 0 16 16",
                              width: "12",
                              height: "12",
                              fill: "none",
                              stroke: "currentColor",
                              "stroke-width": "1.5",
                              "stroke-linecap": "round"
                            }, [
                              vue.createElementVNode("rect", {
                                x: "5",
                                y: "5",
                                width: "9",
                                height: "9",
                                rx: "1.5"
                              }),
                              vue.createElementVNode("path", { d: "M5 11H3.5A1.5 1.5 0 0 1 2 9.5v-7A1.5 1.5 0 0 1 3.5 1h7A1.5 1.5 0 0 1 12 2.5V5" })
                            ], -1),
                            vue.createTextVNode(" \u590D\u5236 ", -1)
                          ])], 8, _hoisted_18)
                        ])) : vue.createCommentVNode("", true)
                      ])
                    ], 2);
                  }), 128)),
                  loading.value && !streaming.value ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_19, [..._cache[12] || (_cache[12] = [
                    vue.createElementVNode("div", { class: "magic-ai-message-avatar" }, [
                      vue.createElementVNode("span", null, [
                        vue.createElementVNode("svg", {
                          viewBox: "0 0 20 20",
                          width: "16",
                          height: "16",
                          fill: "none",
                          stroke: "currentColor",
                          "stroke-width": "1.5",
                          "stroke-linecap": "round",
                          "stroke-linejoin": "round"
                        }, [
                          vue.createElementVNode("path", { d: "M10 3a3.5 3.5 0 0 1 3.5 3.5V8A2.5 2.5 0 0 1 16 10.5V12a2 2 0 0 1-2 2h-.5L12 17H8l-1.5-3H6a2 2 0 0 1-2-2v-1.5A2.5 2.5 0 0 1 6.5 8v-1.5A3.5 3.5 0 0 1 10 3z" }),
                          vue.createElementVNode("circle", {
                            cx: "8",
                            cy: "9",
                            r: "0.8",
                            fill: "currentColor"
                          }),
                          vue.createElementVNode("circle", {
                            cx: "12",
                            cy: "9",
                            r: "0.8",
                            fill: "currentColor"
                          })
                        ])
                      ])
                    ], -1),
                    vue.createElementVNode("div", { class: "magic-ai-message-body" }, [
                      vue.createElementVNode("div", { class: "magic-ai-message-content" }, [
                        vue.createElementVNode("div", { class: "magic-ai-loading" }, [
                          vue.createElementVNode("span"),
                          vue.createElementVNode("span"),
                          vue.createElementVNode("span")
                        ])
                      ])
                    ], -1)
                  ])])) : vue.createCommentVNode("", true)
                ], 64))
              ], 512),
              vue.createElementVNode("div", _hoisted_20, [
                configLoaded.value && selectableProviders.value.length > 0 ? (vue.openBlock(), vue.createElementBlock("div", _hoisted_21, [
                  vue.createElementVNode("div", _hoisted_22, [
                    vue.createElementVNode("select", {
                      class: "magic-ai-select",
                      value: activeProvider.value,
                      onChange: onProviderChange
                    }, [
                      (vue.openBlock(true), vue.createElementBlock(vue.Fragment, null, vue.renderList(selectableProviders.value, (p) => {
                        return vue.openBlock(), vue.createElementBlock("option", {
                          key: p.key,
                          value: p.key
                        }, vue.toDisplayString(p.label), 9, _hoisted_24);
                      }), 128))
                    ], 40, _hoisted_23),
                    _cache[13] || (_cache[13] = vue.createElementVNode("svg", {
                      class: "magic-ai-select-arrow",
                      viewBox: "0 0 12 12",
                      width: "10",
                      height: "10"
                    }, [
                      vue.createElementVNode("path", {
                        d: "M3 5l3 3 3-3",
                        fill: "none",
                        stroke: "currentColor",
                        "stroke-width": "1.5",
                        "stroke-linecap": "round",
                        "stroke-linejoin": "round"
                      })
                    ], -1))
                  ]),
                  vue.createElementVNode("div", _hoisted_25, [
                    vue.createElementVNode("select", {
                      class: "magic-ai-select",
                      value: activeModel.value,
                      onChange: onModelChange
                    }, [
                      (vue.openBlock(true), vue.createElementBlock(vue.Fragment, null, vue.renderList(availableModels.value, (m) => {
                        return vue.openBlock(), vue.createElementBlock("option", {
                          key: m,
                          value: m
                        }, vue.toDisplayString(m), 9, _hoisted_27);
                      }), 128))
                    ], 40, _hoisted_26),
                    _cache[14] || (_cache[14] = vue.createElementVNode("svg", {
                      class: "magic-ai-select-arrow",
                      viewBox: "0 0 12 12",
                      width: "10",
                      height: "10"
                    }, [
                      vue.createElementVNode("path", {
                        d: "M3 5l3 3 3-3",
                        fill: "none",
                        stroke: "currentColor",
                        "stroke-width": "1.5",
                        "stroke-linecap": "round",
                        "stroke-linejoin": "round"
                      })
                    ], -1))
                  ])
                ])) : vue.createCommentVNode("", true),
                vue.createElementVNode("div", _hoisted_28, [
                  vue.createElementVNode("label", _hoisted_29, [
                    vue.withDirectives(vue.createElementVNode("input", {
                      type: "checkbox",
                      "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => useCurrentCode.value = $event)
                    }, null, 512), [
                      [vue.vModelCheckbox, useCurrentCode.value]
                    ]),
                    _cache[15] || (_cache[15] = vue.createElementVNode("span", { class: "magic-ai-toggle-text" }, "\u643A\u5E26\u5F53\u524D\u4EE3\u7801\u4E0A\u4E0B\u6587", -1))
                  ]),
                  vue.createElementVNode("button", {
                    class: "magic-ai-clear-btn",
                    onClick: clearMessages,
                    disabled: messages.value.length === 0
                  }, [..._cache[16] || (_cache[16] = [
                    vue.createElementVNode("svg", {
                      viewBox: "0 0 16 16",
                      width: "12",
                      height: "12",
                      fill: "none",
                      stroke: "currentColor",
                      "stroke-width": "1.5",
                      "stroke-linecap": "round"
                    }, [
                      vue.createElementVNode("path", { d: "M2 4h12M5 4V3a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v1M6 7v5M10 7v5M4 4l1 10h6l1-10" })
                    ], -1),
                    vue.createTextVNode(" \u6E05\u7A7A ", -1)
                  ])], 8, _hoisted_30)
                ]),
                vue.createElementVNode("div", _hoisted_31, [
                  vue.withDirectives(vue.createElementVNode("textarea", {
                    ref_key: "inputRef",
                    ref: inputRef,
                    "onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => inputText.value = $event),
                    class: "magic-ai-textarea",
                    placeholder: "\u63CF\u8FF0\u4F60\u9700\u8981\u5B9E\u73B0\u7684\u529F\u80FD... Enter \u53D1\u9001\uFF0CShift+Enter \u6362\u884C",
                    onKeydown,
                    disabled: loading.value,
                    rows: "3"
                  }, null, 40, _hoisted_32), [
                    [vue.vModelText, inputText.value]
                  ]),
                  vue.createElementVNode("button", {
                    class: "magic-ai-send-btn",
                    onClick: sendMessage,
                    disabled: loading.value || !inputText.value.trim()
                  }, [..._cache[17] || (_cache[17] = [
                    vue.createElementVNode("svg", {
                      viewBox: "0 0 20 20",
                      width: "18",
                      height: "18",
                      fill: "none",
                      stroke: "currentColor",
                      "stroke-width": "2",
                      "stroke-linecap": "round",
                      "stroke-linejoin": "round"
                    }, [
                      vue.createElementVNode("path", { d: "M4 10l6-6 6 6M10 4v12" })
                    ], -1)
                  ])], 8, _hoisted_33)
                ])
              ])
            ])
          ])) : vue.createCommentVNode("", true)
        ]);
      };
    }
  };
  var MagicAiDialog = /* @__PURE__ */ _export_sfc(_sfc_main, [["__scopeId", "data-v-4bd039a3"]]);
  if (typeof window !== "undefined") {
    let loadSvg = function() {
      var body = document.body;
      var svgDom = document.getElementById("__svg__icons__dom__1776134135094__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1776134135094__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol viewBox="0 0 24 24" fill="none"  id="magic-ai-ai"><defs><linearGradient id="magic-ai-ai_a" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#83D3E7" /><stop offset="50%" stop-color="#6950BE" /><stop offset="100%" stop-color="#C53B77" /></linearGradient></defs><path d="M512 128c-211.68 0-384 164.32-384 368 0 123.36 62.88 233.44 159.84 297.28l31.52-36C239.04 702.56 192 606.24 192 496c0-168.48 139.04-304 320-304s320 135.52 320 304c0 110.24-47.04 206.56-127.36 261.28l31.52 36A355.565 355.565 0 0 0 896 496c0-203.68-172.32-368-384-368Zm0 128c-145.76 0-256 106.08-256 240 0 90.4 43.68 170.08 114.4 219.36l31.52-36C348.64 640.16 320 570.72 320 496c0-98.08 78.24-176 192-176s192 77.92 192 176c0 74.72-28.64 144.16-81.92 183.36l31.52 36C724.32 666.08 768 586.4 768 496c0-133.92-110.24-240-256-240Zm0 128c-79.36 0-128 52.64-128 112 0 43.36 19.36 84.32 58.24 109.28l31.52-36C446.56 551.2 448 523.04 448 496c0-26.72 25.76-48 64-48 38.24 0 64 21.28 64 48 0 27.04 1.44 55.2-25.76 73.28l31.52 36C620.64 580.32 640 539.36 640 496c0-59.36-48.64-112-128-112Z" fill="url(#magic-ai-ai_a)" /></symbol>';
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
