<template>
    <div class="magic-ai-panel-sidebar">
        <div class="magic-ai-panel-header">
            <ul>
                <li :title="$i('ai.open')" @click="openDialog">
                    <magic-icon icon="ai"/>
                </li>
            </ul>
        </div>
        <div class="magic-ai-panel-content">
            <div class="magic-ai-welcome-message">
                <div class="magic-ai-icon">✨</div>
                <p>{{ $i('ai.welcomeDesc') }}</p>
                <button class="magic-ai-open-btn" @click="openDialog">
                    {{ $i('ai.open') }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    info: Object
})

const openDialog = () => {
    window.magicEditorBus.$emit('ai-dialog-open')
}
</script>

<style scoped>
.magic-ai-panel-sidebar {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: var(--background);
}

.magic-ai-panel-header {
    padding: 8px;
    border-bottom: 1px solid var(--border-color);
}

.magic-ai-panel-header ul {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    gap: 4px;
}

.magic-ai-panel-header li {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-radius: 4px;
    transition: background 0.2s;
}

.magic-ai-panel-header li:hover {
    background: var(--hover-background);
}

.magic-ai-panel-content {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.magic-ai-welcome-message {
    text-align: center;
}

.magic-ai-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.magic-ai-welcome-message p {
    color: var(--color);
    margin: 0 0 20px 0;
    line-height: 1.6;
    font-size: 13px;
}

.magic-ai-open-btn {
    padding: 8px 24px;
    background: var(--primary-color);
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    transition: opacity 0.2s;
}

.magic-ai-open-btn:hover {
    opacity: 0.9;
}
</style>
