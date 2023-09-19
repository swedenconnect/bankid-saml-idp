<script setup lang="ts">
  import { inject } from 'vue';

  type Position = 'above' | 'below' | 'deviceselect' | 'qrcode' | 'autostart';

  const props = defineProps<{
    position: Position;
  }>();

  const customContent = inject('customContent') as Array<any>;
  const matchingContent = customContent.find((content) => content.position === props.position.toUpperCase());
</script>

<template>
  <div v-if="matchingContent" :class="matchingContent.type.toLowerCase()">
    <h3>{{ $t(matchingContent.title) }}</h3>
    <p>{{ $t(matchingContent.text) }}</p>
  </div>
</template>

<style scoped>
  div {
    padding: 0 1em;
    margin: 1em 0;
  }
  div.info {
    background-color: var(--info-bg-color);
    color: var(--info-fg-color);
    border: 1px solid var(--info-border-color);
  }
  div.warning {
    background-color: var(--warning-bg-color);
    color: var(--warning-fg-color);
    border: 1px solid var(--warning-border-color);
  }
  h3 {
    margin-bottom: 0;
  }
</style>
