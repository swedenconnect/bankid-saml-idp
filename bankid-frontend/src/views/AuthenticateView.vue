<script setup lang="ts">
  import { computed, onBeforeMount, onMounted, ref } from 'vue';
  import AutoStart from '@/components/AutoStart.vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import QrDisplay from '@/components/QrDisplay.vue';
  import QrInstructions from '@/components/QrInstructions.vue';
  import { PATHS } from '@/Redirects';
  import { cancel, polling } from '@/Service';
  import type { ApiResponseStatus, UiInformation } from '@/types';

  const qrImage = ref('');
  const token = ref('');
  const messageCode = ref('');
  const responseStatus = ref<ApiResponseStatus | null>(null);

  const props = defineProps<{
    uiInfo: UiInformation | null;
    otherDevice: boolean;
  }>();

  const showQrInstructions = computed(
    () => messageCode.value === 'bankid.msg.ext2' && props.uiInfo && props.uiInfo.displayQrHelp,
  );

  const cancelRequest = async () => {
    await cancel();
    window.location.href = PATHS.CANCEL;
  };

  const acceptError = async () => {
    await cancel();
    window.location.href = PATHS.ERROR;
  };

  const showContinueErrorButton = () => {
    return responseStatus.value === 'ERROR';
  };

  onBeforeMount(() => {
    if (!props.otherDevice) {
      messageCode.value = 'bankid.msg.rfa13';
    }
  });

  onMounted(() => {
    polling(props.otherDevice, qrImage, token, messageCode, responseStatus);
  });
</script>

<template>
  <div class="content-container">
    <CustomContent v-if="qrImage" position="qrcode" />
    <CustomContent v-else position="autostart" />
    <p v-if="!showQrInstructions">{{ $t(messageCode) }}</p>
    <QrInstructions v-else />
    <AutoStart v-if="!otherDevice && !showContinueErrorButton()" :autoStartToken="token" />
    <QrDisplay :image="qrImage" :size="uiInfo ? uiInfo.qrSize : '200'" />
    <button class="btn-default" v-if="showContinueErrorButton()" @click="acceptError">
      <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
    </button>
    <BankIdLogo />
  </div>
  <div class="return">
    <button
      v-if="!showContinueErrorButton()"
      @click="cancelRequest"
      class="btn-link"
      type="submit"
      name="action"
      value="cancel"
    >
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>
</template>
