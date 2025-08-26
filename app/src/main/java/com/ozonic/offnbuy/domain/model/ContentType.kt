package com.ozonic.offnbuy.domain.model

/**
 * Defines the different types of static content screens available in the app.
 * Each content type has a unique route used for navigation.
 */
enum class ContentType(val route: String) {
    TermsAndConditions("TermsAndConditions"),
    PrivacyPolicy("PrivacyPolicy"),
    HelpAndSupport("HelpAndSupport");
}