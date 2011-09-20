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





  LALR shift/reduce conflicts and how they are resolved:
 
  - 2 shift/reduce conflicts due to the dangling elseif/else ambiguity. Solved by shift.
 
 

*)


%}


%left T_INCLUDE T_INCLUDE_ONCE T_EVAL T_REQUIRE T_REQUIRE_ONCE
%left ','
%left T_LOGICAL_OR
%left T_LOGICAL_XOR
%left T_LOGICAL_AND
%right T_PRINT
%left '=' T_PLUS_EQUAL T_MINUS_EQUAL T_MUL_EQUAL T_DIV_EQUAL T_CONCAT_EQUAL T_MOD_EQUAL T_AND_EQUAL T_OR_EQUAL T_XOR_EQUAL T_SL_EQUAL T_SR_EQUAL
%left '?' ':'
%left T_BOOLEAN_OR
%left T_BOOLEAN_AND
%left '|'
%left '^'
%left '&'
%nonassoc T_IS_EQUAL T_IS_NOT_EQUAL T_IS_IDENTICAL T_IS_NOT_IDENTICAL
%nonassoc '<' T_IS_SMALLER_OR_EQUAL '>' T_IS_GREATER_OR_EQUAL
%left T_SL T_SR
%left '+' '-' '.'
%left '*' '/' '%'
%right '!'
%nonassoc T_INSTANCEOF
%right '~' T_INC T_DEC T_INT_CAST T_DOUBLE_CAST T_STRING_CAST T_ARRAY_CAST T_OBJECT_CAST T_BOOL_CAST T_UNSET_CAST '@'
%right GCROCHET
%nonassoc T_NEW T_CLONE
%token T_EXIT
%token T_IF
%left T_ELSEIF
%left T_ELSE
%left T_ENDIF
%token LBRACKET RBRACKET GCROCHET DCROCHET
%token T_LNUMBER
%token T_DNUMBER
%token T_STRING
%token T_STRING_VARNAME
%token T_VARIABLE
%token T_NUM_STRING
%token T_INLINE_HTML
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
;

top_statement_list:
		top_statement_list   top_statement { print_endline "node"} 

;

namespace_name:
		T_STRING { print_endline "node"}
	|	namespace_name T_NS_SEPARATOR T_STRING { print_endline "node"};

top_statement:
		statement						{ print_endline "node"}
	|	function_declaration_statement	{ print_endline "node"}
	|	class_declaration_statement		{ print_endline "node"}
	|	T_HALT_COMPILER '(' ')' ';'		{ print_endline "node"}
	|	T_NAMESPACE namespace_name ';'	{ print_endline "node"}
	|	T_NAMESPACE namespace_name LBRACKET { print_endline "node"}	   
	|	T_NAMESPACE LBRACKET			{ print_endline "node"}
	|	T_USE use_declarations ';'     { print_endline "node"} 
	|	constant_declaration ';'		{ print_endline "node"};

use_declarations:
		use_declarations ',' use_declaration { print_endline "node"}
	|	use_declaration { print_endline "node"};

use_declaration:
		namespace_name 			{ print_endline "node"}
	|	namespace_name T_AS T_STRING	{ print_endline "node"}
	|	T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name T_AS T_STRING { print_endline "node"};

constant_declaration:
		constant_declaration ',' T_STRING '=' static_scalar	 { print_endline "node"}
	|	T_CONST T_STRING '=' static_scalar  { print_endline "node"};

inner_statement_list:
		inner_statement_list   inner_statement { print_endline "node"}
;


inner_statement:
		statement { print_endline "node"}
	|	function_declaration_statement { print_endline "node"}
	|	class_declaration_statement { print_endline "node"}
	|	T_HALT_COMPILER '(' ')' ';'   { print_endline "node"};


statement:
		unticked_statement { print_endline "node"}
	|	T_STRING ':' { print_endline "node"};

unticked_statement:
		/*''*/
	|	T_IF '(' expr ')'  statement  elseif_list else_single { print_endline "node"}
	|	T_IF '(' expr ')' ':'  inner_statement_list  new_elseif_list new_else_single T_ENDIF ';' { print_endline "node"}
	|	T_WHILE '('  expr  ')'  while_statement { print_endline "node"}
	|	T_DO  statement T_WHILE '('  expr ')' ';' { print_endline "node"}
	|	T_FOR			'('				for_expr			';' 				for_expr			';' 				for_expr			')' 			for_statement { print_endline "node"}
	|	T_SWITCH '(' expr ')'	 switch_case_list { print_endline "node"} 
	|	T_BREAK ';'				{ print_endline "node"}
	|	T_BREAK expr ';'		{ print_endline "node"}
	|	T_CONTINUE ';'			{ print_endline "node"}
	|	T_CONTINUE expr ';'		{ print_endline "node"}
	|	T_RETURN ';'						{ print_endline "node"}
	|	T_RETURN expr_without_variable ';'	{ print_endline "node"}
	|	T_RETURN variable ';'				{ print_endline "node"}
	|	T_GLOBAL global_var_list ';'{ print_endline "node"}
	|	T_STATIC static_var_list ';'{ print_endline "node"}
	|	T_ECHO echo_expr_list ';'{ print_endline "node"}
	|	T_INLINE_HTML			{ print_endline "node"}
	|	expr ';'				{ print_endline "node"}
	|	T_UNSET '(' unset_variables ')' ';'{ print_endline "node"}
	|	T_FOREACH '(' variable T_AS 	foreach_variable foreach_optional_arg ')' foreach_statement { print_endline "node"} 
	|	T_FOREACH '(' expr_without_variable T_AS	variable foreach_optional_arg ')' 		foreach_statement { print_endline "node"}
	|	T_DECLARE  '(' declare_list ')' declare_statement { print_endline "node"}
	|	';'		/* empty statement */ { print_endline "node"}
	|	T_TRY  '' 	T_CATCH '(' 		fully_qualified_class_name 		T_VARIABLE ')' 		'' 		additional_catches { print_endline "node"}
	|	T_THROW expr ';' { print_endline "node"} 
	|	T_GOTO T_STRING ';' { print_endline "node"};


additional_catches:
		non_empty_additional_catches { print_endline "node"} 
;

non_empty_additional_catches:
		additional_catch { print_endline "node"}
	|	non_empty_additional_catches additional_catch { print_endline "node"};


additional_catch:
	T_CATCH '(' fully_qualified_class_name  T_VARIABLE ')'  '' { print_endline "node"}
;


unset_variables:
		unset_variable { print_endline "node"}
	|	unset_variables ',' unset_variable { print_endline "node"};

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
		/* empty */	
	|	'&'	{ print_endline "node"}		;


unticked_function_declaration_statement:
		function_php is_reference T_STRING 
			'(' parameter_list ')' '' { print_endline "node"}
;

unticked_class_declaration_statement:
		class_entry_type T_STRING extends_from	implements_list			LBRACKET				class_statement_list	RBRACKET { print_endline "node"}
	|	interface_entry T_STRING						interface_extends_list			LBRACKET class_statement_list RBRACKET { print_endline "node"} ;


class_entry_type:
		T_CLASS			 { print_endline "node"}
	|	T_ABSTRACT T_CLASS  { print_endline "node"}
	|	T_FINAL T_CLASS  { print_endline "node"};

extends_from:
		/* empty */					
	|	T_EXTENDS fully_qualified_class_name  { print_endline "node"}	;

interface_entry:
	T_INTERFACE		 { print_endline "node"}
;

interface_extends_list:
		/* empty */
	|	T_EXTENDS interface_list  { print_endline "node"};

implements_list:
		/* empty */
	|	T_IMPLEMENTS interface_list { print_endline "node"};

interface_list:
		fully_qualified_class_name			 { print_endline "node"}
	|	interface_list ',' fully_qualified_class_name  { print_endline "node"};

foreach_optional_arg:
		/* empty */						
	|	T_DOUBLE_ARROW foreach_variable	 { print_endline "node"};


foreach_variable:
		variable		 { print_endline "node"}	
	|	'&' variable	 { print_endline "node"}	;

for_statement:
		statement  { print_endline "node"}
	|	':' inner_statement_list T_ENDFOR ';'  { print_endline "node"};


foreach_statement:
		statement  { print_endline "node"}
	|	':' inner_statement_list T_ENDFOREACH ';'{ print_endline "node"};


declare_statement:
		statement  { print_endline "node"}
	|	':' inner_statement_list T_ENDDECLARE ';'{ print_endline "node"};


declare_list:
		T_STRING '=' static_scalar			 { print_endline "node"}		
	|	declare_list ',' T_STRING '=' static_scalar	{ print_endline "node"};


switch_case_list:
		''				 { print_endline "node"}	
	|	''				 { print_endline "node"}
	|	':' case_list T_ENDSWITCH ';'	 { print_endline "node"}	
	|	':' ';' case_list T_ENDSWITCH ';'	{ print_endline "node"};


case_list:
		/* empty */	
	|	case_list T_CASE expr case_separator  inner_statement_list  { print_endline "node"}
	|	case_list T_DEFAULT case_separator  inner_statement_list { print_endline "node"};


case_separator:
		':'  { print_endline "node"}
	|	';'{ print_endline "node"};


while_statement:
		statement  { print_endline "node"}
	|	':' inner_statement_list T_ENDWHILE ';'{ print_endline "node"};



elseif_list:
		/* empty */
	|	elseif_list T_ELSEIF '(' expr ')'  statement { print_endline "node"};


new_elseif_list:
		/* empty */
	|	new_elseif_list T_ELSEIF '(' expr ')' ':'  inner_statement_list { print_endline "node"};


else_single:
		/* empty */
	|	T_ELSE statement{ print_endline "node"};


new_else_single:
		/* empty */
	|	T_ELSE ':' inner_statement_list{ print_endline "node"};


parameter_list:
		non_empty_parameter_list  { print_endline "node"}
	|	/* empty */{ print_endline "node"};


non_empty_parameter_list:
		optional_class_type T_VARIABLE				 { print_endline "node"}
	|	optional_class_type '&' T_VARIABLE	 { print_endline "node"}		
	|	optional_class_type '&' T_VARIABLE '=' static_scalar			{ print_endline "node"}
	|	optional_class_type T_VARIABLE '=' static_scalar				{ print_endline "node"}
	|	non_empty_parameter_list ',' optional_class_type T_VARIABLE 	{ print_endline "node"}
	|	non_empty_parameter_list ',' optional_class_type '&' T_VARIABLE	{ print_endline "node"}
	|	non_empty_parameter_list ',' optional_class_type '&' T_VARIABLE	 '=' static_scalar { print_endline "node"}
	|	non_empty_parameter_list ',' optional_class_type T_VARIABLE '=' static_scalar 	{ print_endline "node"};


optional_class_type:
		/* empty */					
	|	fully_qualified_class_name	{ print_endline "node"}
	|	T_ARRAY						{ print_endline "node"};


function_call_parameter_list:
		non_empty_function_call_parameter_list	{ print_endline "node"}
	|	/* empty */				{ print_endline "node"};


non_empty_function_call_parameter_list:
		expr_without_variable { print_endline "node"	}
	|	variable				{ print_endline "node"}
	|	'&' w_variable 				{ print_endline "node"}
	|	non_empty_function_call_parameter_list ',' expr_without_variable	{ print_endline "node"}
	|	non_empty_function_call_parameter_list ',' variable					{ print_endline "node"}
	|	non_empty_function_call_parameter_list ',' '&' w_variable			{ print_endline "node"};

global_var_list:
		global_var_list ',' global_var	{ print_endline "node"}
	|	global_var						{ print_endline "node" }


global_var:
		T_VARIABLE				{ print_endline "node"} 
	|	'$' r_variable		{ print_endline "node"}
	|	'$' ''	{ print_endline "node"};


static_var_list:
		static_var_list ',' T_VARIABLE { print_endline "node"}
	|	static_var_list ',' T_VARIABLE '=' static_scalar { print_endline "node"}
	|	T_VARIABLE  { print_endline "node"}
	|	T_VARIABLE '=' static_scalar { print_endline "node"}

;


class_statement_list:
		class_statement_list class_statement { print_endline "node"}
	|	/* empty */{ print_endline "node"}


class_statement:
		variable_modifiers  class_variable_declaration ';' { print_endline "node"}
	|	class_constant_declaration ';'{ print_endline "node"}
	|	method_modifiers function_php is_reference T_STRING  '('			parameter_list ')' method_body { print_endline "node"}


method_body:
		';' /* abstract method */		{ print_endline "node"} 	
	|	''	{ print_endline "node"}

variable_modifiers:
		non_empty_member_modifiers		{ print_endline "node"}
	|	T_VAR							{ print_endline "node"}

method_modifiers:
		/* empty */							
	|	non_empty_member_modifiers			 { print_endline "node"}

non_empty_member_modifiers:
		member_modifier				{ print_endline "node"}		
	|	non_empty_member_modifiers member_modifier	{ print_endline "node"}

member_modifier:
		T_PUBLIC					{ print_endline "node"} 
	|	T_PROTECTED				{ print_endline "node"}
	|	T_PRIVATE				{ print_endline "node"}
	|	T_STATIC				{ print_endline "node"}
	|	T_ABSTRACT				{ print_endline "node"}
	|	T_FINAL					{ print_endline "node"}

class_variable_declaration:
		class_variable_declaration ',' T_VARIABLE			{ print_endline "node"}		
	|	class_variable_declaration ',' T_VARIABLE '=' static_scalar	{ print_endline "node"}
	|	T_VARIABLE						{ print_endline "node"}
	|	T_VARIABLE '=' static_scalar	{ print_endline "node"}

class_constant_declaration:
		class_constant_declaration ',' T_STRING '=' static_scalar	 { print_endline "node"}
	|	T_CONST T_STRING '=' static_scalar	{ print_endline "node"}

echo_expr_list:
		echo_expr_list ',' expr  { print_endline "node"}
	|	expr					{ print_endline "node"}


for_expr:
		/* empty */			
	|	non_empty_for_expr	{ print_endline "node"}

non_empty_for_expr:
		non_empty_for_expr ','	 expr  { print_endline "node"}
	|	expr					{ print_endline "node"}

expr_without_variable:
		T_LIST '('  assignment_list ')' '=' expr { print_endline "node"}
	|	variable '=' expr		{ print_endline "node"}
	|	variable '=' '&' variable { print_endline "node"}
	|	variable '=' '&' T_NEW class_name_reference  ctor_arguments { print_endline "node"}
	|	T_NEW class_name_reference  ctor_arguments { print_endline "node"}
	|	T_CLONE expr { print_endline "node"}
	|	variable T_PLUS_EQUAL expr 	{ print_endline "node"}
	|	variable T_MINUS_EQUAL expr	{ print_endline "node"}
	|	variable T_MUL_EQUAL expr		{ print_endline "node"}
	|	variable T_DIV_EQUAL expr		{ print_endline "node"}
	|	variable T_CONCAT_EQUAL expr	{ print_endline "node"}
	|	variable T_MOD_EQUAL expr		{ print_endline "node"}
	|	variable T_AND_EQUAL expr		{ print_endline "node"}
	|	variable T_OR_EQUAL expr 		{ print_endline "node"}
	|	variable T_XOR_EQUAL expr 		{ print_endline "node"}
	|	variable T_SL_EQUAL expr	{ print_endline "node"}
	|	variable T_SR_EQUAL expr	{ print_endline "node"}
	|	rw_variable T_INC { print_endline "node"}
	|	T_INC rw_variable { print_endline "node"}
	|	rw_variable T_DEC { print_endline "node"}
	|	T_DEC rw_variable { print_endline "node"}
	|	expr T_BOOLEAN_OR  expr { print_endline "node"}
	|	expr T_BOOLEAN_AND  expr { print_endline "node"}
	|	expr T_LOGICAL_OR  expr { print_endline "node"}
	|	expr T_LOGICAL_AND  expr { print_endline "node"}
	|	expr T_LOGICAL_XOR expr { print_endline "node"}
	|	expr '|' expr	{ print_endline "node"}
	|	expr '&' expr	{ print_endline "node"}
	|	expr '^' expr	{ print_endline "node"}
	|	expr '.' expr 	{ print_endline "node"}
	|	expr '+' expr 	{ print_endline "node"}
	|	expr '-' expr 	{ print_endline "node"}
	|	expr '*' expr	{ print_endline "node"}
	|	expr '/' expr	{ print_endline "node"}
	|	expr '%' expr 	{ print_endline "node"}
	| 	expr T_SL expr	{ print_endline "node"}
	|	expr T_SR expr	{ print_endline "node"}
	|	'+' expr %prec T_INC  { print_endline "node"}
	|	'-' expr %prec T_INC  { print_endline "node"}
	|	'!' expr { print_endline "node"}
	|	'~' expr { print_endline "node"}
	|	expr T_IS_IDENTICAL expr		{ print_endline "node"}
	|	expr T_IS_NOT_IDENTICAL expr	{ print_endline "node"}
	|	expr T_IS_EQUAL expr			{ print_endline "node"}
	|	expr T_IS_NOT_EQUAL expr 		{ print_endline "node"}
	|	expr '<' expr 					{ print_endline "node"}
	|	expr T_IS_SMALLER_OR_EQUAL expr { print_endline "node"}
	|	expr '>' expr 					{ print_endline "node"}
	|	expr T_IS_GREATER_OR_EQUAL expr { print_endline "node"}
	|	expr T_INSTANCEOF class_name_reference { print_endline "node"}
	|	'(' expr ')' 	{ print_endline "node"}
	|	expr '?' 		expr ':' 		expr	 { print_endline "node"}
	|	expr '?' ':' 		expr     { print_endline "node"}
	|	internal_functions_in_yacc { print_endline "node"}
	|	T_INT_CAST expr 	{ print_endline "node"}
	|	T_DOUBLE_CAST expr 	{ print_endline "node"}
	|	T_STRING_CAST expr	{ print_endline "node"}
	|	T_ARRAY_CAST expr 	{ print_endline "node"}
	|	T_OBJECT_CAST expr 	{ print_endline "node"}
	|	T_BOOL_CAST expr	{ print_endline "node"}
	|	T_UNSET_CAST expr	{ print_endline "node"}
	|	T_EXIT exit_expr	{ print_endline "node"}
	|	'@'  expr { print_endline "node"}
	|	scalar				{ print_endline "node"}
	|	T_ARRAY '(' array_pair_list ')' { print_endline "node"}
	|	'`' backticks_expr '`' { print_endline "node"}
	|	T_PRINT expr  { print_endline "node"}
	|	function_php is_reference '(' 			parameter_list ')' lexical_vars '' { print_endline "node"}

function_php: 
	T_FUNCTION { print_endline "node"}
;

lexical_vars:
		/* empty */
	|	T_USE '(' lexical_var_list ')'{ print_endline "node"};

lexical_var_list:
		lexical_var_list ',' T_VARIABLE		{ print_endline "node"}	
	|	lexical_var_list ',' '&' T_VARIABLE		{ print_endline "node"}
	|	T_VARIABLE								{ print_endline "node"}
	|	'&' T_VARIABLE							{ print_endline "node"}

function_call:
		namespace_name '(' function_call_parameter_list 	')' { print_endline "node"}
	|	T_NAMESPACE T_NS_SEPARATOR namespace_name '(' 				function_call_parameter_list				')' { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name '(' 				function_call_parameter_list				')' { print_endline "node"}
	|	class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING '(' 			function_call_parameter_list			')' { print_endline "node"}
	|	class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects '(' 			function_call_parameter_list			')' { print_endline "node"}
	|	variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING '(' 			function_call_parameter_list			')' { print_endline "node"}
	|	variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects '(' 			function_call_parameter_list			')' { print_endline "node"}
	|	variable_without_objects  '(' 			function_call_parameter_list ')'			{ print_endline "node"}

class_name:
		T_STATIC { print_endline "node"}
	|	namespace_name { print_endline "node"}
	|	T_NAMESPACE T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name { print_endline "node"}

fully_qualified_class_name:
		namespace_name { print_endline "node"}
	|	T_NAMESPACE T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name { print_endline "node"}



class_name_reference:
		class_name		{ print_endline "node"}				
	|	dynamic_class_name_reference	{ print_endline "node"}


dynamic_class_name_reference:
		base_variable T_OBJECT_OPERATOR 	object_property  dynamic_class_name_variable_properties { print_endline "node"}
			
	|	base_variable { print_endline "node"}


dynamic_class_name_variable_properties:
		dynamic_class_name_variable_properties dynamic_class_name_variable_property { print_endline "node"}
	|	/* empty */{ print_endline "node"}


dynamic_class_name_variable_property:
		T_OBJECT_OPERATOR object_property { print_endline "node"}
;

exit_expr:
		/* empty */	
	|	'(' ')'		{ print_endline "node"}
	|	'(' expr ')'	{ print_endline "node"}

backticks_expr:
		/* empty */	
	|	T_ENCAPSED_AND_WHITESPACE	{ print_endline "node"}
	|	encaps_list	{ print_endline "node"}


ctor_arguments:
		/* empty */	
	|	'(' function_call_parameter_list ')'	{ print_endline "node"}


common_scalar:
		T_LNUMBER 					{ print_endline "node"}
	|	T_DNUMBER 					{ print_endline "node"}
	|	T_CONSTANT_ENCAPSED_STRING	{ print_endline "node"}
	|	T_LINE 						{ print_endline "node"}
	|	T_FILE 						{ print_endline "node"}
	|	T_DIR   					{ print_endline "node"}
	|	T_CLASS_C					{ print_endline "node"}
	|	T_METHOD_C					{ print_endline "node"}
	|	T_FUNC_C					{ print_endline "node"}
	|	T_NS_C						{ print_endline "node"}
	|	T_START_HEREDOC T_ENCAPSED_AND_WHITESPACE T_END_HEREDOC { print_endline "node"}
	|	T_START_HEREDOC T_END_HEREDOC { print_endline "node"}


static_scalar: /* compile-time evaluated scalars */
		common_scalar		{ print_endline "node"}
	|	namespace_name 		{ print_endline "node"}
	|	T_NAMESPACE T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	'+' static_scalar { print_endline "node"}
	|	'-' static_scalar { print_endline "node"}
	|	T_ARRAY '(' static_array_pair_list ')' { print_endline "node"}
	|	static_class_constant { print_endline "node"}

static_class_constant:
		class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING { print_endline "node"}
;

scalar:
		T_STRING_VARNAME		{ print_endline "node"}
	|	class_constant		{ print_endline "node"}
	|	namespace_name	{ print_endline "node"}
	|	T_NAMESPACE T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	T_NS_SEPARATOR namespace_name { print_endline "node"}
	|	common_scalar			{ print_endline "node"}
	|	'"' encaps_list '"' 	{ print_endline "node"}
	|	T_START_HEREDOC encaps_list T_END_HEREDOC { print_endline "node"}


static_array_pair_list:
		/* empty */ 
	|	non_empty_static_array_pair_list possible_comma	{ print_endline "node"}

possible_comma:
		/* empty */
	|	','{ print_endline "node"}

non_empty_static_array_pair_list:
		non_empty_static_array_pair_list ',' static_scalar T_DOUBLE_ARROW static_scalar	{ print_endline "node"}
	|	non_empty_static_array_pair_list ',' static_scalar { print_endline "node"}
	|	static_scalar T_DOUBLE_ARROW static_scalar { print_endline "node"}
	|	static_scalar { print_endline "node"}

expr:
		r_variable				{ print_endline "node"}	
	|	expr_without_variable		{ print_endline "node"}


r_variable:
	variable { print_endline "node"}
;


w_variable:
	variable { print_endline "node"}

rw_variable:
	variable { print_endline "node"}

variable:
		base_variable_with_function_calls T_OBJECT_OPERATOR 	object_property  method_or_not variable_properties { print_endline "node"}
			
	|	base_variable_with_function_calls { print_endline "node"}

variable_properties:
		variable_properties variable_property { print_endline "node"}
	|	/* empty */ { print_endline "node"};


variable_property:
		T_OBJECT_OPERATOR object_property  method_or_not { print_endline "node"}
;

method_or_not:
		'(' 
				function_call_parameter_list ')' { print_endline "node"}
	|	/* empty */ { print_endline "node"};

variable_without_objects:
		reference_variable { print_endline "node"}
	|	simple_indirect_reference reference_variable { print_endline "node"};

static_member:
		class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects { print_endline "node"}
	|	variable_class_name T_PAAMAYIM_NEKUDOTAYIM variable_without_objects { print_endline "node"}

;

variable_class_name:
		reference_variable { print_endline "node"}
;

base_variable_with_function_calls:
		base_variable		{ print_endline "node"}
	|	function_call { print_endline "node"}


base_variable:
		reference_variable { print_endline "node"}
	|	simple_indirect_reference reference_variable { print_endline "node"}
	|	static_member { print_endline "node"}

reference_variable:
		reference_variable GCROCHET dim_offset DCROCHET	{ print_endline "node"}
	|	reference_variable ''		{ print_endline "node"}
	|	compound_variable			{ print_endline "node"}


compound_variable:
		T_VARIABLE			{ print_endline "node"}
	|	'$' ''	{ print_endline "node"}

dim_offset:
		/* empty */		
	|	expr			{ print_endline "node"}


object_property:
		object_dim_list { print_endline "node"}
	|	variable_without_objects  { print_endline "node"}

object_dim_list:
		object_dim_list GCROCHET dim_offset DCROCHET	{ print_endline "node"}
	|	object_dim_list ''		{ print_endline "node"}
	|	variable_name { print_endline "node"}

variable_name:
		T_STRING		{ print_endline "node"}
	|	''	{ print_endline "node"}

simple_indirect_reference:
		'$' 	{ print_endline "node"}  
	|	simple_indirect_reference '$' { print_endline "node"}

assignment_list:
		assignment_list ',' assignment_list_element { print_endline "node"}
	|	assignment_list_element{ print_endline "node"}


assignment_list_element:
		variable						{ print_endline "node"}		
	|	T_LIST '('  assignment_list ')'	{ print_endline "node"}
	|	/* empty */							{ print_endline "node"}


array_pair_list:
		/* empty */ 
	|	non_empty_array_pair_list possible_comma	{ print_endline "node"}

non_empty_array_pair_list:
		non_empty_array_pair_list ',' expr T_DOUBLE_ARROW expr	{ print_endline "node"}
	|	non_empty_array_pair_list ',' expr			{ print_endline "node"}
	|	expr T_DOUBLE_ARROW expr	{ print_endline "node"}
	|	expr 				{ print_endline "node"}
	|	non_empty_array_pair_list ',' expr T_DOUBLE_ARROW '&' w_variable { print_endline "node"}
	|	non_empty_array_pair_list ',' '&' w_variable { print_endline "node"}
	|	expr T_DOUBLE_ARROW '&' w_variable	{ print_endline "node"}
	|	'&' w_variable 			{ print_endline "node"}

encaps_list:
		encaps_list encaps_var  { print_endline "node"}
	|	encaps_list T_ENCAPSED_AND_WHITESPACE	{ print_endline "node"}
	|	encaps_var { print_endline "node"}
	|	T_ENCAPSED_AND_WHITESPACE encaps_var	{ print_endline "node"}



encaps_var:
		T_VARIABLE 	{ print_endline "node"} 
	|	T_VARIABLE GCROCHET  encaps_var_offset DCROCHET	{ print_endline "node"}
	|	T_VARIABLE T_OBJECT_OPERATOR T_STRING { print_endline "node"}
	|	T_DOLLAR_OPEN_CURLY_BRACES expr RBRACKET { print_endline "node"}
	|	T_DOLLAR_OPEN_CURLY_BRACES T_STRING_VARNAME GCROCHET expr DCROCHET RBRACKET { print_endline "node"}
	|	T_CURLY_OPEN variable RBRACKET { print_endline "node"}


encaps_var_offset:
		T_STRING			{ print_endline "node"} 
	|	T_NUM_STRING	{ print_endline "node"}
	|	T_VARIABLE		{ print_endline "node"}


internal_functions_in_yacc:
		T_ISSET '(' isset_variables ')'  { print_endline "node"}
	|	T_EMPTY '(' variable ')'	{ print_endline "node"}
	|	T_INCLUDE expr 			{ print_endline "node"}
	|	T_INCLUDE_ONCE expr 	{ print_endline "node"}
	|	T_EVAL '(' expr ')' 	{ print_endline "node"}
	|	T_REQUIRE expr			{ print_endline "node"}
	|	T_REQUIRE_ONCE expr		{ print_endline "node"}

isset_variables:
		variable 				{ print_endline "node"}
	|	isset_variables ','  variable { print_endline "node"}

class_constant:
		class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING  { print_endline "node"}
	|	variable_class_name T_PAAMAYIM_NEKUDOTAYIM T_STRING { print_endline "node"}

%%

