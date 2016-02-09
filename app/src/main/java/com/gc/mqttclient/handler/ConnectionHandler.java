package com.gc.mqttclient.handler;

import android.content.Context;

import com.gc.mqttclient.database.Database;
import com.gc.mqttclient.exceptions.DatabaseException;
import com.gc.mqttclient.model.Connection;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>Connections</code> is a singleton class which stores all the connection objects
 * in one central place so they can be passed between activities using a client
 * handle
 *
 */
public class ConnectionHandler {

  /** Singleton instance of <code>Connections</code>**/
  private static ConnectionHandler instance = null;

  /** List of {@link Connection} objects**/
  private HashMap<String, Connection> connections = null;

  /** {@link Database} object used to save, delete and restore connections**/
  private Database persistence = null;

  /**
   * Create a Connections object
   * @param context Applications context
   */
  private ConnectionHandler(Context context)
  {
    connections = new HashMap<String, Connection>();

    //attempt to restore state
    persistence = new Database(context);
    try {
      List<Connection> l = persistence.restoreConnections(context);
      for (Connection c : l) {
        connections.put(c.handle(), c);
      }
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }

  }

  /**
   * Returns an already initialised instance of <code>Connections</code>, if Connections has yet to be created, it will
   * create and return that instance
   * @param context The applications context used to create the <code>Connections</code> object if it is not already initialised
   * @return Connections instance
   */
  public synchronized static ConnectionHandler getInstance(Context context)
  {
    if (instance == null) {
      instance = new ConnectionHandler(context);
    }

    return instance;
  }

  /**
   * Finds and returns a connection object that the given client handle points to
   * @param handle The handle to the <code>Connection</code> to return
   * @return a connection associated with the client handle, <code>null</code> if one is not found
   */
  public Connection getConnection(String handle)
  {

    return connections.get(handle);
  }

  /**
   * Adds a <code>Connection</code> object to the collection of connections associated with this object
   * @param connection connection to add
   */
  public void addConnection(Connection connection)
  {
    connections.put(connection.handle(), connection);
    try {
      persistence.persistConnection(connection);
    }
    catch (DatabaseException e)
    {
      //error persisting well lets just swallow this
      e.printStackTrace();
    }
  }

  /**
   * Create a fully initialised <code>MqttAndroidClient</code> for the parameters given
   * @param context The Applications context
   * @param etxt_newconnection_server The ServerURI to connect to
   * @param clientId The clientId for this client
   * @return new instance of MqttAndroidClient
   */
  public MqttAndroidClient createClient(Context context, String etxt_newconnection_server, String clientId)
  {
    MqttAndroidClient client = new MqttAndroidClient(context, etxt_newconnection_server, clientId);
    return client;
  }

  /**
   * Get all the connections associated with this <code>Connections</code> object.
   * @return <code>Map</code> of connections
   */
  public Map<String, Connection> getConnections()
  {
    return connections;
  }

  /**
   * Removes a connection from the map of connections
   * @param connection connection to be removed
   */
  public void removeConnection(Connection connection) {
    connections.remove(connection.handle());
    persistence.deleteConnection(connection);
  }

}
