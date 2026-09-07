/// <reference types="vite/client" />

// vue-i18n 9.14 moved its global property types from `@vue/runtime-core` to
// `vue`, where Volar does not pick them up. Re-declare `$t` on the module
// Volar reads so it stays typed in templates.
import type { Composer } from 'vue-i18n';

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $t: Composer['t'];
  }
}
