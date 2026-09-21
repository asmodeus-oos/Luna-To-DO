package com.luna.app

import com.luna.app.data.local.converters.LunaTypeConverters
import com.luna.app.domain.model.EnergyLevel
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.SmartFilter
import com.luna.app.domain.model.TaskStatus
import com.luna.app.domain.service.SmartTaskBreakdownService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LunaBusinessLogicTest {

    @Test
    fun testSmartTaskBreakdown_Engineering() {
        val breakdown = SmartTaskBreakdownService.generateBreakdown("Fix app crash on login screen")
        assertTrue(breakdown.suggestedSubtasks.isNotEmpty())
        assertEquals(EnergyLevel.HIGH, breakdown.suggestedEnergy)
        assertEquals(Priority.P1, breakdown.suggestedPriority)
        assertTrue(breakdown.suggestedSubtasks.any { it.contains("reproduce", ignoreCase = true) || it.contains("bug", ignoreCase = true) })
    }

    @Test
    fun testSmartTaskBreakdown_Design() {
        val breakdown = SmartTaskBreakdownService.generateBreakdown("Design new mobile dashboard UI")
        assertTrue(breakdown.suggestedSubtasks.isNotEmpty())
        assertEquals(EnergyLevel.HIGH, breakdown.suggestedEnergy)
        assertEquals(Priority.P2, breakdown.suggestedPriority)
    }

    @Test
    fun testSmartTaskBreakdown_Generic() {
        val breakdown = SmartTaskBreakdownService.generateBreakdown("Reorganize garage shelving")
        assertTrue(breakdown.suggestedSubtasks.isNotEmpty())
        assertEquals(EnergyLevel.MEDIUM, breakdown.suggestedEnergy)
    }

    @Test
    fun testTypeConverters() {
        val converters = LunaTypeConverters()

        val list = listOf("android", "compose", "kotlin")
        val serialized = converters.fromStringList(list)
        val deserialized = converters.toStringList(serialized)
        assertEquals(list, deserialized)

        assertEquals(Priority.P1, converters.toPriority(Priority.P1.level))
        assertEquals(TaskStatus.DONE, converters.toTaskStatus("DONE"))
    }

    @Test
    fun testEnergyLevels() {
        assertEquals(EnergyLevel.HIGH, EnergyLevel.fromString("HIGH"))
        assertEquals(EnergyLevel.MEDIUM, EnergyLevel.fromString("MEDIUM"))
        assertEquals(EnergyLevel.LOW, EnergyLevel.fromString("LOW"))
        assertEquals(EnergyLevel.MEDIUM, EnergyLevel.fromString("UNKNOWN"))
    }

    @Test
    fun testSmartFilterEnumeration() {
        assertTrue(SmartFilter.entries.contains(SmartFilter.TODAY))
        assertTrue(SmartFilter.entries.contains(SmartFilter.OVERDUE))
        assertTrue(SmartFilter.entries.contains(SmartFilter.DEEP_WORK))
        assertTrue(SmartFilter.entries.contains(SmartFilter.QUICK_WINS))
    }
}
