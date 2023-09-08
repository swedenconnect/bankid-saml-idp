<script setup lang="ts">
  import { onBeforeMount, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { shallSelectDeviceAutomatically } from '@/AutoStartLinkFactory';
  import { selectedDevice } from '@/Service';

  const router = useRouter();

  const authenticate = (pushLocation: string) => {
    router.push({ name: pushLocation });
  };

  onMounted(() => {
    if (shallSelectDeviceAutomatically(window.navigator.userAgent)) {
      authenticate('auto');
    }
  });

  onBeforeMount(async () => {
    const r = await selectedDevice();
    if (r['isSign']) {
      if (r['device'] === 'this') {
        authenticate('sign-same');
      } else if (r['device'] === 'other') {
        authenticate('sign-other');
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
    border: 1px solid #d8d8d8;
    border-radius: 3px;
    font-size: 16px;
    cursor: pointer;
    background-color: #f7f7f7;
  }

  .device-button::after {
    padding: 3px;
    border: solid black;
    border-width: 0 3px 3px 0;
    content: '';
    transform: rotate(-45deg);
  }
</style>
