<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import DeviceSelect from '@/components/DeviceSelect.vue';
  import StatusMessage from '@/components/StatusMessage.vue';
  import { PATHS } from '@/Redirects';
  import { status } from '@/Service';

  const displayServiceMessage = ref(false);

  const cancelSelection = () => {
    window.location.href = PATHS.CANCEL;
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
    <p>{{ $t('bankid.msg.rfa20') }}</p>
    <DeviceSelect />
    <BankIdLogo />
  </div>

  <div class="return">
    <button @click="cancelSelection" class="btn-link" type="submit" name="action" value="cancel">
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>
</template>
