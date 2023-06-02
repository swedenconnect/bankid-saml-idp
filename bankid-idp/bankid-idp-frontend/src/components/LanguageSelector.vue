<template>
    <div class="container main" id="languageDiv">
        <br/>
        <div class="col-sm-12 float-right">
            <select v-model="$i18n.locale">
                <option v-for="(lang, i) in langs" :key="`Lang${i}`" :value="lang">
                    {{ lang }}
                </option>
            </select>
        </div>
    </div>
    <br/>
</template>
<script>
export default {
    name: 'localechanger',
    data() {
        return { langs: ['sv', 'en'] };
    },
};
</script>