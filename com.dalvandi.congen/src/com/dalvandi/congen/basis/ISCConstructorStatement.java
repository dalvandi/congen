package com.dalvandi.congen.basis;

import org.eventb.core.ICommentedElement;
import org.eventb.core.ISCExpressionElement;
import org.eventb.core.ITraceableElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;

import com.dalvandi.congen.Activator;

public interface ISCConstructorStatement extends ICommentedElement, ITraceableElement{
	IInternalElementType<IConstructorStatement> ELEMENT_TYPE = RodinCore
			.getInternalElementType(Activator.PLUGIN_ID + ".scconstructorstatement");

}
