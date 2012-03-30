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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.clevercloud.xpath.functions;

import com.clevercloud.util.L10N;
import com.clevercloud.xml.QAbstractNode;
import com.clevercloud.xpath.Expr;
import com.clevercloud.xpath.ExprEnvironment;
import com.clevercloud.xpath.XPathException;
import com.clevercloud.xpath.XPathParseException;
import com.clevercloud.xpath.expr.AbstractStringExpr;
import com.clevercloud.xpath.pattern.NodeIterator;
import org.w3c.dom.Node;

/**
 * Returns the base URI of a node.
 */
public class BaseURI extends AbstractStringExpr {
   private static final L10N L = new L10N(BaseURI.class);

   private Expr _expr;

   public BaseURI(Expr expr)
      throws XPathParseException {
      _expr = expr;

      if (expr == null)
         throw new XPathParseException(L.l("fn:base-uri requires a single argument"));
   }

   /**
    * Evaluates the expression as an string.
    *
    * @param node the current node
    * @param env  the variable environment.
    * @return the string representation of the expression.
    */
   public String evalString(Node node, ExprEnvironment env)
      throws XPathException {
      NodeIterator iter = _expr.evalNodeSet(node, env);

      Node result = iter.next();

      return QAbstractNode.baseURI(result);
   }

   public String toString() {
      return "fn:base-uri(" + _expr + ")";
   }
}
