////////////////////////////////
/// BUILT-IN FUNCTIONALITIES ///
////////////////////////////////
true.

not(X):- X,!,fail.
not(_).

;(X,Y):- X.
;(X,Y):- Y.

|(X,Y):- X.
|(X,Y):- Y.

->(IF,;(THEN,ELSE)):- IF,!,THEN.
->(IF,;(THEN,ELSE)):- ELSE.

member(A,[A|_]).
member(A,[_|B]):- member(A,B).

length([_|T],R):-
	length(T,R2),
	R is R2 + 1.
length([],0).

repeat_until_stable(X):- built_in_reset_stable_flag, built_in_stable_call(X).
built_in_stable_call(X):- X,!,built_in_is_stable.

succeedretract(X):- retract(X),!.
succeedretract(_).

//gensym(1).
//gensym(Prefix, NewNumber) :-
//	retract(gensym(Number)),
//	NewNumber is Number + 1,
//	assert(gensym(NewNumber)).


//	name(Number, NList),
//	concat(Prefix, NList, AList),
//	name(Atom, AList).

