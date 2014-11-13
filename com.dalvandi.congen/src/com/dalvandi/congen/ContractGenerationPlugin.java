package com.dalvandi.congen;

import java.util.ArrayList;

import com.dalvandi.congen.basis.IConstructorStatement;
import com.dalvandi.congen.core.*;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.Workbench;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;

@SuppressWarnings("restriction")
public class ContractGenerationPlugin implements IObjectActionDelegate {
	IStructuredSelection selection;
	IMachineRoot mch;
	FreeIdentifier[] ident;
	IParseResult parseResult;


	
	@Override
	public void run(org.eclipse.jface.action.IAction arg0) {
		System.out.println("Contract Generator Started...");
		
		
		ArrayList<String> variables = getVariables();
		ArrayList<String> types = getTypes();
		ArrayList<String> constructoroperators = getConstructorStatements();
		IMachineRoot machine = getCurrentMachine();
		
		ClassGenerator cls = new ClassGenerator(machine, constructoroperators, variables, types);
	    try {
			cls.outputGeneratedClass();
	    	} catch (RodinDBException e) {
			e.printStackTrace();
	    	}
		
	}




    public static IMachineRoot[] getMachineRootChildren(IRodinProject project) throws RodinDBException {
        ArrayList<IMachineRoot> result = new ArrayList<IMachineRoot>();
        for (IRodinElement element : project.getChildren()) {
              if (element instanceof IRodinFile) {
                    IInternalElement root = ((IRodinFile) element).getRoot();
                    if (root instanceof IMachineRoot) {
                          result.add((IMachineRoot) root);
                    }
              }
        }
        return result.toArray(new IMachineRoot[result.size()]);
  }

	@Override
	public void selectionChanged(org.eclipse.jface.action.IAction arg0,
			ISelection arg1) {
			this.selection = (IStructuredSelection) selection;
		
	}




	@Override
	public void setActivePart(org.eclipse.jface.action.IAction arg0,
			IWorkbenchPart arg1) {
		// TODO Auto-generated method stub
		
	}
	
	private static IMachineRoot getCurrentMachine(){    
		ISelectionService selectionService =     
            Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();    

        ISelection selection = selectionService.getSelection();    
        IMachineRoot machine = null;    
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    
				if (element instanceof IMachineRoot) {    
    			      machine= (IMachineRoot)element;   
            } 
            }     
        return machine;    
    }
	
	
	private ArrayList<String> getConstructorStatements()
	{
		ArrayList<String> constats = new ArrayList<String>();
		IMachineRoot machine = getCurrentMachine();
		try {
			for(IRodinElement el : machine.getChildren())
			{
				if(el instanceof IConstructorStatement)
				{
				//System.out.println();	
					constats.add(((IConstructorStatement) el).getExpressionString());
				}
			}
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return constats;
		
	}
	
	private ArrayList<String> getVariables()
	{
	
		ArrayList<String> variables = new ArrayList<String>();
		IMachineRoot machine = getCurrentMachine();
		
		try {
			for(IVariable var: machine.getVariables())
			{
				variables.add(var.getIdentifierString());
			}
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return variables;
		
	}
	
	private ArrayList<String> getTypes()
	{

		ArrayList<String> types = new ArrayList<String>();
		IMachineRoot machine = getCurrentMachine();
		ISeesContext[] see = null;
		try {
			see = machine.getSeesClauses();
		} catch (RodinDBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		IContextRoot context;
		for(ISeesContext c: see)
		{
			try {
				context = c.getSeenContextRoot();
				try {
					for(ICarrierSet type : context.getCarrierSets())
					{
						types.add(type.getIdentifierString());
					}
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (RodinDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		return types;
		
	}
}