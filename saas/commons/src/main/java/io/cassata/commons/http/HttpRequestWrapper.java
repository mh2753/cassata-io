/*
 * Copyright (c) 2018. cassata.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.cassata.commons.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestWrapper {

    private HttpURLConnection httpURLConnection;
    private Map<String, String> headers = new HashMap<String, String>();
    private HttpRequestType requestType;
    private int connectionTimeout = 5000;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }


    private HttpRequestWrapper(String url,
                               HttpRequestType requestType,
                               Map<String, String> headers) {

        this.headers = headers;
        URL endpoint;
        try {

            endpoint = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to parse URL: " + url, e);
        }

        httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection)endpoint.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            httpURLConnection.setRequestMethod(requestType.name());
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setDoOutput(true);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse execute(String body) throws ConnectException {
        for (Map.Entry<String, String> entry: headers.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        try {

            DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
            os.writeBytes(body);
            os.flush();
            os.close();

            InputStream in = httpURLConnection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            String responseString = response.toString();

            return new HttpResponse(responseString, httpURLConnection.getResponseCode());
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to server", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public HttpResponse execute(Object requestObject) throws ConnectException {

        ObjectMapper objectMapper = new ObjectMapper();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(requestObject);
        } catch (JsonProcessingException e) {
            return null;
        }

        return execute(body);
    }

    public static class Builder {
        private String url;
        private Map<String, String> headers;
        private HttpRequestType httpRequestType;
        private final String defaultHeaderName = "Content-Type";
        private final String defaultHeaderValue = "application/json";

        public Builder(String url) {
            this.url = url;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withRequestType(HttpRequestType requestType) {
            this.httpRequestType = requestType;
            return this;
        }

        public HttpRequestWrapper build() {
            if (httpRequestType == null) {
                httpRequestType = HttpRequestType.POST;
            }
            if (headers == null) {
                headers = new HashMap<String, String>();
                headers.put(defaultHeaderName, defaultHeaderValue);
            }

            return new HttpRequestWrapper(url, httpRequestType, headers);
        }
    }
}
