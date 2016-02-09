package com.gc.mqttclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Spanned;
import android.widget.ArrayAdapter;

import com.gc.mqttclient.R;
import com.gc.mqttclient.handler.ConnectionHandler;
import com.gc.mqttclient.model.Connection;

/**
 * This fragment displays the history information for a client
 *
 */
public class HistoryFragment extends ListFragment {

  /** Client handle to a {@link Connection} object **/
  String clientHandle = null;
  /** {@link ArrayAdapter} to display the formatted text **/
  ArrayAdapter<Spanned> arrayAdapter = null;

  /**
   * @see ListFragment#onCreate(Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Pull history information out of bundle

    clientHandle = getArguments().getString("handle");
    Connection connection = ConnectionHandler.getInstance(getActivity()).getConnection(clientHandle);

    Spanned[] history = connection.history();

    //Initialise the arrayAdapter, view and add data
    arrayAdapter = new ArrayAdapter<Spanned>(getActivity(), R.layout.fragment_history);

    arrayAdapter.addAll(history);
    setListAdapter(arrayAdapter);

  }

  /**
   * Updates the data displayed to match the current history
   */
  public void refresh() {
    if (arrayAdapter != null) {
      arrayAdapter.clear();
      arrayAdapter.addAll(ConnectionHandler.getInstance(getActivity()).getConnection(clientHandle).history());
      arrayAdapter.notifyDataSetChanged();
    }

  }

}
