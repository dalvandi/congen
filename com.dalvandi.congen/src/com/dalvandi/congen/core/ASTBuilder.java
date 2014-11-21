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
import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.UnaryExpression;

public class ASTBuilder {
	
	
	private Formula<?> inputFormula;

	protected ArrayList<String> vars;
	protected ArrayList<String> types;
	private IMachineRoot machine;
	protected ASTBuilder(){}
	
	protected ASTBuilder(ArrayList<String> v, ArrayList<String> t)
	{
		vars = v;
		types = t;
	}
	
	
	ASTTreeNode treeBuilder(String str, IMachineRoot mch) 
	{
		machine = mch;
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
			root = buildExpressionTree((Expression) inputFormula);
			//System.out.println("Not implemented yet!" + inputFormula.toString());

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
		else if(in instanceof QuantifiedPredicate)
		{
			predicateRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());
			ASTTreeNode quantifiers = new ASTTreeNode("Quantifiers", ",", 9996);
			BoundIdentDecl[] bi = ((QuantifiedPredicate) in).getBoundIdentDecls();
			for(BoundIdentDecl b : bi)
			{
				quantifiers.addNewChild(new ASTTreeNode(b.getClass().getSimpleName(), b.toString(), b.getTag()));
			}
			predicateRoot.addNewChild(quantifiers);
			Predicate p = ((QuantifiedPredicate) in).getPredicate();
			String ps = getPredicateText(p,bi);
			predicateRoot.addNewChild(treeBuilder(ps, machine));			
		}
		else if(in instanceof BinaryPredicate)
		{
			predicateRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());
			Predicate left = ((BinaryPredicate) in).getLeft();
			Predicate right = ((BinaryPredicate) in).getLeft();
			predicateRoot.addNewChild(treeBuilder(left.toString(), machine));
			predicateRoot.addNewChild(treeBuilder(right.toString(), machine));
		}
		else if(in instanceof AssociativePredicate)
		{
			predicateRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());
			Predicate[] pchildren = ((AssociativePredicate) in).getChildren();
			for(Predicate p : pchildren)
			{
				predicateRoot.addNewChild(treeBuilder(p.toString(), machine));
			}
		}
		else if(in instanceof SimplePredicate)
		{
			predicateRoot = new ASTTreeNode(in.getClass().getSimpleName(), in.toString(), in.getTag());
			for(int i = 0; i < in.getChildCount(); i++)
			{
				predicateRoot.addNewChild(treeBuilder(((SimplePredicate) in).getChild(i).toString(), machine));
			}
		}
		else
		{
			System.out.println("Ridi : " + in.getSyntaxTree() + in.getTag());//getClass().getSimpleName());
		}
		return predicateRoot;
	}

	private String getPredicateText(Predicate p, BoundIdentDecl[] bi) {
		String ps = p.toString();
		int i = bi.length-1;
		for(BoundIdentDecl b : bi)
		{
			String id = "[["+i+"]]";
			ps = ps.replace(id, b.toString());
			i--;
		}

		return ps;
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
				//associ_exp.addNewChild(buildExpressionTree(e_ch));
				associ_exp.addNewChild(treeBuilder(e_ch.toString(), machine));
									
			}
		
			return associ_exp;
			
		}
		else if(e instanceof BinaryExpression)
		{
			ASTTreeNode bi_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			
			//bi_exp.addNewChild(buildExpressionTree(((BinaryExpression) e).getLeft()));
			bi_exp.addNewChild(treeBuilder(((BinaryExpression) e).getLeft().toString(), machine));
			//bi_exp.addNewChild(buildExpressionTree(((BinaryExpression) e).getRight()));
			bi_exp.addNewChild(treeBuilder(((BinaryExpression) e).getRight().toString(), machine));
	
			return bi_exp;

		}
		
		else if(e instanceof ExtendedExpression)
		{
			Expression[] exp_child = ((ExtendedExpression) e).getChildExpressions();
			
			ASTTreeNode extended_exp = new ASTTreeNode(e.getClass().getSimpleName(), ((ExtendedExpression) e).getExtension().getSyntaxSymbol(), 99999);//e.getTag()); //
			
			extended_exp.isExtended = true;
			
			for(Expression e_ch : exp_child)
			{
				//extended_exp.addNewChild(buildExpressionTree(e_ch));
				extended_exp.addNewChild(treeBuilder(e_ch.toString(),machine));
									
			}
	
			return extended_exp;

		}

		else if(e instanceof UnaryExpression)
		{
			ASTTreeNode unary_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			//unary_exp.addNewChild(buildExpressionTree(((UnaryExpression) e).getChild()));
			unary_exp.addNewChild(treeBuilder(((UnaryExpression) e).getChild().toString(), machine));
			
			return unary_exp;

		}

		else if(e instanceof SetExtension)
		{
			Expression[] set_child = ((SetExtension) e).getMembers();
			ASTTreeNode setex_exp = new ASTTreeNode(e.getClass().getSimpleName(), e.toString(), e.getTag());
			ASTTreeNode comma = new ASTTreeNode("Comma,", ",", 9996);
			for(Expression s_ch : set_child)
			{
				//comma.addNewChild(buildExpressionTree(s_ch));
				comma.addNewChild(treeBuilder(s_ch.toString(),machine));
				
			}
			setex_exp.addNewChild(comma);

			return setex_exp;

		}


		System.out.println("Not Defined! It is going to crash :D");
		return null;
	}
	
	

}
