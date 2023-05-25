<script>
export default {
    props: {
        headText: String,
        spDisplayName: String
    }
}
</script>
<template>
  <div class="row" id="spInfoText">
    <div class="col-sm-12 content-heading">
      <h2>{{ headText }}</h2>
    </div>
    <div class="col-sm-12">
      <p class="info">
        <span>{{ spDisplayName }}</span>
        <span> requests your authentication OR requests your signature of the text below.</span>
      </p>
    </div>

    <div class="col-sm-12">
      <p class="info">
        <span> The signature will be performed according to the X assurance level.</span>
        <span> The authentication will be performed according to the X assurance level.</span>
      </p>
    </div>
    <input type="hidden" name="loa" value="${ui.getPossibleAuthnContextUris().get(0)}"/>
  </div>
</template>