<template>
  <div :hidden=!qr>
    <p>Open the BankID app and scan the QR code</p>
  </div>
  <div :hidden=qr>
    <p>Open the BankID app by clicking the button</p>
    <hr class="full-width">
    <br/>
    <button>Start App</button>
  </div>
</template>
<script>

export default {
    props: {
        qr: Boolean
    }
}
</script>