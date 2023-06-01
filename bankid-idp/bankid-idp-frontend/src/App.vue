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

<script>
import {spInformation} from "@/service";

export default {
    mounted() {
        spInformation()
    }
}
</script>
