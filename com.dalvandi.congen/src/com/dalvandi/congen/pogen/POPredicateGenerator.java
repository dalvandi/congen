package com.dalvandi.congen.pogen;

import java.util.ArrayList;

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
	public ArrayList<Predicate> caseguardsPredicate; 
	public ArrayList<Predicate> methodguardsPredicate;
	public ArrayList<String> methodguards;
	public String methodname;
	public ArrayList<Predicate> disjointCaseGuards;
	public Predicate goalPredicate;
	public Predicate singlePredicate;
	public ArrayList<Predicate> outputPO;
	public ArrayList<ISCGuard> iscg;
	
	public POPredicateGenerator(IRodinElement e,
			IPOGStateRepository r,
			ISCConstructorStatement isc) throws CoreException {
			iscg = new ArrayList<ISCGuard>();
			element = e;
			repository = r;
			iscConstructorStatement = isc;
			genereteHypoGoalPredicate();
	}
	
	private void genereteHypoGoalPredicate() throws CoreException
	{
		methodname = iscConstructorStatement.getMethodName();
		ArrayList<String> events = iscConstructorStatement.getListedEvents();
		ArrayList<String> par_in = iscConstructorStatement.getMethodInParameters();
		ArrayList<String> par_out = iscConstructorStatement.getMethodOutParameters();
		methodguards = getMethodGuards(events,par_in, par_out);
		methodguardsPredicate = getMethodGuardsPredicate(events,par_in, par_out);
		caseguardsPredicate = getCaseGuardsPredicate(events,methodguardsPredicate, par_in, par_out);
		outputPO = getOutputPO();
		
		
		if(caseguardsPredicate.size()>1)
		{
			goalPredicate = repository.getFormulaFactory().makeAssociativePredicate(Formula.LOR, caseguardsPredicate, null);
			singlePredicate = getSinglePredicate(methodguardsPredicate, goalPredicate, par_in);
			disjointCaseGuards = getdisjointCaseGuards(methodguardsPredicate,caseguardsPredicate,par_in);
	
		}
		else if(caseguardsPredicate.size() == 1)
		{
			goalPredicate = caseguardsPredicate.get(0);
			singlePredicate = getSinglePredicate(methodguardsPredicate, goalPredicate, par_in);

		}
		
	
	}
	
	private ArrayList<Predicate> getOutputPO() throws CoreException {
		
		ArrayList<Predicate> outguards = new ArrayList<Predicate>();

		if(iscConstructorStatement.getMethodOutParameters().size()>0)
		{
		ArrayList<ISCEvent> iscevents = getISCEvents(iscConstructorStatement.getListedEvents());
		ArrayList<String> par_in = iscConstructorStatement.getMethodInParameters();
		ArrayList<String> par_out = iscConstructorStatement.getMethodOutParameters();

		Predicate left = null;
		if(iscevents.size() >= 1)
		{
			for(ISCEvent evt : iscevents){
			ArrayList<String> par = getParameteres(evt.getSCParameters());
			ArrayList<String> par_int = getInternal(par, par_in, par_out);
			//ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(par_int);
			ArrayList<Predicate> evtcg = new ArrayList<Predicate>();
			ArrayList<Predicate> evtog = new ArrayList<Predicate>();
			for(ISCGuard g : evt.getSCGuards())
			{
				if(containsIntenal(g,par,par_in,par_out) || containsOutput(g, par_out))
				{
					IParseResult gc = repository.getFormulaFactory().parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
					Predicate cPred = gc.getParsedPredicate();
					evtog.add(cPred);
				}
				else if(!methodguards.contains(g.getPredicateString()))
				{
					IParseResult gc = repository.getFormulaFactory().parsePredicate(g.getPredicateString(), LanguageVersion.LATEST, null);
					Predicate cPred = gc.getParsedPredicate();
					evtcg.add(cPred);
				}

			}
			
			
			Predicate right = getExistentiallyQuantifiedPredicate(evtog, par_int, par_out);
			
			ArrayList<Predicate> mtdguards = new ArrayList<Predicate>(methodguardsPredicate);
			if(evtcg.size()>0)
				mtdguards.addAll(evtcg);
			
			Predicate implication = null;
			
			if(mtdguards.size()>0 && right != null)
			{
				if(mtdguards.size()>1)
					left = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, mtdguards, null);
				else
					left = mtdguards.get(0);
				
				

				implication = repository.getFormulaFactory().makeBinaryPredicate(Formula.LIMP, left, right, null);
				ArrayList<BoundIdentDecl> bound_in = getBoundIdentifiers(iscConstructorStatement.getMethodInParameters());
				if(bound_in.size()>0)
				{
					ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
					
					for(FreeIdentifier i : implication.getFreeIdentifiers())
					{
					 if(par_in.contains(i.toString()))
						{
							fid.add(i);
						}
						
					}
													
					implication = implication.bindTheseIdents(fid, repository.getFormulaFactory());
					
					QuantifiedPredicate uniPred = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.FORALL, bound_in, implication, null);
					implication = uniPred;
				}
 			}
			
			
			if(implication != null)
				outguards.add(implication);			
		}
		}
	
		}
		
		return outguards;
	}

	private Predicate getExistentiallyQuantifiedPredicate(
			ArrayList<Predicate> evtog, ArrayList<String> par_int, ArrayList<String> par_out) throws CoreException {
		
		ArrayList<String> pars = new ArrayList<String>(par_int);
		pars.addAll(par_out);
		ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(pars);

		if(evtog.size()>0)
		{
			Predicate and;
			if(evtog.size()>1)
				and = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, evtog, null);
			else
				and = evtog.get(0);
			
			Predicate result = and;
			if(bound.size()>0)
			{
				ArrayList<FreeIdentifier> fid = new ArrayList<FreeIdentifier>();
				
				for(FreeIdentifier i : and.getFreeIdentifiers())
				{
				 if(par_int.contains(i.toString()) || par_out.contains(i.toString()))
					{
						fid.add(i);
					}
				}
												
				and = and.bindTheseIdents(fid, repository.getFormulaFactory());
				
				QuantifiedPredicate existPred = repository.getFormulaFactory().makeQuantifiedPredicate(Formula.EXISTS, bound, and, null);
				result = existPred;
			}
			return result;
		}
		else	
			return null;
		
	}

	private ArrayList<Predicate> getdisjointCaseGuards(
			ArrayList<Predicate> method_guards,
			ArrayList<Predicate> case_guards, ArrayList<String> par_in) throws CoreException {
		
		ArrayList<Predicate> disjoint = new ArrayList<Predicate>();
		ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(par_in);
		ArrayList<Integer> served = new ArrayList<Integer>();

		for(int i = 0; i<case_guards.size(); i++)
		{
			ArrayList<Predicate> caseguards = new ArrayList<Predicate>(case_guards);		
			ArrayList<Predicate> left_list = new ArrayList<Predicate>(method_guards);			
			Predicate hyp = case_guards.get(i); 
			served.add(i);
			left_list.add(hyp);
			Predicate left;
			Predicate right;
			Predicate implication;

			
			for(int k : served){
				caseguards.remove(case_guards.get(k));
			}
			
			if(left_list.size()>1)
				left = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, left_list, null);
			else if(left_list.size() == 1)
				left = left_list.get(0);
			else
				left = null;

			for(int j = 0; j<caseguards.size(); j++)
			{
				caseguards.set(j, repository.getFormulaFactory().makeUnaryPredicate(Formula.NOT, caseguards.get(j), null));
			}

			if(caseguards.size() == 1)
			{
				right = caseguards.get(0);
			}
			else if(caseguards.size() > 1)
			{
				right = repository.getFormulaFactory().makeAssociativePredicate(Formula.LAND, caseguards, null);
			}
			else
			{
				right = null;
			}
		
			if(right != null && left != null)
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
		
		ArrayList<BoundIdentDecl> bound = getBoundIdentifiers(par_in);
		
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
				if((containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par_out)) 
						|| (!methodguards.contains(g.getPredicateString()) && !containsOutput(g, par_out)))
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
					if(!containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par_out))
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
					if(!containsIntenal(g,par,par_in,par_out) && !containsOutput(g, par_out))
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

	private boolean containsOutput(ISCGuard g, ArrayList<String> par_out) throws CoreException {
	
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
}
