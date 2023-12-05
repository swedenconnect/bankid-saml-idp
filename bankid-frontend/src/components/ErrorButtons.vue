<script setup lang="ts">
  import { onMounted, ref } from 'vue';

  defineEmits(['acceptError', 'retry']);
  const retryButton = ref<HTMLButtonElement>();

  onMounted(() => {
    retryButton.value?.focus();
  });
</script>

<template>
  <div class="buttons">
    <button class="btn-default" @click="$emit('acceptError')">
      <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
    </button>
    <button class="btn-default" @click="$emit('retry')">
      <span>{{ $t('bankid.msg.btn-retry') }}</span>
    </button>
  </div>
</template>

<style scoped>
  .buttons {
    display: flex;
    gap: 1em;
  }
  .buttons .btn-default {
    flex: 1;
  }
</style>
