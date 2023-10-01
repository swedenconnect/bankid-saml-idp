<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import DeviceSelect from '@/components/DeviceSelect.vue';
  import StatusMessage from '@/components/StatusMessage.vue';
  import { PATHS } from '@/Redirects';
  import { status } from '@/Service';
  import type { SelectedDeviceInformation, UiInformation } from '@/types';

  const displayServiceMessage = ref(false);

  const props = defineProps<{
    uiInfo?: UiInformation;
    deviceData?: SelectedDeviceInformation;
  }>();

  const cancelSelection = () => {
    window.location.href = PATHS.CANCEL;
  };

  const { locale } = useI18n();

  const getSpName = () => {
    return props.uiInfo?.sp.displayNames[locale.value] || '';
  };

  const getSpMessage = () => {
    if (props.deviceData ? props.deviceData.isSign : false) {
      return 'bankid.msg.rp-sign';
    }
    return 'bankid.msg.rp-auth';
  };

  const showSpMessage = () => {
    return props.uiInfo?.sp.showSpMessage;
  };

  onBeforeMount(() => {
    status().then((s) => {
      if (s['status'] !== 'OK') {
        displayServiceMessage.value = true;
      }
    });
  });
</script>

<template>
  <div class="content-container">
    <CustomContent position="deviceselect" />
    <StatusMessage message="bankid.msg.error.service" v-if="displayServiceMessage" />
    <h2>BankID</h2>
    <p v-if="showSpMessage()">{{ getSpName() + ' ' + $t(getSpMessage()) }}</p>
    <p>{{ $t('bankid.msg.rfa20') }}</p>
    <DeviceSelect :deviceData="deviceData" />
    <BankIdLogo />
  </div>

  <div class="return">
    <button @click="cancelSelection" class="btn-link" type="submit" name="action" value="cancel">
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>
</template>
