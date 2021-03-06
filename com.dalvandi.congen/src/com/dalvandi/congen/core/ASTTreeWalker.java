package com.dalvandi.congen.core;

import com.dalvandi.congen.core.ASTTreeNode;

//import java.util.Stack;

public class ASTTreeWalker {
//	private Stack<ASTTreeNode> nodestack;
	//private String treetext;
	//private String btreetext;
	private ASTTreeNode node;
	
	protected ASTTreeWalker()
	{
		//nodestack = new Stack<ASTTreeNode>();
		//treetext = "Tree Text: ";
		//btreetext = "Binary Tree Text: ";
		node = null;
	}
	
	int treeNodeCounter(ASTTreeNode root)
	{
		int thisnode = root.children.size();
		
		for (ASTTreeNode n: root.children)
		{
			thisnode += treeNodeCounter(n);
		}
		
		return thisnode;
		
	}
	
	void treePrinter(ASTTreeNode root)
	{

		if(root.children.size() == 0)
		{
			System.out.println("Node(Tag: "+root.tag+", Content: "+ root.getContent()+ ")	");
			if(node != null && !node.equals(root))
				System.out.println("Child of	Node(Tag: "+node.tag+", Content: "+ node.getContent()+ ")	");

		}
		else{
			System.out.println("Node(Tag: "+root.tag+", Content: "+ root.getContent()+ ")	");
			if(node != null && !node.equals(root))
				System.out.println("Child of	Node(Tag: "+node.tag+", Content: "+ node.getContent()+ ")	");

			for(ASTTreeNode n : root.children)
			{
				node = root;
				treePrinter(n);
			
			}
		}
	}

}
