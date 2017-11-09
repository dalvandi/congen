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

		public TranslationRules() {
			// TODO Auto-generated constructor stub
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
	public static final int ARG = 8;
	protected HashMap<Integer, TranslationRules> translation_map; 
	protected HashMap<Integer, TranslationRules> translation_nmap; 
	protected HashMap<String, TranslationRules> translation_extended; 


	protected ASTTranslator(){
		
		/*translation_map = new HashMap<Integer, TranslationRules>();
		
		translation_map.put(1, new TranslationRules(IDENTIFIER,0,"",false));
		translation_map.put(4, new TranslationRules(LITERAL,0,"",false));
		translation_map.put(107, new TranslationRules(MATHOPERATOR,0,"type( : )#nontype( in )",false)); //∈
		translation_map.put(111, new TranslationRules(MATHOPERATOR,0,"<=",false)); //<=
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
		translation_map.put(9997, new TranslationRules(EXTENDEDOPERATOR,2,"(forall arg1 :: arg2)",false));
		translation_map.put(9998, new TranslationRules(MATHOPERATOR,0," && ",false));
		translation_map.put(9999, new TranslationRules(MATHOPERATOR,0," ==> ",false));

		*/
		translation_extended = new HashMap<String, TranslationRules>();
		
		translation_extended.put("seq", new TranslationRules(EXTENDEDOPERATOR,1,"seq<arg1>",true)); //type
		translation_extended.put("seqSize", new TranslationRules(EXTENDEDOPERATOR,1,"|arg1|",false));
		translation_extended.put("seqSliceToN", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[..arg2]",false));
		translation_extended.put("seqSliceFromN", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2..]",false));
		translation_extended.put("seqConcat", new TranslationRules(EXTENDEDOPERATOR,2,"arg1 + arg2",false));
		translation_extended.put("seqPrepend", new TranslationRules(EXTENDEDOPERATOR,2,"[arg2] + arg1",false));
		translation_extended.put("seqAppend", new TranslationRules(EXTENDEDOPERATOR,2,"arg1 + [arg2]",false));
		translation_extended.put("seqElemUpdate", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2:=arg3]",false));
		translation_extended.put("seqElemAccess", new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2]",false));
		translation_extended.put("emptySeq", new TranslationRules(MATHOPERATOR,2,"[]",false));
		
				
		translation_nmap = new HashMap<Integer, TranslationRules>();
		
		translation_nmap.put(1, new TranslationRules(IDENTIFIER,0,"",false));
		translation_nmap.put(2, new TranslationRules(IDENTIFIER,0,"",false));
		translation_nmap.put(4, new TranslationRules(LITERAL,0,"",false));
		translation_nmap.put(5, new TranslationRules(EXTENDEDOPERATOR,0,"arg1",false)); //Set
		translation_nmap.put(6, new TranslationRules(MATHOPERATOR,0," := ",false)); // Assignment
		translation_nmap.put(401, new TranslationRules(MATHOPERATOR,0,"int",false)); // int
		translation_nmap.put(402, new TranslationRules(MATHOPERATOR,0,"nat",false)); // nat
		translation_nmap.put(404, new TranslationRules(MATHOPERATOR,0,"bool",false)); // bool
		translation_nmap.put(306, new TranslationRules(MATHOPERATOR,0," + ",false)); // Sum
		translation_nmap.put(307, new TranslationRules(MATHOPERATOR,0," * ",false)); // Product
		translation_nmap.put(222, new TranslationRules(MATHOPERATOR,0," - ",false)); // Minus
		translation_nmap.put(223, new TranslationRules(MATHOPERATOR,0," / ",false)); // Quotient
		translation_nmap.put(224, new TranslationRules(MATHOPERATOR,0," % ",false)); // Remainder
		translation_nmap.put(103, new TranslationRules(MATHOPERATOR,0," < ",false)); // Less Than
		translation_nmap.put(104, new TranslationRules(MATHOPERATOR,0," <= ",false)); // Less or Equal
		translation_nmap.put(105, new TranslationRules(MATHOPERATOR,0," > ",false)); // Greater
		translation_nmap.put(106, new TranslationRules(MATHOPERATOR,0," >= ",false)); // Greater or Equal
		translation_nmap.put(405, new TranslationRules(MATHOPERATOR,0," true ",false)); // True
		translation_nmap.put(406, new TranslationRules(MATHOPERATOR,0," false ",false)); // False
		translation_nmap.put(351, new TranslationRules(MATHOPERATOR,0," && ",false)); // Conjunction
		translation_nmap.put(352, new TranslationRules(MATHOPERATOR,0," || ",false)); // Disjunction
		translation_nmap.put(251, new TranslationRules(MATHOPERATOR,0," ==> ",false)); // Implication
		translation_nmap.put(252, new TranslationRules(MATHOPERATOR,0," <==> ",false)); // Equivalence
		translation_nmap.put(701, new TranslationRules(MATHOPERATOR,0," ! ",false)); // Negation
		translation_nmap.put(851, new TranslationRules(EXTENDEDOPERATOR,0,"(forall arg1 :: arg2)",false)); // For all		
		translation_nmap.put(852, new TranslationRules(EXTENDEDOPERATOR,0,"exists arg1 :: arg2",false)); // Exists	
		translation_nmap.put(101, new TranslationRules(MATHOPERATOR,0," == ",false)); // Equality
		translation_nmap.put(102, new TranslationRules(MATHOPERATOR,0," != ",false)); // Inequality
		translation_nmap.put(407, new TranslationRules(MATHOPERATOR,0," {} ",false)); // Empty set		// what about sequences???
		translation_nmap.put(803, new TranslationRules(MATHOPERATOR,0," ! ",false)); // Set comprehension
		translation_nmap.put(301, new TranslationRules(MATHOPERATOR,0," + ",false)); // Union
		translation_nmap.put(302, new TranslationRules(MATHOPERATOR,0," * ",false)); // Intersection
		translation_nmap.put(213, new TranslationRules(MATHOPERATOR,0," - ",false)); // Difference
		translation_nmap.put(302, new TranslationRules(MATHOPERATOR,0," * ",false)); // Intersection
		translation_nmap.put(107, new TranslationRules(MATHOPERATOR,0,"type( : )#nontype( in )",false)); // Membership // what about typing
		translation_nmap.put(108, new TranslationRules(MATHOPERATOR,0," !in ",false)); // Not Member
		translation_nmap.put(109, new TranslationRules(MATHOPERATOR,0," < ",false)); // proper subset // what about typing
		translation_nmap.put(110, new TranslationRules(EXTENDEDOPERATOR,0,"!(arg1 < arg2)",false)); // Not proper subset // what about typing
		translation_nmap.put(111, new TranslationRules(MATHOPERATOR,0," <=  ",false)); // Subset
		translation_nmap.put(9111, new TranslationRules(EXTENDEDOPERATOR,0,"arg1 : set<arg2>",false)); // Set typing
		translation_nmap.put(112, new TranslationRules(EXTENDEDOPERATOR,0,"!(arg1 <= arg2)",false)); // Not Subset // what about typing
		translation_nmap.put(751, new TranslationRules(EXTENDEDOPERATOR,0,"|arg1|",false)); // Set cardinality
		translation_nmap.put(221, new TranslationRules(EXTENDEDOPERATOR,0,"(set k0| arg1<=k0 && k0<=arg2)",false)); // Up to
		translation_nmap.put(201, new TranslationRules(EXTENDEDOPERATOR,0,"arg1:=arg2",false)); // Maps to
		translation_nmap.put(226, new TranslationRules(EXTENDEDOPERATOR,0,"arg1[arg2]",false)); // FunImage
		translation_nmap.put(757, new TranslationRules(EXTENDEDOPERATOR,2,"arg1",false)); //ran(arg) only if arg is a sequence
		translation_nmap.put(305, new TranslationRules(EXTENDEDOPERATOR,2,"arg1[arg2]",false)); //override op only if arg is a sequence
		//translation_nmap.put(801, new TranslationRules(EXTENDEDOPERATOR,2,"(set arg1 | arg2 :: arg3)",false)); //set comprehension {arg.arg2|arg3}

		translation_nmap.put(9990, new TranslationRules(MATHOPERATOR,0,"ND",false)); // Next line
		translation_nmap.put(9991, new TranslationRules(EXTENDEDOPERATOR,0,"(arg1)",false)); // parentheses
		translation_nmap.put(9992, new TranslationRules(MATHOPERATOR,0,"\b\b",false)); // Backspace
		translation_nmap.put(9995, new TranslationRules(MATHOPERATOR,0,"\n",false)); // Next line
		translation_nmap.put(9996, new TranslationRules(MATHOPERATOR,0,",",false)); // comma
		translation_nmap.put(9997, new TranslationRules(MATHOPERATOR,0,"",false)); // Empty Node
		

		
		translation_nmap.put(9500, new TranslationRules(EXTENDEDOPERATOR,0,"class arg1<arg2>\narg3\n",false)); // Class
		translation_nmap.put(9510, new TranslationRules(EXTENDEDOPERATOR,0,"arg1",false)); // Class Name
		translation_nmap.put(9511, new TranslationRules(MATHOPERATOR,0,"",false)); // Class Generic
		translation_nmap.put(9512, new TranslationRules(EXTENDEDOPERATOR,0,"{\n\narg1\narg2\narg3\n}",false)); // Class Body
		translation_nmap.put(9520, new TranslationRules(MATHOPERATOR,0,"",false)); // Class Variables
		translation_nmap.put(9531, new TranslationRules(EXTENDEDOPERATOR,0,"var arg1;\n",false)); // Ghost Variable
		translation_nmap.put(9521, new TranslationRules(EXTENDEDOPERATOR,0,"predicate Valid()\nreads this;\n{\narg1\n}",false)); // Class Invariants //TODO: Add a next line node
		translation_nmap.put(9522, new TranslationRules(MATHOPERATOR,0,"",false)); // Class Methods
		translation_nmap.put(9530, new TranslationRules(EXTENDEDOPERATOR,0,"\n\nmethod arg1(arg2) returns(arg3)\narg4\nmodifies this;\narg5",false)); // Method
		translation_nmap.put(9540, new TranslationRules(MATHOPERATOR,0,"",false)); // Method Name
		translation_nmap.put(9541, new TranslationRules(MATHOPERATOR,0,"",false)); // Method Arguments
		translation_nmap.put(9542, new TranslationRules(MATHOPERATOR,0,"",false)); // Method Preconditions
		translation_nmap.put(9543, new TranslationRules(EXTENDEDOPERATOR,0,"arg1",false)); // Method Postconditions
		translation_nmap.put(9544, new TranslationRules(MATHOPERATOR,0,"",false)); // Method Body
		translation_nmap.put(9545, new TranslationRules(EXTENDEDOPERATOR,0,"arg1",false)); // Invariant next line
		translation_nmap.put(9600, new TranslationRules(EXTENDEDOPERATOR,0,"requires arg1;",false)); // Pre-condition
		translation_nmap.put(9601, new TranslationRules(EXTENDEDOPERATOR,0,"ensures arg1;",false)); // Post-condition
		
		
		//PRiME ASSERGEN
		translation_nmap.put(11100, new TranslationRules(EXTENDEDOPERATOR,0,"arg1",false));
		translation_nmap.put(11101, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 1 \n if(std::find_if(arg3.begin(),arg3.end(), [=](std::pair<arg2, std::shared_ptr<prime::uds>> x) {return x.first == arg1;}) != arg3.end()) throw std::runtime_error(\"arg4\");",false));
		translation_nmap.put(11102, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 2 \n if(std::find_if(arg3.begin(),arg3.end(), [=](std::pair<arg2, std::shared_ptr<prime::uds>> x) {return x.first == arg1;}) == arg3.end())throw std::runtime_error(\"arg4\");",false));
		translation_nmap.put(11103, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 3 \n if(std::find_if(arg1.begin(),arg1.end(), [=](prime::api::app::arg2 x) {return x.arg3 == arg3;}) != arg1.end())throw std::runtime_error(\"arg4\");",false));
		translation_nmap.put(11104, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 4 \n if(std::find_if(arg5.begin(),arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;})!= arg5.end())throw std::runtime_error(\"arg6\");",false));
		translation_nmap.put(11105, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 5 \n if(arg1.arg3 > arg1.arg4) throw std::runtime_error(\"arg5\");",false));
		translation_nmap.put(11106, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 6 \n if(std::find_if(arg5.begin(),arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;})!= arg5.end()) throw std::runtime_error(\"arg6\");",false));
		translation_nmap.put(11107, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 7 \n if(arg1.arg8 >= (*std::find_if(arg5.begin(), arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;}).arg6) throw std::runtime_error(\"arg9\");",false));
		translation_nmap.put(11108, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 8 \n if(arg1.arg8 > (*std::find_if(arg5.begin(), arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;}).arg6) throw std::runtime_error(\"arg9\");",false));
		translation_nmap.put(11109, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 9 \n if(arg1.arg8 <= (*std::find_if(arg5.begin(), arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;}).arg6) throw std::runtime_error(\"arg9\");",false));
		translation_nmap.put(11110, new TranslationRules(EXTENDEDOPERATOR,0,"//rule 10 \n if(arg1.arg8 <= (*std::find_if(arg5.begin(), arg5.end(), [=](prime::api::app::arg2 x) {return x.arg4 == arg1.arg4 && x.arg3 == arg1.arg3;}).arg6) throw std::runtime_error(\"arg9\");",false));


		




	}
	
	
	String translateASTTree(ASTTreeNode root)
	{
			String translation = "";
			translation = toDafny(root);
			return translation;
	}
	

	
	private String toDafny(ASTTreeNode node)
	{
		TranslationRules rule = selectTrRule(node);
		
		
		if(rule != null)
		{
			if(rule.nodetype == IDENTIFIER || rule.nodetype == LITERAL)
			{
				return node.getContent();
			}
			else if(rule.nodetype == TYPE)
			{
				return selectTrRule(node).translation;
			}
			
			else if(rule.nodetype == MATHOPERATOR)
			{
				return  translateOpToDafny(node, rule);
			}
		
			else if(rule.nodetype == EXTENDEDOPERATOR)
			{
				return  translateExpToDafny(node, rule);
			}
		
		}
		else
		{
			return "ND: [" + node.tag + "]";
		}
		return "";
	}

	protected String translateExpToDafny(ASTTreeNode node, TranslationRules rule) 
	{
		String translation = "";
		int i = 0;
		String[] args = new String[node.children.size()];
		for(ASTTreeNode n : node.children)
		{
			args[i] = toDafny(n);
			i++;
		}
	
		translation = replaceArguments(args, rule.translation);
		return translation;
	}


	
	protected String translateOpToDafny(ASTTreeNode node, TranslationRules rule) {
		String translation = "";

		if(node.children.isEmpty()) //??? not sure about !node.
		{
			//translation = translation + translation_map.get(node.tag).translation;
			return translation;
		}
		else
		for(ASTTreeNode n: node.children)
		{
			translation = translation + toDafny(n);
			int i = 0;
			if(!node.children.get(node.children.size()-1).equals(n)) // if n is not the last node in children list
			{
				
				if(!selectTrRule(node).translation.contains("type")) // if the translation rule have just one translation
					translation = translation + selectTrRule(node).translation;
				else
				{
					
					if(selectTrRule(node.children.get(i+1)).isType || node.children.get(i+1).isType)
					{
						if(!selectTrRule(node).translation.contains("arg"))
							{String tr = selectTranslation(selectTrRule(node).translation, true);
							translation = translation + tr;
							}
						else
							{
							translation = translation + "ridi";
							}
					}
					else
					{
						String tr = selectTranslation(selectTrRule(node).translation, false);
						translation = translation + tr;
					}
				}
				
				i++;
			}
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
		for(int i = 0 ; i <args.length; i++)
		{
			String arg = "arg"+(i+1);
			translation = translation.replace(arg, args[i]);
		}
		
		return translation;
	}

	
	protected TranslationRules selectTrRule(ASTTreeNode node)
	{
		TranslationRules rule = new TranslationRules();
		
		if(node.tag == 99999)
		{
			rule = (TranslationRules) translation_extended.get(node.getContent());
		}
		else
		{
			rule = (TranslationRules) translation_nmap.get(node.tag);
			if(rule == null)
			{
				rule = translation_nmap.get(9990);
			}
		}
		
		return rule;
		
	}

}
