package com.gc.mqttclient.exceptions;

import com.gc.mqttclient.database.Database;
import com.gc.mqttclient.model.Connection;

/**
 * Persistence Exception, defines an error with persisting a {@link Connection}
 * fails. Example operations are {@link com.gc.mqttclient.Persistence#persistConnection(Connection)} and {@link Database#restoreConnections(android.content.Context)};
 * these operations throw this exception to indicate unexpected results occurred when performing actions on the database.
 *
 */
public class DatabaseException extends Exception {

  /**
   * Creates a persistence exception with the given error message
   * @param message The error message to display
   */
  public DatabaseException(String message) {
    super(message);
  }

  /** Serialisation ID**/
  private static final long serialVersionUID = 5326458803268855071L;

}
