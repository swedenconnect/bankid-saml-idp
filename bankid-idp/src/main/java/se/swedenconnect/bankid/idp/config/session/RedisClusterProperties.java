/*
 *
 *  * Copyright 2023-${year} Sweden Connect
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package se.swedenconnect.bankid.idp.config.session;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for containing additional redis cluster properties
 */
public class RedisClusterProperties {

  @Getter
  @Setter
  private List<NatTranslationEntry> natTranslation;

  public Map<String, String> getNatTable() {
    return natTranslation.stream()
        .collect(Collectors.toMap(NatTranslationEntry::getFrom, NatTranslationEntry::getTo));
  }

  public static class NatTranslationEntry {

    @Getter
    @Setter
    private String from;
    @Getter
    @Setter
    private String to;
  }
}
