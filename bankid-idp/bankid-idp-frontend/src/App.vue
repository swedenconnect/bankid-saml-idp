<script setup>
import Header from "@/components/Header.vue";
import Footer from "@/components/Footer.vue";
import Localechanger from "@/components/LanguageSelector.vue";
</script>

<template>

  <Header/>
  <Localechanger/>
  <RouterView/>
  <Footer/>
</template>
