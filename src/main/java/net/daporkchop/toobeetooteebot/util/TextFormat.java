/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2017 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.daporkchop.toobeetooteebot.util;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A bunch of utilities for dealing with Minecraft color codes
 * Totally not skidded from Nukkit
 * sorry nukkit
 * deal with it
 * kek
 */
public enum TextFormat {
    /**
     * Represents black.
     */
    BLACK('0', 0x00),
    /**
     * Represents dark blue.
     */
    DARK_BLUE('1', 0x1),
    /**
     * Represents dark green.
     */
    DARK_GREEN('2', 0x2),
    /**
     * Represents dark blue (aqua).
     */
    DARK_AQUA('3', 0x3),
    /**
     * Represents dark red.
     */
    DARK_RED('4', 0x4),
    /**
     * Represents dark purple.
     */
    DARK_PURPLE('5', 0x5),
    /**
     * Represents gold.
     */
    GOLD('6', 0x6),
    /**
     * Represents gray.
     */
    GRAY('7', 0x7),
    /**
     * Represents dark gray.
     */
    DARK_GRAY('8', 0x8),
    /**
     * Represents blue.
     */
    BLUE('9', 0x9),
    /**
     * Represents green.
     */
    GREEN('a', 0xA),
    /**
     * Represents aqua.
     */
    AQUA('b', 0xB),
    /**
     * Represents red.
     */
    RED('c', 0xC),
    /**
     * Represents light purple.
     */
    LIGHT_PURPLE('d', 0xD),
    /**
     * Represents yellow.
     */
    YELLOW('e', 0xE),
    /**
     * Represents white.
     */
    WHITE('f', 0xF),
    /**
     * Makes the text obfuscated.
     */
    OBFUSCATED('k', 0x10, true),
    /**
     * Makes the text bold.
     */
    BOLD('l', 0x11, true),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', 0x12, true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', 0x13, true),
    /**
     * Makes the text italic.
     */
    ITALIC('o', 0x14, true),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r', 0x15);

    /**
     * The special character which prefixes all format codes. Use this if
     * you need to dynamically convert format codes from your custom format.
     */
    public static final char ESCAPE = '\u00A7';

    private static final Pattern CLEAN_PATTERN = Pattern.compile("(?i)" + String.valueOf(ESCAPE) + "[0-9A-FK-OR]");
    private static final Map<Integer, TextFormat> BY_ID = Maps.newTreeMap();
    private static final Map<Character, TextFormat> BY_CHAR = new HashMap<>();

    static {
        for (TextFormat color : values()) {
            BY_ID.put(color.intCode, color);
            BY_CHAR.put(color.code, color);
        }
    }

    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final String toString;

    TextFormat(char code, int intCode) {
        this(code, intCode, false);
    }

    TextFormat(char code, int intCode, boolean isFormat) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.toString = new String(new char[]{ESCAPE, code});
    }

    /**
     * Gets the TextFormat represented by the specified format code.
     *
     * @param code Code to check
     * @return Associative {@link TextFormat} with the given code,
     * or null if it doesn't exist
     */
    public static TextFormat getByChar(char code) {
        return BY_CHAR.get(code);
    }

    /**
     * Gets the TextFormat represented by the specified format code.
     *
     * @param code Code to check
     * @return Associative {@link TextFormat} with the given code,
     * or null if it doesn't exist
     */
    public static TextFormat getByChar(String code) {
        if (code == null || code.length() <= 1) {
            return null;
        }

        return BY_CHAR.get(code.charAt(0));
    }

    /**
     * Cleans the given message of all format codes.
     *
     * @param input String to clean.
     * @return A copy of the input string, without any formatting.
     */
    public static String clean(final String input) {
        if (input == null) {
            return null;
        }

        return CLEAN_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Translates a string using an alternate format code character into a
     * string that uses the internal TextFormat.ESCAPE format code
     * character. The alternate format code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altFormatChar   The alternate format code character to replace. Ex: &
     * @param textToTranslate Text containing the alternate format code character.
     * @return Text containing the TextFormat.ESCAPE format code character.
     */
    public static String colorize(char altFormatChar, String textToTranslate) {
        return colorize(altFormatChar, textToTranslate, false);
    }

    /**
     * Translates a string using an alternate format code character into a
     * string that uses the internal TextFormat.ESCAPE format code
     * character. The alternate format code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altFormatChar   The alternate format code character to replace. Ex: &
     * @param textToTranslate Text containing the alternate format code character.
     * @param resetFormatting If RESET should be added before a color change
     * @return Text containing the TextFormat.ESCAPE format code character.
     */
    public static String colorize(char altFormatChar, String textToTranslate, boolean resetFormatting) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altFormatChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = TextFormat.ESCAPE;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        String str = new String(b);
        if (resetFormatting) {
            Matcher match = Pattern.compile("§([0-9a-f])").matcher(str);
            int idx = 0;
            while (match.find()) {
                str = str.replace("§" + match.group(idx), "§r§" + match.group(idx));
                idx++;
            }
        }
        return str;
    }

    /**
     * Translates a string, using an ampersand (&) as an alternate format code
     * character, into a string that uses the internal TextFormat.ESCAPE format
     * code character. The alternate format code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param textToTranslate Text containing the alternate format code character.
     * @return Text containing the TextFormat.ESCAPE format code character.
     */
    public static String colorize(String textToTranslate) {
        return colorize('&', textToTranslate);
    }

    /**
     * Gets the chat color used at the end of the given input string.
     *
     * @param input Input string to retrieve the colors from.
     * @return Any remaining chat color to pass onto the next line.
     */
    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ESCAPE && index < length - 1) {
                char c = input.charAt(index + 1);
                TextFormat color = getByChar(c);

                if (color != null) {
                    result = color.toString() + result;

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the char value associated with this color
     *
     * @return A char value of this color code
     */
    public char getChar() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    /**
     * Checks if this code is a format code as opposed to a color code.
     */
    public boolean isFormat() {
        return this.isFormat;
    }

    /**
     * Checks if this code is a color code as opposed to a format code.
     */
    public boolean isColor() {
        return !this.isFormat && this != RESET;
    }
}