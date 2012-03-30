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
package com.clevercloud.bianca;

import java.lang.reflect.InvocationTargetException;

/**
 * Java exception caught and rethrown by modules.
 */
public class BiancaModuleException extends BiancaException {

   public BiancaModuleException() {
   }

   public BiancaModuleException(String msg) {
      super(msg);
   }

   public BiancaModuleException(Throwable cause) {
      super(cause);
   }

   public BiancaModuleException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public static RuntimeException create(Throwable e) {
      if (e instanceof RuntimeException) {
         return (RuntimeException) e;
      } else if (e instanceof InvocationTargetException && e.getCause() != null) {
         return new BiancaModuleException(e.getCause());
      } else {
         return new BiancaModuleException(e);
      }
   }
}
