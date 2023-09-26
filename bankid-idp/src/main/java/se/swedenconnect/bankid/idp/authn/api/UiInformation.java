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
package se.swedenconnect.bankid.idp.authn.api;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents information about what to display in the UI.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 * @author Mattias Kesti
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UiInformation {

  /**
   * Information about connecting SP (service provider).
   */
  private SpInformation sp;

  /**
   * Tells whether we should display an intermediate view before displaying the QR-code. This page/view
   * will contain extra help texts.
   */
  @Builder.Default
  private boolean displayQrHelp = false;

  /**
   * Holds the size of the QR code in pixels.
   */
  @Builder.Default
  private int qrSize = 200;

  /**
   * Swedish public e-services are required to include a link to the "accessibility report" (tillgänglighetsrapport) of
   * their web site. If this property is assigned, the UI will display this link at the bottom of the page.
   */
  private String accessibilityReportLink;

  /**
   * The name of the provider of the service delivered as a map where keys are language codes and values the name(s). If
   * {@code null}, elements that this information should be displayed should not be included in the UI.
   */
  private Map<String, String> providerName;

}
