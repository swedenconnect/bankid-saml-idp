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
        <span>{{ $t("bankid.msg.btn-cancel") }}</span>
      </button>
    </div>
  </div>

</template>
<script>
import QRDisplay from "@/components/QRDisplay.vue";
import Status from "@/components/Status.vue";
import {cancel, poll} from "@/Service";

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
    showQR: Boolean
  },
  mounted() {
    this.pollingActive = true;
    this.poll();
  },
  methods: {
    poll: function () {
      poll(this.showQR).then(response => {
        this.qrImage = response["qrCode"];
        this.pollingActive = response["status"] === "IN_PROGRESS";
        this.token = response["autoStartToken"];
        this.messageCode = response["messageCode"];
        return response;
      }).then(response => {
        if (this.shouldCancel) {
          cancel().then(canel => {
            window.location.href = import.meta.env.BASE_URL + "/view/cancel";
          });
        } else {
          if (this.pollingActive) {
            window.setTimeout(() => this.poll(), 500);
          } else {
              if (response["status"] !== "ERROR") {
                  window.location.href = import.meta.env.BASE_URL + "/view/complete";
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
              window.location.href = import.meta.env.BASE_URL + "/view/cancel";
          });
      } else {
          this.shouldCancel = true;
      }
    }
  }
}

</script>