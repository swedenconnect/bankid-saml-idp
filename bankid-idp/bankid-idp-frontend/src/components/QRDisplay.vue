<script setup>
  const props = defineProps({
    image: String,
  });
</script>

<template>
  <div v-if="props.image">
    <hr class="full-width" />
    <center>
      <!-- TODO center with css -->
      <img width="165" height="165" :src="props.image" alt="QR Code" />
    </center>
  </div>
</template>

<style scoped>
  img {
    margin-top: 25px;
  }
</style>
