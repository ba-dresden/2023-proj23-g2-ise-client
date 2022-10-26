package de.ba.railroadclient.rest

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
    private var serverURL: String?,
    private val requestQueue: RequestQueue,
    private val errorListener: Response.ErrorListener?
) : ArrayAdapter<Server?>(context, R.layout.support_simple_spinner_dropdown_item) {

    /**
     * the Update Handler is responsible for calling the update task
     */
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
        val newItems: MutableList<Server?> = ArrayList()
        for (i in 0 until response.length()) try {
            val `object` = response.getJSONObject(i)
            val dao = ServerDAO()
            newItems.add(dao.read(`object`.toString()))
        } catch (t: Throwable) {
            Log.e("main", "can not create JSON object", t)
        }

        // remove all older items
        for (i in 0 until count) {
            val item = getItem(i)
            if (!newItems.contains(item)) {
                remove(item)
            }
        }

        // add new items
        for (i in newItems.indices) {
            val item = newItems[i]
            if (getPosition(item) < 0) {
                add(item)
            }
        }
    }
}