package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;

import static org.semanticweb.ontop.executor.join.JoinExtractionUtils.*;

/**
* TODO: explain
*/
public class JoinBooleanExpressionExecutor implements InternalProposalExecutor<InnerJoinOptimizationProposal> {

    /**
     * Standard method (InternalProposalExecutor)
     */
    @Override
    public void apply(InnerJoinOptimizationProposal proposal, IntermediateQuery query,  QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        transformJoin(proposal.getTopJoinNode(), query, treeComponent);
    }

    /**
     * Direct method (without proposal)
     * RESERVED TO COMPOSITE InternalProposalExecutor<InnerJoinOptimizationProposal>!
     */
    protected Optional<InnerJoinNode> transformJoin(InnerJoinNode topJoinNode, IntermediateQuery query,
                                          QueryTreeComponent treeComponent) {


        ImmutableList<JoinOrFilterNode> filterOrJoinNodes = extractFilterAndInnerJoinNodes(topJoinNode, query);

        Optional<ImmutableBooleanExpression> optionalAggregatedFilterCondition;
        try {
            optionalAggregatedFilterCondition = extractFoldAndOptimizeBooleanExpressions(filterOrJoinNodes);
        }
        /**
         * The filter condition can be satisfied --> the join node and its sub-tree is thus removed from the tree.
         * Returns no join node.
         */
        catch (InsatisfiedExpressionException e) {
            treeComponent.removeNodeAndItsSubTree(topJoinNode);
            return Optional.absent();
        }

        InnerJoinNode newJoinNode = new InnerJoinNodeImpl(optionalAggregatedFilterCondition);

        /**
         * TODO: only if the filter condition are not violated!
         */
        try {
            treeComponent.replaceNodesByOneNode(ImmutableList.<QueryNode>copyOf(filterOrJoinNodes), newJoinNode, true);
        } catch (IllegalTreeUpdateException e) {
            throw new RuntimeException("Internal error: " + e.getMessage());
        }

        return Optional.of(newJoinNode);
    }



}
