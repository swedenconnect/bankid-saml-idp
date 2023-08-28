<template>
  <div class="providers">
    <div class="provider">
      <Button class="provider-button" @click="this.authenticate('auto')">
        {{ $t('bankid.msg.btn-this') }}
      </Button>
    </div>
    <div class="provider">
      <Button class="provider-button" @click="this.authenticate('qr')">
        {{ $t('bankid.msg.btn-other') }}
      </Button>
    </div>
  </div>
</template>
<script>
  import { shallSelectDeviceAutomatically } from '@/AutoStartLinkFactory';
  import { selectedDecvice } from '@/Service';

  export default {
    methods: {
      authenticate: function (pushLocation) {
        this.$router.push({ name: pushLocation });
      },
    },
    mounted() {
      if (shallSelectDeviceAutomatically(window.navigator.userAgent)) {
        this.authenticate('auto');
      }
    },
    beforeMount() {
      selectedDecvice().then((r) => {
        if (r['isSign']) {
          if (r['device'] === 'this') {
            this.authenticate('sign-same');
          } else if (r['device'] === 'other') {
            this.authenticate('sign-other');
          }
        }
      });
    },
  };
</script>
