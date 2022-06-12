package com.hyperlife;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hyperlife.fragment.UserProfileFragment;
import com.hyperlife.fragment.WorkoutFragment;

import java.time.LocalDate;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private DocumentReference docRef;
    private FirebaseFirestore firestore;
    private static final int PHYISCAL_ACTIVITY = 1;
    private String mDate = "";
    private static final String tempEmail = "tempEmail";
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    public BottomNavigationView btmNav;
    public FloatingActionButton btnSetWeight, btnDrinkWater, btnPomodoro;
    private String numberOfCupHadDrink;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSetWeight = findViewById(R.id.set_weigh);
        btnDrinkWater = findViewById(R.id.drink_water_fltbtn);
        btnPomodoro = findViewById(R.id.pomodoro);
        addEvents();
//        getSupportActionBar().hide();
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,HomeFragment.class,null);
        fragmentTransaction.commit();


        btmNav = findViewById(R.id.bottom_nav);
        btmNav.setBackground(null);
        btmNav.setItemIconTintList(null);
        btmNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    handleChangeFragment(item.getItemId());
                    return true;
                }
            });

    }

//    public void handleBtmNavClick(int id) {
//            btmNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//                @Override
//                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                    handleChangeFragment(id);
//                    return true;
//                }
//            });
//    }
    private void addEvents(){
        btnSetWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillForm(Gravity.CENTER,2);
            }
        });
        btnDrinkWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wateredit(Gravity.CENTER, 1);
            }
        });

        btnPomodoro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                startActivity(intent);

            }
        });
    }
    public void handleChangeFragment(int id) {
        if(id == R.id.nav_home){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,HomeFragment.class,null);
            fragmentTransaction.commit();
        }
        if(id == R.id.nav_workout){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, WorkoutFragment.class,null);
            fragmentTransaction.commit();
        }
        if(id == R.id.nav_user){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, UserProfileFragment.class,null);
            fragmentTransaction.commit();
        }
        if(id == R.id.nav_meal){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,MealFragment.class,null);
            fragmentTransaction.commit();
        }
    }
    public void openEditNameDialog(int gravity, int fragmentPos) {
        final Dialog nameDialog = new Dialog(this);
        nameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nameDialog.setContentView(R.layout.layout_dialog_name);

        Window window = nameDialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if (Gravity.CENTER == gravity) {
            nameDialog.setCancelable(true);
        } else {
            nameDialog.setCancelable(false);
        }

        EditText editTextName = nameDialog.findViewById(R.id.edit_text_name);
        AppCompatButton cancelButton = nameDialog.findViewById(R.id.cancel_button_name_edit);
        AppCompatButton saveButton = nameDialog.findViewById(R.id.save_button_name_edit);

        SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
        String theTempEmail = sharedPreferences.getString("Email", "");

        firestore = FirebaseFirestore.getInstance();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextName.getText().toString().isEmpty()) {
                    docRef = firestore.collection("users").document(theTempEmail);
                    docRef.update("name", editTextName.getText().toString());
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("fragmentPosition", fragmentPos);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Name can not be empty!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        nameDialog.show();
    }

    public void openEditGenderDialog(int gravity, int fragmentPos) {
        final Dialog genderDialog = new Dialog(this);
        genderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        genderDialog.setContentView(R.layout.layout_dialog_gender);

        Window window = genderDialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if (Gravity.CENTER == gravity) {
            genderDialog.setCancelable(true);
        } else {
            genderDialog.setCancelable(false);
        }

        ImageView maleImage = genderDialog.findViewById(R.id.male_image_edit);
        ImageView femaleImage = genderDialog.findViewById(R.id.female_image_edit);
        CheckBox femaleCheckbox = genderDialog.findViewById(R.id.female_checkbox_edit);
        CheckBox maleCheckbox = genderDialog.findViewById(R.id.male_checkbox_edit);
        AppCompatButton cancelButton = genderDialog.findViewById(R.id.cancel_button_name_edit);
        AppCompatButton saveButton = genderDialog.findViewById(R.id.save_button_name_edit);

        maleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (maleCheckbox.isChecked()) {
                    femaleCheckbox.setChecked(false);
                    maleImage.setImageDrawable(getResources().getDrawable(R.drawable.male_checkbox_icon_color));
                    femaleImage.setImageDrawable(getResources().getDrawable(R.drawable.female_checkbox_icon_black));
                } else {
                    maleImage.setImageDrawable(getResources().getDrawable(R.drawable.male_checkbox_icon_black));
                }
            }
        });

        femaleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (femaleCheckbox.isChecked()) {
                    maleCheckbox.setChecked(false);
                    maleImage.setImageDrawable(getResources().getDrawable(R.drawable.male_checkbox_icon_black));
                    femaleImage.setImageDrawable(getResources().getDrawable(R.drawable.female_checkbox_icon_color));
                } else {
                    femaleImage.setImageDrawable(getResources().getDrawable(R.drawable.female_checkbox_icon_black));
                }
            }
        });

        maleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleCheckbox.setChecked(!maleCheckbox.isChecked());
            }
        });

        femaleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                femaleCheckbox.setChecked(!femaleCheckbox.isChecked());
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
        String theTempEmail = sharedPreferences.getString("Email", "");

        firestore = FirebaseFirestore.getInstance();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(!maleCheckbox.isChecked() && !femaleCheckbox.isChecked())) {
                    docRef = firestore.collection("users").document(theTempEmail);
                    String tempGender = getGender(maleCheckbox, femaleCheckbox);

                    docRef.update("gender", tempGender);

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("fragmentPosition", fragmentPos);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(MainActivity.this, "Please choose a gender", Toast.LENGTH_SHORT).show();
                }

            }
        });
        genderDialog.show();
    }

    public void openEditBirthdayDialog(int gravity, int fragmentPos) {
        final Dialog birthDayDialog = new Dialog(this);
        birthDayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        birthDayDialog.setContentView(R.layout.layout_dialog_date_of_birth);

        Window window = birthDayDialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if (Gravity.CENTER == gravity) {
            birthDayDialog.setCancelable(true);
        } else {
            birthDayDialog.setCancelable(false);
        }

        TextView datePicker = birthDayDialog.findViewById(R.id.date_picker_edit);
        AppCompatButton cancelButton = birthDayDialog.findViewById(R.id.cancel_button_name_edit);
        AppCompatButton saveButton = birthDayDialog.findViewById(R.id.save_button_name_edit);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d("AddToDoItemActivity", "onDateSet: date" + dayOfMonth + "/" + month + "/" + year);
                mDate = dayOfMonth + "/" + month + "/" + year;

                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int age = currentYear - year;
                if (age >= 13) {
                    datePicker.setText(mDate);
                } else {
                    Toast.makeText(MainActivity.this,
                            "You need to be older than 12 years old to use this application!"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        };

        SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
        String theTempEmail = sharedPreferences.getString("Email", "");

        firestore = FirebaseFirestore.getInstance();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                birthDayDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!datePicker.getText().toString().equals("Select your date of birth")) {
                    docRef = firestore.collection("users").document(theTempEmail);
                    docRef.update("date_of_birth", mDate);

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("fragmentPosition", fragmentPos);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(MainActivity.this, "Please choose your date of birth", Toast.LENGTH_SHORT).show();
                }
            }
        });
        birthDayDialog.show();
    }
    private String getGender(CheckBox maleCheckbox, CheckBox femaleCheckbox) {
        if (maleCheckbox.isChecked()) {
            return "Male";
        } else {
            return "Female";
        }
    }

    public void fillForm(int gravity, int fragmentPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_edit);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if (Gravity.CENTER == gravity) {
            dialog.setCancelable(true);
        } else {
            dialog.setCancelable(false);
        }

        AppCompatButton btncancel = dialog.findViewById(R.id.cancel_dialog);
        EditText weightEditText = dialog.findViewById(R.id.edit_weight);
        EditText heightEditText = dialog.findViewById(R.id.edit_height);
        Button saveButton = dialog.findViewById(R.id.save_btn_dialog);

        Runnable setUpBMIRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                SetUpBMIDialogFirebase(weightEditText, heightEditText);
            }
        };

        Thread backgroundBMIThread = new Thread(setUpBMIRunnable);
        backgroundBMIThread.start();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String checkWeight = weightEditText.getText().toString();
                String checkHeight = heightEditText.getText().toString();
                if (checkHeight.trim().equals("") || checkWeight.trim().equals("")) {
                    Toast.makeText(MainActivity.this, "Please fill out the form!", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(checkHeight) <= 0) {
                    Toast.makeText(MainActivity.this, "Please fill in a normal Weight", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(checkWeight) <= 0) {
                    Toast.makeText(MainActivity.this, "Please fill in a normal Height", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
                    String theTempEmail = sharedPreferences.getString("Email", "");

                    firestore = FirebaseFirestore.getInstance();

                    LocalDate today = LocalDate.now();
                    LocalDate monday = today.with(previousOrSame(MONDAY));

                    docRef = firestore.collection("daily").
                            document("week-of-" + monday.toString()).
                            collection(today.toString()).
                            document(theTempEmail);
                    docRef.update("weight", String.valueOf(weightEditText.getText()));
                    docRef.update("height", String.valueOf(heightEditText.getText()));

                    docRef = firestore.collection("users").document(theTempEmail);
                    docRef.update("weight", String.valueOf(weightEditText.getText()));
                    docRef.update("height", String.valueOf(heightEditText.getText()));

                    if (fragmentPos != 5) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putExtra("fragmentPosition", fragmentPos);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                    }

                }
            }
        });

        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveButton.setText("Update");
            }
        });

        heightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveButton.setText("Update");
            }
        });

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SetUpBMIDialogFirebase(EditText weightEditText, EditText heightEditText) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(previousOrSame(MONDAY));

        SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
        String theTempEmail = sharedPreferences.getString("Email", "");

        firestore = FirebaseFirestore.getInstance();

        docRef = firestore.collection("users").document(theTempEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        String tempWeight = document.getString("weight");
                        String tempHeight = document.getString("height");

                        assert tempWeight != null;
                        if (!tempWeight.equals("empty")) {
                            weightEditText.setText(tempWeight);
                        }
                        assert tempHeight != null;
                        if (!tempHeight.equals("empty")) {
                            heightEditText.setText(tempHeight);
                        }

                        if (tempHeight.equals("empty") && tempWeight.equals("empty")) {
                            docRef = firestore.collection("daily").
                                    document("week-of-" + monday.toString()).
                                    collection(today.toString()).
                                    document(theTempEmail);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document != null) {
                                            String weight = document.getString("weight");
                                            String height = document.getString("height");
                                            if (!"empty".equals(weight)) {
                                                weightEditText.setText(weight);
                                            }
                                            if (!"empty".equals(height)) {
                                                heightEditText.setText(height);
                                            }

                                        } else {
                                            Log.d("LOGGER", "No such document");
                                        }
                                    } else {
                                        Log.d("LOGGER", "get failed with ", task.getException());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });


    }

    private void wateredit(int gravity, int fragmentPos) {
        final Dialog drinkWaterDialog = new Dialog(this);
        drinkWaterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        drinkWaterDialog.setContentView(R.layout.layout_water_edit);
        Window window = drinkWaterDialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if (Gravity.CENTER == gravity) {
            drinkWaterDialog.setCancelable(true);
        } else {
            drinkWaterDialog.setCancelable(false);
        }
        AppCompatButton drinkWaterCancelBtn = drinkWaterDialog.findViewById(R.id.cancel_dialog2);
        AppCompatButton drinkWaterBtn = drinkWaterDialog.findViewById(R.id.drink_btn_dialog);
        TextView cupOfWater = drinkWaterDialog.findViewById(R.id.cup_of_water);
        ImageView waterCupIcon = drinkWaterDialog.findViewById(R.id.water_cup_icon);

        Runnable setUpWaterDialogRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                SetUpWaterDialogFirebase(cupOfWater, waterCupIcon);
            }
        };

        Thread backgroundThread = new Thread(setUpWaterDialogRunnable);
        backgroundThread.start();

        drinkWaterBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String cupOfWaterLabel = String.valueOf(cupOfWater.getText());
                if (!cupOfWaterLabel.equals("Set a Goal First")) {
                    int updateDrinkValue = Integer.parseInt(cupOfWaterLabel) + 1;
                    numberOfCupHadDrink = String.valueOf(updateDrinkValue);
                    String updateDrinkValueToFirebase = String.valueOf(updateDrinkValue * 250);

                    LocalDate today = LocalDate.now();
                    LocalDate monday = today.with(previousOrSame(MONDAY));

                    SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
                    String theTempEmail = sharedPreferences.getString("Email", "");

                    firestore = FirebaseFirestore.getInstance();

                    firestore.collection("daily").
                            document("week-of-" + monday.toString()).
                            collection(today.toString()).
                            document(theTempEmail).
                            update("drink", String.valueOf(updateDrinkValueToFirebase));
                    cupOfWater.setText(numberOfCupHadDrink);
                    startActivity(getIntent());
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });

        drinkWaterCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drinkWaterDialog.dismiss();
            }
        });
        drinkWaterDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SetUpWaterDialogFirebase(TextView cupOfWater, ImageView waterCupIcon) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(previousOrSame(MONDAY));

        SharedPreferences sharedPreferences = getSharedPreferences(tempEmail, MODE_PRIVATE);
        String theTempEmail = sharedPreferences.getString("Email", "");

        firestore = FirebaseFirestore.getInstance();

        docRef = firestore.collection("users").document(theTempEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        String temp = document.getString("drink_goal");
                        assert temp != null;
                        if (temp.equals("empty")) {
                            cupOfWater.setText("Set a Goal First");
                            waterCupIcon.setVisibility(View.INVISIBLE);
                        } else {
                            docRef = firestore.collection("daily").
                                    document("week-of-" + monday.toString()).
                                    collection(today.toString()).
                                    document(theTempEmail);

                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document != null) {
                                            String temp = document.getString("drink");
                                            if (!"empty".equals(temp)) {
                                                float waterHadDrink = Float.parseFloat(temp) / 250;
                                                cupOfWater.setText(String.valueOf((int) waterHadDrink));
                                                waterCupIcon.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            Log.d("LOGGER", "No such document");
                                        }
                                    } else {
                                        Log.d("LOGGER", "get failed with ", task.getException());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

}