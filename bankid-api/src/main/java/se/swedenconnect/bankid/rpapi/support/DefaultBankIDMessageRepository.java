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
package se.swedenconnect.bankid.rpapi.support;

import java.util.Arrays;
import java.util.List;

import se.swedenconnect.bankid.rpapi.support.BankIDMessage.ShortName;

/**
 * Default implementation for the {@code BankIDMessageRepository} interface. The implementation uses the message codes
 * defined in this jar under bankid-messages.properties.
 *
 * @author Martin Lindström
 */
public class DefaultBankIDMessageRepository implements BankIDMessageRepository {

  /** The hardwired mappings. */
  private final List<BankIDMessage> repository = Arrays.asList(
      new BankIDMessageImpl(ShortName.RFA1, "bankid.msg.rfa1"),
      new BankIDMessageImpl(ShortName.RFA2, "bankid.msg.rfa2"),
      new BankIDMessageImpl(ShortName.RFA3, "bankid.msg.rfa3"),
      new BankIDMessageImpl(ShortName.RFA4, "bankid.msg.rfa4"),
      new BankIDMessageImpl(ShortName.RFA5, "bankid.msg.rfa5"),
      new BankIDMessageImpl(ShortName.RFA6, "bankid.msg.rfa6"),
      new BankIDMessageImpl(ShortName.RFA8, "bankid.msg.rfa8"),
      new BankIDMessageImpl(ShortName.RFA9, "bankid.msg.rfa9"),
      new BankIDMessageImpl(ShortName.RFA9_AUTH, "bankid.msg.rfa9-auth"),
      new BankIDMessageImpl(ShortName.RFA9_SIGN, "bankid.msg.rfa9-sign"),
      new BankIDMessageImpl(ShortName.RFA13, "bankid.msg.rfa13"),
      new BankIDMessageImpl(ShortName.RFA14_DESKTOP, "bankid.msg.rfa14-desktop.1", "bankid.msg.rfa14-desktop.2"),
      new BankIDMessageImpl(ShortName.RFA14_MOBILE, "bankid.msg.rfa14-mobile.1", "bankid.msg.rfa14-mobile.2"),
      new BankIDMessageImpl(ShortName.RFA15_DESKTOP, "bankid.msg.rfa15-desktop.1", "bankid.msg.rfa15-desktop.2"),
      new BankIDMessageImpl(ShortName.RFA15_MOBILE, "bankid.msg.rfa15-mobile.1", "bankid.msg.rfa15-mobile.2"),
      new BankIDMessageImpl(ShortName.RFA16, "bankid.msg.rfa16"),
      new BankIDMessageImpl(ShortName.RFA17_PNR, "bankid.msg.rfa17-pnr"),
      new BankIDMessageImpl(ShortName.RFA17_QR, "bankid.msg.rfa17-qr"),
      new BankIDMessageImpl(ShortName.RFA18, "bankid.msg.rfa18"),
      new BankIDMessageImpl(ShortName.RFA19, "bankid.msg.rfa19"),
      new BankIDMessageImpl(ShortName.RFA19_AUTH, "bankid.msg.rfa19-auth"),
      new BankIDMessageImpl(ShortName.RFA19_SIGN, "bankid.msg.rfa19-sign"),
      new BankIDMessageImpl(ShortName.RFA20, "bankid.msg.rfa20"),
      new BankIDMessageImpl(ShortName.RFA20_AUTH, "bankid.msg.rfa20-auth"),
      new BankIDMessageImpl(ShortName.RFA20_SIGN, "bankid.msg.rfa20-sign"),
      new BankIDMessageImpl(ShortName.RFA21, "bankid.msg.rfa21"),
      new BankIDMessageImpl(ShortName.RFA21_AUTH, "bankid.msg.rfa21-auth"),
      new BankIDMessageImpl(ShortName.RFA21_SIGN, "bankid.msg.rfa21-sign"),
      new BankIDMessageImpl(ShortName.RFA22, "bankid.msg.rfa22"),
      new BankIDMessageImpl(ShortName.EXT1, "bankid.msg.ext1"),
      new BankIDMessageImpl(ShortName.EXT2, "bankid.msg.ext2"));

  /** {@inheritDoc} */
  @Override
  public BankIDMessage getBankIDMessage(final ShortName id) {
    return this.repository.stream().filter(b -> b.getShortName().equals(id)).findFirst().orElse(null);
  }

}
