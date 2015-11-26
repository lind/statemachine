package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.nextstate.statemachine.SimpleState.state;

import java.util.Arrays;

import org.junit.Test;

public class StateMachineTest {

    private static final String A_SIMPLE_STATE = "ASimpleState";
    private static final String A_SIMPLE_STATE_2 = "ASimpleState2";
    private static final String FINAL = "Final";
    private static final String FINAL_STATE = "FinalState";
    private static final String A_SIMPLE_EVENT = "ASimpleEvent";
    private static final String A_SIMPLE_ACTION = "ASimpleAction";

    @Test
    public void no_active_state() {
        StateMachine stateMachine = new NoActiveStateMachine();

        try {
            stateMachine.execute(A_SIMPLE_EVENT);

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("No active state");
        }
    }

    @Test
    public void load_simple_state() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(A_SIMPLE_EVENT);

        assertThat(stateMachine.getActiveStateConfiguration()).isEqualTo(A_SIMPLE_STATE_2);
    }

    @Test
    public void to_fina() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(A_SIMPLE_EVENT);
        stateMachine.execute(FinalState.FINAL_EVENT);

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(FINAL_STATE);
    }

    // ---- Helper classes for testing ----
    private class NoActiveStateMachine extends StateMachine {
    }

    // ASimpleState -> ASimpleState2 -> FinalState
    private class ASimpleStateMachine extends StateMachine {
        {
            State finalState = new FinalState(FINAL_STATE);

            State aSimpleState2 = state(A_SIMPLE_STATE_2)
                    .transition(FINAL).guardedBy(FinalState.FINAL_EVENT)
                    .to(finalState).build();

            State aSimpleState = state(A_SIMPLE_STATE)
                    .transition(A_SIMPLE_ACTION).guardedBy(A_SIMPLE_EVENT)
                    .to(aSimpleState2)
                    .build();

            addStates(Arrays.asList(aSimpleState, aSimpleState2, finalState));
            activeState(aSimpleState);
            validate();
        }
    }
}
