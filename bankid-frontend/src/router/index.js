import { createRouter, createWebHashHistory } from 'vue-router';
import AutoStartView from '@/views/AutoStartView.vue';
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
      component: AutoStartView,
    },
    {
      path: '/qr',
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
