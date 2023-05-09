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
package se.swedenconnect.bankid.rpapi.support;

/**
 * Defines a repository for BankID messages.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
public interface BankIDMessageRepository {

  /**
   * Based on the message ID (short name), the method finds the message.
   * 
   * @param id the short name for the message
   * @return the {@code BankIDMessage} or {@code null} if no message is found
   */
  BankIDMessage getBankIDMessage(final BankIDMessage.ShortName id);

}
