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
  const newMessages: Messages = JSON.parse(JSON.stringify(originalMessages)); // Deep copy

  // Create empty object for any new language in the overrides
  for (const override of overrides) {
    for (const lang in override.text) {
      if (!newMessages[lang]) {
        newMessages[lang] = {};
      }
    }
  }

  for (const override of overrides) {
    const path = override.code.split('.');

    for (const lang in override.text) {
      let currentPart = newMessages[lang];

      for (let i = 0; i < path.length; i++) {
        const part = path[i];

        if (i === path.length - 1) {
          currentPart[part] = override.text[lang];
        } else {
          if (!currentPart[part]) {
            currentPart[part] = {};
          }

          if (typeof currentPart[part] === 'object' && currentPart[part] !== null) {
            currentPart = currentPart[part] as LangObject;
          }
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
