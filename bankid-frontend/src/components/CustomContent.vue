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
  <aside v-if="matchingContent" :class="matchingContent.type.toLowerCase()">
    <h3>{{ $t(matchingContent.title) }}</h3>
    <p v-for="item in matchingContent.content" :key="item.text">
      <a v-if="item.link" :href="item.link">{{ $t(item.text) }}</a>
      <span v-else>{{ $t(item.text) }}</span>
    </p>
  </aside>
</template>

<style scoped>
  aside {
    padding: 0 1em;
    margin: 1em 0;
  }
  aside.info {
    background-color: var(--info-bg-color);
    color: var(--info-fg-color);
    border: 1px solid var(--info-border-color);
  }
  aside.warning {
    background-color: var(--warning-bg-color);
    color: var(--warning-fg-color);
    border: 1px solid var(--warning-border-color);
  }
  h3 {
    margin-bottom: 0;
  }
  a {
    font-size: 1em;
  }
</style>
