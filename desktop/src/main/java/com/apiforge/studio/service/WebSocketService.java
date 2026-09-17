package com.apiforge.studio.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketService {
    private WebSocket webSocket;
    private final HttpClient client;

    public WebSocketService() {
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<WebSocket> connect(String url, 
                                                Consumer<String> messageConsumer, 
                                                Consumer<String> statusConsumer, 
                                                Consumer<Throwable> errorConsumer) {
        
        WebSocket.Listener listener = new WebSocket.Listener() {
            private final StringBuilder builder = new StringBuilder();

            @Override
            public void onOpen(WebSocket webSocket) {
                statusConsumer.accept("Connected to " + url);
                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                builder.append(data);
                if (last) {
                    messageConsumer.accept(builder.toString());
                    builder.setLength(0);
                }
                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                messageConsumer.accept("[Binary message of size: " + data.remaining() + " bytes]");
                webSocket.request(1);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                errorConsumer.accept(error);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                statusConsumer.accept("Connection closed. Status: " + statusCode + ", Reason: " + reason);
                return null;
            }
        };

        return client.newWebSocketBuilder()
                .buildAsync(URI.create(url), listener)
                .thenApply(ws -> {
                    this.webSocket = ws;
                    return ws;
                });
    }

    public void sendMessage(String message) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendText(message, true);
        }
    }

    public void disconnect() {
        if (webSocket != null && !webSocket.isInputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "User closed connection");
        }
    }

    public boolean isConnected() {
        return webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed();
    }
}
