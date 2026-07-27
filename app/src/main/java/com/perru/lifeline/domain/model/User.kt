package com.perru.LifeLine.domain.model

enum class UserRole { DONOR, HOSPITAL, UNSET }

enum class BloodGroup(val label: String) {
    A_POS("A+"), A_NEG("A-"),
    B_POS("B+"), B_NEG("B-"),
    AB_POS("AB+"), AB_NEG("AB-"),
    O_POS("O+"), O_NEG("O-")
}

// Firestore requires a no-arg constructor for automatic deserialization,
// hence every field has a default value below.
data class LifeLineUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.UNSET,
    val bloodGroup: BloodGroup? = null,
    val city: String = "",
    val lastDonationDateMillis: Long? = null,
    val hospitalName: String = "",
    val hospitalVerified: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)