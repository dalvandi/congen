package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class VariablesDeclaration {

	
	
	public ArrayList<String> getVariablesDeclaration(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types) throws RodinDBException
	{
		ASTBuilder itb = new ASTBuilder();
		ArrayList<String> vars_decl = new ArrayList<String>();
		ASTTranslator tr = new ASTTranslator();
		
		for(IInvariant inv : mch.getInvariants())
		{
			if(isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch, mch.getFormulaFactory()), vars, types))
			{
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch, mch.getFormulaFactory()); 
				// TO DO: It is better to set metadata datas when we are building the tree. Especially isType and isVariable
				n.children.get(1).isType = true;
				vars_decl.add(tr.translateASTTree(n));
			}
		}
		
		return vars_decl;
		
	}

	private boolean isTypingTree(ASTTreeNode node, ArrayList<String> vars, ArrayList<String> types) {

		if(node.tag != 107 )
			return false;
		else 
		{
			if(vars.contains(node.children.get(0).content))
					{
					if(types.contains(node.children.get(1).content) || node.children.get(1).tag == 401 ||  node.children.get(1).tag == 1001)
					{
						return true;
					}
					}
			
		}
		return false;
			
	}
}