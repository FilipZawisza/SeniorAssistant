package com.zst.senior.assistant.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentChange
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.PelneZlecenie
import com.zst.senior.assistant.model.PelneZlecenieDlaSeniora
import com.zst.senior.assistant.model.PelneZlecenieDlaWolontariusza
import com.zst.senior.assistant.model.ZlecenieRef
import com.zst.senior.assistant.repository.HelpRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** * Stany interfejsu (UI State) dla widoków wyświetlających listy zleceń. */
sealed class ZleceniaListUiState {
    data object Loading : ZleceniaListUiState()
    data class Success(val zlecenia: List<Any>) : ZleceniaListUiState()
    data class Error(val message: String) : ZleceniaListUiState()
}

/** * Stany interfejsu (UI State) dla ekranu ze szczegółami konkretnego zlecenia. */
sealed class ZlecenieDetailsUiState {
    data object Loading : ZlecenieDetailsUiState()
    data class Success(val pelneZlecenie: PelneZlecenie) : ZlecenieDetailsUiState()
    data class Error(val message: String) : ZlecenieDetailsUiState()
}

/** * Stany interfejsu (UI State) dla akcji wywoływanych przez użytkownika. */
sealed class ZlecenieActionUiState {
    data object Idle : ZlecenieActionUiState()
    data object Loading : ZlecenieActionUiState()
    data class Success(val message: String) : ZlecenieActionUiState()
    data class Error(val message: String) : ZlecenieActionUiState()
}

/**
 * Główny ViewModel do zarządzania pełnym cyklem życia zleceń.
 *
 * Komponent ten koordynuje współpracę między Seniorami a Wolontariuszami.
 * Deleguje operacje na danych do [HelpRequestRepository].
 *
 * @property authViewModel ViewModel autoryzacji do pobierania ID aktualnie zalogowanego użytkownika.
 * @property repository Repozytorium obsługujące logikę zleceń pomocy.
 */
class HelpRequestViewModel(
    private val application: Application,
    private val authViewModel: AuthViewModel,
    private val repository: HelpRequestRepository = HelpRequestRepository()
) : AndroidViewModel(application) {

    private val _zleceniaListUiState = MutableStateFlow<ZleceniaListUiState>(ZleceniaListUiState.Loading)
    val zleceniaListUiState: StateFlow<ZleceniaListUiState> = _zleceniaListUiState.asStateFlow()

    private val _zlecenieDetailsUiState = MutableStateFlow<ZlecenieDetailsUiState>(ZlecenieDetailsUiState.Loading)
    val zlecenieDetailsUiState: StateFlow<ZlecenieDetailsUiState> = _zlecenieDetailsUiState.asStateFlow()

    private val _actionUiState = MutableStateFlow<ZlecenieActionUiState>(ZlecenieActionUiState.Idle)
    val actionUiState: StateFlow<ZlecenieActionUiState> = _actionUiState.asStateFlow()

    /** Resetuje stan akcji powracając do bezczynności ([ZlecenieActionUiState.Idle]). */
    fun resetActionState() {
        _actionUiState.value = ZlecenieActionUiState.Idle
    }

    /** Pobiera wszystkie niezakończone zlecenia. */
    fun fetchAllZlecenia() {
        _zleceniaListUiState.value = ZleceniaListUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getZleceniaByStatus(listOf("Wolne", "Aktywne", "DoPotwierdzenia"))
                val sortedList = list.sortedWith(compareBy {
                    when (it.status) {
                        "Wolne" -> 1
                        "DoPotwierdzenia" -> 2
                        "Aktywne" -> 3
                        else -> 4
                    }
                })
                _zleceniaListUiState.value = ZleceniaListUiState.Success(sortedList)
            } catch (e: Exception) {
                _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.help_msg_error_load, e.message))
            }
        }
    }

    /** Pobiera historię zakończonych zleceń. */
    fun fetchArchiwalneZlecenia() {
        _zleceniaListUiState.value = ZleceniaListUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getZleceniaByStatus(listOf("Zakonczone"))
                _zleceniaListUiState.value = ZleceniaListUiState.Success(list)
            } catch (e: Exception) {
                _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.help_msg_error_load_archive, e.message))
            }
        }
    }

    /** Pobiera publicznie dostępne zlecenia o statusie "Wolne". */
    fun fetchPublicZlecenia() {
        _zleceniaListUiState.value = ZleceniaListUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getPublicZlecenia()
                _zleceniaListUiState.value = ZleceniaListUiState.Success(list)
            } catch (e: Exception) {
                _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.help_msg_error_load, e.message))
            }
        }
    }

    /** Tworzy nowe zlecenie pomocy. */
    fun utworzZlecenie(context: Context, opis: String, location: Location? = null) {
        val seniorId = authViewModel.currentUserId.value ?: run {
            _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_unlogged))
            return
        }
        if (opis.isBlank()) {
            _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_empty_desc))
            return
        }

        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.createZlecenie(context, seniorId, opis, location)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_created))
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Pobiera pełne dane o konkretnym zleceniu. */
    fun pobierzPelneDaneZlecenia(context: Context, zlecenieId: String) {
        _zlecenieDetailsUiState.value = ZlecenieDetailsUiState.Loading
        viewModelScope.launch {
            try {
                val details = repository.getPelneZlecenie(context, zlecenieId)
                _zlecenieDetailsUiState.value = ZlecenieDetailsUiState.Success(details)
            } catch (e: Exception) {
                _zlecenieDetailsUiState.value = ZlecenieDetailsUiState.Error(application.getString(R.string.help_msg_error_fetch_data, e.message))
            }
        }
    }

    /** Wolontariusz podejmuje zlecenie. */
    fun podejmijZlecenie(zlecenieId: String) {
        val wolontariuszId = authViewModel.currentUserId.value ?: run {
            _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_unlogged))
            return
        }

        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.acceptZlecenie(zlecenieId, wolontariuszId)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_accepted))
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_accept, e.message))
            }
        }
    }

    /** Wolontariusz oznacza zlecenie jako wykonane. */
    fun oznaczJakoWykonane(context: Context, zlecenieId: String) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.updateZlecenieStatus(zlecenieId, "DoPotwierdzenia")
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_volunteer_done))
                fetchMojeZleceniaDlaWolontariusza(context)
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Starsza metoda potwierdzania wykonania przez Seniora. */
    fun potwierdzWykonanie(zlecenieId: String) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.updateZlecenieStatus(zlecenieId, "Zakonczone")
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_senior_confirmed))
                fetchMojeZleceniaDlaSeniora()
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Senior potwierdza wykonanie zlecenia z oceną. */
    fun potwierdzWykonanieZOcena(zlecenieId: String, ocena: Int) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.confirmZlecenieWithRating(zlecenieId, ocena)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_senior_confirmed_rating))
                fetchMojeZleceniaDlaSeniora()
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Senior zgłasza błąd - wykonanie zlecenia odrzucone. */
    fun odrzucWykonanieZlecenia(zlecenieId: String) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.rejectExecution(zlecenieId)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_rejected))
                fetchMojeZleceniaDlaSeniora()
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Senior anuluje zlecenie. */
    fun anulujZlecenie(zlecenieId: String) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.cancelZlecenie(zlecenieId)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_cancelled))
                fetchMojeZleceniaDlaSeniora()
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Senior zgłasza brak wolontariusza. */
    fun zglosBrakWolontariusza(zlecenieId: String) {
        _actionUiState.value = ZlecenieActionUiState.Loading
        viewModelScope.launch {
            try {
                repository.reportNoShow(zlecenieId)
                _actionUiState.value = ZlecenieActionUiState.Success(application.getString(R.string.help_msg_success_no_show))
                fetchMojeZleceniaDlaSeniora()
            } catch (e: Exception) {
                _actionUiState.value = ZlecenieActionUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Pobiera zlecenia dla zalogowanego Seniora. */
    fun fetchMojeZleceniaDlaSeniora() {
        val seniorId = authViewModel.currentUserId.value ?: run {
            _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.profile_msg_no_user))
            return
        }
        _zleceniaListUiState.value = ZleceniaListUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getZleceniaForSenior(seniorId)
                _zleceniaListUiState.value = ZleceniaListUiState.Success(list)
            } catch (e: Exception) {
                _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Pobiera zlecenia dla zalogowanego Wolontariusza. */
    fun fetchMojeZleceniaDlaWolontariusza(context: Context) {
        val wolontariuszId = authViewModel.currentUserId.value ?: run {
            _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.profile_msg_no_user))
            return
        }
        _zleceniaListUiState.value = ZleceniaListUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getZleceniaForVolunteer(context, wolontariuszId)
                _zleceniaListUiState.value = ZleceniaListUiState.Success(list)
            } catch (e: Exception) {
                _zleceniaListUiState.value = ZleceniaListUiState.Error(application.getString(R.string.help_msg_error_generic, e.message))
            }
        }
    }

    /** Uruchamia nasłuch nowych zleceń in okolicy. */
    fun startListeningForNearbyZlecenia(context: Context, wolontariuszLat: Double, wolontariuszLng: Double, maxPromienKm: Double) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            repository.observeNewZlecenia().collect { dc ->
                val timestamp = try {
                    (dc.document.get("Timestamp") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                } catch (e: Exception) { 0L }

                if (timestamp >= startTime) {
                    val sLat = dc.document.getDouble("seniorLat")
                    val sLng = dc.document.getDouble("seniorLng")
                    val opis = dc.document.getString("Zlecenie") ?: application.getString(R.string.help_msg_nearby_notification_desc)

                    if (sLat != null && sLng != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(wolontariuszLat, wolontariuszLng, sLat, sLng, results)
                        if ((results[0] / 1000.0) <= maxPromienKm) {
                            sendVolunteerNotification(context, dc.document.id, opis)
                        }
                    }
                }
            }
        }
    }

    private fun sendVolunteerNotification(context: Context, zlecenieId: String, opis: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = com.zst.senior.assistant.utils.NotificationHelper.VOLUNTEER_CHANNEL_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(application.getString(R.string.help_msg_nearby_notification_title))
            .setContentText(opis)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(zlecenieId.hashCode(), notification)
    }
}

/** Fabryka dla [HelpRequestViewModel]. */
class HelpRequestViewModelFactory(
    private val application: Application,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HelpRequestViewModel(application, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
