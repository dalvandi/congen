package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class Invariants{

	public ArrayList<String> getInvariants(IMachineRoot mch,
			ArrayList<String> vars, ArrayList<String> types) throws RodinDBException {
		ASTBuilder itb = new ASTBuilder(vars, types);
		ASTTranslator tr = new ASTTranslator();
		ArrayList<String> invs = new ArrayList<String>();
		
		for(IInvariant inv : mch.getInvariants())
		{
			if(!isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch), vars, types))
			{
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch);
				invs.add(tr.translateASTTree(n));

			}

		}
		
		
		return invs;
	}

	
	

	private boolean isTypingTree(ASTTreeNode node, ArrayList<String> vars, ArrayList<String> types) {

		Types t = new Types(); 

		if(node.tag != 107  && node.tag != 111) //????)
			return false;
		else 
		{
			if(vars.contains(node.children.get(0).content))
					{
					if(types.contains(node.children.get(1).content) || t.builtin_types.contains(node.children.get(1).tag) ||  t.extended_types.contains(node.children.get(1).content))
					{
						return true;
					}
					}
			
		}
		return false;
			
	}
}
