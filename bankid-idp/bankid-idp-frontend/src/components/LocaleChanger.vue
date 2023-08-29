<script setup>
  import { useI18n } from 'vue-i18n';

  const { locale } = useI18n();

  const selectLanguage = (lang) => {
    locale.value = lang;
  };
</script>

<template>
  <div class="container main" id="languageDiv">
    <br />
    <div class="col-sm-12 float-right">
      <button class="btn btn-link" v-if="locale === 'en'" @click="selectLanguage('sv')">
        <span> Svenska </span>
      </button>
      <button class="btn btn-link" v-if="locale === 'sv'" @click="selectLanguage('en')">
        <span> English </span>
      </button>
    </div>
  </div>
  <br />
</template>
