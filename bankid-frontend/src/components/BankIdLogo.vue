<template>
  <div class="bankid-logo">
    <img src="@/assets/BankID_logo.svg" alt="BankID logo" />
  </div>
</template>

<style scoped>
  .bankid-logo {
    margin-top: 2em;
    text-align: center;
  }

  @media (min-width: 576px) {
    img {
      height: 120px;
    }
  }

  @media (max-width: 576px) {
    img {
      height: 90px;
    }
  }
</style>
