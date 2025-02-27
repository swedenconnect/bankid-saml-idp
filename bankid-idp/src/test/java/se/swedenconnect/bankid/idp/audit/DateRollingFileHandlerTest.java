/*
 * Copyright 2023-2025 Sweden Connect
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
package se.swedenconnect.bankid.idp.audit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Test cases for DateRollingFileHandler.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DateRollingFileHandlerTest {

  private static final String LOG_FILE = "target/file-audit.log";
  private static final String LOG_FILE2 = "target/audit/audit.log";

  @BeforeEach
  public void setup() throws Exception {
    this.removeLogFile(LOG_FILE);
    this.removeLogFile(LOG_FILE2);
  }

  @AfterEach
  public void cleanup() throws Exception {
    this.removeLogFile(LOG_FILE);
    this.removeLogFile(LOG_FILE2);
  }

  @Test
  public void testLogging() throws Exception {
    DateRollingFileHandler handler = new DateRollingFileHandler(LOG_FILE);
    Logger auditLogger = Logger.getLogger("audit");
    auditLogger.setLevel(Level.INFO);
    auditLogger.addHandler(handler);
    auditLogger.setUseParentHandlers(false);

    String event = "This is entry #1";

    auditLogger.log(Level.INFO, event);

    final Path logFile = Path.of(LOG_FILE);
    List<String> lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.size() == 1);
    Assertions.assertEquals(event, lines.get(0));

    handler.close();

    // Again. Now with an existing file ...
    handler = new DateRollingFileHandler(LOG_FILE);
    auditLogger = Logger.getLogger("audit");
    auditLogger.setLevel(Level.INFO);
    auditLogger.addHandler(handler);
    auditLogger.setUseParentHandlers(false);

    event = "This is entry #2";

    auditLogger.log(Level.INFO, event);

    lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.size() == 2);
    Assertions.assertEquals(event, lines.get(1));

    // With a level that we don't log ...
    LogRecord record = new LogRecord(Level.FINE, "Should not be logged");
    handler.publish(record);
    handler.publish(null);

    lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.size() == 2);

    handler.close();
  }

  @Test
  public void testCreateDirectories() throws Exception {
    DateRollingFileHandler handler = new DateRollingFileHandler(LOG_FILE2);
    Logger auditLogger = Logger.getLogger("audit");
    auditLogger.setLevel(Level.INFO);
    auditLogger.addHandler(handler);
    auditLogger.setUseParentHandlers(false);

    String event = "This is entry #1";

    auditLogger.log(Level.INFO, event);

    final Path logFile = Path.of(LOG_FILE2);
    List<String> lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.size() == 1);
    Assertions.assertEquals(event, lines.get(0));

    handler.flush();
    handler.close();
  }

  @Test
  public void testBackup() throws Exception {

    DateRollingFileHandler handler = new DateRollingFileHandler(LOG_FILE);
    Logger auditLogger = Logger.getLogger("audit");
    auditLogger.setLevel(Level.INFO);
    auditLogger.addHandler(handler);
    auditLogger.setUseParentHandlers(false);

    final String event = "This is entry #1";
    auditLogger.log(Level.INFO, event);

    handler.flush();
    handler.close();

    final Path logFile = Path.of(LOG_FILE);
    final Instant old = Instant.parse("2021-12-24T23:30:30.00Z");

    Files.setLastModifiedTime(logFile, FileTime.from(old));

    handler = new DateRollingFileHandler(LOG_FILE);
    auditLogger = Logger.getLogger("audit");
    auditLogger.setLevel(Level.INFO);
    auditLogger.addHandler(handler);
    auditLogger.setUseParentHandlers(false);

    String event2 = "This is an entry from today";
    auditLogger.log(Level.INFO, event2);

    List<String> lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.size() == 1);
    Assertions.assertEquals(event2, lines.get(0));

    lines = Files.readAllLines(Path.of("target/file-audit-20211224.log"));
    Assertions.assertTrue(lines.size() == 1);
    Assertions.assertEquals(event, lines.get(0));

    handler.close();

    this.removeLogFile("target/file-audit-20211224.log");
  }

  @Test
  public void testLogFileIsDirectory() throws Exception {
    assertThatThrownBy(() -> {
      new DateRollingFileHandler("target");
    }).isInstanceOf(IOException.class)
        .hasMessage("Given logFile points to a directory and not a file");
  }

  private void removeLogFile(final String file) throws IOException {
    final Path logFile = Path.of(file);
    Files.deleteIfExists(logFile);
  }

}
