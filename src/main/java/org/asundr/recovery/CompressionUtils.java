/*
 * Copyright (c) 2025, Arun <trade-tracker-plugin.acwel@dralias.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.asundr.recovery;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils
{
    private final static int BUFFER_SIZE = 1024 * 32;

    public static byte[] compress(final String str)
    {
        final byte[] input = str.getBytes();
        final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setStrategy(Deflater.FILTERED);
        deflater.setInput(input);
        deflater.finish();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(input.length);
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (!deflater.finished())
        {
            final int len = deflater.deflate(buffer);
            byteArrayOutputStream.write(buffer, 0, len);
        }
        deflater.end();
        return byteArrayOutputStream.toByteArray();
    }

    public static String decompress(final byte[] compressed)
    {
        final Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(compressed.length);
        final byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            while (!inflater.finished())
            {
                final int len = inflater.inflate(buffer);
                byteArrayOutputStream.write(buffer, 0, len);
            }
        }
        catch (Exception e)
        {
            System.out.println("ERROR: failed to decompress trade history save data");
        }
        finally
        {
            inflater.end();
        }
        return byteArrayOutputStream.toString();
    }

    // Compresses the passed string, then encodes in Base64. ~16% of original size
    public static String compressToEncode(final String str)
    {
        return Base64.getEncoder().encodeToString(compress(str));
    }

    // Given a compressed string encoded in Base64, returns the original string
    public static String decompressFromEncode(final String compressed)
    {
        return decompress(Base64.getDecoder().decode(compressed));
    }

}
