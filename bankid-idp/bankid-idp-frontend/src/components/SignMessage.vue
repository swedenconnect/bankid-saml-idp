<script>
export default {
    props: {
        signMessage : String
    }
}
</script>
<template>
  <div class="full-width sign-message">
    <div class="row no-gutters">
      <div class="col">
        <span :text="sign-message">{{ signMessage }}</span>
      </div>
    </div>
  </div>
  <input type="hidden" name="signMessageDisplayed"/>
</template>