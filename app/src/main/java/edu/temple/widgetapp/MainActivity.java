package edu.temple.widgetapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends Activity {

    TextView timezoneTextView;

    LocationListener ll;
    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timezoneTextView = (TextView) findViewById(R.id.timezoneTextView);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        final SharedPreferences.Editor editor = sp.edit();

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent alarmIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                alarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        AppWidgetManager.getInstance(MainActivity.this).getAppWidgetIds(
                                new ComponentName(getPackageName(), ExampleWidget.class.getName())
                        ));

                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);




                if (isChecked) {
                    am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 5000, 60000, pi);
                } else {
                    am.cancel(pi);
                }

                editor.putBoolean("alarm_set", isChecked);
                editor.apply();
            }
        });


        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(Uri.parse("content://edu.temple.widgetapp.provider.TIME_ZONE"), null, null, null, null);


        SimpleCursorAdapter sca = new SimpleCursorAdapter(this
                , android.R.layout.simple_list_item_1
        ,c
        ,new String[] {"timezone"}
        ,new int[] {android.R.id.text1}
        , 0);

        c.close();


        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(sca);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editor.putString("timezone", ((TextView) view).getText().toString());
                editor.commit();
            }
        });

        findViewById(R.id.timezoneTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("timezone", timezoneTextView.getText().toString());
                editor.commit();
            }
        });

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                new Thread() {
                    public void run ()

                    {
                        String urlString = "https://maps.googleapis.com/maps/api/timezone/json?location="
                                + location.getLatitude()
                                + ","
                                + location.getLongitude()
                                + "&timestamp=0";

                        Log.d("request string", urlString);
                        StringBuilder responseStingBuilder = new StringBuilder();
                        try

                        {
                            URL url = new URL(urlString);

                            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                            String tmpString;

                            while ((tmpString = br.readLine()) != null) {
                                responseStingBuilder.append(tmpString);
                            }

                            String response = responseStingBuilder.toString();

                            Log.d("Server response", response);

                            JSONObject responseObject = new JSONObject(response);

                            Message msg = Message.obtain();

                            msg.obj = responseObject.get("timeZoneId");

                            timezoneUpdateHandler.sendMessage(msg);

                        } catch (
                                Exception e
                                )

                        {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    Handler timezoneUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            timezoneTextView.setText(msg.obj.toString());
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(ll);


    }
}
