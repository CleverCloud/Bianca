/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 * Copyright (c) 2011-2012 Clever Cloud SAS -- all rights reserved
 *
 * This file is part of Bianca(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Bianca Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Bianca Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bianca Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.env;

import com.clevercloud.bianca.BiancaRequestAdapter;
import com.clevercloud.util.Base64;
import com.clevercloud.vfs.WriteStream;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents the server
 */
public class ServerArrayValue extends ArrayValueImpl {

   private static final StringValue SERVER_ADDR_V = new StringValue("SERVER_ADDR");
   private static final StringValue SERVER_NAME_V = new StringValue("SERVER_NAME");
   private static final StringValue SERVER_PORT_V = new StringValue("SERVER_PORT");
   private static final StringValue REMOTE_HOST_V = new StringValue("REMOTE_HOST");
   private static final StringValue REMOTE_ADDR_V = new StringValue("REMOTE_ADDR");
   private static final StringValue REMOTE_PORT_V = new StringValue("REMOTE_PORT");
   private static final StringValue DOCUMENT_ROOT_V = new StringValue("DOCUMENT_ROOT");
   private static final StringValue SERVER_SOFTWARE_V = new StringValue("SERVER_SOFTWARE");
   private static final StringValue SERVER_PROTOCOL_V = new StringValue("SERVER_PROTOCOL");
   private static final StringValue REQUEST_METHOD_V = new StringValue("REQUEST_METHOD");
   private static final StringValue QUERY_STRING_V = new StringValue("QUERY_STRING");
   private static final StringValue REQUEST_URI_V = new StringValue("REQUEST_URI");
   private static final StringValue REQUEST_TIME_V = new StringValue("REQUEST_TIME");
   private static final StringValue SCRIPT_URL_V = new StringValue("SCRIPT_URL");
   private static final StringValue SCRIPT_NAME_V = new StringValue("SCRIPT_NAME");
   private static final StringValue SCRIPT_FILENAME_V = new StringValue("SCRIPT_FILENAME");
   private static final StringValue PATH_INFO_V = new StringValue("PATH_INFO");
   private static final StringValue PATH_TRANSLATED_V = new StringValue("PATH_TRANSLATED");
   private static final StringValue PHP_SELF_V = new StringValue("PHP_SELF");
   private static final StringValue PHP_AUTH_USER_V = new StringValue("PHP_AUTH_USER");
   private static final StringValue PHP_AUTH_PW_V = new StringValue("PHP_AUTH_PW");
   private static final StringValue PHP_AUTH_DIGEST_V = new StringValue("PHP_AUTH_DIGEST");
   private static final StringValue AUTH_TYPE_V = new StringValue("AUTH_TYPE");
   private static final StringValue HTTPS_V = new StringValue("HTTPS");
   private static final StringValue HTTP_HOST_V = new StringValue("HTTP_HOST");
   private static final StringValue CONTENT_LENGTH_V = new StringValue("CONTENT_LENGTH");
   private static final StringValue CONTENT_TYPE_V = new StringValue("CONTENT_TYPE");
   private final Env _env;
   private boolean _isFilled;

   public ServerArrayValue(Env env) {
      _env = env;
   }

   /**
    * Converts to an object.
    */
   @Override
   public Object toObject() {
      return null;
   }

   /**
    * Adds a new value.
    */
   @Override
   public ArrayValue append(Value key, Value value) {
      if (!_isFilled) {
         fillMap();
      }

      return super.append(key, value);
   }

   /**
    * Adds a new value.
    */
   @Override
   public Value put(Value value) {
      if (!_isFilled) {
         fillMap();
      }

      return super.put(value);
   }

   /**
    * Gets a new value.
    */
   @Override
   public Value get(Value key) {
      if (!_isFilled) {
         fillMap();
      }

      return super.get(key);
   }

   /**
    * Gets a new value.
    */
   @Override
   public Value getArg(Value key, boolean isTop) {
      if (!_isFilled) {
         fillMap();
      }

      return super.getArg(key, isTop);
   }

   /**
    * Returns the array ref.
    */
   @Override
   public Var getVar(Value key) {
      if (!_isFilled) {
         fillMap();
      }

      return super.getVar(key);
   }

   /**
    * Copy for assignment.
    */
   @Override
   public Value copy() {
      if (!_isFilled) {
         fillMap();
      }

      return super.copy();
   }

   /**
    * Copy for saving a function arguments.
    */
   @Override
   public Value copySaveFunArg() {
      if (!_isFilled) {
         fillMap();
      }

      return super.copySaveFunArg();
   }

   /**
    * Returns an iterator of the entries.
    */
   @Override
   public Set<Map.Entry<Value, Value>> entrySet() {
      if (!_isFilled) {
         fillMap();
      }

      return super.entrySet();
   }

   /**
    * Convenience for lib.
    */
   @Override
   public void put(String key, String value) {
      if (!_isFilled) {
         fillMap();
      }

      super.put(_env.createString(key), _env.createString(value));
   }

   /**
    * Returns true if the value is isset().
    */
   @Override
   public boolean isset(Value key) {
      return get(key).isset();
   }

   @Override
   public void varDumpImpl(Env env,
                           WriteStream out,
                           int depth,
                           IdentityHashMap<Value, String> valueSet)
      throws IOException {
      if (!_isFilled) {
         fillMap();
      }

      super.varDumpImpl(env, out, depth, valueSet);
   }

   @Override
   protected void printRImpl(Env env,
                             WriteStream out,
                             int depth,
                             IdentityHashMap<Value, String> valueSet)
      throws IOException {
      if (!_isFilled) {
         fillMap();
      }

      super.printRImpl(env, out, depth, valueSet);
   }

   /**
    * Fills the map.
    */
   private void fillMap() {
      if (_isFilled) {
         return;
      }

      _isFilled = true;

      for (Map.Entry<Value, Value> entry : _env.getBianca().getServerEnvMap().entrySet()) {
         super.put(entry.getKey(), entry.getValue());
      }

      HttpServletRequest request = _env.getRequest();

      if (request != null) {
         super.put(SERVER_ADDR_V,
            _env.createString(request.getLocalAddr()));
         super.put(SERVER_NAME_V,
            _env.createString(request.getServerName()));

         super.put(SERVER_PORT_V,
            LongValue.create(request.getServerPort()));
         super.put(REMOTE_HOST_V,
            _env.createString(request.getRemoteHost()));
         super.put(REMOTE_ADDR_V,
            _env.createString(request.getRemoteAddr()));
         super.put(REMOTE_PORT_V,
            LongValue.create(request.getRemotePort()));

         // Drupal's optional activemenu plugin only works on Apache servers!
         // bug at http://drupal.org/node/221867
         super.put(SERVER_SOFTWARE_V,
            _env.createString("Apache PHP Bianca("
               + _env.getBianca().getVersion()
               + ")"));

         super.put(SERVER_PROTOCOL_V,
            _env.createString(request.getProtocol()));
         super.put(REQUEST_METHOD_V,
            _env.createString(request.getMethod()));

         String queryString = BiancaRequestAdapter.getPageQueryString(request);
         String requestURI = BiancaRequestAdapter.getPageURI(request);
         String servletPath = BiancaRequestAdapter.getPageServletPath(request);
         String pathInfo = BiancaRequestAdapter.getPagePathInfo(request);
         String contextPath = BiancaRequestAdapter.getPageContextPath(request);

         if (queryString != null) {
            super.put(QUERY_STRING_V,
               _env.createString(queryString));
         }

         // TODO: a better way?
         // getRealPath() returns a native path
         // need to convert windows paths to bianca paths
         String root = request.getRealPath("/");
         if (root.indexOf('\\') >= 0) {
            root = root.replace('\\', '/');
            root = '/' + root;
         }

         super.put(DOCUMENT_ROOT_V,
            _env.createString(root));
         super.put(SCRIPT_NAME_V,
            _env.createString(contextPath + servletPath));
         super.put(SCRIPT_URL_V,
            _env.createString(requestURI));

         if (queryString != null) {
            requestURI = requestURI + '?' + queryString;
         }

         super.put(REQUEST_URI_V,
            _env.createString(requestURI));

         super.put(REQUEST_TIME_V,
            LongValue.create(_env.getStartTime() / 1000));

         super.put(SCRIPT_FILENAME_V,
            _env.createString(request.getRealPath(servletPath)));

         if (pathInfo != null) {
            super.put(PATH_INFO_V,
               _env.createString(pathInfo));
            super.put(PATH_TRANSLATED_V,
               _env.createString(request.getRealPath(pathInfo)));
         }

         if (request.isSecure()) {
            super.put(HTTPS_V,
               _env.createString("on"));
         }

         if (pathInfo == null) {
            super.put(PHP_SELF_V,
               _env.createString(contextPath + servletPath));
         } else {
            super.put(PHP_SELF_V,
               _env.createString(contextPath + servletPath + pathInfo));
         }

         // authType is not set on Tomcat
         //String authType = request.getAuthType();
         String authHeader = request.getHeader("Authorization");

         if (authHeader != null) {
            if (authHeader.indexOf("Basic") == 0) {
               super.put(AUTH_TYPE_V,
                  _env.createString("Basic"));

               if (request.getRemoteUser() != null) {
                  super.put(PHP_AUTH_USER_V,
                     _env.createString(request.getRemoteUser()));

                  String digest = authHeader.substring("Basic ".length());

                  String userPass = Base64.decode(digest);

                  int i = userPass.indexOf(':');
                  if (i > 0) {
                     super.put(PHP_AUTH_PW_V,
                        _env.createString(userPass.substring(i + 1)));
                  }
               }
            } else if (authHeader.indexOf("Digest") == 0) {
               super.put(AUTH_TYPE_V,
                  _env.createString("Digest"));

               String digest = authHeader.substring("Digest ".length());

               super.put(PHP_AUTH_DIGEST_V,
                  _env.createString(digest));
            }
         }

         Enumeration e = request.getHeaderNames();
         while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            String value = request.getHeader(key);

            if (key.equalsIgnoreCase("Host")) {
               super.put(HTTP_HOST_V,
                  _env.createString(value));
            } else if (key.equalsIgnoreCase("Content-Length")) {
               super.put(CONTENT_LENGTH_V,
                  _env.createString(value));
            } else if (key.equalsIgnoreCase("Content-Type")) {
               super.put(CONTENT_TYPE_V,
                  _env.createString(value));
            } else {
               super.put(convertHttpKey(key), _env.createString(value));
            }
         }
      }
   }

   /**
    * Converts a header key to HTTP_
    */
   private StringValue convertHttpKey(String key) {
      StringValue sb = new StringValue();

      sb.append("HTTP_");

      int len = key.length();
      for (int i = 0; i < len; i++) {
         char ch = key.charAt(i);

         if (Character.isLowerCase(ch)) {
            sb.append(Character.toUpperCase(ch));
         } else if (ch == '-') {
            sb.append('_');
         } else {
            sb.append(ch);
         }
      }

      return sb;
   }

   //
   // Java serialization code
   //
   private Object writeReplace() {
      if (!_isFilled) {
         fillMap();
      }

      return super.copy();
   }
}
