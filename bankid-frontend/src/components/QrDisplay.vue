<script setup lang="ts">
  const props = defineProps<{
    image: string;
    size?: string;
  }>();
</script>

<template>
  <div v-if="props.image" class="qr-code">
    <div class="corner-frame">
      <img :width="props.size || 200" :src="props.image" alt="QR Code" />
    </div>
  </div>
</template>

<style scoped>
  .qr-code {
    padding: 20px 0;
    text-align: center;
  }
  .corner-frame {
    --size: 50px;
    --width: 10px;
    --padding: 10px;
    --color: #2537ff;

    display: inline-block;
    padding: calc(var(--padding) + var(--width));
    border-radius: 20px;
    outline: var(--width) solid var(--color);
    outline-offset: calc(-1 * var(--width));
    mask:
      conic-gradient(at var(--size) var(--size), #0000 75%, #000 0) 0 0 / calc(100% - var(--size))
        calc(100% - var(--size)),
      linear-gradient(#000 0 0) content-box;
    -webkit-mask:
      conic-gradient(at var(--size) var(--size), #0000 75%, #000 0) 0 0 / calc(100% - var(--size))
        calc(100% - var(--size)),
      linear-gradient(#000 0 0) content-box;
  }
  img {
    border: 2px solid black;
    max-width: 60vw;
    max-height: 60vw;
  }
</style>
