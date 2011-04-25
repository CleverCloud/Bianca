/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */
package com.caucho.quercus.env;

import com.caucho.quercus.QuercusRequestAdapter;
import com.caucho.util.Base64;
import com.caucho.vfs.WriteStream;

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

   private static final StringValue SERVER_ADDR_V = new ConstStringValue("SERVER_ADDR");
   private static final StringValue SERVER_ADDR_VU = new UnicodeBuilderValue("SERVER_ADDR");
   private static final StringValue SERVER_NAME_V = new ConstStringValue("SERVER_NAME");
   private static final StringValue SERVER_NAME_VU = new UnicodeBuilderValue("SERVER_NAME");
   private static final StringValue SERVER_PORT_V = new ConstStringValue("SERVER_PORT");
   private static final StringValue SERVER_PORT_VU = new UnicodeBuilderValue("SERVER_PORT");
   private static final StringValue REMOTE_HOST_V = new ConstStringValue("REMOTE_HOST");
   private static final StringValue REMOTE_HOST_VU = new UnicodeBuilderValue("REMOTE_HOST");
   private static final StringValue REMOTE_ADDR_V = new ConstStringValue("REMOTE_ADDR");
   private static final StringValue REMOTE_ADDR_VU = new UnicodeBuilderValue("REMOTE_ADDR");
   private static final StringValue REMOTE_PORT_V = new ConstStringValue("REMOTE_PORT");
   private static final StringValue REMOTE_PORT_VU = new UnicodeBuilderValue("REMOTE_PORT");
   private static final StringValue DOCUMENT_ROOT_V = new ConstStringValue("DOCUMENT_ROOT");
   private static final StringValue DOCUMENT_ROOT_VU = new UnicodeBuilderValue("DOCUMENT_ROOT");
   private static final StringValue SERVER_SOFTWARE_V = new ConstStringValue("SERVER_SOFTWARE");
   private static final StringValue SERVER_SOFTWARE_VU = new UnicodeBuilderValue("SERVER_SOFTWARE");
   private static final StringValue SERVER_PROTOCOL_V = new ConstStringValue("SERVER_PROTOCOL");
   private static final StringValue SERVER_PROTOCOL_VU = new UnicodeBuilderValue("SERVER_PROTOCOL");
   private static final StringValue REQUEST_METHOD_V = new ConstStringValue("REQUEST_METHOD");
   private static final StringValue REQUEST_METHOD_VU = new UnicodeBuilderValue("REQUEST_METHOD");
   private static final StringValue QUERY_STRING_V = new ConstStringValue("QUERY_STRING");
   private static final StringValue QUERY_STRING_VU = new UnicodeBuilderValue("QUERY_STRING");
   private static final StringValue REQUEST_URI_V = new ConstStringValue("REQUEST_URI");
   private static final StringValue REQUEST_URI_VU = new UnicodeBuilderValue("REQUEST_URI");
   private static final StringValue REQUEST_TIME_V = new ConstStringValue("REQUEST_TIME");
   private static final StringValue REQUEST_TIME_VU = new UnicodeBuilderValue("REQUEST_TIME");
   private static final StringValue SCRIPT_URL_V = new ConstStringValue("SCRIPT_URL");
   private static final StringValue SCRIPT_URL_VU = new UnicodeBuilderValue("SCRIPT_URL");
   private static final StringValue SCRIPT_NAME_V = new ConstStringValue("SCRIPT_NAME");
   private static final StringValue SCRIPT_NAME_VU = new UnicodeBuilderValue("SCRIPT_NAME");
   private static final StringValue SCRIPT_FILENAME_V = new ConstStringValue("SCRIPT_FILENAME");
   private static final StringValue SCRIPT_FILENAME_VU = new UnicodeBuilderValue("SCRIPT_FILENAME");
   private static final StringValue PATH_INFO_V = new ConstStringValue("PATH_INFO");
   private static final StringValue PATH_INFO_VU = new UnicodeBuilderValue("PATH_INFO");
   private static final StringValue PATH_TRANSLATED_V = new ConstStringValue("PATH_TRANSLATED");
   private static final StringValue PATH_TRANSLATED_VU = new UnicodeBuilderValue("PATH_TRANSLATED");
   private static final StringValue PHP_SELF_V = new ConstStringValue("PHP_SELF");
   private static final StringValue PHP_SELF_VU = new UnicodeBuilderValue("PHP_SELF");
   private static final StringValue PHP_AUTH_USER_V = new ConstStringValue("PHP_AUTH_USER");
   private static final StringValue PHP_AUTH_USER_VU = new UnicodeBuilderValue("PHP_AUTH_USER");
   private static final StringValue PHP_AUTH_PW_V = new ConstStringValue("PHP_AUTH_PW");
   private static final StringValue PHP_AUTH_PW_VU = new UnicodeBuilderValue("PHP_AUTH_PW");
   private static final StringValue PHP_AUTH_DIGEST_V = new ConstStringValue("PHP_AUTH_DIGEST");
   private static final StringValue PHP_AUTH_DIGEST_VU = new UnicodeBuilderValue("PHP_AUTH_DIGEST");
   private static final StringValue AUTH_TYPE_V = new ConstStringValue("AUTH_TYPE");
   private static final StringValue AUTH_TYPE_VU = new UnicodeBuilderValue("AUTH_TYPE");
   private static final StringValue HTTPS_V = new ConstStringValue("HTTPS");
   private static final StringValue HTTPS_VU = new UnicodeBuilderValue("HTTPS");
   private static final StringValue HTTP_HOST_V = new ConstStringValue("HTTP_HOST");
   private static final StringValue HTTP_HOST_VU = new UnicodeBuilderValue("HTTP_HOST");
   private static final StringValue CONTENT_LENGTH_V = new ConstStringValue("CONTENT_LENGTH");
   private static final StringValue CONTENT_LENGTH_VU = new UnicodeBuilderValue("CONTENT_LENGTH");
   private static final StringValue CONTENT_TYPE_V = new ConstStringValue("CONTENT_TYPE");
   private static final StringValue CONTENT_TYPE_VU = new UnicodeBuilderValue("CONTENT_TYPE");
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

      for (Map.Entry<Value, Value> entry : _env.getQuercus().getServerEnvMap().entrySet()) {
         super.put(entry.getKey(), entry.getValue());
      }

      HttpServletRequest request = _env.getRequest();

      if (request != null) {
         super.put(SERVER_ADDR_VU,
                 _env.createString(request.getLocalAddr()));
         super.put(SERVER_NAME_VU,
                 _env.createString(request.getServerName()));

         super.put(SERVER_PORT_VU,
                 LongValue.create(request.getServerPort()));
         super.put(REMOTE_HOST_VU,
                 _env.createString(request.getRemoteHost()));
         super.put(REMOTE_ADDR_VU,
                 _env.createString(request.getRemoteAddr()));
         super.put(REMOTE_PORT_VU,
                 LongValue.create(request.getRemotePort()));

         // Drupal's optional activemenu plugin only works on Apache servers!
         // bug at http://drupal.org/node/221867
         super.put(SERVER_SOFTWARE_VU,
                 _env.createString("Apache PHP Quercus("
                 + _env.getQuercus().getVersion()
                 + ")"));

         super.put(SERVER_PROTOCOL_VU,
                 _env.createString(request.getProtocol()));
         super.put(REQUEST_METHOD_VU,
                 _env.createString(request.getMethod()));

         String queryString = QuercusRequestAdapter.getPageQueryString(request);
         String requestURI = QuercusRequestAdapter.getPageURI(request);
         String servletPath = QuercusRequestAdapter.getPageServletPath(request);
         String pathInfo = QuercusRequestAdapter.getPagePathInfo(request);
         String contextPath = QuercusRequestAdapter.getPageContextPath(request);

         if (queryString != null) {
            super.put(QUERY_STRING_VU,
                    _env.createString(queryString));
         }

         // TODO: a better way?
         // getRealPath() returns a native path
         // need to convert windows paths to resin paths
         String root = request.getRealPath("/");
         if (root.indexOf('\\') >= 0) {
            root = root.replace('\\', '/');
            root = '/' + root;
         }

         super.put(DOCUMENT_ROOT_VU,
                 _env.createString(root));
         super.put(SCRIPT_NAME_VU,
                 _env.createString(contextPath + servletPath));
         super.put(SCRIPT_URL_VU,
                 _env.createString(requestURI));

         if (queryString != null) {
            requestURI = requestURI + '?' + queryString;
         }

         super.put(REQUEST_URI_VU,
                 _env.createString(requestURI));

         super.put(REQUEST_TIME_VU,
                 LongValue.create(_env.getStartTime() / 1000));

         super.put(SCRIPT_FILENAME_VU,
                 _env.createString(request.getRealPath(servletPath)));

         if (pathInfo != null) {
            super.put(PATH_INFO_VU,
                    _env.createString(pathInfo));
            super.put(PATH_TRANSLATED_VU,
                    _env.createString(request.getRealPath(pathInfo)));
         }

         if (request.isSecure()) {
            super.put(HTTPS_VU,
                    _env.createString("on"));
         }

         if (pathInfo == null) {
            super.put(PHP_SELF_VU,
                    _env.createString(contextPath + servletPath));
         } else {
            super.put(PHP_SELF_VU,
                    _env.createString(contextPath + servletPath + pathInfo));
         }

         // authType is not set on Tomcat
         //String authType = request.getAuthType();
         String authHeader = request.getHeader("Authorization");

         if (authHeader != null) {
            if (authHeader.indexOf("Basic") == 0) {
               super.put(AUTH_TYPE_VU,
                       _env.createString("Basic"));

               if (request.getRemoteUser() != null) {
                  super.put(PHP_AUTH_USER_VU,
                          _env.createString(request.getRemoteUser()));

                  String digest = authHeader.substring("Basic ".length());

                  String userPass = Base64.decode(digest);

                  int i = userPass.indexOf(':');
                  if (i > 0) {
                     super.put(PHP_AUTH_PW_VU,
                             _env.createString(userPass.substring(i + 1)));
                  }
               }
            } else if (authHeader.indexOf("Digest") == 0) {
               super.put(AUTH_TYPE_VU,
                       _env.createString("Digest"));

               String digest = authHeader.substring("Digest ".length());

               super.put(PHP_AUTH_DIGEST_VU,
                       _env.createString(digest));
            }
         }

         Enumeration e = request.getHeaderNames();
         while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            String value = request.getHeader(key);

            if (key.equalsIgnoreCase("Host")) {
               super.put(HTTP_HOST_VU,
                       _env.createString(value));
            } else if (key.equalsIgnoreCase("Content-Length")) {
               super.put(CONTENT_LENGTH_VU,
                       _env.createString(value));
            } else if (key.equalsIgnoreCase("Content-Type")) {
               super.put(CONTENT_TYPE_VU,
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
      StringValue sb = new StringBuilderValue();

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
