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
 * @author Nam Nguyen
 */
package com.clevercloud.bianca.lib;

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.annotation.Name;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.module.AbstractBiancaModule;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Bianca functions to get information about the Bianca environment.
 */
public class BiancaModule extends AbstractBiancaModule {

   /**
    * Returns the Bianca object.
    */
   @Name("bianca_get_bianca")
   public static BiancaContext get_bianca(Env env) {
      return env.getBianca();
   }

   /**
    * Returns the Env object.
    */
   @Name("bianca_get_env")
   public static Env get_env(Env env) {
      return env;
   }

   /**
    * Returns the version of Bianca.
    */
   @Name("bianca_version")
   public static String version(Env env) {
      return env.getBianca().getVersion();
   }

   /**
    * Returns the version date of Bianca.
    */
   @Name("bianca_version_date")
   public static String version_date(Env env) {
      return env.getBianca().getVersionDate();
   }

   /**
    * Returns true if this is the Professional version.
    */
   @Name("bianca_is_pro")
   public static boolean is_pro(Env env) {
      return env.getBianca().isPro();
   }

   /**
    * Returns true if pages will be compiled.
    */
   @Name("bianca_is_compile")
   public static boolean is_compile(Env env) {
      return env.getBianca().isCompile();
   }

   /**
    * Returns true if Bianca is running under Bianca.
    */
   @Name("bianca_is_bianca")
   public static boolean is_bianca(Env env) {
      return env.getBianca().isBianca();
   }

   /**
    * Returns true if a JDBC database has been explicitly set.
    */
   @Name("bianca_has_database")
   public static boolean has_database(Env env) {
      return env.getBianca().getDatabase() != null;
   }

   /**
    * Returns true if there is an HttpRequest associated with this Env.
    */
   @Name("bianca_has_request")
   public static boolean has_request(Env env) {
      return env.getRequest() != null;
   }

   /**
    * Returns the HttpServletRequest associated with this Env.
    */
   @Deprecated
   @Name("bianca_get_request")
   public static HttpServletRequest get_request(Env env) {
      return env.getRequest();
   }

   /**
    * Returns the HttpServletRequest associated with this Env.
    */
   @Name("bianca_servlet_request")
   public static HttpServletRequest get_servlet_request(Env env) {
      return env.getRequest();
   }

   /**
    * Returns the HttpServletResponse associated with this Env.
    */
   @Deprecated
   @Name("bianca_get_response")
   public static HttpServletResponse get_response(Env env) {
      return env.getResponse();
   }

   /**
    * Returns the HttpServletResponse associated with this Env.
    */
   @Name("bianca_servlet_response")
   public static HttpServletResponse get_servlet_response(Env env) {
      return env.getResponse();
   }

   /**
    * Returns the ServletContext.
    */
   @Name("bianca_get_servlet_context")
   public static ServletContext get_servlet_context(Env env) {
      return env.getServletContext();
   }

   /**
    * Special bianca-only import statements.
    */
   @Name("bianca_import")
   public static void q_import(Env env, String name) {
      if (name.endsWith("*")) {
         env.addWildcardImport(name);
      } else {
         env.putQualifiedImport(name);
      }
   }
}
