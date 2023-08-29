<script>
  import StatusMessage from '@/components/StatusMessage.vue';
  import { PATHS } from '@/Redirects';
  import { contactInformation } from '@/Service';

  export default {
    data() {
      return {
        contactEmail: '',
        displayEmail: false,
      };
    },
    beforeMount() {
      this.getContactInformation();
    },
    components: { StatusMessage },
    methods: {
      cancelSelection: function () {
        window.location.href = PATHS.CANCEL;
      },
      getErrorMessage: function () {
        let msg = this.$route.params.msg;
        if (this.$i18n.te(msg)) {
          return msg;
        }
        return 'bankid.msg.error.unknown';
      },
      getTraceId: function () {
        let pattern = /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/;
        if (pattern.test(this.$route.params.trace)) {
          return '' + this.$route.params.trace;
        }
        return '';
      },
      getMailToEmail: function () {
        return 'mailto: ' + this.contactEmail;
      },
      getContactInformation: function () {
        contactInformation().then((r) => {
          this.displayEmail = r['displayInformation'];
          this.contactEmail = r['email'];
        });
      },
    },
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
