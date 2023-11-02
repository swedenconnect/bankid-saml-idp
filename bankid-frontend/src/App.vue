<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
  import {handleApiError, isUserErrorResponse, selectedDevice, uiInformation} from '@/Service';
  import type { SelectedDeviceInformation, UiInformation } from '@/types';

  const uiInfo = ref<UiInformation>();
  const device = ref<SelectedDeviceInformation>();

  const logo = computed(() => uiInfo.value?.sp?.imageUrl);

  onBeforeMount(async () => {
    var information = await uiInformation()
    if (isUserErrorResponse(information)) {
      handleApiError(information);
    } else {
      uiInfo.value = information;
    }
    device.value = await selectedDevice();
  });
</script>

<template>
  <AppHeader v-if="logo" :logo="logo" />
  <LocaleChanger />
  <main class="main-width">
    <CustomContent position="above" />
    <RouterView :ui-info="uiInfo" :deviceData="device" />
    <CustomContent position="below" />
  </main>
  <AppFooter :accessibility-link="uiInfo?.accessibilityReportLink ?? ''" :provider-name="uiInfo?.providerName" />
</template>
