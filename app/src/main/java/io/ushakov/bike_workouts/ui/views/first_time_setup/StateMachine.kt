package io.ushakov.bike_workouts.ui.views.first_time_setup

import com.tinder.StateMachine

sealed class State {
    object Name : State()
    object Measurements : State()
    object Permissions : State()
    object NoPermissions : State()
    object Finish : State()
}

sealed class Event {
    object OnNameSubmitted : Event()
    object OnBack : Event()
    object OnMeasurementsSubmitted : Event()
    object OnPermissionsConfirmed : Event()
    object OnPermissionsDenied : Event()
}

sealed class SideEffect {
    object NameSubmitted : SideEffect()
    object MeasurementsSubmitted : SideEffect()
}

fun createStateMachine(initialState: State) = StateMachine.create<State, Event, SideEffect> {
    initialState(initialState)

    state<State.Name> {
        on<Event.OnNameSubmitted> {
            transitionTo(State.Measurements, SideEffect.NameSubmitted)
        }
    }
    state<State.Measurements> {
        on<Event.OnMeasurementsSubmitted> {
            transitionTo(State.Permissions, SideEffect.MeasurementsSubmitted)
        }
        on<Event.OnBack> {
            transitionTo(State.Name)
        }
    }
    state<State.Permissions> {
        on<Event.OnPermissionsConfirmed> {
            transitionTo(State.Finish)
        }
        on<Event.OnBack> {
            transitionTo(State.Measurements)
        }
        on<Event.OnPermissionsDenied> {
            transitionTo(State.NoPermissions)
        }
    }
    state<State.Finish> { }
//    onTransition {
//        val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
//        when (validTransition.sideEffect) {
//            SideEffect.LogMelted -> logger.log(ON_MELTED_MESSAGE)
//            SideEffect.LogFrozen -> logger.log(ON_FROZEN_MESSAGE)
//            SideEffect.LogVaporized -> logger.log(ON_VAPORIZED_MESSAGE)
//            SideEffect.LogCondensed -> logger.log(ON_CONDENSED_MESSAGE)
//        }
//    }
}