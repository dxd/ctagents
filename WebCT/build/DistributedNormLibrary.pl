//////////////////////////////////////
/// NORM INTERPRETER FUNCTIONALITY ///
//////////////////////////////////////
// Entailment of labeled literals
@lbl(Label,Literal):- 
	@connected(Label,Host,Port),
	@external(network,entailed(Host,Port,Literal),true).
@pre_lbl(Label,Literal):- 
	@connected(Label,Host,Port),
    @external(network,substitutions(Host,Port,Literal),Subs),
    member(Literal,Subs). 
    
@broadcast(Pi):-
	@connected(_,Host,Port),
	@external(network,broadcast(Host,Port,Pi),_),
	fail.
@broadcast(_).

// Operation: instantiate norms
@instantiate_norms:-
    @scheme(Name,Pre,_,_,_,_,_,_), 
    Pre, 
    not(@ni(Name,Pre,_)), 
    uniqueassertz(@ni(Name,Pre,keep)), fail.
@instantiate_norms.

// Function: update
@update([plus(Rho)|Pi]):- uniqueassertz(Rho), @update(Pi).
@update([min(Rho)|Pi]):- retract(Rho), @update(Pi).
@update([update(Alpha)|Pi]):- Alpha \= @external(_,_,_), @perform_update(Alpha), @update(Pi).
@update([update(@external(Source,Call,Return))|Pi]):- @external(Source,Call,Return), @update(Pi).
@update([@lbl(_,_)|Pi]):- @update(Pi).
@update([]).

// Function: extract
@extract(Label,[@lbl(Label,Mod)|Pi],[Mod|R]):- @extract(Label,Pi,R).
@extract(Label,[plus(_)|Pi],R):- @extract(Label,Pi,R).
@extract(Label,[min(_)|Pi],R):- @extract(Label,Pi,R).
@extract(Label,[update(_)|Pi],R):- @extract(Label,Pi,R).
@extract(Label,[@external(_,_,_)|Pi],R):- @extract(Label,Pi,R).
@extract(Label,[@lbl(Label2,Mod)|Pi],R):- 
    Label\=Label2, @extract(Label,Pi,R).
@extract(_,[],[]).

// Operation: update facts
@update_facts(Psi):- @id(ID), @extract(ID,Psi,Pi), @update(Pi).

// Function: can_clear
@can_clear(@ni(Name,Pre,keep)):-
    @scheme(Name,Pre,Pro,Obl,Dead,Exp,_,_), (Pro;(Obl;(Dead;Exp))),!.

// Function: mod
@mod(@ni(Name,Pre,keep),[]):- 
    @scheme(Name,Pre,_,_,_,Exp,_,_), Exp.
@mod(@ni(Name,Pre,keep),Obey):- 
    @scheme(Name,Pre,_,Obl,_,Exp,_,Obey), not(Exp), Obl.
@mod(@ni(Name,Pre,keep),Obey):- 
    @scheme(Name,Pre,Pro,_,_,Exp,_,Obey), not(Exp), Pro \= false, not(Pro).
@mod(@ni(Name,Pre,keep),Viol):- 
    @scheme(Name,Pre,_,Obl,_,Exp,Viol,_), not(Exp), Obl \= false, not(Obl).
@mod(@ni(Name,Pre,keep),Viol):- 
    @scheme(Name,Pre,Pro,_,_,Exp,Viol,_), not(Exp), Pro.

// Operation: clear norm
@clear_norms:-
    @ni(Name,Pre,keep),
    @can_clear(@ni(Name,Pre,keep)),
    @mod(@ni(Name,Pre,keep),Pi),
    @update(Pi),
    atomic_update(remove,3,@ni(Name,Pre,keep)),
    @broadcast(Pi),
    fail.
@clear_norms.

// Operation: perform update
@perform_update(Alpha):-
    @update(Phi,Alpha,Psi), 
    Phi, 
    //retract(@update_call(Alpha)),
    @update(Psi),
    @broadcast(Psi),!.
@perform_updates:-
	@update_call(Alpha),
	@perform_update(Alpha),
	fail.
@perform_updates.
	
// Execution policies
// was instantiated is needed for repeat until stable, because you reinstantiate if precondition and one of the flags both hold
@policy_1 :- @perform_updates.
@policy_2 :- @perform_updates, repeat_until_stable((@instantiate_norms,@clear_norms)), retractall(@ni(_,_,remove)).
@execution_cycle(Alpha):-
	@perform_update(Alpha),
	repeat_until_stable((@instantiate_norms,@clear_norms)),
	retractall(@ni(_,_,remove)).
	
@rule_closure(Type):- 
	assert(Type),
	repeat_until_stable( (@instantiate_norms,@clear_norms) ),
	retractall(@ni(_,_,remove)),
	retract(Type).
@execution_cycle_oopl(Alpha):- 
	@perform_update(Alpha), 
	@rule_closure(@countsas), 
	@rule_closure(@sanction). 