package com.gc.mqttclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gc.mqttclient.R;

/**
 * Fragment for the publish message pane.
 *
 */
public class PublishFragment extends Fragment {

  /**
   * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_publish, null);

  }

}
