<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
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

export default {
  data() {
    return {
      qrImage: "",
      token: "",
      pollingActive: false,
      shouldCancel: false,
      messageCode: "bankid.msg.rfa13"
    }
  },
  components: {Status, QRDisplay},
  props: {
    otherDevice: Boolean, sign: Boolean
  },
  mounted() {
    this.pollingActive = true;
    this.poll();
  },
  methods: {
    poll: function () {
      poll(this.otherDevice && !this.sign).then(response => {
        if (response["retry"] !== true) {
          this.qrImage = response["qrCode"];
          this.pollingActive = response["status"] === "IN_PROGRESS";
          this.token = response["autoStartToken"];
          this.messageCode = response["messageCode"];
        }
        return response;
      }).then(response => {
        if (this.shouldCancel) {
          cancel().then(canel => {
            window.location.href = PATHS.CANCEL;
          });
        } else {
          if (this.pollingActive) {
            if (response["retry"] === true) {
              /* Time is defined in seconds and setTimeout is in millis*/
              window.setTimeout(() => this.poll(), parseInt(response["time"] * 1000));
            } else {
              window.setTimeout(() => this.poll(), 2000);
            }
          } else {
            if (response["status"] === "COMPLETE") {
              window.location.href = PATHS.COMPLETE;
            }
          }
        }
      })
    },
    base64Image: function () {
      return "data:image/png;base64, " + this.qrImage;
    },
    cancelRequest: function () {
      if (!this.pollingActive) {
        cancel().then(r => {
          window.location.href = PATHS.CANCEL;
        });
      } else {
        this.shouldCancel = true;
      }
    }
  }
}

</script>