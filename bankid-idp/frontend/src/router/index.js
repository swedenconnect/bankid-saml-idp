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
            props: {showQR: false}
        },
        {
            path: '/qr',
            name: 'qr',
            component: AuthenticateView,
            props: {showQR: true}
        }
    ]
})

export default router
