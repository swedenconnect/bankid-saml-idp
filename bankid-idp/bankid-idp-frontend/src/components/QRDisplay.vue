<script setup>
  const props = defineProps({
    image: String,
  });
</script>

<template>
  <div v-if="props.image" class="qr-code">
    <hr class="full-width" />
    <img width="165" height="165" :src="props.image" alt="QR Code" />
  </div>
</template>

<style scoped>
  .qr-code {
    text-align: center;
  }
  img {
    margin-top: 25px;
  }
</style>
