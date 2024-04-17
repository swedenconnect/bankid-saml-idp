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
package se.swedenconnect.bankid.rpapi.service;

/**
 * Testing the {@link BankIDClient}.
 *
 * @author Martin Lindström
 */
public class BankIDClientTest {
  
  // private RestTemplate restTemplate = new RestTemplate();
  
//  private MockRestServiceServer mockServer;
//  
//  private BankIDClientImpl client;
//  
//  private static final String BANKID_URL = "https://appapi2.bankid.com/rp/v5.1";
//  
//  @Before
//  public void setup() {
//    RestGatewaySupport gateway = new RestGatewaySupport();
//    gateway.setRestTemplate(this.restTemplate);
//    this.mockServer = MockRestServiceServer.createServer(gateway);
//    
//    this.client = new BankIDClientImpl(this.restTemplate, BANKID_URL, null);
//  }
//
//  @Test
//  public void testAuthenticate() throws Exception {
//    
//    String responseBytes = "{ \"orderRef\" : \"131daac9-16c6-4618-beb0-365768f37288\", \"autoStartToken\" : \"7c40b5c9-fa74-49cf-b98c-bfe651f9a7c6\", " 
//        +  "\"qrStartToken\" : \"67df3917-fa0d-44e5-b327-edcc928297f8\", \"qrStartSecret\": \"d28db9a7-4cde-429e-a983-359be676944c\" }";
//    
//    this.mockServer.expect(MockRestRequestMatchers.requestTo(BANKID_URL + "/auth"))
//      .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
//      .andExpect(MockRestRequestMatchers.header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
//      .andRespond(MockRestResponseCreators.withSuccess(responseBytes, MediaType.APPLICATION_JSON));
//    
//    OrderResponse response = this.client.authenticate("196911292032", "85.228.133.223", null);
//    Assert.assertEquals("131daac9-16c6-4618-beb0-365768f37288", response.getOrderReference());
//    Assert.assertNotNull(response.getOrderTime());
//    Assert.assertEquals("7c40b5c9-fa74-49cf-b98c-bfe651f9a7c6", response.getAutoStartToken());
//    Assert.assertEquals("67df3917-fa0d-44e5-b327-edcc928297f8", response.getQrStartToken());
//    Assert.assertEquals("d28db9a7-4cde-429e-a983-359be676944c", response.getQrStartSecret());
//  }
}
