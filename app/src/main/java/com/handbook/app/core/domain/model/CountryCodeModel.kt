package com.handbook.app.core.domain.model

import java.io.Serializable

data class CountryCodeModel(
    var name : String = "",
    var dialcode : String = "",
    var isocode : String = "",
    var unicode : ArrayList<String> = ArrayList()
) : Serializable {
    companion object {
        internal val India = CountryCodeModel(
            name = "India",
            dialcode = "+91",
            isocode = "IN",
            unicode = arrayListOf("0x1F1EE", "0x1F1F3")
        )
        internal val UnitedStates = CountryCodeModel(
            name = "United States",
            dialcode = "+1",
            isocode = "US",
            unicode = arrayListOf("0x1F1FA", "0x1F1F8")
        )
    }
}