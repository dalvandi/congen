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

	//int tag is tag of invariants tree. If it is 9997, it means that there is no invariant to be translated 
	public ASTTreeNode getMethodsNode(IMachineRoot mch, ArrayList<String> vars,ArrayList<String> types, ArrayList<String> constat, int tag) throws RodinDBException {

		ASTTreeNode methods = new ASTTreeNode("Methods", "", 9522);
		for(String s : constat)
		{	
			
			String mtdname = getMethodName(s);
			ArrayList<String> metpar_in = getMethodInParameters(s);
			ArrayList<String> metpar_out = getMethodOutParameters(s);
			ArrayList<String> events = getMethodEvents(s);
			ASTTreeNode mtd = getMethodNode(mch, vars, types, mtdname, metpar_in, metpar_out, events, tag);
			methods.addNewChild(mtd);

		}
		
		
		return methods;
	}
	
	private ASTTreeNode getMethodNode(IMachineRoot mch, ArrayList<String> vars,
			ArrayList<String> types, String methodname, ArrayList<String> metpar, 
			ArrayList<String> metpar_out, ArrayList<String> events, int tag)
					throws RodinDBException 
	{
		
		ASTTreeNode mtd = new ASTTreeNode("Method", "", 9530);
		
		
		ASTTreeNode mtdname = new ASTTreeNode("Method Name", "", 9540);
		//Add method name child
		mtdname.addNewChild(new ASTTreeNode("Method Name", methodname, 1));
			mtd.addNewChild(mtdname);
	
		//Add method's input arguments
		ASTTreeNode mtdargs = new ASTTreeNode("Method Args In", "", 9541);
		mtdargs.addNewChild(getMethodArgsNode(mch, methodname, metpar,metpar_out, events, vars, types));
		mtd.addNewChild(mtdargs);

		//Add method's output arguments
		ASTTreeNode mtdargs_out = new ASTTreeNode("Method Args Out", "", 9541);
		mtdargs_out.addNewChild(getMethodArgsNodeOut(mch, methodname, metpar, metpar_out, events, vars, types));
		mtd.addNewChild(mtdargs_out);

		//Add method's contracts:
		ContractGenerator contract = new ContractGenerator(mch, vars, types, methodname, metpar, metpar_out, events, tag);
				
		//Add method's preconditions
		ASTTreeNode precondition = new ASTTreeNode("Method Precondition", "", 9542); //
		precondition.addNewChild(contract.getMethodPreconditionNode());
		if(!methodname.equals("Init")){
			mtd.addNewChild(precondition);
		}	
		else
		{
			mtd.addNewChild(new ASTTreeNode("Empty", "", 9997));
		}
		
		//Add method's postconditions
		ASTTreeNode postcondition = new ASTTreeNode("Method Postcondition", "", 9543);//
		postcondition.addNewChild(contract.getMethodPostconditionsNode());
		mtd.addNewChild(postcondition);

						
		return mtd;
	}
	
	
	
	private ASTTreeNode getMethodArgsNode(IMachineRoot mch,
			String methodname, ArrayList<String> metpar, ArrayList<String> metpar_out, ArrayList<String> events,ArrayList<String> vars,ArrayList<String> types) throws RodinDBException {
		
		ASTTreeNode methodtypes;
		
		if(metpar.size() != 0)
		{
		AssertionTreeBuilder astTree = new AssertionTreeBuilder(vars, types);
		String evt = events.get(0);
		methodtypes = astTree.methodTypingTreeBuilder(mch, metpar,metpar_out, evt);
		}
		else
			methodtypes = new ASTTreeNode("EMPTY", "" , 9997);
		
		return methodtypes;
	}
	
	private ASTTreeNode getMethodArgsNodeOut(IMachineRoot mch,
			String methodname, ArrayList<String> metpar, ArrayList<String> metpar_out, ArrayList<String> events,ArrayList<String> vars,ArrayList<String> types) throws RodinDBException {

		ASTTreeNode methodtypes;
		
		if(metpar_out.size() != 0)
		{
		AssertionTreeBuilder astTree = new AssertionTreeBuilder(vars, types);
		String evt = events.get(0);
		methodtypes = astTree.methodTypingTreeBuilder(mch, metpar_out,metpar, evt);
		}
		else
			methodtypes = new ASTTreeNode("EMPTY", "" , 9997);
		
		return methodtypes;
		
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

	private ArrayList<String> getMethodInParameters(String s) {

		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);
		
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
	
	
	private ArrayList<String> getMethodOutParameters(String s) {
		
		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(i_ret,s.length());
		
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
		//method $name($a,$b) returns ($c,$d) = {$ev1, $ev2}
		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);

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

	
	@Deprecated
	public ArrayList<String> getMethods(IMachineRoot mch, ArrayList<String> vars,ArrayList<String> types, ArrayList<String> constat, int tag) throws RodinDBException
	{
		
		ArrayList<String> methods = new ArrayList<String>();
		
		for(String s : constat)
		{	
		
			String methodname = getMethodName(s);
			ArrayList<String> metpar_in = getMethodInParameters(s);
			ArrayList<String> metpar_out = getMethodOutParameters(s);
			ArrayList<String> events = getMethodEvents(s);
			ArrayList<String> method = getMethod(mch, vars, types, methodname, metpar_in, metpar_out, events, tag);
			methods.addAll(methods.size(), method);

		}
		
		
		return methods;
		
	}

	@Deprecated
	private ArrayList<String> getMethod(IMachineRoot mch, ArrayList<String> vars, ArrayList<String> types, String methodname, ArrayList<String> metpar,ArrayList<String> metpar_out, ArrayList<String> events, int tag) throws RodinDBException {
		
		String method_decl = getMethodDeclaration(mch,methodname, metpar,metpar_out, events,vars, types);
		ContractGenerator methodpostcondition = new ContractGenerator(mch, vars, types, methodname, metpar, metpar_out, events, tag);
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
	@Deprecated
	private String getMethodDeclaration(IMachineRoot mch,
			String methodname, ArrayList<String> metpar, ArrayList<String> metpar_out, ArrayList<String> events,ArrayList<String> vars,ArrayList<String> types) throws RodinDBException {

		AssertionTreeBuilder astTree = new AssertionTreeBuilder(vars, types);
		String evt = events.get(0);
		ASTTreeNode methodtypes = astTree.methodTypingTreeBuilder(mch, metpar,metpar_out, evt);
		ASTTranslator translation = new ASTTranslator();
		String method_decl = "method "+ methodname + "("+translation.translateASTTree(methodtypes) +")";
		
		return method_decl;
	}


}
