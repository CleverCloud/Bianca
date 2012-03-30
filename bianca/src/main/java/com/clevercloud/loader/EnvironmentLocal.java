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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */
package com.clevercloud.loader;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates a ClassLoader dependent variable. The value of the ClassLoaderLocal
 * variable depends on the context ClassLoader.
 */
public class EnvironmentLocal<E> {
   // true on initialization if getting the system classloader is allowed,
   // i.e. not forbiggen by the security manager

   private static Boolean _isSystemClassLoader;

   private static AtomicLong _varCount = new AtomicLong();

   private String _varName;
   private E _globalValue;

   /**
    * Creates a new environment local variable with an anonymous identifier.
    */
   public EnvironmentLocal() {
      _varName = "bianca:var-" + _varCount.incrementAndGet();
   }

   public EnvironmentLocal(String varName) {
      _varName = varName;
   }

   public String getVariable() {
      return _varName;
   }

   /**
    * Returns the variable for the context classloader.
    */
   @SuppressWarnings("unchecked")
   public E get() {
      return _globalValue;
   }

   /**
    * Sets the variable for the context classloader.
    *
    * @param value the new value
    * @return the old value
    */
   @SuppressWarnings("unchecked")
   public final E set(E value) {
      return setGlobal(value);
   }

   /**
    * Removes this variable
    *
    * @return the old value
    */
   @SuppressWarnings("unchecked")
   public final E remove() {
      return setGlobal(null);
   }

   /**
    * Sets the global value.
    *
    * @param value the new value
    * @return the old value
    */
   public E setGlobal(E value) {
      E oldValue = _globalValue;

      _globalValue = value;

      return oldValue;
   }

   public static ClassLoader getSystemClassLoader() {
      if (_isSystemClassLoader == null) {
         _isSystemClassLoader = false;

         try {
            ClassLoader.getSystemClassLoader();

            _isSystemClassLoader = true;
         } catch (Throwable e) {
         }
      }

      if (_isSystemClassLoader)
         return ClassLoader.getSystemClassLoader();
      else
         return null;
   }
}
