# Empowering BDI Agents with Automated Planning for Failure Recovery

This project focuses on empowering BDI (Belief-Desire-Intention) agents with automated planning for failure recovery in MCAPL and specifically Gwendolen, a multi-agent system platform. The goal is to enable agents to establish action or plan predicates to overcome action, plan, and goal failures efficiently.

## Table of Contents

- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
- [Contact](#contact)

## Features

- Integration of automated planning techniques into BDI agents.
- Failure detection and recovery mechanisms.
- Ability for agents to re-establish predicates once failure has occurred using automated planning.
- Support for handling action, plan, and goal failures seamlessly.

## Setup

### Project Location
All files for project can be found:
`mcapl/src/examples/gwendolen/project`

### Adding an Action

This can be done in the `ActionClass.java` class, following a similar example to what has been done previously, adding to the three lists then calling `runAction()`.

1. Select action in `startAction()`.
2. Create a linking function, e.g., `buyPhone()`.
3. Add to the three lists: predicates, beliefs to add, and beliefs to delete.
4. Pass this to `runAction` function.


### Selecting Planner
Within ActionClass.java change this variable 1=Offline, 2=Online:
```
public int typeOfPlanning = 2;
```
### Configuring Offline Planner

We are utilizing Fast Forward (FF) as the planner of choice. You must ensure that FF is reachable by this code, either by using an absolute file path or placing FF in the correct place. Tip: Change command to pwd to see where you are at currently.

```java
String command = "./ff -o domain.pddl -f " + agName + "problem.pddl";
```

Give FF permissions:

```bash
chmod +x ff
```

### Configuring Online Planner

We are utilizing a customised version of PDDL4J as the planner of choice. RunPlanner.java contains some options for customisation, also see https://github.com/ramonpereira/PDDL4J-Planning


### Debugging FF (offline)

Within `RunPlanner.java`, the planner will produce "output", just before "return plan" is executed do:

```java
System.out.println(output);
```

Ensure any variables used in `.asl` or elsewhere are established in `domain.pddl`.

### Configuring .gwen

Due to the way FF works, all actions must be fully in lowercase to avoid parsing issues. e.g.:

```prolog
+!start : true <- gotowork; textfriend; gotogym.
```

### Integrating a Different Planner

If wishing to change the planner, focus your attention on the following files and functions:

- `"command"` in `RunPlanner.java`
- `"extractSteps()"` in `RunPlanner.java`
- `"preProcessPredicates()"`, multiple files
- Any other parsing or string manipulation, multiple files

### Compatability
Currently, the plan's context has to be quite basic however this can be extended by modifying the parsing operations throughout.

### Key Locations

- Action Predicate Recovery = Recovery Operation in `ActionClass.java`
- No Applicable Plan Recovery = Recovery Operation `src/classes/ail/semantics/operationalrules/GenerateApplicablePlansEmptyProblemGoal.java`
- Direct Plannning Call = `Planning.java`
- Execution of Planner = `RunPlanner.java`

### Suggested Installs

These additional tools or packages may be necessary for the project to run smoothly:

- Java Development Kit (JDK): Required for running Java applications. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
- Setting Java Home and relevant paths.

## Usage

### Option 1: Using run-AIL

To use this project, follow these steps:

![Alt text](Capture111.png?raw=true "Usage")

For questions, suggestions, or feedback regarding this project, feel free to reach out to the project maintainers:

Developer:
- Tom Lumley (c1008462@newcastle.ac.uk)

Tutor:
- Mengwei Xu (mengwei.xu@newcastle.ac.uk)

Adapted PDDL4J Developer:
- Ramon Pereira (ramon.fragapereira@manchester.ac.uk)

Also:
- Felipe Meneguzzi (felipe.meneguzzi@abdn.ac.uk)

---
