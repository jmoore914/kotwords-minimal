package com.jeffpdavidson.kotwords.formats

import com.jeffpdavidson.kotwords.model.Acrostic
import com.jeffpdavidson.kotwords.readStringResource
import com.jeffpdavidson.kotwords.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApzFileTest {
    @Test
    fun parseApz() = runTest {
        val acrostic = Apz.fromXmlString(readStringResource(ApzFileTest::class, "acrostic/acrostic.apz")).toAcrostic(
            completionMessage = "",
            includeAttribution = true,
        )
        val expected = Acrostic(
            title = "Test title",
            creator = "Test creator",
            copyright = "Test copyright",
            description = "Test description",
            suggestedWidth = null,
            solution = "ACRO-ST IC",
            gridKey = listOf(listOf(2, 1, 3), listOf(5, 6, 4, 7, 8)),
            clues = listOf("Clue 1", "Clue 2"),
            answers = listOf("CAR", "STOIC"),
            completionMessage = "MADE UP QUOTE\n\nAcrostic!",
            includeAttribution = true,
        )
        assertEquals(expected, acrostic)
    }
}