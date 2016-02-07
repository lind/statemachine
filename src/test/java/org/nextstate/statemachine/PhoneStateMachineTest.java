package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.PhoneStateMachine.*;

import org.junit.Test;

/**
 * Tests using the state machine defined in {@link PhoneStateMachine}
 */
@SuppressWarnings("checkstyle:methodname")
public class PhoneStateMachineTest {

    @Test
    public void one_transition() {
        // Given
        StateMachine phone = new PhoneStateMachine();

        // When
        phone.execute(CALL_DIALED);

        // Then
        assertThat(phone.getActiveStateConfiguration()).isEqualTo(RINGING);
    }

    @Test
    public void no_transition() {
        StateMachine phone = new PhoneStateMachine();
        phone.execute(HUNG_UP);
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void to_state_phone_destroyed() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(CALL_DIALED);
        phone.execute(CALL_CONNECTED);
        phone.execute(PLACED_ON_HOLD);
        phone.execute(PHONE_HURLED_AGAINST_WALL);

        assertThat(phone.getActiveStateName()).isEqualTo(PHONE_DESTROYED);
    }

    @Test
    public void transition_to_same_state() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(CALL_DIALED);
        phone.execute(HUNG_UP);

        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void start_with_ringing_active_state() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(RINGING);

        phone.execute(CALL_CONNECTED);

        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);
    }

    @Test
    public void through_all_states_using_all_transitions() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(CALL_DIALED);
        phone.execute(HUNG_UP);
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(CALL_DIALED);
        phone.execute(CALL_CONNECTED);
        phone.execute(HUNG_UP);
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(CALL_DIALED);
        phone.execute(CALL_CONNECTED);
        phone.execute(MESSAGE_LEFT);
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(CALL_DIALED);
        phone.execute(CALL_CONNECTED);
        phone.execute(HUNG_UP);
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(CALL_DIALED);
        phone.execute(CALL_CONNECTED);
        phone.execute(PLACED_ON_HOLD);
        phone.execute(PHONE_HURLED_AGAINST_WALL);

        assertThat(phone.getActiveStateName()).isEqualTo(PHONE_DESTROYED);
    }

    @Test
    public void transitions_from_on__hold_state() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(ON_HOLD);

        phone.execute(TOOK_OFF_HOLD);
        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);

        phone.activeStateConfiguration(ON_HOLD);
        phone.execute(HUNG_UP);

        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void to_dot() {
        StateMachine phone = new PhoneStateMachine();

        System.out.println(phone.toDot(false));
    }

}
