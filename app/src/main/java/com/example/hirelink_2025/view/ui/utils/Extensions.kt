package com.example.hirelink_2025.view.ui.utils

import android.R
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

/**
 * Extensiones útiles para el proyecto HireLink
 * Simplifican tareas comunes y mejoran la legibilidad del código
 */

// ================================
// EXTENSIONES PARA FRAGMENT
// ================================

/**
 * Muestra un Toast corto
 */
fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

/**
 * Muestra un Toast corto con string resource
 */
fun Fragment.showToast(@StringRes messageRes: Int) {
    Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
}

/**
 * Muestra un Snackbar
 */
fun Fragment.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(requireView(), message, duration)
    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action() }
    }
    snackbar.show()
}

/**
 * Muestra un Snackbar de éxito
 */
fun Fragment.showSuccessSnackbar(message: String) {
    val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.holo_green_light))
    snackbar.show()
}

/**
 * Muestra un Snackbar de error
 */
fun Fragment.showErrorSnackbar(message: String, retryAction: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.holo_red_light))
    if (retryAction != null) {
        snackbar.setAction("Reintentar") { retryAction() }
    }
    snackbar.show()
}

// ================================
// EXTENSIONES PARA VIEW
// ================================

/**
 * Hace visible una vista
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hace invisible una vista
 */
fun View.hide() {
    visibility = View.INVISIBLE
}

/**
 * Hace gone una vista
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Cambia la visibilidad basada en una condición
 */
fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Activa o desactiva una vista
 */
fun View.setEnabled(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1.0f else 0.5f
}

// ================================
// EXTENSIONES PARA LIVEDATA - CORREGIDAS
// ================================

/**
 * Observa LiveData con manejo automático de ciclo de vida (versión corregida)
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, onChanged: (T?) -> Unit) {
    var observer: Observer<T>? = null
    observer = Observer { value ->
        onChanged(value)
        observer?.let { removeObserver(it) }
    }
    observe(owner, observer)
}

/**
 * Observa LiveData solo cuando no es null
 */
fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    observe(owner) { value ->
        value?.let { onChanged(it) }
    }
}

/**
 * Observa LiveData y ejecuta acción solo si el valor cambió
 */
fun <T> LiveData<T>.observeDistinct(owner: LifecycleOwner, onChanged: (T?) -> Unit) {
    var lastValue: T? = null
    observe(owner) { value ->
        if (value != lastValue) {
            lastValue = value
            onChanged(value)
        }
    }
}

// ================================
// EXTENSIONES PARA CONTEXT
// ================================

/**
 * Obtiene un color de los recursos
 */
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

/**
 * Convierte dp a px
 */
fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

/**
 * Convierte px a dp
 */
fun Context.pxToDp(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}

// ================================
// EXTENSIONES PARA STRING
// ================================

/**
 * Verifica si un string es válido (no null, no vacío, no solo espacios)
 */
fun String?.isValidString(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * Capitaliza la primera letra de cada palabra
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}

/**
 * Trunca un string a una longitud específica
 */
fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${take(maxLength - 3)}..."
}

/**
 * Quita espacios y caracteres especiales
 */
fun String.sanitize(): String {
    return replace(Regex("[^A-Za-z0-9áéíóúÁÉÍÓÚñÑ ]"), "").trim()
}

/**
 * Convierte a formato de nombre propio
 */
fun String.toTitleCase(): String {
    return lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}

// ================================
// EXTENSIONES PARA LISTAS
// ================================

/**
 * Verifica si una lista es válida (no null y no vacía)
 */
fun <T> List<T>?.isValidList(): Boolean {
    return !this.isNullOrEmpty()
}

/**
 * Obtiene un elemento seguro de una lista
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index >= 0 && index < size) this[index] else null
}

/**
 * Divide una lista en chunks de tamaño específico
 */
fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (size <= 0) emptyList() else windowed(size, size, true)
}

/**
 * Encuentra el índice de un elemento usando un predicado
 */
fun <T> List<T>.indexOfFirst(predicate: (T) -> Boolean): Int {
    for (index in indices) {
        if (predicate(this[index])) {
            return index
        }
    }
    return -1
}

// ================================
// EXTENSIONES PARA NUMBERS
// ================================

/**
 * Formatea un número como precio en soles
 */
fun Double.toPriceString(): String {
    return "S/ ${String.format("%.2f", this)}"
}

/**
 * Formatea un número como precio en soles (Int)
 */
fun Int.toPriceString(): String {
    return "S/ ${String.format("%,d", this)}"
}

/**
 * Convierte a porcentaje
 */
fun Double.toPercentage(): String {
    return "${(this * 100).toInt()}%"
}

/**
 * Limita un número a un rango
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Double.clamp(min: Double, max: Double): Double {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

// ================================
// CONSTANTES ÚTILES
// ================================

object Constants {
    // Delays
    const val SPLASH_DELAY = 2000L
    const val ANIMATION_DURATION = 300L
    const val NETWORK_TIMEOUT = 30000L
    const val DEBOUNCE_DELAY = 500L

    // Keys para Bundle
    const val KEY_JOB_ID = "job_id"
    const val KEY_JOB_TITLE = "job_title"
    const val KEY_USER_ID = "user_id"
    const val KEY_APPLICANT_ID = "applicant_id"

    // Valores por defecto
    const val DEFAULT_PAGE_SIZE = 20
    const val DEFAULT_RETRY_COUNT = 3
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_TITLE_LENGTH = 100

    // Formato de fechas
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm"
    const val TIME_FORMAT = "HH:mm"

    // Preferencias
    const val PREF_NAME = "hirelink_prefs"
    const val PREF_USER_LOGGED_IN = "user_logged_in"
    const val PREF_USER_ID = "user_id"
    const val PREF_FIRST_TIME = "first_time"
    const val PREF_THEME = "theme"

    // Validation
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    const val PHONE_PATTERN = "^[0-9]{9,12}$"

    // Request codes
    const val REQUEST_CODE_PICK_IMAGE = 1001
    const val REQUEST_CODE_PERMISSIONS = 1002
    const val REQUEST_CODE_LOCATION = 1003
}

// ================================
// VALIDACIONES
// ================================

/**
 * Valida si un email es válido
 */
fun String.isValidEmail(): Boolean {
    return matches(Constants.EMAIL_PATTERN.toRegex())
}

/**
 * Valida si un teléfono es válido
 */
fun String.isValidPhone(): Boolean {
    return matches(Constants.PHONE_PATTERN.toRegex())
}

/**
 * Valida si una contraseña es válida
 */
fun String.isValidPassword(): Boolean {
    return length >= Constants.MIN_PASSWORD_LENGTH && isNotBlank()
}

/**
 * Valida si un nombre es válido
 */
fun String.isValidName(): Boolean {
    return length >= 2 && matches("^[A-Za-záéíóúÁÉÍÓÚñÑ ]+$".toRegex())
}