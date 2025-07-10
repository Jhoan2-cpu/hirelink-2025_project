package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Company
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompanyRepository {
    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val companiesList = mutableListOf<Company>()

    init {
        // Initialize with sample data
        loadSampleData()
    }

    private fun loadSampleData() {
        val sampleCompanies = listOf(
            Company(
                id = "1",
                name = "TechSolutions S.A.C.",
                type = "Tecnología",
                description = "Empresa líder en desarrollo de software y soluciones tecnológicas innovadoras para el mercado peruano.",
                size = "50-100",
                foundedYear = 2020,
                address = "Av. Javier Prado Este 123",
                city = "Lima",
                country = "Perú",
                phone = "987654321",
                email = "info@techsolutions.com",
                website = "www.techsolutions.com",
                employeeCount = 75,
                activeJobsCount = 3,
                ownerId = "user1"
            ),
            Company(
                id = "2",
                name = "Innovate Corp",
                type = "Consultoría",
                description = "Consultoría especializada en transformación digital y gestión empresarial.",
                size = "10-50",
                foundedYear = 2018,
                address = "Calle Los Incas 456",
                city = "Arequipa",
                country = "Perú",
                phone = "123456789",
                email = "contact@innovate.com",
                website = "www.innovate.com",
                employeeCount = 25,
                activeJobsCount = 1,
                ownerId = "user1"
            ),
            Company(
                id = "3",
                name = "DigitalWorks",
                type = "Marketing Digital",
                description = "Agencia de marketing digital especializada en estrategias de crecimiento online.",
                size = "1-10",
                foundedYear = 2022,
                address = "Jr. Junín 789",
                city = "Cusco",
                country = "Perú",
                phone = "555666777",
                email = "hello@digitalworks.pe",
                website = "www.digitalworks.pe",
                employeeCount = 8,
                activeJobsCount = 2,
                ownerId = "user1"
            )
        )
        
        companiesList.addAll(sampleCompanies)
        _companies.value = companiesList.toList()
    }

    suspend fun getAllCompanies(): List<Company> {
        return companiesList.toList()
    }

    suspend fun getCompaniesByOwner(ownerId: String): List<Company> {
        return companiesList.filter { it.ownerId == ownerId }
    }

    suspend fun getCompanyById(id: String): Company? {
        return companiesList.find { it.id == id }
    }

    suspend fun addCompany(company: Company): Boolean {
        return try {
            companiesList.add(0, company) // Add to beginning
            _companies.value = companiesList.toList()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCompany(company: Company): Boolean {
        return try {
            val index = companiesList.indexOfFirst { it.id == company.id }
            if (index != -1) {
                companiesList[index] = company
                _companies.value = companiesList.toList()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCompany(companyId: String): Boolean {
        return try {
            val removed = companiesList.removeIf { it.id == companyId }
            if (removed) {
                _companies.value = companiesList.toList()
            }
            removed
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchCompanies(query: String): List<Company> {
        return companiesList.filter { company ->
            company.name.contains(query, ignoreCase = true) ||
            company.type.contains(query, ignoreCase = true) ||
            company.city.contains(query, ignoreCase = true) ||
            company.description.contains(query, ignoreCase = true)
        }
    }

    suspend fun getCompaniesByType(type: String): List<Company> {
        return companiesList.filter { it.type.equals(type, ignoreCase = true) }
    }

    suspend fun getCompaniesByCity(city: String): List<Company> {
        return companiesList.filter { it.city.equals(city, ignoreCase = true) }
    }
}