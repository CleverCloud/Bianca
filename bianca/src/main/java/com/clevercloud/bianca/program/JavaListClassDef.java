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
package com.clevercloud.bianca.program;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.JavaListAdapter;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.module.ModuleContext;

import java.util.List;

/**
 * Represents an introspected Java class.
 */
public class JavaListClassDef extends JavaClassDef {

   JavaListClassDef(ModuleContext moduleContext, String name, Class type) {
      super(moduleContext, name, type);
   }

   @Override
   public Value wrap(Env env, Object obj) {
      if (!_isInit) {
         init();
      }

      return new JavaListAdapter((List) obj, this);
   }
}
