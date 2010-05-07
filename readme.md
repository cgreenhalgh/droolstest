p. Prototype to try using Drools rule/event engine as behaviour engine for ubicomp experiences, including basic notion of authoring and simulation.

p. Chris Greenhalgh, 2010-05-07

p. The broad plan is that the game rules are expressed as Drools rules, a single instance of a game (experience, etc.) is represented by a persistent stateful drools session, the authored game initial state is loaded at "game" startup as a set of facts, and the user actions are then represented by other facts being added/modified/removed. The effect on the player(s) is communicated by explicit rule RHS actions and/or creation/modification/removal of facts that are represented back to the user.
