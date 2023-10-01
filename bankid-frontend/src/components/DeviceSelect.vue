<script setup lang="ts">
  import { onBeforeMount, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { shallSelectDeviceAutomatically } from '@/AutoStartLinkFactory';
  import type { SelectedDeviceInformation } from '@/types';

  const props = defineProps<{
    deviceData?: SelectedDeviceInformation;
  }>();

  const router = useRouter();

  const authenticate = (pushLocation: string) => {
    router.push({ name: pushLocation });
  };

  onMounted(() => {
    if (shallSelectDeviceAutomatically(window.navigator.userAgent)) {
      authenticate('auto');
    }
  });

  onBeforeMount(() => {
    if (props.deviceData && props.deviceData.isSign) {
      if (props.deviceData.device === 'this') {
        authenticate('auto');
      } else if (props.deviceData.device === 'other') {
        authenticate('qr');
      }
    }
  });
</script>

<template>
  <div class="devices">
    <button class="device-button" @click="authenticate('auto')">
      {{ $t('bankid.msg.btn-this') }}
    </button>
    <button class="device-button" @click="authenticate('qr')">
      {{ $t('bankid.msg.btn-other') }}
    </button>
  </div>
</template>

<style scoped>
  .device-button {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    margin: 12px auto;
    padding: 20px 28px;
    border: 1px solid var(--btn-border-color);
    border-radius: var(--btn-border-radius);
    font-size: 16px;
    cursor: pointer;
    color: var(--btn-fg-color);
    background-color: var(--btn-bg-color);
  }

  .device-button::after {
    padding: 3px;
    border: solid var(--btn-fg-color);
    border-width: 0 3px 3px 0;
    content: '';
    transform: rotate(-45deg);
  }
</style>
