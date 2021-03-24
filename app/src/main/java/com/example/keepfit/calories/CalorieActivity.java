package com.example.keepfit.calories;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.keepfit.MainActivity;
import com.example.keepfit.StartLivestreamActivity;
import com.example.keepfit.VideoActivity;
import com.example.keepfit.VideoUploadActivity;
import com.example.keepfit.authapp.ProfileActivityEdits;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.keepfit.R;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalorieActivity extends AppCompatActivity {

    TextView myTitle;

    Spinner spinner1;

    Spinner spinner2;

    TextView timerTextView; //myTimer

    Button startstop;
    Button pausestart;
    Button submit1;
    Button submit2;
    EditText editTextTime;

    String myCurrentValue;

    TextView TotalCalories;
    Button ClearCalories;

    double myTotalCalories;



    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    /*
    DatabaseReference mConditionRef = mRootRef.child("METValues");

    DatabaseReference mConditionRef1 = mConditionRef.child("Aerobic");
    DatabaseReference mConditionRef2 = mConditionRef.child("Anaerobic");
    DatabaseReference mConditionRef3 = mConditionRef.child("Flexibility");
    DatabaseReference mConditionRef4 = mConditionRef.child("Stability");
    */

    DatabaseReference mConditionRef = mRootRef.child("CaloriesTable");





    long startTime = 0;
    long timeInPause = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerMyHandler = new Handler();
    Runnable timerMyRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerMyHandler.postDelayed(this, 500);
        }
    };

    public class METValue {

        public String username;
        public double myCaloriesBurned;
        public double myTime;
        public String exerciseTitle;

        public METValue() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public METValue(String username, double myCaloriesBurned, double myTime, String exerciseTitle) {
            this.username = username;
            this.myCaloriesBurned = myCaloriesBurned;
            this.myTime = myTime;
            this.exerciseTitle = exerciseTitle;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calorie);

        //navbar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set calorie selected
        bottomNavigationView.setSelectedItemId(R.id.nav_calorie);

        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){
                switch (menuItem.getItemId()){
                    case R.id.nav_account:
                        startActivity(new Intent(getApplicationContext(), ProfileActivityEdits.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_search:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_calorie:
                        return true;
                    case R.id.nav_upload:
                        startActivity(new Intent(getApplicationContext(), VideoUploadActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_livestream:
                        startActivity(new Intent(getApplicationContext(), StartLivestreamActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        myTotalCalories = 0;

        timerTextView = (TextView) findViewById(R.id.myTimer);

        startstop = (Button) findViewById(R.id.startstop);
        startstop.setText("start");
        pausestart = (Button) findViewById(R.id.pausestart);
        pausestart.setText("unpause");
        startstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("stop")) {
                    timerMyHandler.removeCallbacks(timerMyRunnable);
                    timeInPause = System.currentTimeMillis() - startTime;
                    b.setText("start");
                    pausestart.setText("unpause");
                } else {
                    startTime = System.currentTimeMillis();
                    timerMyHandler.postDelayed(timerMyRunnable, 0);
                    pausestart.setText("pause");
                    b.setText("stop");
                }
            }
        });
        pausestart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("pause")) { //Start again
                    timerMyHandler.removeCallbacks(timerMyRunnable);
                    timeInPause = System.currentTimeMillis() - startTime;
                    b.setText("unpause");
                    startstop.setText("start");
                } else { //Stop the timer
                    startTime = System.currentTimeMillis() - timeInPause;
                    timerMyHandler.postDelayed(timerMyRunnable, 0);
                    b.setText("pause");
                    startstop.setText("stop");
                }
            }
        });
        submit1 = (Button) findViewById(R.id.submit1);
        submit1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button) v;

                Spinner myFirstSpinner = (Spinner)findViewById((R.id.spinner1));
                Spinner mySecondSpinner = (Spinner)findViewById((R.id.spinner2));

                String myFirstValue = myFirstSpinner.getSelectedItem().toString();
                String mySecondValue = mySecondSpinner.getSelectedItem().toString();

                DatabaseReference myMET = mRootRef.child("METValues").child(myFirstValue).child(mySecondValue);

                Log.d("myTag", myFirstValue);
                Log.d("myTag", mySecondValue);

                myMET.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myCurrentValue = snapshot.getValue().toString();
                        if (myCurrentValue == null){
                            Toast.makeText(CalorieActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("myTag", myCurrentValue);

                        double MET_Exercise = Double.parseDouble(myCurrentValue);

                        SharedPreferences sharedPref = getSharedPreferences("main", Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username", null);

                        String myTempWeight = sharedPref.getString("weight", null);

                        double weight = Double.parseDouble(myTempWeight); //lb



                        long millis = timeInPause;
                        int seconds = (int) (millis / 1000);
                        int minutes = seconds / 60;

                        double doubleSeconds = (double) seconds;

                        double time = doubleSeconds / 3600.0;

                        //////////////

                        weight = weight/2.205; //Weight in kg.

                        double hours = doubleSeconds/3600.0; //Time in hours

                        double NewMETValue = MET_Exercise * weight * hours;

                        METValue myMETValue = new METValue(username, NewMETValue, doubleSeconds, mySecondValue);

                        mConditionRef.push().setValue(myMETValue);

                        TotalCalories = (TextView)findViewById((R.id.TotalCalories));
                        myTotalCalories = myTotalCalories + NewMETValue;
                        BigDecimal bd = new BigDecimal(Double.toString(myTotalCalories));
                        myTotalCalories = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                        String myDisplayValue = Double.toString(myTotalCalories);
                        TotalCalories.setText(myDisplayValue + " Total Calories Burned!");

                        Context context = getApplicationContext();
                        CharSequence text = "Exercise Recorded!";
                        int duration = Toast.LENGTH_SHORT;
                        startTime=0;
                        timerTextView.setText(String.format("%d:%02d", 0, 0));
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });
        editTextTime = (EditText) findViewById(R.id.editTextTime);
        submit2 = (Button) findViewById((R.id.submit2));
        submit2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button) v;

                Spinner myFirstSpinner = (Spinner)findViewById((R.id.spinner1));
                Spinner mySecondSpinner = (Spinner)findViewById((R.id.spinner2));

                String myFirstValue = myFirstSpinner.getSelectedItem().toString();
                String mySecondValue = mySecondSpinner.getSelectedItem().toString();

                DatabaseReference myMET = mRootRef.child("METValues").child(myFirstValue).child(mySecondValue);


                myMET.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myCurrentValue = snapshot.getValue().toString();

                        double MET_Exercise = Double.parseDouble(myCurrentValue);

                        SharedPreferences sharedPref = getSharedPreferences("main", Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username", null);

                        String myTempWeight = sharedPref.getString("weight", null);

                        double weight = Double.parseDouble(myTempWeight); //lb

                        String myString = editTextTime.getText().toString();

                        String[] myParsedString = myString.split(":");

                        int myMinutes = (Integer.parseInt(myParsedString[0]))*60;
                        int mySeconds = (Integer.parseInt(myParsedString[1]));
                        double doubleSeconds = (double) myMinutes + (double) mySeconds;

                        ////////////////////////////////////////////////

                        weight = weight/2.205; //Weight in kg.

                        double hours = doubleSeconds/3600.0; //Time in hours

                        double NewMETValue = MET_Exercise * weight * hours;

                        METValue myMETValue = new METValue(username, NewMETValue, doubleSeconds, mySecondValue);

                        mConditionRef.push().setValue(myMETValue);

                        TotalCalories = (TextView)findViewById((R.id.TotalCalories));
                        myTotalCalories = myTotalCalories + NewMETValue;
                        BigDecimal bd = new BigDecimal(Double.toString(myTotalCalories));
                        myTotalCalories = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                        String myDisplayValue = Double.toString(myTotalCalories);
                        TotalCalories.setText(myDisplayValue + " Total Calories Burned!");

                        Context context = getApplicationContext();
                        CharSequence text = "Exercise Recorded!";
                        startTime=0;
                        timerTextView.setText(String.format("%d:%02d", 0, 0));
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        ClearCalories = (Button) findViewById((R.id.ClearCalories));
        ClearCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;

                myTotalCalories = 0.0;

                TotalCalories = (TextView)findViewById((R.id.TotalCalories));
                TotalCalories.setText("0");

            }
        });




        }

    @Override
    public void onPause() {
        super.onPause();
        timerMyHandler.removeCallbacks(timerMyRunnable);
        Button b = (Button)findViewById(R.id.startstop);
        b.setText("start");
    }




    @Override
    protected void onStart(){
        super.onStart();

        spinner1 = (Spinner)findViewById((R.id.spinner1));
        spinner2 = (Spinner)findViewById((R.id.spinner2));

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // Get Selected Class name from the list
                String myValue = parent.getItemAtPosition(position).toString();
                switch (myValue)
                {
                    case "Aerobic":
                        // assigning div item list defined in XML to the div Spinner
                        spinner2.setAdapter(new ArrayAdapter<String>(CalorieActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                getResources().getStringArray(R.array.tagEntries2)));
                        break;

                    case "Anaerobic":
                        spinner2.setAdapter(new ArrayAdapter<String>(CalorieActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                getResources().getStringArray(R.array.tagEntries3)));
                        break;

                    case "Flexibility":
                        spinner2.setAdapter(new ArrayAdapter<String>(CalorieActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                getResources().getStringArray(R.array.tagEntries4)));
                        break;

                    case "Stability":
                        spinner2.setAdapter(new ArrayAdapter<String>(CalorieActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                getResources().getStringArray(R.array.tagEntries5)));
                        break;
                }

                //set divSpinner Visibility to Visible
                spinner2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // can leave this empty
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // do something upon option selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // can leave this empty
            }

        });





    }




}