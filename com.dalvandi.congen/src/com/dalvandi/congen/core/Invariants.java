package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;


public class Invariants{

	
	public ASTTreeNode getInvariantsNode(IMachineRoot mch,
			ArrayList<String> vars, ArrayList<String> types) throws RodinDBException {
		
		ASTBuilder itb = new ASTBuilder(vars, types);
		ASTTreeNode invs = new ASTTreeNode("Invariants","Invariants", 9521);
		ASTTreeNode and = new ASTTreeNode("And","&&", 351);
		invs.addNewChild(and);
		int numberOfInv = 0;
		
		for(IInvariant inv : mch.getInvariants())
		{
			if(!isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch), vars, types)  && isGluing(inv) && !inv.isTheorem()) 
			{
				ASTTreeNode inva = new ASTTreeNode("Invariant","Inv", 9545);
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch);
				
				inva.addNewChild(n);
				and.addNewChild(inva);
				numberOfInv++;
			}
		}
		
		if(numberOfInv == 0)
		{
				return new ASTTreeNode("Empty Node","", 9997);
		}	
		
		return invs;
	}
	

	private boolean isGluing(IInvariant inv) throws RodinDBException {

		String label = inv.getLabel();
		if(label.contains("g_"))
			return false;
		else
			return true;
		
	}




	private boolean isTypingTree(ASTTreeNode node, ArrayList<String> vars, ArrayList<String> types) {

		Types t = new Types(); 

		if(node.tag != 107  && node.tag != 111) //????)
			return false;
		else 
		{
			if(vars.contains(node.children.get(0).getContent()))
					{
					if(types.contains(node.children.get(1).getContent()) || t.builtin_types.contains(node.children.get(1).tag) ||  t.extended_types.contains(node.children.get(1).getContent()))
					{
						return true;
					}
					}
			
		}
		return false;
			
	}



	@Deprecated
	public ArrayList<String> getInvariants(IMachineRoot mch,
			ArrayList<String> vars, ArrayList<String> types) throws RodinDBException {
		ASTBuilder itb = new ASTBuilder(vars, types);
		ASTTranslator tr = new ASTTranslator();
		ArrayList<String> invs = new ArrayList<String>();
		
		for(IInvariant inv : mch.getInvariants())
		{
			if(!isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch), vars, types) && !inv.isTheorem() && isGluing(inv))
			{
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch);
				invs.add(tr.translateASTTree(n));
			}

		}
		
		
		return invs;
	}

	

}
