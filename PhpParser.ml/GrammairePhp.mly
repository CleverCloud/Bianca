%{
open Syntax
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
   | Authors: Andi Gutmans <andi@zend.com>                                |
   |          Zeev Suraski <zeev@zend.com>                                |
   +----------------------------------------------------------------------+



TODO : Réactiver le HEREDOC, quand la grammaire marchera...

  LALR shift/reduce conflicts and how they are resolved:
 
  - 2 shift/reduce conflicts due to the dangling elseif/else ambiguity. Solved by shift.
 
 

*)


%}


%left T_INCLUDE T_INCLUDE_ONCE T_EVAL T_REQUIRE T_REQUIRE_ONCE
%left VIRGULE
%left T_LOGICAL_OR
%left T_LOGICAL_XOR
%left T_LOGICAL_AND
%right T_PRINT
%left EQUAL T_PLUS_EQUAL T_MINUS_EQUAL T_MUL_EQUAL T_DIV_EQUAL T_CONCAT_EQUAL T_MOD_EQUAL T_AND_EQUAL T_OR_EQUAL T_XOR_EQUAL T_SL_EQUAL T_SR_EQUAL
%left INTEROG DOUBLEPOINT DOUBLE_QUOTE  DOUBLE
%left T_BOOLEAN_OR
%left T_BOOLEAN_AND
%left PIPE
%left CHAPEAU
%left ETCOMMERCIAL
%nonassoc T_IS_EQUAL T_IS_NOT_EQUAL T_IS_IDENTICAL T_IS_NOT_IDENTICAL
%nonassoc INF T_IS_SMALLER_OR_EQUAL SUP T_IS_GREATER_OR_EQUAL
%left T_SL T_SR
%left PLUS MINUS POINT
%left MULT SLASH PERCENT
%right EXCLAM
%nonassoc T_INSTANCEOF
%right TILDE T_INC T_DEC T_INT_CAST T_DOUBLE_CAST T_STRING_CAST T_ARRAY_CAST T_OBJECT_CAST T_BOOL_CAST T_UNSET_CAST T_AT
%right GCROCHET
%nonassoc T_NEW T_CLONE
%token T_EXIT
%token T_IF
%left T_ELSEIF
%left T_ELSE
%left T_ENDIF
%token LBRACKET RBRACKET GCROCHET DCROCHET BACKQUOTE POINT_VIRGULE DOLLAR DOUBLEQUOTE DOUBLEPOINT LPARENTH RPARENTH VIRGULE EQUAL T_ABSTRACT T_FINAL
%token T_LNUMBER T_STATIC QUOTE
%token T_DNUMBER T_ELSE T_ELSEIF ETCOMMERCIAL T_NEW T_CLONE T_PLUS_EQUAL T_MINUS_EQUAL T_MUL_EQUAL T_MOD_EQUAL T_DIV_EQUAL T_CONCAT_EQUAL T_AND_EQUAL
%token T_OR_EQUAL T_XOR_EQUAL T_SL_EQUAL T_SR_EQUAL T_INC T_DEC T_BOOLEAN_OR T_BOOLEAN_AND T_LOGICAL_OR T_LOGICAL_AND T_LOGICAL_XOR 
%token <string>T_STRING
%token T_STRING_VARNAME PIPE CHAPEAU POINT PLUS MINUS MULT SLASH PERCENT INF SUP INTEROG T_AT
%token T_VARIABLE T_SL T_SR EXCLAM  TILDE T_IS_IDENTICAL T_IS_NOT_IDENTICAL T_IS_EQUAL T_IS_NOT_EQUAL T_IS_SMALLER_OR_EQUAL T_IS_GREATER_OR_EQUAL 
%token T_NUM_STRING T_INSTANCEOF T_INT_CAST T_DOUBLE_CAST T_STRING_CAST T_ARRAY_CAST T_OBJECT_CAST T_BOOL_CAST T_UNSET_CAST 
%token T_INLINE_HTML T_PRINT T_INCLUDE T_INCLUDE_ONCE T_EVAL T_REQUIRE T_REQUIRE_ONCE T_PUBLIC T_PROTECTED T_PRIVATE T_ENDIF 
%token T_CHARACTER
%token T_BAD_CHARACTER
%token T_ENCAPSED_AND_WHITESPACE
%token T_CONSTANT_ENCAPSED_STRING
%token T_ECHO
%token T_DO
%token T_WHILE
%token T_ENDWHILE
%token T_FOR
%token T_ENDFOR
%token T_FOREACH
%token T_ENDFOREACH
%token T_DECLARE
%token T_ENDDECLARE
%token T_AS
%token T_SWITCH
%token T_ENDSWITCH
%token T_CASE
%token T_DEFAULT
%token T_BREAK
%token T_CONTINUE
%token T_GOTO
%token T_FUNCTION
%token T_CONST
%token T_RETURN
%token T_TRY
%token T_CATCH
%token T_THROW
%token T_USE
%token T_GLOBAL
%right T_STATIC T_ABSTRACT T_FINAL T_PRIVATE T_PROTECTED T_PUBLIC
%token T_VAR
%token T_UNSET
%token T_ISSET
%token T_EMPTY
%token T_HALT_COMPILER
%token T_CLASS
%token T_INTERFACE
%token T_EXTENDS
%token T_IMPLEMENTS
%token T_OBJECT_OPERATOR
%token T_DOUBLE_ARROW
%token T_LIST
%token T_ARRAY
%token T_CLASS_C
%token T_METHOD_C
%token T_FUNC_C
%token T_LINE
%token T_FILE
%token T_COMMENT
%token T_DOC_COMMENT
%token T_OPEN_TAG
%token T_OPEN_TAG_WITH_ECHO
%token T_CLOSE_TAG
%token T_WHITESPACE
%token T_START_HEREDOC
%token T_END_HEREDOC
%token T_DOLLAR_OPEN_CURLY_BRACES
%token T_CURLY_OPEN
%token T_PAAMAYIM_NEKUDOTAYIM
%token T_NAMESPACE
%token T_NS_C
%token T_DIR
%token T_NS_SEPARATOR

%start start
%type <Syntax.php> start
%% /* Rules */


start:
        top_statement_list { Root([])}	
        

top_statement_list:
                top_statement_list   top_statement { print_endline "top_statement_list"} 

                

namespace_name:
/*   T_STRING_VARNAME { print_endline "node"}*/
	| namespace_name T_NS_SEPARATOR T_STRING_VARNAME  {print_endline "namespace_name T_NS_SEPARATOR T_STRING_VARNAME " };




top_statement:
          statement						{ print_endline "node"}
	| function_declaration_statement	 {print_endline "function_declaration_statement	" }
	| class_declaration_statement		 {print_endline "class_declaration_statement		" }
	| T_HALT_COMPILER LPARENTH RPARENTH POINT_VIRGULE		 {print_endline "T_HALT_COMPILER LPARENTH RPARENTH POINT_VIRGULE		" }
/*	| T_NAMESPACE namespace_name POINT_VIRGULE	 {print_endline "T_NAMESPACE namespace_name POINT_VIRGULE	" }
	| T_NAMESPACE namespace_name LBRACKET top_statement_list RBRACKET  {print_endline "T_NAMESPACE namespace_name LBRACKET top_statement_list RBRACKET " }	   
	| T_NAMESPACE LBRACKET		top_statement_list RBRACKET	 {print_endline "T_NAMESPACE LBRACKET		top_statement_list RBRACKET	" }
	| T_USE use_declarations POINT_VIRGULE      {print_endline "T_USE use_declarations POINT_VIRGULE     " } */
	| constant_declaration POINT_VIRGULE		 {print_endline "constant_declaration POINT_VIRGULE		" };
/*
use_declarations:
          use_declarations VIRGULE use_declaration { print_endline "node"}
	| use_declaration  {print_endline "use_declaration " };

use_declaration:
          namespace_name 			{ print_endline "node"}
	| namespace_name T_AS T_STRING	 {print_endline "namespace_name T_AS T_STRING	" }
	| T_NS_SEPARATOR namespace_name  {print_endline "T_NS_SEPARATOR namespace_name " }
	| T_NS_SEPARATOR namespace_name T_AS T_STRING  {print_endline "T_NS_SEPARATOR namespace_name T_AS T_STRING " }; */

constant_declaration:
          constant_declaration VIRGULE T_STRING EQUAL static_scalar	 { print_endline "node"}
	| T_CONST T_STRING EQUAL static_scalar   {print_endline "T_CONST T_STRING EQUAL static_scalar  " };

inner_statement_list:
          inner_statement_list   inner_statement { print_endline "node"}
          ;


inner_statement:
                  statement { print_endline "node"}
	| function_declaration_statement  {print_endline "function_declaration_statement " }
	| class_declaration_statement  {print_endline "class_declaration_statement " }
	| T_HALT_COMPILER LPARENTH RPARENTH POINT_VIRGULE    {print_endline "T_HALT_COMPILER LPARENTH RPARENTH POINT_VIRGULE   " };


statement:
          unticked_statement { print_endline "node"}
	| T_STRING DOUBLEPOINT  {print_endline "T_STRING DOUBLEPOINT " };

unticked_statement:
        | LBRACKET inner_statement_list RBRACKET {print_endline "code entre bracket" }
	| T_IF LPARENTH expr RPARENTH  statement  elseif_list else_single  {print_endline "T_IF LPARENTH expr RPARENTH  statement  elseif_list else_single " }
	| T_IF LPARENTH expr RPARENTH DOUBLEPOINT  inner_statement_list  new_elseif_list new_else_single T_ENDIF POINT_VIRGULE  {print_endline "T_IF LPARENTH expr RPARENTH DOUBLEPOINT  inner_statement_list  new_elseif_list new_else_single T_ENDIF POINT_VIRGULE " }
	| T_WHILE LPARENTH  expr  RPARENTH  while_statement  {print_endline "T_WHILE LPARENTH  expr  RPARENTH  while_statement " }
	| T_DO  statement T_WHILE LPARENTH  expr RPARENTH POINT_VIRGULE  {print_endline "T_DO  statement T_WHILE LPARENTH  expr RPARENTH POINT_VIRGULE " }
	| T_FOR			LPARENTH				for_expr			POINT_VIRGULE 				for_expr			POINT_VIRGULE 				for_expr			RPARENTH 			for_statement  {print_endline "T_FOR			LPARENTH				for_expr			POINT_VIRGULE 				for_expr			POINT_VIRGULE 				for_expr			RPARENTH 			for_statement " }
	| T_SWITCH LPARENTH expr RPARENTH	 switch_case_list  {print_endline "T_SWITCH LPARENTH expr RPARENTH	 switch_case_list " } 
	| T_BREAK POINT_VIRGULE				 {print_endline "T_BREAK POINT_VIRGULE				" }
	| T_BREAK expr POINT_VIRGULE		 {print_endline "T_BREAK expr POINT_VIRGULE		" }
	| T_CONTINUE POINT_VIRGULE			 {print_endline "T_CONTINUE POINT_VIRGULE			" }
	| T_CONTINUE expr POINT_VIRGULE		 {print_endline "T_CONTINUE expr POINT_VIRGULE		" }
	| T_RETURN POINT_VIRGULE						 {print_endline "T_RETURN POINT_VIRGULE						" }
	| T_RETURN expr_without_variable POINT_VIRGULE	 {print_endline "T_RETURN expr_without_variable POINT_VIRGULE	" }
	| T_RETURN variable POINT_VIRGULE				 {print_endline "T_RETURN variable POINT_VIRGULE				" }
	| T_GLOBAL global_var_list POINT_VIRGULE {print_endline "T_GLOBAL global_var_list POINT_VIRGULE" }
	| T_STATIC static_var_list POINT_VIRGULE {print_endline "T_STATIC static_var_list POINT_VIRGULE" }
        |	T_ECHO echo_expr_list POINT_VIRGULE { print_endline "echo"}
	| T_INLINE_HTML			 {print_endline "T_INLINE_HTML			" }
	| expr POINT_VIRGULE				 {print_endline "expr POINT_VIRGULE				" }
	| T_UNSET LPARENTH unset_variables RPARENTH POINT_VIRGULE {print_endline "T_UNSET LPARENTH unset_variables RPARENTH POINT_VIRGULE" }
	| T_FOREACH LPARENTH variable T_AS 	foreach_variable foreach_optional_arg RPARENTH foreach_statement  {print_endline "T_FOREACH LPARENTH variable T_AS 	foreach_variable foreach_optional_arg RPARENTH foreach_statement " } 
	| T_FOREACH LPARENTH expr_without_variable T_AS	variable foreach_optional_arg RPARENTH 		foreach_statement  {print_endline "T_FOREACH LPARENTH expr_without_variable T_AS	variable foreach_optional_arg RPARENTH 		foreach_statement " }
	| T_DECLARE  LPARENTH declare_list RPARENTH declare_statement  {print_endline "T_DECLARE  LPARENTH declare_list RPARENTH declare_statement " }
        |	POINT_VIRGULE		/* empty statement */ { print_endline "node"}
	| T_TRY LBRACKET inner_statement_list LBRACKET  	T_CATCH LPARENTH 		fully_qualified_class_name 		T_VARIABLE RPARENTH 		LBRACKET inner_statement_list LBRACKET 		additional_catches  {print_endline "T_TRY LBRACKET inner_statement_list LBRACKET  	T_CATCH LPARENTH 		fully_qualified_class_name 		T_VARIABLE RPARENTH 		LBRACKET inner_statement_list LBRACKET 		additional_catches " }
	| T_THROW expr POINT_VIRGULE  {print_endline "T_THROW expr POINT_VIRGULE " } 
	| T_GOTO T_STRING POINT_VIRGULE  {print_endline "T_GOTO T_STRING POINT_VIRGULE " };


additional_catches:
                non_empty_additional_catches { print_endline "node"} 
                ;

non_empty_additional_catches:
                        additional_catch { print_endline "node"}
	| non_empty_additional_catches additional_catch  {print_endline "non_empty_additional_catches additional_catch " };


additional_catch:
          T_CATCH LPARENTH fully_qualified_class_name  T_VARIABLE RPARENTH  LBRACKET inner_statement_list LBRACKET { print_endline "node"}
          ;


unset_variables:
                  unset_variable { print_endline "node"}
	| unset_variables VIRGULE unset_variable  {print_endline "unset_variables VIRGULE unset_variable " };

unset_variable:
          variable	{ print_endline "node"}
          ;

function_declaration_statement:
                  unticked_function_declaration_statement	{ print_endline "node"}
                  ;

class_declaration_statement:
                          unticked_class_declaration_statement	{ print_endline "node"}
                          ;


is_reference:
  	ETCOMMERCIAL	{ print_endline "node"}		;


unticked_function_declaration_statement:
          function_php is_reference T_STRING 
          LPARENTH parameter_list RPARENTH LBRACKET inner_statement_list LBRACKET { print_endline "node"}
          ;

unticked_class_declaration_statement:
        class_entry_type T_STRING extends_from	implements_list			LBRACKET				class_statement_list	RBRACKET { print_endline "node"}
	| interface_entry T_STRING						interface_extends_list			LBRACKET class_statement_list RBRACKET  {print_endline "interface_entry T_STRING						interface_extends_list			LBRACKET class_statement_list RBRACKET " } ;


class_entry_type:
          T_CLASS			 { print_endline "node"}
	| T_ABSTRACT T_CLASS   {print_endline "T_ABSTRACT T_CLASS  " }
	| T_FINAL T_CLASS   {print_endline "T_FINAL T_CLASS  " };

extends_from:
        {print_endline "règle si  vide ?"}				
	| T_EXTENDS fully_qualified_class_name   {print_endline "T_EXTENDS fully_qualified_class_name  " }	;

interface_entry:
          T_INTERFACE		 { print_endline "node"}
          ;

interface_extends_list:
                  /* empty */
	| T_EXTENDS interface_list   {print_endline "T_EXTENDS interface_list  " };

implements_list:
          /* empty */
	| T_IMPLEMENTS interface_list  {print_endline "T_IMPLEMENTS interface_list " };

interface_list:
          fully_qualified_class_name			 { print_endline "node"}
	| interface_list VIRGULE fully_qualified_class_name   {print_endline "interface_list VIRGULE fully_qualified_class_name  " };

foreach_optional_arg:
          /* empty */						
	| T_DOUBLE_ARROW foreach_variable	  {print_endline "T_DOUBLE_ARROW foreach_variable	 " };


foreach_variable:
          variable		 { print_endline "node"}	
	| ETCOMMERCIAL variable	  {print_endline "ETCOMMERCIAL variable	 " }	;

for_statement:
          statement  { print_endline "node"}
	| DOUBLEPOINT inner_statement_list T_ENDFOR POINT_VIRGULE   {print_endline "DOUBLEPOINT inner_statement_list T_ENDFOR POINT_VIRGULE  " };


foreach_statement:
          statement  { print_endline "node"}
	| DOUBLEPOINT inner_statement_list T_ENDFOREACH POINT_VIRGULE {print_endline "DOUBLEPOINT inner_statement_list T_ENDFOREACH POINT_VIRGULE" };


declare_statement:
          statement  { print_endline "node"}
	| DOUBLEPOINT inner_statement_list T_ENDDECLARE POINT_VIRGULE {print_endline "DOUBLEPOINT inner_statement_list T_ENDDECLARE POINT_VIRGULE" };


declare_list:
          T_STRING EQUAL static_scalar			 { print_endline "node"}		
	| declare_list VIRGULE T_STRING EQUAL static_scalar	 {print_endline "declare_list VIRGULE T_STRING EQUAL static_scalar	" };


switch_case_list:
          LBRACKET case_list RBRACKET				 { print_endline "node"}	
	| LBRACKET POINT_VIRGULE case_list RBRACKET					  {print_endline "LBRACKET POINT_VIRGULE case_list RBRACKET					 " }
	| DOUBLEPOINT case_list T_ENDSWITCH POINT_VIRGULE	  {print_endline "DOUBLEPOINT case_list T_ENDSWITCH POINT_VIRGULE	 " }	
	| DOUBLEPOINT POINT_VIRGULE case_list T_ENDSWITCH POINT_VIRGULE	 {print_endline "DOUBLEPOINT POINT_VIRGULE case_list T_ENDSWITCH POINT_VIRGULE	" };


case_list:
          /* empty */	
	| case_list T_CASE expr case_separator  inner_statement_list   {print_endline "case_list T_CASE expr case_separator  inner_statement_list  " }
	| case_list T_DEFAULT case_separator  inner_statement_list  {print_endline "case_list T_DEFAULT case_separator  inner_statement_list " };


case_separator:
          DOUBLEPOINT  { print_endline "node"}
	| POINT_VIRGULE {print_endline "POINT_VIRGULE" };


while_statement:
          statement  { print_endline "node"}
	| DOUBLEPOINT inner_statement_list T_ENDWHILE POINT_VIRGULE {print_endline "DOUBLEPOINT inner_statement_list T_ENDWHILE POINT_VIRGULE" };



elseif_list:
          /* empty */
	| elseif_list T_ELSEIF LPARENTH expr RPARENTH  statement  {print_endline "elseif_list T_ELSEIF LPARENTH expr RPARENTH  statement " };


new_elseif_list:
          /* empty */
	| new_elseif_list T_ELSEIF LPARENTH expr RPARENTH DOUBLEPOINT  inner_statement_list  {print_endline "new_elseif_list T_ELSEIF LPARENTH expr RPARENTH DOUBLEPOINT  inner_statement_list " };


else_single:
          /* empty */
	| T_ELSE statement {print_endline "T_ELSE statement" };


new_else_single:
          /* empty */
	| T_ELSE DOUBLEPOINT inner_statement_list {print_endline "T_ELSE DOUBLEPOINT inner_statement_list" };


parameter_list:
          non_empty_parameter_list  { print_endline "node"}
  |	/* empty */{ print_endline "node"};


non_empty_parameter_list:
          optional_class_type T_VARIABLE				 { print_endline "node"}
	| optional_class_type ETCOMMERCIAL T_VARIABLE	  {print_endline "optional_class_type ETCOMMERCIAL T_VARIABLE	 " }		
	| optional_class_type ETCOMMERCIAL T_VARIABLE EQUAL static_scalar			 {print_endline "optional_class_type ETCOMMERCIAL T_VARIABLE EQUAL static_scalar			" }
	| optional_class_type T_VARIABLE EQUAL static_scalar				 {print_endline "optional_class_type T_VARIABLE EQUAL static_scalar				" }
	| non_empty_parameter_list VIRGULE optional_class_type T_VARIABLE 	 {print_endline "non_empty_parameter_list VIRGULE optional_class_type T_VARIABLE 	" }
	| non_empty_parameter_list VIRGULE optional_class_type ETCOMMERCIAL T_VARIABLE	 {print_endline "non_empty_parameter_list VIRGULE optional_class_type ETCOMMERCIAL T_VARIABLE	" }
	| non_empty_parameter_list VIRGULE optional_class_type ETCOMMERCIAL T_VARIABLE	 EQUAL static_scalar  {print_endline "non_empty_parameter_list VIRGULE optional_class_type ETCOMMERCIAL T_VARIABLE	 EQUAL static_scalar " }
	| non_empty_parameter_list VIRGULE optional_class_type T_VARIABLE EQUAL static_scalar 	 {print_endline "non_empty_parameter_list VIRGULE optional_class_type T_VARIABLE EQUAL static_scalar 	" };


optional_class_type:
          /* empty */					
	| fully_qualified_class_name	 {print_endline "fully_qualified_class_name	" }
	| T_ARRAY						 {print_endline "T_ARRAY						" };


function_call_parameter_list:
          non_empty_function_call_parameter_list	{ print_endline "node"}
  |	/* empty */				{ print_endline "node"};


non_empty_function_call_parameter_list:
          expr_without_variable { print_endline "node"	}
	| variable				 {print_endline "variable				" }
	| ETCOMMERCIAL w_variable 				 {print_endline "ETCOMMERCIAL w_variable 				" }
	| non_empty_function_call_parameter_list VIRGULE expr_without_variable	 {print_endline "non_empty_function_call_parameter_list VIRGULE expr_without_variable	" }
	| non_empty_function_call_parameter_list VIRGULE variable					 {print_endline "non_empty_function_call_parameter_list VIRGULE variable					" }
	| non_empty_function_call_parameter_list VIRGULE ETCOMMERCIAL w_variable			 {print_endline "non_empty_function_call_parameter_list VIRGULE ETCOMMERCIAL w_variable			" };

global_var_list:
          global_var_list VIRGULE global_var	{ print_endline "node"}
  |	global_var						{ print_endline "node" }


global_var:
          T_VARIABLE				{ print_endline "node"} 
	| DOLLAR r_variable		 {print_endline "DOLLAR r_variable		" }
	| DOLLAR LBRACKET expr RBRACKET  {print_endline "DOLLAR LBRACKET expr RBRACKET " };


static_var_list:
          static_var_list VIRGULE T_VARIABLE { print_endline "node"}
	| static_var_list VIRGULE T_VARIABLE EQUAL static_scalar  {print_endline "static_var_list VIRGULE T_VARIABLE EQUAL static_scalar " }
	| T_VARIABLE   {print_endline "T_VARIABLE  " }
	| T_VARIABLE EQUAL static_scalar  {print_endline "T_VARIABLE EQUAL static_scalar " }

  ;


class_statement_list:
          class_statement_list class_statement { print_endline "node"}
  |	/* empty */{ print_endline "node"}


class_statement:
          variable_modifiers  class_variable_declaration POINT_VIRGULE { print_endline "node"}
	| class_constant_declaration POINT_VIRGULE {print_endline "class_constant_declaration POINT_VIRGULE" }
	| method_modifiers function_php is_reference T_STRING  LPARENTH			parameter_list RPARENTH method_body  {print_endline "method_modifiers function_php is_reference T_STRING  LPARENTH			parameter_list RPARENTH method_body " }


method_body:
          POINT_VIRGULE /* abstract method */		{ print_endline "node"} 	
	| RBRACKET	inner_statement_list LBRACKET  {print_endline "RBRACKET	inner_statement_list LBRACKET " }

variable_modifiers:
          non_empty_member_modifiers		{ print_endline "node"}
	| T_VAR							 {print_endline "T_VAR							" }

method_modifiers:
          /* empty */							
	| non_empty_member_modifiers			  {print_endline "non_empty_member_modifiers			 " }

non_empty_member_modifiers:
          member_modifier				{ print_endline "node"}		
	| non_empty_member_modifiers member_modifier	 {print_endline "non_empty_member_modifiers member_modifier	" }

member_modifier:
          T_PUBLIC					{ print_endline "node"} 
	| T_PROTECTED				 {print_endline "T_PROTECTED				" }
	| T_PRIVATE				 {print_endline "T_PRIVATE				" }
	| T_STATIC				 {print_endline "T_STATIC				" }
	| T_ABSTRACT				 {print_endline "T_ABSTRACT				" }
	| T_FINAL					 {print_endline "T_FINAL					" }

class_variable_declaration:
          class_variable_declaration VIRGULE T_VARIABLE			{ print_endline "node"}		
	| class_variable_declaration VIRGULE T_VARIABLE EQUAL static_scalar	 {print_endline "class_variable_declaration VIRGULE T_VARIABLE EQUAL static_scalar	" }
	| T_VARIABLE						 {print_endline "T_VARIABLE						" }
	| T_VARIABLE EQUAL static_scalar	 {print_endline "T_VARIABLE EQUAL static_scalar	" }

class_constant_declaration:
          class_constant_declaration VIRGULE T_STRING EQUAL static_scalar	 { print_endline "node"}
	| T_CONST T_STRING EQUAL static_scalar	 {print_endline "T_CONST T_STRING EQUAL static_scalar	" }

echo_expr_list:
          echo_expr_list VIRGULE expr  { print_endline "node"}
	| expr					 {print_endline "expr					" }


for_expr:
          /* empty */			
	| non_empty_for_expr	 {print_endline "non_empty_for_expr	" }

non_empty_for_expr:
          non_empty_for_expr VIRGULE	 expr  { print_endline "node"}
	| expr					 {print_endline "expr					" }

expr_without_variable:
          T_LIST LPARENTH  assignment_list RPARENTH EQUAL expr { print_endline "node"}
	| variable EQUAL expr		 {print_endline "variable EQUAL expr		" }
	| variable EQUAL ETCOMMERCIAL variable  {print_endline "variable EQUAL ETCOMMERCIAL variable " }
	| variable EQUAL ETCOMMERCIAL T_NEW class_name_reference  ctor_arguments  {print_endline "variable EQUAL ETCOMMERCIAL T_NEW class_name_reference  ctor_arguments " }
	| T_NEW class_name_reference  ctor_arguments  {print_endline "T_NEW class_name_reference  ctor_arguments " }
	| T_CLONE expr  {print_endline "T_CLONE expr " }
	| variable T_PLUS_EQUAL expr 	 {print_endline "variable T_PLUS_EQUAL expr 	" }
	| variable T_MINUS_EQUAL expr	 {print_endline "variable T_MINUS_EQUAL expr	" }
	| variable T_MUL_EQUAL expr		 {print_endline "variable T_MUL_EQUAL expr		" }
	| variable T_DIV_EQUAL expr		 {print_endline "variable T_DIV_EQUAL expr		" }
	| variable T_CONCAT_EQUAL expr	 {print_endline "variable T_CONCAT_EQUAL expr	" }
	| variable T_MOD_EQUAL expr		 {print_endline "variable T_MOD_EQUAL expr		" }
	| variable T_AND_EQUAL expr		 {print_endline "variable T_AND_EQUAL expr		" }
	| variable T_OR_EQUAL expr 		 {print_endline "variable T_OR_EQUAL expr 		" }
	| variable T_XOR_EQUAL expr 		 {print_endline "variable T_XOR_EQUAL expr 		" }
	| variable T_SL_EQUAL expr	 {print_endline "variable T_SL_EQUAL expr	" }
	| variable T_SR_EQUAL expr	 {print_endline "variable T_SR_EQUAL expr	" }
	| rw_variable T_INC  {print_endline "rw_variable T_INC " }
	| T_INC rw_variable  {print_endline "T_INC rw_variable " }
	| rw_variable T_DEC  {print_endline "rw_variable T_DEC " }
	| T_DEC rw_variable  {print_endline "T_DEC rw_variable " }
	| expr T_BOOLEAN_OR  expr  {print_endline "expr T_BOOLEAN_OR  expr " }
	| expr T_BOOLEAN_AND  expr  {print_endline "expr T_BOOLEAN_AND  expr " }
	| expr T_LOGICAL_OR  expr  {print_endline "expr T_LOGICAL_OR  expr " }
	| expr T_LOGICAL_AND  expr  {print_endline "expr T_LOGICAL_AND  expr " }
	| expr T_LOGICAL_XOR expr  {print_endline "expr T_LOGICAL_XOR expr " }
	| expr PIPE expr	 {print_endline "expr PIPE expr	" }
	| expr ETCOMMERCIAL expr	 {print_endline "expr ETCOMMERCIAL expr	" }
	| expr CHAPEAU expr	 {print_endline "expr CHAPEAU expr	" }
	| expr POINT expr 	 {print_endline "expr POINT expr 	" }
	| expr PLUS expr 	 {print_endline "expr PLUS expr 	" }
	| expr MINUS expr 	 {print_endline "expr MINUS expr 	" }
	| expr MULT expr	 {print_endline "expr MULT expr	" }
	| expr SLASH expr	 {print_endline "expr SLASH expr	" }
	| expr PERCENT expr 	 {print_endline "expr PERCENT expr 	" }
	| expr T_SL expr	 {print_endline "expr T_SL expr	" }
	| expr T_SR expr	 {print_endline "expr T_SR expr	" }
  |	PLUS expr %prec T_INC  { print_endline "node"}
  |	MINUS expr %prec T_INC  { print_endline "node"}
	| EXCLAM expr  {print_endline "EXCLAM expr " }
	| TILDE expr  {print_endline "TILDE expr " }
	| expr T_IS_IDENTICAL expr		 {print_endline "expr T_IS_IDENTICAL expr		" }
	| expr T_IS_NOT_IDENTICAL expr	 {print_endline "expr T_IS_NOT_IDENTICAL expr	" }
	| expr T_IS_EQUAL expr			 {print_endline "expr T_IS_EQUAL expr			" }
	| expr T_IS_NOT_EQUAL expr 		 {print_endline "expr T_IS_NOT_EQUAL expr 		" }
	| expr INF expr 					 {print_endline "expr INF expr 					" }
	| expr T_IS_SMALLER_OR_EQUAL expr  {print_endline "expr T_IS_SMALLER_OR_EQUAL expr " }
	| expr SUP expr 					 {print_endline "expr SUP expr 					" }
	| expr T_IS_GREATER_OR_EQUAL expr  {print_endline "expr T_IS_GREATER_OR_EQUAL expr " }
	| expr T_INSTANCEOF class_name_reference  {print_endline "expr T_INSTANCEOF class_name_reference " }
	| LPARENTH expr RPARENTH 	 {print_endline "LPARENTH expr RPARENTH 	" }
	| expr INTEROG 		expr DOUBLEPOINT 		expr	  {print_endline "expr INTEROG 		expr DOUBLEPOINT 		expr	 " }
	| expr INTEROG DOUBLEPOINT 		expr      {print_endline "expr INTEROG DOUBLEPOINT 		expr     " }
	| internal_functions_in_yacc  {print_endline "internal_functions_in_yacc " }
	| T_INT_CAST expr 	 {print_endline "T_INT_CAST expr 	" }
	| T_DOUBLE_CAST expr 	 {print_endline "T_DOUBLE_CAST expr 	" }
	| T_STRING_CAST expr	 {print_endline "T_STRING_CAST expr	" }
	| T_ARRAY_CAST expr 	 {print_endline "T_ARRAY_CAST expr 	" }
	| T_OBJECT_CAST expr 	 {print_endline "T_OBJECT_CAST expr 	" }
	| T_BOOL_CAST expr	 {print_endline "T_BOOL_CAST expr	" }
	| T_UNSET_CAST expr	 {print_endline "T_UNSET_CAST expr	" }
	| T_EXIT exit_expr	 {print_endline "T_EXIT exit_expr	" }
	| T_AT  expr  {print_endline "T_AT  expr " }
	| scalar				 {print_endline "scalar				" }
	| T_ARRAY LPARENTH array_pair_list RPARENTH  {print_endline "T_ARRAY LPARENTH array_pair_list RPARENTH " }
	| BACKQUOTE backticks_expr BACKQUOTE  {print_endline "BACKQUOTE backticks_expr BACKQUOTE " }
	| T_PRINT expr   {print_endline "T_PRINT expr  " }
	| function_php is_reference LPARENTH 			parameter_list RPARENTH lexical_vars LBRACKET inner_statement_list RBRACKET  {print_endline "function_php is_reference LPARENTH 			parameter_list RPARENTH lexical_vars LBRACKET inner_statement_list RBRACKET " }

function_php: 
          T_FUNCTION { print_endline "node"}
          ;

lexical_vars:
                  /* empty */
	| T_USE LPARENTH lexical_var_list RPARENTH {print_endline "T_USE LPARENTH lexical_var_list RPARENTH" };

lexical_var_list:
          lexical_var_list VIRGULE T_VARIABLE		{ print_endline "node"}	
	| lexical_var_list VIRGULE ETCOMMERCIAL T_VARIABLE		 {print_endline "lexical_var_list VIRGULE ETCOMMERCIAL T_VARIABLE		" }
	| T_VARIABLE								 {print_endline "T_VARIABLE								" }
	| ETCOMMERCIAL T_VARIABLE							 {print_endline "ETCOMMERCIAL T_VARIABLE							" }

function_call:
          namespace_name LPARENTH function_call_parameter_list 	RPARENTH { print_endline "node"}
	| T_NAMESPACE T_NS_SEPARATOR namespace_name LPARENTH 				function_call_parameter_list				RPARENTH  {print_endline "T_NAMESPACE T_NS_SEPARATOR namespace_name LPARENTH 				function_call_parameter_list				RPARENTH " }
	| T_NS_SEPARATOR namespace_name LPARENTH 				function_call_parameter_list				RPARENTH  {print_endline "T_NS_SEPARATOR namespace_name LPARENTH 				function_call_parameter_list				RPARENTH " }
	| class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING LPARENTH 			function_call_parameter_list			RPARENTH  {print_endline "class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING LPARENTH 			function_call_parameter_list			RPARENTH " }
	| class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects LPARENTH 			function_call_parameter_list			RPARENTH  {print_endline "class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects LPARENTH 			function_call_parameter_list			RPARENTH " }
	| variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING LPARENTH 			function_call_parameter_list			RPARENTH  {print_endline "variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING LPARENTH 			function_call_parameter_list			RPARENTH " }
	| variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects LPARENTH 			function_call_parameter_list			RPARENTH  {print_endline "variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects LPARENTH 			function_call_parameter_list			RPARENTH " }
	| variable_without_objects  LPARENTH 			function_call_parameter_list RPARENTH			 {print_endline "variable_without_objects  LPARENTH 			function_call_parameter_list RPARENTH			" }

class_name:
          T_STATIC { print_endline "node"}
	| namespace_name  {print_endline "namespace_name " }
	| T_NAMESPACE T_NS_SEPARATOR namespace_name  {print_endline "T_NAMESPACE T_NS_SEPARATOR namespace_name " }
	| T_NS_SEPARATOR namespace_name  {print_endline "T_NS_SEPARATOR namespace_name " }

fully_qualified_class_name:
          namespace_name { print_endline "node"}
	| T_NAMESPACE T_NS_SEPARATOR namespace_name  {print_endline "T_NAMESPACE T_NS_SEPARATOR namespace_name " }
	| T_NS_SEPARATOR namespace_name  {print_endline "T_NS_SEPARATOR namespace_name " }



class_name_reference:
          class_name		{ print_endline "node"}				
	| dynamic_class_name_reference	 {print_endline "dynamic_class_name_reference	" }


dynamic_class_name_reference:
          base_variable T_OBJECT_OPERATOR 	object_property  dynamic_class_name_variable_properties { print_endline "node"}

	| base_variable  {print_endline "base_variable " }


dynamic_class_name_variable_properties:
          dynamic_class_name_variable_properties dynamic_class_name_variable_property { print_endline "node"}
  |	/* empty */{ print_endline "node"}


dynamic_class_name_variable_property:
          T_OBJECT_OPERATOR object_property { print_endline "node"}
          ;

exit_expr:
                  /* empty */	
	| LPARENTH RPARENTH		 {print_endline "LPARENTH RPARENTH		" }
	| LPARENTH expr RPARENTH	 {print_endline "LPARENTH expr RPARENTH	" }

backticks_expr:
          /* empty */	
	| T_ENCAPSED_AND_WHITESPACE	 {print_endline "T_ENCAPSED_AND_WHITESPACE	" }
	| encaps_list	 {print_endline "encaps_list	" }


ctor_arguments:
          /* empty */	
	| LPARENTH function_call_parameter_list RPARENTH	 {print_endline "LPARENTH function_call_parameter_list RPARENTH	" }


common_scalar:
    T_LNUMBER 					{ print_endline "node"}
	| T_DNUMBER 					 {print_endline "T_DNUMBER 					" }
	| T_CONSTANT_ENCAPSED_STRING	 {print_endline "T_CONSTANT_ENCAPSED_STRING	" }
	| T_LINE 						 {print_endline "T_LINE 						" }
	| T_FILE 						 {print_endline "T_FILE 						" }
	| T_DIR   					 {print_endline "T_DIR   					" }
	| T_CLASS_C					 {print_endline "T_CLASS_C					" }
	| T_METHOD_C					 {print_endline "T_METHOD_C					" }
	| T_FUNC_C					 {print_endline "T_FUNC_C					" }
	| T_NS_C						 {print_endline "T_NS_C						" }
/*	| T_START_HEREDOC T_ENCAPSED_AND_WHITESPACE T_END_HEREDOC  {print_endline "T_START_HEREDOC T_ENCAPSED_AND_WHITESPACE T_END_HEREDOC " }
	| T_START_HEREDOC T_END_HEREDOC  {print_endline "T_START_HEREDOC T_END_HEREDOC " } */


static_scalar: /* compile-time evaluated scalars */
  common_scalar		{ print_endline "node"}
	| namespace_name 		 {print_endline "namespace_name 		" }
	| T_NAMESPACE T_NS_SEPARATOR namespace_name  {print_endline "T_NAMESPACE T_NS_SEPARATOR namespace_name " }
	| T_NS_SEPARATOR namespace_name  {print_endline "T_NS_SEPARATOR namespace_name " }
	| PLUS static_scalar  {print_endline "PLUS static_scalar " }
	| MINUS static_scalar  {print_endline "MINUS static_scalar " }
	| T_ARRAY LPARENTH static_array_pair_list RPARENTH  {print_endline "T_ARRAY LPARENTH static_array_pair_list RPARENTH " }
	| static_class_constant  {print_endline "static_class_constant " }

static_class_constant:
          class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING { print_endline "node"}
          ;

scalar:
                  T_STRING_VARNAME		{ print_endline "node"}
	| class_constant		 {print_endline "class_constant		" }
	| namespace_name	 {print_endline "namespace_name	" }
	| T_NAMESPACE T_NS_SEPARATOR namespace_name  {print_endline "T_NAMESPACE T_NS_SEPARATOR namespace_name " }
	| T_NS_SEPARATOR namespace_name  {print_endline "T_NS_SEPARATOR namespace_name " }
	| common_scalar			 {print_endline "common_scalar			" }
	| DOUBLEQUOTE encaps_list DOUBLEQUOTE 	 {print_endline "DOUBLEQUOTE encaps_list DOUBLEQUOTE 	" }
/*	| T_START_HEREDOC encaps_list T_END_HEREDOC  {print_endline "T_START_HEREDOC encaps_list T_END_HEREDOC " }*/


static_array_pair_list:
          /* empty */ 
	| non_empty_static_array_pair_list possible_comma	 {print_endline "non_empty_static_array_pair_list possible_comma	" }

possible_comma:
          /* empty */
	| VIRGULE {print_endline "VIRGULE" }

non_empty_static_array_pair_list:
          non_empty_static_array_pair_list VIRGULE static_scalar T_DOUBLE_ARROW static_scalar	{ print_endline "node"}
	| non_empty_static_array_pair_list VIRGULE static_scalar  {print_endline "non_empty_static_array_pair_list VIRGULE static_scalar " }
	| static_scalar T_DOUBLE_ARROW static_scalar  {print_endline "static_scalar T_DOUBLE_ARROW static_scalar " }
	| static_scalar  {print_endline "static_scalar " }

expr:
          r_variable				{ print_endline "node"}	
	| expr_without_variable		 {print_endline "expr_without_variable		" }


r_variable:
          variable { print_endline "node"}
          ;


w_variable:
        variable { print_endline "node"}

rw_variable:
        variable { print_endline "node"}

variable:
        base_variable_with_function_calls T_OBJECT_OPERATOR 	object_property  method_or_not variable_properties { print_endline "node"}

	| base_variable_with_function_calls  {print_endline "base_variable_with_function_calls " }

variable_properties:
          variable_properties variable_property { print_endline "node"}
  |	/* empty */ { print_endline "node"};


variable_property:
          T_OBJECT_OPERATOR object_property  method_or_not { print_endline "node"}
          ;

method_or_not:
        LPARENTH  function_call_parameter_list RPARENTH { print_endline "node"}
  |	/* empty */ { print_endline "node"};

variable_without_objects:
          reference_variable { print_endline "node"}
	| simple_indirect_reference reference_variable  {print_endline "simple_indirect_reference reference_variable " };

static_member:
          class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects { print_endline "node"}
	| variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects  {print_endline "variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects " }

  ;

variable_class_name:
          reference_variable { print_endline "node"}
          ;

base_variable_with_function_calls:
                  base_variable		{ print_endline "node"}
	| function_call  {print_endline "function_call " }


base_variable:
          reference_variable { print_endline "node"}
	| simple_indirect_reference reference_variable  {print_endline "simple_indirect_reference reference_variable " }
	| static_member  {print_endline "static_member " }

reference_variable:
          reference_variable GCROCHET dim_offset DCROCHET	{ print_endline "node"}
	| reference_variable LBRACKET expr RBRACKET		 {print_endline "reference_variable LBRACKET expr RBRACKET		" }
	| compound_variable			 {print_endline "compound_variable			" }


compound_variable:
          T_VARIABLE			{ print_endline "node"}
	| DOLLAR LBRACKET expr RBRACKET	 {print_endline "DOLLAR LBRACKET expr RBRACKET	" }

dim_offset:
          /* empty */		
	| expr			 {print_endline "expr			" }


object_property:
          object_dim_list { print_endline "node"}
	| variable_without_objects   {print_endline "variable_without_objects  " }

object_dim_list:
          object_dim_list GCROCHET dim_offset DCROCHET	{ print_endline "node"}
	| object_dim_list LBRACKET expr RBRACKET		 {print_endline "object_dim_list LBRACKET expr RBRACKET		" }
	| variable_name  {print_endline "variable_name " }

variable_name:
          T_STRING		{ print_endline "node"}
	| LBRACKET expr RBRACKET	 {print_endline "LBRACKET expr RBRACKET	" }

simple_indirect_reference:
          DOLLAR 	{ print_endline "node"}  
	| simple_indirect_reference DOLLAR  {print_endline "simple_indirect_reference DOLLAR " }

assignment_list:
          assignment_list VIRGULE assignment_list_element { print_endline "node"}
	| assignment_list_element {print_endline "assignment_list_element" }


assignment_list_element:
          variable						{ print_endline "node"}		
	| T_LIST LPARENTH  assignment_list RPARENTH	 {print_endline "T_LIST LPARENTH  assignment_list RPARENTH	" }
  |	/* empty */							{ print_endline "node"}


array_pair_list:
          /* empty */ 
	| non_empty_array_pair_list possible_comma	 {print_endline "non_empty_array_pair_list possible_comma	" }

non_empty_array_pair_list:
          non_empty_array_pair_list VIRGULE expr T_DOUBLE_ARROW expr	{ print_endline "node"}
	| non_empty_array_pair_list VIRGULE expr			 {print_endline "non_empty_array_pair_list VIRGULE expr			" }
	| expr T_DOUBLE_ARROW expr	 {print_endline "expr T_DOUBLE_ARROW expr	" }
	| expr 				 {print_endline "expr 				" }
	| non_empty_array_pair_list VIRGULE expr T_DOUBLE_ARROW ETCOMMERCIAL w_variable  {print_endline "non_empty_array_pair_list VIRGULE expr T_DOUBLE_ARROW ETCOMMERCIAL w_variable " }
	| non_empty_array_pair_list VIRGULE ETCOMMERCIAL w_variable  {print_endline "non_empty_array_pair_list VIRGULE ETCOMMERCIAL w_variable " }
	| expr T_DOUBLE_ARROW ETCOMMERCIAL w_variable	 {print_endline "expr T_DOUBLE_ARROW ETCOMMERCIAL w_variable	" }
	| ETCOMMERCIAL w_variable 			 {print_endline "ETCOMMERCIAL w_variable 			" }

encaps_list:
          encaps_list encaps_var  { print_endline "node"}
	| encaps_list T_ENCAPSED_AND_WHITESPACE	 {print_endline "encaps_list T_ENCAPSED_AND_WHITESPACE	" }
	| encaps_var  {print_endline "encaps_var " }
	| T_ENCAPSED_AND_WHITESPACE encaps_var	 {print_endline "T_ENCAPSED_AND_WHITESPACE encaps_var	" }



encaps_var:
          T_VARIABLE 	{ print_endline "node"} 
	| T_VARIABLE GCROCHET  encaps_var_offset DCROCHET	 {print_endline "T_VARIABLE GCROCHET  encaps_var_offset DCROCHET	" }
	| T_VARIABLE T_OBJECT_OPERATOR T_STRING  {print_endline "T_VARIABLE T_OBJECT_OPERATOR T_STRING " }
/*	| T_DOLLAR_OPEN_CURLY_BRACES expr RBRACKET  {print_endline "T_DOLLAR_OPEN_CURLY_BRACES expr RBRACKET " }
	| T_DOLLAR_OPEN_CURLY_BRACES T_STRING_VARNAME GCROCHET expr DCROCHET RBRACKET  {print_endline "T_DOLLAR_OPEN_CURLY_BRACES T_STRING_VARNAME GCROCHET expr DCROCHET RBRACKET " } */
	| T_CURLY_OPEN variable RBRACKET  {print_endline "T_CURLY_OPEN variable RBRACKET " }


encaps_var_offset:
	| T_NUM_STRING	 {print_endline "T_NUM_STRING	" }
	| T_VARIABLE		 {print_endline "T_VARIABLE		" }


internal_functions_in_yacc:
          T_ISSET LPARENTH isset_variables RPARENTH  { print_endline "node"}
	| T_EMPTY LPARENTH variable RPARENTH	 {print_endline "T_EMPTY LPARENTH variable RPARENTH	" }
	| T_INCLUDE expr 			 {print_endline "T_INCLUDE expr 			" }
	| T_INCLUDE_ONCE expr 	 {print_endline "T_INCLUDE_ONCE expr 	" }
	| T_EVAL LPARENTH expr RPARENTH 	 {print_endline "T_EVAL LPARENTH expr RPARENTH 	" }
	| T_REQUIRE expr			 {print_endline "T_REQUIRE expr			" }
	| T_REQUIRE_ONCE expr		 {print_endline "T_REQUIRE_ONCE expr		" }

isset_variables:
          variable 				{ print_endline "node"}
	| isset_variables VIRGULE  variable  {print_endline "isset_variables VIRGULE  variable " }

class_constant:
          class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING  { print_endline "node"}
	| variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING  {print_endline "variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING " }

  %%

