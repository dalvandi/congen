package com.dalvandi.congen.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;


public class ClassGenerator {

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
	
	public ClassGenerator(IMachineRoot mch,
			ArrayList<String> cons,
			ArrayList<String> vars, ArrayList<String> ty) {
		
			classname = mch.getComponentName();
			variables = vars;
			types = ty;
			constructorstatement = cons;
			machine = mch;
	}
	
	/*
	 * This method prints and build the generated class. 
	 * 
	 */

	public void outputGeneratedClass() throws RodinDBException
	{
		
		
		//*******Build Tree for Class*********
		ASTTreeNode class_node = getClassNode();
		class_node.addNewChild(getClassBodyNode());
		 			
		AssertionTranslator tr = new AssertionTranslator();
		String translation = tr.translateASTTree(class_node);
		buildDafnyFile(machine.getElementName(), translation);
		System.out.println("FILE GENERATED...");
		System.out.println(translation);
			
	}

	/*
	 * This method build a Dafny file containing the generated class 
	 * 
	 */
	@SuppressWarnings("unused")
	private void buildDafnyFile(String mtdname, String st)
	{
		String path = machine.getRodinProject().getResource().getLocation().toString();
		String now = new Date().toString();
		now = now.replaceAll(":", "-");
		boolean dir = new File(path+ "/DafnyContract").mkdirs();
		BufferedWriter bw = null;
		try {
		bw = new BufferedWriter(new FileWriter(path+"/DafnyContract/"+mtdname + "-"+ now  + ".dfy", true));
		bw.write(st);
		bw.newLine();
		bw.flush();
		} catch (IOException ioe) {
		ioe.printStackTrace();
		} finally {
		if (bw != null) try {
		bw.close();
		}
		catch (IOException ioe2){}
		}
		

	}

	private ASTTreeNode getClassNode() {

		ASTTreeNode class_node = new ASTTreeNode("Class", "Class", 9500);
		
		ASTTreeNode class_name = new ASTTreeNode("Class Name", classname, 9510);
		class_name.addNewChild(new ASTTreeNode("Class Name", classname, 1));
	
		class_node.addNewChild(class_name);

		if(!types.isEmpty())
		{
			ASTTreeNode class_generic = new ASTTreeNode("Class Name", classname, 9511);
			ASTTreeNode genericstree = new ASTTreeNode("coma", ",", 9996);
		
			for(String s : types)
			{
				genericstree.addNewChild(new ASTTreeNode("FreeIdentifier", s, 1));
			}
			
			class_generic.addNewChild(genericstree);
			class_node.addNewChild(class_generic);
			
			return class_node;
		}
		
		else
			return class_node;
						
	}


	private ASTTreeNode getClassBodyNode() throws RodinDBException {

		ASTTreeNode body = new ASTTreeNode("Class Body", "", 9512);
		ASTTreeNode vars = new VariablesDeclaration().getVariablesNode(machine, variables, types);
		ASTTreeNode invs = new Invariants().getInvariantsNode(machine, variables, types);
		ASTTreeNode mtd = new MethodGenerator().getMethodsNode(machine, variables, types, constructorstatement, invs.tag);
		body.addNewChild(vars);
		body.addNewChild(invs);
		body.addNewChild(mtd);
		
		return body;
	}
	
	
}
