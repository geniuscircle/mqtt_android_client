package com.gc.mqttclient.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.gc.mqttclient.R;
import com.gc.mqttclient.constants.ActivityConstants;
import com.gc.mqttclient.enums.Action;
import com.gc.mqttclient.enums.ConnectionStatus;
import com.gc.mqttclient.handler.ConnectionHandler;
import com.gc.mqttclient.model.Connection;
import com.gc.mqttclient.ui.activities.ClientConnectionsActivity;
import com.gc.mqttclient.ui.activities.ConnectionDetailsActivity;
import com.gc.mqttclient.ui.activities.NewConnectionActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.LogManager;

/**
 * Deals with actions performed in the {@link ClientConnectionsActivity} activity
 * and the {@link ConnectionDetailsActivity} activity and associated fragments
 *
 */
public class UIElementListener implements OnMenuItemClickListener {

  /** The handle to a {@link Connection} object which contains the {@link MqttAndroidClient} associated with this object **/
  private String clientHandle = null;

  /** {@link ConnectionDetailsActivity} reference used to perform some actions**/
  private ConnectionDetailsActivity connectionDetails = null;
  /** {@link ClientConnectionsActivity} reference used to perform some actions**/
  private ClientConnectionsActivity clientConnections = null;
  /** {@link Context} used to load and format strings **/
  private Context context = null;

  /** Whether Paho is logging is enabled**/
  public static boolean logging = false;

  /**
   * Constructs a listener object for use with {@link ConnectionDetailsActivity} activity and
   * associated fragments.
   * @param connectionDetails The instance of {@link ConnectionDetailsActivity}
   * @param clientHandle The handle to the client that the actions are to be performed on
   */
  public UIElementListener(ConnectionDetailsActivity connectionDetails, String clientHandle)
  {
    this.connectionDetails = connectionDetails;
    this.clientHandle = clientHandle;
    context = connectionDetails;

  }

  /**
   * Constructs a listener object for use with {@link ClientConnectionsActivity} activity.
   * @param clientConnections The instance of {@link ClientConnectionsActivity}
   */
  public UIElementListener(ClientConnectionsActivity clientConnections) {
    this.clientConnections = clientConnections;
    context = clientConnections;
  }

  /**
   * Perform the needed action required based on the button that
   * the user has clicked.
   * 
   * @param item The menu item that was clicked
   * @return If there is anymore processing to be done
   * 
   */
  @Override
  public boolean onMenuItemClick(MenuItem item) {

    int id = item.getItemId();

    switch (id)
    {
      case R.id.publish :
        publish();
        break;
      case R.id.subscribe :
        subscribe();
        break;
      case R.id.newConnection :
        createAndConnect();
        break;
      case R.id.disconnect :
        disconnect();
        break;
      case R.id.connectMenuOption :
        reconnect();
        break;
    }

    return false;
  }

  /**
   * Reconnect the selected client
   */
  private void reconnect() {

    ConnectionHandler.getInstance(context).getConnection(clientHandle).changeConnectionStatus(ConnectionStatus.CONNECTING);

    Connection c = ConnectionHandler.getInstance(context).getConnection(clientHandle);
    try {
      c.getClient().connect(c.getConnectionOptions(), null, new ActionListener(context, Action.CONNECT, clientHandle, null));
    }
    catch (MqttSecurityException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
      c.addAction("Client failed to connect");
    }
    catch (MqttException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
      c.addAction("Client failed to connect");
    }

  }

  /**
   * Disconnect the client
   */
  private void disconnect() {

    Connection c = ConnectionHandler.getInstance(context).getConnection(clientHandle);

    //if the client is not connected, process the disconnect
    if (!c.isConnected()) {
      return;
    }

    try {
      c.getClient().disconnect(null, new ActionListener(context,Action.DISCONNECT, clientHandle, null));
      c.changeConnectionStatus(ConnectionStatus.DISCONNECTING);
    }
    catch (MqttException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to disconnect the client with the handle " + clientHandle, e);
      c.addAction("Client failed to disconnect");
    }

  }

  /**
   * Subscribe to a etxt_subscribe_topic that the user has specified
   */
  private void subscribe()
  {
    String etxt_subscribe_topic = ((EditText) connectionDetails.findViewById(R.id.etxt_subscribe_topic)).getText().toString();
    ((EditText) connectionDetails.findViewById(R.id.etxt_subscribe_topic)).getText().clear();

    RadioGroup radio = (RadioGroup) connectionDetails.findViewById(R.id.rg_subscribe_qos);
    int checked = radio.getCheckedRadioButtonId();
    int qos = ActivityConstants.defaultQos;

    switch (checked) {
      case R.id.rb_subscribe_qos_0 :
        qos = 0;
        break;
      case R.id.rb_subscribe_qos_1 :
        qos = 1;
        break;
      case R.id.rb_subscribe_qos_2 :
        qos = 2;
        break;
    }

    try {
      String[] topics = new String[1];
      topics[0] = etxt_subscribe_topic;
      ConnectionHandler.getInstance(context).getConnection(clientHandle).getClient()
          .subscribe(etxt_subscribe_topic, qos, null, new ActionListener(context, Action.SUBSCRIBE, clientHandle, topics));
    }
    catch (MqttSecurityException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + etxt_subscribe_topic + " the client with the handle " + clientHandle, e);
    }
    catch (MqttException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + etxt_subscribe_topic + " the client with the handle " + clientHandle, e);
    }
  }

  /**
   * Publish the message the user has specified
   */
  private void publish()
  {
    String etxt_publish_topic = ((EditText) connectionDetails.findViewById(R.id.etxt_publish_topic))
        .getText().toString();

 //   ((EditText) connectionDetails.findViewById(R.id.txt_publish_topic)).getText().clear();

    String message = ((EditText) connectionDetails.findViewById(R.id.etxt_publish_message)).getText()
        .toString();

    ((EditText) connectionDetails.findViewById(R.id.etxt_publish_message)).getText().clear();

    RadioGroup radio = (RadioGroup) connectionDetails.findViewById(R.id.rg_publish_qos);
    int checked = radio.getCheckedRadioButtonId();
    int qos = ActivityConstants.defaultQos;

    switch (checked) {
      case R.id.rb_publish_qos_0 :
        qos = 0;
        break;
      case R.id.rb_publish_qos_1 :
        qos = 1;
        break;
      case R.id.rb_publish_qos_2 :
        qos = 2;
        break;
    }

    boolean cb_publish_retain = ((CheckBox) connectionDetails.findViewById(R.id.cb_publish_retain))
        .isChecked();

    String[] args = new String[2];
    args[0] = message;
    args[1] = etxt_publish_topic+";qos:"+qos+";cb_publish_retain:"+cb_publish_retain;

    try {
      ConnectionHandler.getInstance(context).getConnection(clientHandle).getClient()
          .publish(etxt_publish_topic, message.getBytes(), qos, cb_publish_retain, null, new ActionListener(context, Action.PUBLISH, clientHandle, args));
    }
    catch (MqttSecurityException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
    }
    catch (MqttException e) {
      Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
    }

  }

  /**
   * Create a new client and connect
   */
  private void createAndConnect()
  {
    Intent createConnection;

    //start a new activity to gather information for a new connection
    createConnection = new Intent();
    createConnection.setClassName(
        clientConnections.getApplicationContext(),
            NewConnectionActivity.class.getName());

    clientConnections.startActivityForResult(createConnection,
        ActivityConstants.connect);
  }

  /**
   * Enables logging in the Paho MQTT client
   */
  private void enablePahoLogging() {

    /*try {
      InputStream logPropStream = context.getResources().openRawResource(R.raw.jsr47android);
      LogManager.getLogManager().readConfiguration(logPropStream);
      logging = true;
         
      HashMap<String, Connection> connections = (HashMap<String,Connection>)Connections.getInstance(context).getConnections();
      if(!connections.isEmpty()){
    	  Entry<String, Connection> entry = connections.entrySet().iterator().next();
    	  Connection connection = (Connection)entry.getValue();
    	  connection.getClient().setTraceEnabled(true);
    	  //change menu state.
    	  clientConnections.invalidateOptionsMenu();
    	  //Connections.getInstance(context).getConnection(clientHandle).getClient().setTraceEnabled(true);
      }else{
    	  Log.i("SampleListener","No connection to enable log in service");
      }
    }
    catch (IOException e) {
      Log.e("MqttAndroidClient",
          "Error reading logging parameters", e);
    }
*/
  }

  /**
   * Disables logging in the Paho MQTT client
   */
  private void disablePahoLogging() {
    LogManager.getLogManager().reset();
    logging = false;
    
    HashMap<String, Connection> connections = (HashMap<String,Connection>) ConnectionHandler.getInstance(context).getConnections();
    if(!connections.isEmpty()){
  	  Entry<String, Connection> entry = connections.entrySet().iterator().next();
  	  Connection connection = (Connection)entry.getValue();
  	  connection.getClient().setTraceEnabled(false);
  	  //change menu state.
  	  clientConnections.invalidateOptionsMenu();
    }else{
  	  Log.i("SampleListener","No connection to disable log in service");
    }
    clientConnections.invalidateOptionsMenu();
  }

}
