package com.example.hirelink_2025.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.view.ui.activities.MainActivity

/**
 * Administrador de notificaciones locales para nuevas ofertas laborales
 */
class LocalNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "LocalNotificationManager"
        private const val CHANNEL_ID = "job_notifications"
        private const val CHANNEL_NAME = "Ofertas Laborales"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de nuevas ofertas laborales"
        private const val NOTIFICATION_ID_BASE = 2000
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones (requerido para Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = context.getColor(R.color.primary)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Muestra una notificación de nueva oferta laboral
     */
    fun showNewJobNotification(job: Job, company: Company) {
        Log.d(TAG, "Showing new job notification for: ${job.title} by ${company.name}")

        val title = "Nueva oferta laboral"
        val body = "Se publicó un anuncio laboral de ${company.name} - ${job.title}. Ver en HireLink"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigation_destination", "job_detail")
            putExtra("job_id", job.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (NOTIFICATION_ID_BASE + job.id.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(context.getColor(R.color.primary))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationId = NOTIFICATION_ID_BASE + job.id.hashCode()
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Local notification shown with ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing local notification", e)
        }
    }

    /**
     * Muestra una notificación de prueba
     */
    fun showTestNotification() {
        Log.d(TAG, "Showing test notification")

        val title = "HireLink - Prueba"
        val body = "Esta es una notificación de prueba del sistema HireLink"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(context.getColor(R.color.primary))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            notificationManager.notify(NOTIFICATION_ID_BASE, notificationBuilder.build())
            Log.d(TAG, "Test notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing test notification", e)
        }
    }

    /**
     * Cancela todas las notificaciones
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * Cancela una notificación específica
     */
    fun cancelNotification(jobId: String) {
        val notificationId = NOTIFICATION_ID_BASE + jobId.hashCode()
        notificationManager.cancel(notificationId)
        Log.d(TAG, "Notification cancelled for job: $jobId")
    }
}