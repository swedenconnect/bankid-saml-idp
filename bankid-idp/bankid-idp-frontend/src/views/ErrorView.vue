<script setup lang="ts">
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
    const msg = route.params.msg as string;
    if (te(msg)) {
      return msg;
    }
    return 'bankid.msg.error.unknown';
  };

  const getTraceId = () => {
    const pattern = /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/;
    const trace = route.params.trace as string;
    if (pattern.test(trace)) {
      return trace;
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
  <div class="content-container main-width">
    <h2>Bankid</h2>
    <StatusMessage :message="getErrorMessage()" />
    <p v-if="displayEmail && contactEmail">{{ $t('bankid.msg.contact') }}</p>
    <p v-if="displayEmail && contactEmail">Email: {{ contactEmail }}</p>
    <p v-if="displayEmail && getTraceId()">Id: {{ getTraceId() }}</p>
    <p>{{ $t('bankid.msg.error-page-close') }}</p>
  </div>
</template>
