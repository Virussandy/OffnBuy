package com.ozonic.offnbuy.model

sealed class SearchResultStatus {
    object Idle : SearchResultStatus()
    object NoResults : SearchResultStatus()
    object ResultsFound : SearchResultStatus()
    object EndReached : SearchResultStatus()
}