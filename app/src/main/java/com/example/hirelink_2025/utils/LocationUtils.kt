package com.example.hirelink_2025.utils

object LocationUtils {
    fun extractCoordinates(ubication: String): Pair<Double, Double>? {
        return try {
            val latPattern = "Lat:\\s*(-?\\d+\\.\\d+)".toRegex()
            val lngPattern = "Lng:\\s*(-?\\d+\\.\\d+)".toRegex()

            val latMatch = latPattern.find(ubication)
            val lngMatch = lngPattern.find(ubication)

            if (latMatch != null && lngMatch != null) {
                val latitude = latMatch.groupValues[1].toDouble()
                val longitude = lngMatch.groupValues[1].toDouble()
                Pair(latitude, longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}