package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateMachine - Subclass and use the builder in the constructor. See the unit tests for examples.
 * <br>
 * To state should not have the same name. If to states have the same name it is not deterministic which one is chosen
 * when the active stave is loaded.
 */
public class StateMachine {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private State activeState;
    private final List<State> states = new ArrayList<>();

    protected void addStates(List<State> states) {
        this.states.addAll(states);
    }

    protected void activeState(State state) {
        this.activeState = state;
        this.activeState.onEntry();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getActiveStateName() {
        return activeState.getName();
    }

    public List<State> getStates() {
        return states;
    }

    public State getActiveState() {
        return activeState;
    }

    public void validate() {
        if (activeState == null) {
            throw new IllegalStateException("No active state");
        }
    }

    public String getActiveStateConfiguration() {
        return activeState.getName();
    }

    public void activeStateConfiguration(String stateName) {

        Optional<State> state = states.stream().filter(s -> stateName.equals(s.getName())).findFirst();
        state.orElseThrow(() -> new IllegalStateException("No state named " + stateName
                + " exists. Add all states to the StateMachine before setting active state configuration."));

        activeState = state.get();
        log.debug("activeStateConfiguration - active state: {}", activeState.getName());
    }

    public void execute(String event) {
        if (activeState == null) {
            throw new IllegalStateException("No active state");
        }
        log.debug("execute - activeState: " + activeState.getName() + " event: " + event);

        Optional<State> state = activeState.execute(event);

        // Check if new active state and execute onExit on the old and onEntry on the new ...
        if (state.isPresent()) {
            activeState.onExit();
            activeState = state.get();
            activeState.onEntry();
            log.debug("execute - new active state: {}", activeState.getName());
        }
    }

    /**
     * DOT graph description language for the State Machine.
     * Composite states is filled with grey but internal states is not included. Should be possible at least for one
     * level with DOT attrs compound:
     * See: http://www.graphviz.org/content/attrs#dcompound and
     * http://stackoverflow.com/questions/2012036/graphviz-how-to-connect-subgraphs
     *
     * @return sting describing the state machine graph
     */
    public String toDot(boolean showActiveState) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph ");
        sb.append(getName().replaceAll("\\s+", "_"));
        sb.append(" { ");
        sb.append(System.lineSeparator());
        sb.append(getActiveState().getName().replaceAll("\\s+", "_"));
        sb.append("[label=\"");
        sb.append(getActiveState().getName());
        // Add entry action to state
        if (getActiveState().getEntry().isPresent() && getActiveState().getEntryActionName().isPresent()) {
            sb.append("\\nEntry:");
            sb.append(getActiveState().getEntryActionName().get());
        }
        sb.append("\"");
        if (showActiveState) {
            sb.append(", style=filled, fillcolor=lightblue");
        }
        sb.append("];");
        sb.append(System.lineSeparator());

        getActiveState().toDot(sb);

        getStates().stream().filter(state -> state != getActiveState()).forEach(state -> {
            sb.append(state.getName().replaceAll("\\s+", "_"));
            sb.append("[label=\"");
            sb.append(state.getName());
            // Add entry action to state
            if (state.getEntry().isPresent() && state.getEntryActionName().isPresent()) {
                sb.append("\\nEntry:");
                sb.append(state.getEntryActionName().get());
            }
            sb.append("\"");
            sb.append("];");
            sb.append(System.lineSeparator());
            state.toDot(sb);
        });
        sb.append("} ");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

}
