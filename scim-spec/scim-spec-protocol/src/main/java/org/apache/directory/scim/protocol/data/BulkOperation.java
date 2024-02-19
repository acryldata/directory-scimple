/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.protocol.data;

import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.ScimResource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;


@Data
@XmlType(propOrder = {"method","path","bulkId","data"})
@XmlAccessorType(XmlAccessType.NONE)
public class BulkOperation implements Serializable {

  private static final long serialVersionUID = 6528874816710788132L;

  public enum Method {
    @XmlEnumValue("POST") POST,
    @XmlEnumValue("PUT") PUT,
    @XmlEnumValue("PATCH") PATCH,
    @XmlEnumValue("DELETE") DELETE;
  }
  
  @Data
  @AllArgsConstructor
  @XmlAccessorType(XmlAccessType.NONE)
  public static class StatusWrapper implements Serializable {

    private static final long serialVersionUID = 1544738718748608248L;

    public static StatusWrapper wrap(HttpStatus code) {
      return new StatusWrapper(code);
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(StatusAdapter.class)
    HttpStatus code;
  }

  @XmlElement
  Method method;
  
  @XmlElement
  String bulkId;
  
  @XmlElement
  String version;
  
  @XmlElement
  String path;
  
  @XmlElement
  ScimResource data;
  
  @XmlElement
  String location;
  
  @XmlElement
  BaseResource response;
  
  @XmlElement
  StatusWrapper status;
}
