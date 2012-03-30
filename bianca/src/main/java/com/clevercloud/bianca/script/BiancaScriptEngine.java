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
package com.clevercloud.bianca.script;

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.BiancaExitException;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.bianca.page.BiancaPage;
import com.clevercloud.bianca.page.InterpretedPage;
import com.clevercloud.bianca.parser.BiancaParser;
import com.clevercloud.bianca.program.BiancaProgram;
import com.clevercloud.vfs.*;

import javax.script.*;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Script engine
 */
public class BiancaScriptEngine
   extends AbstractScriptEngine
   implements Compilable {

   private BiancaScriptEngineFactory _factory;
   private final BiancaContext _bianca;

   BiancaScriptEngine(BiancaScriptEngineFactory factory) {
      this(factory, createBianca());
   }

   public BiancaScriptEngine(BiancaScriptEngineFactory factory,
                             BiancaContext bianca) {
      _factory = factory;
      _bianca = bianca;
   }

   private static BiancaContext createBianca() {
      BiancaContext bianca = new BiancaContext();

      bianca.init();

      return bianca;
   }

   /**
    * Returns the Bianca object.
    * php/214h
    */
   public BiancaContext getBianca() {
      return _bianca;
   }

   /**
    * evaluates based on a reader.
    */
   @Override
   public Object eval(Reader script, ScriptContext cxt)
      throws ScriptException {
      Env env = null;

      try {
         ReadStream reader = ReaderStream.open(script);

         BiancaProgram program = BiancaParser.parse(_bianca, null, reader);

         Writer writer = cxt.getWriter();

         WriteStream out;

         if (writer != null) {
            WriterStreamImpl s = new WriterStreamImpl();
            s.setWriter(writer);
            WriteStream os = new WriteStream(s);

            os.setNewlineString("\n");

            try {
               os.setEncoding("utf-8");
            } catch (Exception e) {
            }

            out = os;
         } else {
            out = new NullWriteStream();
         }

         BiancaPage page = new InterpretedPage(program);

         env = new Env(_bianca, page, out, null, null);

         env.setScriptContext(cxt);

         // php/214c
         env.start();

         Object result = null;

         try {
            Value value = program.execute(env);

            if (value != null) {
               result = value.toJavaObject();
            }
         } catch (BiancaExitException e) {
            //php/2148
         }

         out.flushBuffer();
         out.free();

         // flush buffer just in case
         //
         // jrunscript in interactive mode does not automatically flush its
         // buffers after every input, so output to stdout will not be seen
         // until the output buffer is full
         //
         // http://bugs.clevercloud.com/view.php?id=1914
         writer.flush();

         return result;

         /*
         } catch (ScriptException e) {
         throw e;
          */
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new ScriptException(e);
      } catch (Throwable e) {
         throw new RuntimeException(e);
      } finally {
         if (env != null) {
            env.close();
         }
      }
   }

   /**
    * evaluates based on a script.
    */
   @Override
   public Object eval(String script, ScriptContext cxt)
      throws ScriptException {
      return eval(new StringReader(script), cxt);
   }

   /**
    * compiles based on a reader.
    */
   @Override
   public CompiledScript compile(Reader script)
      throws ScriptException {
      try {
         ReadStream reader = ReaderStream.open(script);

         BiancaProgram program = BiancaParser.parse(_bianca, null, reader);

         return new BiancaCompiledScript(this, program);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new ScriptException(e);
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * evaluates based on a script.
    */
   @Override
   public CompiledScript compile(String script)
      throws ScriptException {
      return compile(new StringReader(script));
   }

   /**
    * Returns the engine's factory.
    */
   @Override
   public BiancaScriptEngineFactory getFactory() {
      return _factory;
   }

   /**
    * Creates a bindings.
    */
   @Override
   public Bindings createBindings() {
      return new SimpleBindings();
   }

   @Override
   public String toString() {
      return "BiancaScriptEngine[]";
   }
}
