/*
 * This is an example of how the AlarmManager class is used.
 * The AlarmManager class provides access to the system alarm services. 
 * These allow you to schedule methods to be run at some point in the future. 
 * 
 * When an alarm goes off, the Intent that had been registered for it is broadcasted
 * by the system.  If the target application is not already running it will get
 * automatically started.
 */

package org.turntotech.alarmservice;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {

//	private LinearLayout linerLayout = (LinearLayout) findViewById(R.id.mainLayout);
//	private EditText editText = (EditText) findViewById(R.id.editText);

	private Date today = new Date();
	private SimpleDateFormat simpleDay = new SimpleDateFormat("EEEE");
	private String dayOfWeek = simpleDay.format(today);

	private Calendar cal = Calendar.getInstance();
	private int month = cal.get(Calendar.MONTH);
	private int dateOfMonth = cal.get(Calendar.DAY_OF_MONTH);

	private int hour;
	private int minute;
	private int second;
	private long millisecond;
	private int second2 = second %2;

	private String hourString;
	private String minString;

	public boolean is24 = false;

	private boolean isFlashing = false;

	private Uri alarmSound;
	private Ringtone ringtone;

	final static private long ONE_SECOND = 1000;
	static private long DELAY;// = ONE_SECOND * 5;// Delay set to 5 second - this variable is changed in a later method

	private long alarmHoursInt;
	private long alarmMinsInt;

	private final static String ALARM_KEY = "Bite Me!";

	PendingIntent pi;
	BroadcastReceiver br;
	AlarmManager am;


	//Assignment
	//Create a simple alarm clock where you can set an alarm time. Change the screen color to red
	// at alarm time. Also enable a stop-alarm button. When stop is clicked, the background color
	// changes back to white.  Additionally, add a snooze button that will change the screen to
	// white but then back to red after 10 seconds.

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Log.i("TurnToTech", "Project Name - AlarmService");
		// broadcast receiver that will act as the callback for our alarm.


		setTime();
		runHandler1();
		//runAlarm();


		// 1. Create a class that will receive an event.
		br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				Toast.makeText(c, "Alarm is Going Off", Toast.LENGTH_LONG)
						.show();

				playAlarm();
			}
		};

		// 2. Register the class as a receiver of a specific type of event
		// idenified by a String, in our case, ALARM_KEY defined above

		registerReceiver(br, new IntentFilter(ALARM_KEY));

		// 3. Set up the notification that needs to be fired. We don't fire
		// the event yet.

		pi = PendingIntent.getBroadcast(this, 0, new Intent(ALARM_KEY), 0);

		// 4. Get reference to the alarm service
		am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));

		// 5. Connect the button from the UI to the onClick(..) method below
		findViewById(R.id.startAlarmButton).setOnClickListener(new OnClickListener() {

			@Override
			// In the on click listener, we set the alarm.
			public void onClick(View v) {
				// set() method of the AlarmManager is used to register an
				// Intent to be sent.
				// Set the alarm for particular time

				// 6. When button is tapped, we start the alarm to go off at a
				// specific time and also pass it the pending intent reference
				// so it knows which alarm to trigger


				setAlarmAndDelay();

				alarmConfigure();

				setScreenRed();

			}

		});
	}

	@Override
	/*
	 * 7. We need to remove the pending intent. onDestroy() is called when the activity is destroyed 
	 * 
	 */
	protected void onDestroy() {
		am.cancel(pi);
		unregisterReceiver(br);
		super.onDestroy();
	}

	public void runHandler1() {
		Handler handler1 = new Handler();
		Runnable runnable1 = new Runnable() {
			public void run() {

				today = new Date();
				cal = Calendar.getInstance();

				hour = cal.get(Calendar.HOUR_OF_DAY);
				minute = cal.get(Calendar.MINUTE);
				second = cal.get(Calendar.SECOND);
				millisecond = cal.get(Calendar.MILLISECOND);
				second2 = second %2;

				android.util.Log.d("HANDLER", "Time: Sec " + second);
				android.util.Log.d("HANDLER", "Time: Sec2 " + second2);

				setTime();
				setVisibility();
				makeFlash();

				if(isFlashing == true) {
					flashScreen();
				}
				android.util.Log.d("SECONDS", "SECONDS " + second);
				android.util.Log.d("DELAY", "run: " + DELAY);
				DELAY-=250;
				runHandler1();


			}
		};

		handler1.postDelayed(runnable1, 250);
	}



	//core time functions

	public void setTime() { //sets the time of clock on the UI


		hourString = Integer.toString(hour);
		minString = Integer.toString(minute);

		if(minute < 10){
			minString = "0" + Integer.toString(minute);
		}

		TextView hoursText = (TextView) findViewById(R.id.hoursText);
		TextView minsText = (TextView) findViewById(R.id.minsText);

		hoursText.setText(hourString);
		minsText.setText(minString);

	}




	public void setAlarmAndDelay(){  //gets the time of the alarm

		EditText editTextHours = (EditText) findViewById(R.id.editTextHours);
		EditText editTextMins = (EditText) findViewById(R.id.editTextMins);

		String alarmHoursString = editTextHours.getText().toString();
		String alarmMinsString = editTextMins.getText().toString();

		long alarmHoursLong = Long.parseLong(alarmHoursString);
		long alarmMinsLong = Long.parseLong(alarmMinsString);

		long currentHours = hour;
		long currentMinutes = minute;

		long hourSeconds = (alarmHoursLong - currentHours) * 60 * 60 * 1000; // calculates the delay in milliseconds for hours
		long minsSeconds = (alarmMinsLong - currentMinutes) * 60 * 1000; // calculates the delay in milliseconds for minutes
		long seconds = second * 1000;

		DELAY = (hourSeconds + minsSeconds - seconds);

	}




// Alarm sound and visual effects
	public void playAlarm(){

		alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
		ringtone.play();
		isFlashing = true;


	}

	public void flashScreen(){


			LinearLayout linerLayout = (LinearLayout) findViewById(R.id.mainLayout);

			if (second2 % 2 == 0) {
				linerLayout.setBackgroundColor(Color.WHITE);
			} else {
				linerLayout.setBackgroundColor(Color.RED);
			}

	}


	public void killAlarm(){
		isFlashing = false;
		PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		ringtone.stop();
	}


	public void onClickStop(View view){

		EditText editTextHours = (EditText) findViewById(R.id.editTextHours);
		EditText editTextMins = (EditText) findViewById(R.id.editTextMins);

		editTextHours.getText().clear();
		editTextMins.getText().clear();

		killAlarm();

		setScreenWhite();

	}



	public void onClickSnooze(View view){

		EditText editTextHours = (EditText) findViewById(R.id.editTextHours);
		EditText editTextMins = (EditText) findViewById(R.id.editTextMins);

		editTextHours.getText().clear();
		editTextMins.getText().clear();

		killAlarm();

		DELAY = 1 * 60 * 1000; // sets snooze for 1 minute or 60K milliseconds

		alarmConfigure();

		setScreenSnooze();



	}

	public void alarmConfigure(){
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + DELAY, pi);

	}



	//Visual functions below

	public void setVisibility(){
		TextView colon = (TextView) findViewById(R.id.dots);
		colon.setVisibility(View.VISIBLE);
	}





	public void makeFlash() {
		TextView colon = (TextView) findViewById(R.id.dots);

		if (second2 % 2 == 0) {
			colon.setVisibility(View.INVISIBLE);
		}
	}
	

	public void setScreenRed(){

		LinearLayout linerLayout = (LinearLayout) findViewById(R.id.mainLayout);
		linerLayout.setBackgroundColor(Color.RED);

		TextView zzz = (TextView) findViewById(R.id.zzz);
		zzz.setVisibility(View.GONE);

	}



	public void setScreenWhite(){

		LinearLayout linerLayout = (LinearLayout) findViewById(R.id.mainLayout);
		linerLayout.setBackgroundColor(Color.WHITE);

		TextView zzz = (TextView) findViewById(R.id.zzz);
		zzz.setVisibility(View.GONE);

	}

	public void setScreenSnooze(){

		LinearLayout linerLayout = (LinearLayout) findViewById(R.id.mainLayout);
		linerLayout.setBackgroundColor(Color.GRAY);

		TextView zzz = (TextView) findViewById(R.id.zzz);
		zzz.setVisibility(View.VISIBLE);

	}










}
