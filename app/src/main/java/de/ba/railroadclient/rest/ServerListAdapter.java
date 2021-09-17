package de.ba.railroadclient.rest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import model.Server;
import model.ServerDAO;

/**
 * The adapter stores items which it got from a web server. The server sends a JSON string where
 *  * the adapter can read from. This class is abstract, child classes will overwrite
 *  * {@link #updateListItems(JSONArray)} and decide what to do with the servers response.
 *  *
 *
 * Simple adapter for a dropdown list. The adapter will update its content from a railroad server.
 * Items are SwitchServer objects used to control a group of railroad switches.
 */
public class ServerListAdapter extends ArrayAdapter<Server> {

    /**
     * URL for communication.
     */
    private String serverURL;

    /**
     * HTTP request que. This queue can be shared with other tasks.
     */
    private final RequestQueue requestQueue;

    /**
     * the Update Handler is responsible for calling the update task
     */
    private final Handler updateHandler;

    /**
     * Will receive the error messages
     */
    private final Response.ErrorListener errorListener;

    /**
     * Create an adapter for a drop doen list.
     *
     * @param context Context of the Android App
     * @param serverURL URL for the railroad server
     * @param requestQueue queu for HTP requests
     * @param errorListener listener to handle errors
     */
    public ServerListAdapter(Context context, String serverURL, RequestQueue requestQueue, Response.ErrorListener errorListener) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        this.serverURL = serverURL;
        this.requestQueue = requestQueue;
        this.errorListener = errorListener;

        // update the list of active servers
        updateHandler = new Handler(Looper.getMainLooper());
        updateHandler.post(updateRunnable);
    }

    /**
     * The Updater will periodically read all active locomotive servers and update the selection
     * spinner. The user can select a server from this list to connect to a switch server.
     */
    private final Runnable updateRunnable = new Runnable() {

        /**
         * first delay is 0 seconds
         */
        private int delay = 0;

        @Override
        public void run() {
            // call this method again in 10 seconds
            updateHandler.postDelayed(updateRunnable, delay);
            delay = 10000;

            // cancel the HTTP request in case of an empty URI
            if (serverURL == null || serverURL.length() == 0) {
                return;
            }

            // get all active locomotive servers
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, serverURL, null, response -> {

                Log.d("updateRunnable", "getRequest: " + response.toString());

                // update the items
                updateListItems(response);

                // update the view element
                notifyDataSetChanged();
            }, errorListener);

            // add the GET action to the request que
            requestQueue.add(getRequest);
        }
    };

    protected void updateListItems(JSONArray response) {
        List<Server> newItems = new ArrayList<>();

        for (int i = 0; i < response.length(); i++)
            try {
                JSONObject object = response.getJSONObject(i);
                ServerDAO dao = new ServerDAO();
                newItems.add(dao.read(object.toString()));
            } catch (Throwable t) {
                Log.e("main", "can not create JSON object", t);
            }

        // remove all older items
        for (int i = 0; i < getCount(); i++) {
            Server item = getItem(i);

            if (!newItems.contains(item)) {
                remove(item);
            }
        }

        // add new items
        for (int i = 0; i < newItems.size(); i++) {
            Server item = newItems.get(i);

            if (getPosition(item) < 0) {
                add(item);
            }
        }
    }

    /**
     * Set a new server URL
     *
     * @param serverURL URL for railroad server
     */
    public void setServerURL(String serverURL) {
        clear();
        this.serverURL = serverURL;
    }
}
