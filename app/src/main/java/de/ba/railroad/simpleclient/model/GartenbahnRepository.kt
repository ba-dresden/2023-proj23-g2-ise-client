package de.ba.railroad.simpleclient.model

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import de.ba.railroad.simpleclient.start.RailroadClient
import de.ba.railroadclient.LocomotiveWebSocketClient
import de.ba.railroadclient.execute
import model.Locomotive
import model.LocomotivePOJO
import model.Server
import model.ServerDAO
import org.json.JSONArray
import ws.WebSocketFacade
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

class GartenbahnRepository(private val lifecycle: LifecycleCoroutineScope) : ServerData {
    private val _server = MutableLiveData<List<Server>>()
    private val server: LiveData<List<Server>> get() = _server
    private val serverURL = "$RAILROAD_SERVER/locomotive"
    private val locomotiveSocket = LocomotiveWebSocketClient()
    private val locomotive: Locomotive = LocomotivePOJO()
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(RailroadClient.instance.applicationContext)
    }

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

                }, {
                    //Handle Error
                })

            // add the GET action to the request que
            requestQueue.add(getRequest)
        }
    }

    init {
        // update the list of active servers
        updateHandler.post(updateRunnable)
    }

    fun updateListItems(response: JSONArray) {
        var newItems = (0 until response.length())
            .mapNotNull {
                execute {
                    ServerDAO.read(response.getJSONObject(it).toString())
                }.getOrNull()
            }
            .toList()

        _server.postValue(newItems)
    }


    override fun getServerList(): LiveData<List<Server>> {
        return server
    }

    override suspend fun changeLocomotive(url: String) {
        locomotiveSocket.disconnect()
        locomotiveSocket.connect(url, lifecycle)
    }

    override suspend fun postSpeed(speed: Int, direction: Int) {
        locomotive.speed = speed
        locomotive.direction = direction
        locomotiveSocket.sendLocomotive(locomotive)
    }

    companion object {
        /**
         * URL of the RailroadServlet. This servlet knows all active locomotive servers
         *
         * ise-rrs01    Vitrine
         * dv-git01     BA Virtual Development Server
         * 10.0.2.2     (local) Host for Android Emulator
         */
        // private const val RAILROAD_SERVER = "http://10.0.2.2:8095";
        private const val RAILROAD_SERVER = "http://ise-rrs01.dv.ba-dresden.local:8095";
        // private const val RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095"
    }
}
