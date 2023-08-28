import './assets/main.css';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { createI18n } from 'vue-i18n';
import App from './App.vue';
import { messages } from './locale/messages';
import router from './router';

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
