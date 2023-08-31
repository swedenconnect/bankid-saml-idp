<script setup>
  const props = defineProps({
    message: String,
  });
</script>

<template>
  <div class="container-fluid status">
    <p>{{ $t(props.message) }}</p>
  </div>
</template>
