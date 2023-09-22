export const messages = {
    en: {
        "idp": {
            "error": {
                "unrecoverable": {
                    "internal": "An internal error occurred",
                    "unknown-peer": "The sender of the authentication request has not been registered at the Identity Provider",
                    "replay": "Authentication request message has already been processed",
                    "too-old": "Received message is too old and not accepted",
                    "acs": "The indicated Assertion Consumer Service is not registered",
                    "no-signature": "Authentication request was not signed - this is required",
                    "bad-signature": "Signature validation on received authentication request failed",
                    "format": "The format on the received authentication request is invalid",
                    "decode": "The received message could not be decoded into a valid authentication request",
                    "endpoint": "The endpoint information supplied in the authentication request do not correspond with the endpoint on which the message was delivered",
                    "session": "Required session data could not be found"
                }
            }
        },
        "bankid": {
            "msg": {
                "rp-auth": "has requested that you authenticate.",
                "rp-sign": "has requested your signature.",
                "rfa1": "Start your BankID app.",
                "rfa2": "The BankID app is not installed. Please contact your Internet bank.",
                "rfa3": "Action cancelled. Please try again.",
                "rfa4": "An identification or signing for this personal number is already started. Please try again.",
                "rfa5": "Internal error. Please try again.",
                "rfa6": "Action cancelled.",
                "rfa8": "The BankID app is not responding. Please check that the program is started and that you have internet access. If you do not have a valid BankID you can get one from your bank. Then try again.",
                "rfa9": "Enter your security code in the BankID app and select Identify or Sign.",
                "rfa9-auth": "Enter your security code in the BankID app and select Identify.",
                "rfa9-sign": "Enter your security code in the BankID app and select Sign.",
                "rfa13": "Trying to start your BankID app.",
                "rfa14": "Searching for BankID:s, it may take a little while... If a few seconds have passed and still no BankID has been found, you probably do not have a BankID which can be used on this device. If you do not have a BankID you can order one from your Internet bank.", 
                "rfa16": "The BankID you are trying to use is revoked or too old. Please use another BankID or order a new one from your Internet bank.",
                "rfa17": "Failed to scan the QR code. Start the BankID app and scan the QR code. If you do not have the BankID app, you need to install it and order a BankID from your internet bank. Install the app from your app store or go to install.bankid.com.",
                "rfa18": "Start the BankID app",
                "rfa19": "Do you want to use your BankID on this device or on another device?",
                "rfa20": "Do you want to use your BankID on this device or on another device?",
                "rfa21": "Identification or signing in progress.",
                "rfa21-auth": "Identification in progress.",
                "rfa21-sign": "Signing in progress.",
                "rfa22": "Unknown error. Please try again.",
                "rfa23": "Process your machine-readable travel document using the BankID app.",
                "ext1": "Should BankID be started using QR code or personal identity number?",
                "ext2": "Start the BankID app and scan the QR code.",
                "ext3": "Since you are not using Safari the BankID app can not be automatically started. Start the app manually, and afterwards switch back to the application.",
                "blank": "",
                "btn-this": "BankID on this device",
                "btn-other": "Mobile BankID on other device",
                "btn-cancel": "Cancel",
                "btn-autostart": "Click here if the BankID app did not start automatically within 5 seconds.",
                "btn-error-continue": "Continue",
                "cancel-progress": "Stopping authentication ...",
                "contact": "If this is a recurring error please contact support",
                "error-page-close": "Please close this web browser window when you are done",
                "copyright": "Copyright © Service provided by Sweden Connect",
                error: {
                    "timeout": "The time for starting a new operation has expired. Please try again.",
                    "service": "This service is currently experiencing some issues, please try again later",
                    "unknown": "Something went wrong. Please try again later.",
                    "sign": "Signature is invalid. Please try again later.",
                    "server": "The underlying BankID service has responded with errors, please try again later"
                }
            }
        }
    },
    sv: {
        "idp": {
            "error": {
                "unrecoverable": {
                    "internal": "Ett internt fel har inträffat",
                    "unknown-peer": "Avsändaren av autentiseringsförsöket är inte registrerad hos IdP-leverantören",
                    "replay": "Autentiseringsförsöket har redan blivit behandlat",
                    "too-old": "Autentiseringsförsöket är för gammalt och kan inte accepteras",
                    "acs": "Assertion Consumer Service är inte registrerad",
                    "no-signature": "Autentiseringsförsöket är inte signerat",
                    "bad-signature": "Autentiseringsförsökets signatur kunde inte valideras",
                    "format": "Autentiseringsförsökets format är felaktigt",
                    "decode": "Det mottagna meddelandet går ej att avläsa till ett giltigt autentiseringsresultat",
                    "endpoint": "Endpoint informationen från autentiseringsförsöket är inte samma som den endpoint i meddelandet",
                    "session": "Obligatorisk sessionsdata kunde ej hittas."
                }
            }
        },
        "bankid": {
            "msg": {
                "rp-auth": "har begärt att du legitmerar dig.",
                "rp-sign": "har begärt din underskrift.",
                "rfa1": "Starta BankID-appen.",
                "rfa2": "Du har inte BankID-appen installerad. Kontakta din internetbank.",
                "rfa3": "Åtgärden avbruten. Försök igen.",
                "rfa4": "En identifiering eller underskrift för det här personnumret är redan påbörjad. Försök igen.",
                "rfa5": "Internt tekniskt fel. Försök igen.",
                "rfa6": "Åtgärden avbruten.",
                "rfa8": "BankID-appen svarar inte. Kontrollera att den är startad och att du har internetanslutning. Om du inte har något giltigt BankID kan du hämta ett hos din bank. Försök sedan igen.",
                "rfa9": "Skriv in din säkerhetskod i BankID- appen och välj Identifiera eller Skriv under.",
                "rfa9-auth": "Skriv in din säkerhetskod i BankID-appen och välj Identifiera",
                "rfa9-sign": "Skriv in din säkerhetskod i BankID-appen och välj Skriv under.",
                "rfa13": "Försöker starta BankID-appen.",
                "rfa14": "Söker efter BankID, det kan ta en liten stund ... Om det har gått några sekunder och inget BankID har hittats har du sannolikt inget BankID som går att använda på den här enheten. Om du inte har något BankID kan du hämta ett hos din internetbank.",
                "rfa16": "Det BankID du försöker använda är för gammalt eller spärrat. Använd ett annat BankID eller hämta ett nytt hos din internetbank.",
                "rfa17": "Misslyckades att läsa av QR koden. Starta BankID-appen och läs av QR koden. Om du inte har BankID-appen måste du installera den och hämta ett BankID hos din internetbank. Installera appen från din appbutik eller besök install.bankid.com.",
                "rfa18": "Starta BankID-appen",
                "rfa19": "Vill du använda BankID på den här enheten eller på en annan enhet?",
                "rfa20": "Vill du använda BankID på den här enheten eller på en annan enhet?",
                "rfa21": "Identifiering eller underskrift pågår.",
                "rfa21-auth": "Identifiering pågår.",
                "rfa21-sign": "Underskrift pågår.",
                "rfa22": "Okänt fel. Försök igen.",
                "rfa23": "Fotografera och läs av din ID-handling med BankID-appen.",
                "ext1": "Ska BankID startas med QR-kod eller genom inmatning av personnummer?",
                "ext2": "Starta BankID-appen och läs av QR-koden.",
                "ext3": "Eftersom du inte använder Safari kan BankID-appen inte startas automatiskt. Starta BankID-appen, och gå senare tillbaka till applikationen.",
                "blank": "",
                "btn-this": "BankID på den här enheten",
                "btn-other": "Mobilt BankID på annan enhet",
                "btn-cancel": "Avbryt",
                "btn-autostart": "Klicka här om BankID-appen inte startar inom 5 sekunder.",
                "btn-error-continue": "Fortsätt",
                "cancel-progress": "Avbryter ...",
                "contact": "Om detta är ett återkommande problem så kontakta support via e-post",
                "error-page-close": "Vänligen stäng sidan när du är klar",
                "copyright": "Copyright © Tjänsten levereras av Sweden Connect",
                error: {
                    "timeout": "Det tog för lång tid att starta en operation. Vänligen tryck på Fortsätt och försök igen.",
                    "service": "Just nu har vi driftstörningar, försök igen senare",
                    "unknown": "Någonting har gått fel, försök igen senare",
                    "sign": "Signaturen är inte korrekt, försök igen senare",
                    "server": "Den underliggande tjänsten svarar med fel, försök igen senare"
                }
            }
        }
    }
}