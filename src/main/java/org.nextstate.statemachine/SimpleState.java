package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleState implements State {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Optional<Action> entry = Optional.empty();
    protected Optional<String> entryActionName = Optional.empty();
    protected Optional<Action> exit = Optional.empty();
    protected Optional<String> exitActionName = Optional.empty();

    protected final String name;
    protected final List<Transition> transitions = new ArrayList<>();

    public SimpleState(String name) {
        this.name = name;
    }

    @Override public Optional<Action> getEntry() {
        return entry;
    }

    @Override public Optional<String> getEntryActionName() {
        return entryActionName;
    }

    public String getName() {
        return name;
    }

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    public void addTransitions(List<Transition> transitionList) {
        this.transitions.addAll(transitionList);
    }

    @Override public void onEntry() {
        entry.ifPresent(Action::perform);
    }

    @Override public void onExit() {
        exit.ifPresent(Action::perform);
    }

    @Override public Optional<State> execute(String event) {
        return stateTransition(event);
    }

    public Optional<State> stateTransition(String event) {

        Optional<Transition> matchedTransition = transitions.stream()
                .filter(t -> t.guardEvent == null || t.guardEvent.equals(event))
                .findFirst();

        log.debug("execute - {} event: {} ", name, event, (matchedTransition.isPresent() ?
                " transition to state: " + matchedTransition.get().getTargetState().getName() :
                " no transition match."));

        if (!matchedTransition.isPresent()) {
            log.debug("execute - No transition match event: {} in state: {}", event, name);
            return Optional.empty();
        }

        Transition transition = matchedTransition.get();
        transition.onTransition.ifPresent(Action::perform);
        return Optional.ofNullable(transition.getTargetState());
    }

    public boolean transitionToFinalState() {
        Optional<Transition> transition = transitions.stream().filter(
                t -> t.guardEvent.equals(FinalState.FINAL_EVENT)).findFirst();

        return transition.isPresent() && (transition.get().getTargetState() instanceof FinalState);
    }

    @Override public void toDot(StringBuilder sb) {

        for (Transition t : transitions) {
            sb.append(name.replaceAll("\\s+", "_"));
            sb.append(" -> ");
            sb.append(t.getTargetState().getName().replaceAll("\\s+", "_"));
            if (t.getName() != null) {
                sb.append(" [label=\"");
                sb.append(t.getName());
                sb.append("\"];");
            }
            sb.append(System.lineSeparator());
        }
    }

    // =================
    //      Builder
    // =================
    public static StateBuilder state(String name) {
        return new StateBuilder(name);
    }

    public static class StateBuilder {
        final SimpleState state;

        Transition.TransitionBuilder<StateBuilder> transitionBuilder;

        public StateBuilder(String name) {
            this.state = new SimpleState(name);
        }

        public Transition.TransitionBuilder<StateBuilder> transition(String name) {
            // Add previous transition
            if (transitionBuilder != null) {
                state.addTransition(transitionBuilder.build());
            }
            transitionBuilder = Transition.transition(this, name);
            return transitionBuilder;
        }

        public StateBuilder onEntry(Action action) {
            state.entry = Optional.ofNullable(action);
            return this;
        }

        public StateBuilder onEntryActionName(String entryActionName) {
            state.entryActionName = Optional.ofNullable(entryActionName);
            return this;
        }

        public StateBuilder onEntry(Action action, String entryActionName) {
            state.entry = Optional.ofNullable(action);
            state.entryActionName = Optional.ofNullable(entryActionName);
            return this;
        }

        public StateBuilder onExit(Action action) {
            state.exit = Optional.ofNullable(action);
            return this;
        }

        public SimpleState build() {
            // Add current transition on build
            if (transitionBuilder != null) {
                state.addTransition(transitionBuilder.build());
            }
            return state;
        }
    }
}
