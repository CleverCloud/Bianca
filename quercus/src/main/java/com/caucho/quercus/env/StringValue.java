/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
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
package com.caucho.quercus.env;

import com.caucho.quercus.QuercusModuleException;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.lib.file.BinaryInput;
import com.caucho.quercus.marshal.Marshal;
import com.caucho.vfs.ReadStream;
import com.caucho.vfs.TempBuffer;
import com.caucho.vfs.WriteStream;

import java.io.*;
import java.util.IdentityHashMap;
import java.util.zip.CRC32;

/**
 * Represents a PHP string
 */
public class StringValue
   extends Value
   implements CharSequence {

   public static final StringValue EMPTY = new StringValue("");
   private static final StringValue[] CHAR_STRINGS;
   protected static final int IS_STRING = 0;
   protected static final int IS_LONG = 1;
   protected static final int IS_DOUBLE = 2;

   private StringBuilder _buffer;

   public StringValue() {
      _buffer = new StringBuilder();
   }

   public StringValue(String buffer, int offset, int length) {
      if (offset < 0) offset = 0;
      _buffer = new StringBuilder(buffer.substring(Math.min(offset, buffer.length()), Math.min(offset + length, buffer.length())));
   }

   /**
    * Creates a new StringValue with the buffer without copying.
    */
   public StringValue(String buffer, int length) {
      this(buffer, 0, length);
   }

   public StringValue(char ch) {
      _buffer = new StringBuilder().append(ch);
   }

   public StringValue(byte ch) {
      _buffer = new StringBuilder().append(ch);
   }

   public StringValue(String s) {
      _buffer = new StringBuilder(s);
   }

   public StringValue(String s, Value v1) {
      _buffer = new StringBuilder(s);
      v1.appendTo(this);
   }

   public StringValue(Value v1) {
      if (v1 instanceof StringValue) {
         _buffer = new StringBuilder(((StringValue) v1)._buffer);
      } else {
         _buffer = new StringBuilder();
         v1.appendTo(this);
      }
   }

   public StringValue(Value v1, Value v2) {
      _buffer = new StringBuilder();

      v1.appendTo(this);
      v2.appendTo(this);
   }

   public StringValue(Value v1, Value v2, Value v3) {
      _buffer = new StringBuilder();

      v1.appendTo(this);
      v2.appendTo(this);
      v3.appendTo(this);
   }

   public StringValue(TempBuffer head) {
      this();

      // php/0c4l
      append(head);
   }

   /**
    * Creates the string.
    */
   public static Value create(String value) {
      if (value == null) {
         return NullValue.NULL;
      } else if (value.length() == 0) {
         return StringValue.EMPTY;
      } else {
         return new StringValue(value);
      }
   }

   /**
    * Creates the string.
    */
   public static StringValue create(char value) {
      if (value < CHAR_STRINGS.length)
         return CHAR_STRINGS[value];
      return new StringValue();
   }

   /**
    * Creates the string.
    */
   public static Value create(Object value) {
      if (value == null) {
         return NullValue.NULL;
      } else {
         return new StringValue(value.toString());
      }
   }

   /*
    * Decodes the Unicode str from charset.
    *
    * @param str should be a Unicode string
    * @param charset to decode string from
    */
   public StringValue create(Env env, StringValue unicodeStr, String charset) {
      try {
         StringValue sb = new StringValue();

         byte[] bytes = unicodeStr.toString().getBytes(charset);

         sb.append(bytes);
         return sb;

      } catch (UnsupportedEncodingException e) {
         env.warning(e);

         return unicodeStr;
      }
   }

   //
   // Predicates and relations
   //

   /**
    * Returns the type.
    */
   @Override
   public String getType() {
      return "string";
   }

   /**
    * Returns the ValueType.
    */
   @Override
   public ValueType getValueType() {
      return getValueType(_buffer.toString(), 0, _buffer.length());
   }

   public static ValueType getValueType(String buffer,
                                        int offset,
                                        int len) {
      if (len == 0) {
         // php/0307
         return ValueType.LONG_ADD;
      }

      int i = offset;
      int ch = 0;

      while (i < len && Character.isWhitespace(buffer.charAt(i))) {
         i++;
      }

      if (i + 1 < len && buffer.charAt(i) == '0' && buffer.charAt(i + 1) == 'x') {
         return ValueType.LONG_EQ;
      }

      if (i < len && ((ch = buffer.charAt(i)) == '+' || ch == '-')) {
         i++;
      }

      if (len <= i) {
         return ValueType.STRING;
      }

      ch = buffer.charAt(i);

      if (ch == '.') {
         for (i++; i < len && '0' <= (ch = buffer.charAt(i)) && ch <= '9'; i++) {
            return ValueType.DOUBLE_CMP;
         }

         return ValueType.STRING;
      } else if (!('0' <= ch && ch <= '9')) {
         return ValueType.STRING;
      }

      for (; i < len && '0' <= (ch = buffer.charAt(i)) && ch <= '9'; i++) {
      }

      while (i < len && Character.isWhitespace(buffer.charAt(i))) {
         i++;
      }

      if (len <= i) {
         return ValueType.LONG_EQ;
      } else if (ch == '.' || ch == 'e' || ch == 'E') {
         for (i++;
              i < len
                 && ('0' <= (ch = buffer.charAt(i)) && ch <= '9'
                 || ch == '+'
                 || ch == '-'
                 || ch == 'e'
                 || ch == 'E');
              i++) {
         }

         while (i < len && Character.isWhitespace(buffer.charAt(i))) {
            i++;
         }

         if (i < len) {
            return ValueType.STRING;
         } else {
            return ValueType.DOUBLE_CMP;
         }
      } else {
         return ValueType.STRING;
      }
   }

   /**
    * Returns true for a long
    */
   @Override
   public boolean isLongConvertible() {
      return getValueType().isLongCmp();
   }

   /**
    * Returns true for a double
    */
   @Override
   public boolean isDoubleConvertible() {
      return getValueType().isNumberCmp();
   }

   /**
    * Returns true for a number
    */
   public boolean isNumber() {
      return false;
   }

   /**
    * Returns true for is_numeric
    */
   @Override
   public boolean isNumeric() {
      // php/120y

      return getValueType().isNumberCmp();
   }

   /**
    * Returns true for a scalar
    */
   public boolean isScalar() {
      return true;
   }

   /**
    * Returns true for StringValue
    */
   @Override
   public boolean isString() {
      return true;
   }

   /**
    * Returns true if the value is empty
    */
   @Override
   public boolean isEmpty() {
      return length() == 0 || length() == 1 && charAt(0) == '0';
   }

   //
   // marshal cost
   //

   /**
    * Cost to convert to a double
    */
   @Override
   public int toDoubleMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 20;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 10;
      } else {
         return Marshal.COST_INCOMPATIBLE;
      }
   }

   /**
    * Cost to convert to a float
    */
   @Override
   public int toFloatMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 25;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 15;
      } else {
         return Marshal.COST_INCOMPATIBLE;
      }
   }

   /**
    * Cost to convert to a long
    */
   @Override
   public int toLongMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 10;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 40;
      } else {
         return Marshal.COST_INCOMPATIBLE;
      }
   }

   /**
    * Cost to convert to an integer
    */
   @Override
   public int toIntegerMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 10;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 40;
      } else {
         return Marshal.COST_INCOMPATIBLE;
      }
   }

   /**
    * Cost to convert to a short
    */
   @Override
   public int toShortMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 30;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 50;
      } else {
         return Marshal.COST_INCOMPATIBLE;
      }
   }

   /**
    * Cost to convert to a byte
    */
   @Override
   public int toByteMarshalCost() {
      ValueType valueType = getValueType();

      if (valueType.isLongCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 30;
      } else if (valueType.isNumberCmp()) {
         return Marshal.COST_TO_CHAR_ARRAY + 50;
      } else if (isLongConvertible()) {
         return Marshal.COST_NUMERIC_LOSSLESS;
      } else if (isDoubleConvertible()) {
         return Marshal.COST_NUMERIC_LOSSY;
      } else {
         return Marshal.COST_STRING_TO_BYTE;
      }
   }

   /**
    * Cost to convert to a character
    */
   @Override
   public int toCharMarshalCost() {
      return Marshal.COST_STRING_TO_CHAR;
   }

   /**
    * Cost to convert to a String
    */
   @Override
   public int toStringMarshalCost() {
      return Marshal.COST_EQUAL;
   }

   /**
    * Cost to convert to a char[]
    */
   @Override
   public int toCharArrayMarshalCost() {
      return Marshal.COST_STRING_TO_CHAR_ARRAY;
   }

   /**
    * Cost to convert to a StringValue
    */
   @Override
   public int toStringValueMarshalCost() {
      return Marshal.COST_IDENTICAL;
   }

   /**
    * Returns true for equality
    */
   @Override
   public int cmp(Value rValue) {
      if (isNumberConvertible() || rValue.isNumberConvertible()) {
         double l = toDouble();
         double r = rValue.toDouble();

         if (l == r) {
            return 0;
         } else if (l < r) {
            return -1;
         } else {
            return 1;
         }
      } else {
         int result = toString().compareTo(rValue.toString());

         if (result == 0) {
            return 0;
         } else if (result > 0) {
            return 1;
         } else {
            return -1;
         }
      }
   }

   /**
    * Returns true for equality
    */
   @Override
   public boolean eq(Value rValue) {
      ValueType typeA = getValueType();
      ValueType typeB = rValue.getValueType();

      if (typeB.isNumber()) {
         double l = toDouble();
         double r = rValue.toDouble();

         return l == r;
      } else if (typeB.isBoolean()) {
         return toBoolean() == rValue.toBoolean();
      } else if (typeA.isNumberCmp() && typeB.isNumberCmp()) {
         double l = toDouble();
         double r = rValue.toDouble();

         return l == r;
      } else {
         return toString().equals(rValue.toString());
      }
   }

   /**
    * Compare two strings
    */
   public int cmpString(StringValue rValue) {
      return toString().compareTo(rValue.toString());
   }

   // Conversions

   /**
    * Converts to a string value.
    */
   @Override
   public StringValue toStringValue() {
      return this;
   }

   /**
    * Converts to a string value.
    */
   @Override
   public StringValue toStringValue(Env env) {
      return this;
   }


   /**
    * Converts to a long.
    */
   @Override
   public long toLong() {
      return toLong(_buffer.toString());
   }

   public static long toLong(String string) {
      return parseLong(string);
   }

   /**
    * String to long conversion routines used by this module
    * and other modules in this package. These methods are
    * only invoked by other implementations of a "string" object.
    * The 3 implementations should be identical except for the
    * char data source.
    */
   static long parseLong(String buffer, int offset, int len) {
      if (len == 0) {
         return 0;
      }

      long value = 0;
      long sign = 1;
      boolean isResultSet = false;
      long result = 0;

      int end = offset + len;

      while (offset < end && Character.isWhitespace(buffer.charAt(offset))) {
         offset++;
      }

      int ch;

      if (offset + 1 < end && buffer.charAt(offset) == '0'
         && ((ch = buffer.charAt(offset + 1)) == 'x' || ch == 'X')) {

         for (offset += 2; offset < end; offset++) {
            ch = buffer.charAt(offset);

            long oldValue = value;

            if ('0' <= ch && ch <= '9') {
               value = value * 16 + ch - '0';
            } else if ('a' <= ch && ch <= 'z') {
               value = value * 16 + ch - 'a' + 10;
            } else if ('A' <= ch && ch <= 'Z') {
               value = value * 16 + ch - 'A' + 10;
            } else {
               return value;
            }

            if (value < oldValue) {
               return Integer.MAX_VALUE;
            }
         }

         return value;
      }

      if (offset < end && buffer.charAt(offset) == '-') {
         sign = -1;
         offset++;
      } else if (offset < end && buffer.charAt(offset) == '+') {
         sign = +1;
         offset++;
      }

      while (offset < end) {
         ch = buffer.charAt(offset++);

         if ('0' <= ch && ch <= '9') {
            long newValue = 10 * value + ch - '0';
            if (newValue < value) {
               // php/0143
               // long value overflowed
               result = Integer.MAX_VALUE;
               isResultSet = true;
               break;
            }
            value = newValue;
         } else {
            result = sign * value;
            isResultSet = true;
            break;
         }
      }

      if (!isResultSet) {
         result = sign * value;
      }

      return result;
   }

   static long parseLong(CharSequence string) {
      final int len = string.length();

      if (len == 0) {
         return 0;
      }

      long value = 0;
      long sign = 1;
      boolean isResultSet = false;
      long result = 0;

      int offset = 0;
      int end = offset + len;

      while (offset < end && Character.isWhitespace(string.charAt(offset))) {
         offset++;
      }

      if (offset < end && string.charAt(offset) == '-') {
         sign = -1;
         offset++;
      } else if (offset < end && string.charAt(offset) == '+') {
         sign = +1;
         offset++;
      }

      while (offset < end) {
         int ch = string.charAt(offset++);

         if ('0' <= ch && ch <= '9') {
            long newValue = 10 * value + ch - '0';
            if (newValue < value) {
               // long value overflowed, set result to integer max
               result = Integer.MAX_VALUE;
               isResultSet = true;
               break;
            }
            value = newValue;
         } else {
            result = sign * value;
            isResultSet = true;
            break;
         }
      }

      if (!isResultSet) {
         result = sign * value;
      }

      return result;
   }

   /**
    * Converts to a double.
    */
   @Override
   public double toDouble() {
      return toDouble(toString());
   }

   /**
    * Converts to a double.
    */
   public static double toDouble(String s) {
      int len = s.length();

      int start = 0;

      int i = 0;
      int ch = 0;

      while (i < len && Character.isWhitespace(s.charAt(i))) {
         start++;
         i++;
      }

      if (i + 1 < len && s.charAt(i) == '0'
         && ((ch = s.charAt(i)) == 'x' || ch == 'X')) {

         double value = 0;

         for (i += 2; i < len; i++) {
            ch = s.charAt(i);

            if ('0' <= ch && ch <= '9') {
               value = value * 16 + ch - '0';
            } else if ('a' <= ch && ch <= 'z') {
               value = value * 16 + ch - 'a' + 10;
            } else if ('A' <= ch && ch <= 'Z') {
               value = value * 16 + ch - 'A' + 10;
            } else {
               return value;
            }
         }

         return value;
      }

      if (i < len && ((ch = s.charAt(i)) == '+' || ch == '-')) {
         i++;
      }

      for (; i < len && '0' <= (ch = s.charAt(i)) && ch <= '9'; i++) {
      }

      if (ch == '.') {
         for (i++; i < len && '0' <= (ch = s.charAt(i)) && ch <= '9'; i++) {
         }
      }

      if (ch == 'e' || ch == 'E') {
         int e = i++;

         if (i < len && (ch = s.charAt(i)) == '+' || ch == '-') {
            i++;
         }

         for (; i < len && '0' <= (ch = s.charAt(i)) && ch <= '9'; i++) {
         }

         if (i == e + 1) {
            i = e;
         }
      }

      if (i == 0) {
         return 0;
      } else if (i == len && start == 0) {
         return Double.parseDouble(s);
      } else {
         return Double.parseDouble(s.substring(Math.max(0, start), i));
      }
   }

   public static double toDouble(String buffer, int offset, int len) {
      int start = offset;
      int i = offset;
      int ch = 0;

      while (i < len && Character.isWhitespace(buffer.charAt(i))) {
         start++;
         i++;
      }

      int end = offset + len;

      if (offset + 1 < end && buffer.charAt(offset) == '0'
         && ((ch = buffer.charAt(offset + 1)) == 'x' || ch == 'X')) {

         double value = 0;

         for (offset += 2; offset < end; offset++) {
            ch = buffer.charAt(offset);

            if ('0' <= ch && ch <= '9') {
               value = value * 16 + ch - '0';
            } else if ('a' <= ch && ch <= 'z') {
               value = value * 16 + ch - 'a' + 10;
            } else if ('A' <= ch && ch <= 'Z') {
               value = value * 16 + ch - 'A' + 10;
            } else {
               return value;
            }
         }

         return value;
      }

      if (i < len && ((ch = buffer.charAt(i)) == '+' || ch == '-')) {
         i++;
      }

      for (; i < len && '0' <= (ch = buffer.charAt(i)) && ch <= '9'; i++) {
      }

      if (ch == '.') {
         for (i++; i < len && '0' <= (ch = buffer.charAt(i)) && ch <= '9'; i++) {
         }

         if (i == 1) {
            return 0;
         }
      }

      if (ch == 'e' || ch == 'E') {
         int e = i++;

         if (i < len && (ch = buffer.charAt(i)) == '+' || ch == '-') {
            i++;
         }

         for (; i < len && '0' <= (ch = buffer.charAt(i)) && ch <= '9'; i++) {
         }

         if (i == e + 1) {
            i = e;
         }
      }

      if (i == 0) {
         return 0;
      }

      if (start < 0) start = 0;
      try {
         return Double.parseDouble(buffer.substring(Math.min(start, buffer.length()), Math.min(i - start, buffer.length())));
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   /**
    * Converts to a boolean.
    */
   @Override
   public final boolean toBoolean() {
      if (_buffer.length() == 0) {
         return false;
      } else {
         return (_buffer.length() != 1 || _buffer.charAt(0) != '0');
      }
   }

   /**
    * Converts to a key.
    */
   @Override
   public Value toKey() {
      int len = length();

      if (len == 0) {
         return this;
      }

      int sign = 1;
      long value = 0;
      String buffer = toString();

      int i = 0;
      char ch = buffer.charAt(i++);
      if (ch == '-') {
         sign = -1;
      } else if ('0' <= ch && ch <= '9') {
         value = ch - '0';
      } else {
         return this;
      }

      for (; i < len; i++) {
         ch = charAt(i);

         if ('0' <= ch && ch <= '9') {
            value = 10 * value + ch - '0';
         } else {
            return this;
         }
      }

      return LongValue.create(sign * value);
   }

   /**
    * Converts to an object.
    */
   @Override
   final public Value toAutoObject(Env env) {
      return env.createObject();
   }

   /**
    * Converts to an array if null.
    */
   @Override
   public Value toAutoArray() {
      if (length() == 0) {
         return new ArrayValueImpl();
      } else {
         return this;
      }
   }

   /**
    * Converts to a Java object.
    */
   @Override
   public Object toJavaObject() {
      return toString();
   }

   /**
    * Takes the values of this array, unmarshalls them to objects of type
    * <i>elementType</i>, and puts them in a java array.
    */
   @Override
   public Object valuesToArray(Env env, Class elementType) {
      if (char.class.equals(elementType)) {
         return toCharArray();
      } else if (Character.class.equals(elementType)) {
         char[] chars = toCharArray();

         int length = chars.length;

         Character[] charObjects = new Character[length];

         for (int i = 0; i < length; i++) {
            charObjects[i] = Character.valueOf(chars[i]);
         }

         return charObjects;
      } else if (byte.class.equals(elementType)) {
         return toStringValue(env).toString().getBytes();
      } else if (Byte.class.equals(elementType)) {
         byte[] bytes = toStringValue(env).toString().getBytes();

         int length = bytes.length;

         Byte[] byteObjects = new Byte[length];

         for (int i = 0; i < length; i++) {
            byteObjects[i] = Byte.valueOf(bytes[i]);
         }

         return byteObjects;
      } else {
         env.error(L.l("Can't assign {0} with type {1} to {2}",
            this,
            this.getClass(),
            elementType));
         return null;
      }
   }

   /**
    * Converts to a callable object
    */
   @Override
   public Callable toCallable(Env env) {
      // php/1h0o
      if (isEmpty()) {
         return super.toCallable(env);
      }

      String s = toString();

      int p = s.indexOf("::");

      if (p < 0) {
         return new CallbackFunction(env, s);
      } else {
         String className = s.substring(0, p);
         String methodName = s.substring(p + 2);

         QuercusClass cl = env.findClass(className);

         if (cl == null) {
            env.warning(L.l("can't find class {0}",
               className));

            return super.toCallable(env);
         }

         return new CallbackClassMethod(cl, env.createString(methodName));
      }
   }

   /**
    * Sets the array value, returning the new array, e.g. to handle
    * string update ($a[0] = 'A').  Creates an array automatically if
    * necessary.
    */
   @Override
   public Value append(Value index, Value value) {
      if (_buffer.length() > 0) {
         return setCharValueAt(index.toLong(), value);
      } else {
         return new ArrayValueImpl().append(index, value);
      }
   }

   // Operations

   /**
    * Returns the character at an index
    */
   @Override
   public Value get(Value key) {
      return charValueAt(key.toLong());
   }

   @Override
   public Value put(Value index, Value value) {
      setCharValueAt(index.toLong(), value);

      return value;
   }

   /**
    * Returns the character at an index
    */
   @Override
   public Value getArg(Value key, boolean isTop) {
      // php/03ma
      return charValueAt(key.toLong());
   }

   /**
    * Returns the character at an index
    */
   @Override
   public Value charValueAt(long index) {
      int len = length();

      if (index < 0 || len <= index) {
         return StringValue.EMPTY;
      } else {
         return StringValue.create(charAt((int) index));
      }
   }

   /**
    * sets the character at an index
    */
   @Override
   public Value setCharValueAt(long indexL, Value value) {
      int len = _buffer.length();

      if (indexL < 0) {
         return this;
      } else if (indexL < len) {
         StringValue sb = new StringValue(_buffer.toString());

         StringValue str = value.toStringValue();

         int index = (int) indexL;

         if (value.length() == 0) {
            sb._buffer.setCharAt(index, (char) 0);
         } else {
            sb._buffer.setCharAt(index, str.charAt(0));
         }

         return sb;
      } else {
         // php/03mg, #2940

         int index = (int) indexL;

         StringValue sb = new StringValue(this);

         int padLen = index - len;

         for (int i = 0; i <= padLen; i++) {
            sb._buffer.append(' ');
         }

         StringValue str = value.toStringValue();

         if (value.length() == 0) {
            sb._buffer.setCharAt(index, (char) 0);
         } else {
            sb._buffer.setCharAt(index, str.charAt(0));
         }

         return sb;
      }
   }

   /**
    * Converts to a string.
    */
   @Override
   public String toString() {
      return _buffer.toString();
   }


   /**
    * Increment the following value.
    */
   @Override
   public Value increment(int incr) {
      // php/03i6
      if (length() == 0) {
         if (incr == 1) {
            return new StringValue().append("1");
         } else {
            return LongValue.MINUS_ONE;
         }
      }

      if (incr > 0) {
         StringBuilder tail = new StringBuilder();

         for (int i = length() - 1; i >= 0; i--) {
            char ch = charAt(i);

            if (ch == 'z') {
               if (i == 0) {
                  return new StringValue().append("aa").append(tail);
               } else {
                  tail.insert(0, 'a');
               }
            } else if ('a' <= ch && ch < 'z') {
               return (new StringValue().append(this, 0, i).append((char) (ch + 1)).append(tail));
            } else if (ch == 'Z') {
               if (i == 0) {
                  return new StringValue().append("AA").append(tail);
               } else {
                  tail.insert(0, 'A');
               }
            } else if ('A' <= ch && ch < 'Z') {
               return (new StringValue().append(this, 0, i).append((char) (ch + 1)).append(tail));
            } else if ('0' <= ch && ch <= '9' && i == length() - 1) {
               return LongValue.create(toLong() + incr);
            }
         }

         return new StringValue().append(tail.toString());
      } else if (getValueType().isLongAdd()) {
         return LongValue.create(toLong() + incr);
      } else {
         return this;
      }
   }

   /**
    * Adds to the following value.
    */
   @Override
   public Value add(long rValue) {
      if (getValueType().isLongAdd()) {
         return LongValue.create(toLong() + rValue);
      }

      return DoubleValue.create(toDouble() + rValue);
   }

   /**
    * Adds to the following value.
    */
   @Override
   public Value sub(long rValue) {
      if (getValueType().isLongAdd()) {
         return LongValue.create(toLong() - rValue);
      }

      return DoubleValue.create(toDouble() - rValue);
   }

   /*
    * Bit and.
    */
   @Override
   public Value bitAnd(Value rValue) {
      if (rValue.isString()) {
         StringValue rStr = (StringValue) rValue;

         int len = Math.min(length(), rValue.length());
         StringValue sb = new StringValue();

         for (int i = 0; i < len; i++) {
            char l = charAt(i);
            char r = rStr.charAt(i);

            sb.append(l & r);
         }

         return sb;
      } else {
         return LongValue.create(toLong() & rValue.toLong());
      }
   }

   /*
    * Bit or.
    */
   @Override
   public Value bitOr(Value rValue) {
      if (rValue.isString()) {
         StringValue rStr = (StringValue) rValue;

         int len = Math.min(length(), rValue.length());
         StringValue sb = new StringValue();

         for (int i = 0; i < len; i++) {
            char l = charAt(i);
            char r = rStr.charAt(i);

            sb.append(l | r);
         }

         if (len != length()) {
            sb.append(substring(len));
         } else if (len != rStr.length()) {
            sb.append(rStr.substring(len));
         }

         return sb;
      } else {
         return LongValue.create(toLong() | rValue.toLong());
      }
   }

   /*
    * Bit xor.
    */
   @Override
   public Value bitXor(Value rValue) {
      if (rValue.isString()) {
         StringValue rStr = rValue.toStringValue();

         int len = Math.min(length(), rValue.length());
         StringValue sb = new StringValue();

         for (int i = 0; i < len; i++) {
            char l = charAt(i);
            char r = rStr.charAt(i);

            sb.append(l ^ r);
         }

         return sb;
      } else {
         return LongValue.create(toLong() ^ rValue.toLong());
      }
   }

   /**
    * Serializes the value.
    */
   @Override
   public void serialize(Env env, StringBuilder sb) {
      sb.append("s:");
      sb.append(length());
      sb.append(":\"");
      sb.append(toString());
      sb.append("\";");
   }

   /**
    * Encodes the value in JSON.
    */
   @Override
   public void jsonEncode(Env env, StringValue sb) {
      sb.append('"');

      int len = length();
      for (int i = 0; i < len; i++) {
         char c = charAt(i);

         switch (c) {
            case '\b':
               sb.append('\\');
               sb.append('b');
               break;
            case '\f':
               sb.append('\\');
               sb.append('f');
               break;
            case '\n':
               sb.append('\\');
               sb.append('n');
               break;
            case '\r':
               sb.append('\\');
               sb.append('r');
               break;
            case '\t':
               sb.append('\\');
               sb.append('t');
               break;
            case '\\':
               sb.append('\\');
               sb.append('\\');
               break;
            case '"':
               sb.append('\\');
               sb.append('"');
               break;
            case '/':
               sb.append('\\');
               sb.append('/');
               break;
            default:
               if (c <= 0x1f) {
                  addUnicode(sb, c);
               } else if (c < 0x80) {
                  sb.append(c);
               } else if ((c & 0xe0) == 0xc0 && i + 1 < len) {
                  int c1 = charAt(i + 1);
                  i++;

                  int ch = ((c & 0x1f) << 6) + (c1 & 0x3f);

                  addUnicode(sb, ch);
               } else if ((c & 0xf0) == 0xe0 && i + 2 < len) {
                  int c1 = charAt(i + 1);
                  int c2 = charAt(i + 2);

                  i += 2;

                  int ch = ((c & 0x0f) << 12) + ((c1 & 0x3f) << 6) + (c2 & 0x3f);

                  addUnicode(sb, ch);
               } else {
                  // technically illegal
                  addUnicode(sb, c);
               }

               break;
         }
      }

      sb.append('"');
   }

   private void addUnicode(StringValue sb, int c) {
      sb.append('\\');
      sb.append('u');

      int d = (c >> 12) & 0xf;
      if (d < 10) {
         sb.append((char) ('0' + d));
      } else {
         sb.append((char) ('a' + d - 10));
      }

      d = (c >> 8) & 0xf;
      if (d < 10) {
         sb.append((char) ('0' + d));
      } else {
         sb.append((char) ('a' + d - 10));
      }

      d = (c >> 4) & 0xf;
      if (d < 10) {
         sb.append((char) ('0' + d));
      } else {
         sb.append((char) ('a' + d - 10));
      }

      d = (c) & 0xf;
      if (d < 10) {
         sb.append((char) ('0' + d));
      } else {
         sb.append((char) ('a' + d - 10));
      }
   }

   /*
    * Returns a value to be used as a key for the deserialize cache.
    */
   /*
   public StringValue toSerializeKey()
   {
   if (length() <= 4096)
   return this;

   try {
   MessageDigest md = MessageDigest.getInstance("SHA1");

   byte []buffer = toBytes();

   md.update(buffer, 0, buffer.length);

   //XXX: create a special serialize type?
   return new StringValue(md.digest());

   } catch (NoSuchAlgorithmException e) {
   throw new QuercusException(e);
   }
   }
    */
   //
   // append code
   //

   /**
    * Append a Java string to the value.
    */
   public final StringValue append(String s) {
      _buffer.append(s);
      return this;
   }

   /**
    * Append a Java string to the value.
    */
   public final StringValue append(String s, int start, int end) {
      if (start < 0) start = 0;
      if (end > start)
         _buffer.append(s.substring(Math.min(start, s.length()), Math.min(end, s.length())));
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   public final StringValue append(char[] buf, int offset, int length) {
      _buffer.append(buf, offset, length);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   public final StringValue append(char[] buf) {
      _buffer.append(buf);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   public final StringValue append(CharSequence buf, int head, int tail) {
      _buffer.append(buf, head, tail);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   public StringValue append(StringValue sb, int head, int tail) {
      return append((CharSequence) sb, head, tail);
   }

   /**
    * Append a Java char to the value.
    */
   public final StringValue append(char ch) {
      _buffer.append(ch);
      return this;
   }

   /**
    * Append a Java boolean to the value.
    */
   public StringValue append(boolean v) {
      return append(v ? "true" : "false");
   }

   /**
    * Append a Java long to the value.
    */
   public StringValue append(long v) {
      return append(String.valueOf(v));
   }

   /**
    * Append a Java double to the value.
    */
   public StringValue append(double v) {
      return append(String.valueOf(v));
   }

   /**
    * Append a Java object to the value.
    */
   public StringValue append(Object v) {
      return append(v.toString());
   }

   /**
    * Append a Java value to the value.
    */
   public StringValue append(Value v) {
      append(v.toString());
      return this;
   }

   /**
    * Append to a string builder.
    */
   @Override
   public StringValue appendTo(StringValue sb) {
      if (length() == 0) {
         return sb;
      }

      Env env = Env.getInstance();

      try {
         Reader reader = env.getRuntimeEncodingFactory().create(toInputStream());

         if (reader != null) {
            sb.append(reader);

            reader.close();
         }

         return sb;
      } catch (IOException e) {
         throw new QuercusRuntimeException(e);
      }
   }

   /**
    * Append from a read stream
    */
   public StringValue append(Reader reader)
      throws IOException {
      int ch;

      while ((ch = reader.read()) >= 0) {
         append((char) ch);
      }

      return this;
   }

   /**
    * Append from a read stream
    */
   public StringValue append(Reader reader, long length)
      throws IOException {
      int ch;

      while (length-- > 0 && (ch = reader.read()) >= 0) {
         append((char) ch);
      }

      return this;
   }

   /**
    * Append from an input stream, using InputStream.read semantics,
    * i.e. just call is.read once even if more data is available.
    */
   public int appendRead(InputStream is, long length) {
      TempBuffer tBuf = TempBuffer.allocate();
      try {
         byte[] buffer = tBuf.getBuffer();
         int sublen = buffer.length;
         if (length < sublen) {
            sublen = (int) length;
         }

         sublen = is.read(buffer, 0, sublen);

         if (sublen > 0) {
            append(new String(buffer), 0, sublen);
         }

         return sublen;
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempBuffer.free(tBuf);
      }
   }

   /**
    * Append from an input stream, reading from the input stream until
    * end of file or the length is reached.
    */
   public int appendReadAll(InputStream is, long length) {
      TempBuffer tBuf = TempBuffer.allocate();

      try {
         byte[] buffer = tBuf.getBuffer();
         int readLength = 0;

         while (length > 0) {
            int sublen = buffer.length;
            if (length < sublen) {
               sublen = (int) length;
            }

            sublen = is.read(buffer, 0, sublen);

            if (sublen > 0) {
               append(new String(buffer), 0, sublen);
               length -= sublen;
               readLength += sublen;
            } else {
               return readLength > 0 ? readLength : -1;
            }
         }

         return readLength;
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempBuffer.free(tBuf);
      }
   }

   /**
    * Append from an input stream, reading from the input stream until
    * end of file or the length is reached.
    */
   public int appendReadAll(ReadStream is, long length) {
      TempBuffer tBuf = TempBuffer.allocate();

      try {
         byte[] buffer = tBuf.getBuffer();
         int readLength = 0;

         while (length > 0) {
            int sublen = buffer.length;
            if (length < sublen) {
               sublen = (int) length;
            }

            sublen = is.read(buffer, 0, sublen);

            if (sublen > 0) {
               append(new String(buffer), 0, sublen);
               length -= sublen;
               readLength += sublen;
            } else {
               return readLength > 0 ? readLength : -1;
            }
         }

         return readLength;
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempBuffer.free(tBuf);
      }
   }

   /**
    * Append from an input stream, using InputStream semantics, i.e
    * call is.read() only once.
    */
   public int appendRead(BinaryInput is, long length) {
      TempBuffer tBuf = TempBuffer.allocate();

      try {
         byte[] buffer = tBuf.getBuffer();
         int sublen = buffer.length;
         if (length < sublen) {
            sublen = (int) length;
         } else if (length > sublen) {
            buffer = new byte[(int) length];
            sublen = (int) length;
         }

         sublen = is.read(buffer, 0, sublen);

         if (sublen > 0) {
            append(new String(buffer), 0, sublen);
         }

         return sublen;
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempBuffer.free(tBuf);
      }
   }

   /**
    * Append from an input stream, reading all available data from the
    * stream.
    */
   public int appendReadAll(BinaryInput is, long length) {
      TempBuffer tBuf = TempBuffer.allocate();

      try {
         byte[] buffer = tBuf.getBuffer();
         int readLength = 0;

         while (length > 0) {
            int sublen = buffer.length;
            if (length < sublen) {
               sublen = (int) length;
            }

            sublen = is.read(buffer, 0, sublen);

            if (sublen > 0) {
               append(new String(buffer), 0, sublen);
               length -= sublen;
               readLength += sublen;
            } else {
               return readLength > 0 ? readLength : -1;
            }
         }

         return readLength;
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempBuffer.free(tBuf);
      }
   }

   /**
    * Exports the value.
    */
   @Override
   public void varExport(StringBuilder sb) {
      sb.append("'");

      String value = toString();
      int len = value.length();
      for (int i = 0; i < len; i++) {
         char ch = value.charAt(i);

         switch (ch) {
            case '\'':
               sb.append("\\'");
               break;
            case '\\':
               sb.append("\\\\");
               break;
            default:
               sb.append(ch);
         }
      }
      sb.append("'");
   }

   /**
    * Interns the string.
    */
   /*
   public StringValue intern(Quercus quercus)
   {
   return quercus.intern(toString());
   }
    */
   //
   // CharSequence
   //

   /**
    * Returns the length of the string.
    */
   @Override
   public int length() {
      return _buffer.length();
   }

   /**
    * Returns the character at a particular location
    */
   @Override
   public char charAt(int index) {
      if (index < 0 || _buffer.length() <= index) {
         return 0;
      } else {
         return _buffer.charAt(index);
      }
   }

   /**
    * Returns a subsequence
    */
   @Override
   public CharSequence subSequence(int start, int end) {
      if (end <= start) {
         return StringValue.EMPTY;
      } else if (end - start == 1) {
         return CHAR_STRINGS[charAt(start)];
      }

      return substring(start, end);
   }

   //
   // java.lang.String methods
   //

   /**
    * Returns the first index of the match string, starting from the head.
    */
   public final int indexOf(CharSequence match) {
      return indexOf(match, 0);
   }

   /**
    * Returns the first index of the match string, starting from the head.
    */
   public int indexOf(CharSequence match, int head) {
      return substring(head).toString().indexOf(match.toString());
   }

   /**
    * Returns the last index of the match string, starting from the head.
    */
   public int indexOf(char match) {
      return indexOf(match, 0);
   }

   /**
    * Returns the last index of the match string, starting from the head.
    */
   public int indexOf(char match, int head) {
      return substring(head).toString().indexOf(match);
   }

   /**
    * Returns the last index of the match string, starting from the head.
    */
   public final int lastIndexOf(char match) {
      return lastIndexOf(match, Integer.MAX_VALUE);
   }

   /**
    * Returns the last index of the match string, starting from the head.
    */
   public int lastIndexOf(char match, int tail) {
      String str = toString();
      if (str.isEmpty())
         return -1;
      int max = Math.min(str.length(), tail);
      return str.substring(0, max).lastIndexOf(match);
   }

   /**
    * Returns the last index of the match string, starting from the tail.
    */
   public int lastIndexOf(CharSequence match) {
      return lastIndexOf(match, Integer.MAX_VALUE);
   }

   /**
    * Returns the last index of the match string, starting from the tail.
    */
   public int lastIndexOf(CharSequence match, int tail) {
      return substring(0, tail).lastIndexOf(match.toString());
   }

   /**
    * Returns true if the region matches
    */
   public boolean regionMatches(int offset,
                                String mBuffer, int mOffset) {
      int length = mBuffer.length();
      if (offset < 0) offset = 0;
      if (mOffset < 0) mOffset = 0;
      return toString().substring(offset, Math.min(offset + length, length())).equals(mBuffer.substring(Math.min(mOffset, length - 1)));
   }

   /**
    * Returns true if the region matches
    */
   public boolean regionMatchesIgnoreCase(int offset,
                                          String mBuffer, int mOffset) {
      int length = mBuffer.length();
      if (offset < 0) offset = 0;
      if (mOffset < 0) mOffset = 0;
      return toString().toLowerCase().substring(offset, Math.min(offset + length, length())).equals(mBuffer.substring(Math.min(mOffset, length - 1)).toLowerCase());
   }

   /**
    * Returns true if the region matches
    */
   public boolean regionMatches(int offset,
                                StringValue match, int mOffset) {
      return regionMatches(offset, match.toString(), mOffset);
   }

   /**
    * Returns true if the string ends with another string.
    */
   public boolean endsWith(StringValue tail) {
      return toString().endsWith(tail.toString());
   }

   /**
    * Returns a StringValue substring.
    */
   public StringValue substring(int head) {
      String thisVal = toString();
      if (head < 0) head = 0;
      return new StringValue(thisVal.substring(Math.min(head, thisVal.length())));
   }

   /**
    * Returns a StringValue substring.
    */
   public StringValue substring(int begin, int end) {
      return new StringValue(stringSubstring(begin, end));
   }

   /**
    * Returns a String substring
    */
   public String stringSubstring(int begin, int end) {
      String thisVal = toString();
      if (begin < 0) begin = 0;
      if (end < begin) return "";
      return thisVal.substring(begin, end);
   }

   /**
    * Returns a character array
    */
   public char[] toCharArray() {
      return toString().toCharArray();
   }

   public char[] getRawCharArray() {
      return toCharArray();
   }

   /**
    * Convert to lower case.
    */
   public StringValue toLowerCase() {
      return new StringValue(toString().toLowerCase());
   }

   /**
    * Convert to lower case.
    */
   public StringValue toUpperCase() {
      return new StringValue(toString().toUpperCase());
   }

   /**
    * Convert to an input stream.
    */
   @Override
   public final InputStream toInputStream() {
      return new StringValueInputStream();
   }

   public Reader toSimpleReader()
      throws UnsupportedEncodingException {
      return new SimpleStringValueReader(this);
   }

   /**
    * Returns a char stream.
    */
   public Reader toReader()
      throws UnsupportedEncodingException {
      return new InputStreamReader(toInputStream());
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder() {
      return new StringValue(this);
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env) {
      return new StringValue(this);
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env, Value value) {
      StringValue v = new StringValue(this);

      value.appendTo(v);

      return v;
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env, StringValue value) {
      StringValue v = new StringValue(this);

      value.appendTo(v);

      return v;
   }

   /**
    * Return true if the array value is set
    */
   @Override
   public boolean isset(Value indexV) {
      int index = indexV.toInt();
      int len = length();

      return 0 <= index && index < len;
   }

   /**
    * Writes to a stream
    */
   public final void writeTo(OutputStream os) {
      try {
         os.write(_buffer.toString().getBytes(), 0, _buffer.length());
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      }
   }

   /**
    * Calculates CRC32 value.
    */
   public long getCrc32Value() {
      CRC32 crc = new CRC32();

      crc.update(_buffer.toString().getBytes(), 0, length());

      return crc.getValue() & 0xffffffff;
   }

   //
   // java.lang.Object methods
   //

   /**
    * Returns the hash code.
    */
   @Override
   public int hashCode() {
      return toString().hashCode();
   }

   /**
    * Returns the case-insensitive hash code
    */
   public int hashCodeCaseInsensitive() {
      return toString().toLowerCase().hashCode();
   }

   /**
    * Test for equality
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof StringValue)) {
         return false;
      }
      return toString().equals(o.toString());
   }

   @Override
   public boolean eql(Value o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof StringValue)) {
         return false;
      }
      return toString().equals(o.toString());
   }

   /**
    * Test for equality
    */
   public boolean equalsIgnoreCase(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof StringValue)) {
         return false;
      }

      StringValue s = (StringValue) o;

      int aLength = length();
      int bLength = s.length();

      if (aLength != bLength) {
         return false;
      }

      for (int i = aLength - 1; i >= 0; i--) {
         int chA = charAt(i);
         int chB = s.charAt(i);

         if (chA == chB) {
         } else {
            if ('A' <= chA && chA <= 'Z') {
               chA += 'a' - 'A';
            }

            if ('A' <= chB && chB <= 'Z') {
               chB += 'a' - 'A';
            }

            if (chA != chB) {
               return false;
            }
         }
      }

      return true;
   }

   //
   // Java generator code
   //

   /**
    * Generates code to recreate the expression.
    *
    * @param out the writer to the Java source code.
    */
   @Override
   public void generate(PrintWriter out)
      throws IOException {
      // max JVM constant string length
      int maxSublen = 0xFFFE;

      int len = length();

      String className = getClass().getSimpleName();

      if (len == 1) {
         out.print(className + ".create('");
         printJavaChar(out, charAt(0));
         out.print("')");
      } else if (len < maxSublen) {
         out.print("new " + className + "(\"");
         printJavaString(out, this);
         out.print("\")");
      } else {
         out.print("((" + className + ") (new " + className + "(\"");

         // php/313u
         for (int i = 0; i < len; i += maxSublen) {
            if (i != 0) {
               out.print("\").append(\"");
            }

            printJavaString(out, substring(i, Math.min(i + maxSublen, len)));
         }

         out.print("\")))");
      }
   }

   @Override
   public String toDebugString() {
      StringBuilder sb = new StringBuilder();

      int length = length();

      sb.append("string(");
      sb.append(length);
      sb.append(") \"");

      int appendLength = length < 256 ? length : 256;

      for (int i = 0; i < appendLength; i++) {
         sb.append(charAt(i));
      }

      if (length > 256) {
         sb.append(" ...");
      }

      sb.append('"');

      return sb.toString();
   }

   /**
    * Returns an OutputStream.
    */
   public OutputStream getOutputStream() {
      return new StringValueOutputStream();
   }

   @Override
   public void varDumpImpl(Env env,
                           WriteStream out,
                           int depth,
                           IdentityHashMap<Value, String> valueSet)
      throws IOException {
      int length = length();

      if (length < 0) {
         length = 0;
      }

      out.print("string(");
      out.print(length);
      out.print(") \"");
      out.print(toString());
      out.print("\"");
   }

   //
   // Java generator code
   //

   /**
    * Prints the value.
    *
    * @param env
    */
   @Override
   public void print(Env env) {
      try {
         env.getOut().print(toString());
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      }
   }

   /**
    * Prints the value.
    *
    * @param env
    */
   @Override
   public void print(Env env, WriteStream out) {
      try {
         out.print(toString());
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      }
   }

   static {
      CHAR_STRINGS = new StringValue[Character.MAX_VALUE];
      for (char i = 0; i < CHAR_STRINGS.length; ++i) {
         CHAR_STRINGS[i] = new StringValue(i);
      }
   }

   class StringValueInputStream extends java.io.InputStream {

      private final int _length;
      private int _index;

      StringValueInputStream() {
         _length = length();
      }

      /**
       * Reads the next byte.
       */
      @Override
      public int read() {
         if (_index < _length) {
            return charAt(_index++);
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

         byte[] s = substring(_index, _index + sublen).toString().getBytes();
         _index += sublen;

         for (int i = 0; i < sublen; i++) {
            buffer[offset + i] = s[i];
         }

         return sublen;
      }
   }

   static class SimpleStringValueReader extends Reader {

      StringValue _str;
      int _index;
      int _length;

      SimpleStringValueReader(StringValue s) {
         _str = s;
         _length = s.length();
      }

      @Override
      public int read() {
         if (_index >= _length) {
            return -1;
         } else {
            return _str.charAt(_index++);
         }
      }

      @Override
      public int read(char[] buf, int off, int len) {
         if (_index >= _length) {
            return -1;
         }

         int i = 0;
         len = Math.min(_length - _index, len);

         for (; i < len; i++) {
            buf[off + i] = _str.charAt(i + _index++);
         }

         return i;
      }

      @Override
      public void close()
         throws IOException {
      }
   }

   class StringValueOutputStream extends OutputStream {

      /**
       * Writes the next byte.
       */
      @Override
      public void write(int ch) {
         append(ch);
      }

      /**
       * Reads into a buffer.
       */
      @Override
      public void write(byte[] buffer, int offset, int length) {
         append(new String(buffer), offset, length);
      }
   }
}
