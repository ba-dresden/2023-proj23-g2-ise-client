package de.ba.railroad.simpleclient.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.relay.compose.RowScopeInstanceImpl.weight
import de.ba.railroad.simpleclient.buttonlocomotiverenate.ButtonLocomotiveRenate
import de.ba.railroad.simpleclient.buttonlocomotiverenate.Default
import de.ba.railroad.simpleclient.rails.Rails
import de.ba.railroad.simpleclient.speedcontrol.SpeedControl
import de.ba.railroad.simpleclient.trainextensions.TrainExtensions
import model.Locomotive

@Composable
fun mainScreen(
    factory: ViewModelFac,
    viewModel: GartenbahnViewModel = viewModel(factory = factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box() {
            Rails(modifier = Modifier.align(Alignment.TopStart))

            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(width = 2.dp, color = Color.White),
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .size(width = 93.dp, height = 40.dp)
                ) {
                    ButtonLocomotiveRenate(
                        text = "Renate",
                        default = if (uiState.currentLocomotiveName == "Renate") Default.Pressed else Default.Default,
                        onClick = {
                            viewModel.changeLocomotive("Renate")
                        })
                }
                Card(
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(width = 2.dp, color = Color.White),
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .size(width = 90.dp, height = 40.dp)

                ) {
                    ButtonLocomotiveRenate(
                        text = "Steam",
                        default = if (uiState.currentLocomotiveName == "Steam") Default.Pressed else Default.Default,
                        onClick = {
                            viewModel.changeLocomotive("Steam")
                        })
                }
            }
            SpeedControl(
                modifier = Modifier.align(Alignment.BottomCenter),
                onFastForwardClick = {
                    viewModel.changeSpeed(100, Locomotive.DIRECTION_FORWARD)
                },
                onForwardButtonClick = {
                    viewModel.changeSpeed(60, Locomotive.DIRECTION_FORWARD)
                },
                onStopButtonClick = {
                    viewModel.changeSpeed(0, Locomotive.DIRECTION_FORWARD)
                },
                onBackButtonClick = {
                    viewModel.changeSpeed(60, Locomotive.DIRECTION_BACKWARD)
                },
                onFastBackButtonClick = {
                    viewModel.changeSpeed(100, Locomotive.DIRECTION_BACKWARD)
                }
            )
        }
        TrainExtensions()
    }

}
