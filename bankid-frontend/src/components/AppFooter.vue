<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { LangObject } from '@/types';

  const props = defineProps<{
    accessibilityLink: string | null;
    providerName: LangObject | null;
  }>();

  const CONTEXT_PATH = import.meta.env.BASE_URL;
  const LOGO_PATH = CONTEXT_PATH + '/logo.svg';

  const { locale } = useI18n();

  const getProviderName = () => (props.providerName ? props.providerName[locale.value] : '');
</script>

<template>
  <footer class="main-width">
    <img class="logo" :src="LOGO_PATH" alt="Logo" />
    <p v-if="props.providerName" class="copyright">{{ $t('bankid.msg.copyright', { provider: getProviderName() }) }}</p>
    <p v-if="props.accessibilityLink" class="accessibility-link">
      <a :href="props.accessibilityLink">{{ $t('bankid.msg.accessibility-link') }}</a>
    </p>
  </footer>
</template>

<style scoped>
  footer {
    display: flex;
    flex-direction: column-reverse;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
    border-top: 1px solid rgba(32, 0, 0, 0.18);
    padding: 1.5em 0.5em;
  }
  .logo {
    max-height: 40px;
    max-width: 300px;
  }
  .copyright {
    font-size: 10px;
  }
  p {
    text-align: center;
  }
  .accessibility-link {
    width: 100%;
  }
  @media (min-width: 576px) {
    footer {
      flex-direction: row;
      justify-content: space-between;
    }
    p {
      text-align: right;
    }
  }
</style>
