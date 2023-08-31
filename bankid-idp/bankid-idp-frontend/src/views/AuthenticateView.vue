<script setup>
  import { onMounted, ref } from 'vue';
  import QRDisplay from '@/components/QRDisplay.vue';
  import StatusItem from '@/components/StatusItem.vue';
  import { PATHS } from '@/Redirects';
  import { cancel, poll } from '@/Service';

  const qrImage = ref('');
  const token = ref('');
  const messageCode = ref('bankid.msg.rfa13');
  const responseStatus = ref('');

  const props = defineProps({
    otherDevice: Boolean,
    sign: Boolean,
  });

  const polling = () => {
    poll(props.otherDevice)
      .then((response) => {
        if (response['retry'] !== true) {
          responseStatus.value = response['status'];
          if (response['qrCode'] !== '') {
            qrImage.value = response['qrCode'];
          }
          if (response['status'] !== 'NOT_STARTED') {
            qrImage.value = '';
          }
          token.value = response['autoStartToken'];
          messageCode.value = response['messageCode'];
        }
        return response;
      })
      .then((response) => {
        if (response['status'] === 'COMPLETE') {
          window.location.href = PATHS.COMPLETE;
        } else if (response['status'] === 'CANCEL') {
          window.location.href = PATHS.CANCEL;
        } else if (response['status'] === 'ERROR') {
          qrImage.value = '';
        } else if (response['retry'] === true) {
          /* Time is defined in seconds and setTimeout is in millis*/
          window.setTimeout(() => polling(), parseInt(response['time'] * 1000));
        } else {
          window.setTimeout(() => polling(), 500);
        }
      });
  };

  const cancelRequest = async () => {
    await cancel();
    window.location.href = PATHS.CANCEL;
  };

  const acceptError = async () => {
    await cancel();
    window.location.href = PATHS.ERROR;
  };

  const showContinueErrorButton = () => {
    return responseStatus.value === 'ERROR';
  };

  onMounted(() => {
    polling();
  });
</script>

<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
        <StatusItem
          :otherDevice="otherDevice || showContinueErrorButton()"
          :autoStartToken="token"
          :message="messageCode"
        />
        <QRDisplay :image="qrImage" />
        <button v-if="showContinueErrorButton()" @click="acceptError">
          <span>{{ $t('bankid.msg.btn-error-continue') }}</span>
        </button>
      </div>
    </div>
    <div class="col-sm-12 return">
      <button
        v-if="!showContinueErrorButton()"
        @click="cancelRequest"
        class="btn btn-link"
        type="submit"
        name="action"
        value="cancel"
      >
        <span>{{ $t('bankid.msg.btn-cancel') }}</span>
      </button>
    </div>
  </div>
</template>
