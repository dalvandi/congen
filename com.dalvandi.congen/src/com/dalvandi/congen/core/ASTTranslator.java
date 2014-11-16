package com.dalvandi.congen.core;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class ASTTranslator {

	protected class TranslationRules{
		int nodetype; // 0: Variable - 1: Literal - 3: MathOperator - 4: InfixOperator - 5: PrefixOperator
		int argnum; // Number of arguments
		boolean isType;
		String translation; // Dafny translation
	
		protected TranslationRules(int op, int arg, String tr, boolean type)
			{
				nodetype = op;
				argnum = arg;
				translation = tr;
				isType = type;
			
			}
			
		}
	public static final int IDENTIFIER = 0;
	public static final int LITERAL = 1;
	public static final int MATHOPERATOR = 2;
	public static final int INFIXOPERATOR = 3;
	public static final int PREFIXOPERATOR = 4;
	public static final int EXTENDEDOPERATOR = 4;
	public static final int BINARYEXPRESSION = 5;
	public static final int ASSOCIATIVEEXPRESSION = 6;
	public static final int TYPE = 7;
	protected HashMap<Integer, TranslationRules> translation_map; 
	protected HashMap<String, TranslationRules> translation_extended; 


	protected ASTTranslator(){
		
		translation_map = new HashMap<Integer, TranslationRules>();
		
		translation_map.put(1, new TranslationRules(IDENTIFIER,0,"",false));
		translation_map.put(4, new TranslationRules(LITERAL,0,"",false));
		translation_map.put(107, new TranslationRules(MATHOPERATOR,0,"type( : )#nontype( in )",false)); //∈
		translation_map.put(111, new TranslationRules(MATHOPERATOR,0,"type( set<arg1> )#nontype( <= )",false)); //<=
		translation_map.put(108, new TranslationRules(MATHOPERATOR,0," !in ",false)); //∈
		translation_map.put(101, new TranslationRules(MATHOPERATOR,0,"==",false)); // =
		translation_map.put(401, new TranslationRules(TYPE,0,"int",true)); // ℤ
		translation_map.put(6, new TranslationRules(MATHOPERATOR,0,":=",false)); // :=
		translation_map.put(306, new TranslationRules(MATHOPERATOR,0," + ",false)); // + 
		translation_map.put(307, new TranslationRules(MATHOPERATOR,0," * ",false)); // *
		translation_map.put(222, new TranslationRules(MATHOPERATOR,0," - ",false)); // -
		translation_map.put(223, new TranslationRules(MATHOPERATOR,0," / ",false)); // ÷
		translation_map.put(407, new TranslationRules(MATHOPERATOR,0," [] ",false)); // ∅
		translation_map.put(1001, new TranslationRules(EXTENDEDOPERATOR,1,"seq<arg1>",true)); //seq(arg1)
		translation_map.put(1002, new TranslationRules(EXTENDEDOPERATOR,1,"|arg1|",false)); //seqSize(arg1)
		translation_map.put(1009, new TranslationRules(EXTENDEDOPERATOR,2,"arg1[..arg2+1]",false)); //seqSliceToN(arg1, arg2)
		translation_map.put(1010, new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2..]",false)); // seqSliceFromN(arg1, arg2)
		translation_map.put(1008, new TranslationRules(EXTENDEDOPERATOR,2,"arg1 + arg2",false)); // arg1 seqConcat arg2
		translation_map.put(1006, new TranslationRules(EXTENDEDOPERATOR,2,"[arg2] + arg1",false)); //seqPrepend(arg1, arg2)
		translation_map.put(221, new TranslationRules(EXTENDEDOPERATOR,2,"(set j| arg1<=j && j<arg2)",false)); //arg1..arg2
		translation_map.put(226, new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2]",false)); //seq(arg1)
		translation_map.put(751, new TranslationRules(EXTENDEDOPERATOR,2,"|arg1|",false)); //card(arg)
		translation_map.put(757, new TranslationRules(EXTENDEDOPERATOR,2,"arg1",false)); //ran(arg)
		translation_map.put(305, new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2]",false)); //override op
		translation_map.put(5, new TranslationRules(EXTENDEDOPERATOR,2,"arg1",false)); //set expression
		translation_map.put(201, new TranslationRules(EXTENDEDOPERATOR,2,"arg1:= arg2",false)); //maplet
		translation_map.put(9996, new TranslationRules(MATHOPERATOR,0,",",false));
		translation_map.put(9997, new TranslationRules(EXTENDEDOPERATOR,2,"forall arg1 :: arg2",false));
		translation_map.put(9998, new TranslationRules(MATHOPERATOR,0," && ",false));
		translation_map.put(9999, new TranslationRules(MATHOPERATOR,0," ==> ",false));

		
		translation_extended = new HashMap<String, TranslationRules>();
		
		translation_extended.put("seq", new TranslationRules(EXTENDEDOPERATOR,1,"seq<arg1>",true));
		translation_extended.put("seqSize", new TranslationRules(EXTENDEDOPERATOR,1,"|arg1|",false));
		translation_extended.put("seqSliceToN", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[..arg2+1]",false));
		translation_extended.put("seqSliceFromN", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2..]",false));
		translation_extended.put("seqConcat", new TranslationRules(EXTENDEDOPERATOR,2,"arg1 + arg2",false));
		translation_extended.put("seqPrepend", new TranslationRules(EXTENDEDOPERATOR,2,"[arg2] + arg1",false));
	}
	
	
	String translateASTTree(ASTTreeNode root)
	{
			String translation = "";
			translation = translateNodeToDafny(root);
			return translation;
	}
	
	
	private String translateNodeToDafny(ASTTreeNode node) {

		TranslationRules rule = (TranslationRules) translation_map.get(node.tag);
		
		if(rule != null){
		if(rule.nodetype == IDENTIFIER || rule.nodetype == LITERAL)
			{
				return node.content;
			}
		else if(rule.nodetype == TYPE)
			return  translation_map.get(node.tag).translation;
		else if(rule.nodetype == MATHOPERATOR || rule.nodetype == EXTENDEDOPERATOR)
			return  translateOperator(node, rule);
		else
			return " ND:" + node.tag + " ";
		}
		else
			return " ND:" + node.tag + " ";





	}


	private String translateOperator(ASTTreeNode node, TranslationRules rule) {
		String translation = "";

		if(rule.nodetype == MATHOPERATOR)
		{
			if(node.children.isEmpty()) //??? not sure about !node.
			{
				//translation = translation + translation_map.get(node.tag).translation;
				return translation;
			}
			
		for(ASTTreeNode n: node.children){
			translation = translation + translateNodeToDafny(n);
			
			int i = 0;
			if(!node.children.get(node.children.size()-1).equals(n)) // if n is not the last node in children list
			{
				
				if(!translation_map.get(node.tag).translation.contains("type")) // if the translation rule have just one translation
					translation = translation + translation_map.get(node.tag).translation;
				else
				{
					if(translation_map.get(node.children.get(i+1).tag).isType || node.children.get(i+1).isType)
					{
						String tr = selectTranslation(translation_map.get(node.tag).translation, true);
						translation = translation + tr;

					}
					else
					{
						String tr = selectTranslation(translation_map.get(node.tag).translation, false);
						translation = translation + tr;
					}
				}
				
				i++;
			}
		}

		}
		else if(rule.nodetype == EXTENDEDOPERATOR)
		{	
			int i = 0;
			String[] args = new String[node.children.size()];
			for(ASTTreeNode n : node.children)
			{
				args[i] = translateNodeToDafny(n);
				i++;
			}
			translation = replaceArguments(args, rule.translation);
		}
			return translation;
	}


	protected String selectTranslation(String translation, boolean b) {
		
		String trans = translation;
		boolean isType = b; 
		String[] tanslations = trans.split("#");
		String type = "type\\(([\\s*|\\w*|\\W*|\\S*]*)\\)";
		String nontype = "nontype\\(([\\s*|\\w*|\\W*|\\S*]*)\\)";
		Matcher m;
		

			if(isType)
			{
			Pattern p =  Pattern.compile(type);
			 m = p.matcher(tanslations[0]);
			}
			else
			{
				Pattern p =  Pattern.compile(nontype);
				m = p.matcher(tanslations[1]);

			}



		if (m.find())
		{
		    return m.group(1);
		}
		
		else
			return " *ND* ";

		}	


	protected String replaceArguments(String[] args, String translation) {
		// TODO Auto-generated method stub
		for(int i = 0 ; i <args.length; i++)
		{
			String arg = "arg"+(i+1);
			translation = translation.replace(arg, args[i]);
		}
		
		return translation;
	}


}
