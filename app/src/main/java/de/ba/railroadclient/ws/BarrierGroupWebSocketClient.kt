package de.ba.railroadclient.ws

import android.util.Log
import org.eclipse.jetty.websocket.client.WebSocketClient
import ws.BarrierGroupWebSocketFacade
import ws.MessageAdapter

/**
 * Facade for the client to establish a WebSocket connection.
 * The method [.connect] works asynchronously, it will not block the
 * call thread.
 */
class BarrierGroupWebSocketClient : BarrierGroupWebSocketFacade() {
    /**
     * The client object is responsible for the connection.
     */
    private val client: WebSocketClient

    /**
     * The Locomotive client has only one MessageAdapter (= WebSocket)
     */
    private var webSocket: MessageAdapter? = null

    /**
     * Create a [WebSocketClient]
     */
    init {
        client = WebSocketClient()
    }

    /**
     * Start the PING/PONG game to keep connection open
     */
    override fun connectionEstablished(messageAdapter: MessageAdapter) {
        super.connectionEstablished(messageAdapter)

        // Start the ping/pong to keep connection alive
        webSocket!!.ping()
        Log.d("BarrierGroupWebSocketClient", "PING/PONG started")
    }

    /**
     * Connects the faced with a WebSocket server using the [.client] reference.
     *
     * @param url URL to a WebSocket server
     */
    fun connect(url: String?) {
        // Start the client
        try {
            client.start()
        } catch (e: Exception) {
            Log.d("BarrierGroupWebSocketClient", "can not start the websocket client", e)
        }

        // create or reuse a socket
        if (webSocket == null) {
            webSocket = createSocket()
        }

        // connect the client
        ConnectTask(client, webSocket).execute(url)
    }

    /**
     * Stop the [WebSocketClient].
     */
    fun disconnect() {
        try {
            // stop the client
            client.stop()
        } catch (e: Exception) {
            Log.d("BarrierGroupWebSocketClient", "can not disconnect", e)
        }
    }
}