package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.rodinp.core.RodinDBException;

public class ContractGenerator {
	
	private static IMachineRoot machine;
	private static boolean valid;
	private static ArrayList<IGuard> commonguards;
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
			commonguards = findCommonGuards();
			if(tag == 9997) // If invariant node is empty
			{
				valid = false;
			}
			else
				valid = true;
		}
		

		
		
		private ArrayList<IGuard> findCommonGuards() throws RodinDBException {
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
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			for(IGuard grd: grds)
			{
				
				//if(grd.equals(g))
				if(grd.getPredicateString().equals(g.getPredicateString()))
				{
					return true;
				}
			}
			return false;
		}




		public ASTTreeNode getMethodPreconditionNode() throws RodinDBException
		{
			
			ASTTreeNode n;
			ASTTreeNode pre = new ASTTreeNode("Precondition", "", 9600);
			boolean isAnyPre = false;
			
			if(valid)
			{
				pre.addNewChild(new ASTTreeNode("Valid", "Valid()", 1));
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
								pre.addNewChild(tree.treeBuilder(g.getPredicateString(), machine));
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
						pre.addNewChild(tree.treeBuilder(g.getPredicateString(), machine));
						isAnyPre = true;
					}
				}
			}
			
			if(isAnyPre)
			{
				n = new ASTTreeNode("Next Line", "", 9995);
				n.addNewChild(pre);
			}
			else
				 n = new ASTTreeNode("Empty Node", "", 9997);
			
			return n;
		}
		
		

		private boolean hasInternalPar(IGuard g) throws RodinDBException {
			// TODO Auto-generated method stub
			FormulaFactory ff = machine.getFormulaFactory();
			IParseResult parseResult = ff.parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
			Predicate p = parseResult.getParsedPredicate();
			if(p instanceof RelationalPredicate)
			{
				for(FreeIdentifier id : ((Predicate) p).getFreeIdentifiers())
				{
					if(!parameters.contains(id.toString()) && !parameters_out.contains(id.toString()) && !variables.contains(id.toString()))
					{
						return true;
					}
				}
			}

			return false;
		}




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
}
