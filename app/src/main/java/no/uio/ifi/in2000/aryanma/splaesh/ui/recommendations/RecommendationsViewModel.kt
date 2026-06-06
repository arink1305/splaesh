package no.uio.ifi.in2000.aryanma.splaesh.ui.recommendations

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.aryanma.splaesh.data.RecommendationsRepository
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.RecommendationPlace
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val RECOMMENDATIONS_VIEW_MODEL_TAG = "RecommendationsViewModel"

data class RecommendationsUiState(
    val isLoading: Boolean = false,
    val selectedRadiusKm: Int = 5,
    val recommendations: List<RecommendationPlace> = emptyList(),
    val hasLocationData: Boolean = false
)

class RecommendationsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = RecommendationsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    fun setRadiusKm(radiusKm: Int) {
        _uiState.update { current -> current.copy(selectedRadiusKm = radiusKm) }
    }

    fun loadRecommendations(
        warnings: List<Warning>,
        profile: BathingScoreProfile
    ) {
        val radiusKm = _uiState.value.selectedRadiusKm
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    recommendations = emptyList()
                )
            }

            val userPoint = repository.getCurrentUserLocation()
            if (userPoint == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLocationData = false,
                        recommendations = emptyList()
                    )
                }
                return@launch
            }

            try {
                val recommendations = repository.getRecommendations(
                    userLatitude = userPoint.latitude(),
                    userLongitude = userPoint.longitude(),
                    radiusKm = radiusKm,
                    warnings = warnings,
                    profile = profile
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLocationData = true,
                        recommendations = recommendations
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    RECOMMENDATIONS_VIEW_MODEL_TAG,
                    "Kunne ikke laste anbefalinger",
                    exception
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLocationData = true,
                        recommendations = emptyList()
                    )
                }
            }
        }
    }
}
