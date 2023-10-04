<script setup lang="ts">
  import { ref, onMounted, onUnmounted, watch } from 'vue';

  const props = defineProps<{
    image: string;
    size?: string;
    dialog?: HTMLElement;
  }>();

  const qrImage = ref<HTMLElement>();
  const isImageInViewport = ref(true);

  const checkImageVisibility = (root: HTMLElement, element: HTMLElement) => {
    const observer = new IntersectionObserver(
      (entries, observer) => {
        entries.forEach((entry) => {
          isImageInViewport.value = entry.isIntersecting;
          if (entry.isIntersecting) {
            observer.disconnect();
          }
        });
      },
      { root, threshold: 0.85 },
    );
    observer.observe(element);
  };

  const handleEvent = () => {
    if (props.dialog && qrImage.value) checkImageVisibility(props.dialog, qrImage.value);
  };

  watch(
    () => props.dialog,
    (newVal, oldVal) => {
      if (newVal instanceof HTMLElement) {
        newVal.addEventListener('scroll', handleEvent);
        newVal.addEventListener('focus', handleEvent, true);
        handleEvent();
      }

      if (oldVal instanceof HTMLElement) {
        oldVal.removeEventListener('scroll', handleEvent);
        oldVal.removeEventListener('focus', handleEvent, true);
      }
    },
    { immediate: true },
  );

  onMounted(() => {
    window.addEventListener('resize', handleEvent);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleEvent);
  });
</script>

<template>
  <div v-if="props.image" class="qr-code">
    <div class="corner-frame">
      <img
        ref="qrImage"
        :width="props.size || 200"
        :src="props.image"
        :alt="$t('bankid.msg.qr.qr-code')"
        tabindex="0"
      />
    </div>
  </div>
  <p aria-live="assertive" v-if="props.image && !isImageInViewport">{{ $t('bankid.msg.qr.not-visible') }}</p>
</template>

<style scoped>
  .qr-code {
    --qr-margin: 20px;
    text-align: center;
  }
  img {
    max-width: calc(100vmin - 14px);
    max-height: calc(100vmin - 14px);
    min-width: min(100px, 100vmin);
    min-height: min(100px, 100vmin);
    margin: -8px;
  }
  @media (min-width: 200px) and (min-height: 200px) {
    img {
      border: 2px solid black;
      max-width: 60vmin;
      max-height: 60vmin;
      margin: 0;
    }
    .qr-code {
      padding: var(--qr-margin) 0;
    }
  }
  @media (max-width: 200px) or (max-height: 575px) {
    .qr-code {
      padding: 0 0 var(--qr-margin);
    }
  }

  @media (min-width: 300px) and (min-height: 300px) {
    .corner-frame {
      --size: 50px;
      --width: 10px;
      --padding: 10px;
      --color: var(--qr-corner-color);

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
  }
</style>
