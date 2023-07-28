<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">

      <div class="col-sm-12 content-container">
          <StatusMessage/>

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
import {PATHS} from "@/Redirects";
import StatusMessage from "@/components/StatusMessage.vue";

export default {
  components: {StatusMessage, DeviceSelect},
  methods: {
    cancelSelection: function () {
      window.location.href = PATHS.CANCEL;
    }
  }
}
</script>