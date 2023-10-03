<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  const { locale } = useI18n();

  const selectLanguage = (lang: string) => {
    locale.value = lang;
    localStorage.setItem('locale', locale.value);
  };
</script>

<template>
  <div class="locale-changer main-width">
    <button class="btn-link" v-if="locale === 'en'" @click="selectLanguage('sv')">Svenska</button>
    <button class="btn-link" v-if="locale === 'sv'" @click="selectLanguage('en')">English</button>
  </div>
</template>

<style scoped>
  .locale-changer {
    text-align: right;
  }
</style>
