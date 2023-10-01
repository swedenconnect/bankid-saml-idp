<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
  import { selectedDevice, uiInformation } from '@/Service';
  import type { SelectedDeviceInformation, UiInformation } from '@/types';

  const uiInfo = ref<UiInformation>();
  const device = ref<SelectedDeviceInformation>();

  onBeforeMount(async () => {
    uiInfo.value = await uiInformation();
    device.value = await selectedDevice();
  });
</script>

<template>
  <AppHeader :sp-info="uiInfo?.sp" />
  <LocaleChanger />
  <main class="main-width">
    <CustomContent position="above" />
    <RouterView :ui-info="uiInfo" :deviceData="device" />
    <CustomContent position="below" />
  </main>
  <AppFooter :accessibility-link="uiInfo?.accessibilityReportLink ?? ''" :provider-name="uiInfo?.providerName" />
</template>
