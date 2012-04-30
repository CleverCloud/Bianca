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

import com.clevercloud.bianca.BiancaException;
import com.clevercloud.bianca.UnimplementedException;
import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.function.AbstractFunction;
import com.clevercloud.bianca.program.Arg;
import com.clevercloud.util.L10N;

public class ReflectionMethod extends ReflectionFunctionAbstract
   implements Reflector {

   private static final L10N L = new L10N(ReflectionMethod.class);
   public static final int IS_STATIC = 1;
   public static final int IS_ABSTRACT = 2;
   public static final int IS_FINAL = 4;
   public static final int IS_PUBLIC = 256;
   public static final int IS_PROTECTED = 512;
   public static final int IS_PRIVATE = 1024;
   private String _clsName;

   protected ReflectionMethod(AbstractFunction method) {
      super(method);
   }

   protected ReflectionMethod(String clsName, AbstractFunction method) {
      super(method);

      _clsName = clsName;
   }

   public static ReflectionMethod __construct(
      Env env, Value obj, StringValue name) {
      String clsName;

      if (obj.isObject()) {
         clsName = obj.getClassName();
      } else {
         clsName = obj.toString();
      }

      return new ReflectionMethod(
         clsName, env.getClass(clsName).getFunction(name));
   }

   public static String export(Env env,
                               Value cls,
                               String name,
                               @Optional boolean isReturn) {
      return null;
   }

   public Value invoke(Env env, ObjectValue object, Value[] args) {
      return getFunction().callMethod(
         env, object.getBiancaClass(), object, args);
   }

   public Value invokeArgs(Env env, ObjectValue object, ArrayValue args) {
      return getFunction().callMethod(env, object.getBiancaClass(), object,
         args.getValueArray(env));
   }

   public boolean isFinal() {
      return getFunction().isFinal();
   }

   public boolean isAbstract() {
      return getFunction().isAbstract();
   }

   public boolean isPublic() {
      return getFunction().isPublic();
   }

   public boolean isPrivate() {
      throw new UnimplementedException("isPrivate()");
   }

   public boolean isProtected() {
      return getFunction().isProtected();
   }

   public boolean isStatic() {
      return getFunction().isStatic();
   }

   public boolean isConstructor() {
      return false;
   }

   public boolean isDestructor() {
      return false;
   }

   public int getModifiers() {
      int flag = 1024 * 64; //2^6, some out-of-the-blue number?

      if (isProtected()) {
         flag |= IS_PROTECTED;
      } //else if (isPrivate())
      //flag |= IS_PRIVATE;
      else if (isPublic()) {
         flag |= IS_PUBLIC;
      }

      if (isFinal()) {
         flag |= IS_FINAL;
      }
      if (isAbstract()) {
         flag |= IS_ABSTRACT;
      }
      if (isStatic()) {
         flag |= IS_STATIC;
      }

      return flag;
   }

   public ReflectionClass getDeclaringClass(Env env) {
      String clsName = getFunction().getDeclaringClassName();

      if (clsName == null) {
         throw new BiancaException(
            L.l("class name is null {0}: {1}",
               getFunction(), getFunction().getClass()));
      }

      return new ReflectionClass(env, clsName);
   }

   @Override
   public ArrayValue getParameters(Env env) {
      ArrayValue array = new ArrayValueImpl();

      AbstractFunction fun = getFunction();
      Arg[] args = fun.getArgs();

      for (int i = 0; i < args.length; i++) {
         array.put(env.wrapJava(new ReflectionParameter(env, args[i].getExpectedClass(), fun, args[i])));
      }

      return array;
   }

   @Override
   public String toString() {
      String name;

      if (_clsName != null) {
         name = _clsName + "->" + getName();
      } else {
         name = getName();
      }

      return "ReflectionMethod[" + name + "]";
   }
}