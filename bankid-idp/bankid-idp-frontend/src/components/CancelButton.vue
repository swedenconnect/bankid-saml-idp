<template>

</template>
<script>
import {cancel} from "@/service";

export default {
  methods: {
    cancelButton: function () {
      cancel().then(r => {
        this.$router.push({name: 'select'})
      });
    }
  }
}
</script>