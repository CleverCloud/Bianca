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
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.env.Var;
import com.clevercloud.bianca.parser.BiancaParser;
import com.clevercloud.bianca.statement.Statement;

/**
 * Represents an expression that is assignable
 */
abstract public class AbstractVarExpr extends Expr {

   public AbstractVarExpr(Location location) {
      super(location);
   }

   public AbstractVarExpr() {
   }

   /**
    * Returns true if the expression is a var/left-hand-side.
    */
   @Override
   public boolean isVar() {
      return true;
   }

   /**
    * Marks the value as assigned
    */
   @Override
   public void assign(BiancaParser parser) {
      // TODO: used by list, e.g. bianca/03l8.  need further tests
   }

   /**
    * Creates the assignment.
    */
   @Override
   public Expr createAssign(BiancaParser parser, Expr value) {
      return value.createAssignFrom(parser, this);
   }

   /**
    * Creates the assignment.
    */
   @Override
   public Expr createAssignRef(BiancaParser parser,
                               Expr value) {
      return parser.getExprFactory().createAssignRef(this, value);
   }

   /**
    * Creates the reference
    *
    * @param location
    */
   @Override
   public Expr createRef(BiancaParser parser) {
      return parser.getExprFactory().createRef(this);
   }

   /**
    * Creates the copy.
    *
    * @param location
    */
   @Override
   public Expr createCopy(ExprFactory factory) {
      return factory.createCopy(this);
   }

   /**
    * Creates the assignment.
    */
   @Override
   public Statement createUnset(ExprFactory factory, Location location) {
      return factory.createExpr(location, factory.createUnsetVar(this));
   }

   /**
    * Evaluates the expression, returning a Value.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   abstract public Value eval(Env env);

   /**
    * Evaluates the expression as a reference (by RefExpr).
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   abstract public Var evalVar(Env env);

   /**
    * Evaluates the expression as a reference when possible.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalRef(Env env) {
      return evalVar(env);
   }

   /**
    * Evaluates the expression as an argument.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalArg(Env env, boolean isTop) {
      return evalVar(env);
   }

   /**
    * Evaluates the expression and copies the result for an assignment.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalCopy(Env env) {
      return eval(env).copy();
   }

   /**
    * Evaluates the expression as an array.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalArray(Env env) {
      return evalVar(env).toAutoArray();
   }

   /**
    * Evaluates the expression as an object.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalObject(Env env) {
      return evalVar(env).toObject(env);
   }

   /**
    * Evaluates the expression as an argument.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   abstract public void evalUnset(Env env);

   /**
    * Evaluates the expression.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   public Value evalAssignValue(Env env, Value value) {
      return evalAssignRef(env, value);
   }

   /**
    * Assign the variable with a new reference value.
    *
    * @param env the calling environment.
    * @return the expression value.
    */
   @Override
   abstract public Value evalAssignRef(Env env, Value value);
}
