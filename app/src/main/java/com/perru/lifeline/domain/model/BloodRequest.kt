package com.perru.lifeline.domain.model

import com.perru.lifeline.domain.model.BloodGroup

enum class BloodComponent { WHOLE_BLOOD, PLATELETS, PLASMA }

enum class UrgencyLevel(val label: String) {
    CRITICAL("Critical"), HIGH("High"), MODERATE("Moderate")
}

enum class RequestStatus { ACTIVE, FULFILLED, EXPIRED }

data class BloodRequest(
    val id: String = "",
    val hospitalUid: String = "",
    val hospitalName: String = "",
    val hospitalCity: String = "",
    val bloodGroup: BloodGroup = BloodGroup.O_POS,
    val component: BloodComponent = BloodComponent.WHOLE_BLOOD,
    val unitsNeeded: Int = 1,
    val unitsPledged: Int = 0,
    val urgency: UrgencyLevel = UrgencyLevel.MODERATE,
    val contactPhone: String = "",
    val notes: String = "",
    val verificationImageUrl: String = "",
    val status: RequestStatus = RequestStatus.ACTIVE,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class Pledge(
    val id: String = "",
    val requestId: String = "",
    val donorUid: String = "",
    val donorName: String = "",
    val donorBloodGroup: BloodGroup = BloodGroup.O_POS,
    val pledgedAtMillis: Long = System.currentTimeMillis()
)