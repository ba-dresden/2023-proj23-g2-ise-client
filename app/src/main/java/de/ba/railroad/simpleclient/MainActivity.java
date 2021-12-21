package de.ba.railroad.simpleclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.text.MessageFormat;

import de.ba.railroadclient.rest.ServerListAdapter;
import de.ba.railroadclient.ws.CraneWebSocketClient;
import de.ba.railroadclient.ws.LocomotiveWebSocketClient;
import de.ba.railroadclient.ws.SwitchGroupWebSocketClient;
import model.Crane;
import model.CraneDAO;
import model.CranePOJO;
import model.Locomotive;
import model.LocomotiveDAO;
import model.LocomotivePOJO;
import model.Server;
import model.Switch;
import model.SwitchGroup;
import model.SwitchGroupDAO;
import model.SwitchGroupPOJO;
import ws.LocomotiveWebSocketFacade;

public class MainActivity extends AppCompatActivity {

    /**
     * URL of the RailroadServlet. This servlet knows all active locomotive servers
     *
     * ise-rrs01    Vitrine
     * dv-git01     BA Virtual Development Server
     * 10.0.2.2     (local) Host for Android Emulator
     */
    // private static final String RAILROAD_SERVER = "http://ise-rrs01.dv.ba-dresden.local:8095";
    private static final String RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095";
    // private static final String RAILROAD_SERVER = "http://10.0.2.2:8095";

    /**
     * WebSocket connection to a locomotive server
     */
    private LocomotiveWebSocketClient locomotiveSocket;

    /**
     * Locomotive to display and drive
     */
    private Locomotive locomotive;

    /**
     * WebSocket connection to a switch group server
     */
    private SwitchGroupWebSocketClient switchGroupSocket;

    /**
     * SwitchGroup to display and switch
     */
    private SwitchGroup switchGroup;

    /**
     * WebSocket connection to a crane server
     */
    private CraneWebSocketClient craneSocket;

    /**
     * Crane to display and switch
     */
    private Crane crane;

    /**
     * Called on app creation.
     *
     * @param savedInstanceState params given to app, e.g. file associations
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create a request que for HTTP POST and GET
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // -------------------------------------------------------------------------------
        //
        //                  Locomotive
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        Response.ErrorListener locomotiveErrorListener = error -> {
            TextView errorView = MainActivity.this.findViewById(R.id.locomotiveErrors);
            errorView.setText(error.getMessage());
        };

        // Adapter for the locomotiveSpinner view element. If we add or remove a LocomotiveServer
        // here, the view will be updated and the user can select this server to control a locomotive
        ServerListAdapter adapter = new ServerListAdapter(this, RAILROAD_SERVER + "/locomotive", requestQueue, locomotiveErrorListener);
        Spinner locomotiveSpinner = findViewById(R.id.locomotiveSpinner);
        locomotiveSpinner.setAdapter(adapter);

        locomotive = new LocomotivePOJO();

        locomotiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // disconnect from current server
                locomotiveSocket.disconnect();

                // get the current locomotive server and connect
                Server locomotiveServer = (Server) parent.getAdapter().getItem(position);
                locomotiveSocket.connect(locomotiveServer.getURL());

                TextView errorView = MainActivity.this.findViewById(R.id.locomotiveErrors);
                errorView.setText(locomotiveServer.getURL());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                locomotiveSocket.disconnect();
                Log.d("main", "nothing selected");
            }
        });

        // associate direction buttons with their action methods
        findViewById(R.id.forward).setOnClickListener(v -> {
            locomotive.setDirection(Locomotive.DIRECTION_FORWARD);
            locomotiveSocket.sendLocomotive(locomotive);
        });

        findViewById(R.id.backward).setOnClickListener(v -> {
            locomotive.setDirection(Locomotive.DIRECTION_BACKWARD);
            locomotiveSocket.sendLocomotive(locomotive);
        });

        findViewById(R.id.stop).setOnClickListener(v -> {
            locomotive.setSpeed(0);
            locomotiveSocket.sendLocomotive(locomotive);
        });

        findViewById(R.id.send).setOnClickListener(v -> {
            TextView speedView = MainActivity.this.findViewById(R.id.speed);
            int speed = Integer.parseInt(String.valueOf(speedView.getText()));

            locomotive.setSpeed(speed);
            locomotiveSocket.sendLocomotive(locomotive);
        });

        // associate horn button
        findViewById(R.id.horn).setOnClickListener(v -> {
            if (locomotive.isHornSound() == null) {
                return;
            }

            locomotive.setHornSound(!locomotive.isHornSound());
            locomotiveSocket.sendLocomotive(locomotive);
        });

        locomotiveSocket = new LocomotiveWebSocketClient();
        locomotiveSocket.setWebSocketObserver(new LocomotiveWebSocketFacade.WebSocketObserver() {
            @Override
            public void locomotiveChanged(Locomotive locomotive) {
                runOnUiThread(() -> {
                    int speed = locomotive.getSpeed();
                    ((TextView)findViewById(R.id.speed)).setText(MessageFormat.format("{0}", speed));

                    // store the locomotive locally, including it's ID
                    LocomotiveDAO dao = new LocomotiveDAO();
                    dao.copy(locomotive, MainActivity.this.locomotive);
                    MainActivity.this.locomotive.setId(locomotive.getId());
                });
            }

            @Override
            public void connectionEstablished() {

            }

            @Override
            public void connectionClosed() {

            }

            @Override
            public void errorOccurred(Throwable throwable) {
                Spinner locomotiveSpinner = findViewById(R.id.locomotiveSpinner);
                locomotiveSpinner.setSelection(-1);
            }
        });

        // -------------------------------------------------------------------------------
        //
        //                  Switch
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        Response.ErrorListener switchErrorListener = error -> {
            TextView errorView = MainActivity.this.findViewById(R.id.switchErrors);
            errorView.setText(error.getMessage());
        };

        // Adapter for the switchSpinner view element. If we add or remove a SwitchServer
        // here, the view will be updated and the user can select this server to control a switch group.
        ServerListAdapter switchListAdapter = new ServerListAdapter(this, RAILROAD_SERVER + "/switch", requestQueue, switchErrorListener);
        Spinner switchSpinner = findViewById(R.id.switchSpinner);
        switchSpinner.setAdapter(switchListAdapter);

        switchGroup = new SwitchGroupPOJO();

        switchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // disconnect from current server
                switchGroupSocket.disconnect();

                // get the current locomotive server and connect
                Server switchServer = (Server) parent.getAdapter().getItem(position);
                switchGroupSocket.connect(switchServer.getURL());

                TextView errorView = MainActivity.this.findViewById(R.id.switchErrors);
                errorView.setText(switchServer.getURL());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                switchGroupSocket.disconnect();
                Log.d("main", "nothing selected");
            }
        });

        findViewById(R.id.sw1).setOnClickListener(v -> {
            if (switchGroup.getSwitches().size() == 0) {
                return;
            }

            Switch s = switchGroup.getSwitches().get(0);

            if (s.getTrack() == Switch.TRACK_STRAIGHT) {
                s.setTrack(Switch.TRACK_DIVERGING);
            } else {
                s.setTrack(Switch.TRACK_STRAIGHT);
            }
            switchGroupSocket.sendSwitchGroup(switchGroup);
        });

        switchGroupSocket = new SwitchGroupWebSocketClient();
        switchGroupSocket.setWebSocketObserver(new SwitchGroupWebSocketClient.WebSocketObserver() {
            @Override
            public void switchGroupChanged(SwitchGroup switchGroup) {
                runOnUiThread(() -> {
                    // store the locomotive locally, including it's ID
                    SwitchGroupDAO dao = new SwitchGroupDAO();
                    dao.copy(switchGroup, MainActivity.this.switchGroup);
                    MainActivity.this.switchGroup.setId(switchGroup.getId());
                });
            }

            @Override
            public void connectionEstablished() {

            }

            @Override
            public void connectionClosed() {

            }

            @Override
            public void errorOccurred(Throwable throwable) {
                Spinner switchSpinner = findViewById(R.id.switchSpinner);
                switchSpinner.setSelection(-1);
            }
        });

        // -------------------------------------------------------------------------------
        //
        //                  Crane
        //
        // -------------------------------------------------------------------------------

        // listener to display errors
        Response.ErrorListener craneErrorListener = error -> {
            TextView errorView = MainActivity.this.findViewById(R.id.craneErrors);
            errorView.setText(error.getMessage());
        };

        // Adapter for the craneSpinner view element. If we add or remove a CraneServer
        // here, the view will be updated and the user can select this server to control a crane.
        ServerListAdapter craneListAdapter = new ServerListAdapter(this, RAILROAD_SERVER + "/crane", requestQueue, craneErrorListener);
        Spinner craneSpinner = findViewById(R.id.craneSpinner);
        craneSpinner.setAdapter(craneListAdapter);

        crane = new CranePOJO();

        craneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // disconnect from current server
                craneSocket.disconnect();

                // get the current crane server and connect
                Server craneServer = (Server) parent.getAdapter().getItem(position);
                craneSocket.connect(craneServer.getURL());

                TextView errorView = MainActivity.this.findViewById(R.id.craneErrors);
                errorView.setText(craneServer.getURL());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                craneSocket.disconnect();
                Log.d("main", "nothing selected");
            }
        });

        // Determines the action for a click on the "left" button
        findViewById(R.id.craneleft).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (crane.getMotorHorizontal() != Crane.MOVE_LEFT_UP) {
                    crane.setMotorHorizontal(Crane.MOVE_LEFT_UP);
                    craneSocket.sendCrane(crane);
                }
                v.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                crane.setMotorHorizontal(Crane.STOP);
                craneSocket.sendCrane(crane);
                v.setPressed(false);
                v.performClick();
                return true;
            }
            return false;
        });

        // Determines the action for a click on the "right" button
        findViewById(R.id.craneright).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (crane.getMotorHorizontal() != Crane.MOVE_RIGHT_DOWN) {
                    crane.setMotorHorizontal(Crane.MOVE_RIGHT_DOWN);
                    craneSocket.sendCrane(crane);
                }
                v.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                crane.setMotorHorizontal(Crane.STOP);
                craneSocket.sendCrane(crane);

                v.setPressed(false);
                v.performClick();
                return true;
            }
            return false;
        });

        // Determines the action for a click on the "up" button
        findViewById(R.id.craneup).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (crane.getMotorVertical() != Crane.MOVE_LEFT_UP) {
                    crane.setMotorVertical(Crane.MOVE_LEFT_UP);
                    craneSocket.sendCrane(crane);
                }

                v.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                crane.setMotorVertical(Crane.STOP);
                craneSocket.sendCrane(crane);

                v.setPressed(false);
                v.performClick();
                return true;
            }
            return false;
        });

        // determines the action for a click on the "down" button
        findViewById(R.id.cranedown).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (crane.getMotorVertical() != Crane.MOVE_RIGHT_DOWN) {
                    crane.setMotorVertical(Crane.MOVE_RIGHT_DOWN);
                    craneSocket.sendCrane(crane);
                }

                v.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                crane.setMotorVertical(Crane.STOP);
                craneSocket.sendCrane(crane);

                v.setPressed(false);
                v.performClick();
                return true;
            }
            return false;
        });

        craneSocket = new CraneWebSocketClient();
        craneSocket.setWebSocketObserver(new CraneWebSocketClient.WebSocketObserver() {
            @Override
            public void craneChanged(Crane crane) {
                runOnUiThread(() -> {
                    // store the locomotive locally, including it's ID
                    CraneDAO dao = new CraneDAO();
                    dao.copy(crane, MainActivity.this.crane);
                    MainActivity.this.crane.setId(crane.getId());
                });
            }

            @Override
            public void connectionEstablished() {

            }

            @Override
            public void connectionClosed() {
                
            }

            @Override
            public void errorOccurred(Throwable throwable) {
                Spinner craneSpinner = findViewById(R.id.craneSpinner);
                craneSpinner.setSelection(-1);
            }
        });
    }
}
