package colibri.app;
import java.util.Iterator;

import colibri.lib.Concept;
import colibri.lib.HybridLattice;
import colibri.lib.Lattice;
import colibri.lib.Relation;
import colibri.lib.Traversal;
import colibri.lib.TreeRelation;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Relation rel = new TreeRelation();
		rel.add("o1", "a1");
		rel.add("o1", "a2");
		rel.add("o2", "a1");
	
		
		Lattice lattice = new HybridLattice (rel);
		Iterator<Concept> it = lattice.conceptIterator
		                       (Traversal.TOP_ATTRSIZE);
		while (it.hasNext()) {
		    Concept c = it.next();
		    if (c.getAttributes().size() > 10)
		break;
		    System.out.println(c.toString());
		}

	}

}
