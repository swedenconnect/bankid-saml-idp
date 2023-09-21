<script setup lang="ts">
  const CONTEXT_PATH = import.meta.env.BASE_URL;
  const LOGO_PATH = CONTEXT_PATH + '/logo.svg';
</script>
<template>
  <footer class="main-width">
    <img class="logo" :src="LOGO_PATH" alt="Logo" />
    <p class="copyright">{{ $t('bankid.msg.copyright') }}</p>
  </footer>
</template>

<style scoped>
  footer {
    display: flex;
    flex-direction: column-reverse;
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
    text-align: center;
  }
  @media (min-width: 576px) {
    footer {
      flex-direction: row;
      justify-content: space-between;
    }
    .copyright {
      text-align: right;
    }
  }
</style>
