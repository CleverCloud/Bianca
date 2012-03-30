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
 */
package com.clevercloud.bianca.lib.curl;

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.annotation.Optional;
import com.clevercloud.bianca.env.Env;
import com.clevercloud.bianca.env.StringValue;
import com.clevercloud.bianca.env.Value;
import com.clevercloud.util.L10N;
import com.clevercloud.util.RandomUtil;
import com.clevercloud.vfs.Path;
import com.clevercloud.vfs.ReadStream;
import com.clevercloud.vfs.TempBuffer;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MultipartBody extends PostBody {

   private static final L10N L = new L10N(HttpRequest.class);
   private ArrayList<MultipartEntry> _postItems = new ArrayList<MultipartEntry>();
   private String _boundary;
   private byte[] _boundaryBytes;
   private long _length;

   @Override
   protected boolean init(Env env, Value body) {
      _boundary = createBoundary();
      _boundaryBytes = _boundary.getBytes();

      Iterator<Map.Entry<Value, Value>> iter = body.getIterator(env);

      while (iter.hasNext()) {
         Map.Entry<Value, Value> entry = iter.next();

         StringValue key = entry.getKey().toString(env);
         StringValue value = entry.getValue().toString(env);

         if (value.length() > 0 && value.charAt(0) == '@') {
            StringValue fileName = value.substring(1);

            Path path = env.lookup(fileName);

            if (path == null || !path.canRead()) {
               env.warning(L.l("cannot read file '{0}'", fileName));
               return false;
            }

            _postItems.add(new PathEntry(env, key.toString(), path));
         } else {
            _postItems.add(new UrlEncodedEntry(env, key.toString(), value));
         }
      }

      _length = getContentLength(_postItems, _boundary);

      return true;
   }

   private static String createBoundary() {
      return "boundary" + RandomUtil.getRandomLong();
   }

   private static long getContentLength(ArrayList<MultipartEntry> list,
                                        String boundary) {
      long size = (boundary.length() + 2) + 4;

      for (MultipartEntry entry : list) {
         size += entry.getLength() + (boundary.length() + 4) + 2;
      }

      return size;
   }

   @Override
   public String getContentType(@Optional String contentType) {
      return "multipart/form-data; boundary=\"" + _boundary + "\"";
   }

   @Override
   public long getContentLength() {
      return _length;
   }

   @Override
   public void writeTo(Env env,
                       OutputStream os)
      throws IOException {
      for (MultipartEntry entry : _postItems) {
         os.write('-');
         os.write('-');
         os.write(_boundaryBytes);

         os.write('\r');
         os.write('\n');

         entry.write(env, os);

         os.write('\r');
         os.write('\n');
      }

      os.write('-');
      os.write('-');
      os.write(_boundaryBytes);
      os.write('-');
      os.write('-');

      os.write('\r');
      os.write('\n');
   }

   static abstract class MultipartEntry {

      final String _name;
      final String _header;

      MultipartEntry(Env env, String name, String header) {
         _name = name;
         _header = header;
      }

      final String getName() {
         return _name;
      }

      static String getHeader(String name,
                              String contentType,
                              String fileName) {
         StringBuilder sb = new StringBuilder();

         sb.append("Content-Disposition: form-data;");

         sb.append(" name=\"");
         sb.append(name);
         sb.append("\"");

         if (fileName != null) {
            sb.append("; filename=\"");
            sb.append(fileName);
            sb.append("\"");

            sb.append('\r');
            sb.append('\n');
            sb.append("Content-Type: ");
            sb.append(contentType);
         }

         return sb.toString();
      }

      final void write(Env env, OutputStream os)
         throws IOException {
         int len = _header.length();

         for (int i = 0; i < len; i++) {
            os.write(_header.charAt(i));
         }

         os.write('\r');
         os.write('\n');
         os.write('\r');
         os.write('\n');

         writeData(env, os);
      }

      final long getLength() {
         return _header.length() + 4 + getLengthImpl();
      }

      abstract long getLengthImpl();

      abstract void writeData(Env env, OutputStream os) throws IOException;
   }

   static class UrlEncodedEntry extends MultipartEntry {

      StringValue _value;

      UrlEncodedEntry(Env env, String name, StringValue value) {
         super(env, name, getHeader(name,
            "application/x-www-form-urlencoded",
            null));
         _value = value;
      }

      @Override
      long getLengthImpl() {
         return _value.length();
      }

      @Override
      void writeData(Env env, OutputStream os)
         throws IOException {
         os.write(_value.toString().getBytes());
      }
   }

   static class PathEntry extends MultipartEntry {

      Path _path;

      PathEntry(Env env, String name, Path path) {
         super(env, name, getHeader(name,
            getContentType(env, path.getTail()),
            path.getTail()));
         _path = path;
      }

      @Override
      long getLengthImpl() {
         return _path.getLength();
      }

      @Override
      void writeData(Env env, OutputStream os)
         throws IOException {
         TempBuffer tempBuf = null;

         try {
            tempBuf = TempBuffer.allocate();
            byte[] buf = tempBuf.getBuffer();

            ReadStream is = _path.openRead();

            int len;
            while ((len = is.read(buf, 0, buf.length)) > 0) {
               os.write(buf, 0, len);
            }

         } finally {
            if (tempBuf != null) {
               TempBuffer.free(tempBuf);
            }
         }
      }

      private static String getContentType(Env env, String name) {
         BiancaContext bianca = env.getBianca();

         ServletContext context = bianca.getServletContext();

         if (context != null) {
            String mimeType = context.getMimeType(name);

            if (mimeType != null) {
               return mimeType;
            } else {
               return "application/octet-stream";
            }
         } else {
            int i = name.lastIndexOf('.');

            if (i < 0) {
               return "application/octet-stream";
            } else if (name.endsWith(".txt")) {
               return "text/plain";
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
               return "image/jpeg";
            } else if (name.endsWith(".gif")) {
               return "image/gif";
            } else if (name.endsWith(".tif") || name.endsWith(".tiff")) {
               return "image/tiff";
            } else if (name.endsWith(".png")) {
               return "image/png";
            } else if (name.endsWith(".htm") || name.endsWith(".html")) {
               return "text/html";
            } else if (name.endsWith(".xml")) {
               return "text/xml";
            } else {
               return "application/octet-stream";
            }
         }

      }
   }
}
