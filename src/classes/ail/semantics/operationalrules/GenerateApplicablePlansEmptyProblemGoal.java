// ----------------------------------------------------------------------------
// Copyright (C) 2008-2012 Louise A. Dennis, Berndt Farwer, Michael Fisher and 
// Rafael H. Bordini.
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
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//----------------------------------------------------------------------------

package ail.semantics.operationalrules;

import ail.semantics.AILAgent;
import ail.syntax.*;
import ail.tracing.events.GeneratePlansEvent;

import ajpf.util.AJPFLogger;
import gwendolen.project.ActionClass;
import gwendolen.project.RunPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * What do do if there are no applicable plans - note there is a problem with the goal
 * if this is a goal based intention, otherwise simply drop the intention.  
 * 
 * @author lad
 *
 */
public class GenerateApplicablePlansEmptyProblemGoal extends GenerateApplicablePlansEmpty {
	private static final String name = "Generate Applicable Plans Empty with Problem Goal";

	private static final String logname = "ail.semantics.operationalrules.GenerateApplicablePlansEmptyProblemGoal";

	/*
	 * (non-Javadoc)
	 * @see ail.semantics.operationalrules.OSRule#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see ail.semantics.operationalrules.GenerateApplicablePlansEmpty#apply(ail.semantics.AILAgent)
	 */
	public void apply(AILAgent a) {
		Intention I = a.getIntention();

		if (I != null && I.events().size() > 1 && I.hdE().referstoGoal() && I.hdE().isAddition()) {
			ArrayList<ApplicablePlan> Plp = new ArrayList<ApplicablePlan>();
			ArrayList<Deed> ds = new ArrayList<Deed>();
			ArrayList<Guard> gs = new ArrayList<Guard>();
			Event e = new Event(Event.AILDeletion, (Goal) I.hdE().getContent());

			ds.add(new Deed(Deed.Dnpy));
			gs.add(new Guard(new GBelief()));
			Plp.add(new ApplicablePlan(e, ds, gs, 0, I.hdU(), 0, AILAgent.AILdefaultPLname, a.getPrettyPrinter()));
			a.setApplicablePlans(Plp.iterator());

			AJPFLogger.warning(logname, "Warning no applicable plan for goal " + I.hdE().getContent());

			//System.out.println(a.getAgName() + " has no applicable plan for the goal" + I.hdE().getContent());

			recoveryOperation(I.hdE(), a);

			if (a.shouldTrace()) {
				a.trace(new GeneratePlansEvent(Plp, GeneratePlansEvent.NO_APPLICABLE_PLANS_FOR_GOAL, a.getIntention().getID()));
			}
		} else {
			super.apply(a);
		}
	}

	private boolean recoveryOperation(Event goal, AILAgent ag) {
		String goalString = goal.toString();
		ActionClass action = new ActionClass();
		int typeOfPlanning = action.typeOfPlanning;
		List<Plan> plans = ag.getPL().getPlans();
		List<Plan> filteredPlans = new ArrayList<>();

		for (Plan p : plans) {
			if (p.getTriggerEvent().toString().startsWith(goalString)) {
				filteredPlans.add(p);
			}
		}

		Plan planToFix = filteredPlans.get(0);

		if (planToFix.getContext() == null) {
			System.out.println("Plan context was null... Cannot recover context");
			return false;
		}

		List<String> contextString = new ArrayList<>();
		contextString.add(planToFix.getContext().toString());
		List<String> context = preProcessPredicates(contextString);

		List<String> beliefs = action.extractBeliefs(ag);
		List<String> goalStates = findPredicates(beliefs, context);

		// If Type of planning selected is invalid... Default to Online
		if (typeOfPlanning != 1 && typeOfPlanning != 2) {
			System.out.println("Invalid Type of Planning Selected... Defaulting to Online Planning");
			typeOfPlanning = 2;
		}

		// Offline Planning
		if (typeOfPlanning == 1) {
			List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 1);
			if(plan.isEmpty()){
				System.out.println("An error occured with the planner");
				System.out.println("To debug: Go to RunPlanner.java and print the output.");
				return false;
			}
			System.out.println(ag.getAgName()+" --> Context Not Fulfilled, Running Action --> "+plan);
			for (String act : plan) {
				boolean success = action.startAction(ag, act.toLowerCase());
				if (!success) {
					System.out.println("Recovery failure");}
			}
		}


		// Online
		if (typeOfPlanning==2) {
			// While all goalStates have not been achieved... Continue Recovery
			while (goalStates.size() != 0) {
				// Run the planner to find actions to fulfill the context
				List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 2);
				// Debugging information if plan produced by planner is empty...
				if(plan.isEmpty()){
					System.out.println("An error occured with the planner");
					System.out.println("To debug: Go to RunPlanner.java and print the output.");
					return false;
				}

				// If plan is not empty... Execute the action returned by the planner
				if(!plan.isEmpty()) {
					System.out.println(ag.getAgName()+" --> Context Not Fulfilled, Running Action --> "+plan.get(0));
					boolean success = action.startAction(ag, plan.get(0).toLowerCase()); // check output of bool
					if (!success) {System.out.println("Recovery failure");}
				}

				// Recheck Beliefs
				beliefs = action.extractBeliefs(ag);
				goalStates = findPredicates(beliefs, context); //goalstates just fancy word for relevant predicates
			}
		}

		super.apply(ag);

		return true;
	}

	private static List<String> findPredicates(List<String> beliefs, List<String> predicates) {
		List<String> missingPredicates = new ArrayList<>();
		for (String pred : predicates) {
			pred = pred.replaceAll("\\[source\\((self|percepts)\\)]", "");
			if (!beliefs.contains(pred)) {
				missingPredicates.add(pred);
			}
		}
		return missingPredicates;
	}

	private static List<String> preProcessPredicates (List<String> predicates) {
		List<String> formattedPredicates = new ArrayList<>();

		for (String predicate : predicates) {
			String withoutBrackets = predicate.replaceAll("[\\[\\]()]", "").trim();

			withoutBrackets= withoutBrackets.replaceAll("True", "");
			withoutBrackets= withoutBrackets.replaceAll(",", "");
			withoutBrackets= withoutBrackets.replaceAll(" ", "");

			if (withoutBrackets.contains("&")) {
				String[] parts = withoutBrackets.split("&");
				for (String part : parts) {formattedPredicates.add(part.trim());}
			} else{
				formattedPredicates.add(withoutBrackets);
			}
		}
		return formattedPredicates;
	}

}