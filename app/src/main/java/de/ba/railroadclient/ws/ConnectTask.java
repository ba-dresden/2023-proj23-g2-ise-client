package de.ba.railroadclient.ws;

import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

import ws.MessageAdapter;

/**
 * AsyncTask for the WebSocket connection, used in {@link LocomotiveWebSocketClient#connect(String)}.
 */
@SuppressWarnings("ALL")
class ConnectTask extends AsyncTask<String, String, Void> {

    /**
     * Provides a means of establishing connections to remote websocket endpoints
     */
    private org.eclipse.jetty.websocket.client.WebSocketClient client;

    /**
     * JETTY basic Web Socket implementation
     */
    private MessageAdapter webSocket;

    /**
     * Stores the WebSocketClient and MessageAdapter
     * @param client provides a means of establishing connections to remote websocket endpoints
     * @param webSocket JETTY basic Web Socket implementation
     */
    ConnectTask(WebSocketClient client, MessageAdapter webSocket) {
        this.client = client;
        this.webSocket = webSocket;
    }

    /**
     * Connects the {@link #client} using the {@link #webSocket} and given URL.
     *
     * @param uris Array of URLs, first URL is used
     * @return null
     */
    @Override
    protected Void doInBackground(String... uris) {

        if (uris == null || uris.length == 0) {
            Log.d("ConnectTask", "can not connect to an empty URL");
            return null;
        }

        try {
            URI echoUri = new URI(uris[0]);
            client.connect(webSocket, echoUri);
            Log.d("ConnectTask", "Connecting to: " + echoUri);

        } catch (Throwable t) {
            Log.d("ConnectTask", "can not connect", t);
        }

        return null;
    }
}
