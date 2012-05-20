package org.opentripplanner.routing.automata;

import java.util.ArrayList;
import java.util.List;

public class AutomatonState {

	private static final long serialVersionUID = 42L;

	/** Signals that no transition was found for a given input symbol. The input is rejected. */
	public static final int REJECT = Integer.MIN_VALUE;
	
	/** Could be used to provide a single accept state, using transitions on a special terminal from all other accept states. */
	public static final int ACCEPT = Integer.MAX_VALUE;

	private static char nextLabel = 'A';
	
	public String label;
	
	public final List<Transition> transitions = new ArrayList<Transition>(); 
	
	public final List<AutomatonState> epsilonTransitions = new ArrayList<AutomatonState>(); 
	
	/** Indicates whether input is to be accepted if parsing ends at this AutomatonState instance. */
	public boolean accept = false;

	public AutomatonState() {
		this(Character.toString(nextLabel++));
	}
	
	public AutomatonState(String label) {
		this.label = label;
		//System.out.println(this.toString());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AutomatonState ");
		sb.append(label);
		sb.append(" transitions ");
		for (Transition transition : this.transitions) {
			sb.append(transition.terminal);
			sb.append("-");
			sb.append(transition.target.label);
			sb.append(" ");
		}
		sb.append(" epsilon moves {");
		for (AutomatonState as : this.epsilonTransitions) {
			sb.append(as.label);
			sb.append(" ");
		}
		sb.append("}");
		return sb.toString();
	}
	
}
