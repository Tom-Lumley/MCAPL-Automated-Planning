// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis,  Michael Fisher, and Matt Webster
//
// This file is part of Gwendolen
//
// Gwendolen is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// Gwendolen is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with Gwendolen; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------


package gwendolen.project;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.ActionScheduler;
import ail.semantics.AILAgent;
import ail.syntax.*;
import ail.util.AILexception;

import java.util.List;



public class MyEnv extends DefaultEnvironment {
    /**
     * Constructor.
     *
     */
    public MyEnv() {
        super();
        ActionScheduler s = new ActionScheduler();
        setScheduler(s);
        addPerceptListener(s);
    }


    public Unifier executeAction(String agName, Action act) throws AILexception {
        Unifier u = new Unifier();
        // Gets all agents
        List<AILAgent> agentList = getAgents();
        AILAgent currentAg = null;

        // filters to currentAgent
        for (AILAgent ag : agentList) {
            if (ag.getAgName().equals(agName)) {
                currentAg = ag;
            }
        }

        assert currentAg != null;
        // Direct Planning Call
        if(act.getFunctor().equals("planning")) {
            Planning planning = new Planning();
            planning.execute(currentAg, act.getTerms());
        }
        // Execute the action
        else {
            ActionClass action = new ActionClass();
            action.startAction(currentAg, act.toString());
        }

        return u;
    }


}
