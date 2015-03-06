package com.dalvandi.congen.pogen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCParameter;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

import com.dalvandi.congen.basis.ISCConstructorStatement;

public class POPredicateGenerator {

	private IRodinElement element;
	private IPOGStateRepository repository;
	private ISCConstructorStatement iscConstructorStatement;
	
	public ArrayList<Predicate> caseguardsPredicate; // TODO: remove this
	public ArrayList<String> methodguards;
	public String methodname;
	public ArrayList<Predicate> disjointCaseGuards;
	public Predicate goalPredicate;
	public Predicate singlePredicate;
	public ArrayList<ISCGuard> iscg;
	
	public POPredicateGenerator(IRodinElement e,
			IPOGStateRepository r,
			ISCConstructorStatement isc) throws CoreException {
			iscg = new ArrayList<ISCGuard>();
			element = e;
			repository = r;
			iscConstructorStatement = isc;
			generateHypoGoal();
			genereteHypoGoalPredicate();
	}
	
	
	private void genereteHypoGoalPredicate() throws CoreException
	{
		ArrayList<String> events = getEvents(iscConstructorStatement.getComment());
		ArrayList<String> par_in = getMethodInParameters(iscConstructorStatement.getComment());
		ArrayList<String> par_out = getMethodOutParameters(iscConstructorStatement.getComment());
		ArrayList<Predicate> method_guards = getMethodGuardsPredicate(events,par_in, par_out);
		ArrayList<Predicate> case_guards = getCaseGuardsPredicate(events,method_guards, par_in, par_out);
		methodname = getMethodName(iscConstructorStatement.getComment());
		caseguardsPredicate = case_guards;
		
		
		if(case_guards.size()>1)
		{
			goalPredicate = repository.getFormulaFactory().makeAssociativePredicate(Formula.LOR, case_guards, null);
			singlePredicate = getSinglePredicate(method_guards, goalPredicate, par_in);
			disjointCaseGuards = getdisjointCaseGuards(method_guards,case_guards,par_in);
			for(Predicate p : disjointCaseGuards)
			{
				System.out.println("Disjoint PO: " + p.toString());
			}
		}
		else if(case_guards.size() == 1)
		{
			goalPredicate = case_guards.get(0);
			singlePredicate = getSinglePredicate(method_guards, goalPredicate, par_in);

		}
		
	
	}
	
	
	
	private ArrayList<Predicate> getdisjointCaseGuards(
			ArrayList<Predicate> method_guards,
			ArrayList<Predicate> case_guards, ArrayList<String> par_in) throws CoreException {
		
		ArrayList<Predicate> disjoint = new ArrayList<Predicate>();
		ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(par_in);
				
		for(int i = 0; i<case_guards.size()-1; i++)
		{
			
			ArrayList<Predicate> temp = new ArrayList<Predicate>(case_guards);
			ArrayList<Predicate> left_list = new ArrayList<Predicate>(method_guards);			
			left_list.add(temp.remove(i));
			Predicate left;
			Predicate right;
			Predicate implication;

			if(left_list.size()>1)
				left = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, left_list, null);
			else
				left = left_list.get(0);
			
			
			for(int j = 0; j<temp.size(); j++)
			{
				temp.set(j, repository.getFormulaFactory().makeUnaryPredicate(Formula.NOT, temp.get(j), null));
			}
			
			if(temp.size() == 1)
			{
				right = temp.get(0);
			}
			else if(temp.size() > 1)
			{
				right = repository.getFormulaFactory().makeAssociativePredicate(Formula.LOR, temp, null);
			}
			else
			{
				right = null;
			}
			
			if(right != null)
			{
				implication = repository.getFormulaFactory().makeBinaryPredicate(Formula.LIMP, left, right, null);
				if(par_in.size()>0){
				ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
				for(FreeIdentifier k : implication.getFreeIdentifiers())
				{
				 if(par_in.contains(k.toString()))
					{
						fid.add(k);
					}
					
				}
				implication = implication.bindTheseIdents(fid, repository.getFormulaFactory());
				implication = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.FORALL, bound, implication, null);
				}
				
				disjoint.add(implication);
				
			}
		}
		
		return disjoint;
	}


	
	private ArrayList<BoundIdentDecl> getBoundIdentifiers(
			ArrayList<String> par) throws CoreException {
		ArrayList<BoundIdentDecl> bound = new ArrayList<BoundIdentDecl>();
		for(String i : par)
		{
			bound.add(repository.getFormulaFactory().makeBoundIdentDecl(i, null));
		}
		return bound;
	}


	private Predicate getSinglePredicate(ArrayList<Predicate> method_guards,
			Predicate goalPredicate2, ArrayList<String> par_in) throws CoreException {
		
		ArrayList<BoundIdentDecl> bound = new ArrayList<BoundIdentDecl>();
		for(String i : par_in)
		{
			bound.add(repository.getFormulaFactory().makeBoundIdentDecl(i, null));
		}
		
		Predicate andHypoPredicate;
		
		if(method_guards.size()>1)
			andHypoPredicate = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, method_guards, null);
		else if(method_guards.size() == 1)
			andHypoPredicate = method_guards.get(0);
		else
			andHypoPredicate = null;
		
		Predicate implication;
		
		if(andHypoPredicate != null)
			implication = repository.getFormulaFactory().makeBinaryPredicate(Formula.LIMP, andHypoPredicate, goalPredicate2, null);
		else
			implication = goalPredicate2;
		
		
		
		
		
		ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
		
		for(FreeIdentifier i : implication.getFreeIdentifiers())
		{
		 if(par_in.contains(i.toString()))
			{
				fid.add(i);
			}
			
		}
		
		Predicate forallPred = implication;	
		
		if(par_in.size()>0){
		implication = implication.bindTheseIdents(fid, repository.getFormulaFactory());
		forallPred = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.FORALL, bound, implication, null);
		}
				
		
		return forallPred;
	}


	private ArrayList<Predicate> getCaseGuardsPredicate(
			ArrayList<String> events, ArrayList<Predicate> method_guards,
			ArrayList<String> par_in, ArrayList<String> par_out) throws RodinDBException, CoreException {
		
		ArrayList<ISCEvent> iscevents = getISCEvents(events);
		ArrayList<Predicate> caseguards = new ArrayList<Predicate>();
		Predicate finalCaseGuard = null;
		if(iscevents.size() >= 1)
		{
			for(ISCEvent evt : iscevents){
			ArrayList<String> par = getParameteres(evt.getSCParameters());
			ArrayList<String> par_int = getInternal(par, par_in, par_out);
			ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(par_int);
			ArrayList<Predicate> evtcg = new ArrayList<Predicate>();
			Predicate and;
			for(ISCGuard g : evt.getSCGuards())
			{
				if((containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par, par_in, par_out)) 
						|| (!methodguards.contains(g.getPredicateString()) && !containsOutput(g, par, par_in, par_out)))
				{
					iscg.add(g);
					final IParseResult gc = repository.getFormulaFactory().parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);//.parseExpression(formula, LanguageVersion.LATEST, null);
					final Predicate cPred = gc.getParsedPredicate();
					evtcg.add(cPred);
				}

			}
			
			if(evtcg.size()>1)
			{
			and = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, evtcg, null);
			finalCaseGuard = and;
			if(bound.size()>0)
			{
				ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
				
				for(FreeIdentifier i : and.getFreeIdentifiers())
				{
				 if(par_int.contains(i.toString()))
					{
						fid.add(i);
					}
					
				}
												
				and = and.bindTheseIdents(fid, repository.getFormulaFactory());
				
				QuantifiedPredicate existPred = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.EXISTS, bound, and, null);
				finalCaseGuard = existPred;
			}
			}
			else if(evtcg.size()==1)
			{
				and = evtcg.get(0);
				finalCaseGuard = and;
				if(bound.size()>0)
				{
					ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
					
					for(FreeIdentifier i : and.getFreeIdentifiers())
					{
					 if(par_int.contains(i.toString()))
						{
							fid.add(i);
						}
						
					}
					
					and = and.bindTheseIdents(fid, repository.getFormulaFactory());
					
					QuantifiedPredicate existPred = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.EXISTS, bound, and, null);
					finalCaseGuard = existPred;
					}
			}
			
			if(finalCaseGuard != null)
				caseguards.add(finalCaseGuard);			
		}
		}
		return caseguards;
	}


	private ArrayList<Predicate> getMethodGuardsPredicate(
			ArrayList<String> events, ArrayList<String> par_in,
			ArrayList<String> par_out) throws RodinDBException, CoreException {
		
		ArrayList<ISCEvent> iscevents = getISCEvents(events);
		ArrayList<Predicate> methodguard = new ArrayList<Predicate>();
		
		if(iscevents.size()>= 1)
		{
			ArrayList<String> par = getParameteres(iscevents.get(0).getSCParameters());
			
			for(ISCGuard g : iscevents.get(0).getSCGuards())
			{
				int grdNum = 0;
				for(ISCEvent evt : iscevents)
				{
					for(ISCGuard grd : evt.getSCGuards())
						if(g.getPredicateString().equals(grd.getPredicateString()))
						{
							grdNum++;
						}
				}
				
				if(grdNum == iscevents.size())
				{
					if(!containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par, par_in, par_out))
					{
						final IParseResult gp = repository.getFormulaFactory().parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);//.parseExpression(formula, LanguageVersion.LATEST, null);
						final Predicate gPred = gp.getParsedPredicate();
						
						methodguard.add(gPred);
					}

				}
			}
		}
		
		return methodguard;
	}


	private void generateHypoGoal() throws CoreException
	{
		ArrayList<String> events = getEvents(iscConstructorStatement.getComment());
		ArrayList<String> par_in = getMethodInParameters(iscConstructorStatement.getComment());
		ArrayList<String> par_out = getMethodOutParameters(iscConstructorStatement.getComment());
		ArrayList<String> method_guards = getMethodGuards(events,par_in, par_out);
		ArrayList<String> case_guards = getCaseGuards(events,method_guards, par_in, par_out);
		String hypo_com = "";
		String goal_com = "";
		methodname = getMethodName(iscConstructorStatement.getComment());
		
		if(method_guards.size()>1)
		for(int i=0 ; i<method_guards.size(); i++)
		{
	    	if(i == method_guards.size()-1)
	    	{
	    		hypo_com = hypo_com + method_guards.get(i);
	    	}
	    	else
	    	{
	    		hypo_com = hypo_com + method_guards.get(i) + " \u2227 ";

	    	}
		}
		else if(method_guards.size()==1)
			hypo_com = method_guards.get(0);
		
		if(case_guards.size()>1)
		for(int i=0 ; i<case_guards.size(); i++)
		{
	    	if(i == case_guards.size()-1)
	    	{
	    		goal_com =  goal_com + "(" + case_guards.get(i) + ")";
	    	}
	    	else
	    	{
	    		goal_com = goal_com + "(" + case_guards.get(i) + ")" + " \u2228 ";

	    	}
		}
		else if(case_guards.size()==1)
			goal_com = case_guards.get(0);
		
		
		methodguards = method_guards;

	}

	
	private ArrayList<String> getCaseGuards(ArrayList<String> events, ArrayList<String> methodguards, ArrayList<String> par_in, ArrayList<String> par_out) throws CoreException {
		ArrayList<ISCEvent> iscevents = getISCEvents(events);
		ArrayList<String> caseguards = new ArrayList<String>();
		
		if(iscevents.size() >= 1)
		{
			for(ISCEvent evt : iscevents){
			ArrayList<String> par = getParameteres(evt.getSCParameters());
			ArrayList<String> par_int = getInternal(par, par_in, par_out);
			ArrayList<String> evtcg = new ArrayList<String>(); 
			String caseg = "";
			for(ISCGuard g : evt.getSCGuards())
			{
				if((containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par, par_in, par_out)) 
						|| !methodguards.contains(g.getPredicateString()))
				{
					evtcg.add(g.getPredicateString());
				}

			}
			if(par_int.size()>0)
			{
				String exists = "\u2203";
			    for(int i = 0 ; i <= par_int.size()-1; i++)
					{
			    	if(i == par_int.size()-1)
					 exists = exists + par_int.get(i) + "\u00B7";
			    	else
			    	 exists = exists + par_int.get(i) + ", ";
				}
				caseg = caseg + exists;
			}
			
			if(evtcg.size()>0)
			{
				String and = "";
			    for(int i = 0 ; i <= evtcg.size()-1; i++)
					{
			    	if(i == evtcg.size()-1)
			    	{
					 and = and + evtcg.get(i);
			    	}
			    	else{
			    	and = and + evtcg.get(i) + " \u2227 ";
			    	}
				}
			    caseg = caseg + and;
			}
			
			if(caseg != "")
				caseguards.add(caseg);	
			
		}
			
		}
		
		return caseguards;
	}



	private ArrayList<String> getMethodGuards(ArrayList<String> events, ArrayList<String> par_in, ArrayList<String> par_out) throws CoreException {
		ArrayList<ISCEvent> iscevents = getISCEvents(events);
		ArrayList<String> methodguard = new ArrayList<String>();
		
		if(iscevents.size()>= 1)
		{
			ArrayList<String> par = getParameteres(iscevents.get(0).getSCParameters());
			
			for(ISCGuard g : iscevents.get(0).getSCGuards())
			{
				int grdNum = 0;
				for(ISCEvent evt : iscevents)
				{
					for(ISCGuard grd : evt.getSCGuards())
						if(g.getPredicateString().equals(grd.getPredicateString()))
						{
							grdNum++;
						}
				}
				
				if(grdNum == iscevents.size())
				{
					if(!containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par, par_in, par_out))
					{
						methodguard.add(g.getPredicateString());
					}

				}
			}
		}
		
		return methodguard;
	}




	private boolean containsIntenal(ISCGuard g, ArrayList<String> par, ArrayList<String> par_in,
			ArrayList<String> par_out) throws RodinDBException, CoreException {
		
		ArrayList<String> par_int = getInternal(par, par_in, par_out);
		Predicate gp = getPredicate(g); 
		FreeIdentifier[] fid = gp.getFreeIdentifiers();
		
		for(FreeIdentifier id : fid)
		{
			if(par_int.contains(id.getName()))
			{
				return true;
			}
		}
		
		return false;
	}


	private Predicate getPredicate(ISCGuard g) throws CoreException {
		
		FormulaFactory ff = repository.getFormulaFactory();
		IParseResult parseResult = ff.parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
		Predicate pr = parseResult.getParsedPredicate();
		
		return pr;
	}


	private ArrayList<String> getInternal(ArrayList<String> par,
			ArrayList<String> par_in, ArrayList<String> par_out) {
		ArrayList<String> par_int = new ArrayList<String>();
		for(String s : par)
		{
			if(!par_in.contains(s) && !par_out.contains(s))
			{
				par_int.add(s);
			}
		}
		
		return par_int;
	}


	private boolean containsOutput(ISCGuard g, ArrayList<String> par, ArrayList<String> par_in, ArrayList<String> par_out) throws CoreException {
	
		Predicate gp = getPredicate(g); 
		FreeIdentifier[] fid = gp.getFreeIdentifiers();
		
		for(FreeIdentifier id : fid)
		{
			if(par_out.contains(id.getName()))
			{
				return true;
			}
		}
		
		return false;
	}



	
	private ArrayList<String> getParameteres(ISCParameter[] scParameters) throws RodinDBException {

		ArrayList<String> par = new ArrayList<String>();
		for(ISCParameter p : scParameters)
		{
			par.add(p.getIdentifierString());
		}
		return par;
	}

	private ArrayList<ISCEvent> getISCEvents(ArrayList<String> events) throws RodinDBException {
		
		ArrayList<ISCEvent> evts = new ArrayList<ISCEvent>(); 
		IRodinFile machineFile = (IRodinFile) element;
		ISCMachineRoot root = (ISCMachineRoot) machineFile.getRoot();
		ISCEvent[] iscEvents = (ISCEvent[]) root.getChildrenOfType(ISCEvent.ELEMENT_TYPE);
		for(ISCEvent e : iscEvents)
		{
			if(events.contains(e.getLabel()))
			{
				evts.add(e);
			}
		}

		return evts;
	}
	
	private ArrayList<String> getEvents(String s) {
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
	
	private ArrayList<String> getMethodInParameters(String s) {

		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);
		
		s = s.replaceAll("\\s","");
		ArrayList<String> pars = new ArrayList<String>();
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    pars = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return pars;
		}
		return pars;
	}
	
	
	private ArrayList<String> getMethodOutParameters(String s) {
		
		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(i_ret,s.length());
		
		s = s.replaceAll("\\s","");
		ArrayList<String> pars = new ArrayList<String>();
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    pars = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return pars;
		}
		return pars;
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
		else
			return "ND";
		
	}

}
