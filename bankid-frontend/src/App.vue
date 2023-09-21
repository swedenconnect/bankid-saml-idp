<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
  import { spInformation } from '@/Service';
  import type { SpInformation } from '@/types';

  const spInfo = ref<SpInformation | null>(null);

  onMounted(async () => {
    spInfo.value = await spInformation();
  });
</script>

<template>
  <AppHeader :sp-info="spInfo" />
  <LocaleChanger />
  <main class="main-width">
    <CustomContent position="above" />
    <RouterView />
    <CustomContent position="below" />
  </main>
  <AppFooter />
</template>
