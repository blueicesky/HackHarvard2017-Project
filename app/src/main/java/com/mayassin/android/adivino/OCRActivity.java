package com.mayassin.android.adivino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.OCR;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OCRActivity extends AppCompatActivity implements  OCRInterface {

    private static final int RC_HANDLE_GMS = 5;
    private CameraSource mCameraSource;
    private Button goBack;
    private CameraSourcePreview mPreview;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private final Handler mHideHandler = new Handler();

    private VisionServiceClient client;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };




    String[] knownPlayers = new String[]{"Messi", "Messy", "Ronaldo" , "Ronald", "Otters", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ocr);
        initializeFullScreenSettings();

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        goBack = (Button) findViewById(R.id.go_back_button);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        knownPlayers = this.getResources().getStringArray(R.array.player_names);

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (client==null){
            client = new VisionServiceRestClient("aa0981cea359466f84c29a11c2612d0f");
        }


    }
    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

    }



    private void createCameraSource() {
        Context context = getApplicationContext();


        // TODO: Create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(this));
        // TODO: Set the TextRecognizer's Processor.


        if (!textRecognizer.isOperational()) {
            Log.w("TEXT RECOGNIZER", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low Storage!", Toast.LENGTH_LONG).show();
                Log.w("TEXTRECOGNIER", "Low Storage");
            }
        }

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setAutoFocusEnabled(true)
                        .setRequestedFps(60.0f)
                        .build();
        startCameraSource();
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e("ERROR:MOE", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }





    // Full Screen Stuff:

    private void initializeFullScreenSettings()
    {


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.go_back_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraSource = null;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    MaterialDialog correctPlayerDialog;
    MaterialDialog progress;
    boolean dialogVisible;
    @Override
    public void MatchFound(final String text) {
        String MicrosoftOCRResult = analyseScreenWithMicrosoft();
        final String analyzedText = textIsPlayer(text.toLowerCase());
        if(analyzedText != "" && !dialogVisible ) {





            this.runOnUiThread(new Runnable() {
                public void run() {
                    dialogVisible = true;
                    correctPlayerDialog = new MaterialDialog.Builder(OCRActivity.this)
                            .title("Player Detected")
                            .content("Is the player " + analyzedText + "?")
                            .positiveText("Yes")
                            .negativeText("No")
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dialogVisible = false;
                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    progress = new MaterialDialog.Builder(dialog.getContext())
                                            .title("ANALYZING NEURAL NETWORK...")
                                            .content("Loading " + toTitleCase(analyzedText))
                                            .progress(true, 0)
                                            .show();
                                    openPlayerStats(analyzedText);
                                }
                            })
                            .build();

                    correctPlayerDialog.show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCameraSource();
    }

    private void openPlayerStats(final String analyzedText) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.e("OPENING: ", "OPENING: "+analyzedText);
                    Server server = new Server();
                    String jsonData = server.post("http://40.114.51.138:9612/get_score", "{\"detected_text\" : \""+analyzedText+"\"}");
                    if(progress != null) {
                        progress.dismiss();
                    }
                    Intent intent = new Intent(getBaseContext(), PlayerStatsActivity.class);
                    intent.putExtra("json_data", jsonData);
                    intent.putExtra("player_id", analyzedText);
                    startActivity(intent);

                } catch (final Exception e) {
                        Log.e("ERRORR SERVER", e.getMessage());
                }
            }
        }).start();
    }

    private String textIsPlayer(String name) {
        if (name.length() > 15) {
            return "";
        }

        for(String player : knownPlayers){
            String firstHalfOfName = name.substring(0, name.length()/2);
            String secondHalfOfName = name.substring(name.length()/2, name.length());

            if(player.contains(name)){
                String test = "huh";
                return player;
            }
        }

        return "";
    }

    private String analyseScreenWithMicrosoft() {

        String result = "";
        try{
        Gson gson = new Gson();

        Bitmap currentScreen = screenShot(mPreview);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        currentScreen.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
            ocr = client.recognizeText(inputStream, LanguageCodes.English, true);
            result = gson.toJson(ocr);
        } catch (Exception e) {
            //Log.d("ERROR MICROSOFT:", e.getMessage());
        }
        return result;

    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

}
