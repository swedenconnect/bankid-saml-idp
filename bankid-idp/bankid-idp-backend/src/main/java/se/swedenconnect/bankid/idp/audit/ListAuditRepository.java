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
package se.swedenconnect.bankid.idp.audit;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class ListAuditRepository implements AuditEventRepository {

  private final RedissonClient client;

  private final AuditEventMapper mapper;

  @Override
  public void add(final AuditEvent event) {
    client.getList("audit:list").add(event);
  }

  @Override
  public List<AuditEvent> find(final String principal, final Instant after, final String type) {
    return client.getList("audit:list").stream()
        .map(String.class::cast)
        .map(mapper::read)
        .toList();
  }
}
