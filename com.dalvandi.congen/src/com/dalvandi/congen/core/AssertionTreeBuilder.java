package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

public class AssertionTreeBuilder extends ASTBuilder {

	private ArrayList<ASTTreeNode> typingnodes;
	private ArrayList<ASTTreeNode> quantifiernodes;
	private ArrayList<String> quantifierstr;
	protected AssertionTreeBuilder()
	{
		typingnodes = new ArrayList<ASTTreeNode>();
		quantifiernodes = new ArrayList<ASTTreeNode>();
		quantifierstr = new ArrayList<String>();
	}
	
	ASTTreeNode postConditionTreeBuilder(IMachineRoot mch, ArrayList<String> mtdpar, IEvent evt) throws RodinDBException
	{
		ASTTreeNode pcroot;
		
		if(evt.getGuards().length != 0)
		{
			pcroot = new ASTTreeNode("Implication", "==>",9999);
			ASTTreeNode grds = new ASTTreeNode("AND", "&&", 9998);
			ASTTreeNode acts = new ASTTreeNode("AND", "&&", 9998);
			for(IGuard grd: evt.getGuards())
			{
				grds.addNewChild(this.treeBuilder(grd.getPredicateString(), mch, mch.getFormulaFactory()));
			}
			for(IAction act: evt.getActions())
			{
				acts.addNewChild(this.treeBuilder(act.getAssignmentString(), mch, mch.getFormulaFactory()));
			}
			
			pcroot.addNewChild(grds);
			pcroot.addNewChild(acts);
			
		}
		else
		{
			pcroot = new ASTTreeNode("AND", "&&",9998);
			for(IAction act: evt.getActions())
			{
				pcroot.addNewChild(this.treeBuilder(act.getAssignmentString(), mch, mch.getFormulaFactory()));
			}

		}
		
		pcroot = postConditionTreeToTranslatableTree(mch, mtdpar, evt, pcroot);
		return pcroot;
		
	}
	
	
	private ASTTreeNode postConditionTreeToTranslatableTree(IMachineRoot mch,
			ArrayList<String> mtdpar, IEvent evt, ASTTreeNode pcroot) throws RodinDBException {
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<String> parameters = new ArrayList<String>();
		
		for(IVariable vari : mch.getVariables()) {
			variables.add(vari.getIdentifierString());
		}
		
		IParameter[] pars = evt.getParameters();
		
		for(IParameter par : pars)
		{
			parameters.add(par.getIdentifierString());
		}
		
		postConditionTreeAddMetaData(pcroot, variables, parameters);
		postConditionTreeTypingNodeRemover(pcroot,mtdpar);
		postConditionTreeFindNonMethodParameters(pcroot, mtdpar, parameters);
		pcroot = postConditionTreeAddQuantifyOverParameters(pcroot, mtdpar, parameters);
		
		return pcroot;
	}

	private ASTTreeNode postConditionTreeAddQuantifyOverParameters(ASTTreeNode pcroot,
			ArrayList<String> mtdpar, ArrayList<String> parameters) {
			
		if(!quantifiernodes.isEmpty()){
			ASTTreeNode quantifier = new ASTTreeNode("Quantifier", "forall", 9997);
			ASTTreeNode coma = new ASTTreeNode("Quantifier", ",", 9996);
		
		for(ASTTreeNode n : quantifiernodes)
		{
			ASTTreeNode q = (ASTTreeNode) n.clone();
			coma.addNewChild(q);
		}
		
		quantifier.addNewChild(coma);
		quantifier.addNewChild(pcroot);
		
		return quantifier;
		}
		else
			return pcroot;
		
	}

	private void postConditionTreeFindNonMethodParameters(ASTTreeNode node,
			ArrayList<String> mtdpar, ArrayList<String> parameters) {
		//
		
		
		ArrayList<String> mtd_par = mtdpar;
		
		if(node.isParameter && !mtd_par.contains(node.content) && !quantifierstr.contains(node.content))
		{
			quantifiernodes.add(node);
			quantifierstr.add(node.content);
		}
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeFindNonMethodParameters(n, mtdpar, parameters);
		}
		
	}
	
	//TO DO: change the method name to typingNodeRemover
	private void postConditionTreeTypingNodeRemover(ASTTreeNode pcroot, ArrayList<String> para) {

		postConditionTreeTypingNodeFinder(pcroot, null, para);
		ArrayList<ASTTreeNode> temp = new ArrayList<ASTTreeNode>(pcroot.children);
		
		for(ASTTreeNode n : pcroot.children)
		{
			if(typingnodes.contains(n))
			{
				temp.remove(n);
			}
		}
		
		pcroot.children = temp;
		
		for(ASTTreeNode n : pcroot.children)
		{
			postConditionTreeTypingNodeRemover(n, para);
		}
		
	}

	//TO DO: rename the method to typingNodeFinder
	private void postConditionTreeTypingNodeFinder(ASTTreeNode node, ASTTreeNode parent, ArrayList<String> para) { 

		if(parent != null)
		{

		if(node.tag == 107)
		{

			if((node.children.get(node.children.size()-1).isType && para.contains(node.children.get(0).content)))
			{
				typingnodes.add(node);
			}
		}
		
		}
		
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeTypingNodeFinder(n , node, para);
		}
	}
	
	
	
	


	private void postConditionTreeAddMetaData(ASTTreeNode node,
			ArrayList<String> variables, ArrayList<String> parameters) {

		if(node.tag == 1)
		{
			if(variables.contains(node.content))
			{
				node.isVariable = true;
			}
			else if(parameters.contains(node.content))
			{
				node.isParameter = true;
			}
			else
				node.isType = true; // This should be updated.
		}
		
		if(node.tag == 401 || node.tag == 1001) // what about seq?
			node.isType = true;

		if(node.tag == 6){ // right handside of assignements should be old
			for(int i = 1; i < node.children.size(); i++)
			{
				markVariablesOld(node.children.get(i), variables);
			}
			node.tag = 101; // this is not nice! the translator should do this when translating a tree to assertions
		}
		
		if(node.tag == 107) // in or : dont know!
		{
			node.isType = true; //??????????????????????????????? I DONT KNOW
		}

		if(node.tag == 9999) // left handside of implication
		{
			markVariablesOld(node.children.get(0), variables);
		}
				
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeAddMetaData(n, variables, parameters);
		}
		
		
	}

	private void markVariablesOld(ASTTreeNode node, ArrayList<String> variables) {
	
		if(variables.contains(node.content))
			{
				node.isOld = true;
			}
		
		for(ASTTreeNode n : node.children)
		{
			markVariablesOld(n, variables);
		}
	}

	public ASTTreeNode methodTypingTreeBuilder(IMachineRoot machine,
		ArrayList<String> parameters, String evt) throws RodinDBException {
		ASTTreeNode guardtree = new ASTTreeNode("coma", "," , 9996);
		IEvent event = null;
		for(IEvent e : machine.getEvents())
		{
			if(e.getLabel().equals(evt))
			{
				event = e;
			}
		}
		for(IGuard g : event.getGuards())
		{
			guardtree.addNewChild(this.treeBuilder(g.getPredicateString(), machine, machine.getFormulaFactory()));
		}
		
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<String> params = new ArrayList<String>();
		
		for(IVariable vari : machine.getVariables()) {
			variables.add(vari.getIdentifierString());
		}
		
		IParameter[] pars = event.getParameters();
		
		for(IParameter par : pars)
		{
			if(!parameters.contains(par.getIdentifierString()))
			{
				params.add(par.getIdentifierString());
				//System.out.println(par.getIdentifierString());
			}
		}
		
		postConditionTreeAddMetaData(guardtree, variables, params);
		ASTTreeNode typetree = removeNonTypingNodes(guardtree, parameters);
		
		return typetree;

	}

	private ASTTreeNode removeNonTypingNodes(ASTTreeNode node, ArrayList<String> para) {


		postConditionTreeTypingNodeFinder(node, null, para);
		ArrayList<ASTTreeNode> temp = new ArrayList<ASTTreeNode>(node.children);
		
		for(ASTTreeNode n : node.children)
		{
			if(!typingnodes.contains(n) && !n.children.isEmpty())
			{
				temp.remove(n);
			}
		}
		
		node.children = temp;
		
		for(ASTTreeNode n : node.children)
		{
			removeNonTypingNodes(n, para);
		}
		

		return node;
	}



}
