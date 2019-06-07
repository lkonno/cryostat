package es.andrewazor.containertest.tui.ws;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import es.andrewazor.containertest.tui.ClientReader;
import es.andrewazor.containertest.tui.ClientWriter;

class WsClientReaderWriter extends WebSocketAdapter implements ClientReader, ClientWriter {

    private final Semaphore semaphore = new Semaphore(0, true);
    private final Gson gson;
    private final BlockingQueue<String> inQ = new LinkedBlockingQueue<>();
    private volatile Thread readingThread;

    WsClientReaderWriter(MessagingServer server, Gson gson) {
        this.gson = gson;
        server.setConnection(this);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        semaphore.release();
    }

    @Override
    public void onWebSocketText(String text) {
        super.onWebSocketText(text);
        inQ.add(text);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        close();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED") // tryAcquire return value is irrelevant
    @Override
    public void close() {
        semaphore.tryAcquire();
        if (isConnected()) {
            getSession().close();
        }
        super.onWebSocketClose(0, null);
        if (readingThread != null) {
            readingThread.interrupt();
        }
    }

    @Override
    public void print(String s) { }

    void flush(ResponseMessage<?> message) {
        try {
            semaphore.acquireUninterruptibly();
            try {
                getRemote().sendString(gson.toJson(message));
                getRemote().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            semaphore.release();
        }
    }

    @Override
    public String readLine() {
        try {
            readingThread = Thread.currentThread();
            return inQ.take();
        } catch (InterruptedException e) {
            return null;
        } finally {
            readingThread = null;
        }
    }
}