import {createRouter, createWebHashHistory} from 'vue-router'
import AuthenticateView from "@/views/AuthenticateView.vue";
import DeviceSelectView from "@/views/DeviceSelectView.vue";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'select',
      component: DeviceSelectView
    },
    {
      path: '/auto',
      name: 'auto',
      component: AuthenticateView,
      props: {otherDevice: false, sign: false}
    },
    {
      path: '/qr',
      name: 'qr',
      component: AuthenticateView,
      props: {otherDevice: true, sign: false}
    },
    {
      path: '/signsame',
      name: 'sign-same',
      component: AuthenticateView,
      props: {otherDevice: false, sign: true}
    },
    {
      path: '/signother',
      name: 'sign-other',
      component: AuthenticateView,
      props: {otherDevice: true, sign: true}
    }
  ]
})

export default router
