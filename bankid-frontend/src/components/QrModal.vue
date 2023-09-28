<script setup lang="ts">
  import QrDisplay from './QrDisplay.vue';
  import QrInstructions from './QrInstructions.vue';

  const props = defineProps<{
    image: string;
    size: string;
  }>();
</script>

<template>
  <QrDisplay :image="props.image" :size="props.size" />
  <QrInstructions></QrInstructions>
  <form action="dialog">
    <button class="btn-default">Stäng</button>
  </form>
</template>

<style scoped>
  form {
    text-align: center;
  }
</style>
