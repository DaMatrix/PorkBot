/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016 DaPorkchop_
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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Icon containing a base64 encoded jpeg image.
 * <br>Used to different base64 images in the Discord api.
 * <br>Example: {@link net.dv8tion.jda.core.managers.AccountManager#setAvatar(Icon)}.
 *
 * @since 3.0
 */
public class Icon {
    //Not needed by porkbot: protected final String encoding;

    protected Icon(String base64Encoding) {
        //Not needed by porkbot: this.encoding = "data:image/jpeg;base64," + base64Encoding;
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.File File}.
     * <br>We here read the specified File and forward the retrieved byte data to {@link #from(byte[])}.
     *
     * @param file An existing, not-null file.
     * @return An Icon instance representing the specified File
     * @throws IllegalArgumentException if the provided file is either null or does not exist
     * @throws IOException              if there is a problem while reading the file.
     * @see net.dv8tion.jda.core.utils.IOUtil#readFully(File)
     */
    public static Icon from(File file) throws IOException {
        Checks.notNull(file, "Provided File");
        Checks.check(file.exists(), "Provided file does not exist!");

        return from(IOUtil.readFully(file));
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.InputStream InputStream}.
     * <br>We here read the specified InputStream and forward the retrieved byte data to {@link #from(byte[])}.
     *
     * @param stream A not-null InputStream.
     * @return An Icon instance representing the specified InputStream
     * @throws IllegalArgumentException if the provided stream is null
     * @throws IOException              If the first byte cannot be read for any reason other than the end of the file,
     *                                  if the input stream has been closed, or if some other I/O error occurs.
     * @see net.dv8tion.jda.core.utils.IOUtil#readFully(InputStream)
     */
    public static Icon from(InputStream stream) throws IOException {
        Checks.notNull(stream, "InputStream");

        return from(IOUtil.readFully(stream));
    }

    /**
     * Creates an {@link Icon Icon} with the specified image data.
     *
     * @param data not-null image data bytes.
     * @return An Icon instance representing the specified image data
     * @throws IllegalArgumentException if the provided data is null
     */
    public static Icon from(byte[] data) {
        Checks.notNull(data, "Provided byte[]");

        try {
            return new Icon(new String(Base64.getEncoder().encode(data), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }

    /**
     * The base64 encoded data for this Icon
     *
     * @return String representation of the encoded data for this icon
     */
    public String getEncoding() {
        //Not needed by porkbot: return encoding;
        return null;
    }
}
