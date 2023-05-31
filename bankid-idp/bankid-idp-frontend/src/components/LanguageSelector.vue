<template>
    <div class="container main" id="languageDiv">
        <div class="col-sm-12">
            <select v-model="$i18n.locale">
                <option v-for="(lang, i) in langs" :key="`Lang${i}`" :value="lang">
                    {{ lang }}
                </option>
            </select>
        </div>
    </div>
</template>
<script>
export default {
    name: 'localechanger',
    data() {
        return { langs: ['sv', 'en'] };
    },
};
</script>