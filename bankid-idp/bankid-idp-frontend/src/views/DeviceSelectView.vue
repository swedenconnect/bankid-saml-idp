<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import BankIdLogo from '@/components/BankIdLogo.vue';
  import DeviceSelect from '@/components/DeviceSelect.vue';
  import StatusMessage from '@/components/StatusMessage.vue';
  import { PATHS } from '@/Redirects';
  import { getOverrides, status } from '@/Service';

  const displayServiceMessage = ref(false);

  const cancelSelection = () => {
    window.location.href = PATHS.CANCEL;
  };

  onBeforeMount(() => {
    status().then((s) => {
      if (s['status'] !== 'OK') {
        displayServiceMessage.value = true;
      }
      getOverrides().then((response) => {
        console.log(response);
        const style = document.createElement('style');
        style.appendChild(document.createTextNode(response.css[0].style));
        // document.head.append(style);
      });
    });
  });
</script>

<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
        <StatusMessage message="bankid.msg.error.service" v-if="displayServiceMessage" />
        <BankIdLogo />
        <h2>BankID</h2>
        <p>{{ $t('bankid.msg.rfa20') }}</p>

        <hr class="full-width" />
        <br />
        <!-- TODO padding -->
        <DeviceSelect />
      </div>
      <!-- ./col-sm-12 content-container -->

      <div class="return">
        <button @click="cancelSelection" class="btn btn-link" type="submit" name="action" value="cancel">
          <span>{{ $t('bankid.msg.btn-cancel') }}</span>
        </button>
      </div>
    </div>
  </div>
  <!-- main -->
</template>
