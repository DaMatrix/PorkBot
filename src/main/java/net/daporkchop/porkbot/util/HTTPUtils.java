/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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

import com.google.common.base.Charsets;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("deprecation")
public class HTTPUtils {
    private static HttpURLConnection createUrlConnection(@NonNull URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setUseCaches(false);
        return connection;
    }

    /**
     * Performs a POST request to the specified URL and returns the result.
     * <p/>
     * The POST data will be encoded in UTF-8 as the specified contentType. The response will be parsed as UTF-8.
     * If the server returns an error but still provides a body, the body will be returned as normal.
     * If the server returns an error without any body, a relevant {@link java.io.IOException} will be thrown.
     *
     * @param url         URL to submit the POST request to
     * @param post        POST data in the correct format to be submitted
     * @param contentType Content type of the POST data
     * @return Raw text response from the server
     * @throws IOException The request was not successful
     */
    public static String performPostRequest(@NonNull URL url, @NonNull String post, @NonNull String contentType) throws IOException {
        final HttpURLConnection connection = createUrlConnection(url);
        final byte[] postAsBytes = post.getBytes(Charsets.UTF_8);

        connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
        connection.setDoOutput(true);

        OutputStream outputStream = null;
        try {
            outputStream = connection.getOutputStream();
            IOUtils.write(postAsBytes, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return sendRequest(connection);
    }

    /**
     * Performs a POST request to the specified URL and returns the result.
     * <p/>
     * The POST data will be encoded in UTF-8 as the specified contentType. The response will be parsed as UTF-8.
     * If the server returns an error but still provides a body, the body will be returned as normal.
     * If the server returns an error without any body, a relevant {@link java.io.IOException} will be thrown.
     *
     * @param url         URL to submit the POST request to
     * @param post        POST data in the correct format to be submitted
     * @param contentType Content type of the POST data
     * @return Raw text response from the server
     * @throws IOException The request was not successful
     */
    public static String performPostRequestWithAuth(@NonNull URL url, @NonNull String post, @NonNull String contentType, @NonNull String auth) throws IOException {
        final HttpURLConnection connection = createUrlConnection(url);
        final byte[] postAsBytes = post.getBytes(Charsets.UTF_8);

        connection.setRequestProperty("Authorization", auth);
        connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
        connection.setDoOutput(true);

        OutputStream outputStream = null;
        try {
            outputStream = connection.getOutputStream();
            IOUtils.write(postAsBytes, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return sendRequest(connection);
    }

    /**
     * Performs a GET request to the specified URL and returns the result.
     * <p/>
     * The response will be parsed as UTF-8.
     * If the server returns an error but still provides a body, the body will be returned as normal.
     * If the server returns an error without any body, a relevant {@link java.io.IOException} will be thrown.
     *
     * @param url URL to submit the GET request to
     * @return Raw text response from the server
     * @throws IOException The request was not successful
     */
    public static String performGetRequest(@NonNull URL url, int max) throws IOException {
        final HttpURLConnection connection = createUrlConnection(url);
        if (connection.getContentLength() > max) {
            throw new IOException("Content too big!");
        }
        return sendRequest(connection);
    }

    /**
     * Creates a {@link URL} with the specified string, throwing an {@link java.lang.Error} if the URL was malformed.
     * <p/>
     * This is just a wrapper to allow URLs to be created in constants, where you know the URL is valid.
     *
     * @param url URL to construct
     * @return URL constructed
     */
    public static URL constantURL(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException ex) {
            throw new Error("Couldn't create constant for " + url, ex);
        }
    }

    private static String sendRequest(HttpURLConnection connection) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            final String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            return result;
        } catch (final IOException e) {
            IOUtils.closeQuietly(inputStream);
            inputStream = connection.getErrorStream();

            if (inputStream != null) {
                final String result = IOUtils.toString(inputStream, Charsets.UTF_8);
                return result;
            } else {
                throw e;
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
