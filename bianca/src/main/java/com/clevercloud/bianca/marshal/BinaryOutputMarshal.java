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
package com.clevercloud.bianca.marshal;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.JavaValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.expr.Expr;
import com.clevercloud.bianca.lib.file.BinaryOutput;
import com.clevercloud.bianca.lib.file.WriteStreamOutput;

import java.io.OutputStream;

public class BinaryOutputMarshal extends Marshal {

   public static final Marshal MARSHAL = new BinaryOutputMarshal();

   @Override
   public boolean isReadOnly() {
      return true;
   }

   @Override
   public Object marshal(Env env, Expr expr, Class expectedClass) {
      return marshal(env, expr.eval(env), expectedClass);
   }

   @Override
   public Object marshal(Env env, Value value, Class expectedClass) {
      if (value == null) {
         return null;
      } else if (value instanceof BinaryOutput) {
         return (BinaryOutput) value;
      }

      Object javaObj = value.toJavaObject();

      if (javaObj instanceof BinaryOutput) {
         return (BinaryOutput) javaObj;
      } else if (javaObj instanceof OutputStream) {
         return new WriteStreamOutput((OutputStream) javaObj);
      } else {
         throw new IllegalStateException(L.l("Cannot marshal {0} to BinaryOutput",
            javaObj));
      }
   }

   public static BinaryOutput marshal(Env env, Value value) {
      if (value == null) {
         return null;
      } else if (value instanceof BinaryOutput) {
         return (BinaryOutput) value;
      }

      Object javaObj = value.toJavaObject();

      if (javaObj instanceof BinaryOutput) {
         return (BinaryOutput) javaObj;
      } else if (javaObj instanceof OutputStream) {
         return new WriteStreamOutput((OutputStream) javaObj);
      } else {
         throw new IllegalStateException(L.l("Cannot marshal {0} to BinaryOutput",
            javaObj));
      }
   }

   @Override
   public Value unmarshal(Env env, Object value) {
      return (Value) value;
   }

   @Override
   protected int getMarshalingCostImpl(Value argValue) {
      if (argValue instanceof JavaValue
         && OutputStream.class.isAssignableFrom(
         argValue.toJavaObject().getClass())) {
         return Marshal.ZERO;
      } else {
         return Marshal.FOUR;
      }
   }

   @Override
   public Class getExpectedClass() {
      return BinaryOutput.class;
   }
}
