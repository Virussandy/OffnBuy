package com.ozonic.offnbuy.domain.repository

import com.ozonic.offnbuy.data.remote.dto.ApiResponse

/**
 * Interface for the link generation repository.
 * This repository is responsible for communicating with the external API
 * to generate affiliate links from product URLs.
 */
interface LinkRepository {

    /**
     * Sends a product link to the remote API to generate an affiliate link.
     *
     * @param productLink The original product URL to be converted.
     * @return An [ApiResponse] containing either the successfully generated link or an error message.
     */
    suspend fun generateLink(productLink: String): ApiResponse
}