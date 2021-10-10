package io.ushakov.bike_workouts.ui.views.first_time_setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Typography

@Composable
fun Name(initialName: String?, onNameSubmit: (name: String) -> Unit) {
    var nameInputValue by remember { mutableStateOf(initialName ?: "") }

    Surface {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier
                .weight(1f)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.intro__name__titile),
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
                    text = stringResource(R.string.intro__name__subtitle),
                    style = Typography.h5
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = nameInputValue,
                    onValueChange = { nameInputValue = it },
                    label = { Text(stringResource(R.string.intro__name__input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrect = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNameSubmit(nameInputValue) }
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onNameSubmit(nameInputValue) },
                    enabled = nameInputValue.trim() !== "",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.intro__continue_button)
                    )
                }
            }
        }
    }
}