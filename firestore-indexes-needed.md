# Índices de Firestore Necesarios

## Errores de Índice Encontrados:

### 1. Jobs Collection:
```
The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/hirelink-2025/firestore/indexes?create_composite=Ckpwcm9qZWN0cy9oaXJlbGluay0yMDI1L2RhdGFiYXNlcy8oZGVmYXVsdCkvY29sbGVjdGlvbkdyb3Vwcy9qb2JzL2luZGV4ZXMvXxABGgoKBnN0YXR1cxABGg4KCnBvc3RlZERhdGUQAhoMCghfX25hbWVfXxAC
```
- **Colección**: `jobs`
- **Campos**: `status` (Ascending), `postedDate` (Descending), `__name__` (Descending)

### 2. Applications Collection:
```
The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/hirelink-2025/firestore/indexes?create_composite=ClJwcm9qZWN0cy9oaXJlbGluay0yMDI1L2RhdGFiYXNlcy8oZGVmYXVsdCkvY29sbGVjdGlvbkdyb3Vwcy9hcHBsaWNhdGlvbnMvaW5kZXhlcy9fEAEaCQoFam9iSWQQARoNCglhcHBsaWVkQXQQAhoMCghfX25hbWVfXxAC
```
- **Colección**: `applications`
- **Campos**: `jobId` (Ascending), `appliedAt` (Descending), `__name__` (Descending)

## Solución Implementada:
✅ **Modificado SearchViewModel** para usar consultas simples sin índices compuestos
✅ **Modificado FirestoreService** para eliminar `orderBy` en consultas compuestas
✅ **Implementado ordenamiento en el cliente** usando `sortedByDescending { it.createdAt }`
✅ **Solucionado getApplicationsByJobId()** - eliminado `orderBy("appliedAt")` y agregado ordenamiento en cliente

## Optimizaciones Realizadas:

### 1. SearchViewModel.kt
- Consulta directa a Firestore sin usar FirestoreService.getActiveJobs()
- Filtrado por status únicamente en servidor
- Ordenamiento por createdAt en el cliente
- Filtrado por ubicación en el cliente para evitar índices compuestos

### 2. FirestoreService.kt
- Eliminado `orderBy("postedDate")` de múltiples métodos
- Reemplazado con ordenamiento en cliente usando `createdAt`
- Optimizado `getActiveJobs()`, `searchJobs()`, `getJobsByCompany()`, etc.
- **Nuevo**: Eliminado `orderBy("appliedAt")` de `getApplicationsByJobId()`
- **Nuevo**: Agregado ordenamiento por `appliedAt` en el cliente

### 3. Estrategia de Consultas:
- **Servidor**: Filtros simples (status, title, etc.)
- **Cliente**: Ordenamiento, filtros complejos, paginación

## Enlaces Útiles:
- [Firestore Console](https://console.firebase.google.com/project/hirelink-2025/firestore)
- [Guía de Índices](https://firebase.google.com/docs/firestore/query-data/indexing)

## Notas:
- Los cambios eliminan la necesidad de índices compuestos
- El rendimiento se mantiene adecuado para datasets pequeños-medianos
- Si necesitas mejor rendimiento con grandes datasets, crear los índices recomendados