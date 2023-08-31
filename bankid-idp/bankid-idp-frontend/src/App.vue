<script setup>
  import { onMounted, ref } from 'vue';
  import AppFooter from '@/components/AppFooter.vue';
  import AppHeader from '@/components/AppHeader.vue';
  import LocaleChanger from '@/components/LocaleChanger.vue';
  import { spInformation } from '@/Service';

  const spInfo = ref(null);

  onMounted(async () => {
    spInfo.value = await spInformation();
  });
</script>

<template>
  <AppHeader :sp-info="spInfo" />
  <LocaleChanger />
  <RouterView />
  <AppFooter />
</template>
