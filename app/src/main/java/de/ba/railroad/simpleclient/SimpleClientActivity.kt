package de.ba.railroad.simpleclient

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import de.ba.railroadclient.rest.ServerListAdapter
import de.ba.railroadclient.ws.CraneWebSocketClient
import de.ba.railroadclient.ws.LocomotiveWebSocketClient
import de.ba.railroadclient.ws.SwitchGroupWebSocketClient
import model.*
import ws.CraneWebSocketFacade
import ws.LocomotiveWebSocketFacade
import ws.SwitchGroupWebSocketFacade
import java.text.MessageFormat

class SimpleClientActivity : AppCompatActivity() {

    /**
     * WebSocket connection to a locomotive server
     */
    private var locomotiveSocket = LocomotiveWebSocketClient()

    /**
     * Locomotive to display and drive
     */
    private var locomotive: Locomotive = LocomotivePOJO()

    /**
     * WebSocket connection to a switch group server
     */
    private var switchGroupSocket = SwitchGroupWebSocketClient()

    /**
     * SwitchGroup to display and switch
     */
    private var switchGroup: SwitchGroup = SwitchGroupPOJO()

    /**
     * WebSocket connection to a crane server
     */
    private var craneSocket = CraneWebSocketClient()

    /**
     * Crane to display and switch
     */
    private var crane: Crane = CranePOJO()

    /**
     * Called on app creation.
     *
     * @param savedInstanceState params given to app, e.g. file associations
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a request que for HTTP POST and GET
        val requestQueue = Volley.newRequestQueue(this)

        // -------------------------------------------------------------------------------
        //
        //                  Locomotive
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        val locomotiveErrorListener = Response.ErrorListener { error: VolleyError ->
            val errorView = findViewById<TextView>(R.id.locomotiveErrors)
            errorView.text = error.message
        }

        // Adapter for the locomotiveSpinner view element. If we add or remove a LocomotiveServer
        // here, the view will be updated and the user can select this server to control a locomotive
        val adapter = ServerListAdapter(
            this,
            "$RAILROAD_SERVER/locomotive",
            requestQueue,
            locomotiveErrorListener
        )

        val locomotiveSpinner = findViewById<Spinner>(R.id.locomotiveSpinner)
        locomotiveSpinner.adapter = adapter
        locomotive = LocomotivePOJO()
        locomotiveSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // disconnect from current server
                locomotiveSocket.disconnect()

                // get the current locomotive server and connect
                val locomotiveServer = parent.adapter.getItem(position) as Server
                locomotiveSocket.connect(locomotiveServer.url)
                val errorView = findViewById<TextView>(R.id.locomotiveErrors)
                errorView.text = locomotiveServer.url
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                locomotiveSocket.disconnect()
                Log.d("main", "nothing selected")
            }
        }

        // associate direction buttons with their action methods
        findViewById<View>(R.id.forward).setOnClickListener {
            locomotive.direction = Locomotive.DIRECTION_FORWARD
            locomotiveSocket.sendLocomotive(locomotive)
        }

        findViewById<View>(R.id.backward).setOnClickListener {
            locomotive.direction = Locomotive.DIRECTION_BACKWARD
            locomotiveSocket.sendLocomotive(locomotive)
        }

        findViewById<View>(R.id.stop).setOnClickListener {
            locomotive.speed = 0
            locomotiveSocket.sendLocomotive(locomotive)
        }

        findViewById<View>(R.id.send).setOnClickListener {
            val speedView = findViewById<TextView>(R.id.speed)
            locomotive.speed = speedView.text.toString().toInt()
            locomotiveSocket.sendLocomotive(locomotive)
        }

        // associate horn button
        findViewById<View>(R.id.horn).setOnClickListener {
            if (locomotive.isHornSound == null) {
                return@setOnClickListener
            }
            locomotive.isHornSound = !locomotive.isHornSound
            locomotiveSocket.sendLocomotive(locomotive)
        }

        locomotiveSocket.webSocketObserver =
            object : LocomotiveWebSocketFacade.WebSocketObserver {
                override fun locomotiveChanged(locomotive: Locomotive) {
                    runOnUiThread {
                        val speed = locomotive.speed
                        (findViewById<View>(R.id.speed) as TextView).text =
                            MessageFormat.format("{0}", speed)

                        // store the locomotive locally, including it's ID
                        val dao = LocomotiveDAO()
                        dao.copy(locomotive, this@SimpleClientActivity.locomotive)
                        this@SimpleClientActivity.locomotive.id = locomotive.id
                    }
                }

                override fun connectionEstablished() {}
                override fun connectionClosed() {}
                override fun errorOccurred(throwable: Throwable) {
                    findViewById<Spinner>(R.id.locomotiveSpinner).setSelection(-1)
                }
            }

        // -------------------------------------------------------------------------------
        //
        //                  Switch
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        val switchErrorListener = Response.ErrorListener { error: VolleyError ->
            val errorView = findViewById<TextView>(R.id.switchErrors)
            errorView.text = error.message
        }

        // Adapter for the switchSpinner view element. If we add or remove a SwitchServer
        // here, the view will be updated and the user can select this server to control a switch group.
        val switchListAdapter =
            ServerListAdapter(this, "$RAILROAD_SERVER/switch", requestQueue, switchErrorListener)
        val switchSpinner = findViewById<Spinner>(R.id.switchSpinner)
        switchSpinner.adapter = switchListAdapter
        switchGroup = SwitchGroupPOJO()
        switchSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // disconnect from current server
                switchGroupSocket.disconnect()

                // get the current locomotive server and connect
                val switchServer = parent.adapter.getItem(position) as Server
                switchGroupSocket.connect(switchServer.url)
                val errorView = findViewById<TextView>(R.id.switchErrors)
                errorView.text = switchServer.url
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                switchGroupSocket.disconnect()
                Log.d("main", "nothing selected")
            }
        }
        findViewById<View>(R.id.sw1).setOnClickListener {
            if (switchGroup.switches.size == 0) {
                return@setOnClickListener
            }
            val s = switchGroup.switches[0]
            if (s.track == Switch.TRACK_STRAIGHT) {
                s.track = Switch.TRACK_DIVERGING
            } else {
                s.track = Switch.TRACK_STRAIGHT
            }
            switchGroupSocket.sendSwitchGroup(switchGroup)
        }

        switchGroupSocket.webSocketObserver =
            object : SwitchGroupWebSocketFacade.WebSocketObserver {
                override fun switchGroupChanged(switchGroup: SwitchGroup) {
                    runOnUiThread {

                        // store the locomotive locally, including it's ID
                        val dao = SwitchGroupDAO()
                        dao.copy(switchGroup, this@SimpleClientActivity.switchGroup)
                        this@SimpleClientActivity.switchGroup.id = switchGroup.id
                    }
                }

                override fun connectionEstablished() {}
                override fun connectionClosed() {}
                override fun errorOccurred(throwable: Throwable) {
                    findViewById<Spinner>(R.id.switchSpinner).setSelection(-1)
                }
            }

        // -------------------------------------------------------------------------------
        //
        //                  Crane
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        val craneErrorListener = Response.ErrorListener { error: VolleyError ->
            val errorView = findViewById<TextView>(R.id.craneErrors)
            errorView.text = error.message
        }

        // Adapter for the craneSpinner view element. If we add or remove a CraneServer
        // here, the view will be updated and the user can select this server to control a crane.
        val craneListAdapter =
            ServerListAdapter(this, "$RAILROAD_SERVER/crane", requestQueue, craneErrorListener)
        val craneSpinner = findViewById<Spinner>(R.id.craneSpinner)
        craneSpinner.adapter = craneListAdapter
        crane = CranePOJO()
        craneSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // disconnect from current server
                craneSocket.disconnect()

                // get the current crane server and connect
                val craneServer = parent.adapter.getItem(position) as Server
                craneSocket.connect(craneServer.url)
                val errorView = findViewById<TextView>(R.id.craneErrors)
                errorView.text = craneServer.url
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                craneSocket.disconnect()
                Log.d("main", "nothing selected")
            }
        }

        // Determines the action for a click on the "left" button
        findViewById<View>(R.id.craneleft).setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (crane.motorHorizontal != Crane.MOVE_LEFT_UP) {
                    crane.motorHorizontal = Crane.MOVE_LEFT_UP
                    craneSocket.sendCrane(crane)
                }
                v.isPressed = true
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_UP) {
                crane.motorHorizontal = Crane.STOP
                craneSocket.sendCrane(crane)
                v.isPressed = false
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }

        // Determines the action for a click on the "right" button
        findViewById<View>(R.id.craneright).setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (crane.motorHorizontal != Crane.MOVE_RIGHT_DOWN) {
                    crane.motorHorizontal = Crane.MOVE_RIGHT_DOWN
                    craneSocket.sendCrane(crane)
                }
                v.isPressed = true
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_UP) {
                crane.motorHorizontal = Crane.STOP
                craneSocket.sendCrane(crane)
                v.isPressed = false
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }

        // Determines the action for a click on the "up" button
        findViewById<View>(R.id.craneup).setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (crane.motorVertical != Crane.MOVE_LEFT_UP) {
                    crane.motorVertical = Crane.MOVE_LEFT_UP
                    craneSocket.sendCrane(crane)
                }
                v.isPressed = true
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_UP) {
                crane.motorVertical = Crane.STOP
                craneSocket.sendCrane(crane)
                v.isPressed = false
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }

        // determines the action for a click on the "down" button
        findViewById<View>(R.id.cranedown).setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (crane.motorVertical != Crane.MOVE_RIGHT_DOWN) {
                    crane.motorVertical = Crane.MOVE_RIGHT_DOWN
                    craneSocket.sendCrane(crane)
                }
                v.isPressed = true
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_UP) {
                crane.motorVertical = Crane.STOP
                craneSocket.sendCrane(crane)
                v.isPressed = false
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }
        craneSocket = CraneWebSocketClient()
        craneSocket.webSocketObserver = object : CraneWebSocketFacade.WebSocketObserver {
            override fun craneChanged(crane: Crane) {
                runOnUiThread {
                    // store the locomotive locally, including it's ID
                    val dao = CraneDAO()
                    dao.copy(crane, this@SimpleClientActivity.crane)
                    this@SimpleClientActivity.crane.id = crane.id
                }
            }

            override fun connectionEstablished() {}
            override fun connectionClosed() {}
            override fun errorOccurred(throwable: Throwable) {
                findViewById<Spinner>(R.id.craneSpinner).setSelection(-1)
            }
        }
    }

    companion object {
        /**
         * URL of the RailroadServlet. This servlet knows all active locomotive servers
         *
         * ise-rrs01    Vitrine
         * dv-git01     BA Virtual Development Server
         * 10.0.2.2     (local) Host for Android Emulator
         */
        // private static final String RAILROAD_SERVER = "http://10.0.2.2:8095";
        // private static final String RAILROAD_SERVER = "http://ise-rrs01.dv.ba-dresden.local:8095";
        private const val RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095"
    }
}