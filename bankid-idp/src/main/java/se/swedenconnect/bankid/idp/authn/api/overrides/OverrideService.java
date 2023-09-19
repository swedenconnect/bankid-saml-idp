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
package se.swedenconnect.bankid.idp.authn.api.overrides;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OverrideService {

  private final List<Supplier<CssOverride>> cssOverrideSuppliers;
  private final List<Supplier<MessageOverride>> messageOverrideSuppliers;
  private final List<Supplier<ContentOverride>> contentOverrideSuppliers;

  private final OverrideFileLoader fileLoader;

  public FrontendOverrideResponse generateOverrides() {
    List<CssOverride> cssOverrides = Stream.concat(getOverrides(cssOverrideSuppliers), fileLoader.getCssOverrides().stream()).toList();
    List<MessageOverride> messageOverrides = Stream.concat(getOverrides(messageOverrideSuppliers), fileLoader.getMessageOverrides().stream()).toList();
    List<ContentOverride> contentOverrides = Stream.concat(getOverrides(contentOverrideSuppliers), fileLoader.getContentOverrides().stream()).toList();
    return new FrontendOverrideResponse(messageOverrides, cssOverrides, contentOverrides);
  }

  private <T> Stream<T> getOverrides(List<Supplier<T>> overrides) {
    return overrides.stream()
        .map(Supplier::get)
        .filter(Objects::nonNull);
  }

}
