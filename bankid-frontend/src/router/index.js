import { createRouter, createWebHashHistory } from 'vue-router';
import AuthenticateView from '@/views/AuthenticateView.vue';
import DeviceSelectView from '@/views/DeviceSelectView.vue';
import ErrorView from '@/views/ErrorView.vue';
import QrInstructionView from '@/views/QrInstructionView.vue';

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'select',
      component: DeviceSelectView,
    },
    {
      path: '/auto',
      name: 'auto',
      component: AuthenticateView,
      props: { otherDevice: false },
    },
    {
      path: '/qr',
      name: 'qr',
      component: AuthenticateView,
      props: { otherDevice: true },
    },
    {
      path: '/qr-instruction',
      name: 'qr-instruction',
      component: QrInstructionView,
    },
    {
      path: '/error/:msg/:trace?',
      name: 'error',
      component: ErrorView,
    },
  ],
});

export default router;
