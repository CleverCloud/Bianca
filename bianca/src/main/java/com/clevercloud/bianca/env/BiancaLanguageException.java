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
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.env;

import com.clevercloud.bianca.BiancaException;
import com.clevercloud.bianca.Location;

/**
 * Parent of PHP exceptions
 */
public class BiancaLanguageException extends BiancaException {

   private static final StringValue FILE = new StringValue("file");
   private static final StringValue LINE = new StringValue("line");
   private static final StringValue MESSAGE = new StringValue("message");
   private Value _value;

   public BiancaLanguageException(Value value) {
      super(value.toString());

      _value = value;
   }

   /**
    * Returns the value.
    */
   public Value getValue() {
      return _value;
   }

   /**
    * Converts the exception to a Value.
    */
   public Value toValue(Env env) {
      return _value;
   }

   /*
    * Returns the PHP exception message.  If null, returns the empty string.
    */
   public String getMessage(Env env) {
      Value field = _value.getField(env, MESSAGE);

      if (field != null) {
         return field.toString();
      } else {
         return "";
      }
   }

   /**
    * Returns the location of this PHP exception.
    */
   public Location getLocation(Env env) {
      Value file = _value.getField(env, FILE);
      Value line = _value.getField(env, LINE);

      if (file.isNull() || line.isNull()) {
         return Location.UNKNOWN;
      } else {
         return new Location(file.toString(), line.toInt(), null, null);
      }
   }
}
