package com.dalvandi.congen.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
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
	private HashMap<Integer, HashMap<String,ArrayList>> mapping;
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
			
			
			/*mapping.put(3, new HashMap<String, ArrayList>());
			mapping.get(3).put("knobs_disc", new ArrayList<String>());
			mapping.get(3).get("knobs_disc").add("knob_disc_t");
			mapping.get(3).put("knobs_cont", new ArrayList<String>());
			mapping.get(3).get("knobs_cont").add("knob_cont_t");
			mapping.get(3).put("mons_disc", new ArrayList<String>());
			mapping.get(3).get("mons_disc").add("mon_disc_t");
			mapping.get(3).put("mons_cont", new ArrayList<String>());
			mapping.get(3).get("mons_cont").add("mon_cont_t");*/
			
			
			
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
	
	public void generateAssertions2() throws RodinDBException
	{
		ASTTreeWalker w = new ASTTreeWalker();
		ASTBuilder tree = new ASTBuilder(variables, types);
		ASTTreeNode node = tree.treeBuilder("a ∈ b ∧ c ↦ d ∈ e ∧ f = g(c ↦ d) ∧ h ≤ f", machine);
		//System.out.println(createAsserTree(node));
		w.treePrinter(node);
	}
	
	public void generateAssertions() throws RodinDBException
	{
		ASTTreeNode class_node = new ASTTreeNode("Class", "Class", 9500);

		ArrayList<ASTTreeNode> asserts = new ArrayList<ASTTreeNode>();
		
		for(String s : constructorstatement)
		{	
			ArrayList<String> events = getMethodEvents(s);
			String methodName = getMethodName(s);
			String methodInput = getMethodInParameters(s);
			ASTTreeNode methodNode = new ASTTreeNode("function", "function", 11500);
			ASTTreeNode methodNameNode = new ASTTreeNode("methodNameNode", methodName, 1);
			ASTTreeNode methodInputNode = new ASTTreeNode("methodInputNode", methodInput, 1);
			methodNode.addNewChild(methodNameNode);
			methodNode.addNewChild(methodInputNode);
			for(String e : events){
				ASTTreeNode n = getAssertionNode(e);
				methodNode.addNewChild(n);
				if(n != null)
				asserts.add(methodNode);
			}
		}
		
		/*ASTTranslator tr = new ASTTranslator();
		for(ASTTreeNode n : asserts){ 
			String trans = tr.translateASTTree(n);
			trans = trans.replace("maxx", "max");
			trans = trans.replace("minn", "min");
			trans = trans.replace("idd", "id");
			System.out.println(trans);
		}*/
		programText(asserts);

	}
	
	
	private void programText(ArrayList<ASTTreeNode> asserts)
	{
		String code = "";
		
		code += "#include <iostream> \n";
		code += "#include \"uds.h\"\n";
		code += "#include \"util.h\"\n";
		code += "#include \"prime_api_rtm.h\"\n";
		code += "#include \"uds.h\"\n";
		code += "#include \"prime_api_t.h\"\n";
		code += "#include \"prime_api_app_t.h\"\n";
		code += "#include \"prime_api_dev_t.h\"\n\n\n";
		code += "namespace assertion{\n\n\n";

		
		ASTTranslator tr = new ASTTranslator();
		for(ASTTreeNode n : asserts){ 
			String trans = tr.translateASTTree(n);
			trans = trans.replace("maxx", "max");
			trans = trans.replace("minn", "min");
			trans = trans.replace("idd", "id");
			
			//this should be corrected
			trans = trans.replace("app_knobs_disc_t", "knob_disc_t");
			trans = trans.replace("app_knobs_cont_t", "knob_cont_t");
			trans = trans.replace("app_mons_cont_t", "mon_cont_t");
			trans = trans.replace("app_mons_disc_t", "mon_disc_t");

			code = code + trans + "\n\n\n\n";
		}
		
		code += "\n\n}\n";
		
		System.out.println(code);

		outputGeneratedAssert(code);
	}

	private void outputGeneratedAssert(String st)
	{
		String path = machine.getRodinProject().getResource().getLocation().toString();
		String now = new Date().toString();
		now = now.replaceAll(":", "-");
		boolean dir = new File(path+ "/AsserGen").mkdirs();
		BufferedWriter bw = null;
		try {
		bw = new BufferedWriter(new FileWriter(path+"/AsserGen/assergen.cpp", false));
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

	private ArrayList<String> getArgs(ASTTreeNode node)
	{
		ArrayList<String> args = new ArrayList<String>();
		for(ASTTreeNode n : node.children)
		{
			if(n.tag == 1)
			{
				if(!args.contains(n.getContent()))
					args.add(n.getContent());
			}
			else
			{
				ArrayList<String> t = getArgs(n);
				for(String st : t){
					if(!args.contains(st))
						args.add(st);
				}
			}
				
		}
		return args;
		
	}
	
	
	private ASTTreeNode createAsserTree(ASTTreeNode node, String evt)
	{
		ArrayList<ASTTreeNode> c = node.children;
		
		if(node.tag == 351)
		{
			if(c.size() == 2){
				if(c.get(0).tag == 107 && c.get(1).tag == 108)
				{
					
					if(c.get(1).children.get(0).tag == 201)
					{

						ASTTreeNode r4 = createRuleNode("r4", node, evt);
						return r4;
					}
					else{
						ASTTreeNode r1 = createRuleNode("r1", node, evt);
						return r1;

					}
				}
				else if(c.get(0).tag == 107 && c.get(1).tag == 107)
				{
					if(c.get(1).children.get(0).tag == 201)
					{
						ASTTreeNode r6 = createRuleNode("r6", node, evt);
						return r6;

					}
					else{
						ASTTreeNode r2 = createRuleNode("r2", node, evt);
						return r2;

					}
				}
				else if(c.get(0).tag == 111 && c.get(1).tag == 101)
				{
					ASTTreeNode r3 = createRuleNode("r3", node, evt);
					return r3;

				}
				else if(c.get(0).tag == 107 && c.get(1).tag == 103)
				{
					ASTTreeNode r5 = createRuleNode("r5", node, evt);
					return r5;
					}
			}
			else if(c.size() == 4)
			{
				if(c.get(0).tag == 107 && c.get(1).tag == 107 && c.get(2).tag == 101 && c.get(3).tag == 103)
				{
					ASTTreeNode r7 = createRuleNode("r7", node, evt);
					return r7;	
				
				}
				else if(c.get(0).tag == 107 && c.get(1).tag == 107 && c.get(2).tag == 101 && c.get(3).tag == 104)
				{
					//System.out.println("CHECK::: " + c.get(2).children.get(0).getContent() + " -- " + c.get(3).children.get(0).getContent());
					if(c.get(2).children.get(0).getContent().equals(c.get(3).children.get(1).getContent())){
						ASTTreeNode r8 = createRuleNode("r8", node, evt);
						return r8;
					}
					else if(c.get(2).children.get(0).getContent().equals(c.get(3).children.get(0).getContent()))
					{
						ASTTreeNode r11 = createRuleNode("r11", node, evt);
						return r11;	
					}
				}
				else if(c.get(0).tag == 107 && c.get(1).tag == 107 && c.get(2).tag == 101 && c.get(3).tag == 105)
				{
					ASTTreeNode r9 = createRuleNode("r9", node, evt);
					return r9;

				}
				else if(c.get(0).tag == 107 && c.get(1).tag == 107 && c.get(2).tag == 101 && c.get(3).tag == 106)
				{
					if(c.get(2).children.get(0).getContent().equals(c.get(3).children.get(1).getContent())){
						ASTTreeNode r10 = createRuleNode("r10", node, evt);
						return r10;
					}
					else if(c.get(2).children.get(0).getContent().equals(c.get(3).children.get(0).getContent()))
					{
						ASTTreeNode r12 = createRuleNode("r12", node, evt);
						return r12;	
					}


				}
			}

		}
		return null;
	}
	
	private ASTTreeNode createRuleNode(String r, ASTTreeNode node, String evt) {
		
		HashMap<String, Integer> h = new HashMap<String, Integer>();
		
		h.put("r1", 11101);
		h.put("r2", 11102);
		h.put("r3", 11103);
		h.put("r4", 11104);
		h.put("r5", 11105);
		h.put("r6", 11106);
		h.put("r7", 11107);
		h.put("r8", 11108);
		h.put("r9", 11109);
		h.put("r10", 11110);
		h.put("r11", 11111);
		h.put("r12", 11112);
		
		ASTTreeNode rn = new ASTTreeNode(r, r, h.get(r));
		for(String s : getArgs(node))
		{
			rn.addNewChild(new ASTTreeNode("id", s, 1));
		}		
		rn.addNewChild(new ASTTreeNode("id", evt, 1));
		return rn;
	}

	
	private ASTTreeNode getAssertionNode(String evt) throws RodinDBException {
		ArrayList<IGuard> grds = new ArrayList<IGuard>();
		ASTTreeNode node = null;
		ASTTreeNode allnodes = new ASTTreeNode("allnodes","allnodes",11100);
		allnodes.addNewChild(new ASTTreeNode("NL","NL",9995));
	
		for(IEvent e : machine.getEvents())
		{
			if(evt.equals(e.getLabel()))
			{
				for(IGuard g : e.getGuards())
				{
					String comment = "";
					ASTBuilder tree = new ASTBuilder(variables, types);
					node = tree.treeBuilder(g.getPredicateString(), machine);
					try{
						comment = g.getComment() + " (" + g.getLabel() + "@" + evt + ")";
					}
					catch(Exception e1)
					{
						comment = " (" + g.getLabel() + "@" + evt + ")";
					}
				
						
					ASTTreeNode assernode = createAsserTree(node, comment);
					allnodes.children.get(0).addNewChild(assernode);
				}
				
			}
		}
		
		return allnodes;
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

	
	private String getMethodInParameters(String s) {

		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);
		
		//s = s.replaceAll("\\s","");
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    return m.group(1);
		}
		return "";
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
}
