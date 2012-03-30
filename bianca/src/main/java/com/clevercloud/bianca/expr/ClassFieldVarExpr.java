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
package com.clevercloud.bianca.expr;

import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.env.Var;
import com.clevercloud.bianca.parser.BiancaParser;
import com.clevercloud.util.L10N;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a PHP static field reference.
 */
public class ClassFieldVarExpr extends AbstractVarExpr {

   private static final L10N L = new L10N(ClassFieldVarExpr.class);
   protected final String _className;
   protected final Expr _varName;
   private StringValue _prefix;

   public ClassFieldVarExpr(String className, Expr varName) {
      _className = className;

      _varName = varName;

      _prefix = new StringValue(_className).append("::");
   }

   //
   // function call creation
   //

   /**
    * Creates a function call expression
    */
   @Override
   public Expr createCall(BiancaParser parser,
                          Location location,
                          ArrayList<Expr> args)
      throws IOException {
      ExprFactory factory = parser.getExprFactory();

      Expr var = factory.createVarVar(_varName);

      return factory.createClassMethodCall(location, _className, var, args);
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value eval(Env env) {
      StringValue varName = _varName.evalStringValue(env);

      StringValue var = _prefix.toStringBuilder().append(varName);

      return env.getStaticValue(var);
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Var evalVar(Env env) {
      StringValue varName = _varName.evalStringValue(env);

      StringValue var = _prefix.toStringBuilder().append(varName);

      return env.getStaticVar(var);
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalAssignRef(Env env, Value value) {
      StringValue varName = _varName.evalStringValue(env);

      StringValue var = _prefix.toStringBuilder().append(varName);

      env.setStaticRef(var, value);

      return value;
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public void evalUnset(Env env) {
      env.error(getLocation(),
         L.l("{0}::${1}: Cannot unset static variables.",
            _className, _varName));
   }

   @Override
   public String toString() {
      return _className + "::$" + _varName;
   }
}
