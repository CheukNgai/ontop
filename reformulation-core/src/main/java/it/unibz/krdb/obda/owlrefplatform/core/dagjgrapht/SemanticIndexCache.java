package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticIndexCache {

	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;
	
	
	private Map<OClass, List<Interval>> classIntervals = new HashMap<>();
	private Map<Predicate, List<Interval>> roleIntervals = new HashMap<>();
//	private Map<ObjectPropertyExpression, List<Interval>> opeIntervals = new HashMap<>();
//	private Map<DataPropertyExpression, List<Interval>> dpeIntervals = new HashMap<>();

	private Map<OClass, Integer> classIndexes = new HashMap<>();
	private Map<String, Integer> roleIndexes = new HashMap<>();

	private final TBoxReasoner reasonerDag; 
	
	public SemanticIndexCache(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		
		//create the indexes		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(reasonerDag);
		
		/*
		 * Creating cache of semantic indexes and ranges
		 */
		for (ClassExpression description : engine.getIndexedClasses()) {
			OClass cdesc = (OClass) description;
			int idx = engine.getIndex(cdesc);
			List<Interval> intervals = engine.getIntervals(cdesc);

			classIndexes.put(cdesc, idx);
			classIntervals.put(cdesc, intervals);
		} 
		for (ObjectPropertyExpression cdesc : engine.getIndexedObjectProperties()) {
			if (cdesc.isInverse()) {
				/* Inverses don't get indexes or intervals */
				continue;
			}

			int idx = engine.getIndex(cdesc);
			List<Interval> intervals = engine.getIntervals(cdesc);

			String iri = cdesc.getPredicate().getName();
			roleIndexes.put(iri, idx);
			roleIntervals.put(cdesc.getPredicate(), intervals);
		} 
		for (DataPropertyExpression cdesc : engine.getIndexedDataProperties()) {

			int idx = engine.getIndex(cdesc);
			List<Interval> intervals = engine.getIntervals(cdesc);

			String iri = cdesc.getPredicate().getName();
			roleIndexes.put(iri, idx);
			roleIntervals.put(cdesc.getPredicate(), intervals);
		} 
	}

	/***
	 * Returns the index (semantic index) for a class or property. 
	 */

	public int getIndex(OClass c) {
		Integer index = classIndexes.get(c);
		
		if (index == null) {
			/* direct name is not indexed, maybe there is an equivalent */
			OClass equivalent = (OClass)reasonerDag.getClassDAG().getVertex(c).getRepresentative();
			return classIndexes.get(equivalent);
		}
				
		return index;
	}
	
	public int getIndex(ObjectPropertyExpression p) {
		String name = p.getPredicate().getName();
		Integer index = roleIndexes.get(name);
		
		if (index == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			ObjectPropertyExpression equivalent = reasonerDag.getObjectPropertyDAG().getVertex(p).getRepresentative();
			
			Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
			
			if (index1 != null)
				return index1;
			
			// TODO: object property equivalent failed, we now look for data property equivalent 
			
			System.out.println(name + " IN " + roleIndexes);
		}
		
		return index;				
	}

	public int getIndex(DataPropertyExpression p) {
		String name = p.getPredicate().getName();
		Integer index = roleIndexes.get(name);
		
		if (index == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			DataPropertyExpression equivalent = reasonerDag.getDataPropertyDAG().getVertex(p).getRepresentative();
			
			Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
			
			if (index1 != null)
				return index1;
			
			// TODO: object property equivalent failed, we now look for data property equivalent 
			
			System.out.println(name + " IN " + roleIndexes);
		}
		
		return index;				
	}
	
	public void setIndex(OClass concept, Integer idx) {
		classIndexes.put(concept, idx);
	}
		
	public void setIndex(ObjectPropertyExpression ope, Integer idx) {
		roleIndexes.put(ope.getPredicate().getName(), idx);
	}

	public void setIndex(DataPropertyExpression dpe, Integer idx) {
		roleIndexes.put(dpe.getPredicate().getName(), idx);
	}
	
	/***
	 * Returns the intervals (semantic index) for a class or property. The String
	 * identifies a class if type = 1, identifies a property if type = 2.
	 * 
	 * @param name
	 * @param i
	 * @return
	 * @throws OBDAException 
	 */
	public List<Interval> getIntervals(OClass concept)  {
		List<Interval> intervals = classIntervals.get(concept);
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + concept
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
	}
	
	public List<Interval> getIntervals(ObjectPropertyExpression ope)  {
		
		List<Interval> intervals = roleIntervals.get(ope.getPredicate());
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + ope
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
	}

	public List<Interval> getIntervals(DataPropertyExpression dpe)  {
		
		List<Interval> intervals = roleIntervals.get(dpe.getPredicate());
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + dpe
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
	}
	
	public void setIntervals(OClass concept, List<Interval> intervals) {
		classIntervals.put(concept, intervals);
	}
	
	public void setIntervals(ObjectPropertyExpression ope, List<Interval> intervals) {
		roleIntervals.put(ope.getPredicate(), intervals);
	}
	
	public void setIntervals(DataPropertyExpression dpe, List<Interval> intervals) {
		roleIntervals.put(dpe.getPredicate(), intervals);
	}
	
	public void clear() {
		classIndexes.clear();
		classIntervals.clear();
		roleIndexes.clear();
		roleIntervals.clear();
//		opeIntervals.clear();		
//		dpeIntervals.clear();		
	}


	/*
	 * these four methods are used only by SI Repository to save the metadata
	 */
	
	public Set<Entry<OClass, Integer>> getClassIndexEntries() {
		return classIndexes.entrySet();
	}
		
	public Set<Entry<String,Integer>> getRoleIndexEntries() {
		return roleIndexes.entrySet();
	}
	
	public Set<Entry<OClass, List<Interval>>> getClassIntervalsEntries() {
		return classIntervals.entrySet();
	}
	
	public Set<Entry<Predicate, List<Interval>>> getPropertyIntervalsEntries() {
		return roleIntervals.entrySet();
	}
/*	
	public Set<Entry<ObjectPropertyExpression, List<Interval>>> getObjectPropertyIntervalsEntries() {
		return opeIntervals.entrySet();
	}
	public Set<Entry<DataPropertyExpression, List<Interval>>> getDataPropertyIntervalsEntries() {
		return dpeIntervals.entrySet();
	}
*/	
}
