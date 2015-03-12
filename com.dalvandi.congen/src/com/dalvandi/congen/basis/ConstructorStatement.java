package com.dalvandi.congen.basis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eventb.core.basis.EventBElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

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
	
	public ArrayList<String> getListedEvents() throws RodinDBException {
		String s = this.getComment();
		s = s.replaceAll("\\s","");
		ArrayList<String> evts = new ArrayList<String>();
		String type = "\\{([\\s*|\\w*|\\W*|\\S*]+)\\}";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
			evts = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return evts;
		}
		return evts;
	}
	
	public ArrayList<String> getMethodInParameters() throws RodinDBException {

		String s = this.getComment();

		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);
		s = s.replaceAll("\\s","");
		ArrayList<String> pars = new ArrayList<String>();
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    pars = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return pars;
		}
		return pars;
	}
	
	
	public ArrayList<String> getMethodOutParameters() throws RodinDBException {

		String s = this.getComment();
		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(i_ret,s.length());
		
		s = s.replaceAll("\\s","");
		ArrayList<String> pars = new ArrayList<String>();
		String type = "\\(([\\s*|\\w*|\\W*|\\S*]+)\\)";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		

		if (m.find())
		{
		    pars = new ArrayList<String>(Arrays.asList(m.group(1).split(",")));
		    return pars;
		}
		return pars;
	}
	
	public String getMethodName() throws RodinDBException {
		//method $name($a,$b) returns ($c,$d) = {$ev1, $ev2}
		String s = this.getComment();
		int i_ret = s.indexOf("returns");
		s = (String) s.subSequence(0, i_ret);

		String type = "method ([\\s*|\\w*|\\W*|\\S*]+)\\(";
		Matcher m;

			
		Pattern p =  Pattern.compile(type);
		m = p.matcher(s);
		
		if (m.find())
		{
		    return m.group(1);
		}
		else
			return "ND";
		
	}

}
