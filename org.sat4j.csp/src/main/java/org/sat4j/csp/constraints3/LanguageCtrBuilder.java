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

import org.sat4j.csp.intension.ICspToSatEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
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

	// fromState -> var -> varValue -> solverVar
	private Map<String, Map<XVarInteger, Map<Long, Integer>>> transitionVarsMap = new HashMap<>();

	public LanguageCtrBuilder(ICspToSatEncoder solver) {
		this.solver = solver;
	}

	public boolean buildCtrRegular(String id, XVarInteger[] list, Object[][] objTransitions, String startState, String[] finalStates) {
		if(finalStates.length == 0) return true;
		final Map<String, List<RegularTransition>> transitions = buildTransitionMap(objTransitions);
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
		String startState = (String) objTransitions[0][0];
		Map<String, List<Transition>> transitions = extractTransitions(objTransitions, startState);
		int nTransitions = objTransitions.length;
		Map<String, Integer> layers = assignInvolvedVars(transitions, nTransitions, list, startState);
		String finalState = getFinalState(layers);
		try {
			buildAutomatonCtrs(transitions, new String[]{finalState});
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	private Map<String, Integer> assignInvolvedVars(Map<String, List<Transition>> transitions, int nTransitions,
			XVarInteger[] list, String startState) {
		Map<String, Integer> layers = new HashMap<>();
		layers.put(startState, 0);
		int setVars = 0;
		while(setVars < nTransitions) {
			for(List<Transition> trList : transitions.values()) {
				for(Transition tr : trList) {
					String trFromState = tr.getFromState();
					Integer layer = layers.get(trFromState);
					if(tr.getInvolvedVar() == null && layer != null) {
						tr.setInvolvedVar(list[layer]);
						layers.put(tr.getToState(), layer+1);
						if(layer == 0) tr.setFromStartState(true);
						++setVars;
					}
				}
			}
		}
		return layers;
	}

	private String getFinalState(Map<String, Integer> layers) {
		Map.Entry<String, Integer> maxLayer = null;
		for(Map.Entry<String, Integer> layer : layers.entrySet()) {
			if(maxLayer == null) {
				maxLayer = layer;
				continue;
			}
			if(layer.getValue() > maxLayer.getValue()) {
				maxLayer = layer;
			}
		}
		return maxLayer.getKey();
	}
	
	private void buildAutomatonCtrs(Map<String, List<Transition>> transitions, String[] finalStates) throws ContradictionException {
		Map<String, Integer> stateSolverVars = buildStateSolverVars(transitions);
		buildTransitionCtrs(stateSolverVars, transitions);
		buildFinalStateCtr(stateSolverVars, finalStates);
	}

	private Map<String, Integer> buildStateSolverVars(Map<String, List<Transition>> transitions) {
		Map<String, Integer> newVars = new HashMap<>();
		for(Map.Entry<String, List<Transition>> trEntry : transitions.entrySet()) {
			boolean shouldContinue = false;
			for(int i=0; i<trEntry.getValue().size(); ++i) {
				if(!trEntry.getValue().get(i).isFromStartState()) {
					shouldContinue = false;
					break;
				}
			}
			if(shouldContinue) continue;
			newVars.put(trEntry.getKey(), this.solver.newSatSolverVar());
		}
		return newVars;
	}

	private void buildTransitionCtrs(Map<String, Integer> stateSolverVars, Map<String, List<Transition>> transitions) throws ContradictionException {
		for(String toState : transitions.keySet()) {
			List<Integer> firstEqCl = new ArrayList<>();
			Integer toStateSolverVar = stateSolverVars.get(toState);
			firstEqCl.add(-toStateSolverVar);
			for(Transition tr : transitions.get(toState)) {
				Integer transitionVar = null;
				if(tr.isFromStartState()) {
					transitionVar = this.solver.getSolverVar(tr.getInvolvedVar().id, tr.getVarValue().intValue());
				} else {
					transitionVar = transitionVar(stateSolverVars, transitionVarsMap, tr);
				}
				firstEqCl.add(transitionVar);
				this.solver.addClause(new int[]{toStateSolverVar, -transitionVar});
			}
			int[] clArray = new int[firstEqCl.size()];
			for(int i=0; i<clArray.length; ++i) clArray[i] = firstEqCl.get(i);
			this.solver.addClause(clArray);
		}
	}


	private Integer transitionVar(Map<String, Integer> stateSolverVars, Map<String, Map<XVarInteger, Map<Long, Integer>>> transitionVarsMap, Transition tr) throws ContradictionException {
		Map<XVarInteger, Map<Long, Integer>> involvedVarMap = transitionVarsMap.get(tr.getFromState());
		if(involvedVarMap == null) {
			involvedVarMap = new HashMap<>();
			transitionVarsMap.put(tr.getFromState(), involvedVarMap);
		}
		Map<Long, Integer> varValuesMap = involvedVarMap.get(tr.getInvolvedVar());
		if(varValuesMap == null) {
			varValuesMap = new HashMap<>();
			involvedVarMap.put(tr.getInvolvedVar(), varValuesMap);
		}
		Integer solverVar = varValuesMap.get(tr.getVarValue());
		if(solverVar == null) {
			solverVar = createVarAssignmentSolverVar(stateSolverVars, tr);
			varValuesMap.put(tr.getVarValue(), solverVar);
		}
		return solverVar;
	}

	private Integer createVarAssignmentSolverVar(Map<String, Integer> stateSolverVars, Transition tr) throws ContradictionException {
		int assignmentVar = this.solver.getSolverVar(tr.getInvolvedVar().id, tr.getVarValue().intValue());
		int stateVar = stateSolverVars.get(tr.getFromState());
		int newVar = this.solver.newSatSolverVar();
		this.solver.addClause(new int[]{-newVar, stateVar});
		this.solver.addClause(new int[]{-newVar, assignmentVar});
		this.solver.addClause(new int[]{newVar, -stateVar, -assignmentVar});
		return newVar;
	}


	private void buildFinalStateCtr(Map<String, Integer> regularAuxVars, String[] finalStates) throws ContradictionException {
		int[] cl = new int[finalStates.length];
		for(int i=0; i<finalStates.length; ++i) cl[i] = regularAuxVars.get(finalStates[i]);
		this.solver.addClause(cl);
	}

	private Map<String, List<Transition>> extractTransitions(XVarInteger[] list, Object[][] objTransitions, String startState) {
		Map<String, List<Transition>> transitions = new HashMap<>();
		for(int i=0; i<objTransitions.length; ++i) {
			String destinationState = (String) objTransitions[i][2];
			String fromState = (String) objTransitions[i][0];
			if(destinationState.equals(fromState)) continue;
			Transition tr = null;
			if(list == null) {
				tr = new Transition(fromState, (Long) objTransitions[i][1], destinationState);
			} else {
				tr = new Transition(fromState, list[i], (Long) objTransitions[i][1], destinationState);
			}
			List<Transition> incoming = transitions.get(destinationState);
			if(fromState.equals(startState)) tr.setFromStartState(true);
			if(incoming == null) {
				incoming = new ArrayList<>();
				transitions.put(destinationState, incoming);
			}
			incoming.add(tr);
		}
		return transitions;
	}

	private Map<String, List<Transition>> extractTransitions(Object[][] objTransitions, String startState) {
		return extractTransitions(null, objTransitions, startState);
	}

	private class Transition {
		private String fromState;
		private XVarInteger involvedVar;
		private Long varValue;
		private String toState;
		private boolean fromStartState = false;

		public Transition(String fromState, Long varValue, String toState) {
			this.fromState = fromState;
			this.varValue = varValue;
			this.toState = toState;
		}

		public Transition(String fromState, XVarInteger involvedVar, Long varValue, String toState) {
			this(fromState, varValue, toState);
			this.involvedVar = involvedVar;
		}

		private String getFromState() {
			return fromState;
		}

		public String getToState() {
			return this.toState;
		}

		private XVarInteger getInvolvedVar() {
			return involvedVar;
		}

		private void setInvolvedVar(XVarInteger var) {
			this.involvedVar = var;
		}

		private Long getVarValue() {
			return varValue;
		}

		public boolean isFromStartState() {
			return fromStartState;
		}

		public void setFromStartState(boolean fromStartState) {
			this.fromStartState = fromStartState;
		}

		@Override
		public String toString() {
			return "( "+fromState+" --["+involvedVar.id+"="+varValue+"]--> "+toState+" )";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((fromState == null) ? 0 : fromState.hashCode());
			result = prime * result + ((involvedVar == null) ? 0 : involvedVar.hashCode());
			result = prime * result + ((toState == null) ? 0 : toState.hashCode());
			result = prime * result + ((varValue == null) ? 0 : varValue.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Transition other = (Transition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (fromState == null) {
				if (other.fromState != null)
					return false;
			} else if (!fromState.equals(other.fromState))
				return false;
			if (involvedVar == null) {
				if (other.involvedVar != null)
					return false;
			} else if (!involvedVar.id.equals(other.involvedVar.id))
				return false;
			if (toState == null) {
				if (other.toState != null)
					return false;
			} else if (!toState.equals(other.toState))
				return false;
			if (varValue == null) {
				if (other.varValue != null)
					return false;
			} else if (!varValue.equals(other.varValue))
				return false;
			return true;
		}

		private LanguageCtrBuilder getOuterType() {
			return LanguageCtrBuilder.this;
		}

	}

}
