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
package se.swedenconnect.bankid.rpapi.types;

/**
 * An interface defining the response from collect() calls.
 * 
 * @author Martin Lindström
 */
public interface CollectResponse {

  /**
   * Returns the progess status of the operation.
   * 
   * @return the progress status
   */
  ProgressStatus getProgressStatus();

  /**
   * Returns the {@code orderRef} corresponding to the auth or sign call.
   * 
   * @return the order reference
   */
  String getOrderReference();

  /**
   * If the progress status is {@link ProgressStatus#COMPLETE} this method will return the completion data.
   * 
   * @return the completion data
   */
  CompletionData getCompletionData();

}
