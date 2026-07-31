package com.perru.lifeline.util

import com.perru.lifeline.domain.model.BloodGroup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BloodCompatibilityTest {

    @Test
    fun `O negative can give to everyone`() {
        BloodGroup.entries.forEach { target ->
            assertTrue(
                "O- should be able to give to ${target.label}",
                BloodCompatibility.isCompatible(BloodGroup.O_NEG, target)
            )
        }
    }

    @Test
    fun `AB positive can only give to AB positive`() {
        BloodGroup.entries.forEach { target ->
            if (target == BloodGroup.AB_POS) {
                assertTrue(BloodCompatibility.isCompatible(BloodGroup.AB_POS, target))
            } else {
                assertFalse(
                    "AB+ should NOT be able to give to ${target.label}",
                    BloodCompatibility.isCompatible(BloodGroup.AB_POS, target)
                )
            }
        }
    }

    @Test
    fun `Same blood group is always compatible`() {
        BloodGroup.entries.forEach { group ->
            assertTrue(
                "${group.label} should be compatible with itself",
                BloodCompatibility.isCompatible(group, group)
            )
        }
    }

    @Test
    fun `O positive can give to all positives but no negatives`() {
        val positives = setOf(BloodGroup.O_POS, BloodGroup.A_POS, BloodGroup.B_POS, BloodGroup.AB_POS)
        val negatives = setOf(BloodGroup.O_NEG, BloodGroup.A_NEG, BloodGroup.B_NEG, BloodGroup.AB_NEG)

        positives.forEach { target ->
            assertTrue(BloodCompatibility.isCompatible(BloodGroup.O_POS, target))
        }
        negatives.forEach { target ->
            assertFalse(BloodCompatibility.isCompatible(BloodGroup.O_POS, target))
        }
    }
}
