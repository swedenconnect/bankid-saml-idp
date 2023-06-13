<template>
    <div>
        <p> {{ $t(message) }}</p>
    </div>
    <div :hidden=otherDevice>
        <button @click=navigateToApp>
            {{ $t("bankid.msg.btn-autostart") }}
        </button>
    </div>
</template>
<script>

import {createLink} from "@/AutoStartLinkFactory";

export default {
    props: {
        otherDevice: Boolean,
        sign: Boolean,
        autoStartToken: "",
        message: "",
        signDevice: String
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
            if(!this.otherDevice) {
                console.log("Not sign!")
                this.navigateToApp();
            }
        }
    }
}
</script>