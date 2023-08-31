<script setup>
  import { onBeforeMount, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { shallSelectDeviceAutomatically } from '@/AutoStartLinkFactory';
  import { selectedDevice } from '@/Service';

  const router = useRouter();

  const authenticate = (pushLocation) => {
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
  <div class="providers">
    <div class="provider">
      <button class="provider-button" @click="authenticate('auto')">
        {{ $t('bankid.msg.btn-this') }}
      </button>
    </div>
    <div class="provider">
      <button class="provider-button" @click="authenticate('qr')">
        {{ $t('bankid.msg.btn-other') }}
      </button>
    </div>
  </div>
</template>
