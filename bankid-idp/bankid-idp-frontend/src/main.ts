import './assets/main.css';
import './assets/open-sans-fonts.css';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { createI18n } from 'vue-i18n';
import { getOverrides } from '@/Service';
import App from './App.vue';
import { messages } from './locale/messages';
import router from './router';

async function initializeApp() {
  const overrides = await getOverrides();
  if (overrides.css.length > 0) {
    const style = document.createElement('style');
    style.appendChild(document.createTextNode(overrides.css[0].style));
    document.head.append(style);
  }

  const i18n = createI18n({
    legacy: false,
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
}

initializeApp();
