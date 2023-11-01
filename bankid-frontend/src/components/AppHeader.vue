<script setup lang="ts">
  import { computed } from 'vue';
  import type { SpInformation } from '@/types';

  const props = defineProps<{
    spInfo?: SpInformation;
  }>();

  const getImage = computed(() => props.spInfo?.imageUrl ?? '');
</script>

<template>
  <header>
    <div class="top-logo-container main-width">
      <img v-if="getImage" class="top-logo" :src="getImage" alt="Logo" />
    </div>
  </header>
</template>

<style scoped>
  header {
    background: var(--header-bg-color);
    box-shadow: var(--header-shadow);
  }

  .top-logo-container {
    --logo-height: 60px;
    box-sizing: content-box;
    padding: 20px;
    height: var(--logo-height);
  }

  @media (max-width: 576px) {
    .top-logo-container {
      --logo-height: 40px;
    }
  }
  .top-logo {
    height: 100%;
  }
</style>
