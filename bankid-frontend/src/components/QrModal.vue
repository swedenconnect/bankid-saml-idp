<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { PATHS } from '@/Redirects';
  import { cancel, polling } from '@/Service';
  import type { ApiResponseStatus, UiInformation } from '@/types';
  import CustomContent from './CustomContent.vue';
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
  const showContinueErrorButton = computed(() => responseStatus.value === 'ERROR');

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
  <p v-if="!showQrInstructions">{{ $t(messageCode) }}</p>
  <QrInstructions v-else />
  <div class="buttons" v-if="showContinueErrorButton">
    <button class="btn-default" @click="acceptError">
      <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
    </button>
    <button class="btn-default" @click="retry">
      <span>{{ $t('bankid.msg.btn-retry') }}</span>
    </button>
  </div>
  <button v-else class="btn-default" @click="closeDialog">{{ $t('bankid.msg.qr.close') }}</button>
</template>

<style scoped>
  .buttons {
    display: flex;
    gap: 1em;
  }
  .buttons .btn-default {
    flex: 1;
  }
</style>
