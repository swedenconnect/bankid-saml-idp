<template>
    <div class="container-fluid header">
        <center>
            <div class="row">
                <div class="column">
                    <img class="top-logo-dim" :src=defaultImage alt="Logo"/>
                </div>
                <div class="column">
                    <img class="top-logo-dim" src="@/assets/BankID_logo.svg"
                         alt="Logo"/>
                </div>
            </div>
        </center>
    </div>
</template>
<script>
export default {
    props: {
        spInfo: {}
    }, //TODO check spinfo, if image is available display it, otherwise display swedenconnect
    data() {
        return {
            defaultImage: "@/assets/logo.svg"
        }
    }, methods: {
        getImage: function () {
            if (this.spInfo === null) {
                return this.defaultImage;
            }
            return this.spInfo["image"];
        }
    }
}
</script>