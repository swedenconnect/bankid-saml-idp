<script setup lang="ts">

const CONTEXT_PATH = import.meta.env.BASE_URL;
const LOGO_PATH = CONTEXT_PATH + "/logo.svg";
</script>
<template>
  <footer class="main-width">
    <p class="copyright">{{ $t('bankid.msg.copyright') }}</p>
    <img height="16" :src=LOGO_PATH alt="Logo"/>
  </footer>
</template>

<style scoped>
  footer {
    display: flex;
    align-items: center;
    justify-content: end;
    gap: 10px;
  }
  .copyright {
    font-size: 10px;
  }
</style>
