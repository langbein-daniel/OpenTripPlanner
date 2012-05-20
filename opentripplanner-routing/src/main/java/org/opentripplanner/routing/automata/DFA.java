package org.opentripplanner.routing.automata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

/**
 * A deterministic finite automaton, with a transition table for fast incremental parsing. 
 * @author abyrd
 */
public class DFA extends NFA {

	final int[][] table;
	final List<Integer> acceptStates = new ArrayList<Integer>();
	
	/** Build a deterministic finite automaton from an existing, potentially nondeterministic one. */
	public DFA(NFA nfa) {
		super(new AutomatonState("START"), nfa.nt);
		table = determinize(nfa);
	}

	/** Build a deterministic finite automaton that accepts the given nonterminal from a grammar */
	public DFA(Nonterminal nonterminal) {
		this(new NFA(nonterminal));
	}

	/** 
	 * A glorified set of automaton states, used in determinizing NFAs.
	 * Semantic equality inherited from HashSet is needed for determinization. 
	 */
	private class DFAState extends HashSet<AutomatonState> {
		private static final long serialVersionUID = 1L;
		DFAState(AutomatonState... automatonStates) {
			this.addAll(Arrays.asList(automatonStates));
		}
		DFAState(Collection<AutomatonState> automatonStates) { 
			this.addAll(automatonStates);
		}
		public void followEpsilons() {
			Queue<AutomatonState> queue = new LinkedList<AutomatonState>();
			queue.addAll(this);
			while ( ! queue.isEmpty()) {
				AutomatonState as = queue.poll();
				for (AutomatonState target : as.epsilonTransitions)
					if (this.add(target))
						queue.add(target);
			}
		}
		public String deriveLabel() {
			StringBuilder sb = new StringBuilder();
			for (AutomatonState as : this)
				sb.append(as.label);
			return sb.toString();
		}
	}
	
	/** convert a (potentially) nondeterministic automaton into a new deterministic automaton with no epsilon edges */
	private int[][] determinize(NFA nfa) {
		
		int maxTerminal = 0; // track the max token value so we know how big to make the transition table

		Map<DFAState, AutomatonState> newStates = new HashMap<DFAState, AutomatonState>();
		Queue<DFAState> queue = new LinkedList<DFAState>();
		
		DFAState dStart = new DFAState(nfa.start);
		dStart.followEpsilons();
		newStates.put(dStart, this.start);
		queue.add(dStart);
		
		Set<AutomatonState> acceptStates = new HashSet<AutomatonState>();
		
		while ( ! queue.isEmpty()) {
			DFAState ds = queue.poll();
			AutomatonState fromAutomatonState = newStates.get(ds);
			Map<Integer, DFAState> transitions = new HashMap<Integer, DFAState>();
			for (AutomatonState as : ds) {
				if (as.accept) {
					acceptStates.add(fromAutomatonState); // multiple adds OK, it's a set
					fromAutomatonState.accept = true;
				}
				for (Transition t : as.transitions) {
					if (t.terminal > maxTerminal)
						maxTerminal = t.terminal;
					DFAState targets = transitions.get(t.terminal);
					if (targets == null) {
						targets = new DFAState();
						transitions.put(t.terminal, targets);
					}
					targets.add(t.target);
				}
			}
			for (Entry<Integer, DFAState> transition : transitions.entrySet()) {
				int token = transition.getKey();
				DFAState target = transition.getValue();
				target.followEpsilons();
				AutomatonState toAutomatonState = newStates.get(target);
				if (toAutomatonState == null) {
					toAutomatonState = new AutomatonState(target.deriveLabel());
					newStates.put(target, toAutomatonState);
					queue.add(target);
				}
				fromAutomatonState.transitions.add(new Transition(token, toAutomatonState));
			}
		}
		
		/* create a transition table, filled with the reserved value for the reject state */
		int[][] table = new int[newStates.size()][maxTerminal+1];
		for (int[] row : table) {
			Arrays.fill(row, AutomatonState.REJECT);
		}
		
		/* assign integer ids to all new DFA states */
		Map<AutomatonState, Integer> stateNumbers = new HashMap<AutomatonState, Integer>();
		int i = 0;
		for (AutomatonState as : newStates.values())
			stateNumbers.put(as,  i++);
		
		/* copy all edges from the new DFA states into the table */ 
		for (AutomatonState as :  newStates.values()) {
			int row = stateNumbers.get(as);
			for (Transition t : as.transitions)
				table[row][t.terminal] = stateNumbers.get(t.target);
			row++;
		}
		
		/* save the integer ids for the accept states */
		for (AutomatonState as : acceptStates)
			this.acceptStates.add(stateNumbers.get(as));
		
		/* return the transition table to the DFA constructor */
		return table;
	}

	/** Dump the transition table to a string. Rows are states, columns are terminal symbols. */ 
	public String dumpTable() {
		StringBuilder sb = new StringBuilder();
		for (int[] row : table) {
			for (int i : row) {
				if (i == AutomatonState.REJECT)
					sb.append("-- ");
				else
					sb.append(String.format("%02d ", i));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/** Return true if this DFA (and the nonterminal it is built from) match the given sequence of terminal symbols. */ 
	public boolean parse(int... symbols) {
		int state = 0;
		for (int sym : symbols) {
			state = table[state][sym];
			if (state == AutomatonState.REJECT)
				return false;
		}
		return acceptStates.contains(state);
	}
}
