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
package com.clevercloud.bianca.statement;

import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.env.BreakValue;
import com.clevercloud.bianca.env.ContinueValue;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.expr.Expr;

/**
 * Represents a do ... while statement.
 */
public class DoStatement extends Statement {

   protected final Expr _test;
   protected final Statement _block;
   protected final String _label;

   public DoStatement(Location location,
                      Expr test,
                      Statement block,
                      String label) {
      super(location);

      _test = test;
      _block = block;
      _label = label;

      block.setParent(this);
   }

   @Override
   public boolean isLoop() {
      return true;
   }

   @Override
   public Value execute(Env env) {
      try {
         do {
            Value value = _block.execute(env);

            if (value == null) {
            } else if (value instanceof ContinueValue) {
               ContinueValue conValue = (ContinueValue) value;

               int target = conValue.getTarget();

               if (target > 1) {
                  return new ContinueValue(target - 1);
               }
            } else if (value instanceof BreakValue) {
               BreakValue breakValue = (BreakValue) value;

               int target = breakValue.getTarget();

               if (target > 1) {
                  return new BreakValue(target - 1);
               } else {
                  break;
               }
            } else {
               return value;
            }
         } while (_test.evalBoolean(env));
      } catch (RuntimeException e) {
         rethrow(e, RuntimeException.class);
      }

      return null;
   }
}
