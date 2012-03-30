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
package com.clevercloud.bianca.env;

import com.clevercloud.bianca.function.AbstractFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a compiled object value.
 */
public class CompiledObjectValue extends ObjectValue
   implements Serializable {

   private static final Value[] NULL_FIELDS = new Value[0];
   public Value[] _fields;
   private ObjectExtValue _object;

   public CompiledObjectValue(BiancaClass cl) {
      super(cl);

      int size = cl.getFieldSize();
      if (size != 0) {
         _fields = new Value[cl.getFieldSize()];
      } else {
         _fields = NULL_FIELDS;
      }
   }

   /**
    * Returns the number of entries.
    */
   @Override
   public int getSize() {
      int size = 0;

      for (int i = 0; i < _fields.length; i++) {
         if (_fields[i] != UnsetValue.UNSET) {
            size++;
         }
      }

      if (_object != null) {
         size += _object.getSize();
      }

      return size;
   }

   /**
    * Gets a new value.
    */
   @Override
   public Value getField(Env env, StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            return _fields[index].toValue();
         }
      }

      if (_object != null) {
         return _object.getField(env, key);
      } else {
         return UnsetValue.UNSET;
      }
   }

   /**
    * Returns the array ref.
    */
   @Override
   public Var getFieldVar(Env env, StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            Var var = _fields[index].toLocalVarDeclAsRef();

            _fields[index] = var;

            return var;
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.getFieldVar(env, key);
   }

   /**
    * Returns the value as an argument which may be a reference.
    */
   @Override
   public Value getFieldArg(Env env, StringValue key, boolean isTop) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            Var var = _fields[index].toLocalVarDeclAsRef();

            _fields[index] = var;

            return var;
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.getFieldArg(env, key, isTop);
   }

   /**
    * Returns the value as an argument which may be a reference.
    */
   @Override
   public Value getFieldArgRef(Env env, StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            Var var = _fields[index].toLocalVarDeclAsRef();

            _fields[index] = var;

            return var;
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.getFieldArgRef(env, key);
   }

   /**
    * Returns field as an array.
    */
   @Override
   public Value getFieldArray(Env env, StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            _fields[index] = _fields[index].toAutoArray();

            return _fields[index];
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.getFieldArray(env, key);
   }

   /**
    * Returns field as an object.
    */
   @Override
   public Value getFieldObject(Env env, StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            _fields[index] = _fields[index].toAutoObject(env);

            return _fields[index];
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.getFieldObject(env, key);
   }

   /**
    * Adds a new value.
    */
   @Override
   public Value putField(Env env, StringValue key, Value value) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            _fields[index] = _fields[index].set(value);

            return value;
         }
      }

      if (_object == null) {
         _object = new ObjectExtValue(_biancaClass);
      }

      return _object.putField(env, key, value);
   }

   /**
    * Removes a value.
    */
   @Override
   public void unsetField(StringValue key) {
      if (_fields.length > 0) {
         int index = _biancaClass.findFieldIndex(key);

         if (index >= 0) {
            _fields[index] = UnsetValue.UNSET;

            return;
         }
      }

      if (_object != null) {
         _object.unsetField(key);
      }
   }

   /**
    * Finds the method name.
    */
   @Override
   public AbstractFunction findFunction(String methodName) {
      return _biancaClass.findFunction(methodName);
   }

   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName, Value []args)
   {
   AbstractFunction fun = _biancaClass.findFunction(methodName);

   if (fun != null)
   return fun.callMethod(env, this, args);
   else
   return env.error(L.l("Call to undefined method {0}::{1}()",
   _biancaClass.getName(), methodName));
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName, Value a0)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this, a0);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName,
   Value a0, Value a1)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this, a0, a1);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName,
   Value a0, Value a1, Value a2)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this,
   a0, a1, a2);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName,
   Value a0, Value a1, Value a2, Value a3)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this,
   a0, a1, a2, a3);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethod(Env env, String methodName,
   Value a0, Value a1, Value a2, Value a3, Value a4)
   {
   return _biancaClass.getFunction(methodName).callMethod(env, this,
   a0, a1, a2, a3, a4);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName, Expr []args)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this, args);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName, Value []args)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this, args);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName, Value a0)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this, a0);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName,
   Value a0, Value a1)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(
   env, this, a0, a1);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName,
   Value a0, Value a1, Value a2)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this,
   a0, a1, a2);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName,
   Value a0, Value a1, Value a2, Value a3)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this,
   a0, a1, a2, a3);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   public Value callMethodRef(Env env, String methodName,
   Value a0, Value a1, Value a2, Value a3, Value a4)
   {
   return _biancaClass.getFunction(methodName).callMethodRef(env, this,
   a0, a1, a2, a3, a4);
   }
    */
   /**
    * Evaluates a method.
    */
   /*
   @Override
   public Value callClassMethod(Env env, AbstractFunction fun, Value []args)
   {
   Value oldThis = env.getThis();

   try {
   env.setThis(this);

   return fun.call(env, args);
   } finally {
   env.setThis(oldThis);
   }
   }
    */

   /**
    * Returns the value for the variable, creating an object if the var
    * is unset.
    */
   @Override
   public Value getObject(Env env) {
      return this;
   }

   /**
    * Copy for assignment.
    */
   @Override
   public Value copy() {
      return this;
   }

   /**
    * Copy for serialization
    */
   @Override
   public Value copy(Env env, IdentityHashMap<Value, Value> map) {
      Value oldValue = map.get(this);

      if (oldValue != null) {
         return oldValue;
      }

      // TODO:
      // return new ObjectExtValue(env, map, _cl, getArray());

      return this;
   }

   /**
    * Clone the object
    */
   @Override
   public Value clone(Env env) {
      throw new UnsupportedOperationException();
   }

   // TODO: need to check the other copy, e.g. for sessions
   /**
    * Serializes the value.
    */
   /*
   @Override
   public void serialize(Env env, StringBuilder sb, SerializeMap map)
   {
   sb.append("O:");
   sb.append(_biancaClass.getName().length());
   sb.append(":\"");
   sb.append(_biancaClass.getName());
   sb.append("\":");
   sb.append(getSize());
   sb.append(":{");

   HashMap<StringValue,ClassField> names = _biancaClass.getClassFields();

   if (names != null) {
   int index = 0;

   for (int i = 0; i < names.size(); i++) {
   StringValue key = names.get(i);

   if (_fields[i] == UnsetValue.UNSET)
   continue;

   sb.append("s:");
   sb.append(key.length());
   sb.append(":\"");
   sb.append(key);
   sb.append("\";");

   _fields[i].serialize(env, sb, map);
   }
   }

   if (_object != null) {
   for (Map.Entry<Value,Value> mapEntry : _object.sortedEntrySet()) {
   ObjectExtValue.Entry entry = (ObjectExtValue.Entry) mapEntry;

   StringValue key = entry.getKey().toStringValue();

   sb.append("s:");
   sb.append(key.length());
   sb.append(":\"");
   sb.append(key);
   sb.append("\";");

   entry.getValue().serialize(env, sb, map);
   }
   }

   sb.append("}");
   }
    */
   /**
    * Converts to a string.
    * @param env
    */
   /*
   @Override
   public StringValue toString(Env env)
   {
   AbstractFunction fun = _biancaClass.findFunction("__toString");

   if (fun != null)
   return fun.callMethod(env, this, new Expr[0]).toString(env);
   else
   return new StringValue().append(_biancaClass.getName()).append("[]");
   }
    */

   /**
    * Converts to a string.
    *
    * @param env
    */
   @Override
   public void print(Env env) {
      env.print(toString(env));
   }

   /**
    * Converts to an array.
    */
   @Override
   public Value toArray() {
      ArrayValue array = new ArrayValueImpl();

      for (Map.Entry<Value, Value> entry : entrySet()) {
         array.put(entry.getKey().toStringValue(), entry.getValue());
      }

      return array;
   }

   /**
    * Converts to an object.
    */
   @Override
   public Value toObject(Env env) {
      return this;
   }

   /**
    * Converts to an object.
    */
   @Override
   public Object toJavaObject() {
      return this;
   }

   @Override
   public Set<? extends Map.Entry<Value, Value>> entrySet() {
      throw new UnsupportedOperationException();
      // return new EntrySet();
   }

   /**
    * Returns a Set of entries, sorted by key.
    */
   public Set<? extends Map.Entry<Value, Value>> sortedEntrySet() {
      throw new UnsupportedOperationException();
      //return new TreeSet<Map.Entry<String, Value>>(entrySet());
   }

   @Override
   public String toString() {
      return "CompiledObjectValue@" + System.identityHashCode(this)
         + "[" + _biancaClass.getName() + "]";
   }

   //
   // Java Serialization
   //
   private void writeObject(ObjectOutputStream out)
      throws IOException {
      out.writeObject(_fields);
      out.writeObject(_object);
      out.writeObject(_biancaClass.getName());
   }

   private void readObject(ObjectInputStream in)
      throws ClassNotFoundException, IOException {
      _fields = (Value[]) in.readObject();
      _object = (ObjectExtValue) in.readObject();

      Env env = Env.getInstance();
      String name = (String) in.readObject();

      BiancaClass cl = env.findClass(name);

      if (cl != null) {
         setBiancaClass(cl);
      } else {
         cl = env.getBianca().getStdClass();

         setBiancaClass(cl);

         setIncompleteObjectName(name);
      }
   }
}
