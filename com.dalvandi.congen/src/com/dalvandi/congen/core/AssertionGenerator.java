package com.dalvandi.congen.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

/* This calss is gonna be used for generating PRiME assertions*/
public class AssertionGenerator {
	private String classname;
	private ArrayList<String> variables;
	private ArrayList<String> types;
	private ArrayList<String> constructorstatement;
	private IMachineRoot machine;

	/*
	 * This constructor receives a machine, list of its constructor statements,
	 * list of its variables, and list of all types in the context. 
	 * 
	 */
	
	public AssertionGenerator(IMachineRoot mch,
			ArrayList<String> cons,
			ArrayList<String> vars, ArrayList<String> ty) {
		
			classname = mch.getComponentName();
			variables = vars;
			types = ty;
			constructorstatement = cons;
			machine = mch;
	}
	
	
	public ASTTreeNode generateAssertions() throws RodinDBException
	{
		ArrayList<ASTTreeNode> asserts = new ArrayList<ASTTreeNode>();
		
		for(String s : constructorstatement)
		{	
			ArrayList<String> events = getMethodEvents(s);
			for(String e : events){
				ASTTreeNode n = getAssertionNode(e);
				asserts.add(n);
			}
		}
		return null;
	}
	
	private ASTTreeNode getAssertionNode(String evt) throws RodinDBException {
		ArrayList<IGuard> grds = new ArrayList<IGuard>();
	
		for(IEvent e : machine.getEvents())
		{
			if(evt.equals(e.getLabel()))
			{
				for(IGuard g : e.getGuards())
				{
					grds.add(g);		
				}
				
			}
		}
		
		return null;
	}


	private ArrayList<String> getMethodEvents(String s) {
		s = s.replaceAll("\\s","");
		ArrayList<String> evts = new ArrayList<String>();
		String type = "\\{([\\s*|\\w*|\\W*|\\S*]+)\\}";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
			evts = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return evts;
		}
		return evts;
	}


	public void outputGeneratedAssert() {
		// TODO Auto-generated method stub
		
	}
}
