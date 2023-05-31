<template>
    <div>
        <p> {{ $t(message) }}</p>
    </div>
    <div :hidden=qr>
        <button @click=navigateToApp>
            {{ $t("bankid.msg.btn-autostart") }}
        </button>
    </div>
</template>
<script>

export default {
    props: {
        qr: Boolean,
        autoStartToken: "",
        message: ""
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