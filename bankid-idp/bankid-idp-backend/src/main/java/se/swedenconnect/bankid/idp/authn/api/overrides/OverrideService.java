package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
