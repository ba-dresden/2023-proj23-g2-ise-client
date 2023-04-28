package de.ba.railroadclient

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import org.eclipse.jetty.websocket.client.WebSocketClient
import ws.LocomotiveWebSocketFacade
import ws.MessageAdapter
import java.net.URI

/**
 * Facade for the client to establish a WebSocket connection.
 * The method [.connect] works asynchronously, it will not block the
 * call thread.
 *
 * The creation of a connection is based on a given lifecycleScope.
 */
class LocomotiveWebSocketClient : LocomotiveWebSocketFacade() {

    /**
     * The client object is responsible for the connection.
     */
    private val client = WebSocketClient()

    /**
     * The Locomotive client has only one MessageAdapter (= WebSocket)
     */
    private var webSocket: MessageAdapter? = null

    /**
     * Start the PING/PONG game to keep connection open
     */
    override fun connectionEstablished(messageAdapter: MessageAdapter) {
        super.connectionEstablished(messageAdapter)

        // Start the ping/pong to keep connection alive
        webSocket?.ping()
        Log.d(javaClass.name, "PING/PONG started")
    }

    /**
     * Connects the faced with a WebSocket server using the [.client] reference.
     *
     * @param url URL to a WebSocket server
     */
    fun connect(url: String, scope: LifecycleCoroutineScope) {
        // Start the client
        execute(client::start, javaClass.name, "can not start the websocket client")

        // create or reuse a socket
        if (webSocket == null) {
            webSocket = createSocket()
        }

        // connect the client
        scope.launch {
            execute({
                val echoUri = URI(url)
                client.connect(webSocket, URI(url))
                Log.d(javaClass.name, "Connecting to: $echoUri")
            }, javaClass.name, "can not connect")
        }
    }

    /**
     * Stop the [WebSocketClient].
     */
    fun disconnect() {
        // Start the client
        execute(client::stop, javaClass.name, "can not disconnect")
    }
}