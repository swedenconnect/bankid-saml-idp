<template>
    <div class="container-fluid status">
        <p> {{ $t(message)}} </p>
    </div>
</template>
<script>
import {status} from "@/Service";

export default {
    props: {
        message: String
    }
}
</script>