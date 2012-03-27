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
 * @author Scott Ferguson
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.module;

import com.clevercloud.bianca.BiancaExitException;
import com.clevercloud.bianca.BiancaModuleException;
import com.clevercloud.bianca.annotation.Name;
import com.clevercloud.bianca.env.JavaInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Represents the introspected static function information.
 */
public class StaticFunction extends JavaInvoker {

   protected final BiancaModule _biancaModule;
   protected final Method _method;
   private final int _argLength;

   /**
    * Creates the statically introspected function.
    *
    * @param method the introspected method.
    */
   public StaticFunction(ModuleContext moduleContext,
           BiancaModule biancaModule,
           Method method) {
      super(moduleContext,
              getName(method),
              method.getParameterTypes(),
              method.getParameterAnnotations(),
              method.getAnnotations(),
              method.getReturnType());

      _method = method;
      _argLength = method.getParameterTypes().length;
      _biancaModule = biancaModule;
   }

   /*
    * Returns true for a static function.
    */
   @Override
   public boolean isStatic() {
      return true;
   }

   private static String getName(Method method) {
      String name;

      Name nameAnn = method.getAnnotation(Name.class);

      if (nameAnn != null) {
         name = nameAnn.value();
      } else {
         name = method.getName();
      }

      return name;
   }

   @Override
   public String getDeclaringClassName() {
      return _method.getDeclaringClass().getSimpleName();
   }

   /**
    * Returns the owning module object.
    *
    * @return the module object
    */
   public BiancaModule getModule() {
      return _biancaModule;
   }

   /**
    * Returns the function's method.
    *
    * @return the reflection method.
    */
   public Method getMethod() {
      return _method;
   }

   public int getArgumentLength() {
      return _argLength;
   }

   /**
    * Evalutes the function.
    */
   @Override
   public Object invoke(Object obj, Object[] javaArgs) {
      try {
         return _method.invoke(_biancaModule, javaArgs);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(toString(_method, javaArgs), e);
      } catch (RuntimeException e) {
         throw e;
      } catch (InvocationTargetException e) {
         // php/03k5
         // exceptions from invoked calls are wrapped inside
         // InvocationTargetException

         Throwable cause = e.getCause();

         if (cause instanceof BiancaExitException) {
            throw ((BiancaExitException) cause);
         } else if (cause != null) {
            throw BiancaModuleException.create(cause);
         } else {
            throw BiancaModuleException.create(e);
         }
      } catch (Exception e) {
         throw BiancaModuleException.create(e);
      }
   }

   private String toString(Method method, Object[] javaArgs) {
      StringBuilder sb = new StringBuilder();

      sb.append(method.getDeclaringClass().getName());
      sb.append(".");
      sb.append(method.getName());
      sb.append("(");

      for (int i = 0; i < javaArgs.length; i++) {
         if (i != 0) {
            sb.append(", ");
         }

         sb.append(javaArgs[i]);
      }

      sb.append(")");

      return sb.toString();
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "[" + _method + "]";
   }
}
