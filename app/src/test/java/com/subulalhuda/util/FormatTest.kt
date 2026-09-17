package com.subulalhuda.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the Arabic formatting helpers in Format.kt.
 *
 * Only pure, deterministic branches are pinned here. [formatArabicDate] with a
 * parseable instant depends on the host timezone, so only its fallback branch
 * is asserted.
 */
class FormatTest {

    @Test
    fun videoCountLabel_singularForOne() {
        assertEquals("فيديو واحد", videoCountLabel(1))
    }

    @Test
    fun videoCountLabel_dualForTwo() {
        assertEquals("فيديوان", videoCountLabel(2))
    }

    @Test
    fun videoCountLabel_pluralForThreeToTen() {
        assertEquals("3 فيديوهات", videoCountLabel(3))
        assertEquals("10 فيديوهات", videoCountLabel(10))
    }

    @Test
    fun videoCountLabel_singularForElevenAndAbove() {
        assertEquals("11 فيديو", videoCountLabel(11))
        assertEquals("100 فيديو", videoCountLabel(100))
    }

    @Test
    fun formatArabicDate_fallsBackToRawInputWhenUnparseable() {
        assertEquals("2024-01-15", formatArabicDate("2024-01-15"))
        assertEquals("bad", formatArabicDate("bad"))
    }
}
