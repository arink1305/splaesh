package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState


@Composable
fun ZoomButton (
    modifier: Modifier = Modifier,
    mapViewportState: MapViewportState,
    bottomPadding: Dp

    ) {
    Column(
        modifier = modifier
            .padding(bottom = bottomPadding + 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FloatingActionButton(onClick = {
            mapViewportState.easeTo(cameraOptions {
                zoom(mapViewportState.cameraState?.zoom?.plus(1.0))
            })
        }) {
            Text("+", fontSize = 20.sp)
        }
        FloatingActionButton(onClick = {
            mapViewportState.easeTo(cameraOptions {
                zoom(mapViewportState.cameraState?.zoom?.minus(1.0))
            })
        }) {
            Text("−", fontSize = 20.sp)
        }
    }
}