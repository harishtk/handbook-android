package com.handbook.app.core.domain.repository

import com.handbook.app.core.domain.model.CountryCodeModel
import kotlinx.coroutines.flow.Flow

interface CountryCodeListRepository {
    val countryCodeModelListStream: Flow<List<CountryCodeModel>>
}