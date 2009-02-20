/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.client.apache.impl;

import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpMethodTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }
               
        @POST
        public String post(String entity) {
            return entity;
        }
        
        @PUT
        public String put(String entity) {
            return entity;
        }
        
        @DELETE
        public String delete() {
            return "DELETE";
        }

        @POST
        @Path("noproduce")
        public void postNoProduce(String entity) {
        }

        @POST
        @Path("noconsumeproduce")
        public void postNoConsumeProduce() {
        }
    }
        
    public HttpMethodTest(String testName) {
        super(testName);
    }
    
    public void testHead() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        ClientResponse cr = r.head();
        assertFalse(cr.hasEntity());
    }

    public void testOptions() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        ClientResponse cr = r.options(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));

        ClientResponse cr = r.get(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }
    
    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }    
    
    public void testPostChunked() {
//        startServer(HttpMethodResource.class);
        ResourceConfig rc = new DefaultResourceConfig(HttpMethodResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 1024);
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostVoid() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            r.post("POST");
        }
    }

    public void testPostNoProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        ClientResponse cr = r.path("noproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPostNoConsumeProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());

        ClientResponse cr = r.path("noconsumeproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPut() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT"));

        ClientResponse cr = r.put(ClientResponse.class, "PUT");
        assertTrue(cr.hasEntity());
        cr.close();
    }
    
    public void testDelete() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));

        ClientResponse cr = r.delete(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }
    
    public void testAll() {
        startServer(HttpMethodResource.class);
        WebResource r = ApacheHttpClient.create().resource(getUri().path("test").build());

        assertEquals("GET", r.get(String.class));

        assertEquals("POST", r.post(String.class, "POST"));
        
        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());
        
        assertEquals("PUT", r.post(String.class, "PUT"));
        
        assertEquals("DELETE", r.delete(String.class));
    }
}
