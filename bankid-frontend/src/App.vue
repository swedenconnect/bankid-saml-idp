<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
  import { selectedDevice, spInformation } from '@/Service';
  import type { SpInformation, SelectedDeviceInformation } from '@/types';

  const spInfo = ref<SpInformation | null>(null);
  const device = ref<SelectedDeviceInformation | null>(null);

  onMounted(async () => {
    spInfo.value = await spInformation();
    device.value = await selectedDevice();
  });
</script>

<template>
  <AppHeader :sp-info="spInfo" />
  <LocaleChanger />
  <main class="main-width">
    <CustomContent position="above" />
    <RouterView :sp-info="spInfo" :deviceData="device" />
    <CustomContent position="below" />
  </main>
  <AppFooter />
</template>
