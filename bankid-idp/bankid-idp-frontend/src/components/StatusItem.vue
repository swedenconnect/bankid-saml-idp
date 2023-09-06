<script setup lang="ts">
  import { watch } from 'vue';
  import { createLink } from '@/AutoStartLinkFactory';

  const props = defineProps<{
    otherDevice: boolean;
    autoStartToken: string;
    message: string;
  }>();

  const getAutoStartLink = () => {
    return createLink(window.navigator.userAgent, props.autoStartToken, window.location.href);
  };

  const navigateToApp = () => {
    window.location.href = getAutoStartLink();
  };

  watch(
    () => props.autoStartToken,
    (newToken) => {
      if (!props.otherDevice && newToken) {
        navigateToApp();
      }
    },
  );
</script>

<template>
  <p>{{ $t(message) }}</p>
  <a v-if="!otherDevice" :href="getAutoStartLink()">{{ $t('bankid.msg.btn-autostart') }}</a>
</template>
