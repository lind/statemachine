package org.nextstate.statemachine;

public class FinalState extends SimpleState {
    public static final String FINAL_EVENT = "FinalEvent";

    public FinalState(String name) {
        super(name);
    }
}
