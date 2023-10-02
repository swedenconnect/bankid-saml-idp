<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { PATHS } from '@/Redirects';
  import { cancel, polling } from '@/Service';
  import type { ApiResponseStatus, UiInformation } from '@/types';
  import CustomContent from './CustomContent.vue';
  import ErrorButtons from './ErrorButtons.vue';
  import QrDisplay from './QrDisplay.vue';
  import QrInstructions from './QrInstructions.vue';

  const props = defineProps<{
    uiInfo?: UiInformation;
  }>();
  const emit = defineEmits(['close']);

  const qrImage = ref('');
  const hideAutoStart = ref(false); // Enbart för auto
  const token = ref(''); // Enbart för auto
  const messageCode = ref('');
  const responseStatus = ref<ApiResponseStatus>();
  const cancelRetry = ref(false);

  const showQrInstructions = computed(() => messageCode.value === 'bankid.msg.ext2');
  const showErrorButtons = computed(() => responseStatus.value === 'ERROR');

  const closeDialog = () => {
    emit('close');
    cancelRetry.value = true;
  };

  const startPolling = () => {
    cancelRetry.value = false;
    polling(true, qrImage, hideAutoStart, token, messageCode, responseStatus, cancelRetry);
  };

  const acceptError = async () => {
    await cancel();
    window.location.href = PATHS.ERROR;
  };

  const retry = () => {
    cancel().then(() => {
      startPolling();
    });
  };

  onMounted(() => {
    startPolling();
  });
</script>

<template>
  <QrDisplay :image="qrImage" :size="props.uiInfo?.qrSize" />
  <CustomContent position="qrcode" />
  <QrInstructions v-if="showQrInstructions" />
  <p v-else>{{ $t(messageCode) }}</p>
  <ErrorButtons v-if="showErrorButtons" @acceptError="acceptError" @retry="retry" />
  <button v-else class="btn-default" @click="closeDialog">{{ $t('bankid.msg.qr.close') }}</button>
</template>
