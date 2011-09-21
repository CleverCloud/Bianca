(*
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
*)
let tabs_and_space = [ '\t']*
let lnum = ['0'-'9']+
let hnum =  "0x" ['0'-'9' 'a'-'f' 'A'-'F']+  
let dnum = (['0'-'9']*"."['0'-'9']+)|(['0'-'9']+"."['0'-'9']*) 
let tnum_string = ['0']|(['1'-'9']['0'-'9']*)
 
let label =  ['a'-'z' 'A'-'Z' '_']['a'-'z' 'A'-'Z' '0'-'9' '_']*
let whitespace =  [ '\n' '\r' '\t']+
let newline = ("\r"|"\n"|"\r\n") 
let tokens = [ ';' ':' ','  '[' ']' '(' ')' '|' '^' '&' '+' '-' '/' '*' '=' '%' '!' '~' '$' '<' '>' '?' '@'] 
rule token = parse
(*| ((LNUM|DNUM)['e' 'E']['+' '-']?LNUM)    { EXPONENT_DNUM }*)
(*| [^]    { ANY_CHAR }*)

(*TODO : gérer ça*)
(*#define IS_LABEL_START(c) (((c) >= 'a' && (c) <= 'z') || ((c) >= 'A' && (c) <= 'Z') || (c) == '_' || (c) >= 0x7F)
#define ZEND_IS_OCT(c)  ((c)>='0' && (c)<='7')
#define ZEND_IS_HEX(c)  (((c)>='0' && (c)<='9') || ((c)>='a' && (c)<='f') || ((c)>='A' && (c)<='F')) *)
| '(' 	{LPARENTH}
| ')' 	{RPARENTH}
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
| ['a'-'z' 'A'-'Z' '_']['a'-'z' 'A'-'Z' '0'-'9' '_']*  { T_STRING}
| "::"	{T_PAAMAYIM_NEKUDOTAYIM}
| "\\"	{T_NS_SEPARATOR}
| "new"	{T_NEW}
| "clone"	{T_CLONE}
| "var"	{T_VAR}
(*TODO : CHECKER SI CA PASSE *)
| "("tabs_and_space("int"|"integer")tabs_and_space")"	{T_INT_CAST}
| "("tabs_and_space("real"|"double"|"float")tabs_and_space")"	{T_DOUBLE_CAST}
| "("tabs_and_space"string"tabs_and_space")"	{T_STRING_CAST}
| "("tabs_and_space"binary"tabs_and_space")"	{T_STRING_CAST}
| "("tabs_and_space"array"tabs_and_space")"	{T_ARRAY_CAST}
| "("tabs_and_space"object"tabs_and_space")"	{T_OBJECT_CAST}
| "("tabs_and_space("bool"|"boolean")tabs_and_space")"	{T_BOOL_CAST}
| "("tabs_and_space("unset")tabs_and_space")"	{T_UNSET_CAST}
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
| '{'	{ LBRACKET}
| "${"	{T_DOLLAR_OPEN_CURLY_BRACES}
| '}' { RBRACKET }
| label { T_STRING_VARNAME }
(*TODO séparer les DOUBLE et les LONG en fonction de la taille
--> Faire une fonction caml pour ça*)
| lnum {T_LNUMBER}
(*
TODO : on verra après pour les doubles
			return T_DNUMBER;
	return T_LNUMBER;
}
*)
(*TODO faire 2 regexp, une pour les petit hex, une pout les plus gros, qui vont du coup se retrouver être des doubles 
> 2^32  -> LONG sinon DOUBLE  *)
(*
<ST_IN_SCRIPTING>{HNUM} {/*0x[0-9a-fA-F]+*/
	char *hex = yytext + 2; /* Skip "0x" */
	int len = yyleng - 2;
	/* Skip any leading 0s */
	while ( *hex == '0') {
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
*)
| tnum_string { T_NUM_STRING }
(*
<ST_VAR_OFFSET>{LNUM}|{HNUM} { /* Offset must be treated as a string */
	return T_NUM_STRING;
}
<ST_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {
	return T_DNUMBER;
}
*)
| "__CLASS__" { T_CLASS_C }
| "__FUNCTION__" { T_FUNC_C }
| "__METHOD__" { T_METHOD_C }
| "__LINE__" { 	 T_LINE }
| "__FILE__" { T_FILE }
| "__DIR__" { T_DIR }
| "__NAMESPACE__" { T_NS_C }
|  "<script" whitespace  "language"  whitespace* "=" whitespace* ("php"|"\"php\""|"'php'") whitespace ">"     { T_OPEN_TAG }
(*
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
*)
| "<?="  {T_OPEN_TAG_WITH_ECHO }
(*
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
*)
| "<?php"([ '\t']|newline) { T_OPEN_TAG }
| "<?" {  T_OPEN_TAG }
(* Make sure a label character follows "->", otherwise there is no property
 * and "->" will be taken literally
 *)
| "$"label"->"['a'-'z' 'A'-'Z' '_' '\x7f'-'\xff'] { T_VARIABLE}
(* A [ always designates a variable offset, regardless of what follows
 *)
| "$" label "[" { T_VARIABLE }
| "$" label { T_VARIABLE }
| "]" {	 DCROCHET }
(*$| tokens|['{' '}' '"' '`'] { ????}*)
| [ '\n' '\r' '\t' '\\' '\'' '#'] { T_ENCAPSED_AND_WHITESPACE }
| label { T_STRING }
| "#"|"//" {	 T_COMMENT}
| "/*"|"/**" whitespace { T_COMMENT }
| ("?>"|"</script"whitespace*">") newline? {T_CLOSE_TAG }
| "%>" newline ? { T_CLOSE_TAG}
(*b?['] {(* Unclosed single quotes; treat similar to double quotes, but without a separate token
			 * for ' (unrecognized by parser), instead of old flex fallback to "Unexpected character..."
			 * rule, which continued in ST_IN_SCRIPTING state after the quote *)
			return T_ENCAPSED_AND_WHITESPACE	}
 OU  T_CONSTANT_ENCAPSED_STRING;
}
*)
(*
<ST_IN_SCRIPTING>b?["] {
	return '"';
}
<ST_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}(label|([']{LABEL}['])|(["]{LABEL}["])){NEWLINE} {
	return T_START_HEREDOC;
}
*)
|"{$" { T_CURLY_OPEN}
| ['"'] { DOUBLE_QUOTE }
| ['`']   { QUOTEINVERSE }

