package io.ushakov.bike_workouts.ui.views.first_time_setup.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.Constants.MAXIMUM_AGE
import io.ushakov.bike_workouts.util.Constants.MAXIMUM_WEIGHT
import io.ushakov.bike_workouts.util.Constants.MINIMUM_AGE
import io.ushakov.bike_workouts.util.Constants.MINIMUM_WEIGHT


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Measurements(
    name: String,
    initialAge: Int?,
    initialWeight: Int?,
    onMeasurementsSubmit: (age: Int, weight: Int) -> Unit,
) {
    var ageInputValue by remember { mutableStateOf(initialAge?.toString() ?: "") }
    var weightInputValue by remember {
        mutableStateOf(initialWeight?.toString() ?: "")
    }

    val (isAgeValid, ageInputError) = rememberIsAgeValid(ageInputValue)
    val (isWeightValid, weightInputError) = rememberIsWeightValid(weightInputValue)



    fun submitValues() {
        val age = ageInputValue.toIntOrNull()
        val weight = weightInputValue.toIntOrNull()

        if (age == null || weight == null || !isAgeValid || !isWeightValid) return

        onMeasurementsSubmit(age, weight)
    }

    Surface {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier
                .weight(1f)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()) {
                Text(
                    text = "Hi, $name 🙌",
                    style = Typography.h2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(Modifier
                .weight(1f)
                .padding(horizontal = 30.dp)
                .padding(bottom = 30.dp)
            ) {
                Text(
                    text = "App needs your body attributes to calculate calories correctly ",
                    style = Typography.body1,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    val focusManager = LocalFocusManager.current

                    val (first, second) = FocusRequester.createRefs()

                    NumberInputFieldWithError(
                        modifier = Modifier
                            .weight(1f),
                        inputModifier = Modifier.focusOrder(first) { next = second },
                        label = "Age",
                        value = ageInputValue,
                        errorMessage = ageInputError,
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        onChange = { ageInputValue = it },
                        trailingText = "y.o"
                    )
                    Spacer(Modifier.width(24.dp))
                    NumberInputFieldWithError(
                        modifier = Modifier
                            .weight(1f),
                        inputModifier = Modifier.focusOrder(second) { previous = first },
                        label = "Weight",
                        value = weightInputValue,
                        errorMessage = weightInputError,
                        onNext = { submitValues() },
                        onChange = { weightInputValue = it },
                        trailingText = "kg"
                    )
                }

                Spacer(modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 16.dp))

                Button(
                    onClick = { submitValues() },
                    enabled = isAgeValid && isWeightValid,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.intro__continue_button).uppercase()
                    )
                }
            }
        }
    }
}

@Composable
fun NumberInputFieldWithError(
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    value: String,
    label: String,
    trailingText: String?,
    errorMessage: String?,
    onNext: () -> Unit,
    onChange: (String) -> Unit,
) {
    var isInputTouched by remember { mutableStateOf(false) }
    var prevIsFocused by remember { mutableStateOf(false) }

    val showError = errorMessage != null && isInputTouched

    Column(modifier.onFocusChanged {
        if (prevIsFocused && !it.isFocused) isInputTouched = true
        prevIsFocused = it.isFocused
    }) {
        TextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            modifier = inputModifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = false,
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            isError = showError,
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            trailingIcon = { if (trailingText != null) Text(trailingText) }
        )
        if (showError) {
            Spacer(Modifier.height(4.dp))
            Text(errorMessage!!,
                modifier = Modifier.padding(start = 16.dp),
                style = Typography.caption,
                color = MaterialTheme.colors.error)
        }
    }
}

@Composable
fun rememberIsAgeValid(ageValue: String): Pair<Boolean, String?> {
    val intAge = ageValue.toIntOrNull()
        ?: return false to stringResource(R.string.intro__measurements__input_invalid)

    if (intAge < MINIMUM_AGE || intAge > MAXIMUM_AGE) {
        return false to String.format(stringResource(R.string.intro__measurements__age_input__error,
            MINIMUM_AGE,
            MAXIMUM_AGE))
    }

    return true to null
}

@Composable
fun rememberIsWeightValid(weightValue: String): Pair<Boolean, String?> {
    val floatWeight = weightValue.toIntOrNull()
        ?: return false to stringResource(R.string.intro__measurements__input_invalid)

    if (floatWeight < MINIMUM_WEIGHT || floatWeight > MAXIMUM_WEIGHT) {
        return false to String.format(stringResource(R.string.intro__measurements__weight_input__error,
            MINIMUM_WEIGHT,
            MAXIMUM_WEIGHT))
    }

    return true to null
}
