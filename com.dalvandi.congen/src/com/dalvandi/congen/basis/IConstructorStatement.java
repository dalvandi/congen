package com.dalvandi.congen.basis;

import java.util.ArrayList;

import org.eventb.core.ICommentedElement;
import org.eventb.core.IExpressionElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import com.dalvandi.congen.Activator;

public interface IConstructorStatement extends ICommentedElement, IExpressionElement{

	IInternalElementType<IConstructorStatement> ELEMENT_TYPE = RodinCore
			.getInternalElementType(Activator.PLUGIN_ID + ".constructorstatement");
	
	public ArrayList<String> getListedEvents() throws RodinDBException;
	public ArrayList<String> getMethodInParameters() throws RodinDBException;
	public ArrayList<String> getMethodOutParameters() throws RodinDBException;
	public String getMethodName() throws RodinDBException;

}
