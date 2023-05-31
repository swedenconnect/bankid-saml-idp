<template>
    <div class="providers">
        <div class="provider">
            <Button class="provider-button" @click="this.authenticate('auto')">BankID on This Device</Button>
        </div>
        <div class="provider">
            <Button class="provider-button" @click="this.authenticate('qr')">Mobile BankID on Other Device</Button>
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