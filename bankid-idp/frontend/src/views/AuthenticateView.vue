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
    </div>
    <CancelButton/>
  </div>

</template>
<script>
import QRDisplay from "@/components/QRDisplay.vue";
import CancelButton from "@/components/CancelButton.vue";
import Status from "@/components/Status.vue";
import {auth, poll} from "@/service";

export default {
  data() {
    return {
      qrImage: "",
      token: "",
      pollingActive: true
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
      this.poll();
    })

  },
  methods: {
    poll: function () {
      poll().then(r => {
        this.qrImage = r["qrCode"];
        this.pollingActive = r["status"] === "IN_PROGRESS";
      }).then(r => {
        if (this.pollingActive) {
          window.setTimeout(() => this.poll(), 500);
        } else {
          window.location.href = "/idp/complete";
        }
      })
    },
    base64Image: function () {
      return "data:image/png;base64, " + this.qrImage;
    }
  }
}

</script>