/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 * Copyright (c) 2011-2012 Clever Cloud SAS -- all rights reserved
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
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.module;

import com.clevercloud.bianca.env.LongValue;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a bianca module.
 */
public class AbstractBiancaModule implements BiancaModule {

   protected static final int PHP_INI_USER = IniDefinition.PHP_INI_USER;
   protected static final int PHP_INI_PERDIR = IniDefinition.PHP_INI_PERDIR;
   protected static final int PHP_INI_SYSTEM = IniDefinition.PHP_INI_SYSTEM;
   protected static final int PHP_INI_ALL = IniDefinition.PHP_INI_ALL;
   private static final HashMap<StringValue, Value> NULL_MAP = new HashMap<StringValue, Value>();

   @Override
   public Map<StringValue, Value> getConstMap() {
      return NULL_MAP;
   }

   /**
    * Returns the default bianca.ini values.
    */
   @Override
   public IniDefinitions getIniDefinitions() {
      return IniDefinitions.EMPTY;
   }

   /**
    * Returns the extensions loaded by the module.
    */
   @Override
   public String[] getLoadedExtensions() {
      return new String[0];
   }

   protected static void addConstant(Map<StringValue, Value> map,
                                     String name, Value value) {
      map.put(new StringValue(name), value);
   }

   protected static void addConstant(Map<StringValue, Value> map,
                                     String name, long value) {
      map.put(new StringValue(name), LongValue.create(value));
   }

   protected static void addConstant(Map<StringValue, Value> map,
                                     String name, String value) {
      map.put(new StringValue(name), StringValue.create(value));
   }
}
