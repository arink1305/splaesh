package no.uio.ifi.in2000.aryanma.splaesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import no.uio.ifi.in2000.aryanma.splaesh.ui.navigation.AppNavigation
import no.uio.ifi.in2000.aryanma.splaesh.ui.theme.SplaeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplaeshTheme  {
                AppNavigation()
            }
        }
    }
}
