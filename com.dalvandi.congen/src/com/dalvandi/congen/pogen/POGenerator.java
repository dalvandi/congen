/*******************************************************************************
 *******************************************************************************/
package com.dalvandi.congen.pogen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPOPredicateSet;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSource;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCPredicateElement;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pog.IPOGHint;
import org.eventb.core.pog.IPOGPredicate;
import org.eventb.core.pog.IPOGSource;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGProcessorModule;
import org.eventb.core.pog.state.IMachineHypothesisManager;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;

import com.dalvandi.congen.Activator;
import com.dalvandi.congen.basis.ISCConstructorStatement;


/**
 */

public class POGenerator extends POGProcessorModule {

	private ISCMachineRoot root;
	private static final IModuleType<POGenerator> MODULE_TYPE = POGCore
			.getModuleType(Activator.PLUGIN_ID
					+ ".poGenerator");

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IPOGStateRepository repository,
		IProgressMonitor monitor) throws CoreException {
		
		final FormulaFactory ff = repository.getFormulaFactory();
		final IRodinFile machineFile = (IRodinFile) element;
		root = (ISCMachineRoot) machineFile.getRoot();
		final ISCConstructorStatement[] cs = (ISCConstructorStatement[]) root.getChildrenOfType(ISCConstructorStatement.ELEMENT_TYPE);
		
		for(int i = 0 ; i<cs.length; i++){
			if(!cs[i].getComment().contains("Init()"))
			{
			System.out.println(cs[i].getComment() + " - " + cs[i].getElementName() + " :");
			POPredicateGenerator cpo = new POPredicateGenerator(element, repository, cs[i]);

			if(cpo.caseguardsPredicate.size()>0)
				generateCompleteness(cpo.singlePredicate, cpo.iscg, element, repository, ff, monitor,cpo.methodname);
			
			if(cpo.disjointCaseGuards != null)
				generateOverlap(cpo.caseguardsPredicate, cpo.disjointCaseGuards, cpo.methodname, element, repository, ff, monitor);
			}
		}

	}

	private void generateOverlap(ArrayList<Predicate> caseguardsPredicate,
			ArrayList<Predicate> disjointCaseGuards, String methodname, IRodinElement element, IPOGStateRepository repository,
			FormulaFactory ff, IProgressMonitor monitor) throws CoreException {

		final IPOGSource[] sources = new IPOGSource[] {makeSource(IPOSource.DEFAULT_ROLE, element),
				makeSource(IPOSource.DEFAULT_ROLE, element)};
		final IPORoot target = repository.getTarget();
		final IMachineHypothesisManager machineHypothesisManager = (IMachineHypothesisManager) repository
				.getState(IMachineHypothesisManager.STATE_TYPE);
		
		IPOPredicateSet hyp = target.getPredicateSet("ABSHYP");
		List<IPOGPredicate> invariants = new ArrayList<IPOGPredicate>();
		ISCPredicateElement inv[] = root.getSCInvariants();
		for(ISCPredicateElement i : inv)
		{
			if(!((ISCInvariant)i).getLabel().contains("g_")){
				 IParseResult hyporesult = ff.parsePredicate(i.getPredicateString(), LanguageVersion.LATEST, null);
				 Predicate hPredicate =  hyporesult.getParsedPredicate();
				 invariants.add(makePredicate(hPredicate, element));
			}			
		}
		int j = 1;
		for(int i = 0; i <disjointCaseGuards.size(); i++)
		{
		String sequentName = methodname + "/DSJC/C"+j;
		createPO(target, 
				sequentName,
				POGProcessorModule.makeNature("Case_Disjoint_Proof_Obligation"),
				machineHypothesisManager.getRootHypothesis(),
				invariants,
				makePredicate(disjointCaseGuards.get(i), element), 
				sources,
				new IPOGHint[] {
				makeIntervalSelectionHint(hyp, getSequentHypothesis(target, sequentName))
				},
				true,
				monitor);
		j++;
		}

	}

	private void generateCompleteness(Predicate singlePredicate, ArrayList<ISCGuard> iscg, IRodinElement element, IPOGStateRepository repository, FormulaFactory ff, IProgressMonitor monitor, String methodname) throws CoreException {
		
		final IPOGSource[] sources = new IPOGSource[] {makeSource(IPOSource.DEFAULT_ROLE, element),
				makeSource(IPOSource.DEFAULT_ROLE, element)};
		final IPORoot target = repository.getTarget();
		final IMachineHypothesisManager machineHypothesisManager = (IMachineHypothesisManager) repository
				.getState(IMachineHypothesisManager.STATE_TYPE);
		
		IPOPredicateSet hyp = target.getPredicateSet("ABSHYP");
		List<IPOGPredicate> invariants = new ArrayList<IPOGPredicate>();
		ISCPredicateElement inv[] = root.getSCInvariants();
		for(ISCPredicateElement i : inv)
		{
			if(!((ISCInvariant)i).getLabel().contains("g_")){
				 IParseResult hyporesult = ff.parsePredicate(i.getPredicateString(), LanguageVersion.LATEST, null);
				 Predicate hPredicate =  hyporesult.getParsedPredicate();
				 invariants.add(makePredicate(hPredicate, element));
			}			
		}
		
		String sequentName = methodname + "/MTD/CMP";

		createPO(target, 
				sequentName,
				POGProcessorModule.makeNature("Completeness_Proof_Obligation"),
				machineHypothesisManager.getRootHypothesis(),
				invariants,
				makePredicate(singlePredicate, element), 
				sources,
				new IPOGHint[] {
				makeIntervalSelectionHint(hyp, getSequentHypothesis(target, sequentName))
				},
				true,
				monitor);

	}


	@Override
	public void initModule(IRodinElement element,
			IPOGStateRepository repository, IProgressMonitor monitor)
			throws CoreException {

	}


	
	/*
	 * 
	
			ITypeCheckResult tch = goalPredicate2.typeCheck(repository.getTypeEnvironment());
		if(tch.getInferredEnvironment()==null)
			System.out.println("TCH FAILED ");
		System.out.println("*******************************");
		System.out.println("Goal PRED: " + goalPredicate2.toString());
		System.out.println("WD PRED: " + goalPredicate2.getWDPredicate(tch.getInferredEnvironment().getFormulaFactory()).toString());
		Predicate wdGoal = goalPredicate2.getWDPredicate(tch.getInferredEnvironment().getFormulaFactory());
		System.out.println("Flatten PRED: " +  goalPredicate2.flatten(ff).toString());
		System.out.println("*******************************");


		//
		final String goalformula = "\u2203 i \u00B7 i \u2208 1 ‥ seqSize(keys) \u2227 i = seqSize(keys)"; // " (k > 100) \u2228 (k<100)"; //i \u2208 1 ‥ seqSize(keys) \u2227 \u2203 i \u00B7   \u2227 seqElemAccess(keys,i) = k seqElemAccess(keys,i) = k
		final IParseResult result = ff.parsePredicatePattern(goalformula, LanguageVersion.LATEST, null);//.parseExpression(formula, LanguageVersion.LATEST, null);
		final Predicate goalPredicate = result.getParsedPredicate();

		final String hypoformula = hypo;
		final IParseResult hyporesult = ff.parsePredicate(hypoformula, LanguageVersion.LATEST, null);//.parseExpression(formula, LanguageVersion.LATEST, null);
		final Predicate hypoPredicate = hyporesult.getParsedPredicate();

		//
		if(hypoPredicate != null)
			System.out.println("Hypo Pred: " + hypoPredicate.toString());
		else
			System.out.println("Hypo Str: " + hypo);	
		
		if(goalPredicate != null)
			System.out.println("Goal Pred: " + goalPredicate2.toString());
		else
			System.out.println("Goal Str: " + goal);	



	final IPOGSource[] sources = new IPOGSource[] {makeSource(IPOSource.DEFAULT_ROLE, element)};
	final IPORoot target = repository.getTarget();
	final IMachineHypothesisManager machineHypothesisManager = (IMachineHypothesisManager) repository
			.getState(IMachineHypothesisManager.STATE_TYPE);

	
	//
	final String formula = "{1}";
	final IParseResult result = ff.parseExpression(formula, LanguageVersion.LATEST, null);//.parseExpression(formula, LanguageVersion.LATEST, null);
	final Expression csExpression = result.getParsedExpression();
	//
	
	
	final Predicate finPredicate = ff.makeSimplePredicate(
			Formula.KFINITE, csExpression, null);
	
	createPO(target, "BFN",
			POGProcessorModule.makeNature("Finiteness of bound"),
			machineHypothesisManager.getFullHypothesis(),
			Collections.<IPOGPredicate> emptyList(),
			makePredicate(finPredicate, element), sources,
			new IPOGHint[0],
			machineHypothesisManager.machineIsAccurate(), monitor);
*/
}
