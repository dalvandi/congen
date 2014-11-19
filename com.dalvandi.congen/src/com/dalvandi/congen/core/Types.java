/*
 * Sadegh Dalvandi (www.dalvandi.com) - 18 November 2014
 * 
 * Class Type
 * All Event-B built-in types and user-defined types should be stored in this class.
 *
*/

package com.dalvandi.congen.core;

import java.util.ArrayList;

public class Types {

	public ArrayList<Integer> builtin_types;
	public ArrayList<String> extended_types;
	
	Types(){
		builtin_types = new ArrayList<Integer>();
		extended_types = new ArrayList<String>();
		
		builtin_types.add(401); // Integer
		builtin_types.add(402); // NAT
		builtin_types.add(404); // BOOL
		extended_types.add("seq"); // Sequence
	}

}
