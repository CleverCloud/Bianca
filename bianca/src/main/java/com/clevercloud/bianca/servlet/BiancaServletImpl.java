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
package com.clevercloud.bianca.servlet;

import com.clevercloud.bianca.*;
import com.clevercloud.bianca.env.BiancaValueException;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.page.BiancaPage;
import com.clevercloud.java.WorkDir;
import com.clevercloud.util.L10N;
import com.clevercloud.vfs.FilePath;
import com.clevercloud.vfs.Path;
import com.clevercloud.vfs.Vfs;
import com.clevercloud.vfs.WriteStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to call PHP through javax.script.
 */
public class BiancaServletImpl extends HttpServlet {

   private static final L10N L = new L10N(BiancaServletImpl.class);
   private static final Logger log = Logger.getLogger(BiancaServletImpl.class.getName());
   protected BiancaContext _bianca;
   protected ServletConfig _config;
   protected ServletContext _servletContext;

   /**
    * initialize the script manager.
    */
   @Override
   public final void init(ServletConfig config)
      throws ServletException {
      _config = config;
      _servletContext = config.getServletContext();

      checkServletAPIVersion();

      Path pwd = new FilePath(_servletContext.getRealPath("/"));

      getBianca().setPwd(pwd);

      // need to set these for non-Bianca containers
      if (!getBianca().isBianca()) {
         Vfs.setPwd(pwd);
         WorkDir.setLocalWorkDir(pwd.lookup("WEB-INF/work"));
      }

      getBianca().init();
   }

   protected void initImpl(ServletConfig config)
      throws ServletException {
   }

   /**
    * Sets the profiling mode
    */
   public void setProfileProbability(double probability) {
   }

   /**
    * Makes sure the servlet container supports Servlet API 2.4+.
    */
   protected void checkServletAPIVersion() {
      int major = _servletContext.getMajorVersion();
      int minor = _servletContext.getMinorVersion();

      if (major < 2 || major == 2 && minor < 4) {
         throw new BiancaRuntimeException(
            L.l("Bianca requires Servlet API 2.4+."));
      }
   }

   /**
    * Service.
    */
   @Override
   public void service(HttpServletRequest request,
                       HttpServletResponse response)
      throws ServletException, IOException {
      Env env = null;
      WriteStream ws = null;

      try {
         Path path = getPath(request);

         BiancaPage page;

         try {
            page = getBianca().parse(path);
         } catch (FileNotFoundException ex) {
            // php/2001
            log.log(Level.FINER, ex.toString(), ex);

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
         }


         ws = openWrite(response);

         // php/6006
         ws.setNewlineString("\n");

         BiancaContext bianca = getBianca();

         env = bianca.createEnv(page, ws, request, response);
         bianca.setServletContext(_servletContext);

         try {
            env.start();

            // php/2030, php/2032, php/2033
            // Jetty hides server classes from web-app
            // http://docs.codehaus.org/display/JETTY/Classloading
            //
            // env.setGlobalValue("request", env.wrapJava(request));
            // env.setGlobalValue("response", env.wrapJava(response));
            // env.setGlobalValue("servletContext", env.wrapJava(_servletContext));

            StringValue prepend = bianca.getIniValue("auto_prepend_file").toStringValue(env);
            if (prepend.length() > 0) {
               Path prependPath = env.lookup(prepend);

               if (prependPath == null) {
                  env.error(L.l("auto_prepend_file '{0}' not found.", prepend));
               } else {
                  BiancaPage prependPage = getBianca().parse(prependPath);
                  prependPage.executeTop(env);
               }
            }

            env.executeTop();

            StringValue append = bianca.getIniValue("auto_append_file").toStringValue(env);
            if (append.length() > 0) {
               Path appendPath = env.lookup(append);

               if (appendPath == null) {
                  env.error(L.l("auto_append_file '{0}' not found.", append));
               } else {
                  BiancaPage appendPage = getBianca().parse(appendPath);
                  appendPage.executeTop(env);
               }
            }
            //   return;
         } catch (BiancaExitException e) {
            throw e;
         } catch (BiancaErrorException e) {
            throw e;
         } catch (BiancaLineRuntimeException e) {
            log.log(Level.FINE, e.toString(), e);

            ws.println(e.getMessage());
            //  return;
         } catch (BiancaValueException e) {
            log.log(Level.FINE, e.toString(), e);

            ws.println(e.toString());

            //  return;
         } catch (Throwable e) {
            if (response.isCommitted()) {
               e.printStackTrace(ws.getPrintWriter());
            }

            ws = null;

            throw e;
         } finally {
            if (env != null) {
               env.close();
            }

            // don't want a flush for an exception
            if (ws != null && env.getDuplex() == null) {
               ws.close();
            }
         }
      } catch (BiancaDieException e) {
         // normal exit
         log.log(Level.FINE, e.toString(), e);
      } catch (BiancaExitException e) {
         // normal exit
         log.log(Level.FINER, e.toString(), e);
      } catch (BiancaErrorException e) {
         // error exit
         log.log(Level.FINE, e.toString(), e);
      } catch (RuntimeException e) {
         throw e;
      } catch (Throwable e) {
         throw new ServletException(e);
      }
   }

   protected WriteStream openWrite(HttpServletResponse response)
      throws IOException {
      WriteStream ws;

      OutputStream out = response.getOutputStream();

      ws = Vfs.openWrite(out);

      return ws;
   }

   Path getPath(HttpServletRequest req) {
      String scriptPath = BiancaRequestAdapter.getPageServletPath(req);
      String pathInfo = BiancaRequestAdapter.getPagePathInfo(req);

      Path pwd = Vfs.lookup();

      Path path = pwd.lookup(req.getRealPath(scriptPath));

      if (path.isFile()) {
         return path;
      }

      // TODO: include

      String fullPath;
      if (pathInfo != null) {
         fullPath = scriptPath + pathInfo;
      } else {
         fullPath = scriptPath;
      }

      return pwd.lookup(req.getRealPath(fullPath));
   }

   /**
    * Returns the Bianca instance.
    */
   protected BiancaContext getBianca() {
      synchronized (this) {
         if (_bianca == null) {
            _bianca = new BiancaContext();
         }
      }

      return _bianca;
   }

   /**
    * Destroys the bianca instance.
    */
   @Override
   public void destroy() {
      _bianca.close();
   }
}
