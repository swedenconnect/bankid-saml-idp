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
    background: beige;
    border: 1px solid black;
    padding: 1em;
    margin-bottom: 1em;
  }
</style>
