package de.ba.railroad.simpleclient

import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.relay.compose.BoxScopeInstanceImpl.align
import com.google.relay.compose.RelayBox
import com.google.relay.compose.RelayColumn
import com.google.relay.compose.RelayContainer
import de.ba.railroad.simpleclient.speedcontrol.SpeedControl
import de.ba.railroadclient.CraneWebSocketClient
import de.ba.railroadclient.LocomotiveWebSocketClient
import de.ba.railroadclient.SwitchGroupWebSocketClient
import model.*

class SimpleClientActivity : ComponentActivity() {

    /**
     * WebSocket connection to a locomotive server
     */
    private val locomotiveSocket = LocomotiveWebSocketClient()

    /**
     * Locomotive to display and drive
     */
    private val locomotive: Locomotive = LocomotivePOJO()

    /**
     * WebSocket connection to a switch group server
     */
    private val switchGroupSocket = SwitchGroupWebSocketClient()

    /**
     * SwitchGroup to display and switch
     */
    private val switchGroup: SwitchGroup = SwitchGroupPOJO()

    /**
     * WebSocket connection to a crane server
     */
    private val craneSocket = CraneWebSocketClient()

    /**
     * Crane to display and switch
     */
    private val crane: Crane = CranePOJO()

    /**
     * Called on app creation.
     *
     * @param savedInstanceState params given to app, e.g. file associations
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column (
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        )
                        {

                                SpeedControl(

                                        onFastForwardClick = {
                                            locomotive.direction = Locomotive.DIRECTION_FORWARD
                                            locomotive.speed = 100
                                            locomotiveSocket.sendLocomotive(locomotive)
                                        },
                                        onForwardButtonClick = {
                                            locomotive.direction = Locomotive.DIRECTION_FORWARD
                                            locomotive.speed = 20
                                            locomotiveSocket.sendLocomotive(locomotive)
                                        },
                                        onStopButtonClick = {
                                            locomotive.speed = 0
                                            locomotiveSocket.sendLocomotive(locomotive)
                                        },
                                        onBackButtonClick = {
                                            locomotive.direction = Locomotive.DIRECTION_BACKWARD
                                            locomotive.speed = 20
                                            locomotiveSocket.sendLocomotive(locomotive)
                                        },
                                        onFastBackButtonClick = {
                                            locomotive.direction = Locomotive.DIRECTION_BACKWARD
                                            locomotive.speed = 100
                                            locomotiveSocket.sendLocomotive(locomotive)
                                        }
                                )

                        }
                    }
                }
            }
        }
    }

    // Add this line to your gradle dependencies for your Android project's app module:
    //
    //implementation 'com.android.volley:volley:1.1.1'
    // https://stackoverflow.com/questions/20059576/import-android-volley-to-android-studio




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
            locomotiveSocket.connect(locomotiveServer.url, lifecycleScope)
            val errorView = findViewById<TextView>(R.id.locomotiveErrors)
            errorView.text = locomotiveServer.url
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            locomotiveSocket.disconnect()
            Log.d("main", "nothing selected")
        }
    }



    locomotiveSocket.webSocketObserver =
    object : WebSocketFacade.WebSocketObserver<Locomotive> {
        override fun objectReceived(receivedObject: Locomotive) {
            runOnUiThread {
                val speed = receivedObject.speed
                (findViewById<View>(R.id.speed) as TextView).text =
                        MessageFormat.format("{0}", speed)

                // store the locomotive locally, including it's ID
                LocomotiveDAO.copy(receivedObject, this@SimpleClientActivity.locomotive)
                this@SimpleClientActivity.locomotive.id = receivedObject.id
            }
        }

        override fun connectionEstablished() {}
        override fun connectionClosed() {}
        override fun errorOccurred(throwable: Throwable) {
            findViewById<Spinner>(R.id.locomotiveSpinner).setSelection(-1)
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
        private const val RAILROAD_SERVER = "http://10.0.2.2:8095";
        // private const val RAILROAD_SERVER = "http://ise-rrs01.dv.ba-dresden.local:8095";
        // private const val RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095"
    }

}
        /*
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
                locomotiveSocket.connect(locomotiveServer.url, lifecycleScope)
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
            locomotive.isHornSound = !locomotive.isHornSound!!
            locomotiveSocket.sendLocomotive(locomotive)
        }

        locomotiveSocket.webSocketObserver =
            object : WebSocketFacade.WebSocketObserver<Locomotive> {
                override fun objectReceived(receivedObject: Locomotive) {
                    runOnUiThread {
                        val speed = receivedObject.speed
                        (findViewById<View>(R.id.speed) as TextView).text =
                            MessageFormat.format("{0}", speed)

                        // store the locomotive locally, including it's ID
                        LocomotiveDAO.copy(receivedObject, this@SimpleClientActivity.locomotive)
                        this@SimpleClientActivity.locomotive.id = receivedObject.id
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
                switchGroupSocket.connect(switchServer.url, lifecycleScope)
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
            object : WebSocketFacade.WebSocketObserver<SwitchGroup> {
                override fun objectReceived(receivedObject: SwitchGroup) {
                    runOnUiThread {

                        // store the locomotive locally, including it's ID
                        SwitchGroupDAO.copy(receivedObject, this@SimpleClientActivity.switchGroup)
                        this@SimpleClientActivity.switchGroup.id = receivedObject.id
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
                craneSocket.connect(craneServer.url, lifecycleScope)
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

        craneSocket.webSocketObserver = object : WebSocketFacade.WebSocketObserver<Crane> {
            override fun objectReceived(receivedObject: Crane) {
                runOnUiThread {
                    // store the locomotive locally, including it's ID
                    CraneDAO.copy(receivedObject, this@SimpleClientActivity.crane)
                    this@SimpleClientActivity.crane.id = receivedObject.id
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
        private const val RAILROAD_SERVER = "http://10.0.2.2:8095";
        // private const val RAILROAD_SERVER = "http://ise-rrs01.dv.ba-dresden.local:8095";
        // private const val RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095"
    }
}
*/


