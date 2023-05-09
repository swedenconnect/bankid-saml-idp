/*
 * Copyright 2023 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import se.swedenconnect.bankid.idp.config.UiConfigurationProperties.Language;

/**
 * Web MVC configuration.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties(UiConfigurationProperties.class)
public class WebMvcConfiguration implements WebMvcConfigurer {

  /** UI settings. */
  private final UiConfigurationProperties ui;

  /**
   * Constructor.
   * 
   * @param ui the UI configuration
   */
  public WebMvcConfiguration(final UiConfigurationProperties ui) {
    this.ui = Objects.requireNonNull(ui, "ui must not be null");
  }

  /**
   * Creates a bean holding the UI languages.
   * 
   * @return the UI languages
   */
  @Bean
  List<Language> languages() {
    return this.ui.getLanguages();
  }

  /**
   * Creates a {@link LocaleResolver} for resolving which language to use in the UI.
   * 
   * @param contextPath the servlet context path
   * @return a {@link LocaleResolver}
   */
  @Bean
  LocaleResolver localeResolver(@Value("${server.servlet.context-path}") String contextPath) {
    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(new Locale("en"));
    resolver.setCookiePath(contextPath);
    resolver.setCookieMaxAge(31536000);
    return resolver;
  }

  /**
   * Creates a {@link LocaleChangeInterceptor} for changing the locale based on a request parameter name.
   * 
   * @return a {@link LocaleChangeInterceptor}
   */
  @Bean
  LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    interceptor.setParamName("lang");
    return interceptor;
  }

  /**
   * Adds the configured {@link LocaleChangeInterceptor}.
   */
  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(this.localeChangeInterceptor());
  }

}
