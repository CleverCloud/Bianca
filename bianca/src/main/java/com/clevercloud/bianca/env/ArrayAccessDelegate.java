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
 * @author Sam
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.env;

/**
 * A delegate that intercepts array acces methods on the
 * target objects that implement
 * the {@link com.clevercloud.bianca.lib.spl.ArrayAccess} interface.
 */
public class ArrayAccessDelegate implements ArrayDelegate {

   private static final StringValue OFFSET_GET = new StringValue("offsetGet");
   private static final StringValue OFFSET_SET = new StringValue("offsetSet");
   private static final StringValue OFFSET_UNSET = new StringValue("offsetUnset");
   private static final StringValue OFFSET_EXISTS = new StringValue("offsetExists");

   @Override
   public Value get(ObjectValue qThis, Value index) {
      Env env = Env.getInstance();

      return qThis.callMethod(env, OFFSET_GET, index);
   }

   @Override
   public Value put(ObjectValue qThis, Value index, Value value) {
      Env env = Env.getInstance();

      return qThis.callMethod(env, OFFSET_SET, index, value);
   }

   @Override
   public Value put(ObjectValue qThis, Value index) {
      Env env = Env.getInstance();

      return qThis.callMethod(env, OFFSET_SET, UnsetValue.UNSET, index);
   }

   @Override
   public boolean isset(ObjectValue qThis, Value index) {
      Env env = Env.getInstance();

      Value returnValue = qThis.getBiancaClass().issetField(env, qThis, index.toString(env));
      if (returnValue == UnsetValue.UNSET) {
         return qThis.callMethod(env, OFFSET_EXISTS, index).toBoolean();
      } else {
         return returnValue.toBoolean();
      }
   }

   @Override
   public Value unset(ObjectValue qThis, Value index) {
      Env env = Env.getInstance();

      return qThis.callMethod(env, OFFSET_UNSET, index);
   }

   @Override
   public long count(ObjectValue qThis) {
      return 1;
   }
}
