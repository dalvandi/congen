package com.dalvandi.congen.basis;

import org.eventb.core.ICommentedElement;
import org.eventb.core.IExpressionElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;

import com.dalvandi.congen.Activator;

//public interface IConstructorStatement extends ICommentedElement, IExpressionElement{
public interface IConstructorStatement extends ICommentedElement, IExpressionElement{

	IInternalElementType<IConstructorStatement> ELEMENT_TYPE = RodinCore
			.getInternalElementType(Activator.PLUGIN_ID + ".constructorstatement");

}
