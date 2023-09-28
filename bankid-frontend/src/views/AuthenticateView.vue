<script setup lang="ts">
  import { computed, onBeforeMount, onMounted, ref } from 'vue';
  import AutoStart from '@/components/AutoStart.vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import CustomContent from '@/components/CustomContent.vue';
  import QrDisplay from '@/components/QrDisplay.vue';
  import QrInstructions from '@/components/QrInstructions.vue';
  import { PATHS } from '@/Redirects';
  import { cancel, poll } from '@/Service';
  import type { ApiResponse, ApiResponseStatus, RetryResponse, UiInformation } from '@/types';

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

  function isApiResponse(obj: any): obj is ApiResponse {
    return obj && 'status' in obj;
  }

  function isRetryResponse(obj: any): obj is RetryResponse {
    return obj && 'retry' in obj;
  }

  const polling = () => {
    poll(props.otherDevice).then((response) => {
      if (isApiResponse(response)) {
        responseStatus.value = response.status;
        if (response.qrCode !== '') {
          qrImage.value = response.qrCode;
        }
        if (response.status !== 'NOT_STARTED') {
          qrImage.value = '';
        }
        token.value = response.autoStartToken;
        messageCode.value = response.messageCode;

        if (response.status === 'COMPLETE') {
          window.location.href = PATHS.COMPLETE;
        } else if (response.status === 'CANCEL') {
          window.location.href = PATHS.CANCEL;
        } else if (response.status === 'ERROR') {
          qrImage.value = '';
        }
      }
      if (isRetryResponse(response) && response.retry === true) {
        /* Time is defined in seconds and setTimeout is in milliseconds */
        window.setTimeout(() => polling(), parseInt(response.time) * 1000);
      } else if (
        isRetryResponse(response) ||
        (isApiResponse(response) && (response.status === 'NOT_STARTED' || response.status === 'IN_PROGRESS'))
      ) {
        window.setTimeout(() => polling(), 500);
      }
    });
  };

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
    polling();
  });

  const retry = () => {
    cancel().then(r => {
      polling();
    });
  };
</script>

<template>
  <div class="content-container">
    <CustomContent v-if="qrImage" position="qrcode" />
    <CustomContent v-else position="autostart" />
    <p v-if="!showQrInstructions">{{ $t(messageCode) }}</p>
    <QrInstructions v-else />
    <AutoStart v-if="!otherDevice && !showContinueErrorButton()" :autoStartToken="token" />
    <QrDisplay :image="qrImage" />
    <div class="error-buttons" v-if="showContinueErrorButton()">
      <button class="error-button" @click="retry">
        <span>{{ $t('bankid.msg.btn-retry') }}</span>
      </button>
      <button class="error-button" @click="acceptError">
        <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
      </button>
    </div>
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

<style scoped>
.error-button {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin: 12px auto;
  padding: 20px 28px;
  border: 1px solid var(--btn-border-color);
  border-radius: var(--btn-border-radius);
  font-size: 16px;
  cursor: pointer;
  color: var(--btn-fg-color);
  background-color: var(--btn-bg-color);
}

.error-button::after {
  padding: 3px;
  border: solid var(--btn-fg-color);
  border-width: 0 3px 3px 0;
  content: '';
  transform: rotate(-45deg);
}
</style>
