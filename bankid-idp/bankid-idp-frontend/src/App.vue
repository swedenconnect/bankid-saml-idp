<script setup>
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import Localechanger from '@/components/LanguageSelector.vue';
</script>

<script>
  import { spInformation } from '@/Service';

  export default {
    data() {
      return {
        spInfo: {},
      };
    },
    mounted() {
      spInformation().then((r) => {
        this.spInfo = r;
      });
    },
  };
</script>

<template>
  <AppHeader :sp-info="spInfo" />
  <Localechanger />
  <RouterView />
  <AppFooter />
</template>
