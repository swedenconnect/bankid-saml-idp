/*
 * Copyright 2023-2024 Sweden Connect
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
package se.swedenconnect.bankid.rpapi.support.useragent;

/**
 * An interface for representing user agent information.
 * 
 * @author Martin Lindström
 */
public interface UserAgent {

  /**
   * Returns the type of the device the user has, for example "mobile" or "desktop".
   * 
   * @return user device type
   */
  UserDeviceType getUserDeviceType();

  /**
   * Returns the "User-Agent" header received from when the user connected to the service.
   * 
   * @return the "User-Agent" header
   */
  String getUserAgentHeader();

  /**
   * Returns the IP address for the user.
   * 
   * @return the IP address
   */
  String getUserIpAddress();

  /**
   * Predicate that indicates if the "User-Agent" header indicates if the platform is iOS.
   *
   * @return if the "User-Agent" header indicates iOS true is returned, and otherwise false.
   */
  boolean is_iOS();

  /**
   * Predicate that indicates if the "User-Agent" header indicates the Safari mobile web browser (not embedded in
   * another app).
   * <p>
   * The reason that we need special handling of iOS and Safari is that the BankID app needs to be started with an extra
   * parameter for those cases.
   * </p>
   * 
   * @return true if the "User-Agent" header indicates that Safari is used and false otherwise
   */
  boolean isNonEmbeddedMobileSafari();

  /**
   * If the user uses an embedded browser in Facebook, Twitter or any other app, the BankID app will be blocked from
   * starting since iOS requires an app to whitelist all URL-schemes that an app should be able to invoke. And BankID is
   * pretty unknown to the major apps, so in those cases we need to ask the user to manually start the app. This
   * predicate checks if the "User-Agent" header indicates that an embedded browser is in use.
   * 
   * @return true if the "User-Agent" header indicates that an embedded browser is used and false otherwise
   */
  boolean isEmbeddedBrowser();
}
