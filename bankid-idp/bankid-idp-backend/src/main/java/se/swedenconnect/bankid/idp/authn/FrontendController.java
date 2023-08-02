package se.swedenconnect.bankid.idp.authn;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController.AUTHN_PATH;

@RestController
@ConditionalOnProperty(value = "bankid.standalone", havingValue = "true", matchIfMissing = true)
public class FrontendController {
    /**
     * The entry point for the BankID authentication/signature process.
     *
     * @return a {@link ModelAndView}
     */
    @GetMapping(AUTHN_PATH)
    public ModelAndView view() {
        return new ModelAndView("index");
    }
}
