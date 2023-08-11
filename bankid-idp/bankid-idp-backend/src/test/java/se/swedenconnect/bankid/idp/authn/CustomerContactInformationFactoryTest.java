package se.swedenconnect.bankid.idp.authn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerContactInformationFactoryTest {
  @Test
  void customerContactInfoHiddenWhenConfigured() {
    CustomerContactInformationFactory customerContactInformationFactory = new CustomerContactInformationFactory(new UserErrorProperties("", false, false));
  }

}