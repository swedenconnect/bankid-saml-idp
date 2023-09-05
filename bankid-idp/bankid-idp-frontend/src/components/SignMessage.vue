<script setup lang="ts">
  const props = defineProps<{
    signMessage: string;
  }>();
</script>

<template>
  <div class="full-width sign-message">
    <div class="row no-gutters">
      <div class="col">
        <span>{{ props.signMessage }}</span>
      </div>
    </div>
  </div>
  <input type="hidden" name="signMessageDisplayed" />
</template>
