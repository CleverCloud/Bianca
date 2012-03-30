/*
 * Copyright (c) 2012 Clever Cloud SAS -- all rights reserved
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
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.env;

import java.io.InputStream;

/**
 * Represents a Binary value
 */
public class BinaryValue
   extends StringValue {

   public BinaryValue() {
      super();
   }

   public BinaryValue(String buffer, int offset, int length) {
      super(buffer, offset, length);
   }

   /**
    * Creates a new StringValue with the buffer without copying.
    */
   public BinaryValue(String buffer, int length) {
      this(buffer, 0, length);
   }

   public BinaryValue(char ch) {
      super(ch);
   }

   public BinaryValue(byte ch) {
      super(ch);
   }

   public BinaryValue(String s) {
      super(s);
   }

   public BinaryValue(String s, Value v1) {
      super(s, v1);
   }

   public BinaryValue(Value v1) {
      super(v1);
   }

   public BinaryValue(Value v1, Value v2) {
      super(v1, v2);
   }

   public BinaryValue(Value v1, Value v2, Value v3) {
      super(v1, v2, v3);
   }

   /**
    * Returns the type.
    */
   @Override
   public String getType() {
      return "binary";
   }

   /**
    * Convert to an input stream.
    */
   @Override
   public final InputStream toInputStream() {
      return new BinaryValueInputStream(this);
   }

   class BinaryValueInputStream extends java.io.InputStream {

      private final int _length;
      private int _index;
      private BinaryValue _s;

      BinaryValueInputStream(BinaryValue s) {
         _index = 0;
         _s = new BinaryValue(s);
         _length = _s.length();
      }

      /**
       * Reads the next byte.
       */
      @Override
      public int read() {
         if (_index < _length) {
            return _s.charAt(_index++);
         } else {
            return -1;
         }
      }

      /**
       * Reads into a buffer.
       */
      @Override
      public int read(byte[] buffer, int offset, int length) {
         int sublen = _length - _index;

         if (length < sublen) {
            sublen = length;
         }

         if (sublen <= 0) {
            return -1;
         }

         int index = _index;

         for (int i = 0; i < sublen; i++) {
            buffer[offset + i] = (byte) charAt(index + i);
         }

         _index += sublen;

         return sublen;
      }

      @Override
      public int available() {
         return _length - _index;
      }
   }
}
