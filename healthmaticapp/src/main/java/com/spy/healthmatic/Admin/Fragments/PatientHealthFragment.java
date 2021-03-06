package com.spy.healthmatic.Admin.Fragments;

//Team Name: Team SPY

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.spy.healthmatic.Admin.AdminAddPatient;
import com.spy.healthmatic.Admin.AdminMainActivity;
import com.spy.healthmatic.Global.GlobalConst;
import com.spy.healthmatic.Model.Insurance;
import com.spy.healthmatic.Model.Patient;
import com.spy.healthmatic.Model.Room;
import com.spy.healthmatic.Model.Staff;
import com.spy.healthmatic.Model.Tab;
import com.spy.healthmatic.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientHealthFragment extends Fragment implements GlobalConst {

    @Bind(R.id.p_wieght)
    TextInputEditText mWeightView;
    @Bind(R.id.p_height)
    TextInputEditText mHeightView;
    @Bind(R.id.p_blood)
    TextInputEditText mBloodView;
    @Bind(R.id.p_occupation)
    TextInputEditText mOccupationView;
    @Bind(R.id.p_condition)
    TextInputEditText mConditionView;
    @Bind(R.id.p_room)
    AutoCompleteTextView mRoomView;
    @Bind(R.id.p_insurance_name)
    TextInputEditText mInsuranceNameView;
    @Bind(R.id.p_insurance_expiry_date)
    EditText mInsuranceExpiryView;
    @Bind(R.id.save_patient_health)
    FloatingActionButton mSavePatientHelathButton;

    private Patient patient;
    ArrayList<Tab> tabs;
    ViewPager mViewPager;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    ArrayAdapter<String> roomsAdapter;
    ArrayList<String> roomsNumbers;


    public PatientHealthFragment() {
        // Required empty public constructor
    }

    public static PatientHealthFragment newInstance(Patient patient, ArrayList<Tab> tabs) {
        PatientHealthFragment fragment = new PatientHealthFragment();
        Bundle args = new Bundle();
        args.putSerializable(TABS, tabs);
        args.putSerializable(PATIENT, patient);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabs = (ArrayList<Tab>) getArguments().getSerializable(TABS);
            patient = (Patient) getArguments().getSerializable(PATIENT);
        }
        mViewPager = ((AdminAddPatient)getActivity()).getViewPagerObject();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_patient_health, container, false);
        ButterKnife.bind(this, rootView);
        if(patient.getBloodType()!=null && !patient.getBloodType().equals("")) {
            setView();
        }
        myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        mInsuranceExpiryView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(focus){
                    showDatePicker();
                }
            }
        });
        setupAdapter();
        return rootView;
    }

    private void setupAdapter() {
        roomsNumbers = new ArrayList<>();
        for (Room room : AdminAddPatient.rooms) {
            roomsNumbers.add(room.getRoom() + " (" + room.isAvailability()+" )");
        }
        roomsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, roomsNumbers);
        mRoomView.setAdapter(roomsAdapter);
    }

    private void setView(){
        Toast.makeText(getActivity(), "Click on button below to save any changes.", Toast.LENGTH_LONG).show();
    }

    public void showDatePicker(){
        new DatePickerDialog(getActivity(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mInsuranceExpiryView.setText(sdf.format(myCalendar.getTime()));
    }

    @OnClick(R.id.p_insurance_expiry_date)
    public void dateViewClicked(){
        showDatePicker();
    }

    @OnClick(R.id.save_patient_health)
    public void savePatientHealthObject(){
        String weight = mWeightView.getText().toString();
        if (TextUtils.isEmpty(weight)) {
            mWeightView.setError("Required.");
            mWeightView.requestFocus();
            return;
        } else {
            patient.setWeight(Integer.parseInt(weight));
            mWeightView.setError(null);
        }
        String height = mHeightView.getText().toString();
        if (TextUtils.isEmpty(height)) {
            mHeightView.setError("Required.");
            mHeightView.requestFocus();
            return;
        } else {
            patient.setHeight(Integer.parseInt(height));
            mHeightView.setError(null);
        }
        String blood = mBloodView.getText().toString();
        if (TextUtils.isEmpty(blood)) {
            mBloodView.setError("Required.");
            mBloodView.requestFocus();
            return;
        } else {
            patient.setBloodType(blood);
            mBloodView.setError(null);
        }
        String occupation = mOccupationView.getText().toString();
        if (TextUtils.isEmpty(occupation)) {
            mOccupationView.setError("Required.");
            mOccupationView.requestFocus();
            return;
        } else {
            patient.setOccupation(occupation);
            mOccupationView.setError(null);
        }

        String condition = mConditionView.getText().toString();
        if (TextUtils.isEmpty(condition)) {
            mConditionView.setError("Required.");
            mConditionView.requestFocus();
            return;
        } else {
            patient.setCondition(condition);
            mConditionView.setError(null);
        }
        String room = mRoomView.getText().toString();
        if (checkIfValidRoom(room)) {
            mRoomView.setError("Invalid.");
            mRoomView.requestFocus();
            return;
        } else {
            patient.setRoom(setRoomNumber(room));
            mRoomView.setError(null);
        }
        Insurance insurance = new Insurance();
        String insuranceName = mInsuranceNameView.getText().toString();
        if (TextUtils.isEmpty(insuranceName)) {
            mInsuranceNameView.setError("Required.");
            mInsuranceNameView.requestFocus();
            return;
        } else {
            insurance.setName(insuranceName);
            mInsuranceNameView.setError(null);
        }
        String insuranceExpiryDate = mInsuranceExpiryView.getText().toString();
        if (TextUtils.isEmpty(insuranceExpiryDate)) {
            mInsuranceExpiryView.setError("Invalid.");
            mInsuranceExpiryView.requestFocus();
            return;
        } else {
            insurance.setExpiryDate(insuranceExpiryDate);
            mInsuranceExpiryView.setError(null);
        }
        patient.setInsurance(insurance);
        if(tabs.size()<3) {
            Tab tab = new Tab("Doctos", 2);
            tabs.add(2, tab);
            mViewPager.getAdapter().notifyDataSetChanged();
        }
        mViewPager.setCurrentItem(2, true);
    }

    private boolean checkIfValidRoom(String room){
        if(room==null || "".equals(room)){
            return true;
        }
        try{
            String roomString = room.split(" ")[0];
            long roomNumber = Long.parseLong(roomString);
            for(Room room1 : AdminAddPatient.rooms){
                if(roomNumber == room1.getRoom()){
                    if(room1.isAvailability()){
                        AdminAddPatient.selectedRoom = room1;
                        return false;
                    }else{
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private int setRoomNumber(String room){
        try{
            String roomString = room.split(" ")[0];
            return Integer.parseInt(roomString);
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

}
