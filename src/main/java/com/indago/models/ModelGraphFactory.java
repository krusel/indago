/**
 *
 */
package com.indago.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.indago.data.segmentation.ConflictGraph;
import com.indago.data.segmentation.LabelingSegment;
import com.indago.fg.Assignment;
import com.indago.fg.AssignmentMapper;
import com.indago.models.assignments.AssignmentVars;
import com.indago.models.segments.ConflictSet;
import com.indago.models.segments.SegmentVar;
import com.indago.old_fg.CostsFactory;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

/**
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ModelGraphFactory {

	public static Pair< SegmentationModel, AssignmentMapper< SegmentVar, LabelingSegment > > createSegmentationProblem(
			final Collection< LabelingSegment > segments,
			final ConflictGraph< LabelingSegment > conflicts,
			final CostsFactory< ? super LabelingSegment > segmentCosts ) {

		final ArrayList< SegmentVar > segmentVars = new ArrayList< >();

		final Map< LabelingSegment, SegmentVar > varmap = new HashMap< >();
		for ( final LabelingSegment segment : segments ) {
			final SegmentVar var = new SegmentVar( segment, segmentCosts.getCost( segment ) );
			varmap.put( segment, var );
			segmentVars.add( var );
		}

		final Collection< ConflictSet > conflictSets = new ArrayList< >();

		for ( final Collection< LabelingSegment > clique : conflicts.getConflictGraphCliques() ) {
			final ConflictSet cs = new ConflictSet();
			for ( final LabelingSegment seg : clique )
				cs.add( varmap.get( seg ) );
			conflictSets.add( cs );
		}

		final SegmentationModel problem = new SegmentationModel() {

			@Override
			public Collection< SegmentVar > getSegments() {
				return segmentVars;
			}

			@Override
			public Collection< ConflictSet > getConflictSets() {
				return conflictSets;
			}

			@Override
			public int getTime() {
				return 0;
			}

			@Override
			public AssignmentVars getOutAssignments() {
				return null;
			}

			@Override
			public AssignmentVars getInAssignments() {
				return null;
			}
		};

		final AssignmentMapper< SegmentVar, LabelingSegment > mapper =
				new AssignmentMapper< SegmentVar, LabelingSegment >() {
			@Override
					public Assignment< LabelingSegment > map(
							final Assignment< SegmentVar > assignment ) {
						return new Assignment< LabelingSegment >() {
					@Override
							public boolean isAssigned( final LabelingSegment variable ) {
						return assignment.isAssigned( varmap.get( variable ) );
					}

					@Override
							public int getAssignment( final LabelingSegment variable ) {
						return assignment.getAssignment( varmap.get( variable ) );
					}
				};
			}
		};

		return new ValuePair< >( problem, mapper );
	}

}