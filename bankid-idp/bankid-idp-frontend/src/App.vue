<script setup>
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
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
  <LocaleChanger />
  <RouterView />
  <AppFooter />
</template>
