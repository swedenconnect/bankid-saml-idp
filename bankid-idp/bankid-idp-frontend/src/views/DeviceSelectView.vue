<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">

      <div class="col-sm-12 content-container">
        <h2>BankID</h2>
        <p> {{ $t("bankid.msg.rfa20") }}</p>

        <hr class="full-width">
        <br/> <!-- TODO padding -->
        <DeviceSelect/>


      </div> <!-- ./col-sm-12 content-container -->


      <div class="col-sm-12 return">
        <button @click="cancelSelection" class="btn btn-link" type="submit" name="action" value="cancel">
          <span>{{ $t("bankid.msg.btn-cancel") }}</span>
        </button>
      </div>
    </div>

  </div> <!-- main -->
</template>
<script>
import DeviceSelect from "@/components/DeviceSelect.vue";

export default {
  components: {DeviceSelect},
  methods: {
    cancelSelection: function () {
      window.location.href = "/idp/view/cancel"
    }
  }
}
</script>