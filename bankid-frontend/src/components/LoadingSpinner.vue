<script setup lang="ts">
  const props = defineProps<{
    containerSize?: string;
  }>();
</script>

<template>
  <div :style="{ width: props.containerSize, height: props.containerSize }" class="spinner-container">
    <div class="spinner"></div>
  </div>
</template>

<style scoped>
  .spinner-container {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .spinner {
    width: 5em;
    aspect-ratio: 1 / 1;
    margin: 1em auto;
    border: 0.75em solid var(--loading-bg-color);
    border-right-color: var(--loading-fg-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
</style>
