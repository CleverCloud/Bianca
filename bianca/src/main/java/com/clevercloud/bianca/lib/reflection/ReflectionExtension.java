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
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.reflection;

import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.function.AbstractFunction;
import com.clevercloud.bianca.module.IniDefinition;
import com.clevercloud.bianca.module.IniDefinitions;
import com.clevercloud.bianca.module.ModuleInfo;
import com.clevercloud.util.L10N;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReflectionExtension
   implements Reflector {

   private static final L10N L = new L10N(ReflectionExtension.class);
   private String _name;

   protected ReflectionExtension(Env env, String extension) {
      _name = extension;
   }

   private void __clone() {
   }

   public static ReflectionExtension __construct(Env env, String name) {
      return new ReflectionExtension(env, name);
   }

   public static String export(Env env,
                               String name,
                               @Optional boolean isReturn) {
      return null;
   }

   public String getName() {
      return _name;
   }

   public String getVersion() {
      return null;
   }

   public ArrayValue getFunctions(Env env) {
      ArrayValue array = new ArrayValueImpl();

      for (ModuleInfo moduleInfo : env.getBianca().getModules()) {
         Set<String> extensionSet = moduleInfo.getLoadedExtensions();

         if (extensionSet.contains(_name)) {
            for (String functionName : moduleInfo.getFunctions().keySet()) {
               AbstractFunction fun = env.findFunction(functionName);

               array.put(env.wrapJava(new ReflectionFunction(fun)));
            }
         }
      }

      return array;
   }

   public ArrayValue getConstants(Env env) {
      ArrayValue array = new ArrayValueImpl();

      for (ModuleInfo moduleInfo : env.getBianca().getModules()) {
         Set<String> extensionSet = moduleInfo.getLoadedExtensions();

         if (extensionSet.contains(_name)) {
            for (Map.Entry<StringValue, Value> entry : moduleInfo.getUnicodeConstMap().entrySet()) {
               array.put(entry.getKey(), entry.getValue());
            }
         }
      }

      return array;
   }

   public ArrayValue getINIEntries(Env env) {
      ArrayValue array = new ArrayValueImpl();

      for (ModuleInfo moduleInfo : env.getBianca().getModules()) {
         Set<String> extensionSet = moduleInfo.getLoadedExtensions();

         if (extensionSet.contains(_name)) {
            IniDefinitions iniDefs = moduleInfo.getIniDefinitions();

            Set<Map.Entry<String, IniDefinition>> entrySet = iniDefs.entrySet();

            if (entrySet != null) {
               for (Map.Entry<String, IniDefinition> entry : entrySet) {
                  array.put(StringValue.create(entry.getKey()),
                     entry.getValue().getValue(env));
               }
            }
         }
      }

      return array;
   }

   public ArrayValue getClasses(Env env) {
      ArrayValue array = new ArrayValueImpl();

      HashSet<String> exts = env.getModuleContext().getExtensionClasses(_name);

      if (exts != null) {
         for (String name : exts) {
            array.put(StringValue.create(name),
               env.wrapJava(new ReflectionClass(env, name)));
         }
      }

      return array;
   }

   public ArrayValue getClassNames(Env env) {
      ArrayValue array = new ArrayValueImpl();

      HashSet<String> exts = env.getModuleContext().getExtensionClasses(_name);

      if (exts != null) {
         for (String name : exts) {
            array.put(name);
         }
      }

      return array;
   }

   @Override
   public String toString() {
      return "ReflectionExtension[" + _name + "]";
   }
}
