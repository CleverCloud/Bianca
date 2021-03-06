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
package com.clevercloud.bianca.lib.zlib;

import com.clevercloud.bianca.BiancaModuleException;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.lib.file.BinaryInput;
import com.clevercloud.bianca.lib.file.ReadStreamInput;
import com.clevercloud.vfs.ReadStream;
import com.clevercloud.vfs.VfsStream;

import java.io.IOException;

/**
 * Input from a compressed stream.
 */
public class ZlibInputStream extends ReadStreamInput {

   private Env _env;
   private BinaryInput _in;
   private GZInputStream _gzIn;

   public ZlibInputStream(Env env, BinaryInput in) throws IOException {
      super(env);

      _env = env;

      init(in);
   }

   protected final void init(BinaryInput in)
      throws IOException {
      _in = in;

      _gzIn = new GZInputStream(in.getInputStream());
      ReadStream rs = new ReadStream(new VfsStream(_gzIn, null));

      init(rs);
   }

   /**
    * Opens a new copy.
    */
   @Override
   public BinaryInput openCopy()
      throws IOException {
      return new ZlibInputStream(_env, _in.openCopy());
   }

   /**
    * Sets the position.
    */
   @Override
   public boolean setPosition(long offset) {
      try {
         BinaryInput newIn = _in.openCopy();

         /*
         _gzIn.close();
         getInputStream().close();

         _in.close();
         _in = null;
         _gzIn = null;
          */

         close();
         _in.close();

         init(newIn);

         return skip(offset) == offset;

      } catch (IOException e) {
         throw new BiancaModuleException(e);
      }
   }

   @Override
   public String toString() {
      return "ZlibInputStream[]";
   }
}
