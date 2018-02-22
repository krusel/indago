package com.indago.fg;

import java.util.List;

import com.indago.pg.IndicatorNode;
import com.indago.util.Bimap;

public class MappedDiverseFactorGraph {

	private final UnaryCostConstraintGraph fg;
	private final List< Bimap< IndicatorNode, Variable > > listOfVarMaps;
	private final List< AssignmentMapper< Variable, IndicatorNode > > listOfAssignmentMaps;

	public MappedDiverseFactorGraph(
			final UnaryCostConstraintGraph fg,
			final List< Bimap< IndicatorNode, Variable > > listOfVarMaps,
			final List< AssignmentMapper< Variable, IndicatorNode > > listOfAssignmentMaps ) {
		this.fg = fg;
		this.listOfVarMaps = listOfVarMaps;
		this.listOfAssignmentMaps = listOfAssignmentMaps;
	}

	/**
	 * @return the fg
	 */
	public UnaryCostConstraintGraph getFg() {
		return fg;
	}

	/**
	 * @return the listOfVarMaps
	 */
	public List<Bimap<IndicatorNode, Variable>> getListOfVarMaps() {
		return listOfVarMaps;
	}

	/**
	 * @return the listOfAssignmentMaps
	 */
	public List<AssignmentMapper<Variable, IndicatorNode>> getListOfAssignmentMaps() {
		return listOfAssignmentMaps;
	}

}
