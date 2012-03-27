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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;

import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.page.BiancaPage;
import com.clevercloud.vfs.Path;
import com.clevercloud.vfs.StdoutStream;
import com.clevercloud.vfs.StringPath;
import com.clevercloud.vfs.WriteStream;

public class Bianca
        extends BiancaContext {

   private static final Logger log = Logger.getLogger(Bianca.class.getName());
   private String _fileName;
   private String[] _args;

   public Bianca() {
      super();

      init();
   }

   //
   // command-line main
   //
   public static void main(String[] args)
           throws IOException {
      Bianca bianca = new Bianca();

      if (!bianca.parseArgs(args)) {
         printUsage();
         return;
      }

      bianca.init();

      if (bianca.getFileName() != null) {
         bianca.execute();
      } else {
         throw new RuntimeException("input file not specified");
      }
   }

   public static void printUsage() {
      System.out.println("usage: com.clevercloud.bianca.Bianca [flags] <file> [php-args]");
      System.out.println(" -f            : Explicitly set the script filename.");
      System.out.println(" -d name=value : Sets a php ini value.");
   }

   /**
    * Returns the SAPI (Server API) name.
    */
   @Override
   public String getSapiName() {
      return "cli";
   }

   public String getFileName() {
      return _fileName;
   }

   public void setFileName(String name) {
      _fileName = name;
   }

   protected boolean parseArgs(String[] args) {
      ArrayList<String> phpArgList = new ArrayList<String>();

      int i = 0;
      for (; i < args.length; i++) {
         if ("-d".equals(args[i])) {
            int eqIndex = args[i + 1].indexOf('=');

            String name = "";
            String value = "";

            if (eqIndex >= 0) {
               name = args[i + 1].substring(0, eqIndex);
               value = args[i + 1].substring(eqIndex + 1);
            } else {
               name = args[i + 1];
            }

            i++;
            setIni(name, value);
         } else if ("-f".equals(args[i])) {
            _fileName = args[++i];
         } else if ("-q".equals(args[i])) {
            // quiet
         } else if ("-n".equals(args[i])) {
            // no php-pip
         } else if ("--".equals(args[i])) {
            break;
         } else if ("-h".equals(args[i])) {
            return false;
         } else if (args[i].startsWith("-")) {
            System.out.println("unknown option: " + args[i]);
            return false;
         } else {
            phpArgList.add(args[i]);
         }
      }

      for (; i < args.length; i++) {
         phpArgList.add(args[i]);
      }

      _args = phpArgList.toArray(new String[phpArgList.size()]);

      if (_fileName == null && _args.length > 0) {
         _fileName = _args[0];
      }

      return true;
   }

   public void execute()
           throws IOException {
      Path path = getPwd().lookup(_fileName);

      execute(path);
   }

   public void execute(String code)
           throws IOException {
      Path path = new StringPath(code);

      execute(path);
   }

   public void execute(Path path)
           throws IOException {
      BiancaPage page = parse(path);

      WriteStream os = new WriteStream(StdoutStream.create());

      os.setNewlineString("\n");
      os.setEncoding("utf-8");

      Env env = createEnv(page, os, null, null);
      env.start();

      if (_args.length > 0) {
         env.setArgs(_args);
      }

      try {
         env.execute();
      } catch (BiancaDieException e) {
         log.log(Level.FINER, e.toString(), e);
      } catch (BiancaExitException e) {
         log.log(Level.FINER, e.toString(), e);
      } catch (BiancaErrorException e) {
         log.log(Level.FINER, e.toString(), e);
      } finally {
         env.close();

         os.flush();
      }
   }
}
