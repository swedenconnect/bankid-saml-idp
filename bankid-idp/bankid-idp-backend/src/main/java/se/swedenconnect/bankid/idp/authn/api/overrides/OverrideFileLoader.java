package se.swedenconnect.bankid.idp.authn.api.overrides;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.swedenconnect.bankid.idp.config.OverrideProperties;

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

@AllArgsConstructor
public class OverrideFileLoader {

  private final List<CssOverride> cssOverrides = new ArrayList<>();

  private final List<MessageOverride> messageOverrides = new ArrayList<>();

  private final List<ContentOverride> contentOverrides = new ArrayList<>();

  private final ObjectMapper mapper;

  public static final String VALID_FILE_ENDINGS = "^.*\\.(css|content|messages)$";

  public List<CssOverride> getCssOverrides() {
    return List.copyOf(cssOverrides);
  }

  public List<MessageOverride> getMessageOverrides() {
    return List.copyOf(messageOverrides);
  }

  public List<ContentOverride> getContentOverrides() {
    return List.copyOf(contentOverrides);
  }

  public OverrideFileLoader(final OverrideProperties properties, ObjectMapper mapper) {
    this.mapper = mapper;
    String directoryPath = properties.getDirectoryPath();
    if (directoryPath == null) {
      return;
    }
    try (Stream<Path> walk = Files.walk(new File(directoryPath).toPath());) {
      walk.filter(p -> !Files.isDirectory(p))
          .filter(p -> p.toAbsolutePath().toString().matches(VALID_FILE_ENDINGS))
          .map(p -> p.toString().toLowerCase())
          .forEach(filePath -> {
            Matcher matcher = Pattern.compile(VALID_FILE_ENDINGS).matcher(filePath);
            if (matcher.matches()) {
              String group = matcher.group(1);
              switch (group) {
                case "css" -> cssOverrides.add(new CssOverride(readFileContents(filePath)));
                case "content" -> {
                  List<ContentOverride> contentOverridesRead = null;
                  try {
                    contentOverridesRead = this.mapper.readerFor(List.class).readValue(readFileContents(filePath));
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                  contentOverrides.addAll(contentOverridesRead);
                }
                case "messages" -> {
                  List<MessageOverride> messageOverridesRead = null;
                  try {
                    messageOverridesRead = this.mapper.readerFor(List.class).readValue(readFileContents(filePath));
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                  messageOverrides.addAll(messageOverridesRead);
                }
              }
            }
          });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }



  private String readFileContents(String filepath) {
    try (FileInputStream fis = new FileInputStream(filepath)) {
      return new String(fis.readAllBytes());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
