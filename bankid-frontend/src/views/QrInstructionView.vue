<script setup lang="ts">
  import { ref } from 'vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import QrInstructions from '@/components/QrInstructions.vue';
  import QrModal from '@/components/QrModal.vue';
  import { PATHS } from '@/Redirects';
  import router from '@/router';
  import type { SelectedDeviceInformation, UiInformation } from '@/types';

  const qrDialog = ref<HTMLDialogElement>();
  const isQrDialogOpen = ref(false);

  const props = defineProps<{
    uiInfo?: UiInformation;
    deviceData?: SelectedDeviceInformation;
  }>();

  const openQr = () => {
    isQrDialogOpen.value = true;
    qrDialog.value?.showModal();
  };
  const closeQr = () => {
    isQrDialogOpen.value = false;
    qrDialog.value?.close();
  };

  const openThisDevice = () => router.push({ name: 'auto' });

  const cancelSelection = () => {
    window.location.href = PATHS.CANCEL;
  };
</script>

<template>
  <div class="content-container">
    <CustomContent position="qrcode" />
    <h2>BankID med QR-kod</h2>
    <p>
      Efter att du tryckt på knappen nedan för att visa QR-koden har du X minuter på dig att följa dessa instruktioner:
    </p>
    <QrInstructions />
    <p>Skulle tiden ta slut kan du prova igen.</p>
    <div class="buttons">
      <button @click="openQr" class="btn-default">Visa QR-kod</button>
      <button @click="openThisDevice" class="btn-link">Öppna BankID på den här enheten</button>
    </div>
    <BankIdLogo />
  </div>

  <div class="return">
    <button @click="cancelSelection" class="btn-link" type="submit" name="action" value="cancel">
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>

  <dialog ref="qrDialog">
    <QrModal v-if="isQrDialogOpen" :ui-info="props.uiInfo" @close="closeQr" />
  </dialog>
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
</style>
