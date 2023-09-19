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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.swedenconnect.bankid.idp.config.OverrideProperties;

/**
 * The {@code OverrideFileLoader} is responsible of
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class OverrideFileLoader {

  private final List<CssOverride> cssOverrides = new ArrayList<>();

  private final List<MessageOverride> messageOverrides = new ArrayList<>();

  private final List<ContentOverride> contentOverrides = new ArrayList<>();

  private final ObjectMapper mapper;

  public static final String VALID_FILE_ENDINGS = "^.*\\.(css|content|messages)$";

  /**
   * Constructor setting up the file loader.
   *
   * @param properties the overrides properties
   * @param mapper a JSON mapper
   */
  public OverrideFileLoader(final OverrideProperties properties, final ObjectMapper mapper) {
    this.mapper = mapper;
    final String directoryPath = properties.getDirectoryPath();
    if (directoryPath == null) {
      return;
    }
    try (Stream<Path> walk = Files.walk(new File(directoryPath).toPath());) {
      walk.filter(p -> !Files.isDirectory(p))
          .filter(p -> p.toAbsolutePath().toString().matches(VALID_FILE_ENDINGS))
          .map(p -> p.toString().toLowerCase())
          .forEach(filePath -> {
            final Matcher matcher = Pattern.compile(VALID_FILE_ENDINGS).matcher(filePath);
            if (matcher.matches()) {
              final String group = matcher.group(1);
              switch (group) {
              case "css" -> this.cssOverrides.add(new CssOverride(this.readFileContents(filePath)));
              case "content" -> {
                List<ContentOverride> contentOverridesRead = null;
                try {
                  contentOverridesRead = this.mapper.readerFor(List.class).readValue(this.readFileContents(filePath));
                }
                catch (final JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                this.contentOverrides.addAll(contentOverridesRead);
              }
              case "messages" -> {
                List<MessageOverride> messageOverridesRead = null;
                try {
                  messageOverridesRead = this.mapper.readerFor(List.class).readValue(this.readFileContents(filePath));
                }
                catch (final JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                this.messageOverrides.addAll(messageOverridesRead);
              }
              }
            }
          });
    }
    catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private String readFileContents(final String filepath) {
    try (FileInputStream fis = new FileInputStream(filepath)) {
      return new String(fis.readAllBytes());
    }
    catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public List<CssOverride> getCssOverrides() {
    return List.copyOf(this.cssOverrides);
  }

  public List<MessageOverride> getMessageOverrides() {
    return List.copyOf(this.messageOverrides);
  }

  public List<ContentOverride> getContentOverrides() {
    return List.copyOf(this.contentOverrides);
  }

}
