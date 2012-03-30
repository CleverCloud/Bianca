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
package com.clevercloud.bianca;

import com.clevercloud.bianca.env.CgiEnv;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.page.BiancaPage;
import com.clevercloud.vfs.Path;
import com.clevercloud.vfs.StdoutStream;
import com.clevercloud.vfs.WriteStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CgiBianca
   extends CliBianca {

   @Override
   public Env createEnv(BiancaPage page,
                        WriteStream out,
                        HttpServletRequest request,
                        HttpServletResponse response) {
      return new CgiEnv(this, page, out, request, response);
   }

   public static void main(String[] args)
      throws IOException {
      CgiBianca bianca = new CgiBianca();

      bianca.parseArgs(args);

      bianca.init();

      if (bianca.getFileName() != null) {
         bianca.execute();
      } else {
         throw new RuntimeException("input file not specified");
      }
   }

   /**
    * Returns the SAPI (Server API) name.
    */
   @Override
   public String getSapiName() {
      return "cgi";
   }

   @Override
   public void execute()
      throws IOException {
      Path path = getPwd().lookup(getFileName());

      BiancaPage page = parse(path);

      WriteStream os = new WriteStream(StdoutStream.create());

      os.setNewlineString("\n");
      os.setEncoding("utf-8");

      Env env = createEnv(page, os, null, null);
      env.start();

      try {
         env.execute();
      } catch (BiancaDieException e) {
      } catch (BiancaExitException e) {
      }

      env.close();

      os.flush();
   }
}
