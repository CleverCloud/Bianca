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
package com.clevercloud.bianca.lib.reflection;

import com.clevercloud.bianca.env.BiancaClass;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.expr.ParamRequiredExpr;
import com.clevercloud.bianca.function.AbstractFunction;
import com.clevercloud.bianca.program.Arg;
import com.clevercloud.util.L10N;

public class ReflectionParameter
   implements Reflector {

   private static final L10N L = new L10N(ReflectionParameter.class);
   private String _clsName;
   private ReflectionClass _cls;
   private AbstractFunction _fun;
   private Arg _arg;

   protected ReflectionParameter(AbstractFunction fun, Arg arg) {
      _fun = fun;
      _arg = arg;
   }

   protected ReflectionParameter(Env env,
                                 String clsName,
                                 AbstractFunction fun,
                                 Arg arg) {
      this(fun, arg);

      _clsName = clsName;
      _cls = new ReflectionClass(env, clsName);
   }

   private void __clone() {
   }

   public static ReflectionParameter __construct(Env env,
                                                 String funName,
                                                 StringValue paramName) {
      AbstractFunction fun = env.findFunction(funName);

      Arg[] args = fun.getArgs();

      for (int i = 0; i < args.length; i++) {
         if (args[i].getName().equals(paramName)) {
            return new ReflectionParameter(fun, args[i]);
         }
      }

      throw new ReflectionException(
         L.l("cannot find parameter '{0}'", paramName));
   }

   public static String export(Env env,
                               Value function,
                               Value parameter,
                               boolean isReturn) {
      return null;
   }

   public StringValue getName() {
      return _arg.getName();
   }

   public boolean isPassedByReference() {
      return _arg.isReference();
   }

   public ReflectionClass getDeclaringClass(Env env) {
      if (_clsName != null) {
         BiancaClass cls = env.findClass(_clsName);
         BiancaClass parent = cls.getParent();

         if (parent == null || parent.findFunction(_fun.getName()) != _fun) {
            return new ReflectionClass(cls);
         } else {
            return getDeclaringClass(env, parent);
         }
      } else {
         return null;
      }
   }

   private ReflectionClass getDeclaringClass(Env env, BiancaClass cls) {
      if (cls == null) {
         return null;
      }

      ReflectionClass refClass = getDeclaringClass(env, cls.getParent());

      if (refClass != null) {
         return refClass;
      } else if (cls.findFunction(_fun.getName()) != null) {
         return new ReflectionClass(cls);
      } else {
         return null;
      }
   }

   public ReflectionClass getClass(Env env) {
      return _cls;
   }

   public boolean isArray() {
      return false;
   }

   public boolean allowsNull() {
      return false;
   }

   public boolean isOptional() {
      return !(_arg.getDefault() instanceof ParamRequiredExpr);
   }

   public boolean isDefaultValueAvailable() {
      return isOptional();
   }

   public Value getDefaultValue(Env env) {
      //XXX: more specific exception
      if (!isOptional()) {
         throw new ReflectionException(
            L.l("parameter '{0}' is not optional", _arg.getName()));
      }

      return _arg.getDefault().eval(env);
   }

   @Override
   public String toString() {
      return "ReflectionParameter["
         + _fun.getName() + "(" + _arg.getName() + ")]";
   }
}
