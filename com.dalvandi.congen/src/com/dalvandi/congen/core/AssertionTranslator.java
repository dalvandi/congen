package com.dalvandi.congen.core;



public class AssertionTranslator extends ASTTranslator {

	
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
			if(node.isOld)
				return "old("+node.content+")";
			else
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
			if(node.children.isEmpty())
			{
				translation = translation + translation_map.get(node.tag).translation;
				return translation;
			}
			else{	
			for(ASTTreeNode n: node.children){
				translation = translation + translateNodeToDafny(n);
				
				int i = 0;
				if(!node.children.get(node.children.size()-1).equals(n))
				{
					
					if(!translation_map.get(node.tag).translation.contains("type"))
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




	
}
