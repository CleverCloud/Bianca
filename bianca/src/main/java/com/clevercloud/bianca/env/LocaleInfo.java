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

import java.util.Locale;

public final class LocaleInfo {

   private BiancaLocale _collate;
   private BiancaLocale _ctype;
   private BiancaLocale _monetary;
   private BiancaLocale _numeric;
   private BiancaLocale _time;
   private BiancaLocale _messages;

   LocaleInfo() {
      Locale locale = Locale.getDefault();

      setAll(new BiancaLocale(locale, null));
   }

   public void setAll(BiancaLocale locale) {
      setCollate(locale);
      setCtype(locale);
      setMonetary(locale);
      setNumeric(locale);
      setTime(locale);
      setMessages(locale);
   }

   public BiancaLocale getCollate() {
      return _collate;
   }

   public void setCollate(BiancaLocale locale) {
      _collate = locale;
   }

   public BiancaLocale getCtype() {
      return _ctype;
   }

   public void setCtype(BiancaLocale locale) {
      _ctype = locale;
   }

   public BiancaLocale getMonetary() {
      return _monetary;
   }

   public void setMonetary(BiancaLocale locale) {
      _monetary = locale;
   }

   public BiancaLocale getTime() {
      return _time;
   }

   public void setTime(BiancaLocale locale) {
      _time = locale;
   }

   public BiancaLocale getNumeric() {
      return _numeric;
   }

   public void setNumeric(BiancaLocale locale) {
      _numeric = locale;
   }

   public BiancaLocale getMessages() {
      return _messages;
   }

   public void setMessages(BiancaLocale locale) {
      _messages = locale;
   }
}
