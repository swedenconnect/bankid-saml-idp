<script setup lang="ts">
  const props = defineProps<{
    message: string;
  }>();
</script>

<template>
  <div class="status">
    <span>{{ $t(props.message) }}</span>
  </div>
</template>

<style scoped>
  .status {
    background: var(--warning-bg-color);
    border: 1px solid var(--warning-border-color);
    color: var(--warning-fg-color);
    padding: 1em;
    margin-bottom: 1em;
  }
</style>
