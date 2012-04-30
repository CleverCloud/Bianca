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
package com.clevercloud.bianca.parser;

import com.clevercloud.bianca.BiancaContext;
import com.clevercloud.bianca.BiancaRuntimeException;
import com.clevercloud.bianca.Location;
import com.clevercloud.bianca.env.*;
import com.clevercloud.bianca.expr.*;
import com.clevercloud.bianca.function.AbstractFunction;
import com.clevercloud.bianca.program.*;
import com.clevercloud.bianca.statement.BlockStatement;
import com.clevercloud.bianca.statement.Statement;
import com.clevercloud.bianca.statement.TryStatement;
import com.clevercloud.util.L10N;
import com.clevercloud.vfs.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Parses a PHP program.
 */
public class BiancaParser {

   private final static L10N L = new L10N(BiancaParser.class);
   private final static int M_STATIC = 0x1;
   private final static int M_PUBLIC = 0x2;
   private final static int M_PROTECTED = 0x4;
   private final static int M_PRIVATE = 0x8;
   private final static int M_FINAL = 0x10;
   private final static int M_ABSTRACT = 0x20;
   private final static int M_INTERFACE = 0x40;

   private BiancaLexer _lexer;
   private BiancaContext _bianca;
   private Path _sourceFile;
   private int _sourceOffset; // offset into the source file for the first line
   private ParserLocation _parserLocation = new ParserLocation();
   private ExprFactory _factory;
   private ReadStream _is;
   private GlobalScope _globalScope;
   private boolean _returnsReference = false;
   private Scope _scope;
   private InterpretedClassDef _classDef;
   private FunctionInfo _function;
   private boolean _isTop;
   private boolean _isNewExpr;
   private boolean _isIfTest;
   private int _classesParsed;
   private int _functionsParsed;
   private ArrayList<String> _loopLabelList = new ArrayList<String>();
   private int _labelsCreated;

   private BiancaParser(BiancaContext bianca) {
      _bianca = bianca;

      if (bianca == null) {
         _factory = ExprFactory.create();
      } else {
         _factory = bianca.createExprFactory();
      }

      _globalScope = new GlobalScope(_factory);
      _scope = _globalScope;
   }

   public BiancaParser(BiancaContext bianca,
                       Path sourceFile,
                       ReadStream is) {
      this(bianca);

      init(sourceFile, is);
   }

   private void init(Path sourceFile)
      throws IOException {
      init(sourceFile, sourceFile.openRead());
   }

   private void init(Path sourceFile, ReadStream is) {
      _is = is;

      if (sourceFile != null) {
         _parserLocation.setFileName(sourceFile);
         _sourceFile = sourceFile;
      } else {
         _parserLocation.setFileName("eval:");

         // php/2146
         _sourceFile = new NullPath("eval:");
      }

      _parserLocation.setLineNumber(1);

      if (_lexer == null)
         _lexer = new BiancaLexer(this);
      _lexer.init(is);
   }

   public void setLocation(String fileName, int line) {
      _parserLocation.setFileName(fileName);
      _parserLocation.setLineNumber(line);

      if (line > 0) {
         _sourceOffset = 1 - line;
      }
   }

   public static BiancaProgram parse(BiancaContext bianca,
                                     Path path,
                                     String encoding)
      throws IOException {
      ReadStream is = path.openRead();

      try {
         is.setEncoding(encoding);

         BiancaParser parser;
         parser = new BiancaParser(bianca, path, is);

         return parser.parse();
      } finally {
         is.close();
      }
   }

   public static BiancaProgram parse(BiancaContext bianca,
                                     Path path,
                                     String encoding,
                                     String fileName,
                                     int line)
      throws IOException {
      ReadStream is = path.openRead();

      try {
         is.setEncoding(encoding);

         BiancaParser parser;
         parser = new BiancaParser(bianca, path, is);

         if (fileName != null && line >= 0) {
            parser.setLocation(fileName, line);
         }

         return parser.parse();
      } finally {
         is.close();
      }
   }

   public static BiancaProgram parse(BiancaContext bianca,
                                     ReadStream is)
      throws IOException {
      BiancaParser parser;
      parser = new BiancaParser(bianca, is.getPath(), is);

      return parser.parse();
   }

   public static BiancaProgram parse(BiancaContext bianca,
                                     Path path, ReadStream is)
      throws IOException {
      return new BiancaParser(bianca, path, is).parse();
   }

   public static BiancaProgram parseEval(BiancaContext bianca, String str)
      throws IOException {
      Path path = new StringPath(str);

      BiancaParser parser = new BiancaParser(bianca, path, path.openRead());

      return parser.parseCode();
   }

   public static BiancaProgram parseEvalExpr(BiancaContext bianca, String str)
      throws IOException {
      Path path = new StringPath(str);

      BiancaParser parser = new BiancaParser(bianca, path, path.openRead());

      return parser.parseCode().createExprReturn();
   }

   public static AbstractFunction parseFunction(BiancaContext bianca,
                                                String name,
                                                String args,
                                                String code)
      throws IOException {
      Path argPath = new StringPath(args);
      Path codePath = new StringPath(code);

      BiancaParser parser = new BiancaParser(bianca);

      Function fun = parser.parseFunction(name, argPath, codePath);

      parser.close();

      return fun;
   }

   public static Expr parse(BiancaContext bianca, String str)
      throws IOException {
      Path path = new StringPath(str);

      return new BiancaParser(bianca, path, path.openRead()).parseExpr();
   }

   public static Expr parseDefault(String str) {
      try {
         Path path = new StringPath(str);

         return new BiancaParser(null, path, path.openRead()).parseExpr();
      } catch (IOException e) {
         throw new BiancaRuntimeException(e);
      }
   }

   public void incrementLineNumber() {
      _parserLocation.incrementLineNumber();
   }

   public static Expr parseDefault(ExprFactory factory, String str) {
      try {
         Path path = new StringPath(str);

         BiancaParser parser = new BiancaParser(null, path, path.openRead());

         parser._factory = factory;

         return parser.parseExpr();
      } catch (IOException e) {
         throw new BiancaRuntimeException(e);
      }
   }

   /**
    * Returns the current filename.
    */
   public String getFileName() {
      if (_sourceFile == null) {
         return null;
      } else {
         return _sourceFile.getPath();
      }
   }

   /**
    * Returns the current class name
    */
   public String getClassName() {
      if (_classDef != null) {
         return _classDef.getName();
      } else {
         return null;
      }
   }

   /**
    * Returns the current line
    */
   public int getLine() {
      return _parserLocation.getLineNumber();
   }

   public ExprFactory getExprFactory() {
      return _factory;
   }

   public ExprFactory getFactory() {
      return _factory;
   }

   public BiancaProgram parse()
      throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_bianca, globalClass, "");
      _function.setPageMain(true);

      // bianca/0b0d
      _function.setVariableVar(true);
      _function.setUsesSymbolTable(true);

      Statement stmt = parseTop();

      BiancaProgram program = new BiancaProgram(_bianca, _sourceFile,
         _globalScope.getFunctionMap(),
         _globalScope.getFunctionList(),
         _globalScope.getClassMap(),
         _globalScope.getClassList(),
         _function,
         stmt);
      return program;

      /*
      com.clevercloud.vfs.WriteStream out = com.clevercloud
      .vfs.Vfs.lookup("stdout:").openWrite();
      out.setFlushOnNewline(true);
      stmt.debug(new JavaWriter(out));
       */
   }

   BiancaProgram parseCode()
      throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_bianca, globalClass, "eval");
      // TODO: need param or better function name for non-global?
      _function.setGlobal(false);

      Location location = getLocation();

      ArrayList<Statement> stmtList = parseStatementList();

      return new BiancaProgram(_bianca, _sourceFile,
         _globalScope.getFunctionMap(),
         _globalScope.getFunctionList(),
         _globalScope.getClassMap(),
         _globalScope.getClassList(),
         _function,
         _factory.createBlock(location, stmtList));
   }

   public Function parseFunction(String name, Path argPath, Path codePath)
      throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_bianca, globalClass, name);
      _function.setGlobal(false);
      _function.setPageMain(true);

      init(argPath);

      Arg[] args = parseFunctionArgDefinition();

      close();

      init(codePath);

      Statement[] statements = parseStatements();

      Function fun = _factory.createFunction(Location.UNKNOWN,
         name,
         _function,
         args,
         statements);

      close();

      return fun;
   }

   /**
    * Parses the top page.
    */
   Statement parseTop()
      throws IOException {
      _isTop = true;

      ArrayList<Statement> statements = new ArrayList<Statement>();

      Location location = getLocation();

      BiancaLexer.Token token = _lexer.parsePhpText();

      if (_lexer.getLexeme().length() > 0) {
         statements.add(_factory.createText(location, _lexer.getLexeme()));
      }

      if (token == BiancaLexer.Token.TEXT_ECHO) {
         parseEcho(statements);
      } else if (token == BiancaLexer.Token.TEXT_PHP) {
         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.IDENTIFIER && "php".equalsIgnoreCase(_lexer.getLexeme())) {
            _lexer.dropToken();
         }
      }

      statements.addAll(parseStatementList());

      return _factory.createBlock(location, statements);
   }

   /*
    * Parses a statement list.
    */
   private Statement[] parseStatements()
      throws IOException {
      ArrayList<Statement> statementList = parseStatementList();

      Statement[] statements = new Statement[statementList.size()];

      statementList.toArray(statements);

      return statements;
   }

   /**
    * Parses a statement list.
    */
   private ArrayList<Statement> parseStatementList()
      throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      while (true) {
         Location location = getLocation();

         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case NONE:
               return statementList;

            case SEMICOLUMN:
               break;

            case ECHO:
               parseEcho(statementList);
               break;

            case PRINT:
               statementList.add(parsePrint());
               break;

            case UNSET:
               parseUnset(statementList);
               break;

            case ABSTRACT:
            case FINAL: {
               _lexer.saveToken(token);

               int modifiers = 0;
               do {
                  token = _lexer.parseToken();

                  switch (token) {
                     case ABSTRACT:
                        modifiers |= M_ABSTRACT;
                        break;
                     case FINAL:
                        modifiers |= M_FINAL;
                        break;
                     case CLASS:
                        statementList.add(parseClassDefinition(modifiers));
                        break;
                     default:
                        throw error(L.l("expected 'class' at {0}",
                           _lexer.tokenName(token)));
                  }
               } while (token != BiancaLexer.Token.CLASS);
            }
            break;

            case FUNCTION: {
               Location functionLocation = getLocation();

               Function fun = parseFunctionDefinition(M_STATIC);

               if (!_isTop) {
                  statementList.add(
                     _factory.createFunctionDef(functionLocation, fun));
               }
            }
            break;

            case CLASS:
               // parseClassDefinition(0);
               statementList.add(parseClassDefinition(0));
               break;

            case INTERFACE:
               // parseClassDefinition(M_INTERFACE);
               statementList.add(parseClassDefinition(M_INTERFACE));
               break;

            case CONST:
               statementList.addAll(parseConstDefinition());
               break;

            case IF:
               statementList.add(parseIf());
               break;

            case SWITCH:
               statementList.add(parseSwitch());
               break;

            case WHILE:
               statementList.add(parseWhile());
               break;

            case DO:
               statementList.add(parseDo());
               break;

            case FOR:
               statementList.add(parseFor());
               break;

            case FOREACH:
               statementList.add(parseForeach());
               break;

            case PHP_END:
               return statementList;

            case RETURN:
               statementList.add(parseReturn());
               break;

            case THROW:
               statementList.add(parseThrow());
               break;

            case BREAK:
               statementList.add(parseBreak());
               break;

            case CONTINUE:
               statementList.add(parseContinue());
               break;

            case GLOBAL:
               statementList.add(parseGlobal());
               break;

            case STATIC:
               statementList.add(parseStatic());
               break;

            case TRY:
               statementList.add(parseTry());
               break;

            case NAMESPACE:
               statementList.addAll(parseNamespace());
               break;

            case USE:
               parseUse();
               break;

            case LEFT_BRACE: {
               ArrayList<Statement> enclosedStatementList = parseStatementList();

               _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

               statementList.addAll(enclosedStatementList);
            }
            break;

            case RIGHT_BRACE:
            case CASE:
            case DEFAULT:
            case ELSE:
            case ELSEIF:
            case ENDIF:
            case ENDWHILE:
            case ENDFOR:
            case ENDFOREACH:
            case ENDSWITCH:
               _lexer.saveToken(token);
               return statementList;

            case TEXT:
               if (_lexer.getLexeme().length() > 0) {
                  statementList.add(_factory.createText(location, _lexer.getLexeme()));
               }
               break;

            case TEXT_PHP:
               if (_lexer.getLexeme().length() > 0) {
                  statementList.add(_factory.createText(location, _lexer.getLexeme()));
               }

               token = _lexer.parseToken();

               if (token == BiancaLexer.Token.IDENTIFIER && "php".equalsIgnoreCase(_lexer.getLexeme())) {
                  _lexer.dropToken();
               }
               break;

            case TEXT_ECHO:
               if (_lexer.getLexeme().length() > 0) {
                  statementList.add(_factory.createText(location, _lexer.getLexeme()));
               }

               parseEcho(statementList);

               break;

            default:
               _lexer.saveToken(token);

               statementList.add(parseExprStatement());
               break;
         }
      }
   }

   private Statement parseStatement()
      throws IOException {
      Location location = getLocation();

      BiancaLexer.Token token = _lexer.parseToken();

      switch (token) {
         case SEMICOLUMN:
            return _factory.createNullStatement();

         case LEFT_BRACE:
            location = getLocation();

            ArrayList<Statement> statementList = parseStatementList();

            _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

            return _factory.createBlock(location, statementList);

         case IF:
            return parseIf();

         case SWITCH:
            return parseSwitch();

         case WHILE:
            return parseWhile();

         case DO:
            return parseDo();

         case FOR:
            return parseFor();

         case FOREACH:
            return parseForeach();

         case TRY:
            return parseTry();

         case TEXT:
            if (_lexer.getLexeme().length() > 0) {
               return _factory.createText(location, _lexer.getLexeme());
            } else {
               return parseStatement();
            }

         case TEXT_PHP: {
            Statement stmt = null;

            if (_lexer.getLexeme().length() > 0) {
               stmt = _factory.createText(location, _lexer.getLexeme());
            }

            token = _lexer.parseToken();

            if (token == BiancaLexer.Token.IDENTIFIER && "php".equalsIgnoreCase(_lexer.getLexeme())) {
               _lexer.dropToken();
            }

            if (stmt == null) {
               stmt = parseStatement();
            }

            return stmt;
         }

         default:
            Statement stmt = parseStatementImpl(token);

            token = _lexer.parseToken();
            if (token != BiancaLexer.Token.SEMICOLUMN) {
               _lexer.saveToken(token);
            }

            return stmt;
      }
   }

   /**
    * Parses statements that expect to be terminated by ';'.
    */
   private Statement parseStatementImpl(BiancaLexer.Token token)
      throws IOException {
      switch (token) {
         case ECHO: {
            Location location = getLocation();

            ArrayList<Statement> statementList = new ArrayList<Statement>();
            parseEcho(statementList);

            return _factory.createBlock(location, statementList);
         }

         case PRINT:
            return parsePrint();

         case UNSET:
            return parseUnset();

         case GLOBAL:
            return parseGlobal();

         case STATIC:
            return parseStatic();

         case BREAK:
            return parseBreak();

         case CONTINUE:
            return parseContinue();

         case RETURN:
            return parseReturn();

         case THROW:
            return parseThrow();

         case TRY:
            return parseTry();

         default:
            _lexer.saveToken(token);
            return parseExprStatement();

         /*
         default:
         throw error(L.l("unexpected token {0}.", tokenName(token)));
          */
      }
   }

   /**
    * Parses the echo statement.
    */
   private void parseEcho(ArrayList<Statement> statements)
      throws IOException {
      Location location = getLocation();

      while (true) {
         Expr expr = parseTopExpr();

         createEchoStatements(location, statements, expr);

         BiancaLexer.Token token = _lexer.parseToken();

         if (token != BiancaLexer.Token.COMMA) {
            _lexer.saveToken(token);
            return;
         }
      }
   }

   /**
    * Creates echo statements from an expression.
    */
   private void createEchoStatements(Location location,
                                     ArrayList<Statement> statements,
                                     Expr expr) {
      if (expr == null) {
         // since AppendExpr.getNext() can be null.
      } else if (expr instanceof BinaryAppendExpr) {
         BinaryAppendExpr append = (BinaryAppendExpr) expr;

         // TODO: children of append print differently?

         createEchoStatements(location, statements, append.getValue());
         createEchoStatements(location, statements, append.getNext());
      } else if (expr instanceof LiteralStringExpr) {
         LiteralStringExpr string = (LiteralStringExpr) expr;

         Statement statement = _factory.createText(location, string.evalConstant().toString());

         statements.add(statement);
      } else {
         Statement statement = _factory.createEcho(location, expr);

         statements.add(statement);
      }
   }

   /**
    * Parses the print statement.
    */
   private Statement parsePrint()
      throws IOException {
      return _factory.createExpr(getLocation(), parsePrintExpr());
   }

   /**
    * Parses the print statement.
    */
   private Expr parsePrintExpr()
      throws IOException {
      ArrayList<Expr> args = new ArrayList<Expr>();
      args.add(parseTopExpr());

      return _factory.createCall(this, "print", args);
   }

   /**
    * Parses the global statement.
    */
   private Statement parseGlobal()
      throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      Location location = getLocation();

      while (true) {
         Expr expr = parseTopExpr();

         if (expr instanceof VarExpr) {
            VarExpr var = (VarExpr) expr;

            _function.setUsesGlobal(true);

            // php/323c
            // php/3a6g, php/3a58
            //var.getVarInfo().setGlobal();

            statementList.add(_factory.createGlobal(location, var));
         } else if (expr instanceof VarVarExpr) {
            VarVarExpr var = (VarVarExpr) expr;

            statementList.add(_factory.createVarGlobal(location, var));
         } else {
            throw error(L.l("unknown expr {0} to global", expr));
         }

         // statementList.add(new ExprStatement(expr));

         BiancaLexer.Token token = _lexer.parseToken();

         if (token != BiancaLexer.Token.COMMA) {
            _lexer.saveToken(token);
            return _factory.createBlock(location, statementList);
         }
      }
   }

   /**
    * Parses the static statement.
    */
   private Statement parseStatic()
      throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      Location location = getLocation();

      while (true) {
         _lexer.expect(BiancaLexer.Token.DOLLAR);

         String name = parseIdentifier();

         VarExpr var = _factory.createVar(_function.createVar(name));

         Expr init = null;

         BiancaLexer.Token token = _lexer.parseToken();

         if (token == BiancaLexer.Token.ASSIGN) {
            init = parseExpr();
            token = _lexer.parseToken();
         }

         // var.getVarInfo().setReference();

         if (_classDef != null) {
            statementList.add(_factory.createClassStatic(location,
               _classDef.getName(),
               var,
               init));
         } else {
            statementList.add(_factory.createStatic(location, var, init));
         }

         if (token != BiancaLexer.Token.COMMA) {
            _lexer.saveToken(token);
            return _factory.createBlock(location, statementList);
         }
      }
   }

   /**
    * Parses the unset statement.
    */
   private Statement parseUnset()
      throws IOException {
      Location location = getLocation();

      ArrayList<Statement> statementList = new ArrayList<Statement>();
      parseUnset(statementList);

      return _factory.createBlock(location, statementList);
   }

   /**
    * Parses the unset statement.
    */
   private void parseUnset(ArrayList<Statement> statementList)
      throws IOException {
      Location location = getLocation();

      BiancaLexer.Token token = _lexer.parseToken();

      if (token != BiancaLexer.Token.LEFT_PAREN) {
         _lexer.saveToken(token);

         statementList.add(parseTopExpr().createUnset(_factory, location));

         return;
      }

      do {
         // TODO: statementList.add(
         //    parseTopExpr().createUnset(_factory, getLocation()));

         Expr topExpr = parseTopExpr();

         statementList.add(topExpr.createUnset(_factory, getLocation()));
      } while ((token = _lexer.parseToken()) == BiancaLexer.Token.COMMA);

      _lexer.saveToken(token);
      _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);
   }

   /**
    * Parses the if statement
    */
   private Statement parseIf()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      try {
         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         BiancaLexer.Token token = _lexer.parseToken();

         if (token == BiancaLexer.Token.COLUMN) {
            return parseAlternateIf(test, location);
         }
         _lexer.saveToken(token);

         Statement trueBlock = parseStatement();

         Statement falseBlock = null;

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.ELSEIF) {
            falseBlock = parseIf();
         } else if (token == BiancaLexer.Token.ELSE) {
            falseBlock = parseStatement();
         } else {
            _lexer.saveToken(token);
         }

         return _factory.createIf(location, test, trueBlock, falseBlock);

      } finally {
         _isTop = oldTop;
      }
   }

   /**
    * Parses the if statement
    */
   private Statement parseAlternateIf(Expr test, Location location)
      throws IOException {
      Statement trueBlock = null;

      trueBlock = _factory.createBlock(location, parseStatementList());

      Statement falseBlock = null;

      BiancaLexer.Token token = _lexer.parseToken();

      if (token == BiancaLexer.Token.ELSEIF) {
         Location subLocation = getLocation();

         Expr subTest = parseExpr();
         _lexer.expect(BiancaLexer.Token.COLUMN);

         falseBlock = parseAlternateIf(subTest, subLocation);
      } else if (token == BiancaLexer.Token.ELSE) {
         _lexer.expect(BiancaLexer.Token.COLUMN);

         falseBlock = _factory.createBlock(getLocation(), parseStatementList());

         _lexer.expect(BiancaLexer.Token.ENDIF);
      } else {
         _lexer.saveToken(token);
         _lexer.expect(BiancaLexer.Token.ENDIF);
      }

      return _factory.createIf(location, test, trueBlock, falseBlock);
   }

   /**
    * Parses the switch statement
    */
   private Statement parseSwitch()
      throws IOException {
      Location location = getLocation();

      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushSwitchLabel();

      try {
         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         Expr test = parseExpr();

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         boolean isAlternate = false;

         BiancaLexer.Token token = _lexer.parseToken();

         if (token == BiancaLexer.Token.COLUMN) {
            isAlternate = true;
         } else if (token == BiancaLexer.Token.LEFT_BRACE) {
            isAlternate = false;
         } else {
            _lexer.saveToken(token);

            _lexer.expect(BiancaLexer.Token.LEFT_BRACE);
         }

         ArrayList<Expr[]> caseList = new ArrayList<Expr[]>();
         ArrayList<BlockStatement> blockList = new ArrayList<BlockStatement>();

         ArrayList<Integer> fallThroughList = new ArrayList<Integer>();
         BlockStatement defaultBlock = null;

         while ((token = _lexer.parseToken()) == BiancaLexer.Token.CASE || token == BiancaLexer.Token.DEFAULT) {
            Location caseLocation = getLocation();

            ArrayList<Expr> valueList = new ArrayList<Expr>();
            boolean isDefault = false;

            while (token == BiancaLexer.Token.CASE || token == BiancaLexer.Token.DEFAULT) {
               if (token == BiancaLexer.Token.CASE) {
                  Expr value = parseExpr();

                  valueList.add(value);
               } else {
                  isDefault = true;
               }

               token = _lexer.parseToken();
               if (token == BiancaLexer.Token.COLUMN) {
               } else if (token == BiancaLexer.Token.SEMICOLUMN) {
                  // TODO: warning?
               } else {
                  throw error("expected ':' at " + _lexer.tokenName(token));
               }

               token = _lexer.parseToken();
            }

            _lexer.saveToken(token);

            Expr[] values = new Expr[valueList.size()];
            valueList.toArray(values);

            ArrayList<Statement> newBlockList = parseStatementList();

            for (int fallThrough : fallThroughList) {
               BlockStatement block = blockList.get(fallThrough);

               boolean isDefaultBlock = block == defaultBlock;

               block = block.append(newBlockList);

               blockList.set(fallThrough, block);

               if (isDefaultBlock) {
                  defaultBlock = block;
               }
            }

            BlockStatement block = _factory.createBlockImpl(caseLocation, newBlockList);

            if (values.length > 0) {
               caseList.add(values);

               blockList.add(block);
            }

            if (isDefault) {
               defaultBlock = block;
            }

            if (blockList.size() > 0
               && !fallThroughList.contains(blockList.size() - 1)) {
               fallThroughList.add(blockList.size() - 1);
            }

            if (block.fallThrough() != Statement.FALL_THROUGH) {
               fallThroughList.clear();
            }
         }

         _lexer.saveToken(token);

         if (isAlternate) {
            _lexer.expect(BiancaLexer.Token.ENDSWITCH);
         } else {
            _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);
         }

         return _factory.createSwitch(location, test,
            caseList, blockList,
            defaultBlock, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'while' statement
    */
   private Statement parseWhile()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushWhileLabel();

      try {
         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         Statement block;

         BiancaLexer.Token token = _lexer.parseToken();

         if (token == BiancaLexer.Token.COLUMN) {
            block = _factory.createBlock(getLocation(), parseStatementList());

            _lexer.expect(BiancaLexer.Token.ENDWHILE);
         } else {
            _lexer.saveToken(token);

            block = parseStatement();
         }

         return _factory.createWhile(location, test, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'do' statement
    */
   private Statement parseDo()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushDoLabel();

      try {
         Location location = getLocation();

         Statement block = parseStatement();

         _lexer.expect(BiancaLexer.Token.WHILE);
         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         return _factory.createDo(location, test, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'for' statement
    */
   private Statement parseFor()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushForLabel();

      try {
         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         Expr init = null;

         BiancaLexer.Token token = _lexer.parseToken();
         if (token != BiancaLexer.Token.SEMICOLUMN) {
            _lexer.saveToken(token);
            init = parseTopCommaExpr();
            _lexer.expect(BiancaLexer.Token.SEMICOLUMN);
         }

         Expr test = null;

         token = _lexer.parseToken();
         if (token != BiancaLexer.Token.SEMICOLUMN) {
            _lexer.saveToken(token);

            _isIfTest = true;
            test = parseTopCommaExpr();
            _isIfTest = false;

            _lexer.expect(BiancaLexer.Token.SEMICOLUMN);
         }

         Expr incr = null;

         token = _lexer.parseToken();
         if (token != BiancaLexer.Token.RIGHT_PAREN) {
            _lexer.saveToken(token);
            incr = parseTopCommaExpr();
            _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);
         }

         Statement block;

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.COLUMN) {
            block = _factory.createBlock(getLocation(), parseStatementList());

            _lexer.expect(BiancaLexer.Token.ENDFOR);
         } else {
            _lexer.saveToken(token);

            block = parseStatement();
         }

         return _factory.createFor(location, init, test, incr, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'foreach' statement
    */
   private Statement parseForeach()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushForeachLabel();

      try {
         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         Expr objExpr = parseTopExpr();

         _lexer.expect(BiancaLexer.Token.AS);

         boolean isRef = false;

         BiancaLexer.Token token = _lexer.parseToken();
         if (token == BiancaLexer.Token.AND) {
            isRef = true;
         } else {
            _lexer.saveToken(token);
         }

         AbstractVarExpr valueExpr = (AbstractVarExpr) parseLeftHandSide();

         AbstractVarExpr keyVar = null;
         AbstractVarExpr valueVar;

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.ARRAY_RIGHT) {
            if (isRef) {
               throw error(L.l("key reference is forbidden in foreach"));
            }

            keyVar = valueExpr;

            token = _lexer.parseToken();

            if (token == BiancaLexer.Token.AND) {
               isRef = true;
            } else {
               _lexer.saveToken(token);
            }

            valueVar = (AbstractVarExpr) parseLeftHandSide();

            token = _lexer.parseToken();
         } else {
            valueVar = valueExpr;
         }

         if (token != BiancaLexer.Token.RIGHT_PAREN) {
            throw error(L.l("expected ')' in foreach"));
         }

         Statement block;

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.COLUMN) {
            block = _factory.createBlock(getLocation(), parseStatementList());

            _lexer.expect(BiancaLexer.Token.ENDFOREACH);
         } else {
            _lexer.saveToken(token);
            block = parseStatement();
         }

         return _factory.createForeach(location, objExpr, keyVar,
            valueVar, isRef, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the try statement
    */
   private Statement parseTry()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      try {
         Location location = getLocation();

         Statement block = null;

         try {
            block = parseStatement();
         } finally {
            //  _scope = oldScope;
         }

         TryStatement stmt = _factory.createTry(location, block);

         BiancaLexer.Token token = _lexer.parseToken();

         while (token == BiancaLexer.Token.CATCH) {
            _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

            String id = _lexer.parseNamespaceIdentifier();

            AbstractVarExpr lhs = parseLeftHandSide();

            _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

            block = parseStatement();

            stmt.addCatch(id, lhs, block);

            token = _lexer.parseToken();
         }

         _lexer.saveToken(token);

         return stmt;
      } finally {
         _isTop = oldTop;
      }
   }

   /**
    * Parses a function definition
    */
   private Function parseFunctionDefinition(int modifiers)
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      boolean oldReturnsReference = _returnsReference;
      FunctionInfo oldFunction = _function;

      boolean isAbstract = (modifiers & M_ABSTRACT) != 0;
      boolean isStatic = (modifiers & M_STATIC) != 0;

      if (_classDef != null && _classDef.isInterface()) {
         isAbstract = true;
      }

      try {
         _returnsReference = false;

         BiancaLexer.Token token = _lexer.parseToken();

         String comment = _lexer.getComment();
         _lexer.dropComment();

         if (token == BiancaLexer.Token.AND) {
            _returnsReference = true;
         } else {
            _lexer.saveToken(token);
         }

         String name;

         name = parseIdentifier();

         if (_classDef == null) {
            name = _lexer.resolveIdentifier(name);
         }

         if (isAbstract && !_scope.isAbstract()) {
            if (_classDef != null) {
               throw error(L.l(
                  "'{0}' may not be abstract because class {1} is not abstract.",
                  name, _classDef.getName()));
            } else {
               throw error(L.l(
                  "'{0}' may not be abstract. Abstract functions are only "
                     + "allowed in abstract classes.",
                  name));
            }
         }

         boolean isConstructor = false;

         if (_classDef != null
            && (name.equals(_classDef.getName())
            || name.equals("__constructor"))) {
            if (isStatic) {
               throw error(L.l(
                  "'{0}:{1}' may not be static because class constructors "
                     + "may not be static",
                  _classDef.getName(), name));
            }

            isConstructor = true;
         }

         _function = getFactory().createFunctionInfo(_bianca, _classDef, name);
         _function.setPageStatic(oldTop);
         _function.setConstructor(isConstructor);

         _function.setReturnsReference(_returnsReference);

         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         Arg[] args = parseFunctionArgDefinition();

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         if (_classDef != null
            && "__call".equals(name)
            && args.length != 2) {
            throw error(L.l("{0}::{1} must have exactly two arguments defined",
               _classDef.getName(), name));
         }

         Function function;

         if (isAbstract) {
            _lexer.expect(BiancaLexer.Token.SEMICOLUMN);

            function = _factory.createMethodDeclaration(location,
               _classDef, name,
               _function, args);
         } else {
            _lexer.expect(BiancaLexer.Token.LEFT_BRACE);

            Statement[] statements = null;

            Scope oldScope = _scope;
            try {
               _scope = new FunctionScope(_factory, oldScope);
               statements = parseStatements();
            } finally {
               _scope = oldScope;
            }

            _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

            if (_classDef != null) {
               function = _factory.createObjectMethod(location,
                  _classDef,
                  name, _function,
                  args, statements);
            } else {
               function = _factory.createFunction(location, name,
                  _function, args,
                  statements);
            }
         }

         function.setGlobal(oldTop);
         function.setStatic((modifiers & M_STATIC) != 0);
         function.setFinal((modifiers & M_FINAL) != 0);

         function.setParseIndex(_functionsParsed++);
         function.setComment(comment);

         if ((modifiers & M_PROTECTED) != 0) {
            function.setVisibility(Visibility.PROTECTED);
         } else if ((modifiers & M_PRIVATE) != 0) {
            function.setVisibility(Visibility.PRIVATE);
         }

         _scope.addFunction(name, function, oldTop);

         /*
         com.clevercloud.vfs.WriteStream out = com.clevercloud.vfs
         .Vfs.lookup("stdout:").openWrite();
         out.setFlushOnNewline(true);
         function.debug(new JavaWriter(out));
          */

         return function;
      } finally {
         _returnsReference = oldReturnsReference;
         _function = oldFunction;
         _isTop = oldTop;
      }
   }

   /**
    * Parses a function definition
    */
   private Expr parseClosure()
      throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      boolean oldReturnsReference = _returnsReference;
      FunctionInfo oldFunction = _function;

      try {
         _returnsReference = false;

         BiancaLexer.Token token = _lexer.parseToken();

         String comment = null;

         if (token == BiancaLexer.Token.AND) {
            _returnsReference = true;
         } else {
            _lexer.saveToken(token);
         }

         String name = "__bianca_closure_" + _functionsParsed;

         ClassDef classDef = null;
         _function = getFactory().createFunctionInfo(_bianca, classDef, name);
         _function.setReturnsReference(_returnsReference);
         _function.setClosure(true);

         Location location = getLocation();

         _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

         Arg[] args = parseFunctionArgDefinition();

         _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

         Arg[] useArgs;
         ArrayList<VarExpr> useVars = new ArrayList<VarExpr>();

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.USE) {
            _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

            useArgs = parseFunctionArgDefinition();

            for (Arg arg : useArgs) {
               VarExpr var = _factory.createVar(
                  oldFunction.createVar(arg.getName()));

               useVars.add(var);
            }

            _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);
         } else {
            _lexer.saveToken(token);
            useArgs = new Arg[0];
         }

         _lexer.expect(BiancaLexer.Token.LEFT_BRACE);

         Statement[] statements = null;

         Scope oldScope = _scope;
         try {
            _scope = new FunctionScope(_factory, oldScope);
            statements = parseStatements();
         } finally {
            _scope = oldScope;
         }

         _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

         Function function = _factory.createFunction(location, name,
            _function, args,
            statements);

         function.setParseIndex(_functionsParsed++);
         function.setComment(comment);
         function.setClosure(true);
         function.setClosureUseArgs(useArgs);

         _globalScope.addFunction(name, function, oldTop);

         return _factory.createClosure(location, function, useVars);
      } finally {
         _returnsReference = oldReturnsReference;
         _function = oldFunction;
         _isTop = oldTop;
      }
   }

   private Arg[] parseFunctionArgDefinition()
      throws IOException {
      LinkedHashMap<String, Arg> argMap = new LinkedHashMap<String, Arg>();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();
         boolean isReference = false;

         // php/076b, php/1c02
         // TODO: save arg type for type checking upon function call
         String expectedClass = null;
         if (token != BiancaLexer.Token.RIGHT_PAREN
            && token != BiancaLexer.Token.AND
            && token != BiancaLexer.Token.DOLLAR
            && token != BiancaLexer.Token.NONE) {
            _lexer.saveToken(token);
            expectedClass = parseIdentifier();
            token = _lexer.parseToken();
         }

         if (token == BiancaLexer.Token.AND) {
            isReference = true;
            token = _lexer.parseToken();
         }

         if (token != BiancaLexer.Token.DOLLAR) {
            _lexer.saveToken(token);
            break;
         }

         String argName = parseIdentifier();
         Expr defaultExpr = _factory.createRequired();

         token = _lexer.parseToken();
         if (token == BiancaLexer.Token.ASSIGN) {
            // TODO: actually needs to be primitive
            defaultExpr = parseUnary(); // parseTerm(false);

            token = _lexer.parseToken();
         }

         Arg arg = new Arg(argName, defaultExpr, isReference, expectedClass);

         if (argMap.get(argName) != null && _bianca.isStrict()) {
            throw error(L.l("aliasing of function argument '{0}'", argName));
         }

         argMap.put(argName, arg);

         VarInfo var = _function.createVar(argName);

         if (token != BiancaLexer.Token.COMMA) {
            _lexer.saveToken(token);
            break;
         }
      }

      Arg[] args = new Arg[argMap.size()];

      argMap.values().toArray(args);

      return args;
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseBreak()
      throws IOException {
      // commented out for adodb (used by Moodle and others)
      // TODO: should only throw fatal error if break statement is reached
      //      during execution

      if (!_isTop && _loopLabelList.isEmpty() && !_bianca.isLooseParse()) {
         throw error(L.l("cannot 'break' inside a function"));
      }

      Location location = getLocation();

      BiancaLexer.Token token = _lexer.nextToken();

      switch (token) {
         case SEMICOLUMN:
            return _factory.createBreak(location,
               null,
               (ArrayList<String>) _loopLabelList.clone());

         default:
            Expr expr = parseTopExpr();

            return _factory.createBreak(location,
               expr,
               (ArrayList<String>) _loopLabelList.clone());
      }
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseContinue()
      throws IOException {
      if (!_isTop && _loopLabelList.isEmpty() && !_bianca.isLooseParse()) {
         throw error(L.l("cannot 'continue' inside a function"));
      }

      Location location = getLocation();

      BiancaLexer.Token token = _lexer.nextToken();

      switch (token) {
         case TEXT_PHP:
         case SEMICOLUMN:
            return _factory.createContinue(location,
               null,
               (ArrayList<String>) _loopLabelList.clone());

         default:
            Expr expr = parseTopExpr();

            return _factory.createContinue(location,
               expr,
               (ArrayList<String>) _loopLabelList.clone());
      }
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseReturn()
      throws IOException {
      Location location = getLocation();

      BiancaLexer.Token token = _lexer.nextToken();

      switch (token) {
         case SEMICOLUMN:
            return _factory.createReturn(location, _factory.createNull());

         default:
            Expr expr = parseTopExpr();

            /*
            if (_returnsReference)
            expr = expr.createRef();
            else
            expr = expr.createCopy();
             */

            if (_returnsReference) {
               return _factory.createReturnRef(location, expr);
            } else {
               return _factory.createReturn(location, expr);
            }
      }
   }

   /**
    * Parses the 'throw' statement
    */
   private Statement parseThrow()
      throws IOException {
      Location location = getLocation();

      Expr expr = parseExpr();

      return _factory.createThrow(location, expr);
   }

   /**
    * Parses a class definition
    */
   private Statement parseClassDefinition(int modifiers)
      throws IOException {
      String name = parseIdentifier();

      name = _lexer.resolveIdentifier(name);

      String comment = _lexer.getComment();

      String parentName = null;

      ArrayList<String> ifaceList = new ArrayList<String>();

      BiancaLexer.Token token = _lexer.parseToken();
      if (token == BiancaLexer.Token.EXTENDS) {
         if ((modifiers & M_INTERFACE) != 0) {
            do {
               ifaceList.add(_lexer.parseNamespaceIdentifier());

               token = _lexer.parseToken();
            } while (token == BiancaLexer.Token.COMMA);
         } else {
            parentName = _lexer.parseNamespaceIdentifier();

            token = _lexer.parseToken();
         }
      }

      if ((modifiers & M_INTERFACE) == 0 && token == BiancaLexer.Token.IMPLEMENTS) {
         do {
            ifaceList.add(_lexer.parseNamespaceIdentifier());

            token = _lexer.parseToken();
         } while (token == BiancaLexer.Token.COMMA);
      }

      _lexer.saveToken(token);

      InterpretedClassDef oldClass = _classDef;
      Scope oldScope = _scope;

      try {
         _classDef = oldScope.addClass(getLocation(),
            name, parentName, ifaceList,
            _classesParsed++,
            _isTop);

         _classDef.setComment(comment);

         if ((modifiers & M_ABSTRACT) != 0) {
            _classDef.setAbstract(true);
         }
         if ((modifiers & M_INTERFACE) != 0) {
            _classDef.setInterface(true);
         }
         if ((modifiers & M_FINAL) != 0) {
            _classDef.setFinal(true);
         }

         _scope = new ClassScope(_classDef);

         _lexer.expect(BiancaLexer.Token.LEFT_BRACE);

         parseClassContents();

         _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

         return _factory.createClassDef(getLocation(), _classDef);
      } finally {
         _classDef = oldClass;
         _scope = oldScope;
      }
   }

   /**
    * Parses a statement list.
    */
   private void parseClassContents()
      throws IOException {
      while (true) {
         _lexer.dropComment();

         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case SEMICOLUMN:
               break;

            case FUNCTION: {
               Function fun = parseFunctionDefinition(0);
               fun.setStatic(false);
               break;
            }

            case CLASS:
               parseClassDefinition(0);
               break;

            /* bianca/0260
            case VAR:
            parseClassVarDefinition(false);
            break;
             */

            case CONST:
               parseClassConstDefinition();
               break;

            case PUBLIC:
            case PRIVATE:
            case PROTECTED:
            case STATIC:
            case FINAL:
            case ABSTRACT: {
               _lexer.saveToken(token);
               int modifiers = parseModifiers();

               BiancaLexer.Token token2 = _lexer.parseToken();

               if (token2 == BiancaLexer.Token.FUNCTION) {
                  Function fun = parseFunctionDefinition(modifiers);
               } else {
                  _lexer.saveToken(token2);

                  parseClassVarDefinition(modifiers);
               }
            }
            break;

            case IDENTIFIER:
               if (_lexer.getLexeme().equals("var")) {
                  parseClassVarDefinition(0);
               } else {
                  _lexer.saveToken(token);
                  return;
               }
               break;

            case NONE:
            case RIGHT_BRACE:
            default:
               _lexer.saveToken(token);
               return;
         }
      }
   }

   /**
    * Parses a function definition
    */
   private void parseClassVarDefinition(int modifiers)
      throws IOException {
      BiancaLexer.Token token;

      do {
         _lexer.expect(BiancaLexer.Token.DOLLAR);

         String comment = _lexer.getComment();

         String name = parseIdentifier();

         token = _lexer.parseToken();

         Expr expr;

         if (token == BiancaLexer.Token.ASSIGN) {
            expr = parseExpr();
         } else {
            _lexer.saveToken(token);
            expr = _factory.createNull();
         }

         StringValue nameV = new StringValue(name);

         if ((modifiers & M_STATIC) != 0) {
            ((ClassScope) _scope).addStaticVar(nameV, expr, comment);
         } else if ((modifiers & M_PRIVATE) != 0) {
            ((ClassScope) _scope).addVar(nameV,
               expr,
               FieldVisibility.PRIVATE,
               comment);
         } else if ((modifiers & M_PROTECTED) != 0) {
            ((ClassScope) _scope).addVar(nameV,
               expr,
               FieldVisibility.PROTECTED,
               comment);
         } else {
            ((ClassScope) _scope).addVar(nameV,
               expr,
               FieldVisibility.PUBLIC,
               comment);
         }

         token = _lexer.parseToken();
      } while (token == BiancaLexer.Token.COMMA);

      _lexer.saveToken(token);
   }

   /**
    * Parses a const definition
    */
   private ArrayList<Statement> parseConstDefinition()
      throws IOException {
      ArrayList<Statement> constList = new ArrayList<Statement>();

      BiancaLexer.Token token;

      do {
         String name = _lexer.parseNamespaceIdentifier();

         _lexer.expect(BiancaLexer.Token.ASSIGN);

         Expr expr = parseExpr();

         ArrayList<Expr> args = new ArrayList<Expr>();
         args.add(_factory.createString(name));
         args.add(expr);

         Expr fun = _factory.createCall(this, "define", args);

         constList.add(_factory.createExpr(getLocation(), fun));
         // _scope.addConstant(name, expr);

         token = _lexer.parseToken();
      } while (token == BiancaLexer.Token.COMMA);

      _lexer.saveToken(token);

      return constList;
   }

   /**
    * Parses a const definition
    */
   private void parseClassConstDefinition()
      throws IOException {
      BiancaLexer.Token token;

      do {
         String name = parseIdentifier();

         _lexer.expect(BiancaLexer.Token.ASSIGN);

         Expr expr = parseExpr();

         ((ClassScope) _scope).addConstant(name, expr);

         token = _lexer.parseToken();
      } while (token == BiancaLexer.Token.COMMA);

      _lexer.saveToken(token);
   }

   private int parseModifiers()
      throws IOException {
      BiancaLexer.Token token;
      int modifiers = 0;

      while (true) {
         token = _lexer.parseToken();

         switch (token) {
            case PUBLIC:
               modifiers |= M_PUBLIC;
               break;

            case PRIVATE:
               modifiers |= M_PRIVATE;
               break;

            case PROTECTED:
               modifiers |= M_PROTECTED;
               break;

            case FINAL:
               modifiers |= M_FINAL;
               break;

            case STATIC:
               modifiers |= M_STATIC;
               break;

            case ABSTRACT:
               modifiers |= M_ABSTRACT;
               break;

            default:
               _lexer.saveToken(token);
               return modifiers;
         }
      }
   }

   private ArrayList<Statement> parseNamespace()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      String var = "";

      if (token == BiancaLexer.Token.IDENTIFIER) {
         var = _lexer.getLexeme();

         token = _lexer.parseToken();
      }

      if (var.startsWith("\\")) {
         var = var.substring(1);
      }

      String oldNamespace = _lexer.getNamespace();

      _lexer.setNamespace(var);

      if (token == BiancaLexer.Token.LEFT_BRACE) {
         ArrayList<Statement> statementList = parseStatementList();

         _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

         _lexer.setNamespace(oldNamespace);

         return statementList;
      } else if (token == BiancaLexer.Token.SEMICOLUMN) {
         return new ArrayList<Statement>();
      } else {
         throw error(L.l("namespace must be followed by '{' or ';'"));
      }
   }

   private void parseUse()
      throws IOException {
      _lexer.parseNamespaceIdentifier(); /* TODO: why is this unused ? */

      String name = _lexer.getLexeme();

      int ns = name.lastIndexOf('\\');

      String tail;
      if (ns >= 0) {
         tail = name.substring(ns + 1);
      } else {
         tail = name;
      }

      if (name.startsWith("\\")) {
         name = name.substring(1);
      }

      BiancaLexer.Token token = _lexer.parseToken();

      if (token == BiancaLexer.Token.SEMICOLUMN) {
         _lexer.putNamespace(tail, name);
         return;
      } else if (token == BiancaLexer.Token.AS) {
         do {
            tail = parseIdentifier();

            _lexer.putNamespace(tail, name);
         } while ((token = _lexer.parseToken()) == BiancaLexer.Token.COMMA);
      }

      _lexer.saveToken(token);

      _lexer.expect(BiancaLexer.Token.SEMICOLUMN);
   }

   /**
    * Parses an expression statement.
    */
   private Statement parseExprStatement()
      throws IOException {
      Location location = getLocation();

      Expr expr = parseTopExpr();

      Statement statement = _factory.createExpr(location, expr);

      BiancaLexer.Token token = _lexer.nextToken();

      switch (token) {
         case NONE:
         case SEMICOLUMN:
         case RIGHT_BRACE:
         case PHP_END:
         case TEXT:
         case TEXT_PHP:
         case TEXT_ECHO:
            break;

         default:
            _lexer.expect(BiancaLexer.Token.SEMICOLUMN);
            break;
      }

      return statement;
   }

   /**
    * Parses a top expression.
    */
   private Expr parseTopExpr()
      throws IOException {
      return parseExpr();
   }

   /**
    * Parses a top expression.
    */
   private Expr parseTopCommaExpr()
      throws IOException {
      return parseCommaExpr();
   }

   /**
    * Parses a comma expression.
    */
   private Expr parseCommaExpr()
      throws IOException {
      Expr expr = parseExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case COMMA:
               expr = _factory.createComma(expr, parseExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses an expression with optional '&'.
    */
   private Expr parseRefExpr()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      boolean isRef = token == BiancaLexer.Token.AND;

      if (!isRef) {
         _lexer.saveToken(token);
      }

      Expr expr = parseExpr();

      if (isRef) {
         expr = _factory.createRef(expr);
      }

      return expr;
   }

   /**
    * Parses an expression.
    */
   public Expr parseExpr()
      throws IOException {
      return parseWeakOrExpr();
   }

   /**
    * Parses a logical xor expression.
    */
   private Expr parseWeakOrExpr()
      throws IOException {
      Expr expr = parseWeakXorExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case OR_RES:
               expr = _factory.createOr(expr, parseWeakXorExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a logical xor expression.
    */
   private Expr parseWeakXorExpr()
      throws IOException {
      Expr expr = parseWeakAndExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case XOR_RES:
               expr = _factory.createXor(expr, parseWeakAndExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a logical and expression.
    */
   private Expr parseWeakAndExpr()
      throws IOException {
      Expr expr = parseConditionalExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case AND_RES:
               expr = _factory.createAnd(expr, parseConditionalExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a conditional expression.
    */
   private Expr parseConditionalExpr()
      throws IOException {
      Expr expr = parseOrExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case QUESTION:
               token = _lexer.parseToken();

               if (token == BiancaLexer.Token.COLUMN) {
                  expr = _factory.createShortConditional(expr, parseOrExpr());
               } else {
                  _lexer.saveToken(token);
                  Expr trueExpr = parseExpr();
                  _lexer.expect(BiancaLexer.Token.COLUMN);
                  // php/33c1
                  expr = _factory.createConditional(expr, trueExpr, parseOrExpr());
               }
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a logical or expression.
    */
   private Expr parseOrExpr()
      throws IOException {
      Expr expr = parseAndExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case C_OR:
               expr = _factory.createOr(expr, parseAndExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a logical and expression.
    */
   private Expr parseAndExpr()
      throws IOException {
      Expr expr = parseBitOrExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case C_AND:
               expr = _factory.createAnd(expr, parseBitOrExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a bit or expression.
    */
   private Expr parseBitOrExpr()
      throws IOException {
      Expr expr = parseBitXorExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case OR:
               expr = _factory.createBitOr(expr, parseBitXorExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a bit xor expression.
    */
   private Expr parseBitXorExpr()
      throws IOException {
      Expr expr = parseBitAndExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case XOR:
               expr = _factory.createBitXor(expr, parseBitAndExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a bit and expression.
    */
   private Expr parseBitAndExpr()
      throws IOException {
      Expr expr = parseEqExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case AND:
               expr = _factory.createBitAnd(expr, parseEqExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a comparison expression.
    */
   private Expr parseEqExpr()
      throws IOException {
      Expr expr = parseCmpExpr();

      BiancaLexer.Token token = _lexer.parseToken();

      switch (token) {
         case EQ:
            return _factory.createEq(expr, parseCmpExpr());

         case NEQ:
            return _factory.createNeq(expr, parseCmpExpr());

         case EQUALS:
            return _factory.createEquals(expr, parseCmpExpr());

         case NEQUALS:
            return _factory.createNot(_factory.createEquals(expr, parseCmpExpr()));

         default:
            _lexer.saveToken(token);
            return expr;
      }
   }

   /**
    * Parses a comparison expression.
    */
   private Expr parseCmpExpr()
      throws IOException {
      Expr expr = parseShiftExpr();

      BiancaLexer.Token token = _lexer.parseToken();

      switch (token) {
         case LOWER:
            return _factory.createLt(expr, parseShiftExpr());

         case GREATER:
            return _factory.createGt(expr, parseShiftExpr());

         case LEQ:
            return _factory.createLeq(expr, parseShiftExpr());

         case GEQ:
            return _factory.createGeq(expr, parseShiftExpr());

         case INSTANCEOF:
            Location location = getLocation();

            Expr classNameExpr = parseShiftExpr();

            if (classNameExpr instanceof ConstExpr) {
               return _factory.createInstanceOf(expr, classNameExpr.toString());
            } else {
               return _factory.createInstanceOfVar(expr, classNameExpr);
            }

         default:
            _lexer.saveToken(token);
            return expr;
      }
   }

   /**
    * Parses a left/right shift expression.
    */
   private Expr parseShiftExpr()
      throws IOException {
      Expr expr = parseAddExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case LSHIFT:
               expr = _factory.createLeftShift(expr, parseAddExpr());
               break;
            case RSHIFT:
               expr = _factory.createRightShift(expr, parseAddExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses an add/substract expression.
    */
   private Expr parseAddExpr()
      throws IOException {
      Expr expr = parseMulExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case PLUS:
               expr = _factory.createAdd(expr, parseMulExpr());
               break;
            case MINUS:
               expr = _factory.createSub(expr, parseMulExpr());
               break;
            case DOT:
               expr = _factory.createAppend(expr, parseMulExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses a multiplication/division expression.
    */
   private Expr parseMulExpr()
      throws IOException {
      Expr expr = parseAssignExpr();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case MUL:
               expr = _factory.createMul(expr, parseAssignExpr());
               break;
            case DIV:
               expr = _factory.createDiv(getLocation(), expr, parseAssignExpr());
               break;
            case MOD:
               expr = _factory.createMod(expr, parseAssignExpr());
               break;
            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses an assignment expression.
    */
   private Expr parseAssignExpr()
      throws IOException {
      Expr expr = parseUnary();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case ASSIGN:
               token = _lexer.parseToken();

               try {
                  if (token == BiancaLexer.Token.AND) {
                     // php/03d6
                     expr = expr.createAssignRef(this, parseBitOrExpr());
                  } else {
                     _lexer.saveToken(token);

                     if (_isIfTest && _bianca.isStrict()) {
                        throw error(
                           "assignment without parentheses inside If/While/For "
                              + "test statement; please make sure whether equality "
                              + "was intended instead");
                     }

                     expr = expr.createAssign(this, parseConditionalExpr());
                  }
               } catch (BiancaParseException e) {
                  throw e;
               } catch (IOException e) {
                  throw error(e.getMessage());
               }
               break;

            case PLUS_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createAdd(expr,
                        parseConditionalExpr()));
               } else // php/03d4
               {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case MINUS_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createSub(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case APPEND_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createAppend(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case MUL_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createMul(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case DIV_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createDiv(getLocation(), expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case MOD_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createMod(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case LSHIFT_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createLeftShift(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case RSHIFT_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createRightShift(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case AND_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createBitAnd(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case OR_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createBitOr(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case XOR_ASSIGN:
               if (expr.canRead()) {
                  expr = expr.createAssign(this,
                     _factory.createBitXor(expr,
                        parseConditionalExpr()));
               } else {
                  expr = expr.createAssign(this, parseConditionalExpr());
               }
               break;

            case INSTANCEOF:
               Expr classNameExpr = parseShiftExpr();

               if (classNameExpr instanceof ConstExpr) {
                  return _factory.createInstanceOf(expr, classNameExpr.toString());
               } else {
                  return _factory.createInstanceOfVar(expr, classNameExpr);
               }

            default:
               _lexer.saveToken(token);
               return expr;
         }
      }
   }

   /**
    * Parses unary term.
    * <p/>
    * <pre>
    * unary ::= term
    *       ::= '&' unary
    *       ::= '-' unary
    *       ::= '+' unary
    *       ::= '!' unary
    *       ::= '~' unary
    *       ::= '@' unary
    * </pre>
    */
   private Expr parseUnary()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      switch (token) {

         case PLUS: {
            Expr expr = parseAssignExpr();

            return _factory.createPlus(expr);
         }

         case MINUS: {
            Expr expr = parseAssignExpr();

            return _factory.createMinus(expr);
         }

         case NOT: {
            Expr expr = parseAssignExpr();

            return _factory.createNot(expr);
         }

         case TILD: {
            Expr expr = parseAssignExpr();

            return _factory.createBitNot(expr);
         }

         case AROBAS: {
            Expr expr = parseAssignExpr();

            return _factory.createSuppress(expr);
         }

         case CLONE: {
            Expr expr = parseAssignExpr();

            return _factory.createClone(expr);
         }

         case INCR: {
            Expr expr = parseUnary();

            return _factory.createPreIncrement(expr, 1);
         }

         case DECR: {
            Expr expr = parseUnary();

            return _factory.createPreIncrement(expr, -1);
         }

         default:
            _lexer.saveToken(token);

            return parseTerm(true);
      }
   }

   /**
    * Parses a basic term.
    * <p/>
    * <pre>
    * term ::= termBase
    *      ::= term '[' index ']'
    *      ::= term '{' index '}'
    *      ::= term '->' name
    *      ::= term '::' name
    *      ::= term '(' a1, ..., an ')'
    * </pre>
    */
   private Expr parseTerm(boolean isParseCall)
      throws IOException {
      Expr term = parseTermBase();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case LEFT_BRACKET: {
               token = _lexer.parseToken();

               if (token == BiancaLexer.Token.RIGHT_BRACKET) {
                  term = _factory.createArrayTail(getLocation(), term);
               } else {
                  _lexer.saveToken(token);
                  Expr index = parseExpr();
                  token = _lexer.parseToken();

                  term = _factory.createArrayGet(getLocation(), term, index);
               }

               if (token != BiancaLexer.Token.RIGHT_BRACKET) {
                  throw expect("']'", token);
               }
            }
            break;

            case LEFT_BRACE: {
               Expr index = parseExpr();

               _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

               term = _factory.createCharAt(term, index);
            }
            break;

            case INCR:
               term = _factory.createPostIncrement(term, 1);
               break;

            case DECR:
               term = _factory.createPostIncrement(term, -1);
               break;

            case DEREF:
               term = parseDeref(term);
               break;

            case SCOPE:
               term = parseScope(term);
               break;


            case LEFT_PAREN:
               _lexer.saveRead('(');

               if (isParseCall) {
                  term = parseCall(term);
               } else {
                  return term;
               }
               break;

            default:
               _lexer.saveToken(token);
               return term;
         }
      }
   }

   /**
    * Parses a basic term.
    * <p/>
    * <pre>
    * term ::= termBase
    *      ::= term '[' index ']'
    *      ::= term '{' index '}'
    * </pre>
    */
   private Expr parseTermArray()
      throws IOException {
      Expr term = parseTermBase();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         switch (token) {
            case LEFT_BRACKET: {
               token = _lexer.parseToken();

               if (token == BiancaLexer.Token.RIGHT_BRACKET) {
                  term = _factory.createArrayTail(getLocation(), term);
               } else {
                  _lexer.saveToken(token);
                  Expr index = parseExpr();
                  token = _lexer.parseToken();

                  term = _factory.createArrayGet(getLocation(), term, index);
               }

               if (token != BiancaLexer.Token.RIGHT_BRACKET) {
                  throw expect("']'", token);
               }
            }
            break;

            case LEFT_BRACE: {
               Expr index = parseExpr();

               _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

               term = _factory.createCharAt(term, index);
            }
            break;

            case INCR:
               term = _factory.createPostIncrement(term, 1);
               break;

            case DECR:
               term = _factory.createPostIncrement(term, -1);
               break;

            default:
               _lexer.saveToken(token);
               return term;
         }
      }
   }

   /**
    * Parses a deref
    * <p/>
    * <pre>
    * deref ::= term -> IDENTIFIER
    *       ::= term -> IDENTIFIER '(' args ')'
    * </pre>
    */
   private Expr parseDeref(Expr term)
      throws IOException {
      String name;
      Expr nameExpr;

      BiancaLexer.Token token = _lexer.parseToken();

      if (token == BiancaLexer.Token.DOLLAR) {
         _lexer.saveToken(token);
         // php/09e0
         nameExpr = parseTerm(false);

         return term.createFieldGet(_factory, nameExpr);
      } else if (token == BiancaLexer.Token.LEFT_BRACE) {
         nameExpr = parseExpr();
         _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

         return term.createFieldGet(_factory, nameExpr);
      } else {
         _lexer.saveToken(token);
         name = parseIdentifier();

         return term.createFieldGet(_factory, new StringValue(name));
      }
   }

   /**
    * Parses the next string
    */
   private Expr parseEscapedString(String prefix,
                                   BiancaLexer.Token token,
                                   boolean isSystem)
      throws IOException {
      /* TODO: split me in lexer ? */
      Expr expr = createString(prefix);
      StringBuilder sb = new StringBuilder();

      while (true) {
         Expr tail;

         if (token == BiancaLexer.Token.COMPLEX_STRING_ESCAPE
            || token == BiancaLexer.Token.COMPLEX_BINARY_ESCAPE) {
            tail = parseExpr();

            _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);
         } else if (token == BiancaLexer.Token.SIMPLE_STRING_ESCAPE
            || token == BiancaLexer.Token.SIMPLE_BINARY_ESCAPE) {
            int ch = _lexer.read();

            sb = new StringBuilder();

            for (; _lexer.isIdentifierPart(ch); ch = _lexer.read()) {
               sb.append((char) ch);
            }

            _lexer.saveRead(ch);

            String varName = sb.toString();
            sb = new StringBuilder();

            if (varName.equals("this")) {
               tail = _factory.createThis(_classDef);
            } else {
               tail = _factory.createVar(_function.createVar(varName));
            }

            // php/013n
            if (((ch = _lexer.read()) == '[' || ch == '-')) {
               if (ch == '[') {
                  tail = parseSimpleArrayTail(tail);

                  ch = _lexer.read();
               } else {
                  if ((ch = _lexer.read()) != '>') {
                     tail = _factory.createAppend(tail, createString("-"));
                  } else if (_lexer.isIdentifierPart(ch = _lexer.read())) {
                     for (; _lexer.isIdentifierPart(ch); ch = _lexer.read()) {
                        sb.append((char) ch);
                     }

                     tail = tail.createFieldGet(_factory,
                        new StringValue(sb.toString()));
                  } else {
                     tail = _factory.createAppend(tail, createString("->"));
                  }

                  _lexer.saveRead(ch);
               }
            }

            _lexer.saveRead(ch);
         } else {
            throw error("unexpected token");
         }

         expr = _factory.createAppend(expr, tail);

         if (isSystem) {
            token = _lexer.parseEscapedString('`');
         } else {
            token = _lexer.parseEscapedString('"');
         }

         if (sb.length() > 0) {
            Expr string;

            string = createString(sb.toString());

            expr = _factory.createAppend(expr, string);
         }

         expr = _factory.createAppend(expr, createString(_lexer.getLexeme()));

         if (token == BiancaLexer.Token.STRING) {
            return expr;
         }
      }
   }

   /**
    * Parses a basic term.
    * <p/>
    * <pre>
    * term ::= STRING
    *      ::= LONG
    *      ::= DOUBLE
    * </pre>
    */
   private Expr parseTermBase()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      switch (token) {
         case STRING:
            return createString(_lexer.getLexeme());

         case SYSTEM_STRING: {
            ArrayList<Expr> args = new ArrayList<Expr>();
            args.add(createString(_lexer.getLexeme()));
            return _factory.createCall(this, "shell_exec", args);
         }

         case SIMPLE_SYSTEM_STRING: {
            ArrayList<Expr> args = new ArrayList<Expr>();
            args.add(parseEscapedString(_lexer.getLexeme(), BiancaLexer.Token.SIMPLE_STRING_ESCAPE, true));
            return _factory.createCall(this, "shell_exec", args);
         }

         case COMPLEX_SYSTEM_STRING: {
            ArrayList<Expr> args = new ArrayList<Expr>();
            args.add(parseEscapedString(_lexer.getLexeme(), BiancaLexer.Token.COMPLEX_STRING_ESCAPE, true));
            return _factory.createCall(this, "shell_exec", args);
         }

         case SIMPLE_STRING_ESCAPE:
         case COMPLEX_STRING_ESCAPE:
            return parseEscapedString(_lexer.getLexeme(), token, false);

         case BINARY:
            try {
               return createString(_lexer.getLexeme());
            } catch (Exception e) {
               throw new BiancaParseException(e);
            }

         case SIMPLE_BINARY_ESCAPE:
         case COMPLEX_BINARY_ESCAPE:
            return parseEscapedString(_lexer.getLexeme(), token, false);

         case LONG: {
            long value = 0;
            double doubleValue = 0;
            long sign = 1;
            boolean isOverflow = false;

            char ch = _lexer.getLexeme().charAt(0);

            int i = 0;
            if (ch == '+') {
               i++;
            } else if (ch == '-') {
               sign = -1;
               i++;
            }

            int len = _lexer.getLexeme().length();
            for (; i < len; i++) {
               int digit = _lexer.getLexeme().charAt(i) - '0';
               long oldValue = value;

               value = value * 10 + digit;
               doubleValue = doubleValue * 10 + digit;

               if (value < oldValue) {
                  isOverflow = true;
               }
            }

            if (!isOverflow) {
               return _factory.createLiteral(LongValue.create(value * sign));
            } else {
               return _factory.createLiteral(new DoubleValue(doubleValue * sign));
            }
         }
         case DOUBLE:
            return _factory.createLiteral(
               new DoubleValue(Double.parseDouble(_lexer.getLexeme())));

         case NULL:
            return _factory.createNull();

         case TRUE:
            return _factory.createLiteral(BooleanValue.TRUE);

         case FALSE:
            return _factory.createLiteral(BooleanValue.FALSE);

         case DOLLAR:
            return parseVariable();

         case NEW:
            return parseNew();

         case FUNCTION:
            return parseClosure();

         case INCLUDE:
            return _factory.createInclude(getLocation(), _sourceFile, parseExpr());
         case REQUIRE:
            return _factory.createRequire(getLocation(), _sourceFile, parseExpr());
         case INCLUDE_ONCE:
            return _factory.createIncludeOnce(getLocation(),
               _sourceFile, parseExpr());
         case REQUIRE_ONCE:
            return _factory.createRequireOnce(getLocation(),
               _sourceFile, parseExpr());

         case LIST:
            return parseList();

         case PRINT:
            return parsePrintExpr();

         case EXIT:
            return parseExit();

         case DIE:
            return parseDie();

         case IDENTIFIER:
         case NAMESPACE: {
            if ("new".equals(_lexer.getLexeme())) {
               return parseNew();
            }

            String name = _lexer.getLexeme();

            token = _lexer.nextToken();

            if (token == BiancaLexer.Token.LEFT_PAREN && !_isNewExpr) {
               // shortcut for common case of static function

               return parseCall(name);
            } else {
               return parseConstant(name);
            }
         }

         case LEFT_PAREN: {
            _isIfTest = false;

            Expr expr = parseExpr();

            _lexer.expect(BiancaLexer.Token.RIGHT_PAREN);

            if (expr instanceof ConstExpr) {
               String type = ((ConstExpr) expr).getVar();

               int ns = type.lastIndexOf('\\');
               if (ns >= 0) {
                  type = type.substring(ns + 1);
               }

               if ("bool".equalsIgnoreCase(type)
                  || "boolean".equalsIgnoreCase(type)) {
                  return _factory.createToBoolean(parseAssignExpr());
               } else if ("int".equalsIgnoreCase(type)
                  || "integer".equalsIgnoreCase(type)) {
                  return _factory.createToLong(parseAssignExpr());
               } else if ("float".equalsIgnoreCase(type)
                  || "double".equalsIgnoreCase(type)
                  || "real".equalsIgnoreCase(type)) {
                  return _factory.createToDouble(parseAssignExpr());
               } else if ("string".equalsIgnoreCase(type)
                  || "binary".equalsIgnoreCase(type)
                  || "unicode".equalsIgnoreCase(type)) {
                  return _factory.createToString(parseAssignExpr());
               } else if ("object".equalsIgnoreCase(type)) {
                  return _factory.createToObject(parseAssignExpr());
               } else if ("array".equalsIgnoreCase(type)) {
                  return _factory.createToArray(parseAssignExpr());
               }
            }

            return expr;
         }

         case IMPORT: {
            String importTokenString = _lexer.getLexeme();

            token = _lexer.parseToken();

            if (token == BiancaLexer.Token.LEFT_PAREN) {
               _lexer.saveToken(token);
               return parseCall(importTokenString);
            } else {
               return parseImport();
            }
         }

         default:
            throw error(L.l("{0} is an unexpected token, expected an expression.",
               _lexer.tokenName(token)));
      }
   }

   /**
    * Parses a basic term.
    * <p/>
    * <pre>
    * lhs ::= VARIABLE
    *     ::= lhs '[' expr ']'
    *     ::= lhs -> FIELD
    * </pre>
    */
   private AbstractVarExpr parseLeftHandSide()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();
      AbstractVarExpr lhs = null;

      if (token == BiancaLexer.Token.DOLLAR) {
         lhs = parseVariable();
      } else {
         throw error(L.l("expected variable at {0} as left-hand-side",
            _lexer.tokenName(token)));
      }

      while (true) {
         token = _lexer.parseToken();

         switch (token) {
            case LEFT_BRACKET: {
               token = _lexer.parseToken();

               if (token == BiancaLexer.Token.RIGHT_BRACKET) {
                  lhs = _factory.createArrayTail(getLocation(), lhs);
               } else {
                  _lexer.saveToken(token);
                  Expr index = parseExpr();
                  token = _lexer.parseToken();

                  lhs = _factory.createArrayGet(getLocation(), lhs, index);
               }

               if (token != BiancaLexer.Token.RIGHT_BRACKET) {
                  throw expect("']'", token);
               }
            }
            break;

            case LEFT_BRACE: {
               Expr index = parseExpr();

               _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

               lhs = _factory.createCharAt(lhs, index);
            }
            break;

            case DEREF:
               lhs = (AbstractVarExpr) parseDeref(lhs);
               break;

            default:
               _lexer.saveToken(token);
               return lhs;
         }
      }
   }

   private Expr parseScope(Expr classNameExpr)
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      if (isIdentifier(token)) {
         return classNameExpr.createClassConst(this, _lexer.getLexeme());
      } else if (token == BiancaLexer.Token.DOLLAR) {
         token = _lexer.parseToken();

         if (isIdentifier(token)) {
            return classNameExpr.createClassField(this, _lexer.getLexeme());
         } else if (token == BiancaLexer.Token.LEFT_BRACE) {
            Expr expr = parseExpr();

            _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

            return classNameExpr.createClassField(this, expr);
         } else {
            _lexer.saveToken(token);

            return classNameExpr.createClassField(this, parseTermBase());
         }
      }

      throw error(L.l("unexpected token '{0}' in class scope expression",
         _lexer.tokenName(token)));
   }

   private boolean isIdentifier(BiancaLexer.Token token) {
      return token == BiancaLexer.Token.IDENTIFIER || token.isIdentifierLexeme();
   }

   /**
    * Parses the next variable
    */
   private AbstractVarExpr parseVariable()
      throws IOException {
      BiancaLexer.Token token = _lexer.parseToken();

      if (token == BiancaLexer.Token.THIS) {
         return _factory.createThis(_classDef);
      } else if (token == BiancaLexer.Token.DOLLAR) {
         _lexer.saveToken(token);

         // php/0d6c, php/0d6f
         return _factory.createVarVar(parseTermArray());
      } else if (token == BiancaLexer.Token.LEFT_BRACE) {
         AbstractVarExpr expr = _factory.createVarVar(parseExpr());

         _lexer.expect(BiancaLexer.Token.RIGHT_BRACE);

         return expr;
      } else if (_lexer.getLexeme() == null) {
         throw error(L.l("Expected identifier at '{0}'", _lexer.tokenName(token)));
      }

      if (_lexer.getLexeme().indexOf('\\') >= 0) {
         throw error(L.l("Namespace is not allowed for variable ${0}", _lexer.getLexeme()));
      }

      return _factory.createVar(_function.createVar(_lexer.getLexeme()));
   }

   public Expr createVar(String name) {
      return _factory.createVar(_function.createVar(name));
   }

   /**
    * Parses the next function
    */
   private Expr parseCall(String name)
      throws IOException {
      if (name.equalsIgnoreCase("array")) {
         return parseArrayFunction();
      }

      ArrayList<Expr> args = parseArgs();

      name = _lexer.resolveIdentifier(name);

      return _factory.createCall(this, name, args);

      /*
      if (name.equals("each")) {
      if (args.size() != 1)
      throw error(L.l("each requires a single expression"));

      // php/1721
      // we should let ArrayModule.each() handle it
      //return _factory.createEach(args.get(0));
      }
       */
   }

   /**
    * Parses the next constant
    */
   private Expr parseConstant(String name) {
      if (name.equals("__FILE__")) {
         return _factory.createFileNameExpr(_parserLocation.getFileName());
      } else if (name.equals("__DIR__")) {
         Path parent = Vfs.lookup(_parserLocation.getFileName()).getParent();

         return _factory.createDirExpr(parent.getNativePath());
      } else if (name.equals("__LINE__")) {
         return _factory.createLong(_parserLocation.getLineNumber());
      } else if (name.equals("__CLASS__") && _classDef != null) {
         return createString(_classDef.getName());
      } else if (name.equals("__FUNCTION__")) {
         return createString(_function.getName());
      } else if (name.equals("__METHOD__")) {
         if (_classDef != null) {
            if (_function.getName().length() != 0) {
               return createString(_classDef.getName() + "::" + _function.getName());
            } else {
               return createString(_classDef.getName());
            }
         } else {
            return createString(_function.getName());
         }
      } else if (name.equals("__NAMESPACE__")) {
         return createString(_lexer.getNamespace());
      }

      name = _lexer.resolveIdentifier(name);

      if (name.startsWith("\\")) {
         name = name.substring(1);
      }

      return _factory.createConst(name);
   }

   /**
    * Parses the next function
    */
   private Expr parseCall(Expr name)
      throws IOException {
      return name.createCall(this, getLocation(), parseArgs());
   }

   private ArrayList<Expr> parseArgs()
      throws IOException {
      _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

      ArrayList<Expr> args = new ArrayList<Expr>();

      BiancaLexer.Token token;

      while ((token = _lexer.parseToken()) != BiancaLexer.Token.NONE && token != BiancaLexer.Token.RIGHT_PAREN) {
         boolean isRef = false;

         if (token == BiancaLexer.Token.AND) {
            isRef = true;
         } else {
            _lexer.saveToken(token);
         }

         Expr expr = parseExpr();

         if (isRef) {
            expr = expr.createRef(this);
         }

         args.add(expr);

         token = _lexer.parseToken();
         if (token == BiancaLexer.Token.RIGHT_PAREN) {
            break;
         } else if (token != BiancaLexer.Token.COMMA) {
            throw expect("','", token);
         }
      }

      return args;
   }

   public String getSelfClassName() {
      if (_classDef == null) {
         throw error(L.l("'self' is not valid because there is no active class."));
      }

      return _classDef.getName();
   }

   public String getParentClassName() {
      if (_classDef == null) {
         throw error(L.l(
            "'parent' is not valid because there is no active class."));
      }

      return _classDef.getParentName();
   }

   /**
    * Parses the new expression
    */
   private Expr parseNew()
      throws IOException {
      String name = null;
      Expr nameExpr = null;

      boolean isNewExpr = _isNewExpr;
      _isNewExpr = true;

      //nameExpr = parseTermBase();
      nameExpr = parseTerm(false);

      _isNewExpr = isNewExpr;

      // XX: unicode issues?
      if (nameExpr.isLiteral() || nameExpr instanceof ConstExpr) {
         name = nameExpr.evalConstant().toString();

         // php/0957
         if ("self".equals(name) && _classDef != null) {
            name = _classDef.getName();
         } else if ("parent".equals(name) && getParentClassName() != null) {
            name = getParentClassName().toString();
         } else {
            // name = resolveIdentifier(name);
         }
      }

      BiancaLexer.Token token = _lexer.parseToken();

      ArrayList<Expr> args = new ArrayList<Expr>();

      if (token != BiancaLexer.Token.LEFT_PAREN) {
         _lexer.saveToken(token);
      } else {
         while ((token = _lexer.parseToken()) != BiancaLexer.Token.NONE && token != BiancaLexer.Token.RIGHT_PAREN) {
            _lexer.saveToken(token);

            args.add(parseExpr());

            token = _lexer.parseToken();
            if (token == BiancaLexer.Token.RIGHT_PAREN) {
               break;
            } else if (token != BiancaLexer.Token.COMMA) {
               throw error(L.l("expected ','"));
            }
         }
      }

      Expr expr;

      if (name != null) {
         expr = _factory.createNew(getLocation(), name, args);
      } else {
         expr = _factory.createVarNew(getLocation(), nameExpr, args);
      }

      return expr;
   }

   /**
    * Parses the include expression
    */
   private Expr parseInclude()
      throws IOException {
      Expr name = parseExpr();

      return _factory.createInclude(getLocation(), _sourceFile, name);
   }

   /**
    * Parses the list(...) = value expression
    */
   private Expr parseList()
      throws IOException {
      ListHeadExpr leftVars = parseListHead();

      _lexer.expect(BiancaLexer.Token.ASSIGN);

      Expr value = parseConditionalExpr();

      return _factory.createList(this, leftVars, value);
   }

   /**
    * Parses the list(...) expression
    */
   private ListHeadExpr parseListHead()
      throws IOException {
      _lexer.expect(BiancaLexer.Token.LEFT_PAREN);

      BiancaLexer.Token token = _lexer.parseToken();

      ArrayList<Expr> leftVars = new ArrayList<Expr>();

      while (token != BiancaLexer.Token.NONE && token != BiancaLexer.Token.RIGHT_PAREN) {
         if (token == BiancaLexer.Token.LIST) {
            leftVars.add(parseListHead());

            token = _lexer.parseToken();
         } else if (token != BiancaLexer.Token.COMMA) {
            _lexer.saveToken(token);

            Expr left = parseTerm(true);

            leftVars.add(left);

            left.assign(this);

            token = _lexer.parseToken();
         } else {
            leftVars.add(null);
         }

         if (token == BiancaLexer.Token.COMMA) {
            token = _lexer.parseToken();
         } else {
            break;
         }
      }

      if (token != BiancaLexer.Token.RIGHT_PAREN) {
         throw error(L.l("expected ')'"));
      }

      return _factory.createListHead(leftVars);
   }

   /**
    * Parses the exit/die expression
    */
   private Expr parseExit()
      throws IOException {
      BiancaLexer.Token token = _lexer.nextToken();

      if (token == BiancaLexer.Token.LEFT_PAREN) {
         ArrayList<Expr> args = parseArgs();

         if (args.size() > 0) {
            return _factory.createExit(args.get(0));
         } else {
            return _factory.createExit(null);
         }
      } else {
         return _factory.createExit(null);
      }
   }

   /**
    * Parses the exit/die expression
    */
   private Expr parseDie()
      throws IOException {
      BiancaLexer.Token token = _lexer.nextToken();

      if (token == BiancaLexer.Token.LEFT_PAREN) {
         ArrayList<Expr> args = parseArgs();

         if (args.size() > 0) {
            return _factory.createDie(args.get(0));
         } else {
            return _factory.createDie(null);
         }
      } else {
         return _factory.createDie(null);
      }
   }

   /**
    * Parses the array() expression
    */
   private Expr parseArrayFunction()
      throws IOException {
      String name = _lexer.getLexeme();

      BiancaLexer.Token token = _lexer.parseToken();

      if (token != BiancaLexer.Token.LEFT_PAREN) {
         throw error(L.l("Expected '('"));
      }

      ArrayList<Expr> keys = new ArrayList<Expr>();
      ArrayList<Expr> values = new ArrayList<Expr>();

      while ((token = _lexer.parseToken()) != BiancaLexer.Token.FIRST_IDENTIFIER_LEXEME && token != BiancaLexer.Token.RIGHT_PAREN) {
         _lexer.saveToken(token);

         Expr value = parseRefExpr();

         token = _lexer.parseToken();

         if (token == BiancaLexer.Token.ARRAY_RIGHT) {
            Expr key = value;

            value = parseRefExpr();

            keys.add(key);
            values.add(value);

            token = _lexer.parseToken();
         } else {
            keys.add(null);
            values.add(value);
         }

         if (token == BiancaLexer.Token.RIGHT_PAREN) {
            break;
         } else if (token != BiancaLexer.Token.COMMA) {
            throw error(L.l("expected ','"));
         }
      }

      return _factory.createArrayFun(keys, values);
   }

   /**
    * Parses a Bianca import.
    */
   private Expr parseImport()
      throws IOException {
      boolean isWildcard = false;
      boolean isIdentifierStart = true;

      StringBuilder sb = new StringBuilder();

      while (true) {
         BiancaLexer.Token token = _lexer.parseToken();

         if (token == BiancaLexer.Token.IDENTIFIER) {
            sb.append(_lexer.getLexeme());

            token = _lexer.parseToken();

            if (token == BiancaLexer.Token.DOT) {
               sb.append('.');
            } else {
               _lexer.saveToken(token);
               break;
            }
         } else if (token == BiancaLexer.Token.MUL) {
            if (sb.length() > 0) {
               sb.setLength(sb.length() - 1);
            }

            isWildcard = true;
            break;
         } else {
            throw error(L.l("'{0}' is an unexpected token in import",
               _lexer.tokenName(token)));
         }
      }

      //expect(';');

      return _factory.createImport(getLocation(), sb.toString(), isWildcard);
   }

   private String parseIdentifier()
      throws IOException {
      BiancaLexer.Token token = _lexer.getToken();
      _lexer.dropToken();

      if (token == BiancaLexer.Token.NONE) {
         token = _lexer.parseIdentifier();
      }

      if (token != BiancaLexer.Token.IDENTIFIER && !token.isIdentifierLexeme()) {
         throw error(L.l("expected identifier at {0}.", _lexer.tokenName(token)));
      }

      if (_lexer.getRead() == '\\') {
         throw error(L.l("namespace identifier is not allowed at '{0}\\'",
            _lexer.getLexeme()));
      }

      return _lexer.getLexeme();
   }

   /**
    * Parses the next string
    */
   private Expr parseSimpleArrayTail(Expr tail)
      throws IOException {
      /* TODO: split in lexer ? */
      int ch = _lexer.read();

      StringBuilder sb = new StringBuilder();

      if (ch == '$') {
         for (ch = _lexer.read(); _lexer.isIdentifierPart(ch); ch = _lexer.read()) {
            sb.append((char) ch);
         }

         VarExpr var = _factory.createVar(_function.createVar(sb.toString()));

         tail = _factory.createArrayGet(getLocation(), tail, var);
      } else if ('0' <= ch && ch <= '9') {
         long index = ch - '0';

         for (ch = _lexer.read();
              '0' <= ch && ch <= '9';
              ch = _lexer.read()) {
            index = 10 * index + ch - '0';
         }

         tail = _factory.createArrayGet(getLocation(),
            tail, _factory.createLong(index));
      } else if (_lexer.isIdentifierPart(ch)) {
         for (; _lexer.isIdentifierPart(ch); ch = _lexer.read()) {
            sb.append((char) ch);
         }

         Expr constExpr = _factory.createConst(sb.toString());

         tail = _factory.createArrayGet(getLocation(), tail, constExpr);
      } else {
         throw error(L.l("Unexpected character at {0}",
            String.valueOf((char) ch)));
      }

      if (ch != ']') {
         throw error(L.l("Expected ']' at {0}",
            String.valueOf((char) ch)));
      }

      return tail;
   }

   public Expr createString(String lexeme) {
      // TODO: see BiancaParser.parseDefault for _bianca == null
      return _factory.createString(lexeme);
   }

   /**
    * Returns an error.
    */
   private BiancaParseException expect(String expected, BiancaLexer.Token token) {
      return error(L.l("expected {0} at {1}", expected, _lexer.tokenName(token)));
   }

   /**
    * Returns an error.
    */
   public BiancaParseException error(String msg) {
      int lineNumber = _parserLocation.getLineNumber();
      int lines = 5;
      int first = lines / 2;

      String[] sourceLines = Env.getSourceLine(_sourceFile,
         lineNumber - first + _sourceOffset,
         lines);

      if (sourceLines != null
         && sourceLines.length > 0) {
         StringBuilder sb = new StringBuilder();

         String shortFile = _parserLocation.getFileName();
         int p = shortFile.lastIndexOf('/');
         if (p > 0) {
            shortFile = shortFile.substring(p + 1);
         }

         sb.append(_parserLocation.toString()).append(msg).append(" in");

         for (int i = 0; i < sourceLines.length; i++) {
            if (sourceLines[i] == null) {
               continue;
            }

            sb.append("\n");
            sb.append(shortFile).append(":").append(lineNumber - first + i).append(": ").append(sourceLines[i]);
         }

         return new BiancaParseException(sb.toString());
      } else {
         return new BiancaParseException(_parserLocation.toString() + msg);
      }
   }

   /**
    * The location from which the last token was read.
    *
    * @return
    */
   public Location getLocation() {
      return _parserLocation.getLocation();
   }

   private String pushWhileLabel() {
      return pushLoopLabel(createWhileLabel());
   }

   private String pushDoLabel() {
      return pushLoopLabel(createDoLabel());
   }

   private String pushForLabel() {
      return pushLoopLabel(createForLabel());
   }

   private String pushForeachLabel() {
      return pushLoopLabel(createForeachLabel());
   }

   private String pushSwitchLabel() {
      return pushLoopLabel(createSwitchLabel());
   }

   private String pushLoopLabel(String label) {
      _loopLabelList.add(label);

      return label;
   }

   private String popLoopLabel() {
      int size = _loopLabelList.size();

      if (size == 0) {
         return null;
      } else {
         return _loopLabelList.remove(size - 1);
      }
   }

   private String createWhileLabel() {
      return "while_" + _labelsCreated++;
   }

   private String createDoLabel() {
      return "do_" + _labelsCreated++;
   }

   private String createForLabel() {
      return "for_" + _labelsCreated++;
   }

   private String createForeachLabel() {
      return "foreach_" + _labelsCreated++;
   }

   private String createSwitchLabel() {
      return "switch_" + _labelsCreated++;
   }

   /*
    * Returns true if this is a switch label.
    */
   public static boolean isSwitchLabel(String label) {
      return label != null && label.startsWith("switch");
   }

   public void close() {
      ReadStream is = _is;
      _is = null;

      if (is != null) {
         is.close();
      }
   }

   private class ParserLocation {

      private int _lineNumber = 1;
      private String _fileName;
      private String _userPath;
      private String _lastClassName;
      private String _lastFunctionName;
      private Location _location;

      public int getLineNumber() {
         return _lineNumber;
      }

      public void setLineNumber(int lineNumber) {
         _lineNumber = lineNumber;
         _location = null;
      }

      public void incrementLineNumber() {
         _lineNumber++;
         _location = null;
      }

      public String getFileName() {
         return _fileName;
      }

      public void setFileName(String fileName) {
         _fileName = fileName;
         _userPath = fileName;

         _location = null;
      }

      public void setFileName(Path path) {
         // php/600a
         // need to return proper Windows paths (for joomla)
         _fileName = path.getNativePath();
         _userPath = path.getUserPath();
      }

      public String getUserPath() {
         return _userPath;
      }

      public Location getLocation() {
         String currentFunctionName = (_function == null || _function.isPageMain()
            ? null
            : _function.getName());

         String currentClassName = _classDef == null ? null : _classDef.getName();

         if (_location != null) {
            if (!equals(currentFunctionName, _lastFunctionName)) {
               _location = null;
            } else if (!equals(currentClassName, _lastClassName)) {
               _location = null;
            }
         }

         if (_location == null) {
            _location = new Location(_fileName, _lineNumber,
               currentClassName, currentFunctionName);
         }

         _lastFunctionName = currentFunctionName;
         _lastClassName = currentClassName;

         return _location;
      }

      private boolean equals(String s1, String s2) {
         return (s1 == null) ? s2 == null : s1.equals(s2);
      }

      @Override
      public String toString() {
         return _fileName + ":" + _lineNumber + ": ";
      }
   }
}
