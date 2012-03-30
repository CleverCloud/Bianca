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

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.env.Var;
import com.clevercloud.bianca.program.InterpretedClassDef;
import com.clevercloud.util.L10N;

/**
 * Represents the 'this' expression.
 */
public class ThisExpr extends AbstractVarExpr {

   private static final L10N L = new L10N(ThisExpr.class);
   protected final InterpretedClassDef _biancaClass;

   public ThisExpr(InterpretedClassDef biancaClass) {
      _biancaClass = biancaClass;
   }

   public InterpretedClassDef getBiancaClass() {
      return _biancaClass;
   }

   /**
    * Creates a field ref
    */
   @Override
   public Expr createFieldGet(ExprFactory factory,
                              StringValue name) {
      return factory.createThisField(this, name);
   }

   /**
    * Creates a field ref
    */
   @Override
   public Expr createFieldGet(ExprFactory factory,
                              Expr name) {
      return factory.createThisField(this, name);
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value eval(Env env) {
      return env.getThis();
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalArg(Env env, boolean isTop) {
      return env.getThis();
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Var evalVar(Env env) {
      return env.getThis().toVar();
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalAssignValue(Env env, Value value) {
      env.error(getLocation(), "can't assign $this");

      return value;
   }

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalAssignRef(Env env, Value value) {
      env.error(getLocation(), "can't assign $this");

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
      env.error(getLocation(), "can't unset $this");
   }

   @Override
   public String toString() {
      return "$this";
   }
}
