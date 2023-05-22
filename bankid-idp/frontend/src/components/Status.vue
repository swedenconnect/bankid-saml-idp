<template>
  <div :hidden=!qr>
    <p>Open the BankID app and scan the QR code</p>
  </div>
  <div :hidden=qr>
    <p>Open the BankID app by clicking the button</p>
    <hr class="full-width">
    <br/>
    <button @click=navigateToApp> Start App {{ autoStartToken }}</button>
  </div>
</template>
<script>

export default {
    props: {
        qr: Boolean,
        autoStartToken : ""
    },
    methods: {
        getAutoStartLink: function () {
            return "bankid:///?autostarttoken=" + this.autoStartToken + "&redirect=null";
        },
        navigateToApp: function () {
            window.location.href = this.getAutoStartLink();
        }
    }
}
</script>