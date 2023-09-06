<script setup lang="ts">
  import { watch } from 'vue';
  import { createLink } from '@/AutoStartLinkFactory';

  const props = defineProps<{
    autoStartToken: string;
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
      if (newToken) {
        navigateToApp();
      }
    },
  );
</script>

<template>
  <a :href="getAutoStartLink()">{{ $t('bankid.msg.btn-autostart') }}</a>
</template>
