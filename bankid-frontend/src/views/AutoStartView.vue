<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import AutoStart from '@/components/AutoStart.vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import ErrorButtons from '@/components/ErrorButtons.vue';
  import { PATHS } from '@/Redirects';
  import { cancel, pollingAutoStart } from '@/Service';
  import type { ApiResponseStatus } from '@/types';
  import router from "@/router";

  const token = ref('');
  const messageCode = ref('bankid.msg.rfa13');
  const responseStatus = ref<ApiResponseStatus>();
  const hideAutoStart = ref(false);

  const showErrorButtons = computed(() => responseStatus.value === 'ERROR');

  const cancelRequest = async () => {
    await cancel();
    window.location.href = PATHS.CANCEL;
  };

  const acceptError = async () => {
    await cancel();
    router.push({ name: "error"});
  };

  const startPolling = () => {
    pollingAutoStart(hideAutoStart, token, messageCode, responseStatus);
  };

  onMounted(() => {
    startPolling();
  });

  const retry = () => {
    cancel().then(() => {
      hideAutoStart.value = false;
      responseStatus.value = "NOT_STARTED";
      token.value = "";
      startPolling();
    });
  };
</script>

<template>
  <div class="content-container">
    <CustomContent position="autostart" />
    <p>{{ $t(messageCode) }}</p>
    <AutoStart v-if="!showErrorButtons && !hideAutoStart" :autoStartToken="token" />
    <ErrorButtons v-if="showErrorButtons" @acceptError="acceptError" @retry="retry" />
    <BankIdLogo />
  </div>
  <div class="return">
    <button v-if="!showErrorButtons" @click="cancelRequest" class="btn-link" type="submit" name="action" value="cancel">
      <span>{{ $t('bankid.msg.btn-cancel') }}</span>
    </button>
  </div>
</template>
