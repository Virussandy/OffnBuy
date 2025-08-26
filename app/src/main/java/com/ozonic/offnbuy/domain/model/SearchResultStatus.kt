package com.ozonic.offnbuy.domain.model

/**
 * A sealed class representing the various states of a search result.
 * This is used by the ViewModel to communicate the current status of a search operation
 * to the UI, allowing the UI to display appropriate indicators to the user.
 */
sealed class SearchResultStatus {
    /** The initial state before any search has been performed. */
    object Idle : SearchResultStatus()

    /** The state when a search has been completed, but no results were found. */
    object NoResults : SearchResultStatus()

    /** The state when a search has successfully returned results. */
    object ResultsFound : SearchResultStatus()

    /** The state when all available search results for a query have been loaded (pagination end). */
    object EndReached : SearchResultStatus()
}