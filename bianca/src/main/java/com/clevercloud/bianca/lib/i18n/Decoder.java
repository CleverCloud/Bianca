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
 * @author Nam Nguyen
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.i18n;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;

abstract public class Decoder {

   protected CharSequence _replacement;
   protected boolean _isIgnoreErrors = false;
   protected boolean _isReplaceUnicode = false;
   protected boolean _isAllowMalformedOut = false;
   protected boolean _hasError;

   public static Decoder create(String charset) {
      if (charset.equalsIgnoreCase("utf-8")
         || charset.equalsIgnoreCase("utf-8")) {
         return new Utf8Decoder();
      } else if (charset.equalsIgnoreCase("big5")
         || charset.equalsIgnoreCase("big-5")) {
         return new Big5Decoder(charset);
      } else {
         return new GenericDecoder(charset);
      }
   }

   public boolean isUtf8() {
      return false;
   }

   public final boolean isIgnoreErrors() {
      return _isIgnoreErrors;
   }

   public final void setIgnoreErrors(boolean isIgnore) {
      _isIgnoreErrors = isIgnore;
   }

   public final boolean hasError() {
      return _hasError;
   }

   public final void setReplacement(CharSequence replacement) {
      _replacement = replacement;
   }

   public final void setReplaceUnicode(boolean isReplaceUnicode) {
      _isReplaceUnicode = isReplaceUnicode;
   }

   public final void setAllowMalformedOut(boolean isAllowMalformedOut) {
      _isAllowMalformedOut = isAllowMalformedOut;
   }

   public void reset() {
      _hasError = false;
   }

   public final CharSequence decode(Env env, StringValue str) {
      return str;
   }

   public StringBuilder decodeStringBuilder(Env env, StringValue str) {
      return decodeImpl(env, str);
   }

   public StringValue decodeUnicode(Env env, StringValue str) {
      StringValue sb = new StringValue();

      StringBuilder unicodeStr = decodeImpl(env, str);

      return sb.append(unicodeStr);
   }

   abstract protected StringBuilder decodeImpl(Env env, StringValue str);
}
