# HireLink 2025 - Arquitectura de Modelos de Datos Firestore NoSQL

## Resumen Ejecutivo

Basado en un análisis exhaustivo del proyecto Android HireLink 2025, este documento proporciona recomendaciones para una arquitectura de **base de datos NoSQL Firestore** que es escalable y mantenible. El diseño está específicamente optimizado para **almacenamiento basado en documentos NoSQL** y soporta los requisitos comerciales principales: propiedad de múltiples empresas, publicación de empleos, postulaciones laborales, y gestión de estados de postulaciones.

> **⚠️ IMPORTANTE**: Esta arquitectura está específicamente diseñada para **Firebase Firestore (NoSQL)** y sigue las mejores prácticas NoSQL incluyendo desnormalización estratégica, relaciones basadas en documentos, y patrones de consulta optimizados para Firestore.

## Análisis del Estado Actual

### Modelos de Datos Existentes

#### Modelos de Usuario Principales
- **User**: Información básica del usuario (id, email, name, phone, profileImageUrl, active)
- **UserProfile**: Datos de perfil extendido (profesión, biografía, habilidades, experiencia, educación, idiomas, ubicación, etc.)
- **UserCredentials**: Respaldo de autenticación (solo desarrollo)

#### Modelos de Empresa
- **Company**: Información de empresa con ownerId vinculado al Usuario

#### Modelos de Trabajo
- **Job**: Modelo principal de trabajo (contiene datos redundantes e inconsistencias)
- **JobAd**: Modelo alternativo de trabajo (nomenclatura en español, estructura más simple)
- **JobStatus**: Enum integral con gestión de estados
- **JobCategory**: Modelo simple de categoría
- **WorkType**: Enum para modalidad de trabajo

#### Modelos de Postulación
- **Applicant**: Representa una postulación laboral (nomenclatura confusa)
- **Application**: Modelo simple de postulación (enfocado en UI)
- **ApplicationStatus**: Enum para estados de postulación

#### Modelos de Soporte
- **Notification**: Notificaciones del sistema
- **AdvancedStats**: Estadísticas de la plataforma

### Problemas Identificados

1. **Confusión de Nomenclatura**: El modelo "Applicant" representa postulaciones, no usuarios
2. **Redundancia de Datos**: El modelo Job contiene datos de empresa que deberían derivarse
3. **Modelos Inconsistentes**: Múltiples modelos de trabajo (Job, JobAd) con diferentes propósitos
4. **Relaciones Faltantes**: No hay separación clara entre ofertas laborales y postulaciones
5. **Preocupaciones de Escalabilidad**: Integración directa de datos de empresa en trabajos

## Análisis de Requisitos del Negocio

Desde `problems.txt` y análisis del código:

1. **Propiedad de Múltiples Empresas**: Los usuarios pueden poseer múltiples empresas
2. **Roles Duales de Usuario**: Los usuarios pueden tanto publicar empleos como postular a empleos
3. **Flujo de Postulaciones**: Las postulaciones pasan por estados (pendiente, aceptado, rechazado)
4. **Integridad de Datos**: Prevenir que los usuarios postulen a sus propios empleos
5. **Rendimiento**: Consultas eficientes para búsquedas de empleos y gestión de postulaciones

## Arquitectura de Modelos de Datos Recomendada

### Principios de Diseño NoSQL para Firestore

1. **Estructura Basada en Documentos**: Cada colección contiene documentos con datos embebidos o referenciados
2. **Desnormalización Estratégica**: Duplicar datos frecuentemente accedidos para mejorar el rendimiento
3. **Relaciones de Colecciones**: Usar IDs de documentos para referenciar entre colecciones
4. **Optimización de Consultas**: Diseñar para las limitaciones y fortalezas de consulta de Firestore
5. **Escalabilidad**: Soportar escalado horizontal y actualizaciones en tiempo real
6. **Operaciones Atómicas**: Usar transacciones y escrituras en lote para consistencia

### Modelos Recomendados

#### 1. Gestión de Usuarios

```kotlin
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val userId: String = "", // Referencia a Usuario
    val profession: String? = null,
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val experience: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val languages: List<String> = emptyList(),
    val location: String = "",
    val availability: String = "",
    val salaryExpectation: String = "",
    val linkedinUrl: String = "",
    val portfolioUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
```

#### 2. Gestión de Empresas

```kotlin
data class Company(
    val id: String = "",
    val name: String = "",
    val industry: String = "",
    val description: String = "",
    val size: CompanySize = CompanySize.SMALL,
    val foundedYear: Int = 0,
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val logoUrl: String = "",
    val ownerId: String = "", // Referencia a Usuario
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CompanySize {
    STARTUP,     // 1-10 empleados
    SMALL,       // 11-50 empleados
    MEDIUM,      // 51-200 empleados
    LARGE,       // 201-1000 empleados
    ENTERPRISE   // 1000+ empleados
}
```

#### 3. Gestión de Empleos (Optimizado para Firestore)

```kotlin
data class JobOffer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val responsibilities: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    
    // Detalles del Empleo
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,
    val workType: WorkType = WorkType.ONSITE,
    val experienceLevel: ExperienceLevel = ExperienceLevel.MID,
    val salaryMin: Int? = null,
    val salaryMax: Int? = null,
    val salaryCurrency: String = "PEN",
    val location: String = "",
    val vacancies: Int = 1,
    val deadline: Long? = null,
    
    // Referencias a Documentos de Firestore (relaciones NoSQL)
    val companyId: String = "", // Referencia a companies/{companyId}
    val ownerId: String = "",   // Referencia a users/{ownerId}
    val categoryId: String = "", // Referencia a job_categories/{categoryId}
    
    // DATOS DESNORMALIZADOS de Empresa (optimización NoSQL para rendimiento de lectura)
    val companyName: String = "",      // Desnormalizado de Company
    val companyLogoUrl: String = "",   // Desnormalizado de Company
    val companyLocation: String = "",  // Desnormalizado de Company
    val companySize: String = "",      // Desnormalizado de Company
    
    // DATOS DESNORMALIZADOS del Propietario (para visualización y filtrado)
    val ownerName: String = "",        // Desnormalizado de User
    val ownerEmail: String = "",       // Desnormalizado de User
    
    // Status & Metadata
    val status: JobStatus = JobStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val publishedAt: Long? = null,
    val closedAt: Long? = null,
    
    // Firestore-optimized fields for queries and real-time updates
    val searchableTitle: String = "", // Lowercase for case-insensitive search
    val searchableSkills: List<String> = emptyList(), // Lowercase skills for search
    val locationTags: List<String> = emptyList(), // ["remote", "lima", "peru"] for flexible location search
    
    // Statistics (updated via Cloud Functions or batch operations)
    val viewsCount: Int = 0,
    val applicationsCount: Int = 0,
    val bookmarksCount: Int = 0
)

enum class EmploymentType {
    FULL_TIME,    // Tiempo completo
    PART_TIME,    // Tiempo parcial
    CONTRACT,     // Por contrato
    TEMPORARY,    // Temporal
    INTERNSHIP,   // Prácticas profesionales
    FREELANCE     // Independiente
}

enum class WorkType {
    REMOTE,       // Remoto
    ONSITE,       // Presencial
    HYBRID        // Híbrido
}

enum class ExperienceLevel {
    ENTRY,        // 0-2 años
    MID,          // 2-5 años
    SENIOR,       // 5-10 años
    LEAD          // 10+ años
}
```

#### 4. Gestión de Postulaciones (Optimizado para Firestore)

```kotlin
data class JobApplication(
    val id: String = "",
    val jobId: String = "",        // Referencia a job_offers/{jobId}
    val applicantId: String = "",  // Referencia a users/{applicantId}
    val coverLetter: String? = null,
    val resumeUrl: String? = null,
    val customAnswers: Map<String, String> = emptyMap(), // Para preguntas personalizadas
    
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val statusChangedBy: String? = null, // Referencia a users/{userId}
    val statusChangedAt: Long? = null,
    val statusReason: String? = null, // Razón de rechazo/aceptación
    
    val appliedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val notificationsEnabled: Boolean = true,
    
    // DATOS DESNORMALIZADOS del Trabajo (optimización NoSQL para rendimiento de lectura)
    val jobTitle: String = "",         // Desnormalizado de JobOffer
    val jobCompanyName: String = "",   // Desnormalizado de JobOffer
    val jobCompanyLogo: String = "",   // Desnormalizado de JobOffer
    val jobLocation: String = "",      // Desnormalizado de JobOffer
    val jobSalary: String = "",        // Desnormalizado de JobOffer
    val jobOwnerId: String = "",       // Desnormalizado de JobOffer (para permisos)
    
    // DATOS DESNORMALIZADOS del Postulante (para vista del empleador)
    val applicantName: String = "",    // Desnormalizado de User
    val applicantEmail: String = "",   // Desnormalizado de User
    val applicantPhone: String = "",   // Desnormalizado de User
    val applicantProfileImage: String = "", // Desnormalizado de User
    val applicantProfession: String = "", // Desnormalizado de UserProfile
    val applicantExperience: String = "", // Desnormalizado de UserProfile
    val applicantSkills: List<String> = emptyList(), // Desnormalizado de UserProfile
    
    // Campos optimizados para consultas de Firestore
    val statusSearchable: String = "", // Para filtrado de estado sin distinguir mayúsculas
    val applicantNameLower: String = "", // Para búsqueda de postulante sin distinguir mayúsculas
)

enum class ApplicationStatus {
    PENDING,     // Esperando revisión
    REVIEWING,   // En revisión
    SHORTLISTED, // Seleccionado para la siguiente ronda
    INTERVIEWED, // Entrevista completada
    ACCEPTED,    // Trabajo ofrecido
    REJECTED,    // Postulación rechazada
    WITHDRAWN,   // Postulante se retiró
    EXPIRED      // Postulación expirada
}
```

#### 5. Modelos de Apoyo

```kotlin
data class JobCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val parentCategoryId: String? = null, // Para subcategorías
    val displayOrder: Int = 0,
    val active: Boolean = true
)

data class Notification(
    val id: String = "",
    val userId: String = "", // Referencia a Usuario
    val type: NotificationType = NotificationType.INFO,
    val title: String = "",
    val message: String = "",
    val actionUrl: String = "",
    val relatedEntityId: String = "", // ID de JobOffer, Application, etc.
    val relatedEntityType: String = "", // "job_offer", "application", etc.
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

// Mantener el enum NotificationType existente
```

### Estructura de Base de Datos Firestore (Colecciones NoSQL)

```
Base de Datos Firestore:
├── users/
│   └── {userId}                     // Documento con datos de Usuario
├── user_profiles/
│   └── {userId}                     // Documento con datos de UserProfile
├── companies/
│   └── {companyId}                  // Documento con datos de Empresa
├── job_offers/
│   └── {jobId}                      // Documento con datos de JobOffer (incluye datos desnormalizados de empresa/propietario)
├── job_applications/
│   └── {applicationId}              // Documento con datos de JobApplication (incluye datos desnormalizados de trabajo/postulante)
├── job_categories/
│   └── {categoryId}                 // Documento con datos de JobCategory
├── notifications/
│   └── {notificationId}             // Documento con datos de Notification
├── user_credentials/                // (solo desarrollo)
│   └── {userId}                     // Documento con datos de UserCredentials
└── counters/                        // (opcional) Para estadísticas
    ├── job_stats/
    │   └── {jobId}                  // Documento con conteos de vistas, postulaciones
    └── user_stats/
        └── {userId}                 // Documento con estadísticas de actividad del usuario
```

### Relaciones de Documentos NoSQL (Basadas en Referencias)

1. **Usuario → Empresas**: Referenciado por el campo `ownerId` en documentos de Company
2. **Empresa → Ofertas de Trabajo**: Referenciado por el campo `companyId` en documentos de JobOffer  
3. **Usuario → Ofertas de Trabajo**: Referenciado por el campo `ownerId` en documentos de JobOffer
4. **Oferta de Trabajo → Postulaciones**: Referenciado por el campo `jobId` en documentos de JobApplication
5. **Usuario → Postulaciones**: Referenciado por el campo `applicantId` en documentos de JobApplication

> **Nota NoSQL**: A diferencia de SQL, Firestore usa referencias de documentos en lugar de claves foráneas. Los datos se desnormalizan estratégicamente para evitar JOINs costosos y mejorar el rendimiento de lectura.

### Estrategias de Optimización de Consultas Firestore

#### 1. Índices Compuestos de Firestore (Requeridos para consultas complejas)
```
// Crear estos índices en la Consola de Firebase
job_offers: [status, createdAt DESC]                    // Para listados de trabajos por estado
job_offers: [ownerId, status, createdAt DESC]          // Para gestión de trabajos del usuario
job_offers: [companyId, status, createdAt DESC]        // Para listados de trabajos de empresa
job_offers: [status, locationTags, createdAt DESC]     // Para búsqueda basada en ubicación
job_offers: [status, searchableSkills, createdAt DESC] // Para búsqueda basada en habilidades

job_applications: [jobId, status, appliedAt DESC]      // Para gestión de postulaciones de trabajo
job_applications: [applicantId, status, appliedAt DESC] // Para postulaciones del usuario
job_applications: [jobOwnerId, status, appliedAt DESC] // Para gestión de postulaciones del empleador

companies: [ownerId, createdAt DESC]                    // Para empresas del usuario
notifications: [userId, read, createdAt DESC]          // Para notificaciones del usuario
```

#### 2. Desnormalización Estratégica (Mejores Prácticas NoSQL)
```kotlin
// Ejemplo: JobOffer incluye datos desnormalizados de empresa
// Esto evita la necesidad de búsquedas costosas en documentos
data class JobOffer(
    // ... otros campos
    
    // Desnormalizado para rendimiento de lectura (optimización NoSQL)
    val companyName: String = "",      // De Company.name
    val companyLogoUrl: String = "",   // De Company.logoUrl
    val companyLocation: String = "",  // De Company.city
    val ownerName: String = "",        // De User.name
    val ownerEmail: String = ""        // De User.email
)

// Cuando cambian los datos de Company, actualizar todas las JobOffers relacionadas
// Esto se maneja via Cloud Functions u operaciones por lotes
```

#### 3. Patrones de Consulta Firestore
```kotlin
// ✅ BUENO: Consultas simples que Firestore puede optimizar
fun getActiveJobs(): Query {
    return firestore.collection("job_offers")
        .whereEqualTo("status", "ACTIVE")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(20)
}

// ✅ BUENO: Usar datos desnormalizados para filtrado
fun getJobsByCompany(companyName: String): Query {
    return firestore.collection("job_offers")
        .whereEqualTo("status", "ACTIVE")
        .whereEqualTo("companyName", companyName) // Campo desnormalizado
        .orderBy("createdAt", Query.Direction.DESCENDING)
}

// ❌ MALO: Consultas complejas que requieren múltiples lecturas de colecciones
// En su lugar, usa datos desnormalizados u operaciones por lotes
```

#### 4. Listeners en Tiempo Real (Fortaleza de Firestore)
```kotlin
// Escuchar actualizaciones en tiempo real de postulaciones laborales
fun listenToJobApplications(jobId: String): Flow<List<JobApplication>> {
    return firestore.collection("job_applications")
        .whereEqualTo("jobId", jobId)
        .orderBy("appliedAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { it.toObjects<JobApplication>() }
}
```

#### 5. Integración de Reglas de Seguridad Firestore

### Estrategia de Migración

#### Fase 1: Limpieza de Modelos Principales
1. Renombrar `Applicant` a `JobApplication`
2. Consolidar `Job` y `JobAd` en `JobOffer`
3. Agregar relaciones de referencia faltantes

#### Fase 2: Normalización de Datos
1. Remover datos redundantes de empresa de las ofertas de trabajo
2. Implementar consultas de relación apropiadas
3. Agregar índices compuestos

#### Fase 3: Características Mejoradas
1. Agregar estados de flujo de trabajo de postulaciones
2. Implementar sistema de notificaciones
3. Agregar análisis y reportes

### Aplicación de Lógica de Negocio

#### 1. Reglas de Postulación
```kotlin
class ApplicationRules {
    fun canUserApplyToJob(userId: String, jobOffer: JobOffer): Boolean {
        // Prevenir auto-postulación
        if (jobOffer.ownerId == userId) return false
        
        // Verificar si el trabajo está aceptando postulaciones
        if (!jobOffer.status.canReceiveApplications()) return false
        
        // Verificar fecha límite
        jobOffer.deadline?.let { deadline ->
            if (System.currentTimeMillis() > deadline) return false
        }
        
        return true
    }
    
    fun canUserManageApplication(userId: String, application: JobApplication, jobOffer: JobOffer): Boolean {
        // El propietario del trabajo puede gestionar postulaciones
        return jobOffer.ownerId == userId
    }
}
```

#### 2. Reglas de Gestión de Trabajos
```kotlin
class JobOfferRules {
    fun canUserEditJob(userId: String, jobOffer: JobOffer): Boolean {
        return jobOffer.ownerId == userId && jobOffer.status.canEdit()
    }
    
    fun canUserDeleteJob(userId: String, jobOffer: JobOffer): Boolean {
        return jobOffer.ownerId == userId && jobOffer.status.canDelete()
    }
}
```

### Consideraciones de Rendimiento

#### 1. Paginación
- Implementar paginación basada en cursor para listados de trabajos
- Usar limit/offset para gestión de postulaciones

#### 2. Actualizaciones en Tiempo Real
- Usar listeners de Firestore para cambios de estado de postulaciones
- Implementar notificaciones push para actualizaciones importantes

#### 3. Optimización de Búsqueda
- Considerar Algolia para búsqueda avanzada de trabajos
- Implementar filtrado del lado del cliente para conjuntos de datos pequeños

## Recomendaciones de Implementación

### 1. Patrón Repository de Firestore
```kotlin
interface JobOfferRepository {
    // Crear con datos desnormalizados
    suspend fun createJobOffer(jobOffer: JobOffer, company: Company, owner: User): String
    
    // Operaciones de lectura (optimizadas para Firestore)
    suspend fun getJobOfferById(id: String): JobOffer?
    suspend fun getJobOffersByOwner(ownerId: String): Flow<List<JobOffer>>
    suspend fun getJobOffersByCompany(companyId: String): Flow<List<JobOffer>>
    suspend fun searchJobOffers(filters: JobSearchFilters): Flow<List<JobOffer>>
    
    // Actualizar con sincronización de desnormalización
    suspend fun updateJobOffer(jobOffer: JobOffer)
    suspend fun updateDenormalizedCompanyData(companyId: String, companyData: CompanyDenormalizedData)
    
    // Eliminar con limpieza
    suspend fun deleteJobOffer(id: String, cleanupApplications: Boolean = true)
}

interface JobApplicationRepository {
    // Crear con datos desnormalizados de trabajo y usuario
    suspend fun createApplication(
        application: JobApplication, 
        jobOffer: JobOffer, 
        applicant: User, 
        applicantProfile: UserProfile?
    ): String
    
    // Listeners en tiempo real (fortaleza de Firestore)
    fun getApplicationsByJob(jobId: String): Flow<List<JobApplication>>
    fun getApplicationsByUser(userId: String): Flow<List<JobApplication>>
    fun getApplicationsByJobOwner(ownerId: String): Flow<List<JobApplication>>
    
    // Actualizaciones de estado por lotes
    suspend fun updateApplicationStatus(
        applicationId: String, 
        status: ApplicationStatus, 
        changedBy: String, 
        reason: String?
    )
    suspend fun batchUpdateApplicationStatuses(
        applicationIds: List<String>, 
        status: ApplicationStatus, 
        changedBy: String
    )
    
    // Prevenir postulaciones duplicadas
    suspend fun checkUserAppliedToJob(userId: String, jobId: String): Boolean
}

// Clases de datos específicas de Firestore para desnormalización
data class CompanyDenormalizedData(
    val name: String,
    val logoUrl: String,
    val location: String,
    val size: String
)

data class UserDenormalizedData(
    val name: String,
    val email: String,
    val profileImage: String,
    val profession: String
)
```

### 2. Estructura de ViewModel
```kotlin
class JobManagementViewModel(
    private val jobRepository: JobOfferRepository,
    private val companyRepository: CompanyRepository,
    private val applicationRepository: JobApplicationRepository
) : ViewModel() {
    
    fun createJobOffer(jobData: JobOfferRequest) {
        // Validar que el usuario posee la empresa
        // Crear oferta de trabajo
        // Enviar notificaciones
    }
    
    fun getJobApplications(jobId: String) {
        // Obtener postulaciones con detalles del postulante
        // Agrupar por estado
    }
}
```

## Conclusión

La **arquitectura de modelo de datos NoSQL de Firestore** recomendada proporciona:

1. **Diseño Basado en Documentos**: Optimizado para la estructura documento-colección de Firestore
2. **Desnormalización Estratégica**: Optimización del rendimiento de lectura mediante duplicación cuidadosamente planificada de datos
3. **Capacidades en Tiempo Real**: Aprovecha los listeners en tiempo real de Firestore para actualizaciones en vivo
4. **Relaciones NoSQL Escalables**: Referencias de documentos que escalan horizontalmente
5. **Optimización de Consultas**: Diseñado para las limitaciones y fortalezas de consulta de Firestore
6. **Aplicación de Lógica de Negocio**: Las reglas previenen operaciones inválidas mientras mantienen flexibilidad NoSQL

### Beneficios Clave de Firestore:
- ✅ **Sin JOINs Complejos**: Los datos desnormalizados eliminan la necesidad de consultas costosas multi-colección
- ✅ **Actualizaciones en Tiempo Real**: Listeners integrados para actualizaciones en vivo del estado de postulaciones
- ✅ **Escalado Horizontal**: La estructura basada en documentos escala automáticamente
- ✅ **Soporte Offline**: Las capacidades offline de Firestore funcionan perfectamente con este diseño
- ✅ **Reglas de Seguridad**: Control de acceso granular a nivel de documento

### Compromisos NoSQL Abordados:
- **Consistencia de Datos**: Gestionada a través de operaciones por lotes y Cloud Functions
- **Sobrecarga de Almacenamiento**: La desnormalización estratégica equilibra rendimiento vs. costo de almacenamiento
- **Complejidad de Actualización**: Patrones claros para mantener integridad de datos desnormalizados

Esta arquitectura soporta todos los requisitos de negocio actuales mientras proporciona una **base optimizada para NoSQL** para futuras mejoras como:
- Notificaciones en tiempo real
- Búsqueda avanzada con integración de Algolia  
- Paneles de análisis y reportes
- Experiencias móviles offline-first

La migración debe hacerse incrementalmente, comenzando con las colecciones más críticas (users, companies, job_offers) y moviéndose gradualmente a la estructura desnormalizada optimizada para minimizar la disrupción mientras se maximizan las capacidades de Firestore.