<script>
  import { status } from '@/Service';

  export default {
    props: {
      message: String,
    },
  };
</script>

<template>
  <div class="container-fluid status">
    <p>{{ $t(message) }}</p>
  </div>
</template>
