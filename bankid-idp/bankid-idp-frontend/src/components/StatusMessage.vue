<script setup lang="ts">
  const props = defineProps<{
    message: string;
  }>();
</script>

<template>
  <div class="container-fluid status">
    <p>{{ $t(props.message) }}</p>
  </div>
</template>
