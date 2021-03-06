package com.github.elopteryx.upload.internal;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

class QuotedPrintableEncodingTest {

    @Test
    void empty_decode() throws IOException {
        checkEncoding("", "");
    }

    @Test
    void plain_decode() throws IOException {
        checkEncoding("A longer sentence without special characters.", "A longer sentence without special characters.");
    }

    @Test
    void basic_encode_decode() throws IOException {
        checkEncoding("= Hello world =\r\n", "=3D Hello world =3D=0D=0A");
    }

    @Test
    void unsafe_decode() throws IOException {
        checkEncoding("=\r\n", "=3D=0D=0A");
    }

    @Test
    void unsafe_decode_lowercase() throws IOException {
        checkEncoding("=\r\n", "=3d=0d=0a");
    }

    private static void checkEncoding(final String original, final String encoded) throws IOException {
        final var encoding = new MultipartParser.QuotedPrintableEncoding(1024);
        encoding.handle(new MultipartParser.PartHandler() {

            @Override
            public void beginPart(final Headers headers) {}

            @Override
            public void data(final ByteBuffer buffer) {
                final var parserResult = new String(buffer.array(), US_ASCII).trim();
                assertEquals(parserResult, original.trim());
            }

            @Override
            public void endPart() {}

        }, ByteBuffer.wrap(encoded.getBytes(US_ASCII)));
    }

}
