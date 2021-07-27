  package com.example.wearabledevices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;


public class MainActivity extends AppCompatActivity {

    TextView email, name, devicesW, data;
    ImageView imageView;
    Button signoutBtn;
    GoogleSignInClient googleSignInClient;
    ImageButton right;
    ImageButton left;
    Button getDataBtn;
    boolean isGotData = true;
    int toDay, fromDay, month, year;
    GoogleSignInAccount account;
    String month_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = (TextView) findViewById(R.id.email);
        imageView = (ImageView) findViewById(R.id.profileImage);
        name = (TextView) findViewById(R.id.name);
        devicesW = (TextView) findViewById(R.id.devices);
        data = (TextView) findViewById(R.id.data);
        signoutBtn = (Button) findViewById(R.id.logoutBtn);
        left = (ImageButton) findViewById(R.id.arrowleft);
        right = (ImageButton) findViewById(R.id.arrowright);
        getDataBtn =(Button) findViewById(R.id.getDataBtn);
        TextView dataTextView = findViewById(R.id.dateTextView);
        getAccount();
        Intent signIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, 0);
        devicesW.setText("");
        //getTime() returns the current date in default time zone
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        Calendar calendar2 = Calendar.getInstance(TimeZone.getDefault());
        Date date = calendar.getTime();
        toDay = calendar.get(Calendar.DATE);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        calendar.add(Calendar.DATE, -7);
        fromDay = calendar.get(Calendar.DATE);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        month_name = month_date.format(calendar.getTime());
        dataTextView.setText(fromDay + "-" + toDay + " " + month_name);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDay = calendar.get(Calendar.DATE);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
                month_name = month_date.format(calendar.getTime());
                calendar.add(Calendar.DATE, -7);
                fromDay = calendar.get(Calendar.DATE);
                dataTextView.setText(fromDay + "-" + toDay + " " + month_name);
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toDay == calendar2.get(Calendar.DATE)){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "The day hasn't come yet", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    fromDay = calendar.get(Calendar.DATE);
                    calendar.add(Calendar.DATE, 7);
                    month_name = month_date.format(calendar.getTime());
                    toDay = calendar.get(Calendar.DATE);
                    month = calendar.get(Calendar.MONTH);
                    year = calendar.get(Calendar.YEAR);
                    dataTextView.setText(fromDay + "-" + toDay + " " + month_name);
                }
            }
        });
        getDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signoutBtn.getText().equals("Sign in")){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Log in to your account", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    try {
                        getGoogleFitData(account);
                        data.setText("");
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signoutBtn.getText().equals("Sign out")) {
                    googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            signoutBtn.setText("Sign in");
                            name.setText("Name");
                            email.setText("Email");
                            devicesW.setText("Devices");
                            data.setText("Data");
                            imageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.account));
                        }
                    });
                } else {
                    startActivityForResult(signIntent, 0);
                    signoutBtn.setText("Sign out");
                }
            }
        });
    }

    private void getAccount(){
        GoogleSignInOptionsExtension fitnessOptions = FitnessOptions.builder()
                .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ).build();

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .addExtension(fitnessOptions).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);
                if (account != null) {
                    email.setText(account.getEmail());
                    name.setText(account.getDisplayName());
                    Picasso.get().load(account.getPhotoUrl()).placeholder(R.drawable.account).into(imageView);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void getGoogleFitData(GoogleSignInAccount mSignInAccount) throws ExecutionException, InterruptedException {
        DataReadRequest readRequest = queryFitnessData();
        Task<DataReadResponse> response = Fitness.getHistoryClient(this, mSignInAccount)
                .readData(readRequest).addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                printData(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("GoogleFit", "There was a problem reading the data.", e);
                            }
                        });

    }

    private DataReadRequest queryFitnessData() {
      Calendar cal2 = new GregorianCalendar(year, month, toDay);
      long endTime = cal2.getTimeInMillis();
      cal2.add(Calendar.WEEK_OF_YEAR, -1);
       long startTime = cal2.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.i("TAG", "Range Start: " + dateFormat.format(startTime));
        Log.i("TAG", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();
        return readRequest;
    }

    public void printData(DataReadResponse dataReadResult) {
        devicesW.setText("");
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i("TAG", "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i("TAG", "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i("TAG", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            devicesW.append(dp.getOriginalDataSource().getDevice().getManufacturer() + " " + dp.getOriginalDataSource().getDevice().getModel() + "\n");
            data.append("start time - " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + "\n");
            Log.i("TAG", "Device" + dp.getOriginalDataSource().getDevice());
            Log.i("TAG", "Data returned for Data type: " + dataSet.getDataType().getName());
            Log.i("TAG", "Data point:");
            Log.i("TAG", "\tType: " + dp.getDataType().getName());
            Log.i("TAG", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i("TAG", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i("TAG", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                data.append("steps - " + dp.getValue(field) + "\n");
            }
            data.append("end time - " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + "\n");
        }
    }

}