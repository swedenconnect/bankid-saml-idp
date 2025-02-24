import {createRouter, createWebHistory} from 'vue-router';
import AutoStartView from '@/views/AutoStartView.vue';
import DeviceSelectView from '@/views/DeviceSelectView.vue';
import ErrorView from '@/views/ErrorView.vue';
import QrInstructionView from '@/views/QrInstructionView.vue';

let base = import.meta.env.BASE_URL;
let baseHref = (document.getElementById('router-href-id'));
if (typeof(baseHref) != 'undefined' && baseHref != null) {
  let attr = baseHref.attributes.getNamedItem("href");
  if (attr !== null) {
    base = attr.value;
  }
}
const router = createRouter({
  history: createWebHistory(base),
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
