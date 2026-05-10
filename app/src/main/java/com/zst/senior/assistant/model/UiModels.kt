package com.zst.senior.assistant.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector
import com.zst.senior.assistant.R

/**
 * Model interfejsu użytkownika reprezentujący kategorię usługi oferowanej w aplikacji.
 *
 * Wykorzystywany głównie do wyświetlania kafelków wyboru typu pomocy na ekranach
 * tworzenia nowych zleceń.
 *
 * @property titleRes Identyfikator zasobu tekstowego z nazwą usługi (np. R.string.service_shopping).
 * @property icon Ikona reprezentująca daną kategorię.
 */
data class Service(val titleRes: Int, val icon: ImageVector)

/**
 * Predefiniowana, statyczna lista dostępnych kategorii usług.
 *
 * Zawiera standardowe typy pomocy, które senior może zgłosić, a wolontariusz może zrealizować.
 */
val mockServices = listOf(
    Service(R.string.service_shopping, Icons.Default.ShoppingCart),
    Service(R.string.service_meds, Icons.Default.MonitorHeart),
    Service(R.string.service_transport, Icons.Default.MedicalServices),
    Service(R.string.service_tech_help, Icons.Default.Smartphone),
    Service(R.string.service_official_matters, Icons.Default.AccountBalance),
    Service(R.string.service_cleaning, Icons.Default.CleaningServices),
    Service(R.string.service_dog_walking, Icons.Default.Pets),
    Service(R.string.service_repairs, Icons.Default.Build),
    Service(R.string.service_company, Icons.Default.People)
)
