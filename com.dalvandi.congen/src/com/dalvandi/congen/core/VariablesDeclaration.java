package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class VariablesDeclaration {

	
	/*
	 * This method returns declaration of variables.
	 * Its arguments are (machine, list of variables, list of types(carrier sets))  
	 * 
	 */

	public ArrayList<String> getVariablesDeclaration(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types) throws RodinDBException
	{
		ASTBuilder itb = new ASTBuilder(vars, types);
		ArrayList<String> vars_decl = new ArrayList<String>();
		ASTTranslator tr = new ASTTranslator();
		
		for(IInvariant inv : mch.getInvariants())
		{
			if(isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch), vars, types))
			{
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch); 
				// TO DO: It is better to set meta-data data when we are building the tree. Especially isType and isVariable
				n.children.get(1).isType = true;
				vars_decl.add(tr.translateASTTree(n));
			}
		}
		
		return vars_decl;
		
	}

	private boolean isTypingTree(ASTTreeNode node, ArrayList<String> vars, ArrayList<String> types) {

		if(node.tag != 107 && node.tag != 111 )
			return false;
		else 
		{
			if(vars.contains(node.children.get(0).content))
					{
				// This only checks for types or int or seq
				// TO DO: it is not enough. other types like boolean, sets and etc are not included. There should be an arraylist containing
				// tags of all possible types.
				if(types.contains(node.children.get(1).content) || node.children.get(1).tag == 401 ||  node.children.get(1).tag == 1001)
					{
						return true;
					}
					}
			
		}
		return false;
			
	}
}
