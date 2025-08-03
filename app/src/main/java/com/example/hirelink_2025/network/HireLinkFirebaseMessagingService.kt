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
import com.example.hirelink_2025.view.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio para manejar notificaciones push de Firebase Cloud Messaging
 */
class HireLinkFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "job_notifications"
        private const val CHANNEL_NAME = "Ofertas Laborales"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de nuevas ofertas laborales"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "HireLinkFirebaseMessagingService created")
    }

    /**
     * Se llama cuando se recibe un nuevo token FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Enviar token al servidor para poder enviar notificaciones
        sendTokenToServer(token)
    }

    /**
     * Se llama cuando se recibe un mensaje push
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body, remoteMessage.data)
        }
    }

    /**
     * Maneja mensajes con datos personalizados
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val jobId = data["jobId"]
        val companyName = data["companyName"]
        val jobTitle = data["jobTitle"]

        Log.d(TAG, "Handling data message - Type: $type, JobId: $jobId, Company: $companyName")

        when (type) {
            "new_job" -> {
                val title = "Nueva oferta laboral"
                val body = "Se publicó un anuncio laboral de $companyName - $jobTitle. Ver en HireLink"
                showJobNotification(title, body, jobId)
            }
            else -> {
                Log.w(TAG, "Unknown message type: $type")
            }
        }
    }

    /**
     * Muestra una notificación genérica
     */
    private fun showNotification(title: String?, body: String?, data: Map<String, String> = emptyMap()) {
        val notificationTitle = title ?: "HireLink"
        val notificationBody = body ?: "Nueva notificación"
        
        showJobNotification(notificationTitle, notificationBody, data["jobId"])
    }

    /**
     * Muestra una notificación específica para ofertas laborales
     */
    private fun showJobNotification(title: String, body: String, jobId: String?) {
        Log.d(TAG, "Showing job notification - Title: $title, Body: $body, JobId: $jobId")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Si hay jobId, agregar datos para navegar al detalle
            if (!jobId.isNullOrEmpty()) {
                putExtra("navigation_destination", "job_detail")
                putExtra("job_id", jobId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (NOTIFICATION_ID_BASE + (jobId?.hashCode() ?: 0)),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.primary))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = NOTIFICATION_ID_BASE + (jobId?.hashCode() ?: System.currentTimeMillis().toInt())
        
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Notification shown with ID: $notificationId")
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
                lightColor = getColor(R.color.primary)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Envía el token FCM al servidor (Firestore)
     */
    private fun sendTokenToServer(token: String) {
        try {
            val firestoreService = FirestoreService()
            val currentUserId = firestoreService.getCurrentUserId()
            
            if (currentUserId != null) {
                Log.d(TAG, "Saving FCM token for user: $currentUserId")
                firestoreService.saveFCMToken(currentUserId, token)
            } else {
                Log.w(TAG, "No authenticated user, cannot save FCM token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
        }
    }
}