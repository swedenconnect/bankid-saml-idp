<script>
  export default {
    props: {
      image: String,
    },
    methods: {
      base64Image: function () {
        return this.image;
      },
      hideQR: function () {
        return this.base64Image() === '';
      },
    },
  };
</script>

<template>
  <div :hidden="hideQR()">
    <hr class="full-width" />
    <center>
      <!-- TODO center with css -->
      <img width="165" height="165" :src="base64Image()" alt="QR Code" />
    </center>
  </div>
</template>

<style scoped>
  img {
    margin-top: 25px;
  }
</style>
