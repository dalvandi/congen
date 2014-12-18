package com.dalvandi.congen.core;


public class AssertionTranslator extends ASTTranslator {

	
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
				if(node.isOld)
					return "old("+node.getContent()+")";
				else
					return node.getContent();
			}
			else if(rule.nodetype == TYPE)
			{
				return selectTrRule(node).translation;
			}
			
			else if(rule.nodetype == MATHOPERATOR)
			{
				return  translateAssertionOpToDafny(node, rule);
			}
		
			else if(rule.nodetype == EXTENDEDOPERATOR)
			{
				return  translateAssertionExtToDafny(node, rule);
			}
		
		}
		else
		{
			return "ND: [" + node.tag + "]";
		}
		return "";
	}



	private String translateAssertionOpToDafny(ASTTreeNode node, TranslationRules rule)
	{
		String translation = "";

			if(node.children.isEmpty())
			{
				translation = translation + selectTrRule(node).translation;
				return translation;
			}
			else{	
			for(ASTTreeNode n: node.children){
				translation = translation + toDafny(n);
				
				int i = 0;
				if(!node.children.get(node.children.size()-1).equals(n))
				{
					
					if(!selectTrRule(node).translation.contains("type"))
						translation = translation + selectTrRule(node).translation;
					else
					{
						//System.out.println("Tag: " + node.tag + " Content: " + node.content + " Children Size: " + selectTrRule(node.children.get(i+1)).translation);
						if(selectTrRule(node.children.get(i+1)).isType || node.children.get(i+1).isType)
						{
							String tr = selectTranslation(selectTrRule(node).translation, true);
							translation = translation + tr;
	
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
			}
			return translation;
	}
	
	
	
	private String translateAssertionExtToDafny(ASTTreeNode node, TranslationRules rule) {
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

}

