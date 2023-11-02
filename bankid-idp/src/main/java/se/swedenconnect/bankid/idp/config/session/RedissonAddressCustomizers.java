/*
 *
 *  * Copyright 2023 Sweden Connect
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

import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Customizers to handle a bug where the protocol section of the address becomes non-tls when tls is enabled
 */
public class RedissonAddressCustomizers {
  public static BiFunction<ClusterServersConfig, RedisClusterProperties, ClusterServersConfig> clusterServerCustomizer = (config, clusterProperties) -> {
    List<String> addresses = new ArrayList<>();
    config.getNodeAddresses().forEach(address -> {
      String addr = address;
      if (address.contains("redis://")) {
        addr = address.replace("redis://", "rediss://");
      }
      addresses.add(addr);
    });
    config.setNodeAddresses(addresses);
    Map<String, String> natTable = clusterProperties.getNatTable();
    config.setNatMap(natTable);
    return config;
  };

  public static Function<SingleServerConfig, SingleServerConfig> singleServerSslCustomizer = (s) -> {
    String redisAddress = s.getAddress();
    if (redisAddress.contains("redis://")) {
      // The protocol part has not been configured by spring even though we have enabled ssl
      s.setAddress(redisAddress.replace("redis://", "rediss://"));
    }
    return s;
  };
}
