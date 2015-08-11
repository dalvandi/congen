package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IVariable;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.rodinp.core.RodinDBException;

public class AssertionTreeBuilder extends ASTBuilder {
	
	//TODO This class is almost useless. Look at ContractGenerator class for contranct generation.
	//Many things are duplicated.
	
	private ArrayList<ASTTreeNode> typingnodes;
	private ArrayList<ASTTreeNode> quantifiernodes;
	private ArrayList<String> quantifierstr;
	
	protected AssertionTreeBuilder(ArrayList<String> v, ArrayList<String> t)
	{
		typingnodes = new ArrayList<ASTTreeNode>();
		quantifiernodes = new ArrayList<ASTTreeNode>();
		quantifierstr = new ArrayList<String>();
		vars = v;
		types = t;
	}
	
	/*
	 * postConditionTreeBuilder
	 * 
	 * This method builds a post-condition tree driven from event evt.  
	 * */
	ASTTreeNode postConditionTreeBuilder(IMachineRoot mch, ArrayList<String> mtdpar, ArrayList<String> mtdpar_out, IEvent evt) throws RodinDBException
	{
		ASTTreeNode pcroot;
		
		if(evt.getGuards().length != 0)
		{
			pcroot = new ASTTreeNode("Implication", "==>",251);
			ASTTreeNode grds = new ASTTreeNode("AND", "&&", 351);
			ASTTreeNode acts = new ASTTreeNode("AND", "&&", 351);
			
			for(IGuard grd: evt.getGuards())
			{
				if(guardContainsOutParam(grd,mtdpar_out,mch))
					grds.addNewChild(this.treeBuilder(grd.getPredicateString(), mch));
				else
					acts.addNewChild(this.treeBuilder(grd.getPredicateString(), mch));

			}
		
			for(IAction act: evt.getActions())
			{
				acts.addNewChild(this.treeBuilder(act.getAssignmentString(), mch));
			}
			
			pcroot.addNewChild(grds);
			pcroot.addNewChild(acts);
			
		}
		else
		{
			pcroot = new ASTTreeNode("AND", "&&",351);
			for(IAction act: evt.getActions())
			{
				pcroot.addNewChild(this.treeBuilder(act.getAssignmentString(), mch));
			}

		}
		
		pcroot = postConditionTreeToTranslatableTree(mch, mtdpar, mtdpar_out, evt, pcroot);
		return pcroot;
		
	}
	
	
	private boolean guardContainsOutParam(IGuard grd, ArrayList<String> mtdpar_out, IMachineRoot mch) throws RodinDBException {

		FormulaFactory ff = mch.getFormulaFactory();
		IParseResult parseResult = ff.parsePredicate(grd.getPredicateString(), LanguageVersion.LATEST, null);
		Predicate pr = parseResult.getParsedPredicate();
				
		for(FreeIdentifier e :pr.getFreeIdentifiers())
		{
			if(mtdpar_out.contains(e.toString()))
			{
				return false;
			}
		}

		
		return true;
	}

	private ASTTreeNode postConditionTreeToTranslatableTree(IMachineRoot mch,
			ArrayList<String> mtdpar, ArrayList<String> mtdpar_out, IEvent evt, ASTTreeNode pcroot) 
					throws RodinDBException {
		
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
		postConditionTreeTypingNodeRemover(pcroot,mtdpar,mtdpar_out);
		postConditionTreeFindNonMethodParameters(pcroot, mtdpar,mtdpar_out, parameters);
		pcroot = postConditionTreeAddQuantifyOverParameters(pcroot, mtdpar, parameters);
		
		return pcroot;
	}

	private ASTTreeNode postConditionTreeAddQuantifyOverParameters(ASTTreeNode pcroot,
			ArrayList<String> mtdpar, ArrayList<String> parameters) {
			
		if(!quantifiernodes.isEmpty()){
			ASTTreeNode quantifier = new ASTTreeNode("Quantifier", "forall", 851); /// changed the old one
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
			ArrayList<String> mtdpar, ArrayList<String> mtdpar_out, ArrayList<String> parameters) {
		//		
		
		ArrayList<String> mtd_par = mtdpar;
		
		if(node.isParameter && !mtd_par.contains(node.getContent()) && !mtdpar_out.contains(node.getContent()) &&!quantifierstr.contains(node.getContent()))
		{
			quantifiernodes.add(node);
			quantifierstr.add(node.getContent());
		}
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeFindNonMethodParameters(n, mtdpar, mtdpar_out, parameters);
		}
		
	}
	
	//TO DO: change the method name to typingNodeRemover
	private void postConditionTreeTypingNodeRemover(ASTTreeNode pcroot, ArrayList<String> para, ArrayList<String> para_out) {

		postConditionTreeTypingNodeFinder(pcroot, null, para, para_out);
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
			postConditionTreeTypingNodeRemover(n, para, para_out);
		}
		
	}

	//TO DO: rename the method to typingNodeFinder
	private void postConditionTreeTypingNodeFinder(ASTTreeNode node, ASTTreeNode parent, ArrayList<String> para, ArrayList<String> para_out) { 

		if(parent != null)
		{

		if(node.tag == 107)
		{

			if((node.children.get(node.children.size()-1).isType && (para.contains(node.children.get(0).getContent())) || para_out.contains(node.children.get(0).getContent())))
			{
				typingnodes.add(node);
			}
		}
		
		}
		
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeTypingNodeFinder(n , node, para, para_out);
		}
	}
	
	private void postConditionTreeAddMetaData(ASTTreeNode node,
			ArrayList<String> variables, ArrayList<String> parameters) {

		if(node.tag == 1)
		{
			if(variables.contains(node.getContent()))
			{
				node.isVariable = true;
			}
			else if(parameters.contains(node.getContent()))
			{
				node.isParameter = true;
			}
			else
				node.isType = true; // This should be updated.
		}
		Types ty = new Types();
		//if(node.tag == 401 || node.tag == 1001) // what about seq?
		if(ty.builtin_types.contains(node.tag) || ty.extended_types.contains(node.tag))
			node.isType = true;

		if(node.tag == 6 || node.tag == 101){ // right handside of assignements should be old
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

		if(node.tag == 251) // left handside of implication
		{
			markVariablesOld(node.children.get(0), variables);
		}
				
		for(ASTTreeNode n : node.children)
		{
			postConditionTreeAddMetaData(n, variables, parameters);
		}
		
		
	}

	private void markVariablesOld(ASTTreeNode node, ArrayList<String> variables) {
	
		if(variables.contains(node.getContent()))
			{
				node.isOld = true;
			}
		
		for(ASTTreeNode n : node.children)
		{
			markVariablesOld(n, variables);
		}
	}

	public ASTTreeNode getTypingGuards(IMachineRoot mch, String event, ArrayList<String> par) throws RodinDBException
	{
		ASTTreeNode typingtree = new ASTTreeNode("coma", "," , 9996);
		IEvent evt = null;
		for(IEvent e : mch.getEvents())
		{
			if(e.getLabel().equals(event))
			{
				evt = e;
			}
		}

		for(IGuard g : evt.getGuards())
		{
			ASTTreeNode n = this.treeBuilder(g.getPredicateString(), mch);
			if(isTypingGuard(n))
			{
				if(guardsContainPar(mch, g,par))
				{
					typingtree.addNewChild(n);
				}
			}
		}
		
		
		return typingtree;
	}
	
	private boolean guardsContainPar(IMachineRoot mch, IGuard g, ArrayList<String> par) throws RodinDBException {
		
		if(g.hasPredicateString())
		{
			FormulaFactory ff = mch.getFormulaFactory();
			IParseResult parseResult = ff.parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
			Predicate p = parseResult.getParsedPredicate();
			for(FreeIdentifier id : p.getFreeIdentifiers())
			{
				if(par.contains(id.toString()))
					return true;
			}
		}
			
		return false;
	}

	private boolean isTypingGuard(ASTTreeNode node) {
		
		Types type = new Types();
		
		if(node.tag == 107)
		{
			if(node.children.size() == 2)
			{
				int rhs = node.children.get(1).tag;
				String rhss = node.children.get(1).getContent();
				if(type.builtin_types.contains(rhs) || type.extended_types.contains(rhs) || types.contains(rhss))
				{
					node.children.get(1).isType = true;
					return true;
				}
			}
		}
		
		return false;
	}

	private ASTTreeNode removeNonTypingNodes(ASTTreeNode node, ArrayList<String> para, ArrayList<String> parameters_out) {


		postConditionTreeTypingNodeFinder(node, null, para,parameters_out);
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
			removeNonTypingNodes(n, para,parameters_out);
		}
		

		return node;
	}

	@Deprecated
	public ASTTreeNode methodTypingTreeBuilder(IMachineRoot machine,
		ArrayList<String> parameters, ArrayList<String> parameters_out, String evt) throws RodinDBException {
		//TODO: this method is terrible. it should be change. it generates tree for every typing guard regardless to the rule of parameter
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
			//System.out.println(g.getElementType().getName());
			guardtree.addNewChild(this.treeBuilder(g.getPredicateString(), machine));
		}
		
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<String> params = new ArrayList<String>();
		
		for(IVariable vari : machine.getVariables()) {
			variables.add(vari.getIdentifierString());
		}
		
		IParameter[] pars = event.getParameters();
		
		for(IParameter par : pars)
		{
			{
				params.add(par.getIdentifierString());
				//System.out.println(par.getIdentifierString());
			}
		}
		
		postConditionTreeAddMetaData(guardtree, variables, params);
		ASTTreeNode typetree = removeNonTypingNodes(guardtree, parameters, parameters_out);
		
		return typetree;
	
	}



}
