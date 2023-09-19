import { createRouter, createWebHashHistory } from 'vue-router';
import AuthenticateView from '@/views/AuthenticateView.vue';
import DeviceSelectView from '@/views/DeviceSelectView.vue';
import ErrorView from '@/views/ErrorView.vue';

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
      path: '/signsame',
      name: 'sign-same',
      component: AuthenticateView,
      props: { otherDevice: false },
    },
    {
      path: '/signother',
      name: 'sign-other',
      component: AuthenticateView,
      props: { otherDevice: true },
    },
    {
      path: '/error/:msg/:trace?',
      name: 'error',
      component: ErrorView,
    },
  ],
});

export default router;
