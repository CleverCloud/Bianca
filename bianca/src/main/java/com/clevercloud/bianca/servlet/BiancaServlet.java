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

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.BiancaRuntimeException;
import com.clevercloud.bianca.module.BiancaModule;
import com.clevercloud.config.ConfigException;
import com.clevercloud.util.L10N;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to call PHP through javax.script.
 */
public class BiancaServlet
   extends HttpServlet {

   private static final L10N L = new L10N(BiancaServlet.class);
   private static final Logger log = Logger.getLogger(BiancaServlet.class.getName());
   private BiancaContext _bianca;
   private BiancaServletImpl _impl;
   private boolean _isCompile;
   private boolean _isLazyCompile = true;
   private boolean _isCompileFailover = true;
   private double _profileProbability;
   private boolean _isRequireSource = true;
   private DataSource _database;
   private boolean _isStrict;
   private boolean _isLooseParse = true;
   private int _pageCacheSize = -1;
   private int _regexpCacheSize = -1;
   private boolean _isConnectionPool = true;
   private String _iniPath;
   private String _scriptEncoding;
   private String _mysqlVersion;
   private String _phpVersion;
   protected File _licenseDirectory;
   private String _jdbcEncoding;
   private boolean _unicode = false;
   private ArrayList<BiancaModule> _moduleList = new ArrayList<BiancaModule>();
   private ArrayList<PhpClassConfig> _classList = new ArrayList<PhpClassConfig>();
   private ArrayList<PhpClassConfig> _classImplList = new ArrayList<PhpClassConfig>();
   private ArrayList<PhpIni> _phpIniList = new ArrayList<PhpIni>();
   private ArrayList<ServerEnv> _serverEnvList = new ArrayList<ServerEnv>();

   public BiancaServlet() {
      checkJavaVersion();
   }

   protected BiancaServletImpl getBiancaServlet(boolean isBianca) {
      BiancaServletImpl impl = null;

      if (isBianca) {
         try {
            Class cl = Class.forName(
               "com.clevercloud.bianca.servlet.ProBiancaBiancaServlet");

            Constructor cons = cl.getConstructor(File.class);

            impl = (BiancaServletImpl) cons.newInstance(_licenseDirectory);

            //impl = (BiancaServletImpl) cl.newInstance();
         } catch (ConfigException e) {
            log.log(Level.FINEST, e.toString(), e);
            log.info("Bianca compiled mode requires Bianca "
               + "personal or professional licenses");
            log.info(e.getMessage());

         } catch (Exception e) {
            log.log(Level.FINEST, e.toString(), e);
         }

         if (impl == null) {
            try {
               Class cl = Class.forName(
                  "com.clevercloud.bianca.servlet.BiancaBiancaServlet");
               impl = (BiancaServletImpl) cl.newInstance();
            } catch (Exception e) {
               log.log(Level.FINEST, e.toString(), e);
            }
         }
      }

      if (impl == null) {
         try {
            Class cl = Class.forName(
               "com.clevercloud.bianca.servlet.ProBiancaServlet");

            Constructor cons = cl.getConstructor(java.io.File.class);

            impl = (BiancaServletImpl) cons.newInstance(_licenseDirectory);

            //impl = (BiancaServletImpl) cl.newInstance();
         } catch (ConfigException e) {
            log.log(Level.FINEST, e.toString(), e);
            log.info("Bianca compiled mode requires "
               + "valid Bianca professional licenses");
            log.info(e.getMessage());

         } catch (Exception e) {
            log.log(Level.FINEST, e.toString(), e);
         }
      }

      if (impl == null) {
         impl = new BiancaServletImpl();
      }

      return impl;
   }

   /**
    * Make sure Bianca is running on JDK 1.5+.
    */
   private static void checkJavaVersion() {
      String version = System.getProperty("java.version");

      if (version.startsWith("1.3.") || version.startsWith("1.4.")) {
         throw new BiancaRuntimeException(L.l(
            "Bianca requires JDK 1.5 or higher."));
      }
   }

   /**
    * Set true if bianca should be compiled into Java.
    */
   public void setCompile(String isCompile)
      throws ConfigException {
      if ("true".equals(isCompile) || "".equals(isCompile)) {
         _isCompile = true;
         _isLazyCompile = false;
      } else if ("false".equals(isCompile)) {
         _isCompile = false;
         _isLazyCompile = false;
      } else if ("lazy".equals(isCompile)) {
         _isLazyCompile = true;
      } else {
         throw new ConfigException(L.l(
            "'{0}' is an unknown compile value. "
               + "Values are 'true', 'false', or 'lazy'.",
            isCompile));
      }
   }

   /**
    * Set true interpreted pages should be used for pages that fail to compile.
    */
   public void setCompileFailover(String isCompileFailover)
      throws ConfigException {
      if ("true".equals(isCompileFailover) || "".equals(isCompileFailover)) {
         _isCompileFailover = true;
      } else if ("false".equals(isCompileFailover)) {
         _isCompileFailover = false;
      } else {
         throw new ConfigException(L.l(
            "'{0}' is an unknown compile-failover value. "
               + " Values are 'true' or 'false'.",
            isCompileFailover));
      }
   }

   /**
    * Sets the frequency of profiling, expressed as a probability.
    */
   public void setProfileProbability(double probability)
      throws ConfigException {
      _profileProbability = probability;
   }

   /**
    * Set true if the source php is required
    */
   public void setRequireSource(boolean isRequireSource) {
      _isRequireSource = isRequireSource;
   }

   /**
    * Set the default data source.
    */
   public void setDatabase(DataSource database)
      throws ConfigException {
      if (database == null) {
         throw new ConfigException(L.l("invalid database"));
      }

      _database = database;
   }

   /**
    * Sets the strict mode.
    */
   public void setStrict(boolean isStrict) {
      _isStrict = isStrict;
   }

   /**
    * Sets the strict mode.
    */
   public void setLooseParse(boolean isLooseParse) {
      _isLooseParse = isLooseParse;
   }

   /*
    * Sets the max size of the page cache.
    */
   public void setPageCacheEntries(int entries) {
      _pageCacheSize = entries;
   }

   /*
    * Sets the max size of the page cache.
    */
   public void setPageCacheSize(int size) {
      _pageCacheSize = size;
   }

   /*
    * Sets the max size of the regexp cache.
    */
   public void setRegexpCacheSize(int size) {
      _regexpCacheSize = size;
   }

   /*
    * Turns connection pooling on or off.
    */
   public void setConnectionPool(boolean isEnable) {
      _isConnectionPool = isEnable;
   }

   /**
    * Adds a bianca module.
    */
   public void addModule(BiancaModule module)
      throws ConfigException {
      //getBianca().addModule(module);

      _moduleList.add(module);
   }

   /**
    * Adds a bianca class.
    */
   public void addClass(PhpClassConfig classConfig)
      throws ConfigException {
      //getBianca().addJavaClass(classConfig.getName(), classConfig.getType());

      _classList.add(classConfig);
   }

   /**
    * Adds a bianca class.
    */
   public void addImplClass(PhpClassConfig classConfig)
      throws ConfigException {
      //getBianca().addImplClass(classConfig.getName(), classConfig.getType());

      _classImplList.add(classConfig);
   }

   /**
    * Adds a bianca.ini configuration
    */
   public PhpIni createPhpIni()
      throws ConfigException {
      PhpIni ini = new PhpIni();

      _phpIniList.add(ini);

      return ini;
   }

   /**
    * Adds a $_SERVER configuration
    */
   public ServerEnv createServerEnv()
      throws ConfigException {
      ServerEnv ini = new ServerEnv();

      _serverEnvList.add(ini);

      return ini;
   }

   /**
    * Sets a php.ini file.
    */
   public void setIniFile(String relPath) {
      /*
      Bianca bianca = getBianca();

      String realPath = getServletContext().getRealPath(relPath);

      Path path = bianca.getPwd().lookup(realPath);
       */

      _iniPath = relPath;
   }

   /**
    * Sets the script encoding.
    */
   public void setScriptEncoding(String encoding)
      throws ConfigException {
      _scriptEncoding = encoding;
   }

   /**
    * Sets the version of the client mysql library to report as.
    */
   public void setMysqlVersion(String version) {
      _mysqlVersion = version;
   }

   /**
    * Sets the php version that Bianca should report itself as.
    */
   public void setPhpVersion(String version) {
      _phpVersion = version;
   }

   /**
    * Sets JDBC encoding
    */
   public void setJdbcEncoding(String encoding) {
      _jdbcEncoding = encoding;
   }

   public void setUnicode(String unicode) {
      _unicode = Boolean.parseBoolean(unicode);
   }

   /**
    * Sets the directory for Bianca/Bianca licenses.
    */
   public void setLicenseDirectory(String relPath) {
      _licenseDirectory = new File(getServletContext().getRealPath(relPath));
   }

   /**
    * Initializes the servlet.
    */
   @Override
   public void init(ServletConfig config)
      throws ServletException {
      super.init(config);

      Enumeration<String> paramNames = config.getInitParameterNames();

      while (paramNames.hasMoreElements()) {
         String paramName = paramNames.nextElement();
         String paramValue = config.getInitParameter(paramName);

         setInitParam(paramName, paramValue);
      }

      initImpl(config);
   }

   /**
    * Sets a named init-param to the passed value.
    *
    * @throws ServletException if the init-param is not recognized
    */
   protected void setInitParam(String paramName, String paramValue)
      throws ServletException {
      if ("compile".equals(paramName)) {
         setCompile(paramValue);
      } else if ("database".equals(paramName)) {
         try {
            Context ic = new InitialContext();
            DataSource ds;

            if (!paramValue.startsWith("java:comp")) {
               try {
                  ds = (DataSource) ic.lookup("java:comp/env/" + paramValue);
               } catch (Exception e) {
                  // for glassfish
                  ds = (DataSource) ic.lookup(paramValue);
               }
            } else {
               ds = (DataSource) ic.lookup(paramValue);
            }

            if (ds == null) {
               throw new ServletException(L.l(
                  "database '{0}' is not valid", paramValue));
            }

            setDatabase(ds);
         } catch (Exception e) {
            throw new ServletException(e);
         }
      } else if ("ini-file".equals(paramName)) {
         setIniFile(paramValue);
      } else if ("mysql-version".equals(paramName)) {
         setMysqlVersion(paramValue);
      } else if ("php-version".equals(paramName)) {
         setPhpVersion(paramValue);
      } else if ("script-encoding".equals(paramName)) {
         setScriptEncoding(paramValue);
      } else if ("jdbc-encoding".equals(paramName)) {
         setJdbcEncoding(paramValue);
      } else if ("unicode".equals(paramName)) {
         setUnicode(paramValue);
      } else if ("strict".equals(paramName)) {
         setStrict("true".equals(paramValue));
      } else if ("loose-parse".equals(paramName)) {
         setLooseParse("true".equals(paramValue));
      } else if ("page-cache-entries".equals(paramName)
         || "page-cache-size".equals(paramName)) {
         setPageCacheSize(Integer.parseInt(paramValue));
      } else if ("regexp-cache-size".equals(paramName)) {
         setRegexpCacheSize(Integer.parseInt(paramValue));
      } else if ("connection-pool".equals(paramName)) {
         setConnectionPool("true".equals(paramValue));
      } else if ("require-source".equals(paramName)) {
         setRequireSource("true".equals(paramValue));
      } else if ("license-directory".equals(paramName)) {
         setLicenseDirectory(paramValue);
      } else {
         throw new ServletException(
            L.l("'{0}' is not a recognized init-param", paramName));
      }
   }

   private void initImpl(ServletConfig config)
      throws ServletException {
      Class configClass = config.getClass();

      _impl = getBiancaServlet(configClass.getName().startsWith("com.clevercloud"));

      _impl.init(config);

      BiancaContext bianca = getBianca();

      bianca.setCompile(_isCompile);
      bianca.setLazyCompile(_isLazyCompile);
      bianca.setCompileFailover(_isCompileFailover);
      bianca.setProfileProbability(_profileProbability);
      bianca.setRequireSource(_isRequireSource);
      bianca.setDatabase(_database);
      bianca.setStrict(_isStrict);
      bianca.setLooseParse(_isLooseParse);
      bianca.setPageCacheSize(_pageCacheSize);
      bianca.setRegexpCacheSize(_regexpCacheSize);
      bianca.setConnectionPool(_isConnectionPool);

      if (_iniPath != null) {
         String realPath = getServletContext().getRealPath(_iniPath);
         bianca.setIniFile(getBianca().getPwd().lookup(realPath));
      }

      if (_scriptEncoding != null) {
         bianca.setScriptEncoding(_scriptEncoding);
      }

      if (_mysqlVersion != null) {
         bianca.setMysqlVersion(_mysqlVersion);
      }

      if (_phpVersion != null) {
         bianca.setPhpVersion(_phpVersion);
      }

      if (_jdbcEncoding != null) {
         bianca.setJdbcEncoding(_jdbcEncoding);
      }

      for (BiancaModule module : _moduleList) {
         bianca.addModule(module);
      }

      for (PhpClassConfig cls : _classList) {
         bianca.addJavaClass(cls.getName(), cls.getType());
      }

      for (PhpClassConfig cls : _classImplList) {
         bianca.addImplClass(cls.getName(), cls.getType());
      }

      for (PhpIni ini : _phpIniList) {
         for (Map.Entry<String, String> entry : ini._propertyMap.entrySet()) {
            bianca.setIni(entry.getKey(), entry.getValue());
         }
      }

      for (ServerEnv serverEnv : _serverEnvList) {
         for (Map.Entry<String, String> entry : serverEnv._propertyMap.entrySet()) {
            bianca.setServerEnv(entry.getKey(), entry.getValue());
         }
      }
   }

   /**
    * Service.
    */
   @Override
   public void service(HttpServletRequest request,
                       HttpServletResponse response)
      throws ServletException, IOException {
      _impl.service(request, response);
   }

   /**
    * Returns the Bianca instance.
    */
   private BiancaContext getBianca() {
      if (_bianca == null) {
         _bianca = _impl.getBianca();
      }

      return _bianca;
   }

   /**
    * Closes the servlet instance.
    */
   @Override
   public void destroy() {
      _bianca.close();
      _impl.destroy();
   }

   public static class PhpIni {

      HashMap<String, String> _propertyMap = new HashMap<String, String>();

      PhpIni() {
      }

      /**
       * Sets an arbitrary property.
       */
      public void setProperty(String key, String value) {
         //_bianca.setIni(key, value);

         _propertyMap.put(key, value);
      }
   }

   public static class ServerEnv {

      HashMap<String, String> _propertyMap = new HashMap<String, String>();

      ServerEnv() {
      }

      /**
       * Sets an arbitrary property.
       */
      public void setProperty(String key, String value) {
         //_bianca.setServerEnv(key, value);

         _propertyMap.put(key, value);
      }
   }
}
