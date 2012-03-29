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
 * @author Scott Ferguson
 */
package com.clevercloud.bianca.servlet;

import com.clevercloud.config.ConfigException;
import com.clevercloud.util.L10N;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to call PHP through javax.script.
 */
public class GoogleBiancaServlet extends BiancaServlet {

   private static final L10N L = new L10N(GoogleBiancaServlet.class);
   private static final Logger log = Logger.getLogger(GoogleBiancaServlet.class.getName());

   @Override
   protected BiancaServletImpl getBiancaServlet(boolean isResin) {
      BiancaServletImpl impl = null;

      try {
         Class cl = Class.forName(
            "com.clevercloud.bianca.servlet.ProGoogleBiancaServlet");

         Constructor cons = cl.getConstructor(java.io.File.class);

         impl = (BiancaServletImpl) cons.newInstance(_licenseDirectory);

         //impl = (BiancaServletImpl) cl.newInstance();
      } catch (ConfigException e) {
         log.log(Level.FINEST, e.toString(), e);
         log.info(
            "Bianca compiled mode requires valid Bianca professional licenses");
         log.info(e.getMessage());

      } catch (Exception e) {
         log.log(Level.FINEST, e.toString(), e);
      }

      if (impl == null) {
         impl = new GoogleBiancaServletImpl();
      }

      return impl;
   }
}
