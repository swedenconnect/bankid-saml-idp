import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia';

import { messages } from './locale/messages';
import { createI18n } from 'vue-i18n';

const i18n = createI18n({
    locale: 'sv',
    fallbackLocale: 'en',
    messages,
});

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(router);
app.use(i18n);

app.mount('#app');
