package se.swedenconnect.bankid.idp.authn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerContactInformationFactoryTest {
  @Test
  void customerContactInfoHiddenWhenConfigured() {
    CustomerContactInformationFactory customerContactInformationFactory = new CustomerContactInformationFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    CustomerContactInformation contactInformation = customerContactInformationFactory.getContactInformation();
    Assertions.assertEquals("", contactInformation.getEmail());
    Assertions.assertEquals(false, contactInformation.getDisplayInformation());
  }

  @Test
  void customerContactInfoShowWhenConfigured() {
    CustomerContactInformationFactory customerContactInformationFactory = new CustomerContactInformationFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    CustomerContactInformation contactInformation = customerContactInformationFactory.getContactInformation();
    Assertions.assertEquals(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE.getContactEmail(), contactInformation.getEmail());
    Assertions.assertEquals(true, contactInformation.getDisplayInformation());
  }

}