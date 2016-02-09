package com.gc.mqttclient.handler;

import android.content.Context;
import android.content.Intent;

import com.gc.mqttclient.R;
import com.gc.mqttclient.enums.ConnectionStatus;
import com.gc.mqttclient.handler.ConnectionHandler;
import com.gc.mqttclient.handler.NotificationHandler;
import com.gc.mqttclient.model.Connection;
import com.gc.mqttclient.ui.activities.ConnectionDetailsActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {

  /** {@link Context} for the application used to format and import external strings**/
  private Context context;
  /** Client handle to reference the connection that this handler is attached to**/
  private String clientHandle;

  /**
   * Creates an <code>MqttCallbackHandler</code> object
   * @param context The application's context
   * @param clientHandle The handle to a {@link Connection} object
   */
  public MqttCallbackHandler(Context context, String clientHandle)
  {
    this.context = context;
    this.clientHandle = clientHandle;
  }

  /**
   * @see MqttCallback#connectionLost(Throwable)
   */
  @Override
  public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
    if (cause != null) {
      Connection c = ConnectionHandler.getInstance(context).getConnection(clientHandle);
      c.addAction("Connection Lost");
      c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

      //format string to use a notification text
      Object[] args = new Object[2];
      args[0] = c.getId();
      args[1] = c.getHostName();

      String message = context.getString(R.string.connection_lost, args);

      //build intent
      Intent intent = new Intent();
      intent.setClassName(context, ConnectionDetailsActivity.class.getName());
      intent.putExtra("handle", clientHandle);

      //notify the user
      NotificationHandler.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
    }
  }

  /**
   * @see MqttCallback#messageArrived(String, MqttMessage)
   */
  @Override
  public void messageArrived(String etxt_subscribe_topic, MqttMessage message) throws Exception {

    //Get connection object associated with this object
    Connection c = ConnectionHandler.getInstance(context).getConnection(clientHandle);

    //create arguments to format message arrived notifcation string
    String[] args = new String[2];
    args[0] = new String(message.getPayload());
    args[1] = etxt_subscribe_topic+";qos:"+message.getQos()+";cb_publish_retain:"+message.isRetained();

    //get the string from strings.xml and format
    String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

    //create intent to start activity
    Intent intent = new Intent();
    intent.setClassName(context,ConnectionDetailsActivity.class.getName());
    intent.putExtra("handle", clientHandle);

    //format string args
    Object[] notifyArgs = new String[3];
    notifyArgs[0] = c.getId();
    notifyArgs[1] = new String(message.getPayload());
    notifyArgs[2] = etxt_subscribe_topic;

    //notify the user
    NotificationHandler.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

    //update client history
    c.addAction(messageString);

  }

  /**
   * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // Do nothing
  }

}
