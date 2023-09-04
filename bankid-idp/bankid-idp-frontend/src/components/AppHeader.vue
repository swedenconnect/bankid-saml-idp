<script setup lang="ts">
  import { computed } from 'vue';
  import type { SpInformation } from '@/types';

  interface Props {
    spInfo: SpInformation | null;
  }

  const props = withDefaults(defineProps<Props>(), {
    spInfo: null,
  });

  const getImage = computed(() => (props.spInfo ? props.spInfo.imageUrl : ''));
</script>

<template>
  <div class="container-fluid header">
    <div class="row">
      <div class="column">
        <img v-if="getImage" class="top-logo-dim" :src="getImage" alt="Logo" />
        <img v-else class="top-logo-dim" src="@/assets/logo.svg" alt="Logo" />
      </div>
      <div class="column">
        <img class="top-logo-dim" src="@/assets/BankID_logo.svg" alt="Logo" />
      </div>
    </div>
  </div>
</template>

<style scoped>
  .header {
    text-align: center;
  }
</style>
