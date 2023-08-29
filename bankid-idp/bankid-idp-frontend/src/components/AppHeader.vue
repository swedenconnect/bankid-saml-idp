<script>
  export default {
    props: {
      spInfo: {
        type: Object,
        default() {
          return { imageUrl: '' };
        },
      },
    },
    methods: {
      getImage: function () {
        return this.spInfo['imageUrl'];
      },
    },
  };
</script>

<template>
  <div class="container-fluid header">
    <center>
      <div class="row">
        <div class="column">
          <img v-if="getImage()" class="top-logo-dim" :src="getImage()" alt="Logo" />
          <img v-else class="top-logo-dim" src="@/assets/logo.svg" alt="Logo" />
        </div>
        <div class="column">
          <img class="top-logo-dim" src="@/assets/BankID_logo.svg" alt="Logo" />
        </div>
      </div>
    </center>
  </div>
</template>
