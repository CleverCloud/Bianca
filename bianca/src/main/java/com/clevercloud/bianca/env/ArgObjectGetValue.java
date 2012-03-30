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
 */
package com.clevercloud.bianca.env;

/**
 * Represents an object-get argument which might be a call to a reference.
 */
public class ArgObjectGetValue extends Value {

   private final Env _env;
   private final Value _obj;
   private final Value _index;

   public ArgObjectGetValue(Env env, Value obj, Value index) {
      _env = env;
      _obj = obj;
      _index = index;
   }

   /**
    * Returns the value for a get arg.
    */
   @Override
   public Value getArg(Value index, boolean isTop) {
      // bianca/3d2u
      return new ArgObjectGetValue(_env, this, index);
   }

   /**
    * Converts to a ref var.
    */
   @Override
   public Var toLocalVarDeclAsRef() {
      // bianca/3d2t
      return _obj.getVar(_index).toLocalVarDeclAsRef();
   }
   /**
    * Converts to a var.
    */
   /*
   public Var toVar()
   {
   // bianca/3d52
   return _obj.get(_index).toVar();
   }
    */
   /**
    * Returns the value, converting to an object if necessary.
    */
   /*
   public Value getObject(Env env, Value index)
   {
   return _obj.getObject(_env, _index).getObject(env, index);
   }
    */
}
