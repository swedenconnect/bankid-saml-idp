/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 */
package se.swedenconnect.bankid.idp.authn;

import static se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController.AUTHN_PATH;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * If we are running in "standalone" mode, i.e., if we are using the built in Vue frontend app, this controller
 * redirects calls made from the underlying SAML IdP library to our frontend start page.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestController
@ConditionalOnProperty(value = "bankid.built-in-frontend", havingValue = "true", matchIfMissing = true)
public class FrontendController {

  /**
   * The entry point for the BankID authentication/signature process.
   *
   * @return an HTML string of the frontend
   */
  @GetMapping(value = AUTHN_PATH, produces = MediaType.TEXT_HTML_VALUE)
  public @ResponseBody byte[] view() throws IOException {
    final ClassPathResource classPathResource = new ClassPathResource("templates/index.html");
    return IOUtils.toByteArray(classPathResource.getInputStream());
  }
}
