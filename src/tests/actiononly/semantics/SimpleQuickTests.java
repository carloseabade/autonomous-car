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
package actiononly.semantics;

import junit.framework.Assert;

import org.junit.Test;

import actiononly.ActionOnlyMASBuilder;
import ajpf.MCAPLcontroller;
import ail.mas.MAS;
import actiononly.goal.GoalEnv;

public class SimpleQuickTests {
	@Test public void simpleagent() {
		try {
			String abs_filename = MCAPLcontroller.getFilename("/src/examples/actiononly/goal/simplerobot.ao");
			ActionOnlyMASBuilder builder = new ActionOnlyMASBuilder();
			MAS mas = builder.getMAS(abs_filename);
			GoalEnv env = new GoalEnv();
			mas.setEnv(env);
			env.init_before_adding_agents();
			
			ActionOnlyAgent ag = (ActionOnlyAgent) mas.getAg("robot");
			ag.reason();
		} catch (Exception e) {
			System.err.println(e);
			Assert.assertTrue(false);
		}
	}

}
