<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import AutoStart from '@/components/AutoStart.vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import { PATHS } from '@/Redirects';
  import { cancel, polling } from '@/Service';
  import type { ApiResponseStatus } from '@/types';

  const qrImage = ref('');
  const token = ref('');
  const messageCode = ref('bankid.msg.rfa13');
  const responseStatus = ref<ApiResponseStatus>();
  const hideAutoStart = ref(false);

  const showContinueErrorButton = computed(() => responseStatus.value === 'ERROR');

  const cancelRequest = async () => {
    await cancel();
    window.location.href = PATHS.CANCEL;
  };

  const acceptError = async () => {
    await cancel();
    window.location.href = PATHS.ERROR;
  };

  const startPolling = () => {
    polling(false, qrImage, hideAutoStart, token, messageCode, responseStatus);
  };

  onMounted(() => {
    startPolling();
  });

  const retry = () => {
    cancel().then(() => {
      startPolling();
    });
  };
</script>

<template>
  <div class="content-container">
    <CustomContent position="autostart" />
    <p>{{ $t(messageCode) }}</p>
    <AutoStart v-if="!showContinueErrorButton && !hideAutoStart" :autoStartToken="token" />
    <div class="buttons" v-if="showContinueErrorButton">
      <button class="btn-default" @click="acceptError">
        <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
      </button>
      <button class="btn-default" @click="retry">
        <span>{{ $t('bankid.msg.btn-retry') }}</span>
      </button>
    </div>
    <BankIdLogo />
  </div>
  <div class="return">
    <button
      v-if="!showContinueErrorButton"
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

<style scoped>
  .buttons {
    display: flex;
    gap: 1em;
  }
  .buttons .btn-default {
    flex: 1;
  }
</style>
