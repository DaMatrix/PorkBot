/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.porkbot.util;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class Constants {
    public final boolean DEV_MODE = Boolean.valueOf(System.getProperty("porkbot.dev", "false"));

    public final Gson       GSON        = new Gson();
    public final JsonParser JSON_PARSER = new JsonParser();

    public final String UUID_CAPTURE = "([0-9A-Fa-f]{8}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{12})";

    public final String BASE_URL       = DEV_MODE ? "https://porkbot-test.daporkchop.net" : "https://porkbot.daporkchop.net";
    public final String COMMAND_PREFIX = DEV_MODE ? ",," : "..";

    public final int MAX_SEARCH_RESULTS = 5;
}
