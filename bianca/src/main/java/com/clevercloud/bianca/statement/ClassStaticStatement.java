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
package com.clevercloud.bianca.statement;

import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.expr.Expr;
import com.clevercloud.bianca.expr.VarExpr;

/**
 * Represents a static statement in a PHP program.
 */
public class ClassStaticStatement
   extends Statement {

   protected final String _className;
   protected final VarExpr _var;
   protected final Expr _initValue;
   protected StringValue _staticName;

   /**
    * Creates the echo statement.
    */
   public ClassStaticStatement(Location location,
                               String className,
                               VarExpr var,
                               Expr initValue) {
      super(location);

      _className = className;
      _var = var;
      _initValue = initValue;
   }

   @Override
   public Value execute(Env env) {
      try {
         // TODO: this isn't reliable, needs to be Bianca-based
         if (_staticName == null) {
            _staticName = env.createStaticName();
         }

         // String className = _className;
         StringValue staticName = _staticName;

         Value qThis = env.getThis();

         BiancaClass qClass = qThis.getBiancaClass();
         String className = qClass.getName();

         // Var var = qClass.getStaticFieldVar(env, env.createString(staticName));
         // Var var = qClass.getStaticFieldVar(env, staticName);
         Var var = env.getStaticVar(env.createString(className
            + "::" + staticName));

         env.setVar(_var.getName(), var);

         if (!var.isset() && _initValue != null) {
            var.set(_initValue.eval(env));
         }

      } catch (RuntimeException e) {
         rethrow(e, RuntimeException.class);
      }

      return null;
   }
}
