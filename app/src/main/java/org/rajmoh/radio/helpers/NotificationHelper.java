/**
 * NotificationHelper.java
 * Implements the NotificationHelper class
 * A NotificationHelper creates and configures a notification
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-17 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.rajmoh.radio.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import org.rajmoh.radio.MainActivity;
import org.rajmoh.radio.PlayerService;
import org.rajmoh.radio.R;
import org.rajmoh.radio.core.Station;


/**
 * NotificationHelper class
 */
public final class NotificationHelper implements TransistorKeys {

    /* Define log tag */
    private static final String LOG_TAG = NotificationHelper.class.getSimpleName();


    /* Main class variables */
    private static Notification mNotification;
    private static Service mService;
    private static MediaSessionCompat mSession;
    private static String mStationMetadata;


    /* Create and put up notification */
    public static void show(final Service service, MediaSessionCompat session, Station station, int stationID, String stationMetadata) {
        // save service and session
        mService = service;
        mSession = session;
        mStationMetadata = stationMetadata;

        // build notification
        station.setPlaybackState(true);
        mNotification = getNotificationBuilder(station, stationID, mStationMetadata).build(); // TODO: change -> Station object contains metadata, too

        // display notification
        service.startForeground(PLAYER_SERVICE_NOTIFICATION_ID, mNotification);
    }


    /* Updates the notification */
    public static void update(Station station, int stationID, String stationMetadata, MediaSessionCompat session) {

        // session can be null on update
        if (session != null) {
            mSession = session;
        }

        // metadata can be null on update
        if (stationMetadata != null) {
            mStationMetadata = stationMetadata;
        }

        // build notification
        mNotification = getNotificationBuilder(station, stationID, mStationMetadata).build();

        // display updated notification
        NotificationManager notificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PLAYER_SERVICE_NOTIFICATION_ID, mNotification);

        if (!station.getPlaybackState()) {
            // make notification swipe-able
            mService.stopForeground(false);
        }

    }


    /* Stop displaying notification */
    public static void stop() {
        if (mService != null) {
            mService.stopForeground(true);
        }
    }


    /* Creates a notification builder */
    private static NotificationCompat.Builder getNotificationBuilder(Station station, int stationID, String stationMetadata) {

        // explicit intent for notification tap
        Intent tapActionIntent = new Intent(mService, MainActivity.class);
        tapActionIntent.setAction(ACTION_SHOW_PLAYER);
        tapActionIntent.putExtra(EXTRA_STATION, station);
        tapActionIntent.putExtra(EXTRA_STATION_ID, stationID);

        // explicit intent for stopping playback
        Intent stopActionIntent = new Intent(mService, PlayerService.class);
        stopActionIntent.setAction(ACTION_STOP);

        // explicit intent for starting playback
        Intent playActionIntent = new Intent(mService, PlayerService.class);
        playActionIntent.setAction(ACTION_PLAY);

        // explicit intent for swiping notification
        Intent swipeActionIntent = new Intent(mService, PlayerService.class);
        swipeActionIntent.setAction(ACTION_DISMISS);

        // explicit intent for next playback
        Intent nextActionIntent = new Intent(mService, PlayerService.class);
        nextActionIntent.setAction(ACTION_NEXT);


        // explicit intent for previous playback
        Intent previousActionIntent = new Intent(mService, PlayerService.class);
        previousActionIntent.setAction(ACTION_PREVIOUS);



        // artificial back stack for started Activity.
        // -> navigating backward from the Activity leads to Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mService);
//        // backstack: adds back stack for Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
        // backstack: add explicit intent for notification tap
        stackBuilder.addNextIntent(tapActionIntent);

        // pending intent wrapper for notification tap
        PendingIntent tapActionPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent tapActionPendingIntent = PendingIntent.getService(mService, 0, tapActionIntent, 0);
        // pending intent wrapper for notification stop action
        PendingIntent stopActionPendingIntent = PendingIntent.getService(mService, 10, stopActionIntent, 0);
        // pending intent wrapper for notification start action
        PendingIntent playActionPendingIntent = PendingIntent.getService(mService, 11, playActionIntent, 0);
        // pending intent wrapper for notification swipe action
        PendingIntent swipeActionPendingIntent = PendingIntent.getService(mService, 12, swipeActionIntent, 0);

        // pending intent wrapper for notification swipe action
        PendingIntent skipNextActionPendingIntent = PendingIntent.getService(mService, 13, nextActionIntent, 0);
// pending intent wrapper for notification swipe action
        PendingIntent skipPreviousActionPendingIntent = PendingIntent.getService(mService, 14, previousActionIntent, 0);


        // create media style
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setMediaSession(mSession.getSessionToken());
        style.setShowActionsInCompactView(0);
        style.setShowCancelButton(true); // pre-Lollipop workaround
        style.setCancelButtonIntent(swipeActionPendingIntent);

        // construct notification in builder
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(mService);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.ic_notificaiton_radio_icon);
        builder.setLargeIcon(getStationIcon(mService, station));
        builder.setContentTitle(station.getStationName());
        builder.setContentText(stationMetadata);
        builder.setShowWhen(false);
        builder.setStyle(style);
        builder.setContentIntent(tapActionPendingIntent);
        builder.setDeleteIntent(swipeActionPendingIntent);
        builder.addAction(R.drawable.ic_skip_previous, mService.getString(R.string.notification_previous), skipPreviousActionPendingIntent);

        if (station.getPlaybackState()) {
            builder.addAction(R.drawable.ic_stop_white_36dp, mService.getString(R.string.notification_stop), stopActionPendingIntent);

        } else {
            builder.addAction(R.drawable.ic_play_arrow_white_36dp, mService.getString(R.string.notification_play), playActionPendingIntent);
        }
        builder.addAction(R.drawable.ic_skip_next, mService.getString(R.string.notification_next),skipNextActionPendingIntent );

        return builder;
    }


    /* Get station image for notification's large icon */
    private static Bitmap getStationIcon(Context context, Station station) {
        if (station == null) {
            return null;
        }

        // create station image icon
        ImageHelper imageHelper;
        Bitmap stationImage;
        Bitmap stationIcon;

        if (station.getStationImageFile().exists()) {
            // use station image
            stationImage = BitmapFactory.decodeFile(station.getStationImageFile().toString());
        } else {
            stationImage = null;
        }
        imageHelper = new ImageHelper(stationImage, context);
        stationIcon = imageHelper.createStationIcon(512);

        return stationIcon;
    }

}
