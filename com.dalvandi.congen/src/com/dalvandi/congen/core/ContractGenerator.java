package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

public class ContractGenerator {
	
	private static IMachineRoot machine;
	private static String method_name;
	private static ArrayList<String> parameters;
	private static ArrayList<String> events;
	private static ArrayList<String> mch_variables;
	
		public ContractGenerator(IMachineRoot mch, ArrayList<String> vars, String method, ArrayList<String> par, ArrayList<String> evts)
		{
			machine = mch;
			method_name = method;
			parameters = par;
			events = evts;
			mch_variables = vars;
		}
		
		
		private ArrayList<String> postConditionGenerator()
		{
			ArrayList<String> postconditions = new ArrayList<String>(); 
			AssertionTreeBuilder astTree = new AssertionTreeBuilder();
			ASTTreeNode pcroot = null;
			AssertionTranslator translation = new AssertionTranslator();
			try {
				for(IEvent evt: machine.getEvents())
				{
					for(String e : events)
						{
						if(e.contentEquals(evt.getLabel()))
						{
							pcroot = astTree.postConditionTreeBuilder(machine, parameters,evt);
							postconditions.add("ensures " + translation.translateASTTree(pcroot) + ";");

						}
						}
					
				}
			} catch (RodinDBException e) {
				e.printStackTrace();
			}
			return postconditions;
			

		}
		
		public ArrayList<String> getMethodPostconditions() throws RodinDBException
		{
			//AssertionTreeBuilder astTree = new AssertionTreeBuilder();
			//ASTTranslator translation = new ASTTranslator();
			//String evt = events.get(0);
			//ASTTreeNode methodtypes = astTree.methodTypingTreeBuilder(machine, parameters, evt);
			//postconditions.add(0, "method "+ method_name + "("+translation.translateASTTree(methodtypes) +")");

			ArrayList<String> postconditions = postConditionGenerator();
			return postconditions;
			
		}
	
}
