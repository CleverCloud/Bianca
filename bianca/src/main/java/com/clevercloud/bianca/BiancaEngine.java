/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 * Copyright (c) 2011-2012 Clever Cloud SAS -- all rights reserved
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
 * @author Nam Nguyen
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.clevercloud.bianca;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.NullValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.page.BiancaPage;
import com.clevercloud.bianca.page.InterpretedPage;
import com.clevercloud.bianca.parser.BiancaParser;
import com.clevercloud.bianca.program.BiancaProgram;
import com.clevercloud.vfs.*;

import java.io.IOException;
import java.io.OutputStream;

public class BiancaEngine {

   private BiancaContext _bianca;
   private OutputStream _out;

   public BiancaEngine() {
      _bianca = new BiancaContext();
   }

   /**
    * Returns the Bianca object.
    */
   public BiancaContext getBianca() {
      return _bianca;
   }

   /**
    * Sets a php-ini value.
    */
   public void setIni(String name, String value) {
      _bianca.setIni(name, value);
   }

   /**
    * Sets the output stream.
    */
   public void setOutputStream(OutputStream out) {
      _out = out;
   }

   /**
    * Executes the script
    */
   public Value executeFile(String filename)
      throws IOException {
      Path path = _bianca.getPwd().lookup(filename);

      return execute(path);
   }

   /**
    * Executes the script.
    */
   public Value execute(String script)
      throws IOException {
      return execute(new StringPath(script));
   }

   /**
    * Executes the script.
    */
   public Value execute(Path path)
      throws IOException {
      ReadStream reader = path.openRead();

      BiancaProgram program = BiancaParser.parse(_bianca, null, reader);

      OutputStream os = _out;
      WriteStream out;

      if (os != null) {
         OutputStreamStream s = new OutputStreamStream(os);
         WriteStream ws = new WriteStream(s);

         ws.setNewlineString("\n");

         try {
            ws.setEncoding("utf-8");
         } catch (Exception e) {
         }

         out = ws;
      } else {
         out = new WriteStream(StdoutStream.create());
      }

      BiancaPage page = new InterpretedPage(program);

      Env env = new Env(_bianca, page, out, null, null);

      Value value = NullValue.NULL;

      try {
         value = program.execute(env);
      } catch (BiancaExitException e) {
      }

      out.flushBuffer();
      out.free();

      if (os != null) {
         os.flush();
      }

      return value;
   }

   class OutputStreamStream extends StreamImpl {

      OutputStream _out;

      OutputStreamStream(OutputStream out) {
         _out = out;
      }

      /**
       * Returns true if this is a writable stream.
       */
      @Override
      public boolean canWrite() {
         return true;
      }

      /**
       * Writes a buffer to the underlying stream.
       *
       * @param buffer the byte array to write.
       * @param offset the offset into the byte array.
       * @param length the number of bytes to write.
       * @param isEnd  true when the write is flushing a close.
       */
      @Override
      public void write(byte[] buffer, int offset, int length, boolean isEnd)
         throws IOException {
         _out.write(buffer, offset, length);
      }
   }
}
