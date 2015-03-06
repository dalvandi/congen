package com.dalvandi.congen.basis;

import org.eventb.core.basis.EventBElement;
import org.eventb.core.basis.SCExpressionElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;

public class SCConstructorStatement extends EventBElement implements ISCConstructorStatement {

	public SCConstructorStatement(String arg0, IRodinElement arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IInternalElementType<? extends IInternalElement> getElementType() {
		// TODO Auto-generated method stub
		return ELEMENT_TYPE;
	}

}
