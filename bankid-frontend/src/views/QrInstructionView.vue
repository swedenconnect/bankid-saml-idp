<script setup lang="ts">
  import { ref, watchEffect } from 'vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import QrInstructions from '@/components/QrInstructions.vue';
  import QrModal from '@/components/QrModal.vue';
  import { PATHS } from '@/Redirects';
  import router from '@/router';
  import type { SelectedDeviceInformation, UiInformation } from '@/types';

  const showQrModal = ref(false);
  const skipQrInfo = ref<boolean>(localStorage.getItem('skipQrInfo') === 'true');

  const props = defineProps<{
    uiInfo?: UiInformation;
    deviceData?: SelectedDeviceInformation;
  }>();

  watchEffect(() => {
    if (skipQrInfo.value) {
      localStorage.setItem('skipQrInfo', 'true');
    } else {
      localStorage.removeItem('skipQrInfo');
    }
  });

  const toggleQrModal = () => {
    showQrModal.value = !showQrModal.value;
  };

  const openThisDevice = () => router.push({ name: 'auto' });

  const cancelSelection = () => {
    window.location.href = PATHS.CANCEL;
  };
</script>

<template>
  <div class="content-container">
    <CustomContent position="qrcode" />
    <h2>{{ $t('bankid.msg.qr.title') }}</h2>
    <p>
      {{ $t('bankid.msg.qr.time-info', { minutes: $t('bankid.msg.qr.minutes', { n: uiInfo?.qrDisplayInMinutes }) }) }}
    </p>
    <QrInstructions />
    <p>{{ $t('bankid.msg.qr.time-retry') }}</p>
    <div class="buttons">
      <button @click="toggleQrModal" class="btn-default">{{ $t('bankid.msg.qr.show-qr') }}</button>
      <label><input type="checkbox" v-model="skipQrInfo" />{{ $t('bankid.msg.qr.checkbox') }}</label>
      <button @click="openThisDevice" class="btn-link">{{ $t('bankid.msg.qr.this-device') }}</button>
    </div>
    <BankIdLogo />
  </div>

  <div class="return">
    <button @click="cancelSelection" class="btn-link" type="submit" name="action" value="cancel">
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>

  <QrModal v-if="showQrModal" :ui-info="props.uiInfo" @close="toggleQrModal" />
</template>

<style scoped>
  .buttons {
    display: flex;
    align-items: center;
    flex-direction: column;
    margin-top: 2em;
  }
  .buttons button {
    font-size: 1em;
    width: fit-content;
  }
  .buttons label {
    font-size: 0.8em;
    display: flex;
    align-items: center;
  }
</style>
