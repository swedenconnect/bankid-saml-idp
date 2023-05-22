<template>
  <ul>
    <li>
      <Button @click="this.authenticate('auto')">This Device</Button>
    </li>
    <li>
      <Button @click="this.authenticate('qr')">Other Device</Button>
    </li>
  </ul>

</template>
<script>
import {auth} from "@/service";

export default {
    methods: {
        authenticate: function (pushLocation) {
            auth()
                .then(r => {
                    console.log(r);
                    this.$router.push({name: pushLocation, params:{"data":r}});
                });
            //this.$router.push({name: pushLocation});
        }
    }
}
</script>