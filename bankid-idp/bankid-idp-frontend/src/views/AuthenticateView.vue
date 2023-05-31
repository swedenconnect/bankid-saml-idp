<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
        <Status
            :qr=showQR
            :autoStartToken=token
            :message=messageCode
        />
        <QRDisplay
            :qr=showQR
            :image=qrImage
        />
      </div>
      <p v-if="shouldCancel"> Stopping authentication ... </p>
    </div>
    <div class="col-sm-12 return">
      <button @click="cancelRequest" class="btn btn-link" type="submit" name="action" value="cancel">
        <span>Cancel</span>
      </button>
    </div>
  </div>

</template>
<script>
import QRDisplay from "@/components/QRDisplay.vue";
import CancelButton from "@/components/CancelButton.vue";
import Status from "@/components/Status.vue";
import {cancel, poll} from "@/service";

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
  components: {Status, CancelButton, QRDisplay},
  props: {
    showQR: Boolean
  },
  mounted() {
    this.pollingActive = true;
    this.poll();
  },
  methods: {
    poll: function () {
      poll(this.showQR).then(r => {
        this.qrImage = r["qrCode"];
        this.pollingActive = r["status"] === "IN_PROGRESS";
        this.token = r["autoStartToken"];
        this.messageCode = r["messageCode"];
      }).then(r => {
        if (this.shouldCancel) {
          cancel().then(r => {
            window.location.href = "/idp/view/cancel";
          });
        } else {
          if (this.pollingActive) {
            window.setTimeout(() => this.poll(), 500);
          } else {
            window.location.href = "/idp/view/complete";
          }
        }
      })
    },
    base64Image: function () {
      return "data:image/png;base64, " + this.qrImage;
    },
    cancelRequest: function () {
      this.shouldCancel = true;
    }
  }
}

</script>