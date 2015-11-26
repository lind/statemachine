package org.nextstate.statemachine;

import static org.nextstate.statemachine.SimpleState.state;
import static org.nextstate.statemachine.Transition.transitions;

import java.util.Arrays;

/**
 * StateMachine for a phone inspired by: http://simplestatemachine.codeplex.com/
 * <p>
 * States and transitions:
 * <p>
 * state @OffHook:
 * when @CallDial  >> @Ringing
 * <p>
 * state @Ringing:
 * when @HangUp  >> @OffHook
 * when @ConnectCall  >> @Connected
 * <p>
 * state @Connected:
 * when @LeaveMessage  >> @OffHook
 * when @HangUp   >> @OffHook
 * when @PlaceOnHold  >> @OnHold
 * <p>
 * state @OnHold:
 * when @TakeOffHold >> @Connected
 * when @HangUp  >> @OffHook
 * when @HurlPhone >> @PhoneDestroyed
 * state @PhoneDestroyed
 * <p>
 * Events:
 * <p>
 * CallDial trigger @CallDialed
 * HangUp trigger @HungUp
 * ConnectCall trigger @CallConnected
 * LeaveMessage trigger @MessageLeft
 * PlaceOnHold trigger @PlacedOnHold: on_event @PlayMuzak
 * TakeOffHold trigger @TakenOffHold: on_event @StopMuzak
 * HurlPhone trigger @PhoneHurledAgainstWall
 */
class PhoneStateMachine extends StateMachine {
    // State names
    public static final String OFF_HOOK = "OffHook";
    public static final String RINGING = "Ringing";
    public static final String CONNECTED = "Connected";
    public static final String ON_HOLD = "OnHold";
    public static final String PHONE_DESTROYED = "PhoneDestroyed";

    // Event names
    public static final String CALL_DIALED = "CallDialed";
    public static final String HUNG_UP = "HungUp";
    public static final String CALL_CONNECTED = "CallConnected";
    public static final String MESSAGE_LEFT = "MessageLeft";
    public static final String PLACED_ON_HOLD = "PlacedOnHold";
    public static final String TOOK_OFF_HOLD = "TookOffHold";
    public static final String PHONE_HURLED_AGAINST_WALL = "PhoneHurledAgainstWall";

    // Name of transitions - (Commands resulting in triggering events)
    private static final String CALL_DIAL = "CallDial";
    private static final String HANG_UP = "HangUp";
    private static final String CONNECT_CALL = "ConnectCall";
    private static final String LEAVE_MESSAGE = "LeaveMessage";
    private static final String PLACE_ON_HOLD = "PlaceOnHold";
    private static final String TAKE_OFF_HOLD = "TakeOffHold";
    private static final String HURL_PHONE = "HurlPhone";

    {
        Action playMuzak = () -> System.out.println("PlayMuzak");
        Action stopMuzak = () -> System.out.println("StopMuzak");

        SimpleState offHook = state(OFF_HOOK)
                .onEntry(() -> System.out.println("In Sysout Action!")).build();
        State phoneDestroyed = state(PHONE_DESTROYED).build();
        SimpleState connected = state(CONNECTED).build();
        State onHold = state(ON_HOLD)
                .transition(HURL_PHONE).guardedBy(PHONE_HURLED_AGAINST_WALL)
                .to(phoneDestroyed)
                .transition(HANG_UP).guardedBy(HUNG_UP)
                .to(offHook)
                .transition(TAKE_OFF_HOLD).onTransition(stopMuzak)
                .guardedBy(TOOK_OFF_HOLD)
                .to(connected)
                .build();
        connected.addTransitions(transitions()
                .transition(PLACE_ON_HOLD)
                .onTransition(playMuzak).guardedBy(PLACED_ON_HOLD)
                .to(onHold)
                .transition(HANG_UP).guardedBy(HUNG_UP)
                .to(offHook)
                .transition(LEAVE_MESSAGE).guardedBy(MESSAGE_LEFT)
                .to(offHook)
                .build());
        State ringing = state(RINGING)
                .transition(CONNECT_CALL).guardedBy(CALL_CONNECTED)
                .to(connected)
                .transition(HANG_UP).guardedBy(HUNG_UP)
                .to(offHook)
                .build();
        offHook.addTransitions(transitions()
                .transition(CALL_DIAL).guardedBy(CALL_DIALED)
                .to(ringing).build());
        addStates(Arrays.asList(offHook, phoneDestroyed, onHold, connected, ringing));
        activeState(offHook);
        validate();
    }
}
