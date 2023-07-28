<template>
    <div v-if="display" class="container-fluid status">
        <p>{{ $t("bankid.msg.error.service") }}</p>
    </div>
</template>
<script>
import {status} from "@/Service";

export default {
    data() {
        return {
            display: false
        }
    },
    methods: {

    },
    beforeMount() {
        status().then(s => {
           if(s["status"] !== "OK") {
                this.display = true;
           }
        });
    }
}
</script>