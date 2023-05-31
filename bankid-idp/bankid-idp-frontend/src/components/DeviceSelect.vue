<template>
    <div class="providers">
        <div class="provider">
            <Button class="provider-button" @click="this.authenticate('auto')">
                {{ $t("bankid.msg.btn-this") }}
            </Button>
        </div>
        <div class="provider">
            <Button class="provider-button" @click="this.authenticate('qr')">
                {{ $t("bankid.msg.btn-other") }}
            </Button>
        </div>
    </div>
</template>
<script>

export default {
    methods: {
        authenticate: function (pushLocation) {
            this.$router.push({name: pushLocation});
        }
    }
}
</script>