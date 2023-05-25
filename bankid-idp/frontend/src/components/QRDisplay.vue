<template>
  <div :hidden=!qr>
    <hr class="full-width">
    <center> <!-- TODO center with css -->
      <img width="165" height="165" :src=base64Image() alt="QR Code"/>
    </center>
  </div>
</template>
<style scoped>
img {
    margin-top: 25px;
}
</style>
<script>

export default {
    props: {
        image: String,
        qr: Boolean
    },
    methods: {
        base64Image: function () {
            return this.image;
        }
    }
}
</script>
