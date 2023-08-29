<script setup>
  import { onBeforeMount, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import StatusMessage from '@/components/StatusMessage.vue';
  import { contactInformation } from '@/Service';

  const contactEmail = ref('');
  const displayEmail = ref(false);
  const route = useRoute();
  const { te } = useI18n();

  onBeforeMount(() => getContactInformation());

  const getErrorMessage = () => {
    let msg = route.params.msg;
    if (te(msg)) {
      return msg;
    }
    return 'bankid.msg.error.unknown';
  };

  const getTraceId = () => {
    let pattern = /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/;
    if (pattern.test(route.params.trace)) {
      return '' + route.params.trace;
    }
    return '';
  };

  const getContactInformation = () => {
    contactInformation().then((r) => {
      displayEmail.value = r['displayInformation'];
      contactEmail.value = r['email'];
    });
  };
</script>

<template>
  <div class="container main" id="main">
    <div class="row" id="mainRow">
      <div class="col-sm-12 content-container">
        <h2>Bankid</h2>
        <br />
        <StatusMessage :message="getErrorMessage()" />
        <p v-if="displayEmail && contactEmail">{{ $t('bankid.msg.contact') }}</p>
        <p v-if="displayEmail && contactEmail">Email: {{ contactEmail }}</p>
        <p v-if="displayEmail && getTraceId()">Id: {{ getTraceId() }}</p>
        <p>{{ $t('bankid.msg.error-page-close') }}</p>
      </div>
    </div>
  </div>
</template>
