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

			POPredicateGenerator cpo = new POPredicateGenerator(element, repository, cs[i]);

			if(cpo.caseguardsPredicate.size()>0)
				generateCompleteness(cpo.singlePredicate, cpo.iscg, element, repository, ff, monitor,cpo.methodname);
			
			if(cpo.disjointCaseGuards != null)
				generateOverlap(cpo.caseguardsPredicate, cpo.disjointCaseGuards, cpo.methodname, element, repository, ff, monitor);
			}
		}
		
		System.out.println("\n\nAll contract generator POs are generated.\n\n");


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
		System.out.println(sequentName + " PO is generated.");
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

		System.out.println(sequentName + " PO is generated.");


	}

	@Override
	public void initModule(IRodinElement element,
			IPOGStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		System.out.println("Proof Obligation Generator started:\n\n");

	}

}
