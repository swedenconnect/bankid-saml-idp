import './assets/main.css';
import './assets/open-sans-fonts.css';
import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import messages from '@/locale/messages.json';
import { getOverrides } from '@/Service';
import App from './App.vue';
import router from './router';
import type { LangObject, MessageOverride, Messages } from './types';

function applyMessageOverrides(originalMessages: Messages, overrides: MessageOverride[]): Messages {
  return overrides.reduce(
    (newMessages: Messages, override) => {
      const path = override.code.split('.');
      Object.keys(override.text).forEach((lang) => {
        let currentPart = newMessages[lang] || (newMessages[lang] = {});
        path.forEach((part, i) => {
          if (i === path.length - 1) currentPart[part] = override.text[lang];
          else currentPart = (currentPart[part] as LangObject) || (currentPart[part] = {});
        });
      });
      return newMessages;
    },
    JSON.parse(JSON.stringify(originalMessages)),
  );
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

  const ALL_LOCALES = Object.keys(messagesToBeLoaded);
  type LocaleType = (typeof ALL_LOCALES)[number];

  function isLocaleType(value: string): value is LocaleType {
    return ALL_LOCALES.includes(value as LocaleType);
  }

  const storedLocale = localStorage.getItem('locale') ?? '';
  const browserLocale = navigator.language.slice(0, 2);

  let locale: LocaleType;
  if (isLocaleType(storedLocale)) {
    locale = storedLocale;
  } else if (isLocaleType(browserLocale)) {
    locale = browserLocale;
  } else {
    locale = 'sv';
  }

  document.querySelector('html')?.setAttribute('lang', locale);

  const i18n = createI18n({
    legacy: false,
    locale: locale,
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
