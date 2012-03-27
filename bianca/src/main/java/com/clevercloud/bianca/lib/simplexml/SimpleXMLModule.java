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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Charles Reich
 */
package com.clevercloud.bianca.lib.simplexml;

import com.clevercloud.bianca.UnimplementedException;
import com.clevercloud.bianca.annotation.NotNull;
import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.env.BooleanValue;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.BiancaClass;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.module.AbstractBiancaModule;
import com.clevercloud.util.L10N;

import java.util.logging.Logger;

/**
 * PHP SimpleXML
 */
public class SimpleXMLModule
        extends AbstractBiancaModule {

   private static final Logger log = Logger.getLogger(SimpleXMLModule.class.getName());
   private static final L10N L = new L10N(SimpleXMLModule.class);

   @Override
   public String[] getLoadedExtensions() {
      return new String[]{"SimpleXML"};
   }

   public Value simplexml_load_string(Env env,
           Value data,
           @Optional String className,
           @Optional int options,
           @Optional Value namespaceV,
           @Optional boolean isPrefix) {
      if (data.isNull() || data == BooleanValue.FALSE) {
         return BooleanValue.FALSE;
      }

      if (className == null || className.length() == 0) {
         className = "SimpleXMLElement";
      }

      BiancaClass cls = env.getClass(className);

      return SimpleXMLElement.create(env, cls,
              data, options, false,
              namespaceV, isPrefix);
   }

   public Value simplexml_load_file(Env env,
           @NotNull StringValue file,
           @Optional String className,
           @Optional int options,
           @Optional Value namespaceV,
           @Optional boolean isPrefix) {
      if (className == null || className.length() == 0) {
         className = "SimpleXMLElement";
      }

      BiancaClass cls = env.getClass(className);

      return SimpleXMLElement.create(env, cls,
              file, options, true,
              namespaceV, isPrefix);
   }

   public SimpleXMLElement simplexml_import_dom(Env env) {
      // TODO: DOMNode needs to be able to export partial documents
      throw new UnimplementedException("simplexml_import_dom");
   }
}
