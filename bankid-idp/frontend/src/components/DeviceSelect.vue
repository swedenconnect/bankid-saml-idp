<template>
  <ul>
    <li>
      <Button @click="this.$router.push({name:'auto'})">This Device</Button>
    </li>
    <li>
      <Button @click="this.$router.push({name:'qr'})">Other Device</Button>
    </li>
  </ul>

</template>
<script setup>
</script>