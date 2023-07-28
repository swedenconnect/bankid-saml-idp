<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
          <StatusMessage/>
          <Status
            :otherDevice=otherDevice
            :autoStartToken=token
            :message=messageCode
        />
        <QRDisplay
            :image=qrImage
        />
      </div>
      <p v-if="shouldCancel"> {{ $t("bankid.msg.cancel-progress") }} </p>
    </div>
    <div class="col-sm-12 return">
      <button @click="cancelRequest" class="btn btn-link" type="submit" name="action" value="cancel">
        <span>{{ $t("bankid.msg.btn-cancel") }}</span>
      </button>
    </div>
  </div>

</template>
<script>
import QRDisplay from "@/components/QRDisplay.vue";
import Status from "@/components/Status.vue";
import {cancel, poll} from "@/Service";
import {PATHS} from "@/Redirects";
import StatusMessage from "@/components/StatusMessage.vue";

export default {
  data() {
    return {
      qrImage: "",
      token: "",
      shouldCancel: false,
      messageCode: "bankid.msg.rfa13"
    }
  },
  components: {StatusMessage, Status, QRDisplay},
  props: {
    otherDevice: Boolean, sign: Boolean
  },
  mounted() {
    this.poll();
  },
  methods: {
    poll: function () {
      poll(this.otherDevice && !this.sign).then(response => {
        if (response["retry"] !== true) {
          if (response["qrCode"] !== "") {
              this.qrImage = response["qrCode"];
          }
          this.token = response["autoStartToken"];
          this.messageCode = response["messageCode"];
        }
        return response;
      }).then(response => {

            if (response["status"] === "COMPLETE") {
                window.location.href = PATHS.COMPLETE;
            }
            else if (response["status"] === "CANCEL") {
                window.location.href = PATHS.CANCEL;
            }
            else if (response["retry"] === true) {
              /* Time is defined in seconds and setTimeout is in millis*/
              window.setTimeout(() => this.poll(), parseInt(response["time"] * 1000));
            } else {
              window.setTimeout(() => this.poll(), 500);
            }

      })
    },
    base64Image: function () {
      return "data:image/png;base64, " + this.qrImage;
    },
    cancelRequest: function () {
        cancel().then(r => {
          window.location.href = PATHS.CANCEL;
        });
    }
  }
}

</script>