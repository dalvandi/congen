package com.dalvandi.congen.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class ClassGenerator {

	private String classname;
	private ArrayList<String> variables;
	private ArrayList<String> types;
	private ArrayList<String> constructoroperators;
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
			constructoroperators = cons;
			machine = mch;
	}
	
	/*
	 * This method prints and build the generated class. 
	 * 
	 */

	public void outputGeneratedClass() throws RodinDBException
	{
		String class_decl = getClassDeclartion();
		ArrayList<String> variables_decl = new VariablesDeclaration().getVariablesDeclaration(machine, variables, types);
		ArrayList<String> invariants = new Invariants().getInvariants(machine, variables, types);
		ArrayList<String> methods = new MethodGenerator().getMethods(machine, variables, constructoroperators);
		
		
		System.out.println(class_decl + "{");
		buildDafnyFile(classname, class_decl + "{");
		for(String s : variables_decl)
		{
			System.out.println("ghost var " + s + ";");
			buildDafnyFile(classname, "ghost var " + s + ";");

		}
		
		System.out.println("predicate Valid()");
		buildDafnyFile(classname, "predicate Valid()");
		System.out.println("reads this;");
		buildDafnyFile(classname, "reads this;");
		System.out.println("{");
		buildDafnyFile(classname, "{");
		Iterator<String> itr = invariants.iterator();

		while(itr.hasNext())
		{
		      String element = itr.next();
		      System.out.print(element);
		      buildDafnyFile(classname, element);

		      if(itr.hasNext()){
		    	  System.out.print(" && ");
			      buildDafnyFile(classname, " && ");
		      }
		}
		
		System.out.print("\n");
	      buildDafnyFile(classname, "");

		System.out.println("}");
	      buildDafnyFile(classname, "}");

		System.out.print("\n");
	      buildDafnyFile(classname, "");

		for(String s : methods)
		{
			System.out.println(s + "");
		      buildDafnyFile(classname, s + "");

		}
		
		System.out.println("}");
	      buildDafnyFile(classname, "}");

		

		
	}

	/*
	 * This method build a Dafny file containing the generated class 
	 * 
	 */

	private void buildDafnyFile(String mtdname, String st)
	{
		BufferedWriter bw = null;
		try {
		bw = new BufferedWriter(new FileWriter(mtdname + ".dfy", true));
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

	/*
	 * This method build a tree for all generic types (carrier sets taken from contexts)
	 * and then translate it to text and returns declaration of the class with regards 
	 * to those generic types. 
	 */
	private String getClassDeclartion() {

		if(!types.isEmpty())
		{
			ASTTreeNode typetree = new ASTTreeNode("coma", ",", 9996);
			for(String s : types)
			{
				typetree.addNewChild(new ASTTreeNode("FreeIdentifier", s, 1));
			}
			ASTTranslator t = new ASTTranslator();
			
			return "class " + classname +"<"+ t.translateASTTree(typetree)+">";
		}
		
		else
			return "class " + classname;
						
	}
	
	
	
}
