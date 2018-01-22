// ----------------------------------------------------------------------------
// Copyright (C) 2014 Louise A. Dennis, and Michael Fisher 
// 
// This file is part of the Agent Infrastructure Layer (AIL)
// 
// The AIL is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// The AIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with the AIL; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package actiononly;

import mcaplantlr.runtime.ANTLRFileStream;
import mcaplantlr.runtime.ANTLRStringStream;
import mcaplantlr.runtime.CommonTokenStream;

import ail.mas.MAS;
import ail.syntax.ast.Abstract_MAS;
import ail.mas.MASBuilder;

import actiononly.parser.ActionOnlyLexer;
import actiononly.parser.ActionOnlyParser;

/**
 * Utility class.  Builds an Action Only MAS by parsing a string or a file.
 * @author louiseadennis
 *
 */
public class ActionOnlyMASBuilder implements MASBuilder {
	MAS mas;
	
	Abstract_MAS amas;
	
	public ActionOnlyMASBuilder() {};
	
	public ActionOnlyMASBuilder(String masstring, boolean filename) {
		if (filename) {
			parsefile(masstring);
		} else {
			parse(masstring);
		}
		mas = amas.toMCAPL();
     }
	
	
	public void parsefile(String masstring) {
		try {
			ActionOnlyLexer lexer = new ActionOnlyLexer(new ANTLRFileStream(masstring));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ActionOnlyParser parser = new ActionOnlyParser(tokens);
    		amas = parser.mas();
      	} catch (Exception e) {
     		e.printStackTrace();
    	}
		
	}

	public void parse(String masstring) {
	   	ActionOnlyLexer lexer = new ActionOnlyLexer(new ANTLRStringStream(masstring));
    	CommonTokenStream tokens = new CommonTokenStream(lexer);
    	ActionOnlyParser parser = new ActionOnlyParser(tokens);
    	try {
    		amas = parser.mas();
     	} catch (Exception e) {
     		e.printStackTrace();
    	}
		
	}
	/**
	 * Getter method for the resulting MAS.
	 * @return
	 */
	public MAS getMAS() {
		return mas;
	}
	
	public MAS getMAS(String filename) {
		parsefile(filename);

		return amas.toMCAPL();
	}
	

}
