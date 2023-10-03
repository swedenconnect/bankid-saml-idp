<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { PATHS } from '@/Redirects';
  import { cancel, pollingQr } from '@/Service';
  import type { ApiResponseStatus, UiInformation } from '@/types';
  import CustomContent from './CustomContent.vue';
  import ErrorButtons from './ErrorButtons.vue';
  import QrDisplay from './QrDisplay.vue';
  import QrInstructions from './QrInstructions.vue';
  import LoadingSpinner from './LoadingSpinner.vue';

  const props = defineProps<{
    uiInfo?: UiInformation;
  }>();
  const emit = defineEmits(['close']);

  const qrDialog = ref<HTMLDialogElement>();
  const qrImage = ref('');
  const messageCode = ref('');
  const responseStatus = ref<ApiResponseStatus>();
  const cancelRetry = ref(false);

  const showQrInstructions = computed(() => messageCode.value === 'bankid.msg.ext2');
  const showErrorButtons = computed(() => responseStatus.value === 'ERROR');
  const containerSize = computed(() => (props.uiInfo ? (parseInt(props.uiInfo.qrSize) * 2).toString() + 'px' : ''));
  const qrSizeWithPx = computed(() => (props.uiInfo ? props.uiInfo.qrSize + 'px' : '0px'));
  const modalMaxWidth = computed(() => `max(${qrSizeWithPx.value}, 700px)`);

  const closeDialog = () => {
    qrDialog.value?.close();
  };
  const closeModal = () => {
    emit('close');
    cancelRetry.value = true;
  };

  const startPolling = () => {
    cancelRetry.value = false;
    pollingQr(qrImage, messageCode, responseStatus, cancelRetry);
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
    qrDialog.value?.showModal();
  });
</script>

<template>
  <dialog ref="qrDialog" @close="closeModal" :style="{ maxWidth: modalMaxWidth }">
    <LoadingSpinner v-if="!messageCode" :container-size="containerSize" />
    <QrDisplay :image="qrImage" :size="props.uiInfo?.qrSize" />
    <CustomContent position="qrcode" />
    <QrInstructions v-if="showQrInstructions" />
    <p v-else>{{ $t(messageCode) }}</p>
    <ErrorButtons v-if="showErrorButtons" @acceptError="acceptError" @retry="retry" />
    <button v-else class="btn-default" @click="closeDialog">{{ $t('bankid.msg.qr.close') }}</button>
  </dialog>
</template>
