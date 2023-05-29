<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
        <Status :qr=showQR :autoStartToken=token
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
import {auth, cancel, poll} from "@/service";

export default {
  data() {
    return {
      qrImage: "",
      token: "",
      pollingActive: false,
      shouldCancel: false
    }
  },
  components: {Status, CancelButton, QRDisplay},
  props: {
    showQR: Boolean
  },
  mounted() {
    auth().then(r => {
      this.token = r["autoStartToken"];
      this.qrImage = r["qrCode"];
      this.pollingActive = true;
      this.poll();
    })
  },
  beforeUnmount() {
    this.pollingActive = false;
  },
  methods: {
    poll: function () {
      poll().then(r => {
        this.qrImage = r["qrCode"];
        this.pollingActive = r["status"] === "IN_PROGRESS";
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