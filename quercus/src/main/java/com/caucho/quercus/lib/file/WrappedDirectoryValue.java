/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
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
 * @author Emil Ong
 * @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
 */
package com.caucho.quercus.lib.file;

import com.caucho.quercus.env.*;

/**
 * Represents a PHP directory listing
 */
public class WrappedDirectoryValue extends DirectoryValue {

   private static final StringValue DIR_CLOSEDIR_U = new StringValue("dir_closedir");
   private static final StringValue DIR_OPENDIR_U = new StringValue("dir_opendir");
   private static final StringValue DIR_READDIR_U = new StringValue("dir_readdir");
   private static final StringValue DIR_REWINDDIR_U = new StringValue("dir_rewinddir");
   private Env _env;
   private Value _wrapper;

   public WrappedDirectoryValue(Env env, QuercusClass qClass) {
      super(env);

      _env = env;
      _wrapper = qClass.callNew(_env, new Value[0]);
   }

   public boolean opendir(StringValue path, LongValue flags) {
      return _wrapper.callMethod(_env, DIR_OPENDIR_U, path, flags).toBoolean();
   }

   /**
    * Returns the next value.
    */
   @Override
   public Value readdir() {
      return _wrapper.callMethod(_env, DIR_READDIR_U);
   }

   /**
    * Rewinds the directory
    */
   @Override
   public void rewinddir() {
      _wrapper.callMethod(_env, DIR_REWINDDIR_U);
   }

   /**
    * Closes the directory
    */
   @Override
   public void close() {
      _wrapper.callMethod(_env, DIR_CLOSEDIR_U);
   }

   /**
    * Converts to a string.
    * @param env
    */
   @Override
   public String toString() {
      return "Directory[]";
   }
}
