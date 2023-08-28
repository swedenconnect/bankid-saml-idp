<script>
  export default {
    name: 'localechanger',
    methods: {
      selectLanguage: function (lang) {
        this.$i18n.locale = lang;
      },
      getLanguage: function () {
        return this.$i18n.locale;
      },
    },
  };
</script>

<template>
  <div class="container main" id="languageDiv">
    <br />
    <div class="col-sm-12 float-right">
      <button class="btn btn-link" v-if="getLanguage() === 'en'" @click="selectLanguage('sv')">
        <span> Svenska </span>
      </button>
      <button class="btn btn-link" v-if="getLanguage() === 'sv'" @click="selectLanguage('en')">
        <span> English </span>
      </button>
    </div>
  </div>
  <br />
</template>
