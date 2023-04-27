package de.ba.railroadclient.ws

import org.eclipse.jetty.websocket.client.WebSocketClient
import ws.MessageAdapter
import android.os.AsyncTask
import android.util.Log
import ws.CraneWebSocketFacade
import de.ba.railroadclient.ws.ConnectTask
import ws.LocomotiveWebSocketFacade
import ws.SwitchGroupWebSocketFacade
import ws.BarrierGroupWebSocketFacade
import java.net.URI

/**
 * AsyncTask for the WebSocket connection, used in [LocomotiveWebSocketClient.connect].
 */
internal class ConnectTask
/**
 * Stores the WebSocketClient and MessageAdapter
 * @param client provides a means of establishing connections to remote websocket endpoints
 * @param webSocket JETTY basic Web Socket implementation
 */(
    /**
     * Provides a means of establishing connections to remote websocket endpoints
     */
    private val client: WebSocketClient,
    /**
     * JETTY basic Web Socket implementation
     */
    private val webSocket: MessageAdapter?
) : AsyncTask<String?, String?, Void?>() {

    /**
     * Connects the [.client] using the [.webSocket] and given URL.
     *
     * @param uris Array of URLs, first URL is used
     * @return null
     */
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String?): Void? {
        if (params == null || params.size == 0) {
            Log.d("ConnectTask", "can not connect to an empty URL")
            return null
        }
        try {
            val echoUri = URI(params[0])
            client.connect(webSocket, echoUri)
            Log.d("ConnectTask", "Connecting to: $echoUri")
        } catch (t: Throwable) {
            Log.d("ConnectTask", "can not connect", t)
        }
        return null
    }
}