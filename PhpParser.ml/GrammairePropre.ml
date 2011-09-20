exception Error

type token = 
  | VIRGULE
  | T_XOR_EQUAL
  | T_WHITESPACE
  | T_WHILE
  | T_VARIABLE
  | T_VAR
  | T_USE
  | T_UNSET_CAST
  | T_UNSET
  | T_TRY
  | T_THROW
  | T_SWITCH
  | T_STRING_VARNAME
  | T_STRING_CAST
  | T_STRING
  | T_STATIC
  | T_START_HEREDOC
  | T_SR_EQUAL
  | T_SR
  | T_SL_EQUAL
  | T_SL
  | T_RETURN
  | T_REQUIRE_ONCE
  | T_REQUIRE
  | T_PUBLIC
  | T_PROTECTED
  | T_PRIVATE
  | T_PRINT
  | T_PLUS_EQUAL
  | T_PAAMAYIM_NEKUDOTAYIM
  | T_OR_EQUAL
  | T_OPEN_TAG_WITH_ECHO
  | T_OPEN_TAG
  | T_OBJECT_OPERATOR
  | T_OBJECT_CAST
  | T_NUM_STRING
  | T_NS_SEPARATOR
  | T_NS_C
  | T_NEW
  | T_NAMESPACE
  | T_MUL_EQUAL
  | T_MOD_EQUAL
  | T_MINUS_EQUAL
  | T_METHOD_C
  | T_LOGICAL_XOR
  | T_LOGICAL_OR
  | T_LOGICAL_AND
  | T_LNUMBER
  | T_LIST
  | T_LINE
  | T_IS_SMALLER_OR_EQUAL
  | T_IS_NOT_IDENTICAL
  | T_IS_NOT_EQUAL
  | T_IS_IDENTICAL
  | T_IS_GREATER_OR_EQUAL
  | T_IS_EQUAL
  | T_ISSET
  | T_INT_CAST
  | T_INTERFACE
  | T_INSTANCEOF
  | T_INLINE_HTML
  | T_INCLUDE_ONCE
  | T_INCLUDE
  | T_INC
  | T_IMPLEMENTS
  | T_IF
  | T_HALT_COMPILER
  | T_GOTO
  | T_GLOBAL
  | T_FUNC_C
  | T_FUNCTION
  | T_FOREACH
  | T_FOR
  | T_FINAL
  | T_FILE
  | T_EXTENDS
  | T_EXIT
  | T_EVAL
  | T_END_HEREDOC
  | T_ENDWHILE
  | T_ENDSWITCH
  | T_ENDIF
  | T_ENDFOREACH
  | T_ENDFOR
  | T_ENDDECLARE
  | T_ENCAPSED_AND_WHITESPACE
  | T_EMPTY
  | T_ELSEIF
  | T_ELSE
  | T_ECHO
  | T_DOUBLE_CAST
  | T_DOUBLE_ARROW
  | T_DOLLAR_OPEN_CURLY_BRACES
  | T_DOC_COMMENT
  | T_DO
  | T_DNUMBER
  | T_DIV_EQUAL
  | T_DIR
  | T_DEFAULT
  | T_DECLARE
  | T_DEC
  | T_CURLY_OPEN
  | T_CONTINUE
  | T_CONSTANT_ENCAPSED_STRING
  | T_CONST
  | T_CONCAT_EQUAL
  | T_COMMENT
  | T_CLOSE_TAG
  | T_CLONE
  | T_CLASS_C
  | T_CLASS
  | T_CHARACTER
  | T_CATCH
  | T_CASE
  | T_BREAK
  | T_BOOL_CAST
  | T_BOOLEAN_OR
  | T_BOOLEAN_AND
  | T_BAD_CHARACTER
  | T_AT
  | T_AS
  | T_ARRAY_CAST
  | T_ARRAY
  | T_AND_EQUAL
  | T_ABSTRACT
  | TILDE
  | SUP
  | SLASH
  | RPARENTH
  | RBRACKET
  | POINT_VIRGULE
  | POINT
  | PLUS
  | PIPE
  | PERCENT
  | MULT
  | MINUS
  | LPARENTH
  | LBRACKET
  | INTEROG
  | INF
  | GCROCHET
  | EXCLAM
  | ETCOMMERCIAL
  | EQUAL
  | DOUBLEQUOTE
  | DOUBLEPOINT
  | DOLLAR
  | DCROCHET
  | CHAPEAU
  | BACKQUOTE

and _menhir_env = {
  _menhir_lexer: Lexing.lexbuf -> token;
  _menhir_lexbuf: Lexing.lexbuf;
  mutable _menhir_token: token;
  mutable _menhir_startp: Lexing.position;
  mutable _menhir_endp: Lexing.position;
  mutable _menhir_shifted: int
}

and _menhir_state = 
  | MenhirState813
  | MenhirState805
  | MenhirState796
  | MenhirState794
  | MenhirState789
  | MenhirState787
  | MenhirState783
  | MenhirState782
  | MenhirState781
  | MenhirState776
  | MenhirState775
  | MenhirState774
  | MenhirState773
  | MenhirState769
  | MenhirState767
  | MenhirState764
  | MenhirState763
  | MenhirState762
  | MenhirState761
  | MenhirState756
  | MenhirState754
  | MenhirState751
  | MenhirState750
  | MenhirState745
  | MenhirState737
  | MenhirState731
  | MenhirState730
  | MenhirState727
  | MenhirState724
  | MenhirState721
  | MenhirState709
  | MenhirState707
  | MenhirState706
  | MenhirState705
  | MenhirState703
  | MenhirState701
  | MenhirState700
  | MenhirState699
  | MenhirState697
  | MenhirState696
  | MenhirState695
  | MenhirState693
  | MenhirState688
  | MenhirState687
  | MenhirState685
  | MenhirState680
  | MenhirState679
  | MenhirState674
  | MenhirState673
  | MenhirState671
  | MenhirState668
  | MenhirState667
  | MenhirState662
  | MenhirState660
  | MenhirState659
  | MenhirState655
  | MenhirState653
  | MenhirState652
  | MenhirState651
  | MenhirState647
  | MenhirState637
  | MenhirState636
  | MenhirState632
  | MenhirState631
  | MenhirState630
  | MenhirState629
  | MenhirState626
  | MenhirState624
  | MenhirState623
  | MenhirState622
  | MenhirState615
  | MenhirState614
  | MenhirState612
  | MenhirState611
  | MenhirState609
  | MenhirState608
  | MenhirState605
  | MenhirState604
  | MenhirState602
  | MenhirState600
  | MenhirState599
  | MenhirState593
  | MenhirState592
  | MenhirState591
  | MenhirState586
  | MenhirState585
  | MenhirState582
  | MenhirState581
  | MenhirState572
  | MenhirState568
  | MenhirState565
  | MenhirState562
  | MenhirState558
  | MenhirState552
  | MenhirState545
  | MenhirState544
  | MenhirState541
  | MenhirState538
  | MenhirState534
  | MenhirState531
  | MenhirState530
  | MenhirState527
  | MenhirState524
  | MenhirState521
  | MenhirState520
  | MenhirState517
  | MenhirState514
  | MenhirState513
  | MenhirState511
  | MenhirState508
  | MenhirState503
  | MenhirState497
  | MenhirState488
  | MenhirState487
  | MenhirState483
  | MenhirState481
  | MenhirState480
  | MenhirState462
  | MenhirState461
  | MenhirState458
  | MenhirState456
  | MenhirState454
  | MenhirState452
  | MenhirState451
  | MenhirState449
  | MenhirState444
  | MenhirState440
  | MenhirState438
  | MenhirState437
  | MenhirState435
  | MenhirState434
  | MenhirState433
  | MenhirState431
  | MenhirState429
  | MenhirState427
  | MenhirState426
  | MenhirState424
  | MenhirState422
  | MenhirState419
  | MenhirState418
  | MenhirState415
  | MenhirState414
  | MenhirState412
  | MenhirState407
  | MenhirState402
  | MenhirState401
  | MenhirState400
  | MenhirState398
  | MenhirState394
  | MenhirState393
  | MenhirState392
  | MenhirState383
  | MenhirState380
  | MenhirState373
  | MenhirState371
  | MenhirState370
  | MenhirState366
  | MenhirState359
  | MenhirState356
  | MenhirState355
  | MenhirState354
  | MenhirState352
  | MenhirState351
  | MenhirState350
  | MenhirState349
  | MenhirState348
  | MenhirState347
  | MenhirState346
  | MenhirState345
  | MenhirState340
  | MenhirState339
  | MenhirState336
  | MenhirState334
  | MenhirState333
  | MenhirState331
  | MenhirState330
  | MenhirState324
  | MenhirState323
  | MenhirState318
  | MenhirState315
  | MenhirState311
  | MenhirState310
  | MenhirState309
  | MenhirState308
  | MenhirState307
  | MenhirState306
  | MenhirState305
  | MenhirState304
  | MenhirState303
  | MenhirState302
  | MenhirState301
  | MenhirState300
  | MenhirState299
  | MenhirState298
  | MenhirState297
  | MenhirState296
  | MenhirState295
  | MenhirState294
  | MenhirState293
  | MenhirState292
  | MenhirState291
  | MenhirState290
  | MenhirState289
  | MenhirState288
  | MenhirState287
  | MenhirState286
  | MenhirState285
  | MenhirState284
  | MenhirState283
  | MenhirState282
  | MenhirState281
  | MenhirState280
  | MenhirState279
  | MenhirState278
  | MenhirState277
  | MenhirState276
  | MenhirState275
  | MenhirState274
  | MenhirState273
  | MenhirState272
  | MenhirState271
  | MenhirState270
  | MenhirState269
  | MenhirState268
  | MenhirState267
  | MenhirState266
  | MenhirState265
  | MenhirState264
  | MenhirState263
  | MenhirState262
  | MenhirState261
  | MenhirState260
  | MenhirState259
  | MenhirState258
  | MenhirState257
  | MenhirState256
  | MenhirState255
  | MenhirState254
  | MenhirState253
  | MenhirState252
  | MenhirState251
  | MenhirState250
  | MenhirState249
  | MenhirState247
  | MenhirState246
  | MenhirState245
  | MenhirState244
  | MenhirState243
  | MenhirState242
  | MenhirState241
  | MenhirState240
  | MenhirState237
  | MenhirState236
  | MenhirState234
  | MenhirState233
  | MenhirState232
  | MenhirState231
  | MenhirState228
  | MenhirState227
  | MenhirState226
  | MenhirState225
  | MenhirState224
  | MenhirState223
  | MenhirState220
  | MenhirState215
  | MenhirState212
  | MenhirState208
  | MenhirState205
  | MenhirState202
  | MenhirState200
  | MenhirState198
  | MenhirState197
  | MenhirState196
  | MenhirState195
  | MenhirState193
  | MenhirState192
  | MenhirState191
  | MenhirState184
  | MenhirState181
  | MenhirState179
  | MenhirState178
  | MenhirState162
  | MenhirState161
  | MenhirState159
  | MenhirState158
  | MenhirState157
  | MenhirState154
  | MenhirState152
  | MenhirState146
  | MenhirState144
  | MenhirState142
  | MenhirState141
  | MenhirState140
  | MenhirState139
  | MenhirState133
  | MenhirState130
  | MenhirState129
  | MenhirState128
  | MenhirState126
  | MenhirState122
  | MenhirState121
  | MenhirState120
  | MenhirState119
  | MenhirState113
  | MenhirState112
  | MenhirState111
  | MenhirState110
  | MenhirState109
  | MenhirState108
  | MenhirState107
  | MenhirState106
  | MenhirState105
  | MenhirState103
  | MenhirState102
  | MenhirState101
  | MenhirState99
  | MenhirState97
  | MenhirState96
  | MenhirState94
  | MenhirState91
  | MenhirState90
  | MenhirState86
  | MenhirState84
  | MenhirState82
  | MenhirState77
  | MenhirState74
  | MenhirState71
  | MenhirState70
  | MenhirState69
  | MenhirState68
  | MenhirState67
  | MenhirState66
  | MenhirState65
  | MenhirState63
  | MenhirState60
  | MenhirState59
  | MenhirState58
  | MenhirState56
  | MenhirState55
  | MenhirState54
  | MenhirState53
  | MenhirState52
  | MenhirState51
  | MenhirState47
  | MenhirState46
  | MenhirState45
  | MenhirState44
  | MenhirState43
  | MenhirState41
  | MenhirState40
  | MenhirState39
  | MenhirState38
  | MenhirState37
  | MenhirState36
  | MenhirState34
  | MenhirState31
  | MenhirState30
  | MenhirState29
  | MenhirState28
  | MenhirState27
  | MenhirState26
  | MenhirState25
  | MenhirState23
  | MenhirState21
  | MenhirState10
  | MenhirState7
  | MenhirState5
  | MenhirState3
  | MenhirState1


# 1 "GrammairePropre.mly"
  
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



# 573 "GrammairePropre.ml"
let _eRR =
  Error

let rec start : (Lexing.lexbuf -> token) -> Lexing.lexbuf -> (
# 139 "GrammairePropre.mly"
      (Syntax.php)
# 580 "GrammairePropre.ml"
) =
  fun lexer lexbuf ->
    let _menhir_env =
      let (lexer : Lexing.lexbuf -> token) = lexer in
      let (lexbuf : Lexing.lexbuf) = lexbuf in
      ((let _tok = lexer lexbuf in
      {
        _menhir_lexer = lexer;
        _menhir_lexbuf = lexbuf;
        _menhir_token = _tok;
        _menhir_startp = lexbuf.Lexing.lex_start_p;
        _menhir_endp = lexbuf.Lexing.lex_curr_p;
        _menhir_shifted = 1073741823;
        }) : _menhir_env)
    in
    Obj.magic (let (_menhir_env : _menhir_env) = _menhir_env in
    let (_menhir_stack : 'freshtv5) = () in
    ((assert (Pervasives.(<>) _menhir_env._menhir_shifted (-1));
    let _tok = _menhir_env._menhir_token in
    let (_menhir_env : _menhir_env) = _menhir_env in
    let (_menhir_stack : 'freshtv3) = _menhir_stack in
    let (_tok : token) = _tok in
    ((match _tok with
    | _ ->
        assert (Pervasives.(<>) _menhir_env._menhir_shifted (-1));
        _menhir_env._menhir_shifted <- (-1);
        let (_menhir_env : _menhir_env) = _menhir_env in
        let (_menhir_stack : 'freshtv1) = Obj.magic _menhir_stack in
        (raise _eRR : 'freshtv2)) : 'freshtv4)) : 'freshtv6))





