/*
 * Sadegh Dalvandi (www.dalvandi.com) - 13 November 2014
 * 
 * Class ASTBuilder
 * Base class for building abstract syntax tree from assignments, predicates and expressions.
 * 
 * method treeBuilder receives a string of an expression and the machine that the expression comes from
 * and returns the root node for the AST for that expression. 
 * 
 *
*/

package com.dalvandi.congen.core;

import java.util.ArrayList;

import org.eventb.core.IMachineRoot;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.UnaryExpression;

public class ASTBuilder {
	
	
	private Formula<?> inputFormula;

	protected ArrayList<String> vars;
	protected ArrayList<String> types;
	
	protected ASTBuilder(){}
	
	protected ASTBuilder(ArrayList<String> v, ArrayList<String> t)
	{
		vars = v;
		types = t;
	}
	
	
	ASTTreeNode treeBuilder(String str, IMachineRoot mch) 
	{
		ASTTreeNode root = null;
		FormulaFactory ff = mch.getFormulaFactory();
		IParseResult parseResult = ff.parseExpression(str, LanguageVersion.LATEST, null);
		if(parseResult == null || parseResult.hasProblem())
		{
			parseResult = ff.parsePredicate(str, LanguageVersion.LATEST, null);
			if(parseResult == null || parseResult.hasProblem())
			{
				parseResult = ff.parseAssignment(str, LanguageVersion.LATEST, null);
				inputFormula = parseResult.getParsedAssignment();
				root = buildAssignmentTree((Assignment) inputFormula);
			}
			else 
			{
				inputFormula = parseResult.getParsedPredicate();
				root = buildPredicateTree((Predicate) inputFormula);
			}
		}
		else
		{
			inputFormula = parseResult.getParsedExpression();
			System.out.println("Not implemented yet!");

		}
		return root;
		
		
		
	} 

	private ASTTreeNode buildPredicateTree(Predicate in) {
		ASTTreeNode predicateRoot = null;
		if(in instanceof RelationalPredicate)
		{
			predicateRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());
			Expression left = ((RelationalPredicate) in).getLeft();
			Expression right = ((RelationalPredicate) in).getRight();
			predicateRoot.addNewChild(buildExpressionTree(left));
			predicateRoot.addNewChild(buildExpressionTree(right));
		}
		
		return predicateRoot;
	}

	private ASTTreeNode buildAssignmentTree(Assignment in) {
		ASTTreeNode assignmentRoot = null;
		ASTTreeNode rhsroot = null;
		if(in instanceof BecomesEqualTo)
		{
			
			assignmentRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());

			FreeIdentifier[] assignedIdent = in.getAssignedIdentifiers();
			if(assignedIdent.length<=1){
				for(FreeIdentifier id : assignedIdent)
				{
					ASTTreeNode fid = new ASTTreeNode(id.getClass().getSimpleName(), id.toString(), id.getTag());
					
					assignmentRoot.addNewChild(fid);
				}
				
				Expression[] exp = ((BecomesEqualTo) in).getExpressions();

				for(Expression e: exp)
					rhsroot = buildExpressionTree(e);
				
				assignmentRoot.addNewChild(rhsroot);
				
			}
			else
			{
				System.out.println("ERROR: left hand side MUST have only one identifier!");
			}
			
			
		}
		else if(in instanceof BecomesMemberOf)
		{
			// TODO
		}
		
		else if(in instanceof BecomesSuchThat)
		{
			// TODO
		}
		
		
		return assignmentRoot;
		
	}

	private ASTTreeNode buildExpressionTree(Expression e) {

		if(e.getChildCount() == 0)
		{

			ASTTreeNode leaf = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			
			if(vars.contains(e.toString()))
				{
					leaf.isVariable = true;
				}
			else if(types.contains(e.toString()))
				{
					leaf.isType = true;
				}
			
			return leaf;
		}
		else if(e instanceof AssociativeExpression)
		{
			Expression[] exp_child = ((AssociativeExpression) e).getChildren();
			ASTTreeNode associ_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag()); 

			for(Expression e_ch : exp_child)
			{
				associ_exp.addNewChild(buildExpressionTree(e_ch));
									
			}
		
			return associ_exp;
			
		}
		else if(e instanceof BinaryExpression)
		{
			ASTTreeNode bi_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			
			bi_exp.addNewChild(buildExpressionTree(((BinaryExpression) e).getLeft()));
			bi_exp.addNewChild(buildExpressionTree(((BinaryExpression) e).getRight()));
	
			return bi_exp;

		}
		
		else if(e instanceof ExtendedExpression)
		{
			Expression[] exp_child = ((ExtendedExpression) e).getChildExpressions();
			
			ASTTreeNode extended_exp = new ASTTreeNode(e.getClass().getSimpleName(), ((ExtendedExpression) e).getExtension().getSyntaxSymbol(), e.getTag());
			
			extended_exp.isExtended = true;
			
			for(Expression e_ch : exp_child)
			{
				extended_exp.addNewChild(buildExpressionTree(e_ch));
									
			}
	
			return extended_exp;

		}

		else if(e instanceof UnaryExpression)
		{
			ASTTreeNode unary_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			unary_exp.addNewChild(buildExpressionTree(((UnaryExpression) e).getChild()));
			
			return unary_exp;

		}

		else if(e instanceof SetExtension)
		{
			Expression[] set_child = ((SetExtension) e).getMembers();
			ASTTreeNode setex_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());

			for(Expression s_ch : set_child)
			{
				setex_exp.addNewChild(buildExpressionTree(s_ch));
			}

			return setex_exp;

		}


		
		return null;
	}
	
	

}
