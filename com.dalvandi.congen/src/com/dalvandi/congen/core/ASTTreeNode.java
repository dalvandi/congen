package com.dalvandi.congen.core;

import java.util.ArrayList;

public class ASTTreeNode implements Cloneable{
	public ArrayList<ASTTreeNode> children;
	private String content;
	String type;
	int tag;
	boolean isType;			// Is this node contains a type?
	boolean isVariable;		// Is this a variable?
	boolean isParameter;	// Is this an event's type?
	boolean isTyping;		// Is this node for typing?
	boolean isExtended;		// Is this an operator?
	boolean isOperator;		// Is this an operator?
	boolean isOld;			// Is this an old operator?
	
	
	ASTTreeNode(String ty, String c, int t)
	{
		children = new ArrayList<ASTTreeNode>();
		type = ty;
		setContent(c);
		tag = t;
		
		isType = false;
		isVariable = false;
		isParameter = false;
		isTyping = false;
		isExtended = false;
		isOperator = false;
		isOld = false;
	}
	
	
	ASTTreeNode(String ty, String c, int t, boolean Type, boolean Variable, boolean Parameter, 
			boolean Typing, boolean Extended, boolean Operator, boolean Old)
	{
		children = new ArrayList<ASTTreeNode>();
		type = ty;
		setContent(c);
		tag = t;
		
		isType = Type;
		isVariable = Variable;
		isParameter = Parameter;
		isTyping = Typing;
		isExtended = Extended;
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


	public String getContent() {
		return this.content;
	}


	private void setContent(String content) {
		this.content = content;
	}

}
