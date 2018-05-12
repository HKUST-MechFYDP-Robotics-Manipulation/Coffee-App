/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_tutorial_pubsub;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.coffee_app.ItemAdapter;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.rosjava_tutorial_pubsub.Talker;
import org.ros.rosjava_tutorial_pubsub.Listener;

import android.widget.AdapterView;
import android.widget.ListView;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

  private RosTextView<std_msgs.String> rosTextView;
  private Talker talker;
  private Listener listener;
  private RosTextView<std_msgs.String> Robotend;
  private TextView Androidend;

  public MainActivity() {
    // The RosActivity constructor configures the notification title and ticker
    // messages.
    super("CoffeeApp", "CoffeeApp");
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {
    talker = new Talker();
    listener = new Listener();
    // At this point, the user has already been prompted to either enter the URI
    // of a master to use or to start a master locally.

    // The user can easily use the selected ROS Hostname in the master chooser
    // activity.
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());
    nodeMainExecutor.execute(talker, nodeConfiguration);
    // The RosTextView is also a NodeMain that must be executed in order to
    // start displaying incoming messages.
    //nodeMainExecutor.execute(rosTextView, nodeConfiguration);
    nodeMainExecutor.execute(Robotend, nodeConfiguration);
    nodeMainExecutor.execute(listener, nodeConfiguration);
  }

  ListView myListView;
  String[] items;
  String[] prices;
  int[] coffeepic = {
          R.drawable.hearts,
          R.drawable.leaf,
          R.drawable.bw,
          R.drawable.flower,
          R.drawable.swirl};

  @Override
  @SuppressWarnings("unchecked")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_second);

    startService(new Intent(this, Listener.class));

    //configuration of publisher message
    Robotend = (RosTextView<std_msgs.String>) findViewById(R.id.robotend);
    Robotend.setTopicName("/chatter");//"robottophne"
    Robotend.setMessageType(std_msgs.String._TYPE);
    Robotend.setMessageToStringCallable(new MessageCallable<String, std_msgs.String>() {
      @Override
      public String call(std_msgs.String message) {
        return message.getData();
      }
    });

    //generate menu
    Resources res = getResources();
    myListView = (ListView) findViewById(R.id.myListView);
    items = res.getStringArray(R.array.items);
    prices = res.getStringArray(R.array.prices);

    ItemAdapter itemAdapter = new ItemAdapter(this,items,prices,null,coffeepic);
    myListView.setAdapter(itemAdapter);

    //show confirmation dialog after customer clicked on list item
    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Confirmation");
        mBuilder.setMessage("Confirm order?");
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {

        //for confirming order
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

          dialogInterface.dismiss(); // close the dialogue

          //set message as the position of ordered item
          talker.c = position;
          talker.click( 1);

          //clear received_msg first to
          if ("ready".equals(listener.received_msg)) {
            listener.resetReceived_msg();
          }

          //for checking the received_msg
          Androidend = (TextView) findViewById(R.id.androidend);
          Androidend.setText(listener.received_msg);

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          listener.getReceived_msg();
          while ("waiting".equals(listener.received_msg)) {
            //keep updating received_msg
            listener.getReceived_msg();
            //create notification if "ready" message from ROS is received
            if ("ready".equals(listener.received_msg)) {
              createNotification();
            }
          }

          //reset order to zero
          talker.clickclear();

        }
      });

        //for cancelling order
        mBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          talker.clickclear();
          listener.resetReceived_msg();
          dialogInterface.dismiss();
        }
      });
      AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
      }
    });
  }

  //configuration of notification
  private void createNotification() {

    Intent intent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

    Notification notification = new Notification.Builder(this)
            .setContentTitle("Your coffee is ready.")
            .setContentText("Please collect at the counter.")
            .setSmallIcon(R.mipmap.icon)
            .setContentIntent(pendingIntent)
            .build();

    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notificationManager.notify(0, notification);
  }
}