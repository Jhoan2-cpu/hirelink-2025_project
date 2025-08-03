package com.example.hirelink_2025.utils

import kotlin.math.*

class LocationUtils {
    
    /**
     * Extrae coordenadas de un string con formato "Lat: X, Lng: Y"
     */
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

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * @param lat1 Latitud del primer punto
     * @param lon1 Longitud del primer punto
     * @param lat2 Latitud del segundo punto
     * @param lon2 Longitud del segundo punto
     * @return Distancia en kilómetros
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radio de la Tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * Obtiene coordenadas aproximadas desde una dirección
     * Esta es una implementación simplificada para demostración
     * En una app real, usarías Google Geocoding API
     */
    fun getCoordinatesFromAddress(address: String, city: String, country: String): Pair<Double, Double>? {
        android.util.Log.d("LocationUtils", "Getting coordinates for address: '$address', city: '$city', country: '$country'")
        
        // Coordenadas aproximadas para ciudades principales de Perú
        val peruCities = mapOf(
            "lima" to Pair(-12.0464, -77.0428),
            "arequipa" to Pair(-16.4090, -71.5375),
            "trujillo" to Pair(-8.1116, -79.0287),
            "chiclayo" to Pair(-6.7714, -79.8377),
            "piura" to Pair(-5.1945, -80.6328),
            "iquitos" to Pair(-3.7492, -73.2531),
            "cusco" to Pair(-13.5319, -71.9675),
            "huancayo" to Pair(-12.0653, -75.2049),
            "tacna" to Pair(-18.0056, -70.2494),
            "ica" to Pair(-14.0678, -75.7286),
            // Agregar más ciudades comunes
            "callao" to Pair(-12.0520, -77.1294),
            "chimbote" to Pair(-9.0853, -78.5783),
            "huaraz" to Pair(-9.5270, -77.5279),
            "pucallpa" to Pair(-8.3791, -74.5539),
            "sullana" to Pair(-4.9037, -80.6856),
            "tarapoto" to Pair(-6.4889, -76.3725)
        )

        return try {
            val cityKey = city.lowercase().trim()
            val countryKey = country.lowercase().trim()
            
            android.util.Log.d("LocationUtils", "Searching for city: '$cityKey' in country: '$countryKey'")
            
            // Si es Perú, buscar en el mapa de ciudades
            if (countryKey.contains("peru") || countryKey.contains("perú") || countryKey.isEmpty()) {
                val coordinates = peruCities[cityKey]
                if (coordinates != null) {
                    android.util.Log.d("LocationUtils", "Found coordinates for $cityKey: $coordinates")
                    coordinates
                } else {
                    android.util.Log.w("LocationUtils", "City '$cityKey' not found, using Lima as default")
                    peruCities["lima"] // Default a Lima si no encuentra la ciudad
                }
            } else {
                android.util.Log.d("LocationUtils", "Non-Peru country, using default coordinates")
                // Para otros países, usar coordenadas por defecto
                // En una app real, aquí usarías un servicio de geocoding
                Pair(-12.0464, -77.0428) // Default Lima
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationUtils", "Error getting coordinates from address", e)
            null
        }
    }

    /**
     * Verifica si una ubicación está dentro del rango "cercano" (10km)
     */
    fun isNearby(distance: Double): Boolean = distance <= 10.0

    /**
     * Formatea la distancia para mostrar
     */
    fun formatDistance(distance: Double): String {
        return when {
            distance < 1.0 -> "${(distance * 1000).toInt()}m"
            distance < 10.0 -> String.format("%.1f km", distance)
            else -> "${distance.toInt()} km"
        }
    }

    companion object {
        const val NEARBY_DISTANCE_KM = 10.0
        const val EARTH_RADIUS_KM = 6371.0
    }
}