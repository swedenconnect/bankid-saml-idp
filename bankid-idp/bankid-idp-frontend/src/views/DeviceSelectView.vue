<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">

      <div class="col-sm-12 content-container">
        <h2>BankID</h2>
        <p>Log in using your BankID</p>

        <hr class="full-width">
        <br/> <!-- TODO padding -->
        <DeviceSelect/>


      </div> <!-- ./col-sm-12 content-container -->


      <CancelButton/>
    </div>

  </div> <!-- main -->
</template>
<script setup>
import DeviceSelect from "@/components/DeviceSelect.vue";
import CancelButton from "@/components/CancelButton.vue";
</script>