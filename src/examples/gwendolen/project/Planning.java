package gwendolen.project;

import ail.semantics.AILAgent;
import ail.syntax.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * The Planning class is responsible for executing planning tasks for an AILAgent.
 * It provides methods to execute planning based on different types of planning.
 */
public class Planning {

    /**
     * Executes planning based on the provided AILAgent and arguments.
     * @param ag The AILAgent for which planning is executed.
     * @param args The list of arguments representing the planning predicates.
     * @return true if planning execution is successful, false otherwise.
     */
    public boolean execute(AILAgent ag, List<Term> args) {
        ActionClass action = new ActionClass();
        int typeOfPlanning = action.typeOfPlanning;

        List<String> predicates = new ArrayList<>();
        List<String> beliefs = action.extractBeliefs(ag);

        for (Term pred : args){predicates.add(pred.toString());}
        List<String>goalStates = findPredicates(beliefs, predicates);

        // If Type of planning selected is invalid... Default to Online
        if (typeOfPlanning != 1 && typeOfPlanning != 2) {
            System.out.println("Invalid Type of Planning Selected... Defaulting to Online Planning");
            typeOfPlanning = 2;
        }

        if (typeOfPlanning==1) {
            List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 1);

            if(plan.isEmpty()){
                System.out.println("An error occured with the planner");
                System.out.println("To debug: Go to RunPlanner.java and print the output.");
                return false;
            }

            // Log recovery and planner information
            System.out.println(ag.getAgName()+" --> Direct Planning Call --> Running Action --> "+plan);

            for (String act : plan) {
                boolean success = action.startAction(ag, act.toLowerCase());
                if (success) {return true;} else {return false;}
            }
        }
        if (typeOfPlanning ==2) {
            // While goalStates are not fulfilled...
            while (goalStates.size() != 0) {
                // Prepare a list for the planner with the provided argument
                List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 2);

                // Debugging information if plan produced by planner is empty...
                if (plan.isEmpty()) {
                    System.out.println("An error occured with the planner");
                    System.out.println("To debug: Go to RunPlanner.java and print the output.");
                    return false;
                }
                System.out.println(ag.getAgName()+" --> Direct Planning Call --> Running Action --> "+plan);

                // Execute the actions returned by the planner
                boolean success = action.startAction(ag, plan.get(0).toLowerCase());
                if (!success) {
                    System.out.println("Recovery failure");
                }

                beliefs = action.extractBeliefs(ag);
                goalStates = findPredicates(beliefs, predicates);
            }
        }
        return true;
    }

    /**
     * Finds the predicates that are missing in the provided beliefs.
     * @param beliefs The list of beliefs.
     * @param predicates The list of predicates to check.
     * @return A list of predicates that are missing in the beliefs.
     */
    private static List<String> findPredicates(List<String> beliefs, List<String> predicates) {
        List<String> missingPredicates = new ArrayList<>();
        for (String pred : predicates) {
            if (!beliefs.contains(pred)) {missingPredicates.add(pred);}
        }
        return missingPredicates;
    }

}
