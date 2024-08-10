package com.example.surakshamitra.data

import com.google.gson.Gson

data class UserRegistrationData(
    val isAuthorized: Boolean = false,
    var authDocument: String = "",
    val username: String = "",
    val agencyName: String = "",
    val agencyType: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val totalMembers: String = "",
    val password: String = "",
    val activeStatus: Boolean = false,
    val latitude: String ="0.00",
    val longitude: String ="0.00",
) {
    fun toJson(): String {
        return Gson().toJson(this)  // No semicolon needed inside a function body
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "isAuthorized" to isAuthorized,
            "authDocument" to authDocument,
            "username" to username,
            "agencyName" to agencyName,
            "agencyType" to agencyType,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "emailAddress" to emailAddress,
            "totalMembers" to totalMembers,
            "password" to password,
            "activeStaus" to activeStatus,
            "latitude" to latitude,
            "longitude" to longitude
        )
    }
}
