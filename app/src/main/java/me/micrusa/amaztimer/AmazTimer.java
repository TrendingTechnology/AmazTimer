package me.micrusa.amaztimer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import me.micrusa.amaztimer.activities.SettingsActivity;
import me.micrusa.amaztimer.utils.file;
import me.micrusa.amaztimer.utils.hrSensor;
import me.micrusa.amaztimer.utils.utils;
import me.micrusa.app.amazwidgets.R;

public class AmazTimer extends Activity {

    private static final String TAG = "me.micrusa.amaztimer.AmazTimer";
    // Activity variables
    private Context mContext;
    private Activity activity;
    //These get set up later
    private View mView;
    private boolean mHasActive = false;
    private int v;
    //Define items
    private Button plus, plus2, plus3, minus, minus2, minus3, start, cancel;
    private TextView sets, rest, work, time, hr, rSets, status, settingstext, setsText, workText, restText;
    private ConstraintLayout L1, L2;
    //Define timers and timer booleans
    private CountDownTimer workTimer;
    private CountDownTimer restTimer;
    private boolean workStarted = false;
    private boolean restStarted = false;
    //Classes
    private me.micrusa.amaztimer.utils.utils utils = new utils();
    //Default values
    private me.micrusa.amaztimer.defValues defValues = new defValues();
    //Settings
    private boolean batterySaving;
    private boolean hrEnabled;


    //Much like a fragment, getView returns the content view of the page. You can set up your layout here
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Save Activity variables
        this.mContext = this;
        activity = this;
        setContentView(R.layout.amaztimer);
        this.mView = this.findViewById(android.R.id.content);
        final file file = new file(defValues.timerFile, this.mView.getContext());
        //Setup items
        this.init();
        //Set language to setting's language
        utils.setLang(this.mView.getContext(), new file(defValues.settingsFile, this.mView.getContext()).get(defValues.sLang, defValues.LangDefault));
        //Set texts
        this.setTexts();
        //Check if the view is already inflated (reloading)
        if ((!this.mHasActive) && (this.mView != null)) {
            //It is, simply refresh
            refreshView();
        }
        //Setup hrSensor class
        final hrSensor hrSensor = new hrSensor(this.mView.getContext(), hr);
        //Text default values
        sets.setText(String.valueOf(file.get(defValues.sSets, defValues.defSets)));
        work.setText(utils.sToMinS(file.get(defValues.sWork, defValues.defWorkTime)));
        rest.setText(utils.sToMinS(file.get(defValues.sRest, defValues.defRestTime)));
        //Plus and minus buttons
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sSets, defValues.defSets) + 1;
                if (v > defValues.maxSets) {
                    v = defValues.maxSets;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sSets, v);
                sets.setText(String.valueOf(v));
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sSets, defValues.defSets) - 1;
                if (v < defValues.minSets) {
                    v = defValues.minSets;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sSets, v);
                sets.setText(String.valueOf(v));
            }
        });
        //Work
        plus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sWork, defValues.defWorkTime) + 1;
                if (v > defValues.maxTime) {
                    v = defValues.maxTime;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sWork, v);
                work.setText(utils.sToMinS(v));
            }
        });
        minus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sWork, defValues.defWorkTime) - 1;
                if (v < defValues.minTime) {
                    v = defValues.minTime;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sWork, v);
                work.setText(utils.sToMinS(v));
            }
        });
        //Rest
        plus3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sRest, defValues.defRestTime) + 1;
                if (v > defValues.maxTime) {
                    v = defValues.maxTime;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sRest, v);
                rest.setText(utils.sToMinS(v));
            }
        });
        minus3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = file.get(defValues.sRest, defValues.defRestTime) - 1;
                if (v < defValues.minTime) {
                    v = defValues.minTime;
                    utils.vibrate(defValues.sVibration, view.getContext());
                }
                file.set(defValues.sRest, v);
                rest.setText(utils.sToMinS(v));
            }
        });

        //Start button
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //Set language to setting's language
                utils.setLang(view.getContext(), new file(defValues.settingsFile, view.getContext()).get(defValues.sLang, defValues.LangDefault));
                //Move to second layout with timer's stuff and set all texts
                L1.setVisibility(View.GONE);
                L2.setVisibility(View.VISIBLE);
                L2.setBackgroundColor(view.getResources().getColor(R.color.yellow));
                rSets.setText(String.valueOf(file.get(defValues.sSets, defValues.defSets)));
                status.setText(view.getResources().getString(R.string.prepare));
                //Get battery saving settings
                getSettings();
                //hrSensor stuff
                hrState(true, hrSensor, hr);
                final CountDownTimer PrepareTimer = new CountDownTimer(5 * 1000, 1000) {
                    @Override
                    public void onTick(long l) {
                        timerUpdate((int) l / 1000);
                    }

                    @Override
                    public void onFinish() {
                        startTimer(view, view.getResources().getString(R.string.work), view.getResources().getString(R.string.rest), file.get(defValues.sWork, defValues.defWorkTime), file.get(defValues.sRest, defValues.defRestTime), hrSensor);
                    }
                };
                PrepareTimer.start();

            }
        });
        //Start long press opens settings
        start.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
                return true;
            }
        });
        //Cancel button
        //To avoid accidental clicks, just a long click will cancel it
        cancel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //Display start layout
                L1.setVisibility(View.VISIBLE);
                L2.setVisibility(View.GONE);
                //Stop timers
                stopTimers();
                //Unregister hr sensor listener to avoid battery drain
                hrState(false, hrSensor, hr);
                return true;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send toast
                Toast.makeText(view.getContext(), view.getResources().getString(R.string.canceltoast), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSettings() {
        file file = new file(defValues.settingsFile, this.mView.getContext());
        this.batterySaving = file.get(defValues.sBatterySaving, defValues.BatterySavingDefault);
        this.hrEnabled = file.get(defValues.sHrSwitch, defValues.HrSwitchDefault);
    }

    private void hrState(boolean state, hrSensor hrSensor, TextView hr) {
        if (state) {
            if (this.hrEnabled) {
                hrSensor.registerListener();
                if (hr.getVisibility() == View.INVISIBLE) {
                    hr.setVisibility(View.VISIBLE);
                }
            } else if (hr.getVisibility() == View.VISIBLE) {
                hr.setVisibility(View.INVISIBLE);
            }
        } else if (this.hrEnabled) {
            hrSensor.unregisterListener();
        }
    }

    private void init() {
        //Buttons
        plus = this.mView.findViewById(R.id.plus);
        plus2 = this.mView.findViewById(R.id.plus2);
        plus3 = this.mView.findViewById(R.id.plus3);
        minus = this.mView.findViewById(R.id.minus2);
        minus2 = this.mView.findViewById(R.id.minus);
        minus3 = this.mView.findViewById(R.id.minus3);
        start = this.mView.findViewById(R.id.start);
        cancel = this.mView.findViewById(R.id.cancel);
        //TextViews
        sets = this.mView.findViewById(R.id.sets);
        rest = this.mView.findViewById(R.id.rest);
        work = this.mView.findViewById(R.id.work);
        time = this.mView.findViewById(R.id.time);
        hr = this.mView.findViewById(R.id.heartbeat);
        rSets = this.mView.findViewById(R.id.remSets);
        status = this.mView.findViewById(R.id.status);
        settingstext = this.mView.findViewById(R.id.textView);
        setsText = this.mView.findViewById(R.id.textView4);
        workText = this.mView.findViewById(R.id.textView5);
        restText = this.mView.findViewById(R.id.textView6);
        //Layouts
        L1 = this.mView.findViewById(R.id.startScreen);
        L2 = this.mView.findViewById(R.id.timerScreen);
    }

    private void setTexts() {
        Resources res = this.mView.getContext().getResources();
        this.init();
        start.setText(res.getString(R.string.start));
        cancel.setText(res.getString(R.string.cancel));
        setsText.setText(res.getString(R.string.sets));
        workText.setText(res.getString(R.string.work));
        restText.setText(res.getString(R.string.rest));
        settingstext.setText(res.getString(R.string.startsettings));
    }

    private void timerUpdate(int v) {
        this.init();
        if (!this.batterySaving) {
            time.setText(utils.sToMinS(v));
        } else if (!time.getText().toString().equals("--:--")) {
            time.setText("--:--");
        }
        if (v < 4) {
            if (v == 1) {
                utils.vibrate(defValues.lVibration, this.mView.getContext());
            }
            if (v != 1) {
                utils.vibrate(defValues.sVibration, this.mView.getContext());
            }
        }
    }

    private void stopTimers() {
        if (this.workStarted) {
            this.workTimer.cancel();
        }
        if (this.restStarted) {
            this.restTimer.cancel();
        }
    }

    private void startTimer(final View view, final String sWork, final String sRest, final int work, final int rest, final hrSensor hrSensor) {
        this.init();
        this.workStarted = true;
        this.restStarted = false;
        if (!this.mHasActive) {
            this.workStarted = false;
            return;
        }
        status.setText(sWork);
        L2.setBackgroundColor(view.getResources().getColor(R.color.red));
        this.workTimer = new CountDownTimer(work * 1000, 1000) {
            @Override
            public void onTick(long l) {
                timerUpdate((int) l / 1000);
            }

            @Override
            public void onFinish() {
                restTimer(view, sWork, sRest, work, rest, hrSensor);
            }
        };
        this.workTimer.start();
    }

    private void restTimer(final View view, final String sWork, final String sRest, final int work, final int rest, final hrSensor hrSensor) {
        this.init();
        this.workStarted = false;
        this.restStarted = true;
        if (!this.mHasActive) {
            this.restStarted = false;
            return;
        }
        status.setText(sRest);
        L2.setBackgroundColor(view.getResources().getColor(R.color.green));
        this.restTimer = new CountDownTimer(rest * 1000, 1000) {
            @Override
            public void onTick(long l) {
                timerUpdate((int) l / 1000);
            }

            @Override
            public void onFinish() {
                if (Integer.parseInt(rSets.getText().toString()) != 1) {
                    rSets.setText(String.valueOf(Integer.parseInt(rSets.getText().toString()) - 1));
                    startTimer(view, sWork, sRest, work, rest, hrSensor);
                } else {
                    //Unregister hrSensor listener and make visible initial screen again
                    hrState(false, hrSensor, hr);
                    L1.setVisibility(View.VISIBLE);
                    L2.setVisibility(View.GONE);
                }
            }
        };
        this.restTimer.start();
    }

    private void refreshView() {
        //Set language to setting's language
        utils.setLang(this.mView.getContext(), new file(defValues.settingsFile, this.mView.getContext()).get(defValues.sLang, defValues.LangDefault));
        //Set texts
        this.setTexts();
    }


    //Called when the page is destroyed completely (in app mode). Same as the onDestroy method of an activity
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //Called when the page is shown again (in app mode)
    @Override
    public void onResume() {
        super.onResume();
        //Set language to setting's language
        utils.setLang(this.mView.getContext(), new file(defValues.settingsFile, this.mView.getContext()).get(defValues.sLang, defValues.LangDefault));
        //Set texts
        this.setTexts();
        //Check if view already loaded
        if ((!this.mHasActive) && (this.mView != null)) {
            //It is, simply refresh
            this.mHasActive = true;
            refreshView();
        }
        //Store active state
        this.mHasActive = true;
    }

    //Called when the page is stopped (in app mode)
    @Override
    public void onStop() {
        super.onStop();
        this.mHasActive = false;
    }
}