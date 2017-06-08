/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2016 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *******************************************************************************/
package org.sat4j.csp.constraints3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sat4j.csp.intension.ICspToSatEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to language (regular, MDD) constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class LanguageCtrBuilder {

	/** the solver in which the problem is encoded */
	private ICspToSatEncoder solver;

	public LanguageCtrBuilder(ICspToSatEncoder solver) {
		this.solver = solver;
	}

	public boolean buildCtrRegular(String id, XVarInteger[] list, Object[][] objTransitions, String startState, String[] finalStates) {
		final Map<String, List<RegularTransition>> transitions = buildTransitionMap(objTransitions);
		return buildCtrRegular(list, transitions, startState, finalStates);
	}

	private boolean buildCtrRegular(XVarInteger[] list, final Map<String, List<RegularTransition>> transitions,
			String startState, String[] finalStates) {
		if(finalStates.length == 0) return true;
		final Set<String> reachable = computeReachableStates(startState, transitions);
		final Map<String, Map<String, Integer>> reachabilityVars = createReachabilitySolverVars(list, reachable);
		int finalStateClause[] = new int[finalStates.length];
		for(int i=0; i<finalStates.length; ++i) {
			finalStateClause[i] = reachabilityVars.get(finalStates[i]).get(list[list.length-1].id); // be in a final state after consuming last var
		}
		this.solver.addClause(finalStateClause);
		for(final String finalState : finalStates) {
			enforceReachability(finalState, list, transitions, reachabilityVars, startState, list.length-1);
		}
		return false;
	}

	private Map<String, Map<String, Integer>> createReachabilitySolverVars(XVarInteger[] list,
			final Set<String> reachable) {
		Map<String, Map<String, Integer>> reachabilityVars = new HashMap<>(); // (state, l[i]) -> solverVar
		for(final String state : reachable) {
			final Map<String, Integer> varMap = new HashMap<>();
			for(final XVarInteger var : list) {
				varMap.put(var.id, this.solver.newSatSolverVar());
			}
			reachabilityVars.put(state, varMap);
		}
		return reachabilityVars;
	}

	private Set<String> computeReachableStates(String startState,
			final Map<String, List<RegularTransition>> transitions) {
		final Set<String> reachable = new HashSet<>();
		reachable.add(startState);
		int reachableTempSize = 0;
		while(reachableTempSize < reachable.size()) {
			reachableTempSize = reachable.size();
			for(final String toState : transitions.keySet()) {
				for(final RegularTransition transition : transitions.get(toState)) {
					if(reachable.contains(transition.getFromState())) {
						reachable.add(transition.getToState());
					}
				}
			}
		}
		return reachable;
	}

	private Map<String, List<RegularTransition>> buildTransitionMap(Object[][] objTransitions) {
		final Map<String, List<RegularTransition>> transitions = new HashMap<>(objTransitions.length);
		for(final Object[] transition : objTransitions) {
			final String toState = (String) transition[2];
			List<RegularTransition> toStateTransitions = transitions.get(toState);
			if(toStateTransitions == null) {
				toStateTransitions = new ArrayList<>();
				transitions.put(toState, toStateTransitions);
			}
			toStateTransitions.add(new RegularTransition((String) transition[0], (long) transition[1], toState));
		}
		return transitions;
	}

	private void enforceReachability(String toState, XVarInteger[] list,
			Map<String, List<RegularTransition>> transitions, Map<String, Map<String, Integer>> reachabilityVars, String startState, int listIndex) {
		if(listIndex == 0) {
			enforceReachabilityOfStartState(toState, startState, list[0], transitions, reachabilityVars);
			return;
		}
		final List<RegularTransition> toTransitions = transitions.get(toState);
		final List<Integer> tseitinVars = new ArrayList<>(toTransitions.size());
		for(final RegularTransition tr : toTransitions) {
			final int fromVar = reachabilityVars.get(tr.getFromState()).get(list[listIndex-1].id);
			int valueVar;
			try {
				valueVar = this.solver.getSolverVar(list[listIndex].id, (int) tr.getValue());
			} catch (IllegalArgumentException e) {
				continue;
			}
			final int tseitinVar = this.solver.newSatSolverVar();
			this.solver.addClause(new int[]{-tseitinVar, fromVar});
			this.solver.addClause(new int[]{-tseitinVar, valueVar});
			this.solver.addClause(new int[]{-fromVar, -valueVar, tseitinVar});
			tseitinVars.add(tseitinVar);
		}
		final int toVar = reachabilityVars.get(toState).get(list[listIndex].id);
		final int nTseitinVars = tseitinVars.size();
		final int implCl[] = new int[nTseitinVars+1];
		for(int i=0; i<nTseitinVars; ++i) {
			final Integer var = tseitinVars.get(i);
			implCl[i] = var;
			this.solver.addClause(new int[]{-var, toVar});
		}
		implCl[nTseitinVars] = -toVar;
		this.solver.addClause(implCl);
		for(final RegularTransition tr : toTransitions) {
			enforceReachability(tr.getFromState(), list, transitions, reachabilityVars, startState, listIndex-1);
		}
	}

	private void enforceReachabilityOfStartState(String toState, String startState, XVarInteger var,
			Map<String, List<RegularTransition>> transitions, Map<String, Map<String, Integer>> reachabilityVars) {
		final int toVar = reachabilityVars.get(toState).get(var.id);
		final List<RegularTransition> toTransitions = transitions.get(toState);
		if(toTransitions == null) {
			this.solver.addClause(new int[]{-toVar});
			return;
		}
		final List<Integer> valueVars = new ArrayList<>();
		for(final RegularTransition tr : toTransitions) {
			if(!tr.getFromState().equals(startState)) {
				continue;
			}
			int valueVar;
			try {
				valueVar = this.solver.getSolverVar(var.id, (int) tr.getValue());
			} catch (IllegalArgumentException e) {
				continue;
			}
			valueVars.add(valueVar);
			this.solver.addClause(new int[]{-valueVar, toVar});
		}
		final int implCl[] = new int[valueVars.size()+1];
		for(int i=0; i<valueVars.size(); ++i) {
			implCl[i] = valueVars.get(i);
		}
		implCl[valueVars.size()] = -toVar;
		this.solver.addClause(implCl);
	}

	public class RegularTransition {
		private String fromState;
		private long value;
		private String toState;

		private RegularTransition(final String fromState, final long value, final String toState) {
			this.fromState = fromState;
			this.value = value;
			this.toState = toState;
		}

		public String getFromState() {
			return fromState;
		}

		public long getValue() {
			return value;
		}

		public String getToState() {
			return toState;
		}

		@Override
		public String toString() {
			return fromState+" --["+value+"]--> "+toState;
		}
	}

	public boolean buildCtrMDD(String id, XVarInteger[] list, Object[][] objTransitions) {
		final MddToRegularHelper helper = new MddToRegularHelper(list, objTransitions);
		return helper.buildCtr();
	}

	private class MddToRegularHelper {
		
		private XVarInteger[] list;
		
		private Map<String, List<RegularTransition>> transitions;
		
		private String startState;
		
		private String finalState;

		private MddToRegularHelper(final XVarInteger[] list, final Object[][] objTransitions)  {
			this.list = list;
			this.transitions = buildTransitionMap(objTransitions);
		}
		
		private boolean buildCtr() {
			computeRootAndFinalNodes();
			return buildCtrRegular(list, this.transitions, this.startState, new String[]{this.finalState});
		}

		private void computeRootAndFinalNodes() {
			final Set<String> states = new HashSet<>();
			this.transitions.values().stream().forEach(list -> list.stream().forEach(tr -> {states.add(tr.getFromState());states.add(tr.getToState());}));
			final Map<String, Integer> level = new HashMap<>();
			states.stream().filter(state -> !this.transitions.containsKey(state)).collect(Collectors.toList()).stream().forEach(state -> level.put(state, 0));
			if(level.size() != 1) {
				throw new IllegalArgumentException("not exactly one root node in MDD");
			}
			this.startState = level.keySet().iterator().next();
			states.removeAll(level.keySet());
			int oldSize = states.size();
			int i;
			for(i = 1; !states.isEmpty(); ++i){
				final Integer currentState = i;
				List<String> newLevel = states.stream().filter(state -> transitions.get(state).stream().anyMatch(tr -> Integer.valueOf(currentState-1).equals(level.get(tr.getFromState())))).collect(Collectors.toList());
				states.removeAll(newLevel);
				newLevel.stream().forEach(state -> level.put(state, currentState));
				if(states.size() == oldSize) {
					throw new IllegalArgumentException("loop in MDD");
				}
				oldSize = states.size();
			}
			final Integer maxLevel = i-1;
			List<String> finalStates = level.keySet().stream().filter(state -> level.get(state).equals(maxLevel)).collect(Collectors.toList());
			if(finalStates.size() > 1) {
				throw new IllegalArgumentException("more than one final node in MDD");
			}
			this.finalState = finalStates.iterator().next();
		}
	}

}
