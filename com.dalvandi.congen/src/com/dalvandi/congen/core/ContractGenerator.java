package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class ContractGenerator {
	
	private static IMachineRoot machine;
	//private static String method_name;
	private static ArrayList<String> parameters;
	private static ArrayList<String> parameters_out;
	private static ArrayList<String> events;
	private static ArrayList<String> variables;
	private static ArrayList<String> types;
	
		public ContractGenerator(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> t, 
				String method, ArrayList<String> par, ArrayList<String> par_out, ArrayList<String> evts)
		{
			machine = mch;
			//method_name = method;
			parameters = par;
			parameters_out = par_out;
			events = evts;
			variables = vars;
			types = t;
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
