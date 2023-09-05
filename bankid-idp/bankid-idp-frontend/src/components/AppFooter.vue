<template>
  <footer>
    <p class="copyright">{{ $t('bankid.msg.copyright') }}</p>
    <img height="16" src="@/assets/logo-notext.svg" alt="Logo" />
  </footer>
</template>

<style scoped>
  footer {
    max-width: 620px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: end;
    gap: 10px;
  }
  .copyright {
    font-size: 10px;
  }
</style>
