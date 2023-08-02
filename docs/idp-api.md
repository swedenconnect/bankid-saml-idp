# Api Documentation

| Method | Path           | Request                | Response                                                                                                                         |
|--------|----------------|------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| POST   | /api/poll      | QueryParam: Boolean qr | [ApiResponse](../bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/ApiResponse.java)                 |
| POST   | /api/cancel    | Void                   | Void                                                                                                                             |
| GET    | /api/device    | Void                   | [SelectedDeviceInformation](../bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/SelectedDeviceInformation.java) |
| GET    | /api/status    | Void                   | [ServiceInformation](../bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/ServiceInformation.java) |
| GET    | /api/sp        | Void                   | [SpInformation](../bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/SpInformation.java)                                                                                                             |                                                                                                
| GET    | /view/complete | Void                   | ModelAndView                                                                                                                     |
| GET    | /view/cancel   | Void                   | ModelAndView                                                                                                                     |
| GET    | /bankid        | Void                   | ModelAndView                                                                                                                     |

## Views

If you want to implement your own frontend you can either replace the index page with your own frontend or host it separately.

If hosted separately the /bankid path should be disabled by using the configuration flag `bankid.standalone = false` 

### /bankid

This is the primary view that displays the frontend that has been built into resources (defaults to vue frontend)

### /view/complete

Redirect here to consume a completed BankId login and get a AuthNResponse

Will redirect to sp when done

### /view/cancel

Redirect here if the user wants to cancel the current login

Will redirect to sp when done

## Polling

The polling endpoint is the primary endpoint that the frontend will call upon which can require some additional handling
if you write your own frontend.

### Status Codes

| Code | Description                                                                                                                                                                                                                                                                                                                                                                                                          |
|------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200  | Ok                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 429  | Too many attempts, resource busy, parallel requests have been made for the same user, a header "Retry-After" will be present as a time for when you can retry the request again <br><br> This can also happend when a request has been blocked by the circuit breaker due to api errors towards Bankid api <br> <br> This status code means that the request has been rejected before being made towards BankIds api |
