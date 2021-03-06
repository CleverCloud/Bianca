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
 * @author Charles Reich
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.exception;

import com.clevercloud.bianca.BiancaException;
import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.annotation.ClassImplementation;
import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.annotation.This;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.lib.ErrorModule;

import java.util.Iterator;

/**
 * Exception object facade.
 */
@ClassImplementation
public class ExceptionClass {

   private static final StringValue MESSAGE = new StringValue("message");
   private static final StringValue FUNCTION = new StringValue("function");
   private static final StringValue FILE = new StringValue("file");
   private static final StringValue LINE = new StringValue("line");
   private static final StringValue CODE = new StringValue("code");
   private static final StringValue TRACE = new StringValue("trace");
   private static final StringValue JAVA_EXCEPTION = new StringValue("__javaException");

   /**
    * Create a new exception API object.
    */
   public static Value __construct(Env env,
                                   @This ObjectValue value,
                                   @Optional StringValue message,
                                   @Optional("0") int code) {
      value.putField(env, "message", message);
      value.putField(env, "code", LongValue.create(code));

      Location location = env.getLocation();

      if (location != null) {
         if (location.getFileName() != null) {
            value.putField(env, "file", env.createString(location.getFileName()));
         } else {
            value.putField(env, "file", env.createString("unknown"));
         }

         value.putField(env, "line", LongValue.create(location.getLineNumber()));
      }

      value.putField(env, "trace", ErrorModule.debug_backtrace(env, false));
      BiancaException e = new BiancaException();
      e.fillInStackTrace();

      value.putField(env, "_biancaException", env.wrapJava(e));

      return value;
   }

   /**
    * Returns a String representation of this Exception.
    */
   public static Value __toString(Env env, @This ObjectValue value) {
      StringValue sb = new StringValue();

      sb.append("ExceptionClass[" + value.getName() + "]\n");
      sb.append(getMessage(env, value));
      sb.append("\n");
      sb.append(getTraceAsString(env, value));
      sb.append("\n");

      return sb;
   }

   /**
    * Returns the message.
    */
   public static Value getMessage(Env env, @This ObjectValue obj) {
      return obj.getField(env, MESSAGE);
   }

   /**
    * Returns the code.
    */
   public static Value getCode(Env env, @This ObjectValue obj) {
      return obj.getField(env, CODE);
   }

   /**
    * Returns the file.
    */
   public static Value getFile(Env env, @This ObjectValue obj) {
      return obj.getField(env, FILE);
   }

   /**
    * Returns the line.
    */
   public static Value getLine(Env env, @This ObjectValue obj) {
      return obj.getField(env, LINE);
   }

   /**
    * Returns the trace.
    */
   public static Value getTrace(Env env, @This Value obj) {
      return obj.getField(env, TRACE);
   }

   /**
    * Returns the trace.
    */
   public static Value getJavaException(Env env, @This Value obj) {
      return obj.getField(env, JAVA_EXCEPTION);
   }

   /**
    * Returns the trace.
    */
   public static Value getTraceAsString(Env env, @This Value obj) {
      Value trace = getTrace(env, obj);

      StringValue sb = new StringValue();
      sb.append("<trace>");

      Iterator<Value> iter = trace.getValueIterator(env);

      while (iter.hasNext()) {
         Value value = iter.next();

         sb = sb.append('\n');
         sb = sb.append(value.get(FILE));
         sb = sb.append(':');
         sb = sb.append(value.get(LINE));
         sb = sb.append(": ");
         sb = sb.append(value.get(FUNCTION));
      }

      return sb;
   }
}
