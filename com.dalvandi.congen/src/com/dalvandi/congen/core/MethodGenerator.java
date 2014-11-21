package com.dalvandi.congen.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

public class MethodGenerator {

	public ArrayList<String> getMethods(IMachineRoot mch, ArrayList<String> vars,ArrayList<String> types, ArrayList<String> constat) throws RodinDBException
	{
		
		ArrayList<String> methods = new ArrayList<String>();
		
		for(String s : constat)
		{	
		
			String methodname = getMethodName(s);
			ArrayList<String> metpar = getMethodParameters(s);
			ArrayList<String> events = getMethodEvents(s);
			ArrayList<String> method = getMethod(mch, vars, types, methodname, metpar, events);
			methods.addAll(methods.size(), method);

		}
		
		
		return methods;
		
	}

	private ArrayList<String> getMethod(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types, String methodname, ArrayList<String> metpar, ArrayList<String> events) throws RodinDBException {
		
		String method_decl = getMethodDeclaration(mch,methodname, metpar, events,vars, types);
		ContractGenerator methodpostcondition = new ContractGenerator(mch, vars, types, methodname, metpar, events);
		ArrayList<String> postconditions = methodpostcondition.getMethodPostconditions();
		ArrayList<String> method = new ArrayList<String>();
		ArrayList<String> precondition = new ArrayList<String>();
		precondition.add("requires Valid();");
		postconditions.add(0, "ensures Valid();");
		method.add(0, method_decl);
		
		if(!methodname.equals("Init")){
		method.addAll(method.size(), precondition);
		}
		
		method.addAll(method.size(), postconditions);
		
		return method;
	}

	private String getMethodDeclaration(IMachineRoot mch,
			String methodname, ArrayList<String> metpar, ArrayList<String> events,ArrayList<String> vars,ArrayList<String> types) throws RodinDBException {

		AssertionTreeBuilder astTree = new AssertionTreeBuilder(vars, types);
		String evt = events.get(0);
		ASTTreeNode methodtypes = astTree.methodTypingTreeBuilder(mch, metpar, evt);
		ASTTranslator translation = new ASTTranslator();
		String method_decl = "method "+ methodname + "("+translation.translateASTTree(methodtypes) +")";
		
		return method_decl;
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
		    //System.out.println(m.group(1));
			evts = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return evts;
		}
		return evts;
	}

	private ArrayList<String> getMethodParameters(String s) {

		s = s.replaceAll("\\s","");
		ArrayList<String> pars = new ArrayList<String>();
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    //System.out.println(m.group(1));
		    pars = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return pars;
		}
		return pars;
	}

	private String getMethodName(String s) {
		//method $name($a,$b) = {$ev1, $ev2}
		String type = "method ([\\s*|\\w*|\\W*|\\S*]+)\\(";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    return m.group(1);
		}
		
		else{
			Shell shell = null;
			MessageDialog dialog = new MessageDialog(shell, "Invalid Constructor Statement", null,
				   ""+ s + "\nis an invalid constructor statement. \nA constructor statement must have "
				    		+ "the following format:\n"
				    		+ "method $name($par1, $par2,...) = {$evt1, $evt2,...}", MessageDialog.ERROR, new String[] { "Ok",
	}, 0);
				dialog.open();
				return " *ND* ";
		}

	}

	public ASTTreeNode getMethodsNode(IMachineRoot mch, ArrayList<String> vars,ArrayList<String> types, ArrayList<String> constat) throws RodinDBException {

		ASTTreeNode methods = new ASTTreeNode("Methods", "", 9522);
		for(String s : constat)
		{	
			
			String mtdname = getMethodName(s);
			ArrayList<String> metpar = getMethodParameters(s);
			ArrayList<String> events = getMethodEvents(s);
			ASTTreeNode mtd = getMethodNode(mch, vars, types, mtdname, metpar, events);
			methods.addNewChild(mtd);

		}
		
		
		return methods;
	}
	
	private ASTTreeNode getMethodNode(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types, String methodname, ArrayList<String> metpar, ArrayList<String> events) throws RodinDBException 
	{
		
		ASTTreeNode mtd = new ASTTreeNode("Method", "", 9530);
		
		ASTTreeNode mtdname = new ASTTreeNode("Method Name", "", 9540);
		mtdname.addNewChild(new ASTTreeNode("Method Name", methodname, 1));
		
		ASTTreeNode mtdargs = new ASTTreeNode("Method Name", "", 9541);
		mtdargs.addNewChild(getMethodArgsNode(mch,methodname, metpar, events,vars, types));
		
		ASTTreeNode precondition = new ASTTreeNode("Method Precondition", "", 9542); //
		
		ContractGenerator methodpostcondition = new ContractGenerator(mch, vars, types, methodname, metpar, events);
		methodpostcondition.getMethodPostconditionsNode();
		ASTTreeNode postcondition = new ASTTreeNode("Method Precondition", "", 9543);
		postcondition.addNewChild(methodpostcondition.getMethodPostconditionsNode());

		mtd.addNewChild(mtdname);
		mtd.addNewChild(mtdargs);
		
		if(!methodname.equals("Init")){
			mtd.addNewChild(precondition);
		}	
		else
		{
			mtd.addNewChild(new ASTTreeNode("Empty", "", 9997));
		}
		
		mtd.addNewChild(postcondition);
		
		
		
		return mtd;
	}
	
	private ASTTreeNode getMethodArgsNode(IMachineRoot mch,
			String methodname, ArrayList<String> metpar, ArrayList<String> events,ArrayList<String> vars,ArrayList<String> types) throws RodinDBException {

		AssertionTreeBuilder astTree = new AssertionTreeBuilder(vars, types);
		String evt = events.get(0);
		ASTTreeNode methodtypes = astTree.methodTypingTreeBuilder(mch, metpar, evt);
		
		return methodtypes;
	}

}
