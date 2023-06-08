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

import {createLink} from "@/AutoStartLinkFactory";

export default {
    props: {
        qr: Boolean,
        autoStartToken: "",
        message: ""
    },
    methods: {
        getAutoStartLink: function () {
            return createLink(window.navigator.userAgent, this.autoStartToken, window.location.href);
        },
        navigateToApp: function () {
            window.location.href = this.getAutoStartLink();
        }
    },
    watch: {
        autoStartToken(oldToken, newToken) {
            if(!this.qr) {
                this.navigateToApp();
            }
        }
    }
}
</script>