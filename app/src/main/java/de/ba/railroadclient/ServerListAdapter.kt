package de.ba.railroadclient

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import de.ba.railroad.simpleclient.R
import model.Server
import model.ServerDAO
import org.json.JSONArray

/**
 * The adapter stores items which it got from a web server. The server sends a JSON string where
 * * the adapter can read from. This class is abstract, child classes will overwrite
 * * [.updateListItems] and decide what to do with the servers response.
 * *
 *
 * Simple adapter for a dropdown list. The adapter will update its content from a railroad server.
 * Items are SwitchServer objects used to control a group of railroad switches.
 *
 * @param serverURL URL for communication.
 * @param requestQueue HTTP request que. This queue can be shared with other tasks.
 * @param errorListener Will receive the error messages
 *
 * @param context Context of the Android App
 * @param serverURL URL for the railroad server
 * @param requestQueue queu for HTP requests
 * @param errorListener listener to handle errors
 */
open class ServerListAdapter(
        context: Context,
        private val serverURL: String?,
        private val requestQueue: RequestQueue,
        private val errorListener: Response.ErrorListener?
) {
    private lateinit var newItems: MutableList<Server>
    fun run() {
        // cancel the HTTP request in case of an empty URI
        if (serverURL == null || serverURL!!.isEmpty()) {
            return
        }

        // get all active locomotive servers
        val getRequest =
                JsonArrayRequest(Request.Method.GET, serverURL, null, { response: JSONArray ->
                    Log.d("updateRunnable", "getRequest: $response")

                    // update the items
                    updateListItems(response)
                }, errorListener)

        // add the GET action to the request que
        requestQueue.add(getRequest)
    }

    private fun updateListItems(response: JSONArray) {
        newItems = (0 until response.length())
                .mapNotNull {
                    execute {
                        ServerDAO.read(response.getJSONObject(it).toString())
                    }.getOrNull()
                }
                .toMutableList()
    }

    fun getServers():MutableList<Server> {
        return newItems
    }

    /*

     /
     * the Update Handler is responsible for calling the update task

    private val updateHandler = Handler(Looper.getMainLooper())

    /**
     * The Updater will periodically read all active locomotive servers and update the selection
     * spinner. The user can select a server from this list to connect to a switch server.
     */
    private val updateRunnable: Runnable = object : Runnable {
        /**
         * first delay is 0 seconds
         */
        private var delay = 0
        override fun run() {
            // call this method again in 10 seconds
            updateHandler.postDelayed(this, delay.toLong())
            delay = 10000

            // cancel the HTTP request in case of an empty URI
            if (serverURL == null || serverURL!!.isEmpty()) {
                return
            }

            // get all active locomotive servers
            val getRequest =
                JsonArrayRequest(Request.Method.GET, serverURL, null, { response: JSONArray ->
                    Log.d("updateRunnable", "getRequest: $response")

                    // update the items
                    updateListItems(response)

                    // update the view element
                    notifyDataSetChanged()
                }, errorListener)

            // add the GET action to the request que
            requestQueue.add(getRequest)
        }
    }

    /**
     * Create an adapter for a drop doen list.
     */
    init {
        // update the list of active servers
        updateHandler.post(updateRunnable)
    }

    protected fun updateListItems(response: JSONArray) {
        val newItems = (0 until response.length())
            .mapNotNull {
                execute {
                    ServerDAO.read(response.getJSONObject(it).toString())
                }.getOrNull()
            }
            .toMutableList()

        (0 until count)
            .mapNotNull { getItem(it) } // Erstellt eine Liste der Items, die nicht null sind
            .filter { !newItems.contains(it) } // Filtert die Items, die nicht in `newItems` enthalten sind
            .toMutableSet() // Konvertiert die Liste in ein MutableSet
            .forEach { remove(it) } // Entferne die so ermittelten Items aus der Liste

        // add new items
        newItems.filter { getPosition(it) < 0 }
            .forEach { add(it) }
    } */
}
