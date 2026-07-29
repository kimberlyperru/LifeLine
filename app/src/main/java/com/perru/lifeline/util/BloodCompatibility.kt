package com.perru.lifeline.util

import com.perru.lifeline.domain.model.BloodGroup

/**
 * Standard donor-compatibility rules: which blood groups a given donor
 * can safely donate to (whole blood rules — used for filtering the SOS feed).
 */
object BloodCompatibility {

    private val donorCanGiveTo: Map<BloodGroup, Set<BloodGroup>> = mapOf(
        BloodGroup.O_NEG to setOf(
            BloodGroup.O_NEG, BloodGroup.O_POS, BloodGroup.A_NEG, BloodGroup.A_POS,
            BloodGroup.B_NEG, BloodGroup.B_POS, BloodGroup.AB_NEG, BloodGroup.AB_POS
        ), // universal donor
        BloodGroup.O_POS to setOf(
            BloodGroup.O_POS, BloodGroup.A_POS, BloodGroup.B_POS, BloodGroup.AB_POS
        ),
        BloodGroup.A_NEG to setOf(
            BloodGroup.A_NEG, BloodGroup.A_POS, BloodGroup.AB_NEG, BloodGroup.AB_POS
        ),
        BloodGroup.A_POS to setOf(BloodGroup.A_POS, BloodGroup.AB_POS),
        BloodGroup.B_NEG to setOf(
            BloodGroup.B_NEG, BloodGroup.B_POS, BloodGroup.AB_NEG, BloodGroup.AB_POS
        ),
        BloodGroup.B_POS to setOf(BloodGroup.B_POS, BloodGroup.AB_POS),
        BloodGroup.AB_NEG to setOf(BloodGroup.AB_NEG, BloodGroup.AB_POS),
        BloodGroup.AB_POS to setOf(BloodGroup.AB_POS) // universal recipient only receives
    )

    /** Returns true if [donor] is eligible to donate to a request needing [requested]. */
    fun isCompatible(donor: BloodGroup, requested: BloodGroup): Boolean =
        donorCanGiveTo[donor]?.contains(requested) == true

    /** Eligibility window: 56 days between whole-blood donations (standard guideline). */
    const val ELIGIBILITY_WINDOW_DAYS = 56
}
