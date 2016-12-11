package com.spy.healthmatic.Doctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spy.healthmatic.Doctor.Utilities.TimeHelpers;
import com.spy.healthmatic.Doctor.adapters.PatientsAdapter;
import com.spy.healthmatic.Global.GlobalConst;
import com.spy.healthmatic.Global.GlobalFunctions;
import com.spy.healthmatic.Model.DrNotes;
import com.spy.healthmatic.Model.Patient;
import com.spy.healthmatic.Model.PatientRef;
import com.spy.healthmatic.Model.Staff;
import com.spy.healthmatic.R;
import com.spy.healthmatic.Welcome.Logout;

import java.util.ArrayList;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Team Name: Team SPY
 * Created by shelalainechan on 2016-10-26.
 */
public class MainDrActivity extends AppCompatActivity implements GlobalConst, SwipeRefreshLayout.OnRefreshListener{

    private Staff doctor;
    private ArrayList<Patient> patients;
    private PatientsAdapter patientsAdapter;
    private CircleProgressView circleProgressView;
    private long numOfPatientsChecked;
    private LinearLayoutManager mLayoutManager;
    private static final int CIRCLE_PROGRESS_VIEW_DELAY = 1000;

    @Bind(R.id.recyler_list) RecyclerView mRecyclerView;
    @Bind(R.id.progress_dialog) ProgressBar mProgressDialog;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dr);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a reference to the staff object
        if (doctor == null) {
            doctor = GlobalFunctions.getStaff(this);
        }

        // Setting Recyclerview
        mRecyclerView.setHasFixedSize(false);
        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.circlePVRim),

                getResources().getColor(R.color.circlePVBar),

                getResources().getColor(R.color.appBarScrim),

                getResources().getColor(R.color.yellow));

        // Setup the circle progress view
        circleProgressView = (CircleProgressView) findViewById(R.id.cpvPatients);

        // Show the title if the toolbar is collapsed
        // Otherwise if the toolbar is expanded, hide the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener(){
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getResources().getString(R.string.strDr) + " " +
                            doctor.getFirstName() + " " +
                            doctor.getLastName()
                    );
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
        getPatientList(false);
   }

    @Override
    protected void onResume() {
        super.onResume();
        getPatientList(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            Intent intent = new Intent(this, Logout.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getPatientList(true);
    }

    /**
     * Get the list of patients from the web server
     * @param isRefresh true -
     */
    private void getPatientList(final boolean isRefresh) {
        Call<ArrayList<Patient>> call = STAFF_API.getAllStaffPatinet(doctor.get_id());
        call.enqueue(new Callback<ArrayList<Patient>>() {
            @Override
            public void onResponse(Call<ArrayList<Patient>> call, Response<ArrayList<Patient>> response) {
                if (!response.isSuccessful()) {
                    Log.d("RETROFIT", "RETROFIT FAILURE - RESPONSE FAIL >>>>> " + response.errorBody());
                    Toast.makeText(MainDrActivity.this,
                            getResources().getString(R.string.strRetroFitFailureMsg),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                patients = response.body();
                loadRecyclerViewElements();
                if (isRefresh) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Patient>> call, Throwable t) {
                Log.d("RETROFIT", "RETROFIT FAILURE >>>>> " + t.toString());
                Toast.makeText(MainDrActivity.this,
                        getResources().getString(R.string.strRetroFitFailureMsg),
                        Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadRecyclerViewElements() {
        numOfPatientsChecked = getPatientsCheckedToday(doctor.getPatientRefs());
        initCircleProgressView();
        mProgressDialog.setVisibility(View.GONE);
        patientsAdapter = new PatientsAdapter(this, patients, doctor);
        mRecyclerView.setAdapter(patientsAdapter);
    }

    /**
     * Initialize the Circle Progress View
     */
    private void initCircleProgressView() {

        // Exit if there are no patients to check
        if(patients == null){
            return;
        }

        // Get the number of patients assigned to this staff
        int patientsSize = patients.size();

        // Setup the number of blocks in the circle based on the number of patients
        circleProgressView.setBlockCount(patientsSize);
        circleProgressView.setUnitScale((float)1.20);
        circleProgressView.setMaxValue(patientsSize);
        circleProgressView.setText(Long.toString(numOfPatientsChecked));
        circleProgressView.setValueAnimated(numOfPatientsChecked, CIRCLE_PROGRESS_VIEW_DELAY);

        TextView textViewPatientNum = (TextView) findViewById(R.id.tvPatientNum);
        textViewPatientNum.setText(Integer.toString(patientsSize) + " ");
    }

    /**
     * Get the number of patients checked relative to the date today
     * @param patientRefs List of patient references
     * @return the number of patients already checked
     */
    private long getPatientsCheckedToday(ArrayList<PatientRef> patientRefs) {
        long numOfPatientsChecked = 0;

        // Get current date
        String dateNow = TimeHelpers.getCurrentDateAndTime(TimeHelpers.FORMAT_YYYMMDD);

        // Go through every patient who are still in the hospital
        for (Patient patient : patients) {
            if (patient.getDischargedDate() != null) {
                if (!patient.getDischargedDate().equals(" ")) {

                    // Check dr notes
                    // The dr notes contains dates when a doctor checks on the patient
                    for (DrNotes drNote : patient.getDrNotes()) {

                        // Check if the note is given by the logged in doctor
                        if (drNote.getDrId().equals(doctor.get_id())) {

                            // Check if one of the checkup dates matches the current date
                            String[] dateChecked = drNote.getDate().split(", ");
                            if (dateChecked[0].equals(dateNow)) {
                                // Increment number of patients checked counter
                                numOfPatientsChecked++;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Returns the number of patients already checked
        return numOfPatientsChecked;
    }
}
