package org.nextstate.statemachine;

import java.util.Optional;

public interface State {

    String getName();

    boolean transitionToFinalState();

    Optional<State> execute(String event);

    void onEntry();

    Optional<Action> getEntry();

    Optional<String> getEntryActionName();

    void onExit();

    void toDot(StringBuilder sb);
}
