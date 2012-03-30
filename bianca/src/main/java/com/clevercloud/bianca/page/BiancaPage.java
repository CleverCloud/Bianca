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
 */
package com.clevercloud.bianca.page;

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.env.BiancaLanguageException;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.NullValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.function.AbstractFunction;
import com.clevercloud.bianca.program.ClassDef;
import com.clevercloud.util.L10N;
import com.clevercloud.vfs.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a compiled PHP program.
 */
abstract public class BiancaPage {

   private static final L10N L = new L10N(BiancaPage.class);
   private HashMap<String, AbstractFunction> _funMap = new HashMap<String, AbstractFunction>();
   private HashMap<String, AbstractFunction> _funMapLowerCase = new HashMap<String, AbstractFunction>();
   private HashMap<String, ClassDef> _classMap = new HashMap<String, ClassDef>();
   private BiancaPage _profilePage;

   /**
    * Returns true if the page is modified.
    */
   public boolean isModified() {
      return false;
   }

   /**
    * Returns the compiling page, if any
    */
   public BiancaPage getCompiledPage() {
      return null;
   }

   /**
    * Returns the user name for profiling, if any
    */
   public String getUserPath() {
      return null;
   }

   /**
    * Returns the profiling page, if any
    */
   public BiancaPage getProfilePage() {
      return _profilePage;
   }

   /**
    * Sets the profiling page, if any
    */
   public void setProfilePage(BiancaPage profilePage) {
      _profilePage = profilePage;
   }

   /**
    * Returns the page's path.
    */
   abstract public Path getSelfPath(Env env);

   /**
    * Finds a function.
    */
   public AbstractFunction findFunction(String name) {
      AbstractFunction fun = _funMap.get(name);

      if (fun != null) {
         return fun;
      }

      fun = _funMapLowerCase.get(name.toLowerCase());

      return fun;
   }

   /**
    * Finds a function.
    */
   public ClassDef findClass(String name) {
      return _classMap.get(name);
   }

   /**
    * Returns the class map.
    */
   public HashMap<String, ClassDef> getClassMap() {
      return _classMap;
   }

   /**
    * Execute the program as top-level, i.e. not included.
    *
    * @param env the calling environment
    */
   public Value executeTop(Env env) {
      BiancaPage compile = getCompiledPage();

      Path oldPwd = env.getPwd();

      Path pwd = getPwd(env);

      env.setPwd(pwd);
      try {
         if (compile != null) {
            return compile.executeTop(env);
         }

         return execute(env);
      } catch (BiancaLanguageException e) {
         if (env.getExceptionHandler() != null) {
            try {
               env.getExceptionHandler().call(env, e.getValue());
            } catch (BiancaLanguageException e2) {
               uncaughtExceptionError(env, e2);
            }
         } else {
            uncaughtExceptionError(env, e);
         }

         return NullValue.NULL;
      } finally {
         env.setPwd(oldPwd);
      }
   }

   /*
    * Throws an error for this uncaught exception.
    */
   private void uncaughtExceptionError(Env env, BiancaLanguageException e) {
      Location location = e.getLocation(env);
      String type = e.getValue().toString();
      String message = e.getMessage(env);

      if ("".equals(type)) {
         type = e.getValue().getType();
      }

      if (message.equals("")) {
         env.error(location,
            L.l(
               "Uncaught exception of type '{0}'",
               type));
      } else {
         env.error(location,
            L.l(
               "Uncaught exception of type '{0}' with message '{1}'",
               type,
               message));
      }
   }

   /**
    * Returns the pwd according to the source page.
    */
   public Path getPwd(Env env) {
      return getSelfPath(env).getParent();
   }

   /**
    * Execute the program
    *
    * @param env the calling environment
    */
   abstract public Value execute(Env env);

   /**
    * Initialize the program
    *
    * @param bianca the owning engine
    */
   public void init(BiancaContext bianca) {
   }

   /**
    * Initialize the environment
    *
    * @param bianca the owning engine
    */
   public void init(Env env) {
   }

   /**
    * Imports the page definitions.
    */
   public void importDefinitions(Env env) {
      for (Map.Entry<String, AbstractFunction> entry : _funMap.entrySet()) {
         AbstractFunction fun = entry.getValue();

         if (fun.isGlobal()) {
            env.addFunction(entry.getKey(), entry.getValue());
         }
      }

      for (Map.Entry<String, ClassDef> entry : _classMap.entrySet()) {
         env.addClassDef(entry.getKey(), entry.getValue());
      }
   }

   /**
    * Adds a function.
    */
   protected void addFunction(String name, AbstractFunction fun) {
      AbstractFunction oldFun = _funMap.put(name, fun);

      _funMapLowerCase.put(name.toLowerCase(), fun);
   }

   /**
    * Adds a class.
    */
   protected void addClass(String name, ClassDef cl) {
      _classMap.put(name, cl);
   }

   /**
    * Sets a runtime function array after an env.
    */
   public boolean setRuntimeFunction(AbstractFunction[] funList) {
      return false;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "[]";
   }
}
