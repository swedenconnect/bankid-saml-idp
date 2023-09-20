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
      <span v-for="(item, index) in matchingContent.content">
        <a v-if="item.link" :href=item.link >{{ $t(item.text) }}</a>
        <p v-else>{{ $t(item.text) }}</p>
      </span>
    <div> <!-- Empty --> </div>
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
