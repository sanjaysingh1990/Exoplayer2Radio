/**
 * DialogRename.java
 * Implements the DialogRename class
 * A DialogRename renames a station after asking the user for a new name
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-17 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.rajmoh.radio.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.rajmoh.radio.R;
import org.rajmoh.radio.core.Station;


/**
 * DialogRename class
 */
public final class DialogRename implements TransistorKeys {

    /* Define log tag */
    private static final String LOG_TAG = DialogRename.class.getSimpleName();


    /* Main class variables */
    private final Activity mActivity;
    private final int mStationID;
    private final Station mStation;


    /* Constructor */
    public DialogRename(Activity activity, Station station, int stationID) {
        mActivity = activity;
        mStation = station;
        mStationID = stationID;
    }


    /* Construct and show dialog */
    public void show() {
        // prepare dialog builder
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        // get input field
        View view = inflater.inflate(R.layout.dialog_rename_station, null);
        final EditText inputField = (EditText) view.findViewById(R.id.dialog_rename_station_input);
        inputField.setText(mStation.getStationName());

        // set dialog view
        builder.setView(view);

        // add rename button
        builder.setPositiveButton(R.string.dialog_button_rename, new DialogInterface.OnClickListener() {
            // listen for click on delete button
            public void onClick(DialogInterface arg0, int arg1) {

                // rename station shortcut
                // TODO implement

                // rename station
                String stationNewName = inputField.getText().toString();

                // send local broadcast
                Intent i = new Intent();
                i.setAction(ACTION_COLLECTION_CHANGED);
                i.putExtra(EXTRA_COLLECTION_CHANGE, STATION_RENAMED);
                i.putExtra(EXTRA_STATION_ID, mStationID);
                i.putExtra(EXTRA_STATION, mStation);
                i.putExtra(EXTRA_STATION_NEW_NAME, stationNewName);
                LocalBroadcastManager.getInstance(mActivity.getApplication()).sendBroadcast(i);

            }
        });

        // add cancel button
        builder.setNegativeButton(R.string.dialog_generic_button_cancel, new DialogInterface.OnClickListener() {
            // listen for click on cancel button
            public void onClick(DialogInterface arg0, int arg1) {
                // do nothing
            }
        });

        // display rename dialog
        builder.show();
    }

}