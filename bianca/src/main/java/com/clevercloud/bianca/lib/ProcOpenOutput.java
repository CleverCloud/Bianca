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
 * @author Emil Ong
 */
package com.clevercloud.bianca.lib;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.EnvCleanup;
import com.clevercloud.bianca.lib.file.AbstractBinaryOutput;
import com.clevercloud.bianca.lib.file.PopenOutput;
import com.clevercloud.vfs.VfsStream;
import com.clevercloud.vfs.WriteStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an output stream for a proc_open process.
 */
public class ProcOpenOutput extends AbstractBinaryOutput
   implements EnvCleanup {

   private static final Logger log = Logger.getLogger(PopenOutput.class.getName());
   private Env _env;
   private WriteStream _os;

   public ProcOpenOutput(Env env, OutputStream out)
      throws IOException {
      _env = env;

      _env.addCleanup(this);

      _os = new WriteStream(new VfsStream(null, out));
   }

   /**
    * Returns the write stream.
    */
   @Override
   public OutputStream getOutputStream() {
      return _os;
   }

   /**
    * Prints a string to a file.
    */
   @Override
   public void print(char v)
      throws IOException {
      if (_os != null) {
         _os.print(v);
      }
   }

   /**
    * Prints a string to a file.
    */
   @Override
   public void print(String v)
      throws IOException {
      if (_os != null) {
         _os.print(v);
      }
   }

   /**
    * Writes a character
    */
   @Override
   public void write(int ch)
      throws IOException {
      if (_os != null) {
         _os.write(ch);
      }
   }

   /**
    * Writes a buffer to a file.
    */
   @Override
   public void write(byte[] buffer, int offset, int length)
      throws IOException {
      if (_os != null) {
         _os.write(buffer, offset, length);
      }
   }

   /**
    * Flushes the output.
    */
   @Override
   public void flush() {
      try {
         if (_os != null) {
            _os.flush();
         }
      } catch (IOException e) {
         log.log(Level.FINE, e.toString(), e);
      }
   }

   /**
    * Closes the file.
    */
   @Override
   public void closeWrite() {
      close();
   }

   /**
    * Closes the file.
    */
   @Override
   public void close() {
      _env.removeCleanup(this);

      cleanup();
   }

   /**
    * Implements the EnvCleanup interface.
    */
   @Override
   public void cleanup() {
      try {
         WriteStream os = _os;
         _os = null;

         if (os != null) {
            os.close();
         }
      } catch (IOException e) {
         log.log(Level.FINE, e.toString(), e);
      }
   }

   /**
    * Converts to a string.
    *
    * @param env
    */
   @Override
   public String toString() {
      return "ProcOpenOutput[pipe]";
   }
}