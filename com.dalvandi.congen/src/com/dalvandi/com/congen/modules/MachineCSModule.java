package com.dalvandi.com.congen.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import com.dalvandi.congen.Activator;
import com.dalvandi.congen.basis.IConstructorStatement;
import com.dalvandi.congen.basis.ISCConstructorStatement;

public class MachineCSModule extends SCProcessorModule {

	public static final IModuleType<MachineCSModule> MODULE_TYPE = SCCore.getModuleType(Activator.PLUGIN_ID + ".machineCSModule");
	private static final String CS_NAME_PREFIX = "CS";

	@Override
	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		//System.out.println("\n\n******************************\nSTATIC CHECK:\n******************************");
		monitor.subTask("ProcessingBound");

		final IRodinFile machineFile = (IRodinFile) element;
		final IMachineRoot machineRoot = (IMachineRoot) machineFile.getRoot();
		machineRoot.getFormulaFactory();
		final IConstructorStatement[] constatement = machineRoot.getChildrenOfType(IConstructorStatement.ELEMENT_TYPE);
		//System.out.println(constatement.length);

		// ****************************************************************
		// ****************************************************************
		// A number of static checks can be implemented here in the future
		// ****************************************************************
		// ****************************************************************
		
		if(constatement.length>0){
		for(int i=0; i<constatement.length; i++){
		//System.out.println(constatement[i].getExpressionString());
		ISCConstructorStatement scCons = (ISCConstructorStatement) target.getInternalElement(ISCConstructorStatement.ELEMENT_TYPE, CS_NAME_PREFIX+i);
		scCons.create(null, monitor);
		scCons.setComment(constatement[i].getExpressionString(), null);
		scCons.setSource(constatement[i], monitor);
		
		
		}	

		}
		//System.out.println("******************************\nSTATIC DONE!\n******************************\n\n");

	}

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
