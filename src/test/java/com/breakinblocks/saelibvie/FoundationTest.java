package com.breakinblocks.saelibvie;

import com.breakinblocks.saelibvie.color.Color;
import com.breakinblocks.saelibvie.color.ColorTables;
import com.breakinblocks.saelibvie.color.MutableColor;
import com.breakinblocks.saelibvie.math.MathUtil;
import com.breakinblocks.saelibvie.math.XZ;
import com.breakinblocks.saelibvie.text.StringUtil;
import com.breakinblocks.saelibvie.text.TimeUtil;
import com.breakinblocks.saelibvie.util.SearchTerms;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoundationTest {
    @Test
    void colorParsingAndFormatting() {
        assertSame(Color.EMPTY, Color.fromString(""));
        assertSame(Color.EMPTY, Color.fromString("transparent"));
        assertSame(Color.EMPTY, Color.fromString("#00FF0000"));
        assertEquals("#FF0000", Color.fromString("#ff0000").toString());
        assertEquals("#80FF0000", Color.fromString("#80FF0000").toString());
        assertEquals("", Color.EMPTY.toString());
        assertSame(Color.BLACK, Color.rgb(0, 0, 0));
        assertSame(Color.WHITE, Color.rgba(255, 255, 255, 255));
        assertEquals(Color.GRAY, Color.fromString("gray"));
        assertSame(Color.EMPTY, Color.fromString("#FFF"));
        assertEquals(Color.rgba(0x80112233), Color.fromJson(JsonParser.parseString("[17, 34, 51, 128]")));
        assertEquals(Color.rgb(0x112233), Color.fromJson(JsonParser.parseString("{\"red\":17,\"green\":34,\"blue\":51}")));
        assertTrue(Color.fromJson(JsonParser.parseString("{\"red\":1,\"green\":2,\"blue\":3,\"mutable\":true}")).isMutable());
        assertEquals("null", Color.EMPTY.getJson().toString());
        assertEquals(Color.rgb(0xFFAA00), ColorTables.chat(6));
        assertEquals(Color.rgb(0x5555FF), ColorTables.chat(9));
        assertEquals(Color.rgb(0xFFDA96), ColorTables.get256(255));
        assertEquals(Color.rgb(0x412000), ColorTables.get256(16));
        MutableColor mutable = Color.EMPTY.mutable();
        assertTrue(mutable.isEmpty());
        mutable.set(1, 2, 3, 255);
        assertFalse(mutable.isEmpty());
        assertEquals(Color.WHITE, Color.rgb(0x808080).withTint(Color.WHITE).withAlpha(255).lerp(Color.WHITE, 1f));
        assertSame(Color.EMPTY, Color.RED.withTint(Color.EMPTY));
    }

    @Test
    void spiralAndRegion() {
        int[][] expected = {{0, 0}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 2}};
        for (int i = 0; i < expected.length; i++) {
            XZ point = MathUtil.getSpiralPoint(i);
            assertEquals(expected[i][0], point.x(), "x at " + i);
            assertEquals(expected[i][1], point.z(), "z at " + i);
        }
        assertEquals(MathUtil.getSpiralPoint0(100), MathUtil.getSpiralPoint(100));
        assertEquals("0EA60-0EA60", XZ.of(0, 0).toRegionString());
        assertEquals(XZ.of(3, -4), XZ.of(XZ.of(3, -4).toLong()));
    }

    @Test
    void timeStrings() {
        assertEquals("500ms", TimeUtil.getTimeString(500));
        assertEquals("01:05", TimeUtil.getTimeString(65000));
        assertEquals("01:01:01", TimeUtil.getTimeString(3661000));
        assertEquals("1d 01:00:00", TimeUtil.getTimeString(90000000));
        assertEquals("-01:05", TimeUtil.getTimeString(-65000));
        assertEquals("0 seconds", TimeUtil.prettyTimeString(0));
        assertEquals("1 second", TimeUtil.prettyTimeString(1));
        assertEquals("1 minute and 1 second", TimeUtil.prettyTimeString(61));
        assertEquals("1 hour and 1 minute", TimeUtil.prettyTimeString(3661));
        assertEquals("1 day and 1 hour", TimeUtil.prettyTimeString(90000));
    }

    @Test
    void stringUtilities() {
        assertEquals("NaN", StringUtil.formatDouble(Double.NaN, true));
        assertEquals("+Inf", StringUtil.formatDouble(Double.POSITIVE_INFINITY, true));
        assertEquals("0", StringUtil.formatDouble(0, true));
        assertEquals("15K", StringUtil.formatDouble(15000, true));
        assertEquals("15.50K", StringUtil.formatDouble(15500, true));
        assertEquals("2.50M", StringUtil.formatDouble(2_500_000, true));
        assertEquals("1,234", StringUtil.formatDouble(1234, false));
        assertEquals("hello_world_", StringUtil.toSnakeCase("Hello World!"));
        assertEquals("Hello World", StringUtil.camelCaseToWords("helloWorld"));
        assertEquals("plain text", StringUtil.unformatted("&aplain &ltext"));
        assertEquals("top_left", StringUtil.getID("TOP_LEFT", StringUtil.DEFAULTS));
        assertEquals("a-b", StringUtil.getID("a-b", 0));
        assertEquals("a_b", StringUtil.getID("a-b", StringUtil.DEFAULTS));
        assertEquals("007", StringUtil.add0s(7, 999));
        assertEquals("a b", StringUtil.splitProperties("k:a%20b").get("k"));
    }

    @Test
    void searchTerms() {
        SearchTerms terms = SearchTerms.parse("@mine #forge:ores Iron");
        assertEquals(3, terms.terms().size());
        assertTrue(terms.match(Identifier.withDefaultNamespace("iron_ore"), "Iron Ore", tag -> tag.getPath().equals("ores")));
        assertFalse(terms.match(Identifier.fromNamespaceAndPath("other", "iron_ore"), "Iron Ore", tag -> true));
        assertTrue(SearchTerms.parse("").isEmpty());
        assertTrue(SearchTerms.parse("").match(Identifier.withDefaultNamespace("x"), "", tag -> false));
    }
}
