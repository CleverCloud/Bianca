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
 * @author Charles Reich
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca.lib.zip;

import com.clevercloud.bianca.BiancaModuleException;
import com.clevercloud.bianca.annotation.NotNull;
import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.annotation.ReturnNullAsFalse;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.lib.file.BinaryInput;
import com.clevercloud.bianca.lib.file.BinaryStream;
import com.clevercloud.bianca.lib.file.FileModule;
import com.clevercloud.bianca.module.AbstractBiancaModule;
import com.clevercloud.util.L10N;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PHP Zip
 */
public class ZipModule extends AbstractBiancaModule {

   private static final Logger log = Logger.getLogger(ZipModule.class.getName());
   private static final L10N L = new L10N(ZipModule.class);

   @Override
   public String[] getLoadedExtensions() {
      return new String[]{"zip"};
   }

   /**
    * Opens stream to read zip entries.
    * Since we're only reading, fopen mode is always "rb".
    */
   @ReturnNullAsFalse
   public ZipDirectory zip_open(Env env,
                                @NotNull StringValue filename) {
      if (filename == null || filename.length() == 0) {
         return null;
      }

      BinaryStream s = FileModule.fopen(env, filename, "rb", false, null);

      if (s == null) {
         return null;
      }

      return new ZipDirectory((BinaryInput) s);
   }

   /**
    * Reads an entry's metadata from the zip stream.
    * It appears PHP's zip_read also does a zip_entry_open.
    */
   @ReturnNullAsFalse
   public BiancaZipEntry zip_read(Env env,
                                  @NotNull ZipDirectory directory) {
      if (directory == null) {
         return null;
      }

      try {
         BiancaZipEntry qze = directory.zip_read();
         zip_entry_open(env, directory, qze, "rb");

         return qze;

      } catch (IOException e) {
         throw new BiancaModuleException(e);
      }
   }

   /**
    * Returns the file name.
    *
    * @return false if zipEntry is null
    */
   public Value zip_entry_name(Env env,
                               @NotNull BiancaZipEntry entry) {
      if (entry == null) {
         return BooleanValue.FALSE;
      }

      return env.createString(entry.zip_entry_name());
   }

   /**
    * Returns the file's uncompressed size.
    *
    * @return false if zipEntry is null
    */
   public Value zip_entry_filesize(@NotNull BiancaZipEntry entry) {
      if (entry == null) {
         return BooleanValue.FALSE;
      }

      return LongValue.create(entry.zip_entry_filesize());
   }

   /**
    * Closes the file.
    */
   public boolean zip_close(@NotNull ZipDirectory directory) {
      if (directory == null) {
         return false;
      }

      return directory.zip_close();
   }

   /**
    * Opens entry for decompression.
    *
    * @return true on success or false on failure
    */
   public boolean zip_entry_open(Env env,
                                 @NotNull ZipDirectory directory,
                                 @NotNull BiancaZipEntry entry,
                                 @Optional String mode) {
      if ((directory == null) || (entry == null)) {
         return false;
      }

      return entry.zip_entry_open(env, directory);
   }

   /**
    * Closes this entry's stream.
    *
    * @return true if successful, else false;
    */
   public boolean zip_entry_close(Env env,
                                  @NotNull BiancaZipEntry entry) {
      try {
         if (entry == null) {
            return false;
         }

         return entry.zip_entry_close();

      } catch (IOException e) {
         env.warning(L.l(e.toString()));
         log.log(Level.FINE, e.toString(), e);
         return false;
      }
   }

   /**
    * Reads and decompresses entry's compressed data.
    *
    * @return false or decompressed StringValue
    */
   @ReturnNullAsFalse
   public StringValue zip_entry_read(Env env,
                                     @NotNull BiancaZipEntry entry,
                                     @Optional("1024") int length) {
      if (entry == null) {
         return null;
      }

      return entry.zip_entry_read(env, length);
   }

   /**
    * Returns the compression method used for this entry.
    * Only "deflate" and "store" are supported.
    *
    * @return empty string, stored or deflated
    */
   public String zip_entry_compressionmethod(@NotNull BiancaZipEntry entry) {
      if (entry == null) {
         return "";
      }

      return entry.zip_entry_compressionmethod();
   }

   /**
    * Returns the size of the compressed data.
    *
    * @return -1, or compressed size
    */
   public long zip_entry_compressedsize(@NotNull BiancaZipEntry entry) {
      if (entry == null) {
         return -1;
      }

      return entry.zip_entry_compressedsize();
   }
}
