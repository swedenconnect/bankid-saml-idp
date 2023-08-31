<script setup>
  import { watch } from 'vue';
  import { createLink } from '@/AutoStartLinkFactory';

  const props = defineProps({
    otherDevice: Boolean,
    autoStartToken: String,
    message: String,
  });

  const getAutoStartLink = () => {
    return createLink(window.navigator.userAgent, props.autoStartToken, window.location.href);
  };

  const navigateToApp = () => {
    window.location.href = getAutoStartLink();
  };

  watch(
    () => props.autoStartToken,
    (newToken) => {
      if (!props.otherDevice && !newToken) {
        navigateToApp();
      }
    },
  );
</script>

<template>
  <div>
    <p>{{ $t(message) }}</p>
  </div>
  <div :hidden="otherDevice">
    <button @click="navigateToApp">
      {{ $t('bankid.msg.btn-autostart') }}
    </button>
  </div>
</template>
