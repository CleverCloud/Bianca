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

import com.caucho.vfs.*;
import com.caucho.quercus.QuercusModuleException;
import com.caucho.quercus.QuercusRuntimeException;

import java.io.*;
import java.util.IdentityHashMap;
import java.util.zip.CRC32;

/**
 * Represents a PHP String
 */
public class StringBuilderValue
        extends StringValue {

   private static final StringBuilderValue[] CHAR_STRINGS;
   private StringBuilder _buffer;
   private int _hashCode;

   public StringBuilderValue() {
      _buffer = new StringBuilder();
   }

   public StringBuilderValue(String buffer, int offset, int length) {
      _buffer = new StringBuilder(buffer.substring(Math.min(offset, buffer.length()), Math.min(offset + length, buffer.length())));
   }

   /**
    * Creates a new StringBuilderValue with the buffer without copying.
    */
   public StringBuilderValue(String buffer, int length) {
      this(buffer, 0, length);
   }

   public StringBuilderValue(char ch) {
      _buffer = new StringBuilder().append(ch);
   }

   public StringBuilderValue(byte ch) {
      _buffer = new StringBuilder().append(ch);
   }

   public StringBuilderValue(String s) {
      _buffer = new StringBuilder(s);
   }

   public StringBuilderValue(String s, Value v1) {
      _buffer = new StringBuilder(s);
      v1.appendTo(this);
   }

   public StringBuilderValue(Value v1) {
      if (v1 instanceof StringBuilderValue) {
         _buffer = new StringBuilder((StringBuilderValue) v1);
      } else {
         _buffer = new StringBuilder();
         v1.appendTo(this);
      }
   }

   public StringBuilderValue(StringBuilderValue v) {
      _buffer = new StringBuilder(v._buffer);
   }

   public StringBuilderValue(Value v1, Value v2) {
      _buffer = new StringBuilder();

      v1.appendTo(this);
      v2.appendTo(this);
   }

   public StringBuilderValue(Value v1, Value v2, Value v3) {
      _buffer = new StringBuilder();

      v1.appendTo(this);
      v2.appendTo(this);
      v3.appendTo(this);
   }

   public StringBuilderValue(TempBuffer head) {
      this();

      // php/0c4l
      append(head);
   }

   /**
    * Creates the string.
    */
   public static StringValue create(byte value) {
      if (value < CHAR_STRINGS.length)
         return CHAR_STRINGS[value];
      return new StringBuilderValue();
   }

   /**
    * Creates the string.
    */
   public static StringValue create(char value) {
      if (value < CHAR_STRINGS.length)
         return CHAR_STRINGS[value];
      return new StringBuilderValue();
   }

   /**
    * Creates a PHP string from a Java String.
    * If the value is null then NullValue is returned.
    */
   public static Value create(String value) {
      if (value == null) {
         return NullValue.NULL;
      } else if (value.length() == 0) {
         return StringBuilderValue.EMPTY;
      } else {
         return new StringBuilderValue(value);
      }
   }

   public static StringValue create(int value) {
      if (value < CHAR_STRINGS.length) {
         return CHAR_STRINGS[value];
      } else {
         return new StringBuilderValue();
      }
   }

   /**
    * Returns the value.
    */
   public final String getValue() {
      return toString();
   }

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
    * Returns true for a scalar
    */
   @Override
   public final boolean isScalar() {
      return true;
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
    * Converts to a long.
    */
   @Override
   public long toLong() {
      return parseLong(_buffer.toString(), 0, _buffer.length());
   }

   /**
    * Converts to a double.
    */
   @Override
   public double toDouble() {
      return toDouble(_buffer.toString(), 0, _buffer.length());
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

      try {
         return Double.parseDouble(buffer.substring(Math.min(start, buffer.length()), Math.min(i - start, buffer.length())));
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   /**
    * Convert to an input stream.
    */
   @Override
   public final InputStream toInputStream() {
      return new BuilderInputStream();
   }

   /**
    * Converts to a string.
    */
   @Override
   public String toString() {
      return _buffer.toString();
   }

   /**
    * Converts to an object.
    */
   @Override
   public final Object toJavaObject() {
      return toString();
   }

   /**
    * Returns true if the value is empty.
    */
   @Override
   public final boolean isEmpty() {
      return _buffer.length() == 0 || _buffer.length() == 1 && _buffer.charAt(0) == '0';
   }

   /**
    * Writes to a stream
    */
   @Override
   public final void writeTo(OutputStream os) {
      try {
         os.write(_buffer.toString().getBytes(), 0, _buffer.length());
      } catch (IOException e) {
         throw new QuercusModuleException(e);
      }
   }

   /**
    * Append to a string builder.
    */
   @Override
   public StringValue appendTo(StringBuilderValue sb) {
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
    * Converts to a key.
    */
   @Override
   public Value toKey() {
      String buffer = _buffer.toString();
      int len = buffer.length();

      if (len == 0) {
         return this;
      }

      int sign = 1;
      long value = 0;

      int i = 0;
      int ch = buffer.charAt(i);
      if (ch == '-') {
         sign = -1;
         i++;
      }

      for (; i < len; i++) {
         ch = buffer.charAt(i);

         if ('0' <= ch && ch <= '9') {
            value = 10 * value + ch - '0';
         } else {
            return this;
         }
      }

      return LongValue.create(sign * value);
   }

   //
   // Operations
   //
   /**
    * Returns the character at an index
    */
   @Override
   public final Value get(Value key) {
      return charValueAt(key.toLong());
   }

   /**
    * Sets the array ref.
    */
   @Override
   public Value put(Value index, Value value) {
      setCharValueAt(index.toLong(), value);

      return value;
   }

   /**
    * Sets the array ref.
    */
   @Override
   public Value append(Value index, Value value) {
      if (_buffer.length() > 0) {
         return setCharValueAt(index.toLong(), value);
      } else {
         return new ArrayValueImpl().append(index, value);
      }
   }

   /**
    * sets the character at an index
    */
   /*
   public Value setCharAt(long index, String value)
   {
   int len = _length;

   if (index < 0 || len <= index)
   return this;
   else {
   StringBuilderValue sb = new StringBuilderValue(_buffer, 0, (int) index);
   sb.append(value);
   sb.append(_buffer, (int) (index + 1), (int) (len - index - 1));

   return sb;
   }
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
   public final char charAt(int index) {
      if (index < 0 || _buffer.length() <= index) {
         return 0;
      } else {
         return _buffer.charAt(index);
      }
   }

   /**
    * Returns the character at an index
    */
   @Override
   public Value charValueAt(long index) {
      if (index < 0 || length() <= index) {
         return UnsetStringValue.UNSET;
      } else {
         return StringBuilderValue.create(toString().charAt((int) index));
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
         StringBuilderValue sb = new StringBuilderValue(_buffer.toString());

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

         StringBuilderValue sb = (StringBuilderValue) copyStringBuilder();

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
    * Returns a subsequence
    */
   @Override
   public CharSequence subSequence(int start, int end) {
      if (end <= start) {
         return StringBuilderValue.EMPTY;
      } else if (end - start == 1) {
         return CHAR_STRINGS[_buffer.charAt(start)];
      }

      return substring(start, end);
   }

   /**
    * Returns a subsequence
    */
   @Override
   public String stringSubstring(int start, int end) {
      return substring(start, end).toString();
   }

   /**
    * Convert to lower case.
    */
   @Override
   public StringValue toLowerCase() {
      return new StringBuilderValue(_buffer.toString().toLowerCase());
   }

   /**
    * Convert to lower case.
    */
   @Override
   public StringValue toUpperCase() {
      return new StringBuilderValue(_buffer.toString().toUpperCase());
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue copyStringBuilder() {
      return new StringBuilderValue(this);
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder() {
      return new StringBuilderValue(this);
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env) {
      return new StringBuilderValue(this);
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env, Value value) {
      StringBuilderValue v = new StringBuilderValue(this);

      value.appendTo(v);

      return v;
   }

   /**
    * Converts to a string builder
    */
   @Override
   public StringValue toStringBuilder(Env env, StringValue value) {
      StringBuilderValue v = new StringBuilderValue(this);

      value.appendTo(v);

      return v;
   }

   //
   // append code
   //
   /**
    * Append a Java string to the value.
    */
   @Override
   public final StringValue append(String s) {
      _buffer.append(s);
      return this;
   }

   /**
    * Append a Java string to the value.
    */
   @Override
   public final StringValue append(String s, int start, int end) {
      _buffer.append(s.substring(Math.min(start, s.length()), Math.min(end, s.length())));
      return this;
   }

   /**
    * Append a Java char to the value.
    */
   @Override
   public final StringValue append(char ch) {
      _buffer.append(ch);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   @Override
   public final StringValue append(char[] buf, int offset, int length) {
      _buffer.append(buf, offset, length);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   @Override
   public final StringValue append(char[] buf) {
      _buffer.append(buf);
      return this;
   }

   /**
    * Append a value to the value.
    */
   @Override
   public final StringValue append(Value value) {
      append(value.toString());
      return this;
   }

   /**
    * Append a Java long to the value.
    */
   @Override
   public StringValue append(long v) {
      return append(String.valueOf(v));
   }

   /**
    * Append a Java double to the value.
    */
   @Override
   public StringValue append(double v) {
      return append(String.valueOf(v));
   }

   /**
    * Append a Java object to the value.
    */
   @Override
   public StringValue append(Object v) {
      return append(v.toString());
   }

   /**
    * Append a Java buffer to the value.
    */
   @Override
   public final StringValue append(CharSequence buf, int head, int tail) {
      _buffer.append(buf, head, tail);
      return this;
   }

   /**
    * Append a Java buffer to the value.
    */
   @Override
   public StringValue append(StringBuilderValue sb, int head, int tail) {
      _buffer.append(sb, head, tail);
      return this;
   }

   /**
    * Returns the first index of the match string, starting from the head.
    */
   @Override
   public final int indexOf(CharSequence match, int head) {
      return _buffer.toString().substring(Math.min(head, _buffer.length())).indexOf(match.toString());
   }

   /**
    * Append a Java value to the value.
    */
   @Override
   public StringValue appendUnicode(Value v1, Value v2) {
      v1.appendTo(this);
      v2.appendTo(this);

      return this;
   }

   @Override
   public StringValue append(Reader reader, long length)
           throws IOException {
      // php/4407 - oracle clob callback passes very long length

      TempCharBuffer tempBuf = TempCharBuffer.allocate();

      char[] buffer = tempBuf.getBuffer();

      int sublen = (int) Math.min(buffer.length, length);

      try {
         while (length > 0) {
            int count = reader.read(buffer, 0, sublen);

            if (count <= 0) {
               break;
            }

            append(buffer, 0, count);

            length -= count;
         }

      } catch (IOException e) {
         throw new QuercusModuleException(e);
      } finally {
         TempCharBuffer.free(tempBuf);
      }

      return this;
   }

   /**
    * Return true if the array value is set
    */
   @Override
   public boolean isset(Value indexV) {
      int index = indexV.toInt();

      return 0 <= index && index < length();
   }

   //
   // Java generator code
   //
   /**
    * Prints the value.
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

   /**
    * Serializes the value.
    */
   @Override
   public void serialize(Env env, StringBuilder sb) {
      sb.append("s:");
      sb.append(length());
      sb.append(":\"");

      for (int i = 0; i < length(); i++) {
         sb.append(_buffer.charAt(i));
      }

      sb.append("\";");
   }

   @Override
   public String toDebugString() {
      StringBuilder sb = new StringBuilder();

      int length = length();

      sb.append("binary(");
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

      /*for (int i = 0; i < length; i++) {
         char ch = charAt(i);

         if (0x20 <= ch && ch <= 0x7f || ch == '\t' || ch == '\r' || ch == '\n') {
            out.print(ch);
         } else if (ch <= 0xff) {
            out.print("\\x"
                    + Integer.toHexString(ch / 16)
                    + Integer.toHexString(ch % 16));
         } else {
            out.print("\\u"
                    + Integer.toHexString((ch >> 12) & 0xf)
                    + Integer.toHexString((ch >> 8) & 0xf)
                    + Integer.toHexString((ch >> 4) & 0xf)
                    + Integer.toHexString((ch) & 0xf));
         }
      }*/
      out.print(toString());

      out.print("\"");
   }

   /**
    * Returns an OutputStream.
    */
   public OutputStream getOutputStream() {
      return new BuilderOutputStream();
   }

   /**
    * Calculates CRC32 value.
    */
   @Override
   public long getCrc32Value() {
      CRC32 crc = new CRC32();

      crc.update(_buffer.toString().getBytes(), 0, length());

      return crc.getValue() & 0xffffffff;
   }

   /**
    * Returns the hash code.
    */
   @Override
   public int hashCode() {
      int hash = _hashCode;

      if (hash != 0) {
         return hash;
      }

      hash = 37;

      int length = length();
      String buffer = _buffer.toString();

      if (length > 256) {
         for (int i = 127; i >= 0; i--) {
            hash = 65521 * hash + buffer.charAt(i);
         }

         for (int i = length - 128; i < length; i++) {
            hash = 65521 * hash + buffer.charAt(i);
         }

         _hashCode = hash;

         return hash;
      }

      for (int i = length - 1; i >= 0; --i) {
         hash = 65521 * hash + buffer.charAt(i);
      }

      _hashCode = hash;

      return hash;
   }

   /**
    * Returns the hash code.
    */
   @Override
   public int hashCodeCaseInsensitive() {
      int hash = 0;

      if (hash != 0) {
         return hash;
      }

      hash = 37;

      int length = length();
      String buffer = _buffer.toString();

      if (length > 256) {
         for (int i = 127; i >= 0; i--) {
            hash = 65521 * hash + toLower(buffer.charAt(i));
         }

         for (int i = length - 128; i < length; i++) {
            hash = 65521 * hash + toLower(buffer.charAt(i));
         }

         _hashCode = hash;

         return hash;
      }

      for (int i = length - 1; i >= 0; i--) {
         int ch = toLower(buffer.charAt(i));

         hash = 65521 * hash + ch;
      }

      return hash;
   }

   private int toLower(int ch) {
      if ('A' <= ch && ch <= 'Z') {
         return ch + 'a' - 'A';
      } else {
         return ch;
      }
   }

   /**
    * Returns true for equality
    */
   @Override
   public boolean eq(Value rValue) {
      rValue = rValue.toValue();

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
      }

      return toString().equals(rValue.toString());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (o instanceof StringBuilderValue) {
         return toString().equals(o.toString());
      } else {
         return false;
      }
   }

   @Override
   public boolean eql(Value o) {
      o = o.toValue();

      if (o == this) {
         return true;
      }

      if (o instanceof StringBuilderValue) {
         return toString().equals(o.toString());
      } else {
         return false;
      }
   }

   class BuilderInputStream extends InputStream {

      private int _index;

      /**
       * Reads the next byte.
       */
      @Override
      public int read() {
         if (_index < _buffer.length()) {
            return _buffer.charAt(_index++);
         } else {
            return -1;
         }
      }

      /**
       * Reads into a buffer.
       */
      @Override
      public int read(byte[] buffer, int offset, int length) {
         int sublen = Math.min(_buffer.length() - _index, length);

         if (sublen <= 0) {
            return -1;
         }

         _buffer = new StringBuilder(_buffer.substring(0, _index)).append(new String(buffer).substring(_index, _index + sublen));

         _index += sublen;

         return sublen;
      }
   }

   class BuilderOutputStream extends OutputStream {

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

   static {
      CHAR_STRINGS = new StringBuilderValue[256];
      for (int i = 0; i < CHAR_STRINGS.length; ++i) {
         CHAR_STRINGS[i] = new StringBuilderValue((byte) i);
      }
   }
}
