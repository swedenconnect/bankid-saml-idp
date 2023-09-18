# Working example of applying overrides in runtime
One option to customize the look of your BankIdIdp Application is to use overloads.
This comes with the benefit of not having to write your own frontend and not having to rebuild the application.

## Docker + Overload + Environemnt Variable
For this example we will build the application using the following script.
```shell
sh build-backend.sh
```

Then we will use an example deployment specified under the folder `services/`

We use a property named `BANKID_OVERRIDE_DIRECTORYPATH` to allow overrides to be configured at runtime (startup)
Please see (docs/override.md)[docs/override.md] for more information regarding overrides

To make the overrides available for our docker container we use a volume mount of our overrides

### Example
We will run the application twice, first without overrides and secondly with overrides.
With the example overrides, we will make the whole page pink and define a new message as well as display it on the main page.

#### Without Overrides
```yml
      #- BANKID_OVERRIDE_DIRECTORYPATH=/opt/overrides #Disabled
```
![Before](before.png)
#### With Overrides
```yml
      - BANKID_OVERRIDE_DIRECTORYPATH=/opt/overrides #Enabled
```
![After](after.png)

