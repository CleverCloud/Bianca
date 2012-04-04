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
 * @author Kevin Decherf <kdecherf@gmail.com>
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.zip;

import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.env.*;
import com.clevercloud.util.L10N;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipArchive implements EnvCleanup {

   private static final Logger log = Logger.getLogger(ZipArchive.class.getName());
   private static final L10N L = new L10N(ZipArchive.class);
   public static final int CREATE = 1;
   public static final int EXCL = 2;
   public static final int CHECKCONS = 4;
   public static final int OVERWRITE = 8;
   public static final int FL_NOCASE = 1;
   public static final int FL_NODIR = 2;
   public static final int FL_COMPRESSED = 4;
   public static final int FL_UNCHANGED = 8;
   public static final int FL_RECOMPRESS = 16;
   public static final int CM_DEFAULT = -1;
   public static final int CM_STORE = 0;
   public static final int CM_SHRINK = 1;
   public static final int CM_REDUCE_1 = 2;
   public static final int CM_REDUCE_2 = 3;
   public static final int CM_REDUCE_3 = 4;
   public static final int CM_REDUCE_4 = 5;
   public static final int CM_IMPLODE = 6;
   public static final int CM_DEFLATE = 8;
   public static final int CM_DEFLATE64 = 9;
   public static final int CM_PKWARE_IMPLODE = 10;
   public static final int CM_BZIP2 = 12;
   public static final int ER_OK = 0;
   public static final int ER_MULTIDISK = 1;
   public static final int ER_RENAME = 2;
   public static final int ER_CLOSE = 3;
   public static final int ER_SEEK = 4;
   public static final int ER_READ = 5;
   public static final int ER_WRITE = 6;
   public static final int ER_CRC = 7;
   public static final int ER_ZIPCLOSED = 8;
   public static final int ER_NOENT = 9;
   public static final int ER_EXISTS = 10;
   public static final int ER_OPEN = 11;
   public static final int ER_TMPOPEN = 12;
   public static final int ER_ZLIB = 13;
   public static final int ER_MEMORY = 14;
   public static final int ER_CHANGED = 15;
   public static final int ER_COMPNOTSUPP = 16;
   public static final int ER_EOF = 17;
   public static final int ER_INVAL = 18;
   public static final int ER_NOZIP = 19;
   public static final int ER_INTERNAL = 20;
   public static final int ER_INCONS = 21;
   public static final int ER_REMOVE = 22;
   public static final int ER_DELETED = 23;

   public LongValue numFiles = new LongValue(0);

   private ZipFile _zip = null;

   public BooleanValue open(StringValue filename, @Optional int flags) {
      /*TODO: handle flags*/
      if (filename == null || filename.length() == 0) {
         return BooleanValue.FALSE;
      }

      try {
         _zip = new ZipFile(filename.toString());
      } catch (IOException e) {
         Logger.getLogger(ZipArchive.class.getName()).severe(e.getMessage());
         return BooleanValue.FALSE;
      }

      Enumeration<? extends ZipEntry> entries = _zip.entries();
      long num = 0;
      while (entries.hasMoreElements()) {
         if (!entries.nextElement().isDirectory())
            ++num;
      }
      numFiles = new LongValue(num);

      return BooleanValue.TRUE;
   }

   public ZipEntry getFromIndex(LongValue index, @Optional LongValue length, @Optional int flags) {
      if (_zip == null)
         return null;
      Enumeration<? extends ZipEntry> entries = _zip.entries();
      ZipEntry ret = null;
      for (long i = 0; i <= index.toLong(); ++i) {
         if (entries.hasMoreElements()) {
            ret = entries.nextElement();
            if (ret.isDirectory())
               --i;
         } else
            return null;
      }
      return ret;
   }

   public Value statIndex(LongValue index, @Optional int flags) {
      ZipEntry entry = getFromIndex(index, new LongValue(-1), flags);
      if (entry == null || entry.isDirectory())
         return BooleanValue.FALSE;

      ArrayValue ret = new ArrayValueImpl();
      ret.append(new StringValue("name"), new StringValue(entry.getName()));
      ret.append(new StringValue("index"), index);
      ret.append(new StringValue("crc"), new LongValue(entry.getCrc()));
      ret.append(new StringValue("size"), new LongValue(entry.getSize()));
      ret.append(new StringValue("mtime"), new LongValue(entry.getTime()));
      ret.append(new StringValue("comp_size"), new LongValue(entry.getCompressedSize()));
      ret.append(new StringValue("comp_method"), new LongValue(entry.getMethod()));

      return ret;
   }

   public BooleanValue close() {
      if (_zip == null)
         return BooleanValue.FALSE;
      numFiles = new LongValue(0);
      try {
         _zip.close();
      } catch (IOException e) {
         Logger.getLogger(ZipArchive.class.getName()).severe(e.getMessage());
         return BooleanValue.FALSE;
      }
      _zip = null;
      return BooleanValue.TRUE;
   }

   @Override
   public void cleanup() throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
