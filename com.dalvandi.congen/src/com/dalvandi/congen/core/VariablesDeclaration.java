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
				if(n.tag == 111)
					n.tag = 9111;
				n.children.get(1).isType = true;
				vars_decl.add(tr.translateASTTree(n));
			}
		}
		
		return vars_decl;
		
	}

	
	
	/*
	 * This method returns the root node of variables tree.
	 * Its arguments are (machine, list of variables, list of types(carrier sets))  
	 * 
	 */

	public ASTTreeNode getVariablesNode(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types) throws RodinDBException
	{
		ASTBuilder itb = new ASTBuilder(vars, types);
		ASTTreeNode vars_dec = new ASTTreeNode("Class Variables", "", 9520);
		for(IInvariant inv : mch.getInvariants())
		{
			if(isTypingTree(itb.treeBuilder(inv.getPredicateString(), mch), vars, types))
			{
				ASTTreeNode v = new ASTTreeNode("Variable", "", 9531);
				ASTTreeNode n = itb.treeBuilder(inv.getPredicateString(), mch); 
				if(n.tag == 111)
					n.tag = 9111;
				n.children.get(1).isType = true;
				v.addNewChild(n);
				vars_dec.addNewChild(v);
				
			}
		}
		
		return vars_dec;
		
	}
	
	
	private boolean isTypingTree(ASTTreeNode node, ArrayList<String> vars, ArrayList<String> types) {
		Types t = new Types(); 
		
		if(node.tag != 107 && node.tag != 111) //????
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
}
