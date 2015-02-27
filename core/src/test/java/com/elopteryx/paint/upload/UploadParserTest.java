package com.elopteryx.paint.upload;

import com.elopteryx.paint.upload.impl.AsyncUploadParser;
import com.elopteryx.paint.upload.impl.BlockingUploadParser;
import com.elopteryx.paint.upload.util.MockAsyncContext;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.mockito.Mockito.*;
import static com.elopteryx.paint.upload.util.FunctionSupplier.*;
import static com.elopteryx.paint.upload.util.Servlets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class UploadParserTest implements OnPartBegin, OnPartEnd, OnRequestComplete, OnError {

    @Test(expected = ServletException.class)
    public void valid_and_invalid_content_type() throws Exception {
        HttpServletRequest request = newRequest();
        HttpServletResponse response = newResponse();

        when(request.getContentType()).thenReturn("multipart/");
        assertTrue(UploadParser.isMultipart(request));

        when(request.getContentType()).thenReturn("text/plain;charset=UTF-8");
        assertFalse(UploadParser.isMultipart(request));
        UploadParser.newParser(request).withResponse(UploadResponse.from(response));
    }

    @Test
    public void create_async_parser() throws Exception {
        HttpServletRequest request = newRequest();
        HttpServletResponse response = newResponse();

        when(request.isAsyncSupported()).thenReturn(true);

        UploadParser asyncParser = UploadParser.newParser(request).withResponse(UploadResponse.from(response));
        assertThat(asyncParser, instanceOf(AsyncUploadParser.class));
    }

    @Test
    public void create_blocking_parser() throws Exception {
        HttpServletRequest request = newRequest();
        HttpServletResponse response = newResponse();
        
        when(request.isAsyncSupported()).thenReturn(false);

        UploadParser blockingParser = UploadParser.newParser(request).withResponse(UploadResponse.from(response));
        assertThat(blockingParser, instanceOf(BlockingUploadParser.class));
    }

    @Test
    public void use_the_full_api() throws Exception {
        HttpServletRequest request = newRequest();
        HttpServletResponse response = newResponse();

        when(request.startAsync()).thenReturn(new MockAsyncContext(request, response));

        UploadParser.newParser(request)
                .onPartBegin(partBeginCallback())
                .onPartEnd(partEndCallback())
                .onRequestComplete(requestCallback())
                .onError(errorCallback())
                .sizeThreshold(1024 * 1024 * 10)
                .maxPartSize(1024 * 1024 * 50)
                .maxRequestSize(1024 * 1024 * 50);

        Random random = new Random();
        UploadParser.newParser(request)
                .onPartBegin(this)
                .onPartEnd(this)
                .onRequestComplete(this)
                .onError(this)
                .sizeThreshold(() -> random.nextInt(1024 * 1024 * 10))
                .maxPartSize(() -> random.nextInt(1024 * 1024 * 50))
                .maxRequestSize(() -> random.nextInt(1024 * 1024 * 50));
    }

    @Override
    public PartOutput onPartBegin(UploadContext context, ByteBuffer buffer) throws IOException {
        if(true)
            return PartOutput.from(Files.newByteChannel(Paths.get("")));
        else
            return PartOutput.from(Files.newOutputStream(Paths.get("")));
    }

    @Override
    public void onPartEnd(UploadContext context) throws IOException {}

    @Override
    public void onRequestComplete(UploadContext context) throws IOException, ServletException {}

    @Override
    public void onError(UploadContext context, Throwable throwable) {}
}