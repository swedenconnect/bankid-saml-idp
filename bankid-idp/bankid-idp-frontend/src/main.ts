import './assets/main.css';
import './assets/open-sans-fonts.css';
import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import { getOverrides } from '@/Service';
import App from './App.vue';
import { messages } from './locale/messages';
import router from './router';
import type { LangObject, MessageOverride, Messages } from './types';

function applyMessageOverrides(originalMessages: Messages, overrides: MessageOverride[]): Messages {
  const newMessages: Messages = JSON.parse(JSON.stringify(originalMessages)); // Deep copy

  for (const override of overrides) {
    const path = override.code.split('.');
    let currentPartEn = newMessages.en;
    let currentPartSv = newMessages.sv;

    for (let i = 0; i < path.length; i++) {
      const part = path[i];

      if (i === path.length - 1) {
        currentPartEn[part] = override.en;
        currentPartSv[part] = override.sv;
      } else {
        if (!currentPartEn[part]) {
          currentPartEn[part] = {};
        }
        if (!currentPartSv[part]) {
          currentPartSv[part] = {};
        }

        if (typeof currentPartEn[part] === 'object' && currentPartEn[part] !== null) {
          currentPartEn = currentPartEn[part] as LangObject;
        }
        if (typeof currentPartSv[part] === 'object' && currentPartSv[part] !== null) {
          currentPartSv = currentPartSv[part] as LangObject;
        }
      }
    }
  }

  return newMessages;
}

async function initializeApp() {
  let messagesToBeLoaded = messages as Messages;
  let customContent = [];

  const overrides = await getOverrides();
  if (overrides.css.length > 0) {
    const style = document.createElement('style');
    style.appendChild(document.createTextNode(overrides.css[0].style));
    document.head.append(style);
  }
  if (overrides.messages.length > 0) {
    messagesToBeLoaded = applyMessageOverrides(messages, overrides.messages);
  }
  if (overrides.content.length > 0) {
    customContent = overrides.content;
  }

  const i18n = createI18n({
    legacy: false,
    locale: 'sv',
    fallbackLocale: 'en',
    messages: messagesToBeLoaded,
  });

  const app = createApp(App);
  app.provide('customContent', customContent);
  app.use(router);
  app.use(i18n);

  app.mount('#app');
}

initializeApp();
