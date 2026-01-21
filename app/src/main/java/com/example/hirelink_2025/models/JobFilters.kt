package com.example.hirelink_2025.models

/**
 * Modelo para los filtros de búsqueda de empleos
 */
data class JobFilters(
    val modality: String = "", // "Presencial", "Remoto", "Híbrido", ""
    val salaryRange: SalaryRange = SalaryRange.ALL // Rango salarial
) {
    /**
     * Verifica si hay algún filtro activo
     */
    fun hasActiveFilters(): Boolean {
        return modality.isNotEmpty() || 
               salaryRange != SalaryRange.ALL
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clear(): JobFilters {
        return JobFilters()
    }
}

/**
 * Enum para rangos salariales
 */
enum class SalaryRange(val displayName: String, val minSalary: Int, val maxSalary: Int) {
    ALL("Todos los salarios", 0, Int.MAX_VALUE),
    RANGE_0_1000("Hasta S/1,000", 0, 1000),
    RANGE_1000_2000("S/1,000 - S/2,000", 1000, 2000),
    RANGE_2000_3000("S/2,000 - S/3,000", 2000, 3000),
    RANGE_3000_5000("S/3,000 - S/5,000", 3000, 5000),
    RANGE_5000_PLUS("Más de S/5,000", 5000, Int.MAX_VALUE);
    
    companion object {
        /**
         * Extrae el valor numérico del salario de un string
         * Ej: "S/2500", "2500 soles", "2500" -> 2500
         */
        fun extractSalaryValue(salaryString: String): Int {
            if (salaryString.isEmpty()) return 0
            
            // Buscar números en el string
            val regex = Regex("\\d+")
            val match = regex.find(salaryString.replace(",", ""))
            return match?.value?.toIntOrNull() ?: 0
        }
    }
}