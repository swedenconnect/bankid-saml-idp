<script setup lang="ts">
  const props = defineProps<{
    signMessage: string;
  }>();
</script>

<template>
  <div class="sign-message">
    <div>
      <span>{{ props.signMessage }}</span>
    </div>
  </div>
  <input type="hidden" name="signMessageDisplayed" />
</template>

<style scoped>
  .sign-message {
    margin-bottom: 30px;
    background-color: #f7f7f7;
    border: 1px solid #d0caca;
    padding: 23px 28px;
  }

  .sign-message div {
    margin: 0;
    font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  }
</style>
