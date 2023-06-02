<template>
    <div class="container-fluid header">
        <center>
            <div class="row">
                <div class="column">
                    <img class="top-logo-dim" :src=getImage() alt="Logo" :hidden=hideImage()
                    />
                </div>
                <div class="column">
                    <img class="top-logo-dim" src="@/assets/BankID_logo.svg" alt="Logo"/>
                </div>
            </div>
        </center>
    </div>
</template>
<script>
export default {
    props: {
        spInfo: {}
    },
    data() {
        return {
            defaultImage: "/idp/assets/logo.svg"
        }
    }, methods: {
        getImage: function () {
            return this.spInfo["imageUrl"];
        },
        hideImage: function () {
            return !this.spInfo.hasOwnProperty("imageUrl");
        }
    }
}
</script>