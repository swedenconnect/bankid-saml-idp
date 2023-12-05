<script setup lang="ts">
  import { onMounted, ref } from 'vue';

  const props = defineProps<{
    describedBy?: string;
  }>();

  defineEmits(['acceptError', 'retry']);
  const retryButton = ref<HTMLButtonElement>();

  onMounted(() => {
    retryButton.value?.focus();
  });
</script>

<template>
  <div class="buttons">
    <button id="cancel-button" class="btn-default" @click="$emit('acceptError')" :aria-describedby="props.describedBy">
      <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
    </button>
    <button
      id="retry-button"
      ref="retryButton"
      class="btn-default"
      @click="$emit('retry')"
      :aria-describedby="props.describedBy"
    >
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
