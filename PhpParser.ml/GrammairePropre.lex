/*
   +----------------------------------------------------------------------+
   | Zend Engine                                                          |
   +----------------------------------------------------------------------+
   | Copyright (c) 1998-2011 Zend Technologies Ltd. (http://www.zend.com) |
   +----------------------------------------------------------------------+
   | This source file is subject to version 2.00 of the Zend license,     |
   | that is bundled with this package in the file LICENSE, and is        |
   | available through the world-wide-web at the following url:           |
   | http://www.zend.com/license/2_00.txt.                                |
   | If you did not receive a copy of the Zend license and are unable to  |
   | obtain it through the world-wide-web, please send a note to          |
   | license@zend.com so we can mail you a copy immediately.              |
   +----------------------------------------------------------------------+
   | Authors: Marcus Boerger <helly@php.net>                              |
   |          Nuno Lopes <nlopess@php.net>                                |
   |          Scott MacVicar <scottmac@php.net>                           |
   | Flex version authors:                                                |
   |          Andi Gutmans <andi@zend.com>                                |
   |          Zeev Suraski <zeev@zend.com>                                |
   +----------------------------------------------------------------------+
*/

/* compute yyleng before each rule */

| [0-9]+    { LNUM }
| ([0-9]*"."[0-9]+)|([0-9]+"."[0-9]*)    { DNUM }
| (({LNUM}|{DNUM})[eE][+-]?{LNUM})    { EXPONENT_DNUM }
| "0x"[0-9a-fA-F]+    { HNUM }
| [a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*    { LABEL }
| [ \n\r\t]+    { WHITESPACE }
| [ \t]*    { TABS_AND_SPACES }
| [;:,.\[\]()|^&+-/*=%!~$<>?@]    { TOKENS }
| [^]    { ANY_CHAR }
| ("\r"|"\n"|"\r\n"    { NEWLINE }


(*TODO : gérer ça*)
#define IS_LABEL_START(c) (((c) >= 'a' && (c) <= 'z') || ((c) >= 'A' && (c) <= 'Z') || (c) == '_' || (c) >= 0x7F)

#define ZEND_IS_OCT(c)  ((c)>='0' && (c)<='7')
#define ZEND_IS_HEX(c)  (((c)>='0' && (c)<='9') || ((c)>='a' && (c)<='f') || ((c)>='A' && (c)<='F'))

| "exit"	{T_EXIT}

| "die"	{T_EXIT}

| "function"	{T_FUNCTION}

| "const"	{T_CONST}

| "return"	{T_RETURN}

| "try"	{T_TRY}

| "catch"	{T_CATCH}

| "throw"	{T_THROW}

| "if"	{T_IF}

| "elseif"	{T_ELSEIF}

| "endif"	{T_ENDIF}

| "else"	{T_ELSE}

| "while"	{T_WHILE}

| "endwhile"	{T_ENDWHILE}

| "do"	{T_DO}

| "for"	{T_FOR}

| "endfor"	{T_ENDFOR}

| "foreach"	{T_FOREACH}

| "endforeach"	{T_ENDFOREACH}

| "declare"	{T_DECLARE}

| "enddeclare"	{T_ENDDECLARE}

| "instanceof"	{T_INSTANCEOF}

| "as"	{T_AS}

| "switch"	{T_SWITCH}

| "endswitch"	{T_ENDSWITCH}

| "case"	{T_CASE}

| "default"	{T_DEFAULT}

| "break"	{T_BREAK}

| "continue"	{T_CONTINUE}

| "goto"	{T_GOTO}

| "echo"	{T_ECHO}

| "print"	{T_PRINT}

| "class"	{T_CLASS}

| "interface"	{T_INTERFACE}

| "extends"	{T_EXTENDS}

| "implements"	{T_IMPLEMENTS}

| "->"	{T_OBJECT_OPERATOR}


  |  '#' [^'\n']* '\n' { incr_linenum lexbuf; token lexbuf }
  | '\n'            { incr_linenum lexbuf; token lexbuf }
  | [' ' '\t']      { token lexbuf }
| "->"	{T_OBJECT_OPERATOR}

<ST_LOOKING_FOR_PROPERTY>{LABEL} {
	zendlval->type = IS_STRING;
	return T_STRING;
}

<ST_LOOKING_FOR_PROPERTY>{ANY_CHAR} {
	goto restart; //TODO : ATTENTION A CA !!!!
}

| "::"	{T_PAAMAYIM_NEKUDOTAYIM}

| "\\"	{T_NS_SEPARATOR}

| "new"	{T_NEW}

| "clone"	{T_CLONE}

| "var"	{T_VAR}
//TODO : CHECKER SI CA PASSE
| "("{TABS_AND_SPACES}("int"|"integer"){TABS_AND_SPACES}")"	{T_INT_CAST}

| "("{TABS_AND_SPACES}("real"|"double"|"float"){TABS_AND_SPACES}")"	{T_DOUBLE_CAST}

| "("{TABS_AND_SPACES}"string"{TABS_AND_SPACES}")"	{T_STRING_CAST}

| "("{TABS_AND_SPACES}"binary"{TABS_AND_SPACES}")"	{T_STRING_CAST}

| "("{TABS_AND_SPACES}"array"{TABS_AND_SPACES}")"	{T_ARRAY_CAST}

| "("{TABS_AND_SPACES}"object"{TABS_AND_SPACES}")"	{T_OBJECT_CAST}

| "("{TABS_AND_SPACES}("bool"|"boolean"){TABS_AND_SPACES}")"	{T_BOOL_CAST}

| "("{TABS_AND_SPACES}("unset"){TABS_AND_SPACES}")"	{T_UNSET_CAST}

| "eval"	{T_EVAL}

| "include"	{T_INCLUDE}

| "include_once"	{T_INCLUDE_ONCE}

| "require"	{T_REQUIRE}

| "require_once"	{T_REQUIRE_ONCE}

| "namespace"	{T_NAMESPACE}

| "use"	{T_USE}

| "global"	{T_GLOBAL}

| "isset"	{T_ISSET}

| "empty"	{T_EMPTY}

| "__halt_compiler"	{T_HALT_COMPILER}

| "static"	{T_STATIC}

| "abstract"	{T_ABSTRACT}

| "final"	{T_FINAL}

| "private"	{T_PRIVATE}

| "protected"	{T_PROTECTED}

| "public"	{T_PUBLIC}

| "unset"	{T_UNSET}

| "=>"	{T_DOUBLE_ARROW}

| "list"	{T_LIST}

| "array"	{T_ARRAY}

| "++"	{T_INC}

| "--"	{T_DEC}

| "==="	{T_IS_IDENTICAL}

| "!=="	{T_IS_NOT_IDENTICAL}

| "=="	{T_IS_EQUAL}

| "!="|"<>"	{T_IS_NOT_EQUAL}

| "<="	{T_IS_SMALLER_OR_EQUAL}

| ">="	{T_IS_GREATER_OR_EQUAL}

| "+="	{T_PLUS_EQUAL}

| "-="	{T_MINUS_EQUAL}

| "*="	{T_MUL_EQUAL}

| "/="	{T_DIV_EQUAL}

| ".="	{T_CONCAT_EQUAL}

| "%="	{T_MOD_EQUAL}

| "<<="	{T_SL_EQUAL}

| ">>="	{T_SR_EQUAL}

| "&="	{T_AND_EQUAL}

| "|="	{T_OR_EQUAL}

| "^="	{T_XOR_EQUAL}

| "||"	{T_BOOLEAN_OR}

| "&&"	{T_BOOLEAN_AND}

| "OR"	{T_LOGICAL_OR}

| "AND"	{T_LOGICAL_AND}

| "XOR"	{T_LOGICAL_XOR}

| "<<"	{T_SL}

| ">>"	{T_SR}

<ST_IN_SCRIPTING>{TOKENS} {
	return yytext[0];
}


| '{'	{ LBRACKET}


| "${"	{T_DOLLAR_OPEN_CURLY_BRACES}


| '}' { RBRACKET }


<ST_LOOKING_FOR_VARNAME>{LABEL} {
	return T_STRING_VARNAME;
}


<ST_LOOKING_FOR_VARNAME>{ANY_CHAR} {
	yy_push_state(ST_IN_SCRIPTING TSRMLS_CC);
	goto restart;
}


<ST_IN_SCRIPTING>{LNUM} {
			return T_DNUMBER;
	return T_LNUMBER;
}

//TODO faire 2 regexp, une pour les petit hex, une pout les plus gros, qui vont du coup se retrouver être des doubles
<ST_IN_SCRIPTING>{HNUM} {/*0x[0-9a-fA-F]+*/
	char *hex = yytext + 2; /* Skip "0x" */
	int len = yyleng - 2;

	/* Skip any leading 0s */
	while (*hex == '0') {
		hex++;
		len--;
	}

	if (len < SIZEOF_LONG * 2 || (len == SIZEOF_LONG * 2 && *hex <= '7')) {
		zendlval->value.lval = strtol(hex, NULL, 16);
		zendlval->type = IS_LONG;
		return T_LNUMBER;
	} else {
		zendlval->value.dval = zend_hex_strtod(hex, NULL);
		zendlval->type = IS_DOUBLE;
		return T_DNUMBER;
	}
}


let tnum_string = [0]|([1-9][0-9]*
| tnum_string { T_NUM_STRING }

<ST_VAR_OFFSET>{LNUM}|{HNUM} { /* Offset must be treated as a string */

	return T_NUM_STRING;
}

<ST_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {

	return T_DNUMBER;
}

| "__CLASS__" { T_CLASS_C };


| "__FUNCTION__" { T_FUNC_C }

"__METHOD__" { T_METHOD_C }

"__LINE__" { 	 T_LINE }

"__FILE__" { T_FILE }

"__DIR__" { T_DIR }

"__NAMESPACE__" { T_NS_C }

<INITIAL>"<script"{WHITESPACE}+"language"{WHITESPACE}*"="{WHITESPACE}*("php"|"\"php\""|"'php'"){WHITESPACE}*">" {
	YYCTYPE *bracket = zend_memrchr(yytext, '<', yyleng - (sizeof("script language=php>") - 1));

	if (bracket != SCNG(yy_text)) {
		/* Handle previously scanned HTML, as possible <script> tags found are assumed to not be PHP's */
		YYCURSOR = bracket;
		goto inline_html;
	}

	HANDLE_NEWLINES(yytext, yyleng);
	zendlval->value.str.val = yytext; /* no copying - intentional */
	zendlval->value.str.len = yyleng;
	zendlval->type = IS_STRING;
	BEGIN(ST_IN_SCRIPTING);
	return T_OPEN_TAG;
}


<INITIAL>"<%=" {
	if (CG(asp_tags)) {
		zendlval->value.str.val = yytext; /* no copying - intentional */
		zendlval->value.str.len = yyleng;
		zendlval->type = IS_STRING;
		BEGIN(ST_IN_SCRIPTING);
		return T_OPEN_TAG_WITH_ECHO;
	} else {
		goto inline_char_handler;
	}
}


<INITIAL>"<?=" {
	if (CG(short_tags)) {
		zendlval->value.str.val = yytext; /* no copying - intentional */
		zendlval->value.str.len = yyleng;
		zendlval->type = IS_STRING;
		BEGIN(ST_IN_SCRIPTING);
		return T_OPEN_TAG_WITH_ECHO;
	} else {
		goto inline_char_handler;
	}
}


<INITIAL>"<%" {
	if (CG(asp_tags)) {
		zendlval->value.str.val = yytext; /* no copying - intentional */
		zendlval->value.str.len = yyleng;
		zendlval->type = IS_STRING;
		BEGIN(ST_IN_SCRIPTING);
		return T_OPEN_TAG;
	} else {
		goto inline_char_handler;
	}
}


<INITIAL>"<?php"([ \t]|{NEWLINE}) {
	zendlval->value.str.val = yytext; /* no copying - intentional */
	zendlval->value.str.len = yyleng;
	zendlval->type = IS_STRING;
	HANDLE_NEWLINE(yytext[yyleng-1]);
	BEGIN(ST_IN_SCRIPTING);
	return T_OPEN_TAG;
}


<INITIAL>"<?" {
	if (CG(short_tags)) {
		zendlval->value.str.val = yytext; /* no copying - intentional */
		zendlval->value.str.len = yyleng;
		zendlval->type = IS_STRING;
		BEGIN(ST_IN_SCRIPTING);
		return T_OPEN_TAG;
	} else {
		goto inline_char_handler;
	}
}

<INITIAL>{ANY_CHAR} {
	if (YYCURSOR > YYLIMIT) {
		return 0;
	}

inline_char_handler:

	while (1) {
		YYCTYPE *ptr = memchr(YYCURSOR, '<', YYLIMIT - YYCURSOR);

		YYCURSOR = ptr ? ptr + 1 : YYLIMIT;

		if (YYCURSOR < YYLIMIT) {
			switch (*YYCURSOR) {
				case '?':
					if (CG(short_tags) || !strncasecmp(YYCURSOR + 1, "php", 3)) { /* Assume [ \t\n\r] follows "php" */
						break;
					}
					continue;
				case '%':
					if (CG(asp_tags)) {
						break;
					}
					continue;
				case 's':
				case 'S':
					/* Probably NOT an opening PHP <script> tag, so don't end the HTML chunk yet
					 * If it is, the PHP <script> tag rule checks for any HTML scanned before it */
					YYCURSOR--;
					yymore();
				default:
					continue;
			}

			YYCURSOR--;
		}

		break;
	}

inline_html:
	yyleng = YYCURSOR - SCNG(yy_text);

#ifdef ZEND_MULTIBYTE
	if (SCNG(output_filter)) {
		int readsize;
		size_t sz = 0;
		readsize = SCNG(output_filter)((unsigned char **)&(zendlval->value.str.val), &sz, (unsigned char *)yytext, (size_t)yyleng TSRMLS_CC);
		zendlval->value.str.len = sz;
		if (readsize < yyleng) {
			yyless(readsize);
		}
	} else {
	  zendlval->value.str.val = (char *) estrndup(yytext, yyleng);
	  zendlval->value.str.len = yyleng;
	}
#else /* !ZEND_MULTIBYTE */
	zendlval->value.str.val = (char *) estrndup(yytext, yyleng);
 	zendlval->value.str.len = yyleng;
#endif
	zendlval->type = IS_STRING;
	HANDLE_NEWLINES(yytext, yyleng);
	return T_INLINE_HTML;
}


/* Make sure a label character follows "->", otherwise there is no property
 * and "->" will be taken literally
 */
<ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE>"$"{LABEL}"->"[a-zA-Z_\x7f-\xff] {
	yyless(yyleng - 3);
	yy_push_state(ST_LOOKING_FOR_PROPERTY TSRMLS_CC);
	zend_copy_value(zendlval, (yytext+1), (yyleng-1));
	zendlval->type = IS_STRING;
	return T_VARIABLE;
}

/* A [ always designates a variable offset, regardless of what follows
 */
<ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE>"$"{LABEL}"[" {
	yyless(yyleng - 1);
	yy_push_state(ST_VAR_OFFSET TSRMLS_CC);
	zend_copy_value(zendlval, (yytext+1), (yyleng-1));
	zendlval->type = IS_STRING;
	return T_VARIABLE;
}

<ST_IN_SCRIPTING,ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE,ST_VAR_OFFSET>"$"{LABEL} {
	zend_copy_value(zendlval, (yytext+1), (yyleng-1));
	zendlval->type = IS_STRING;
	return T_VARIABLE;
}

<ST_VAR_OFFSET>"]" {
	yy_pop_state(TSRMLS_C);
	return ']';
}

<ST_VAR_OFFSET>{TOKENS}|[{}"`] {
	/* Only '[' can be valid, but returning other tokens will allow a more explicit parse error */
	return yytext[0];
}

<ST_VAR_OFFSET>[ \n\r\t\\'#] {
	/* Invalid rule to return a more explicit parse error with proper line number */
	yyless(0);
	yy_pop_state(TSRMLS_C);
	return T_ENCAPSED_AND_WHITESPACE;
}

<ST_IN_SCRIPTING,ST_VAR_OFFSET>{LABEL} {
	zend_copy_value(zendlval, yytext, yyleng);
	zendlval->type = IS_STRING;
	return T_STRING;
}


<ST_IN_SCRIPTING>"#"|"//" {
	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '\r':
				if (*YYCURSOR == '\n') {
					YYCURSOR++;
				}
				/* fall through */
			case '\n':
				CG(zend_lineno)++;
				break;
			case '%':
				if (!CG(asp_tags)) {
					continue;
				}
				/* fall through */
			case '?':
				if (*YYCURSOR == '>') {
					YYCURSOR--;
					break;
				}
				/* fall through */
			default:
				continue;
		}

		break;
	}

	yyleng = YYCURSOR - SCNG(yy_text);

	return T_COMMENT;
}

<ST_IN_SCRIPTING>"/*"|"/**"{WHITESPACE} {
	int doc_com;

	if (yyleng > 2) {
		doc_com = 1;
		RESET_DOC_COMMENT();
	} else {
		doc_com = 0;
	}

	while (YYCURSOR < YYLIMIT) {
		if (*YYCURSOR++ == '*' && *YYCURSOR == '/') {
			break;
		}
	}

	if (YYCURSOR < YYLIMIT) {
		YYCURSOR++;
	} else {
		zend_error(E_COMPILE_WARNING, "Unterminated comment starting line %d", CG(zend_lineno));
	}

	yyleng = YYCURSOR - SCNG(yy_text);
	HANDLE_NEWLINES(yytext, yyleng);

	if (doc_com) {
		CG(doc_comment) = estrndup(yytext, yyleng);
		CG(doc_comment_len) = yyleng;
		return T_DOC_COMMENT;
	}

	return T_COMMENT;
}

<ST_IN_SCRIPTING>("?>"|"</script"{WHITESPACE}*">"){NEWLINE}? {
	zendlval->value.str.val = yytext; /* no copying - intentional */
	zendlval->value.str.len = yyleng;
	zendlval->type = IS_STRING;
	BEGIN(INITIAL);
	return T_CLOSE_TAG;  /* implicit ';' at php-end tag */
}


<ST_IN_SCRIPTING>"%>"{NEWLINE}? {
	if (CG(asp_tags)) {
		BEGIN(INITIAL);
		zendlval->value.str.len = yyleng;
		zendlval->type = IS_STRING;
		zendlval->value.str.val = yytext; /* no copying - intentional */
		return T_CLOSE_TAG;  /* implicit ';' at php-end tag */
	} else {
		yyless(1);
		return yytext[0];
	}
}


<ST_IN_SCRIPTING>b?['] {
	register char *s, *t;
	char *end;
	int bprefix = (yytext[0] != '\'') ? 1 : 0;

	while (1) {
		if (YYCURSOR < YYLIMIT) {
			if (*YYCURSOR == '\'') {
				YYCURSOR++;
				yyleng = YYCURSOR - SCNG(yy_text);

				break;
			} else if (*YYCURSOR++ == '\\' && YYCURSOR < YYLIMIT) {
				YYCURSOR++;
			}
		} else {
			yyleng = YYLIMIT - SCNG(yy_text);

			/* Unclosed single quotes; treat similar to double quotes, but without a separate token
			 * for ' (unrecognized by parser), instead of old flex fallback to "Unexpected character..."
			 * rule, which continued in ST_IN_SCRIPTING state after the quote */
			return T_ENCAPSED_AND_WHITESPACE;
		}
	}

	zendlval->value.str.val = estrndup(yytext+bprefix+1, yyleng-bprefix-2);
	zendlval->value.str.len = yyleng-bprefix-2;
	zendlval->type = IS_STRING;

	/* convert escape sequences */
	s = t = zendlval->value.str.val;
	end = s+zendlval->value.str.len;
	while (s<end) {
		if (*s=='\\') {
			s++;

			switch(*s) {
				case '\\':
				case '\'':
					*t++ = *s;
					zendlval->value.str.len--;
					break;
				default:
					*t++ = '\\';
					*t++ = *s;
					break;
			}
		} else {
			*t++ = *s;
		}

		if (*s == '\n' || (*s == '\r' && (*(s+1) != '\n'))) {
			CG(zend_lineno)++;
		}
		s++;
	}
	*t = 0;

#ifdef ZEND_MULTIBYTE
	if (SCNG(output_filter)) {
		size_t sz = 0;
		s = zendlval->value.str.val;
		SCNG(output_filter)((unsigned char **)&(zendlval->value.str.val), &sz, (unsigned char *)s, (size_t)zendlval->value.str.len TSRMLS_CC);
		zendlval->value.str.len = sz;
		efree(s);
	}
#endif /* ZEND_MULTIBYTE */
	return T_CONSTANT_ENCAPSED_STRING;
}


<ST_IN_SCRIPTING>b?["] {
	int bprefix = (yytext[0] != '"') ? 1 : 0;

	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '"':
				yyleng = YYCURSOR - SCNG(yy_text);
				zend_scan_escape_string(zendlval, yytext+bprefix+1, yyleng-bprefix-2, '"' TSRMLS_CC);
				return T_CONSTANT_ENCAPSED_STRING;
			case '$':
				if (IS_LABEL_START(*YYCURSOR) || *YYCURSOR == '{') {
					break;
				}
				continue;
			case '{':
				if (*YYCURSOR == '$') {
					break;
				}
				continue;
			case '\\':
				if (YYCURSOR < YYLIMIT) {
					YYCURSOR++;
				}
				/* fall through */
			default:
				continue;
		}

		YYCURSOR--;
		break;
	}

	/* Remember how much was scanned to save rescanning */
	SET_DOUBLE_QUOTES_SCANNED_LENGTH(YYCURSOR - SCNG(yy_text) - yyleng);

	YYCURSOR = SCNG(yy_text) + yyleng;

	BEGIN(ST_DOUBLE_QUOTES);
	return '"';
}


<ST_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}({LABEL}|([']{LABEL}['])|(["]{LABEL}["])){NEWLINE} {
	char *s;
	int bprefix = (yytext[0] != '<') ? 1 : 0;

	/* save old heredoc label */
	Z_STRVAL_P(zendlval) = CG(heredoc);
	Z_STRLEN_P(zendlval) = CG(heredoc_len);

	CG(zend_lineno)++;
	CG(heredoc_len) = yyleng-bprefix-3-1-(yytext[yyleng-2]=='\r'?1:0);
	s = yytext+bprefix+3;
	while ((*s == ' ') || (*s == '\t')) {
		s++;
		CG(heredoc_len)--;
	}

	if (*s == '\'') {
		s++;
		CG(heredoc_len) -= 2;

		BEGIN(ST_NOWDOC);
	} else {
		if (*s == '"') {
			s++;
			CG(heredoc_len) -= 2;
		}

		BEGIN(ST_HEREDOC);
	}

	CG(heredoc) = estrndup(s, CG(heredoc_len));

	/* Check for ending label on the next line */
	if (CG(heredoc_len) < YYLIMIT - YYCURSOR && !memcmp(YYCURSOR, s, CG(heredoc_len))) {
		YYCTYPE *end = YYCURSOR + CG(heredoc_len);

		if (*end == ';') {
			end++;
		}

		if (*end == '\n' || *end == '\r') {
			BEGIN(ST_END_HEREDOC);
		}
	}

	return T_START_HEREDOC;
}


<ST_IN_SCRIPTING>[`] {
	BEGIN(ST_BACKQUOTE);
	return '`';
}


<ST_END_HEREDOC>{ANY_CHAR} {
	YYCURSOR += CG(heredoc_len) - 1;
	yyleng = CG(heredoc_len);

	Z_STRVAL_P(zendlval) = CG(heredoc);
	Z_STRLEN_P(zendlval) = CG(heredoc_len);
	CG(heredoc) = NULL;
	CG(heredoc_len) = 0;
	BEGIN(ST_IN_SCRIPTING);
	return T_END_HEREDOC;
}


<ST_DOUBLE_QUOTES,ST_BACKQUOTE,ST_HEREDOC>"{$" {
	zendlval->value.lval = (long) '{';
	yy_push_state(ST_IN_SCRIPTING TSRMLS_CC);
	yyless(1);
	return T_CURLY_OPEN;
}


<ST_DOUBLE_QUOTES>["] {
	BEGIN(ST_IN_SCRIPTING);
	return '"';
}

<ST_BACKQUOTE>[`] {
	BEGIN(ST_IN_SCRIPTING);
	return '`';
}


<ST_DOUBLE_QUOTES>{ANY_CHAR} {
	if (GET_DOUBLE_QUOTES_SCANNED_LENGTH()) {
		YYCURSOR += GET_DOUBLE_QUOTES_SCANNED_LENGTH() - 1;
		SET_DOUBLE_QUOTES_SCANNED_LENGTH(0);

		goto double_quotes_scan_done;
	}

	if (YYCURSOR > YYLIMIT) {
		return 0;
	}
	if (yytext[0] == '\\' && YYCURSOR < YYLIMIT) {
		YYCURSOR++;
	}

	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '"':
				break;
			case '$':
				if (IS_LABEL_START(*YYCURSOR) || *YYCURSOR == '{') {
					break;
				}
				continue;
			case '{':
				if (*YYCURSOR == '$') {
					break;
				}
				continue;
			case '\\':
				if (YYCURSOR < YYLIMIT) {
					YYCURSOR++;
				}
				/* fall through */
			default:
				continue;
		}

		YYCURSOR--;
		break;
	}

double_quotes_scan_done:
	yyleng = YYCURSOR - SCNG(yy_text);

	zend_scan_escape_string(zendlval, yytext, yyleng, '"' TSRMLS_CC);
	return T_ENCAPSED_AND_WHITESPACE;
}


<ST_BACKQUOTE>{ANY_CHAR} {
	if (YYCURSOR > YYLIMIT) {
		return 0;
	}
	if (yytext[0] == '\\' && YYCURSOR < YYLIMIT) {
		YYCURSOR++;
	}

	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '`':
				break;
			case '$':
				if (IS_LABEL_START(*YYCURSOR) || *YYCURSOR == '{') {
					break;
				}
				continue;
			case '{':
				if (*YYCURSOR == '$') {
					break;
				}
				continue;
			case '\\':
				if (YYCURSOR < YYLIMIT) {
					YYCURSOR++;
				}
				/* fall through */
			default:
				continue;
		}

		YYCURSOR--;
		break;
	}

	yyleng = YYCURSOR - SCNG(yy_text);

	zend_scan_escape_string(zendlval, yytext, yyleng, '`' TSRMLS_CC);
	return T_ENCAPSED_AND_WHITESPACE;
}


<ST_HEREDOC>{ANY_CHAR} {
	int newline = 0;

	if (YYCURSOR > YYLIMIT) {
		return 0;
	}

	YYCURSOR--;

	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '\r':
				if (*YYCURSOR == '\n') {
					YYCURSOR++;
				}
				/* fall through */
			case '\n':
				/* Check for ending label on the next line */
				if (IS_LABEL_START(*YYCURSOR) && CG(heredoc_len) < YYLIMIT - YYCURSOR && !memcmp(YYCURSOR, CG(heredoc), CG(heredoc_len))) {
					YYCTYPE *end = YYCURSOR + CG(heredoc_len);

					if (*end == ';') {
						end++;
					}

					if (*end == '\n' || *end == '\r') {
						/* newline before label will be subtracted from returned text, but
						 * yyleng/yytext will include it, for zend_highlight/strip, tokenizer, etc. */
						if (YYCURSOR[-2] == '\r' && YYCURSOR[-1] == '\n') {
							newline = 2; /* Windows newline */
						} else {
							newline = 1;
						}

						CG(increment_lineno) = 1; /* For newline before label */
						BEGIN(ST_END_HEREDOC);

						goto heredoc_scan_done;
					}
				}
				continue;
			case '$':
				if (IS_LABEL_START(*YYCURSOR) || *YYCURSOR == '{') {
					break;
				}
				continue;
			case '{':
				if (*YYCURSOR == '$') {
					break;
				}
				continue;
			case '\\':
				if (YYCURSOR < YYLIMIT && *YYCURSOR != '\n' && *YYCURSOR != '\r') {
					YYCURSOR++;
				}
				/* fall through */
			default:
				continue;
		}

		YYCURSOR--;
		break;
	}

heredoc_scan_done:
	yyleng = YYCURSOR - SCNG(yy_text);

	zend_scan_escape_string(zendlval, yytext, yyleng - newline, 0 TSRMLS_CC);
	return T_ENCAPSED_AND_WHITESPACE;
}


<ST_NOWDOC>{ANY_CHAR} {
	int newline = 0;

	if (YYCURSOR > YYLIMIT) {
		return 0;
	}

	YYCURSOR--;

	while (YYCURSOR < YYLIMIT) {
		switch (*YYCURSOR++) {
			case '\r':
				if (*YYCURSOR == '\n') {
					YYCURSOR++;
				}
				/* fall through */
			case '\n':
				/* Check for ending label on the next line */
				if (IS_LABEL_START(*YYCURSOR) && CG(heredoc_len) < YYLIMIT - YYCURSOR && !memcmp(YYCURSOR, CG(heredoc), CG(heredoc_len))) {
					YYCTYPE *end = YYCURSOR + CG(heredoc_len);

					if (*end == ';') {
						end++;
					}

					if (*end == '\n' || *end == '\r') {
						/* newline before label will be subtracted from returned text, but
						 * yyleng/yytext will include it, for zend_highlight/strip, tokenizer, etc. */
						if (YYCURSOR[-2] == '\r' && YYCURSOR[-1] == '\n') {
							newline = 2; /* Windows newline */
						} else {
							newline = 1;
						}

						CG(increment_lineno) = 1; /* For newline before label */
						BEGIN(ST_END_HEREDOC);

						goto nowdoc_scan_done;
					}
				}
				/* fall through */
			default:
				continue;
		}
	}

nowdoc_scan_done:
	yyleng = YYCURSOR - SCNG(yy_text);

	zend_copy_value(zendlval, yytext, yyleng - newline);
	zendlval->type = IS_STRING;
	HANDLE_NEWLINES(yytext, yyleng - newline);
	return T_ENCAPSED_AND_WHITESPACE;
}


<ST_IN_SCRIPTING,ST_VAR_OFFSET>{ANY_CHAR} {
	if (YYCURSOR > YYLIMIT) {
		return 0;
	}

	zend_error(E_COMPILE_WARNING,"Unexpected character in input:  '%c' (ASCII=%d) state=%d", yytext[0], yytext[0], YYSTATE);
	goto restart;
}

*/
}
