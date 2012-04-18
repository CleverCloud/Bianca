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
* @author Marc-Antoine Perennou <Marc-Antoine@Perennou.com>
*/
package com.clevercloud.bianca.parser;

import com.clevercloud.util.CharBuffer;
import com.clevercloud.util.L10N;
import com.clevercloud.vfs.IOExceptionWrapper;
import com.clevercloud.vfs.ReadStream;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.HashMap;

public class BiancaLexer {
   private final static L10N L = new L10N(BiancaLexer.class);
   private int _peek = -1;
   private Token _peekToken = Token.LAST_IDENTIFIER_LEXEME;
   private BiancaParser _parser;
   private ReadStream _is;
   private boolean _hasCr;
   private String _lexeme = "";
   private String _heredocEnd = null;
   private CharBuffer _sb = new CharBuffer();
   private String _comment;
   private final static HashMap<String, Token> _insensitiveReserved = new HashMap<String, Token>();
   private String _namespace = "";
   private HashMap<String, String> _namespaceUseMap = new HashMap<String, String>();

   public BiancaLexer(BiancaParser parser, ReadStream is) {
      this._parser = parser;
      this._is = is;
   }

   public void init(ReadStream is) {
      this._is = is;
      this._peek = -1;
      this._peekToken = Token.LAST_IDENTIFIER_LEXEME;
   }

   public String getLexeme() {
      return _lexeme;
   }

   public String getComment() {
      return _comment;
   }

   public void dropComment() {
      _comment = null;
   }

   public enum Token {
      IDENTIFIER,
      STRING,
      LONG,
      DOUBLE,
      LSHIFT,
      RSHIFT,
      PHP_END,
      EQ,
      DEREF,
      LEQ,
      GEQ,
      NEQ,
      EQUALS,
      NEQUALS,
      C_AND,
      C_OR,
      PLUS_ASSIGN,
      MINUS_ASSIGN,
      APPEND_ASSIGN,
      MUL_ASSIGN,
      DIV_ASSIGN,
      MOD_ASSIGN,
      AND_ASSIGN,
      OR_ASSIGN,
      XOR_ASSIGN,
      LSHIFT_ASSIGN,
      RSHIFT_ASSIGN,
      INCR,
      DECR,
      SCOPE,
      ESCAPED_STRING,
      HEREDOC,
      ARRAY_RIGHT,
      SIMPLE_STRING_ESCAPE,
      COMPLEX_STRING_ESCAPE,
      BINARY,
      SIMPLE_BINARY_ESCAPE,
      COMPLEX_BINARY_ESCAPE,
      FIRST_IDENTIFIER_LEXEME,
      ECHO,
      NULL,
      IF,
      WHILE,
      FUNCTION,
      CLASS,
      NEW,
      RETURN,
      VAR,
      PRIVATE,
      PROTECTED,
      PUBLIC,
      FOR,
      DO,
      BREAK,
      CONTINUE,
      ELSE,
      EXTENDS,
      STATIC,
      INCLUDE,
      REQUIRE,
      INCLUDE_ONCE,
      REQUIRE_ONCE,
      UNSET,
      FOREACH,
      AS,
      TEXT,
      ISSET,
      SWITCH,
      CASE,
      DEFAULT,
      EXIT,
      GLOBAL,
      ELSEIF,
      PRINT,
      SYSTEM_STRING,
      SIMPLE_SYSTEM_STRING,
      COMPLEX_SYSTEM_STRING,
      TEXT_ECHO,
      ENDIF,
      ENDWHILE,
      ENDFOR,
      ENDFOREACH,
      ENDSWITCH,
      XOR_RES,
      AND_RES,
      OR_RES,
      LIST,
      THIS,
      TRUE,
      FALSE,
      CLONE,
      INSTANCEOF,
      CONST,
      ABSTRACT,
      FINAL,
      DIE,
      THROW,
      TRY,
      CATCH,
      INTERFACE,
      IMPLEMENTS,
      IMPORT,
      TEXT_PHP,
      NAMESPACE,
      USE,
      LAST_IDENTIFIER_LEXEME
   }

   /**
    * Reads the next character.
    */
   public int read()
      throws IOException {
      int peek = _peek;

      if (peek >= 0) {
         _peek = -1;
         return peek;
      }

      try {
         int ch = _is.readChar();

         if (ch == '\r') {
            _parser.incrementLineNumber();
            _hasCr = true;
         } else if (ch == '\n' && !_hasCr) {
            _parser.incrementLineNumber();
         } else {
            _hasCr = false;
         }

         return ch;
      } catch (CharConversionException e) {
         throw new BiancaParseException(_parser.getFileName() + ":" + _parser.getLine()
            + ": " + e
            + "\nCheck that the script-encoding setting matches the "
            + "source file's encoding", e);
      } catch (IOException e) {
         throw new IOExceptionWrapper(
            _parser.getFileName() + ":" + _parser.getLine() + ":" + e, e);
      }
   }

   private boolean parseTextMatch(StringBuilder sb, String text)
      throws IOException {
      int len = text.length();

      for (int i = 0; i < len; i++) {
         int ch = read();

         if (ch < 0) {
            return false;
         }

         if (Character.toLowerCase(ch) != text.charAt(i)) {
            _peek = ch;
            return false;
         } else {
            sb.append((char) ch);
         }
      }

      return true;
   }

   private void parseWhitespace(StringBuilder sb)
      throws IOException {
      int ch;

      while (Character.isWhitespace((ch = read()))) {
         sb.append((char) ch);
      }

      _peek = ch;
   }

   /**
    * Parses the <script language="bianca"> opening
    */
   private boolean parseScriptBegin(StringBuilder sb)
      throws IOException {
      int begin = sb.length();

      sb.append('<');

      if (!parseTextMatch(sb, "script")) {
         return false;
      }

      parseWhitespace(sb);

      if (!parseTextMatch(sb, "language=")) {
         return false;
      }

      int openingParentheses = read();

      if (openingParentheses == '\'' || openingParentheses == '"') {
         if (!parseTextMatch(sb, "php")) {
            sb.append((char) openingParentheses);
            return false;
         }

         int closingParentheses = read();
         if (openingParentheses != closingParentheses) {
            sb.append((char) closingParentheses);
            return false;
         }
      }


      parseWhitespace(sb);

      int ch = read();

      if (ch == '>') {
         sb.setLength(begin);
         return true;
      } else {
         _peek = ch;
         return false;
      }
   }

   /**
    * Parses bianca text
    */
   public Token parsePhpText()
      throws IOException {
      StringBuilder sb = new StringBuilder();

      int ch = read();
      while (ch > 0) {
         if (ch == '<') {
            if ((ch = read()) == 's' || ch == 'S') {
               _peek = ch;
               if (parseScriptBegin(sb)) {
                  _lexeme = sb.toString();
                  return Token.TEXT;
               }
               ch = read();
            } else if (ch == '%') {
               if ((ch = read()) == '=') {
                  _lexeme = sb.toString();

                  return Token.TEXT_ECHO;
               } else if (Character.isWhitespace(ch)) {
                  _lexeme = sb.toString();

                  return Token.TEXT;
               }
            } else if (ch != '?') {
               sb.append('<');
            } else if ((ch = read()) == '=') {
               _lexeme = sb.toString();

               return Token.TEXT_ECHO;
            } else {
               _lexeme = sb.toString();
               _peek = ch;

               if (ch == 'p' || ch == 'P') {
                  return Token.TEXT_PHP;
               } else {
                  return Token.TEXT;
               }
            }
         } else {
            sb.append((char) ch);

            ch = read();
         }
      }

      _lexeme = sb.toString();

      return Token.TEXT;
   }

   private int parseOctalEscape(int ch)
      throws IOException {
      int value = ch - '0';

      ch = read();
      if (ch < '0' || ch > '7') {
         _peek = ch;
         return value;
      }

      value = 8 * value + ch - '0';

      ch = read();
      if (ch < '0' || ch > '7') {
         _peek = ch;
         return value;
      }

      value = 8 * value + ch - '0';

      return value;
   }

   private int parseHexEscape()
      throws IOException {
      int value = 0;

      int ch = read();

      if ('0' <= ch && ch <= '9') {
         value = 16 * value + ch - '0';
      } else if ('a' <= ch && ch <= 'f') {
         value = 16 * value + 10 + ch - 'a';
      } else if ('A' <= ch && ch <= 'F') {
         value = 16 * value + 10 + ch - 'A';
      } else {
         _peek = ch;
         return -1;
      }

      ch = read();

      if ('0' <= ch && ch <= '9') {
         value = 16 * value + ch - '0';
      } else if ('a' <= ch && ch <= 'f') {
         value = 16 * value + 10 + ch - 'a';
      } else if ('A' <= ch && ch <= 'F') {
         value = 16 * value + 10 + ch - 'A';
      } else {
         _peek = ch;
         return value;
      }

      return value;
   }

   private int parseUnicodeEscape(boolean isLongForm)
      throws IOException {
      int codePoint = parseHexEscape();

      if (codePoint < 0) {
         return -1;
      }

      int low = parseHexEscape();

      if (low < 0) {
         return codePoint;
      }

      codePoint = codePoint * 256 + low;

      if (isLongForm) {
         low = parseHexEscape();

         if (low < 0) {
            return codePoint;
         }

         codePoint = codePoint * 256 + low;
      }

      return codePoint;
   }

   private boolean isIdentifierStart(int ch) {
      if (ch < 0) {
         return false;
      } else {
         return (ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z'
            || ch == '_'
            || Character.isLetter(ch));
      }
   }

   /**
    * Parses the next string
    */
   public Token parseEscapedString(char end)
      throws IOException {
      _sb.setLength(0);

      int ch;

      while ((ch = read()) > 0) {
         if (_heredocEnd == null && ch == end) {
            _lexeme = _sb.toString();
            return Token.STRING;
         } else if (ch == '\\') {
            ch = read();

            int result;
            switch (ch) {
               case '0':
               case '1':
               case '2':
               case '3':
                  _sb.append((char) parseOctalEscape(ch));
                  break;
               case 't':
                  _sb.append('\t');
                  break;
               case 'r':
                  _sb.append('\r');
                  break;
               case 'n':
                  _sb.append('\n');
                  break;
               case '"':
               case '`':
                  if (_heredocEnd != null) {
                     _sb.append('\\');
                  }

                  _sb.append((char) ch);
                  break;
               case '$':
               case '\\':
                  _sb.append((char) ch);
                  break;
               case 'x': {
                  int value = parseHexEscape();

                  if (value >= 0) {
                     _sb.append((char) value);
                  } else {
                     _sb.append('\\');
                     _sb.append('x');
                  }

                  break;
               }
               case 'u':
                  result = parseUnicodeEscape(false);

                  if (result < 0) {
                     _sb.append('\\');
                     _sb.append('u');
                  } else {
                     _sb.append(Character.toChars(result));
                  }
                  break;
               case 'U':
                  result = parseUnicodeEscape(true);

                  if (result < 0) {
                     _sb.append('\\');
                     _sb.append('U');
                  } else {
                     _sb.append(Character.toChars(result));
                  }
                  break;
               case '{':
                  ch = read();
                  _peek = ch;
                  if (ch == '$' && _heredocEnd == null) {
                     _sb.append('{');
                  } else {
                     _sb.append("\\{");
                  }
                  break;
               default:
                  _sb.append('\\');
                  _sb.append((char) ch);
                  break;
            }
         } else if (ch == '$') {
            ch = read();

            if (ch == '{') {
               _peek = '$';
               _lexeme = _sb.toString();
               return Token.COMPLEX_STRING_ESCAPE;
            } else if (isIdentifierStart(ch)) {
               _peek = ch;
               _lexeme = _sb.toString();
               return Token.SIMPLE_STRING_ESCAPE;
            } else {
               _sb.append('$');
               _peek = ch;
            }
         } else if (ch == '{') {
            ch = read();

            if (ch == '$') {
               _peek = ch;
               _lexeme = _sb.toString();
               return Token.COMPLEX_STRING_ESCAPE;
            } else {
               _peek = ch;
               _sb.append('{');
            }
         } /* bianca/013c
         else if ((ch == '\r' || ch == '\n') && _heredocEnd == null)
         throw error(L.l("unexpected newline in string."));
          */ else {
            _sb.append((char) ch);

            if (_heredocEnd == null || !_sb.endsWith(_heredocEnd)) {
            } else if (_sb.length() == _heredocEnd.length()
               || _sb.charAt(_sb.length() - _heredocEnd.length() - 1) == '\n'
               || _sb.charAt(_sb.length() - _heredocEnd.length() - 1) == '\r') {
               _sb.setLength(_sb.length() - _heredocEnd.length());

               if (_sb.length() > 0 && _sb.charAt(_sb.length() - 1) == '\n') {
                  _sb.setLength(_sb.length() - 1);
               }
               if (_sb.length() > 0 && _sb.charAt(_sb.length() - 1) == '\r') {
                  _sb.setLength(_sb.length() - 1);
               }

               _heredocEnd = null;
               _lexeme = _sb.toString();
               return Token.STRING;
            }
         }
      }

      _lexeme = _sb.toString();

      return Token.STRING;
   }

   /**
    * Parses the next string token.
    */
   private void parseStringToken(int end)
      throws IOException {
      _sb.setLength(0);

      int ch;

      for (ch = read(); ch >= 0 && ch != end; ch = read()) {
         if (ch == '\\') {
            ch = read();

            if (ch == 'u') {
               int value = parseUnicodeEscape(false);

               if (value < 0) {
                  _sb.append('\\');
                  _sb.append('u');
               } else {
                  _sb.append(Character.toChars(value));
               }

               continue;
            } else if (ch == 'U') {
               int value = parseUnicodeEscape(true);

               if (value < 0) {
                  _sb.append('\\');
                  _sb.append('U');
               } else {
                  _sb.append(Character.toChars(value));
               }

               continue;
            }

            if (end == '"') {
               _sb.append('\\');

               if (ch >= 0) {
                  _sb.append((char) ch);
               }
            } else {
               switch (ch) {
                  case '\'':
                  case '\\':
                     _sb.append((char) ch);
                     break;
                  default:
                     _sb.append('\\');
                     _sb.append((char) ch);
                     break;
               }
            }
         } else {
            _sb.append((char) ch);
         }
      }

      _lexeme = _sb.toString();
   }

   /**
    * Parses a multiline comment.
    */
   private void parseMultilineComment()
      throws IOException {
      int ch = read();

      if (ch == '*') {
         _sb.setLength(0);
         _sb.append('/');
         _sb.append('*');

         do {
            if (ch != '*') {
               _sb.append((char) ch);
            } else if ((ch = read()) == '/') {
               _sb.append('*');
               _sb.append('/');

               _comment = _sb.toString();

               return;
            } else {
               _sb.append('*');
               _peek = ch;
            }
         } while ((ch = read()) >= 0);

         _comment = _sb.toString();
      } else if (ch >= 0) {
         do {
            if (ch != '*') {
            } else if ((ch = read()) == '/') {
               return;
            } else {
               _peek = ch;
            }
         } while ((ch = read()) >= 0);
      }
   }

   /**
    * Returns the token name.
    */
   public String tokenName(Token token) {
      switch (token) {
         case LAST_IDENTIFIER_LEXEME:
            return "end of file";

         case '\'':
            return "'";

         case AS:
            return "'as'";

         case TRUE:
            return "true";
         case FALSE:
            return "false";

         case AND_RES:
            return "'and'";
         case OR_RES:
            return "'or'";
         case XOR_RES:
            return "'xor'";

         case C_AND:
            return "'&&'";
         case C_OR:
            return "'||'";

         case IF:
            return "'if'";
         case ELSE:
            return "'else'";
         case ELSEIF:
            return "'elseif'";
         case ENDIF:
            return "'endif'";

         case WHILE:
            return "'while'";
         case ENDWHILE:
            return "'endwhile'";
         case DO:
            return "'do'";

         case FOR:
            return "'for'";
         case ENDFOR:
            return "'endfor'";

         case FOREACH:
            return "'foreach'";
         case ENDFOREACH:
            return "'endforeach'";

         case SWITCH:
            return "'switch'";
         case ENDSWITCH:
            return "'endswitch'";

         case ECHO:
            return "'echo'";
         case PRINT:
            return "'print'";

         case LIST:
            return "'list'";
         case CASE:
            return "'case'";

         case DEFAULT:
            return "'default'";
         case CLASS:
            return "'class'";
         case INTERFACE:
            return "'interface'";
         case EXTENDS:
            return "'extends'";
         case IMPLEMENTS:
            return "'implements'";
         case RETURN:
            return "'return'";

         case DIE:
            return "'die'";
         case EXIT:
            return "'exit'";
         case THROW:
            return "'throw'";

         case CLONE:
            return "'clone'";
         case INSTANCEOF:
            return "'instanceof'";

         case SIMPLE_STRING_ESCAPE:
            return "string";
         case COMPLEX_STRING_ESCAPE:
            return "string";

         case REQUIRE:
            return "'require'";
         case REQUIRE_ONCE:
            return "'require_once'";

         case PRIVATE:
            return "'private'";
         case PROTECTED:
            return "'protected'";
         case PUBLIC:
            return "'public'";
         case STATIC:
            return "'static'";
         case FINAL:
            return "'final'";
         case ABSTRACT:
            return "'abstract'";
         case CONST:
            return "'const'";

         case GLOBAL:
            return "'global'";

         case FUNCTION:
            return "'function'";

         case THIS:
            return "'this'";

         case ARRAY_RIGHT:
            return "'=>'";
         case LSHIFT:
            return "'<<'";

         case IDENTIFIER:
            return "'" + _lexeme + "'";

         case LONG:
            return "integer (" + _lexeme + ")";

         case DOUBLE:
            return "double (" + _lexeme + ")";

         case TEXT:
            return "TEXT (token " + token + ")";

         case STRING:
            return "string(" + _lexeme + ")";

         case TEXT_ECHO:
            return "<?=";

         case SCOPE:
            return "SCOPE (" + _lexeme + ")";

         case NAMESPACE:
            return "NAMESPACE";

         case USE:
            return "USE";

         default:
            if (32 <= token && token < 127) {
               return "'" + (char) token + "'";
            } else {
               return "(token " + token + ")";
            }
      }
   }

   /**
    * Parses the next heredoc token.
    */
   private Token parseHeredocToken()
      throws IOException {
      _sb.setLength(0);

      int ch;

      // eat whitespace
      while ((ch = read()) >= 0 && (ch == ' ' || ch == '\t')) {
      }
      _peek = ch;

      while ((ch = read()) >= 0 && ch != '\r' && ch != '\n') {
         _sb.append((char) ch);
      }

      _heredocEnd = _sb.toString();

      if (ch == '\n') {
      } else if (ch == '\r') {
         ch = read();
         if (ch != '\n') {
            _peek = ch;
         }
      } else {
         _peek = ch;
      }

      return parseEscapedString('"');
   }

   public void expect(Token expect)
      throws IOException {
      Token token = parseToken();

      if (token != expect) {
         throw _parser.error(L.l("expected {0} at {1}",
            tokenName(expect),
            tokenName(token)));
      }
   }

   /**
    * Parses the next as hex
    */
   private Token parseHex()
      throws IOException {
      long value = 0;
      double dValue = 0;

      while (true) {
         int ch = read();

         if ('0' <= ch && ch <= '9') {
            value = 16 * value + ch - '0';
            dValue = 16 * dValue + ch - '0';
         } else if ('a' <= ch && ch <= 'f') {
            value = 16 * value + ch - 'a' + 10;
            dValue = 16 * dValue + ch - 'a' + 10;
         } else if ('A' <= ch && ch <= 'F') {
            value = 16 * value + ch - 'A' + 10;
            dValue = 16 * dValue + ch - 'A' + 10;
         } else {
            _peek = ch;
            break;
         }
      }

      if (value == dValue) {
         _lexeme = String.valueOf(value);
         return Token.LONG;
      } else {
         _lexeme = String.valueOf(dValue);

         return Token.DOUBLE;
      }
   }

   /**
    * Parses the next as bin
    */
   private Token parseBin()
      throws IOException {
      long value = 0;
      double dValue = 0;

      while (true) {
         int ch = read();

         if ('0' == ch || ch == '1') {
            value = 2 * value + ch - '0';
            dValue = 2 * dValue + ch - '0';
         } else {
            _peek = ch;
            break;
         }
      }

      if (value == dValue) {
         _lexeme = String.valueOf(value);
         return Token.LONG;
      } else {
         _lexeme = String.valueOf(dValue);
         return Token.DOUBLE;
      }
   }

   /**
    * Parses the next number.
    */
   private Token parseNumberToken(int ch)
      throws IOException {
      int ch0 = ch;

      if (ch == '0') {
         ch = read();
         if (ch == 'x' || ch == 'X') {
            return parseHex();
         } else if (ch == 'b' || ch == 'B') {
            return parseBin();
         } else if (ch == '0') {
            return parseNumberToken(ch);
         } else {
            _peek = ch;
            ch = '0';
         }
      }

      _sb.setLength(0);

      Token token = Token.LONG;

      for (; '0' <= ch && ch <= '9'; ch = read()) {
         _sb.append((char) ch);
      }

      if (ch == '.') {
         token = Token.DOUBLE;

         _sb.append((char) ch);

         for (ch = read(); '0' <= ch && ch <= '9'; ch = read()) {
            _sb.append((char) ch);
         }
      }

      if (ch == 'e' || ch == 'E') {
         token = Token.DOUBLE;

         _sb.append((char) ch);

         ch = read();
         if (ch == '+' || ch == '-') {
            _sb.append((char) ch);
            ch = read();
         }

         if ('0' <= ch && ch <= '9') {
            for (; '0' <= ch && ch <= '9'; ch = read()) {
               _sb.append((char) ch);
            }
         } else {
            throw _parser.error(L.l("illegal exponent"));
         }
      }

      _peek = ch;

      if (ch0 == '0' && token == Token.LONG) {
         int len = _sb.length();
         int value = 0;

         for (int i = 0; i < len; i++) {
            ch = _sb.charAt(i);
            if ('0' <= ch && ch <= '7') {
               value = value * 8 + ch - '0';
            } else {
               break;
            }
         }

         _lexeme = String.valueOf(value);
      } else {
         _lexeme = _sb.toString();
      }

      return token;
   }

   private int ignoreWhiteSpace(int ch)
      throws IOException {
      for (; Character.isWhitespace(ch); ch = read()) {
      }
      return ch;
   }

   private int ignoreMultiLineComment(int ch)
      throws IOException {
      do {
         if (ch == '*') {
            if ((ch = read()) == '/') {
               return read();
            }
         }
      } while ((ch = read()) >= 0);

      return 0;
   }

   private int ignoreSingleLineComment(int ch)
      throws IOException {
      do {
         if (ch == '\r' || ch == '\n') {
            return ch;
         }
      } while ((ch = read()) >= 0);

      return 0;
   }

   private boolean isNamespaceIdentifierStart(int ch) {
      return isIdentifierStart(ch) || ch == '\\';
   }

   private boolean isNamespaceIdentifierPart(int ch) {
      return isIdentifierPart(ch) || ch == '\\';
   }

   public boolean isIdentifierPart(int ch) {
      if (ch < 0) {
         return false;
      } else {
         return (ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z'
            || ch >= '0' && ch <= '9'
            || ch == '_'
            || Character.isLetterOrDigit(ch));
      }
   }

   private Token lexemeToToken()
      throws IOException {
      _lexeme = _sb.toString();

      // the 'static' reserved keyword vs late static binding (static::$a)
      if (_peek == ':' && "static".equals(_lexeme)) {
         return Token.IDENTIFIER;
      }

      Token reserved = _insensitiveReserved.get(_lexeme.toLowerCase());
      if (reserved != Token.LAST_IDENTIFIER_LEXEME) {
         return reserved;
      } else {
         return Token.IDENTIFIER;
      }
   }

   private Token parseNamespaceIdentifier(int ch)
      throws IOException {
      ch = ignoreWhiteSpace(ch);

      long pos = _is.getPosition();

      if (ch == '/') {
         if ((ch = read()) == '*') {
            ch = ignoreMultiLineComment(ch);
         } else if (ch == '/') {
            ch = ignoreSingleLineComment(ch);
         } else {
            // Restore previous character if not comment
            _is.setPosition(pos);
            ch = '/';
         }
      }

      ch = ignoreWhiteSpace(ch);

      if (isNamespaceIdentifierStart(ch)) {
         _sb.setLength(0);
         _sb.append((char) ch);

         for (ch = read(); ch >= 0; ch = read()) {
            pos = _is.getPosition();
            if (ch == '/') {
               if ((ch = read()) == '*') {
                  ch = ignoreMultiLineComment(ch);
                  continue;
               } else if (ch == '/') {
                  ch = ignoreSingleLineComment(ch);
                  continue;
               }

               // Restore previous character if not comment
               _is.setPosition(pos);
               ch = '/';
            }

            if (isNamespaceIdentifierPart(ch)) {
               _sb.append((char) ch);
            } else {
               break;
            }
         }

         _peek = ch;

         return lexemeToToken();
      }

      throw _parser.error("unknown lexeme:" + (char) ch);
   }

   /**
    * Parses the next token.
    */
   public Token parseToken()
      throws IOException {
      Token peekToken = _peekToken;
      if (peekToken != Token.LAST_IDENTIFIER_LEXEME) {
         _peekToken = Token.LAST_IDENTIFIER_LEXEME;
         return peekToken;
      }

      while (true) {
         int ch = read();

         switch (ch) {
            case -1:
               return Token.LAST_IDENTIFIER_LEXEME;

            case ' ':
            case '\t':
            case '\n':
            case '\r':
               break;

            case '#':
               while ((ch = read()) != '\n' && ch != '\r' && ch >= 0) {
                  if (ch != '?') {
                  } else if ((ch = read()) != '>') {
                     _peek = ch;
                  } else {
                     ch = read();
                     if (ch == '\r') {
                        ch = read();
                     }
                     if (ch != '\n') {
                        _peek = ch;
                     }

                     return parsePhpText();
                  }
               }
               break;

            case '"': {
               String heredocEnd = _heredocEnd;
               _heredocEnd = null;

               Token result = parseEscapedString('"');
               _heredocEnd = heredocEnd;

               return result;
            }
            case '`': {
               Token token = parseEscapedString('`');

               switch (token) {
                  case STRING:
                     return Token.SYSTEM_STRING;
                  case SIMPLE_STRING_ESCAPE:
                     return Token.SIMPLE_SYSTEM_STRING;
                  case COMPLEX_STRING_ESCAPE:
                     return Token.COMPLEX_SYSTEM_STRING;
                  default:
                     throw new IllegalStateException();
               }
            }

            case '\'':
               parseStringToken('\'');
               return Token.STRING;

            case ';':
            case '$':
            case '(':
            case ')':
            case '@':
            case '[':
            case ']':
            case ',':
            case '{':
            case '}':
            case '~':
               return ch;

            case '+':
               ch = read();
               if (ch == '=') {
                  return Token.PLUS_ASSIGN;
               } else if (ch == '+') {
                  return Token.INCR;
               } else {
                  _peek = ch;
               }

               return '+';

            case '-':
               ch = read();
               if (ch == '>') {
                  return Token.DEREF;
               } else if (ch == '=') {
                  return Token.MINUS_ASSIGN;
               } else if (ch == '-') {
                  return Token.DECR;
               } else {
                  _peek = ch;
               }

               return '-';

            case '*':
               ch = read();
               if (ch == '=') {
                  return Token.MUL_ASSIGN;
               } else {
                  _peek = ch;
               }

               return '*';

            case '/':
               ch = read();
               if (ch == '=') {
                  return Token.DIV_ASSIGN;
               } else if (ch == '/') {
                  while (ch >= 0) {
                     if (ch == '\n' || ch == '\r') {
                        break;
                     } else if (ch == '?') {
                        ch = read();

                        if (ch == '>') {
                           ch = read();

                           if (ch == '\r') {
                              ch = read();
                           }
                           if (ch != '\n') {
                              _peek = ch;
                           }

                           return parsePhpText();
                        }
                     } else {
                        ch = read();
                     }
                  }
                  break;
               } else if (ch == '*') {
                  parseMultilineComment();
                  break;
               } else {
                  _peek = ch;
               }

               return '/';

            case '%':
               ch = read();
               if (ch == '=') {
                  return Token.MOD_ASSIGN;
               } else if (ch == '>') {
                  ch = read();
                  if (ch == '\r') {
                     ch = read();
                  }
                  if (ch != '\n') {
                     _peek = ch;
                  }

                  return parsePhpText();
               } else {
                  _peek = ch;
               }

               return '%';

            case ':':
               ch = read();
               if (ch == ':') {
                  return Token.SCOPE;
               } else {
                  _peek = ch;
               }

               return ':';

            case '=':
               ch = read();
               if (ch == '=') {
                  ch = read();
                  if (ch == '=') {
                     return Token.EQUALS;
                  } else {
                     _peek = ch;
                     return Token.EQ;
                  }
               } else if (ch == '>') {
                  return Token.ARRAY_RIGHT;
               } else {
                  _peek = ch;
                  return '=';
               }

            case '!':
               ch = read();
               if (ch == '=') {
                  ch = read();
                  if (ch == '=') {
                     return Token.NEQUALS;
                  } else {
                     _peek = ch;
                     return Token.NEQ;
                  }
               } else {
                  _peek = ch;
                  return '!';
               }

            case '&':
               ch = read();
               if (ch == '&') {
                  return Token.C_AND;
               } else if (ch == '=') {
                  return Token.AND_ASSIGN;
               } else {
                  _peek = ch;
                  return '&';
               }

            case '^':
               ch = read();
               if (ch == '=') {
                  return Token.XOR_ASSIGN;
               } else {
                  _peek = ch;
               }

               return '^';

            case '|':
               ch = read();
               if (ch == '|') {
                  return Token.C_OR;
               } else if (ch == '=') {
                  return Token.OR_ASSIGN;
               } else {
                  _peek = ch;
                  return '|';
               }

            case '<':
               ch = read();
               if (ch == '<') {
                  ch = read();

                  if (ch == '=') {
                     return Token.LSHIFT_ASSIGN;
                  } else if (ch == '<') {
                     return parseHeredocToken();
                  } else {
                     _peek = ch;
                  }

                  return Token.LSHIFT;
               } else if (ch == '=') {
                  return Token.LEQ;
               } else if (ch == '>') {
                  return Token.NEQ;
               } else if (ch == '/') {
                  StringBuilder sb = new StringBuilder();

                  if (!parseTextMatch(sb, "script")) {
                     throw _parser.error(L.l("expected 'script' at '{0}'", sb));
                  }

                  expect('>');

                  return parsePhpText();
               } else {
                  _peek = ch;
               }

               return '<';

            case '>':
               ch = read();
               if (ch == '>') {
                  ch = read();

                  if (ch == '=') {
                     return Token.RSHIFT_ASSIGN;
                  } else {
                     _peek = ch;
                  }

                  return Token.RSHIFT;
               } else if (ch == '=') {
                  return Token.GEQ;
               } else {
                  _peek = ch;
               }

               return '>';

            case '?':
               ch = read();
               if (ch == '>') {
                  ch = read();
                  if (ch == '\r') {
                     ch = read();
                  }
                  if (ch != '\n') {
                     _peek = ch;
                  }

                  return parsePhpText();
               } else {
                  _peek = ch;
               }

               return '?';

            case '.':
               ch = read();

               if (ch == '=') {
                  return Token.APPEND_ASSIGN;
               }

               _peek = ch;

               if ('0' <= ch && ch <= '9') {
                  return parseNumberToken('.');
               } else {
                  return '.';
               }

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               return parseNumberToken(ch);

            default:

               if (ch == 'b') {
                  int ch2 = read();

                  if (ch2 == '\'') {
                     parseStringToken('\'');
                     return Token.BINARY;
                  } else if (ch2 == '"') {

                     Token token = parseEscapedString('"');
                     switch (token) {
                        case STRING:
                           return Token.BINARY;
                        case SIMPLE_STRING_ESCAPE:
                           return Token.SIMPLE_BINARY_ESCAPE;
                        case COMPLEX_STRING_ESCAPE:
                           return Token.COMPLEX_BINARY_ESCAPE;
                        default:
                           return token;
                     }
                  } else {
                     _peek = ch2;
                  }
               }

               return parseNamespaceIdentifier(ch);
         }
      }
   }

   private Token parseIdentifier(int ch)
      throws IOException {
      ch = ignoreWhiteSpace(ch);

      if (isIdentifierStart(ch)) {
         _sb.setLength(0);
         _sb.append((char) ch);

         for (ch = read(); isIdentifierPart(ch); ch = read()) {
            _sb.append((char) ch);
         }

         _peek = ch;

         return lexemeToToken();
      }

      throw _parser.error("expected identifier at " + (char) ch);
   }

   public Token parseIdentifier() throws IOException {
      return parseIdentifier(read());
   }

   public String resolveIdentifier(String id) {
      if (id.startsWith("\\")) {
         return id.substring(1);
      }

      int ns = id.indexOf('\\');

      if (ns > 0) {
         String prefix = id.substring(0, ns);

         String use = _namespaceUseMap.get(prefix);

         if (use != null) {
            return use + id.substring(ns);
         } else if (_namespace.equals("")) {
            return id;
         } else {
            return _namespace + "\\" + id;
         }
      } else {
         String use = _namespaceUseMap.get(id);

         if (use != null) {
            return use;
         } else if (_namespace.equals("")) {
            return id;
         } else {
            return _namespace + '\\' + id;
         }
      }
   }

   public String parseNamespaceIdentifier()
      throws IOException {
      BiancaLexer.Token token = _peekToken;

      if (token == Token.LAST_IDENTIFIER_LEXEME) {
         token = parseNamespaceIdentifier(read());
      }

      if (token == BiancaLexer.Token.IDENTIFIER) {
         return resolveIdentifier(_lexeme);
      } else if (BiancaLexer.Token.FIRST_IDENTIFIER_LEXEME <= token) {
         return resolveIdentifier(_lexeme);
      } else {
         throw _parser.error(L.l("expected identifier at {0}.", tokenName(token)));
      }
   }

   public Token nextToken() throws IOException {
      /*TODO: check lexeme of callers*/
      _peekToken = parseToken();
      return _peekToken;
   }

   public void saveToken(Token token) {
      _peekToken = token;
   }

   public void dropToken() {
      _peekToken = Token.LAST_IDENTIFIER_LEXEME;
   }

   public Token getToken() {
      return _peekToken;
   }

   public void saveRead(int ch) {
      _peek = ch;
   }

   public int getRead() {
      return _peek;
   }
   
   public String getNamespace() {
      return _namespace;
   }
   
   public void setNamespace(String namespace) {
      _namespace = namespace;
   }
   
   public void putNamespace(String key, String name) {
      _namespaceUseMap.put(key, name);
   }

   static {
      _insensitiveReserved.put("echo", Token.ECHO);
      _insensitiveReserved.put("print", Token.PRINT);
      _insensitiveReserved.put("if", Token.IF);
      _insensitiveReserved.put("else", Token.ELSE);
      _insensitiveReserved.put("elseif", Token.ELSEIF);
      _insensitiveReserved.put("do", Token.DO);
      _insensitiveReserved.put("while", Token.WHILE);
      _insensitiveReserved.put("for", Token.FOR);
      _insensitiveReserved.put("function", Token.FUNCTION);
      _insensitiveReserved.put("class", Token.CLASS);
      _insensitiveReserved.put("new", Token.NEW);
      _insensitiveReserved.put("return", Token.RETURN);
      _insensitiveReserved.put("break", Token.BREAK);
      _insensitiveReserved.put("continue", Token.CONTINUE);
      // bianca/0260
      //    _insensitiveReserved.put("var", Token.VAR);
      _insensitiveReserved.put("this", Token.THIS);
      _insensitiveReserved.put("private", Token.PRIVATE);
      _insensitiveReserved.put("protected", Token.PROTECTED);
      _insensitiveReserved.put("public", Token.PUBLIC);
      _insensitiveReserved.put("and", Token.AND_RES);
      _insensitiveReserved.put("xor", Token.XOR_RES);
      _insensitiveReserved.put("or", Token.OR_RES);
      _insensitiveReserved.put("extends", Token.EXTENDS);
      _insensitiveReserved.put("static", Token.STATIC);
      _insensitiveReserved.put("include", Token.INCLUDE);
      _insensitiveReserved.put("require", Token.REQUIRE);
      _insensitiveReserved.put("include_once", Token.INCLUDE_ONCE);
      _insensitiveReserved.put("require_once", Token.REQUIRE_ONCE);
      _insensitiveReserved.put("unset", Token.UNSET);
      _insensitiveReserved.put("foreach", Token.FOREACH);
      _insensitiveReserved.put("as", Token.AS);
      _insensitiveReserved.put("switch", Token.SWITCH);
      _insensitiveReserved.put("case", Token.CASE);
      _insensitiveReserved.put("default", Token.DEFAULT);
      _insensitiveReserved.put("die", Token.DIE);
      _insensitiveReserved.put("exit", Token.EXIT);
      _insensitiveReserved.put("global", Token.GLOBAL);
      _insensitiveReserved.put("list", Token.LIST);
      _insensitiveReserved.put("endif", Token.ENDIF);
      _insensitiveReserved.put("endwhile", Token.ENDWHILE);
      _insensitiveReserved.put("endfor", Token.ENDFOR);
      _insensitiveReserved.put("endforeach", Token.ENDFOREACH);
      _insensitiveReserved.put("endswitch", Token.ENDSWITCH);

      _insensitiveReserved.put("true", Token.TRUE);
      _insensitiveReserved.put("false", Token.FALSE);
      _insensitiveReserved.put("null", Token.NULL);
      _insensitiveReserved.put("clone", Token.CLONE);
      _insensitiveReserved.put("instanceof", Token.INSTANCEOF);
      _insensitiveReserved.put("const", Token.CONST);
      _insensitiveReserved.put("final", Token.FINAL);
      _insensitiveReserved.put("abstract", Token.ABSTRACT);
      _insensitiveReserved.put("throw", Token.THROW);
      _insensitiveReserved.put("try", Token.TRY);
      _insensitiveReserved.put("catch", Token.CATCH);
      _insensitiveReserved.put("interface", Token.INTERFACE);
      _insensitiveReserved.put("implements", Token.IMPLEMENTS);

      _insensitiveReserved.put("import", Token.IMPORT);
      // backward compatibility issues
      _insensitiveReserved.put("namespace", Token.NAMESPACE);
      _insensitiveReserved.put("use", Token.USE);
   }
}
