package gwendolen.project;

import ail.semantics.AILAgent;
import ail.syntax.BeliefBase;
import ail.syntax.Literal;
import ail.syntax.PredicateTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents selection, execution and recovering an action performed by an agent.
 */
public class ActionClass {
    // Initialise Key Variables
    BeliefBase bb;
    boolean recoveryRequired;
    List<Literal> predicate;
    List<Literal> beliefsToAdd;
    List<Literal> beliefsToDelete;

    /**
     * Choice of Planner: Offline (1) or Online (2)
     */
    public int typeOfPlanning = 2;

    // Logger for normal logging
    private Logger logger = Logger.getLogger("t1."+"Env");
    // Logger for recovery logging
    private Logger recoveryLogger = Logger.getLogger("t1."+"Recovery");
    Literal hasMoneyLiteral = new Literal("hasMoney");
    Literal hasPhoneLiteral = new Literal("hasPhone");
    Literal parentsHappyLiteral = new Literal("parentsHappy");
    Literal onPhoneLiteral = new Literal("onPhone");
    Literal messageSentLiteral = new Literal("messageSent");
    Literal motivatedLiteral = new Literal("motivated");
    Literal hasCarLiteral = new Literal("hasCar");
    Literal hungryLiteral = new Literal("hungry");
    Literal atGymLiteral = new Literal("atGym");
    Literal happyLiteral = new Literal("happy");
    Literal inCarLiteral = new Literal("inCar");
    Literal atHomeLiteral = new Literal("atHome");
    Literal bossHappyLiteral = new Literal("bossHappy");
    Literal tiredLiteral = new Literal("tired");
    Literal atWorkLiteral = new Literal("atWork");

    /**
     * Selects and executes the corresponding action.
     * @param ag The agent performing the action.
     * @param action The name of the action to execute.
     * @return True if the action was executed successfully, otherwise false.
     */
    public boolean startAction(AILAgent ag, String action) {
        predicate = new ArrayList<>();
        beliefsToAdd = new ArrayList<>();
        beliefsToDelete = new ArrayList<>();

        System.out.println("["+ag.getAgName()+"]"+" executing: "+action);

        if (action.equals("buyphone")) {
            boolean success = buyPhone(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("dochores")) {
            boolean success = doChores(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("earnsalary")) {
            boolean success = earnSalary(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("usephone")) {
            boolean success = usePhone(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("textfriend")) {
            boolean success = textFriend(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("gooffphone")) {
            boolean success = goOffPhone(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("gotogym")) {
            boolean success = goToGym(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("getincar")) {
            boolean success = getInCar(ag, action);
            if(success) {return true;} else {return false;}
        }

        if (action.equals("gotowork")) {
            boolean success = goToWork(ag, action);
            if(success) {return true;} else {return false;}
        }

        System.out.println("Action not yet Implemented!");

        return false;
    }

    /**
     * Runs the selected action and handles predicate checks, belief additions, deletions, and recovery operations.
     * @param ag The agent performing the action.
     * @param action The name of the action to execute.
     * @param predicate The predicate associated with the action.
     * @param beliefsToAdd The beliefs to add after executing the action.
     * @param beliefsToDelete The beliefs to delete after executing the action.
     * @return True if the action was executed successfully, otherwise false.
     */
    public Boolean runAction(AILAgent ag, String action, List<Literal> predicate, List<Literal> beliefsToAdd, List<Literal> beliefsToDelete) { // Could make the literals lists
        bb = ag.getBB();
        recoveryRequired = false;

        // No Predicate... add/del beliefs
        if (predicate == null) {
                if(beliefsToAdd != null) {for (Literal belief : beliefsToAdd) {ag.addBel(belief, AILAgent.refertoself());}}
                if(beliefsToDelete != null) {for (Literal belief : beliefsToDelete) {ag.delBel(belief);}}
                return true; // Ran action successfully
        }

        // Checks if all predicates are in ag beliefbase
        boolean allBelsPresent;
        allBelsPresent = allBeliefsPresent(bb, predicate);

        // If all predicates in bb add/del bels...
        if (allBelsPresent) { // Pre-Condition
                if(beliefsToAdd != null) {for (Literal belief : beliefsToAdd) {ag.addBel(belief, AILAgent.refertoself());}}
                if(beliefsToDelete != null) {for (Literal belief : beliefsToDelete) {ag.delBel(belief);}}
                return true; // Ran action successfully
        } else {
            recoveryRequired = true; // if all predicates not present... recovery required...
        }

        if(recoveryRequired) { // Extract Knowledge for Failure Recovery...
            return recoveryOperation(ag, action, predicate);
        }

        return false; // failed to runAction successfully
    }

    /**
     * Performs a recovery operation for the agent based on the given action and predicates.
     *
     * @param ag The agent for which the recovery operation is performed.
     * @param action The action to be executed during the recovery operation.
     * @param predicates The list of predicates representing the desired state to recover.
     * @return True if the recovery operation succeeds, otherwise false.
     */
    private boolean recoveryOperation(AILAgent ag, String action, List<Literal> predicates) {
        // Extract Beliefs, Find which predicates are not in bb...
        List<String> beliefs = extractBeliefs(ag);
        List<String> goalStates = findPredicates(beliefs, predicates);


        // If Type of planning selected is invalid... Default to Online
        if (typeOfPlanning != 1 && typeOfPlanning != 2) {
            recoveryLogger.info("Invalid Type of Planning Selected... Defaulting to Online Planning");
            typeOfPlanning = 2;
        }

        // Offline Planning
        if (typeOfPlanning == 1) {
            List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 1);
            assert plan != null;
            if(plan.isEmpty()){
                System.out.println("An error occurred with the planner");
                System.out.println("To debug: Go to RunPlanner.java and print the output.");
                return false;
            }
            System.out.println(ag.getAgName() + " --> Action Predicate Failure --> Running Action --> " + plan);
            for (String act : plan) {
                boolean success = startAction(ag, act.toLowerCase());
                if (!success) {recoveryLogger.info("Recovery failure");}
            }
        }
        // Online Planning
        if (typeOfPlanning==2) {
            while (goalStates.size() != 0) {
                List<String> plan = RunPlanner.run(ag.getAgName(), beliefs, goalStates, 2);
                assert plan != null;
                if (plan.isEmpty()) {
                    System.out.println("An error occured with the planner");
                    System.out.println("To debug: Go to RunPlanner.java and print the output.");
                    return false;
                }
                System.out.println(ag.getAgName() + " --> Action Predicate Failure --> Running Action --> " + plan.get(0));

                boolean success = startAction(ag, plan.get(0).toLowerCase()); //execute action .get(0) as simulating online using FF
                if (!success) {
                    recoveryLogger.info("Recovery failure");
                }
                beliefs = extractBeliefs(ag); //Check env again
                goalStates = findPredicates(beliefs, predicates); //Recheck
            }
        }
        // Execute Original Action
        boolean success = startAction(ag, action.toLowerCase());
        if (success) {return true;}else {return false;}
    }

    /**
     * Checks which predicates are present in the belief base.
     * @param beliefs The list of beliefs.
     * @param predicates The list of predicates to check.
     * @return The list of predicates that are missing from the belief base.
     */
    private static List<String> findPredicates(List<String> beliefs, List<Literal> predicates) {
        List<String> missingPredicates = new ArrayList<>();
        for (Literal pred : predicates) {
            String tempPred = pred.toString();
            tempPred = pred.toString().replaceAll("\\[source\\((self|percepts)\\)]", "");
            if (!beliefs.contains(tempPred)) {
                missingPredicates.add(tempPred);
            }
        }
        return missingPredicates;
    }
    /**
     * Checks if all predicates are present in the belief base.
     * @param beliefBase The belief base to check.
     * @param predicate The list of predicates to check.
     * @return True if all predicates are present, otherwise false.
     */
    private boolean allBeliefsPresent(BeliefBase beliefBase, List<Literal> predicate) {
        for (Literal belief : predicate) {
            if (beliefBase.contains(belief) == null) {
                return false; // If any belief is not present, return false immediately
            }
        }
        return true; // If all beliefs are present, return true
    }

    /**
     * Extracts beliefs from the agent's belief base, filtering out KQML beliefs and irrelevant sources.
     * @param ag The agent.
     * @return The list of filtered beliefs.
     */
    public List<String> extractBeliefs(AILAgent ag) {
        List<String> filteredBeliefs = new ArrayList<>();
        for (PredicateTerm b : ag.getBB()) {
            String beliefString = b.toString();
            beliefString = beliefString.replace("[source(self)]", "").replace("[source(percepts)]", "");
            filteredBeliefs.add(beliefString.trim());
        }
        return filteredBeliefs;
    }

    private boolean doChores(AILAgent ag, String action) {
        beliefsToAdd.add(hasMoneyLiteral);
        beliefsToAdd.add(parentsHappyLiteral);

        boolean success = runAction(ag, action, null, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }
    private boolean buyPhone(AILAgent ag, String action) {
        predicate.add(hasMoneyLiteral);
        beliefsToAdd.add(hasPhoneLiteral);
        beliefsToDelete.add(hasMoneyLiteral);

        boolean success = runAction(ag, action, predicate, beliefsToAdd, beliefsToDelete);
        if(success) {return true;} else {return false;}
    }

    // Action 3
    public boolean earnSalary(AILAgent ag, String action) {
        beliefsToAdd.add(hasMoneyLiteral);

        boolean success = runAction(ag, action, null, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }

    // Action 4
    public boolean textFriend(AILAgent ag, String action) {
        predicate.add(onPhoneLiteral);
        predicate.add(hasPhoneLiteral);
        beliefsToAdd.add(messageSentLiteral);

        boolean success = runAction(ag, action, predicate, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }

    // Action 5
    public boolean usePhone(AILAgent ag, String action) {
        predicate.add(hasPhoneLiteral);
        beliefsToAdd.add(onPhoneLiteral);

        boolean success = runAction(ag, action, predicate, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }

    // Action 6
    private boolean goOffPhone(AILAgent ag, String action) {
        predicate.add(hasPhoneLiteral);
        predicate.add(onPhoneLiteral);

        beliefsToDelete.add(onPhoneLiteral);
        boolean success = runAction(ag, action, predicate, null, beliefsToDelete);
        if(success) {return true;} else {return false;}
    }
    // Action 7
    private boolean goToGym(AILAgent ag, String action) {
        predicate.add(motivatedLiteral);
        predicate.add(inCarLiteral);

        beliefsToAdd.add(atGymLiteral);
        beliefsToAdd.add(hungryLiteral);
        beliefsToAdd.add(happyLiteral);

        boolean success = runAction(ag, action, predicate, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }
    // Action 8
    private boolean getInCar(AILAgent  ag, String action) {
        predicate.add(hasCarLiteral);

        beliefsToAdd.add(inCarLiteral);
        beliefsToDelete.add(atHomeLiteral);

        boolean success = runAction(ag, action, predicate, beliefsToAdd, beliefsToDelete);
        if(success) {return true;} else {return false;}
    }
    // Action 9
    private boolean goToWork(AILAgent ag, String action) {
        predicate.add(inCarLiteral);

        beliefsToAdd.add(atWorkLiteral);
        beliefsToAdd.add(tiredLiteral);
        beliefsToAdd.add(bossHappyLiteral);

        Boolean success = runAction(ag, action, predicate, beliefsToAdd, null);
        if(success) {return true;} else {return false;}
    }

}
