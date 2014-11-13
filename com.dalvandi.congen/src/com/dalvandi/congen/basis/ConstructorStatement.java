package com.dalvandi.congen.basis;

import org.eventb.core.basis.EventBElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;

public class ConstructorStatement extends EventBElement implements IConstructorStatement {

	public ConstructorStatement(String arg0, IRodinElement arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IInternalElementType<? extends IInternalElement> getElementType() {
		// TODO Auto-generated method stub
		return IConstructorStatement.ELEMENT_TYPE;
	}

}
