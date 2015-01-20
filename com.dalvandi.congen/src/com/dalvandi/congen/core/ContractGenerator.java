package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.rodinp.core.RodinDBException;

public class ContractGenerator {
	
	private static IMachineRoot machine;
	private static boolean valid;
	private static ArrayList<IGuard> commonguards;
	private static ArrayList<String> commonguards_str;
	private static ArrayList<String> parameters;
	private static ArrayList<String> parameters_out;
	private static ArrayList<String> events;
	private static ArrayList<String> variables;
	private static ArrayList<String> types;
	
		//ContractGenerator(machine, list of variables, types, method name, list of input parameters, list of output parameters, list of events, integer tag)
		public ContractGenerator(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> t, 
				String method, ArrayList<String> par, ArrayList<String> par_out, ArrayList<String> evts, int tag) throws RodinDBException
		{
			machine = mch;
			parameters = par;
			parameters_out = par_out;
			events = evts;
			variables = vars;
			types = t;
			commonguards = getCommonGuards();
			if(tag == 9997) // If invariant node is empty
			{
				valid = false;
			}
			else
				valid = true;
			commonguards_str = new ArrayList<String>();
			for(IGuard g: commonguards)
			{
				commonguards_str.add(g.getPredicateString());
			}

		}
		

		
		
		private ArrayList<IGuard> getCommonGuards() throws RodinDBException {
			ArrayList<IGuard> commonguardslist = new ArrayList<IGuard>();
			
			if(events.size()>=1)
			{
				String evt = events.get(0);
				for(IEvent e : machine.getEvents())
				{
					if(evt.equals(e.getLabel()))
					{
						for(IGuard g : e.getGuards())
						{
							if(isCommonGuard(g))
							{
								commonguardslist.add(g);
							}
						}
						
					}
				}

			}
					
			return commonguardslist;
		}




		private boolean isCommonGuard(IGuard g) throws RodinDBException {
			int cg = 0;
			for(IEvent e : machine.getEvents())
				{
					if(events.contains(e.getLabel()))
					{
						IGuard[] grds =e.getGuards(); 
						if(guardExists(g, grds))
						{
							cg++;
						}
					}
				}
			if(cg == events.size())
				return true;
			else
				return false;
		}




		private boolean guardExists(IGuard g, IGuard[] grds) throws RodinDBException {
			for(IGuard grd: grds)
			{
				
				if(grd.getPredicateString().equals(g.getPredicateString()))
				{
					return true;
				}
			}
			return false;
		}



		//Returns method's preconditions
		public ASTTreeNode getMethodPreconditionNode() throws RodinDBException
		{
			
			ASTTreeNode n = new ASTTreeNode("Next Line", "", 9995);
			boolean isAnyPre = false;
			
			if(valid)
			{
				ASTTreeNode pre = new ASTTreeNode("Precondition", "", 9600);
				pre.addNewChild(new ASTTreeNode("Valid", "Valid()", 1));
				n.addNewChild(pre);
				isAnyPre = true;
			}
			
			if(events.size() == 1)
			{
				String evt = events.get(0);
				for(IEvent e: machine.getEvents())
				{
					if(evt.equals(e.getLabel()))
					{
						for(IGuard g : e.getGuards())
						{
							if(!isTyping(g) && !isOutput(g) && !hasInternalPar(g))
							{
								ASTBuilder tree = new ASTBuilder(variables, types);
								ASTTreeNode pre = new ASTTreeNode("Precondition", "", 9600);
								pre.addNewChild(tree.treeBuilder(g.getPredicateString(), machine));
								n.addNewChild(pre);
								isAnyPre = true;

							}
													
						}
					}
				}
			}		
			else if(events.size() > 1)
			{
				for(IGuard g : commonguards)
				{
					ASTBuilder tree = new ASTBuilder(variables, types);
					if(!isTyping(g) && !isOutput(g))
					{
						ASTTreeNode pre = new ASTTreeNode("Precondition", "", 9600);
						pre.addNewChild(tree.treeBuilder(g.getPredicateString(), machine));
						n.addNewChild(pre);
						isAnyPre = true;
					}
				}
			}
			
			if(isAnyPre)
			{
				return n;
			}
			else
				 return new ASTTreeNode("Empty Node", "", 9997);
			
		}
		
		

		private boolean hasInternalPar(IGuard g) throws RodinDBException {

			FormulaFactory ff = machine.getFormulaFactory();
			IParseResult parseResult = ff.parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
			Predicate p = parseResult.getParsedPredicate();
				for(FreeIdentifier id : ((Predicate) p).getFreeIdentifiers())
				{
					if(!parameters.contains(id.toString()) && !parameters_out.contains(id.toString()) && !variables.contains(id.toString()) && !types.contains(id.toString()))
					{
						return true;
					}
				}

			return false;
		}



		
		public ASTTreeNode getPostconditions() throws RodinDBException
		{
			ASTTreeNode or = new ASTTreeNode("OR", "", 352);
			ASTTreeNode post = new ASTTreeNode("Post", "", 9601);
			if(events.size() == 1)
			{
				ASTTreeNode and = getEventPostcondition(events.get(0));
				post.addNewChild(and);
		}
			else if(events.size() > 1)
			{
				for(String s : events)
				{
					ASTTreeNode paran = new ASTTreeNode("Paran", "", 9991);
					paran.addNewChild(getEventPostcondition(s));
					or.addNewChild(paran);
				}
				
				post.addNewChild(or);
			}
			
			return post;
			
		}

		private ASTTreeNode getEventPostcondition(String evt) throws RodinDBException {
			
			ASTTreeNode and = new ASTTreeNode("AND", "", 351);
			ArrayList<String> unchanged_vars = getUnchangedVariables(evt);
			boolean isExistential = true;
			
			for(IEvent e : machine.getEvents())
			{
				if(evt.contentEquals(e.getLabel()))
				{
				
					for(IGuard grd: e.getGuards())
					{
						if(hasInternalPar(grd))
						{
							ASTTreeNode existential = getExistentialQuantifier(e);
							markOldVariables(existential); /// old

							if(isExistential)
							{
								and.addNewChild(existential);
								isExistential = false;
							}

						}
					}
					
					for(IGuard grd: e.getGuards())
					{
						if(!isOutput(grd) && !isTyping(grd) && !commonguards_str.contains(grd.getPredicateString()) && !hasInternalPar(grd))
						{
							ASTBuilder tree = new ASTBuilder(variables, types);
							ASTTreeNode grdtree = tree.treeBuilder(grd.getPredicateString(), machine);
							markOldVariables(grdtree); /// old
							and.addNewChild(grdtree);
						}
					}
					
					for(IAction act : e.getActions())
					{
						ASTTreeNode banode = getBeforeAfterPredicateTree(act);
						and.addNewChild(banode);
					}
					
					for(String s : unchanged_vars)
					{
						ASTTreeNode equal = new ASTTreeNode("Equal", "", 101);
						equal.addNewChild(new ASTTreeNode("Free Identifier", s, 1));
						ASTTreeNode oldid = new ASTTreeNode("Free Identifier", s, 1);
						oldid.isOld = true;
						equal.addNewChild(oldid);
						and.addNewChild(equal);
					}
					
					for(IGuard grd: e.getGuards())
					{
						if(isOutput(grd) && !isTyping(grd))
						{
							ASTBuilder tree = new ASTBuilder(variables, types);
							ASTTreeNode grdtree = tree.treeBuilder(grd.getPredicateString(), machine);
							markOldVariables(grdtree); /// old
							and.addNewChild(grdtree);
						}
					}

				}
			}
			return and;
		}




		private ArrayList<String> getUnchangedVariables(String evt) throws RodinDBException {

			ArrayList<String> unchanged_vars = new ArrayList<String>();
			
			for(IEvent e : machine.getEvents())
			{
				if(evt.contentEquals(e.getLabel()))
				{
					for(String var : variables)
					{
						boolean isChanged = false;
						for(IAction act : e.getActions())
						{
							ASTBuilder tree = new ASTBuilder(variables, types);
							ASTTreeNode assignmentnode = tree.treeBuilder(act.getAssignmentString(), machine);
							if(var.equals(assignmentnode.children.get(0).getContent()))
							{
								isChanged = true;
							}
						}
						if(!isChanged)
						{
							unchanged_vars.add(var);
						}
					}
				}
			}
			
			return unchanged_vars;
		}




		private ASTTreeNode getExistentialQuantifier(IEvent e) throws RodinDBException {
			ArrayList<String> internals = getInternalParameters(e);
			ASTTreeNode exists = new ASTTreeNode("Exists", "", 852);
			ASTTreeNode boundIds = new ASTTreeNode("Comma", "", 9996);
			ASTTreeNode and = new ASTTreeNode("AND", "", 351);
			for(String s : internals)
			{
				boundIds.addNewChild(new ASTTreeNode("Free Identifier", s, 1));
			}
			exists.addNewChild(boundIds);
			for(IGuard g : e.getGuards())
			{
				if(hasInternalPar(g) && !isTyping(g))
				{
					ASTBuilder tree = new ASTBuilder(variables, types);
					ASTTreeNode grdtree = tree.treeBuilder(g.getPredicateString(), machine);
					and.addNewChild(grdtree);
				}
			}
			exists.addNewChild(and);
			return exists;
		}




		private ArrayList<String> getInternalParameters(IEvent e) throws RodinDBException {
			// TODO Auto-generated method stub
			ArrayList<String> internals = new ArrayList<String>();
			for(IParameter p :e.getParameters())
			{
				if(!parameters.contains(p.getIdentifierString()) && !parameters_out.contains(p.getIdentifierString()))
				{
					internals.add(p.getIdentifierString());
				}
			}
			return internals;
		}




		private ASTTreeNode getBeforeAfterPredicateTree(IAction act) throws RodinDBException {
			ASTBuilder tree = new ASTBuilder(variables, types);
			ASTTreeNode assignmentnode = tree.treeBuilder(act.getAssignmentString(), machine);
			markOldVariables(assignmentnode.children.get(1));
			if(assignmentnode.tag == 6)
				assignmentnode.tag = 101;
			
			return assignmentnode;
		}




	private void markOldVariables(ASTTreeNode node) {
		
		if(variables.contains(node.getContent()))
		{
			node.isOld = true;
		}
	
		for(ASTTreeNode n : node.children)
		{
			markOldVariables(n);
		}
			
		}




	private boolean isTyping(IGuard g) throws RodinDBException {
			
			ASTBuilder tree = new ASTBuilder(variables, types);
			ASTTreeNode node = tree.treeBuilder(g.getPredicateString(), machine);

			Types t = new Types(); 

			if(node.tag != 107  && node.tag != 111)
				return false;
			else 
			{
				if(types.contains(node.children.get(1).getContent()) || t.builtin_types.contains(node.children.get(1).tag) ||  t.extended_types.contains(node.children.get(1).getContent()))
						{
							return true;
						}
										
			}
			return false;
				
		}
		
		private boolean isOutput(IGuard g) throws RodinDBException {
			
			FormulaFactory ff = machine.getFormulaFactory();
			IParseResult parseResult = ff.parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
			for(FreeIdentifier i :parseResult.getParsedPredicate().getFreeIdentifiers())
			{
	
				if(parameters_out.contains(i.toString()) )
				{
					return true;
				}
			}
			
				return false;
		}



	
		@Deprecated 
		private ArrayList<String> postConditionGenerator()
		{
			ArrayList<String> postconditions = new ArrayList<String>(); 
			AssertionTreeBuilder pctree = new AssertionTreeBuilder(variables, types);			//Build AST tree for 
			ASTTreeNode pcroot = null;
			AssertionTranslator translation = new AssertionTranslator();
			try {
				for(IEvent evt: machine.getEvents())
				{
					for(String e : events)
						{
						if(e.contentEquals(evt.getLabel()))
						{
							pcroot = pctree.postConditionTreeBuilder(machine, parameters,parameters_out,evt);
							postconditions.add("ensures " + translation.translateASTTree(pcroot) + ";");

						}
						}
					
				}
			} catch (RodinDBException e) {
				e.printStackTrace();
			}
			return postconditions;
			

		}

		@Deprecated 
		public ArrayList<String> getMethodPostconditions() throws RodinDBException
		{

			ArrayList<String> postconditions = postConditionGenerator();
			return postconditions;
			
		}
		
		@Deprecated 
		public ASTTreeNode getMethodPostconditionsNode() {

			ASTTreeNode nl = new ASTTreeNode("Next Line", "", 9995);
			AssertionTreeBuilder pctree = new AssertionTreeBuilder(variables, types);			//Build AST tree for 
			try {
				for(IEvent evt: machine.getEvents())
				{
					for(String e : events)
						{
							if(e.contentEquals(evt.getLabel()))
								{
									//pcroot = pctree.postConditionTreeBuilder(machine, parameters,evt);
									ASTTreeNode post = new ASTTreeNode("Post", "", 9601);
									post.addNewChild(pctree.postConditionTreeBuilder(machine, parameters,parameters_out,evt));
									nl.addNewChild(post);
								}
						}
					
				}
			}
			catch (RodinDBException e)
				{
					e.printStackTrace();
				}
			
			return nl;
						
		}

}
