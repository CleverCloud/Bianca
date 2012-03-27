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
 * @author Sam
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.spl;

import com.clevercloud.bianca.env.CountDelegate;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.ObjectValue;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.env.StringValue;

/**
 * A delegate that intercepts the global count() function and calls count()
 * method on target objects that implement
 * the {@link com.clevercloud.bianca.lib.spl.Countable} interface.
 */
public class CountableDelegate implements CountDelegate {

   private static final StringValue COUNT_METHOD = new StringValue("count");

   @Override
   public int count(ObjectValue qThis) {
      Env env = Env.getInstance();

      Value count = qThis.callMethod(env, COUNT_METHOD);

      return count.toInt();
   }
}
