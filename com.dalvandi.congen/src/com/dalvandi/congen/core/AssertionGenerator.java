package com.dalvandi.congen.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinElement;
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
	
	public ASTTreeNode generateNode() throws RodinDBException
	{
		ASTTreeNode node = new ASTTreeNode("Assert", "Assert", 10000);
		ASTBuilder tree = new ASTBuilder(variables, types);
		node.addNewChild(tree.treeBuilder("app_sockets", machine));
		node.addNewChild(tree.treeBuilder("pid_t", machine));
		for(ASTTreeNode n : node.children)
		{
			System.out.println(n.type);
		}
		
		ASTTranslator tr = new ASTTranslator();
		System.out.println(tr.translateASTTree(node));
		

		
		
		
		return node;
	}
	
	public void generateAssertions() throws RodinDBException
	{
		ASTTreeNode class_node = new ASTTreeNode("Class", "Class", 9500);

		ArrayList<ASTTreeNode> asserts = new ArrayList<ASTTreeNode>();
		
		for(String s : constructorstatement)
		{	
			ArrayList<String> events = getMethodEvents(s);
			for(String e : events){
				ASTTreeNode n = getAssertionNode(e);
				asserts.add(n);
			}
		}
		
		ASTTranslator tr = new ASTTranslator();
		for(ASTTreeNode n : asserts)
			System.out.println(tr.translateASTTree(n));

	}
	
	private ASTTreeNode getAssertionNode(String evt) throws RodinDBException {
		ArrayList<IGuard> grds = new ArrayList<IGuard>();
		ASTTreeNode node = null;

		for(IEvent e : machine.getEvents())
		{
			if(evt.equals(e.getLabel()))
			{
				for(IGuard g : e.getGuards())
				{
					//System.out.println(g.getPredicateString());
					ASTTreeWalker w = new ASTTreeWalker();
					ASTBuilder tree = new ASTBuilder(variables, types);
					node = tree.treeBuilder(g.getPredicateString(), machine);
					//w.treePrinter(node);
					if(node.tag == 108)
						node.tag = 10108;
					else if(node.tag == 107)
						node.tag = 10107;
					
					node.addNewChild(tree.treeBuilder(evt, machine));
				}
				
			}
		}
		
		return node;
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
