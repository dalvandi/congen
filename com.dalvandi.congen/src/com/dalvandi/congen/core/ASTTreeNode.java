package com.dalvandi.congen.core;

import java.util.ArrayList;

public class ASTTreeNode implements Cloneable{
	ArrayList<ASTTreeNode> children;
	String content;
	String type;
	int tag;
	boolean isType;			// Is this node contains a type?
	boolean isVariable;		// Is this a variable?
	boolean isParameter;	// Is this an event's type?
	boolean isTyping;		// Is this node is for typing?
	boolean isOperator;		// Is this an operator?
	boolean isOld;			// Is this an old operator?
	
	
	ASTTreeNode(String ty, String c, int t)
	{
		children = new ArrayList<ASTTreeNode>();
		type = ty;
		content = c;
		tag = t;
		
		isType = false;
		isVariable = false;
		isParameter = false;
		isTyping = false;
		isOperator = false;
		isOld = false;
	}
	
	
	ASTTreeNode(String ty, String c, int t, boolean Type, boolean Variable, boolean Parameter, 
			boolean Typing, boolean Operator, boolean Old)
	{
		children = new ArrayList<ASTTreeNode>();
		type = ty;
		content = c;
		tag = t;
		
		isType = Type;
		isVariable = Variable;
		isParameter = Parameter;
		isTyping = Typing;
		isOperator = Operator;
		isOld = Old;
	}

	protected Object clone()
             {
	    try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
             }
            
	
	void addNewChild(ASTTreeNode child)
	{
		children.add(child);
	}

}
