/*
 * The Pennsylvania State University © 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.client.rest;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.springframework.http.ResponseEntity;


public final class RestClientUtil {

  private RestClientUtil() {

  }

  public static void checkForSuccess(Response response) throws RestException{
    if (!isSuccessful(response)) {
      throw new RestException(response);
    }
  }
  
  public static boolean checkForFourOhFour(WebTarget target, Response response) {
    try {
      verifyNotFourOhFour(target, response);
      return false;
    } catch (RestException e) {
      return true;
    }
  }
  
  public static void verifyNotFourOhFour(WebTarget target, Response response) throws RestException {
    if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      throw new RestException(response);
    }
  }

  public static boolean isSuccessful(Response response) {
    boolean isSuccessful;
    Family responseFamily = response.getStatusInfo()
                                    .getFamily();
    isSuccessful = responseFamily != Family.CLIENT_ERROR && responseFamily != Family.SERVER_ERROR;

    return isSuccessful;
  }

  /**
   * Closes <code>response</code> and suppresses any known/expected exceptions
   * from closing it.
   * 
   * @param response
   */
  public static void close(Response response) {
    try {
      response.close();
    } catch (ProcessingException ignored) {
    }
  }

  /**
   * Closes <code>response</code> and passes any known/expected exceptions from
   * closing it to <code>consumer</code> (e.g. for logging).
   * 
   * @param response
   * @param consumer
   */
  public static void close(Response response, Consumer<Throwable> consumer) {
    try {
      response.close();
    } catch (ProcessingException processingException) {
      consumer.accept(processingException);
    }
  }

  /**
   * Read an entity from the response if it was found and returned.
   * 
   * @param response
   *          the {@link Response} to read from
   * @param entityType
   *          the type of entity
   * @return <code>Optional.empty()</code> if <b>Not Found</b> or empty
   *         response, otherwise <code>Optional.ofNullable(T)</code>
   * @throws RestException
   *           if <code>response</code> is an error response other than
   *           <code>404 Not Found</code>
   * @throws ProcessingException
   *           see {@link Response#readEntity(Class)}
   * @throws IllegalStateException
   *           see {@link Response#readEntity(Class)}
   * @throws RestException
   */
  public <T> Optional<T> tryReadEntity(Response response, Class<T> entityType) throws RestException, ProcessingException, IllegalStateException{
    return readEntity(response, entityType, response::readEntity, Optional::ofNullable);
  }

  /**
   * Read an entity from the response if it was found and returned.
   * 
   * @param response
   *          the {@link Response} to read from
   * @param entityType
   *          the type of entity
   * @return <code>Optional.empty()</code> if <code>Not Found</code> or empty
   *         response, otherwise <code>Optional.ofNullable(T)</code>
   * @throws RestException
   *           if <code>response</code> is an error response other than
   *           <code>404 Not Found</code>
   * @throws ProcessingException
   *           see {@link Response#readEntity(GenericType)}
   * @throws IllegalStateException
   *           see {@link Response#readEntity(GenericType)}
   * @throws RestException
   */
  public <T> Optional<T> tryReadEntity(Response response, GenericType<T> entityType) throws RestException, ProcessingException, IllegalStateException {
    return readEntity(response, entityType, response::readEntity, Optional::ofNullable);
  }

  /**
   * <p>
   * Read an entity from the response if it was found.
   * </p>
   * <p>
   * Useful for REST endpoints that <b>MUST</b> return an entity.
   * </p>
   * 
   * @param response
   *          the {@link Response} to read from
   * @param entityType
   *          the type of entity
   * @return <code>Optional.empty()</code> if <code>Not Found</code>, otherwise
   *         <code>Optional.of(T)</code>
   * @throws RestException
   *           if <code>response</code> is an error response other than
   *           <code>404 Not Found</code>
   * @throws ProcessingException
   *           see {@link Response#readEntity(Class)}
   * @throws IllegalStateException
   *           see {@link Response#readEntity(Class)}
   * @throws RestException
   */
  public static <T> Optional<T> readEntity(Response response, Class<T> entityType) throws RestException, ProcessingException, IllegalStateException{
    return readEntity(response, entityType, response::readEntity, Optional::of);
  }

  /**
   * <p>
   * Read an entity from the response if it was found.
   * </p>
   * <p>
   * Useful for REST endpoints that <b>MUST</b> return an entity.
   * </p>
   * 
   * @param response
   *          the {@link Response} to read from
   * @param entityType
   *          the type of entity
   * @return <code>Optional.empty()</code> if <code>Not Found</code>, otherwise
   *         <code>Optional.of(T)</code>
   * @throws RestException
   *           if <code>response</code> is an error response other than
   *           <code>404 Not Found</code>
   * @throws ProcessingException
   *           see {@link Response#readEntity(GenericType)}
   * @throws IllegalStateException
   *           see {@link Response#readEntity(GenericType)}
   * @throws RestException
   */
  public static <T> Optional<T> readEntity(Response response, GenericType<T> entityType) throws RestException, ProcessingException, IllegalStateException{
    return readEntity(response, entityType, response::readEntity, Optional::of);
  }

  private static <T, E> Optional<E> readEntity(Response response, T entityType, Function<T, E> readEntity, Function<E, Optional<E>> optionalOf) throws RestException, ProcessingException, IllegalStateException{
    Optional<E> result;

    if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      result = Optional.empty();
    } else {
      checkForSuccess(response);

      E responseEntity = readEntity.apply(entityType);
      result = optionalOf.apply(responseEntity);
    }
    return result;
  }
  
  public static Optional<String> extractIdFromLocationTag(Response response) {
    String location = response.getHeaderString("Location");
    if (location == null) {
      return Optional.empty();
    }
    
    String[] uriParts = location.split("/");
    Integer nbrParts = uriParts.length;
    return Optional.of(uriParts[nbrParts - 1]);
  }
}
