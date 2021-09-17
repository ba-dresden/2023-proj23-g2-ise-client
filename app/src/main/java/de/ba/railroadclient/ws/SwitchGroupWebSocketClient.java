package de.ba.railroadclient.ws;

import android.util.Log;

import org.eclipse.jetty.websocket.client.WebSocketClient;

import ws.SwitchGroupWebSocketFacade;
import ws.MessageAdapter;

/**
 * Facade for the client to establish a WebSocket connection.
 * The method {@link #connect(String)} works asynchronously, it will not block the
 * call thread.
 */
public class SwitchGroupWebSocketClient extends SwitchGroupWebSocketFacade {

    /**
     * The client object is responsible for the connection.
     */
    private final org.eclipse.jetty.websocket.client.WebSocketClient client;

    /**
     * The Locomotive client has only one MessageAdapter (= WebSocket)
     */
    private MessageAdapter webSocket;

    /**
     * Create a {@link WebSocketClient}
     */
    public SwitchGroupWebSocketClient () {
        client = new WebSocketClient();
    }

    /**
     * Start the PING/PONG game to keep connection open
     */
    @Override
    public void connectionEstablished(MessageAdapter messageAdapter) {
        super.connectionEstablished(messageAdapter);

        // Start the ping/pong to keep connection alive
        webSocket.ping();
        Log.d("SwitchGroupWebSocketClient", "PING/PONG started");
    }

    /**
     * Connects the faced with a WebSocket server using the {@link #client} reference.
     *
     * @param url URL to a WebSocket server
     */
    public void connect(String url) {
        // Start the client
        try {
            client.start();
        } catch (Exception e) {
            Log.d("SwitchGroupWebSocketClient", "can not start the websocket client", e);
        }

        // create or reuse a socket
        if (webSocket == null) {
            webSocket = createSocket();
        }

        // connect the client
        new ConnectTask(client, webSocket).execute(url);
    }

    /**
     * Stop the {@link WebSocketClient}.
     */
    public void disconnect() {
        try {
            // stop the client
            client.stop();
        } catch (Exception e) {
            Log.d("SwitchGroupWebSocketClient", "can not disconnect", e);
        }
    }
}
