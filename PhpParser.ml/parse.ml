open GrammairePhp


exception Fatal_error of string;;

let lexer = function str ->  Message.lexer_from_string str;;
let fatal_error msg = raise (Fatal_error msg);;

	let cmds lex =
	  try
	    GrammairePhp.start LexerPhp.token lex
	  with
	    | Failure("lexing: empty token")
	    | Parsing.Parse_error -> fatal_error (Message.syntax_error lex) ;; 


let parse = function str -> cmds (lexer str);;
let t a = LexerPhp.token a;;
let l = lexer "echo \"premier test parsing php en OCaml\"; ";;
(*
T_OPEN_TAG T_ECHO LPARENTH T_STRING "premier test parsing php en OCaml" RPARENTH POINT_VIRGULE T_CLOSE_TAG
T_OPEN_TAG T_ECHO LPARENTH T_STRING RPARENTH POINT_VIRGULE T_CLOSE_TAG
T_OPEN_TAG T_ECHO  T_STRING POINT_VIRGULE T_CLOSE_TAG
 *)

let rec list_of_tokens = function tkns -> let res = (try   LexerPhp.token tkns
                                                        with
                                                            | Failure "lexing: empty token" -> T_EXIT)
      in match res with       
        | T_EXIT -> [] 
        | typ    -> typ::(list_of_tokens tkns);; 

let rec list_of_tokens_of_string = function txt -> list_of_tokens (lexer txt);;
(*T_STRING_VARNAME LPARENTH T_STRING  VIRGULE T_STRING  RPARENTH*)

