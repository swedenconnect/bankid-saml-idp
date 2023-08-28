<script setup>
  import Footer from '@/components/Footer.vue';
  import Header from '@/components/Header.vue';
  import Localechanger from '@/components/LanguageSelector.vue';
</script>

<template>
  <Header :sp-info="spInfo" />
  <Localechanger />
  <RouterView />
  <Footer />
</template>

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
