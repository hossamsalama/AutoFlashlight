package com.etoutstore.flashnav;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import static android.content.Context.MODE_PRIVATE;

public class TorchFragment extends Fragment {
    private ToggleButton sound_button;
    private ToggleButton powerButton;
    private SeekBar seekBar;
    private CameraType camera;

    TorchFragment(CameraType camera) {
        this.camera = camera;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Please note the third parameter should be false, otherwise a java.lang.IllegalStateException maybe thrown.
        View rootView = inflater.inflate(R.layout.torch_layout, container, false);
        sound_button =  rootView.findViewById(R.id.sound);
        powerButton = rootView.findViewById(R.id.toggleButton);
        powerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (sound_button.isChecked()) {
                    playSound();
                }
                camera.switchFlashLight(isChecked);

            }
        });
        final TextView myText = rootView.findViewById(R.id.sos_textview);

        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(400); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        seekBar = rootView.findViewById(R.id.seekBar);
        seekBar.setMax(9);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress > 0){
                    myText.startAnimation(anim);
                }
                else {
                    anim.cancel();
                }
                camera.setFreq(progress);
                camera.switchFlashLight(powerButton.isChecked());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sound_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sound_button.isChecked()){
                    playSound();
                }
            }
        });

        return rootView;
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        Boolean state = sharedPreferences.getBoolean("state", false);
        powerButton.setChecked(state);
        int prog = sharedPreferences.getInt("progress", 0);
        seekBar.setProgress(prog);
        Boolean soundState = sharedPreferences.getBoolean("sound_state", false);
        sound_button.setChecked(soundState);
    }

    private void savePreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("state", powerButton.isChecked());
        editor.putInt("progress", seekBar.getProgress());
        editor.putBoolean("sound_state", sound_button.isChecked());
        editor.commit();
    }

    /**
     *Playing sound will play button toggle sound on flash on / off
     */
    private void playSound() {
        MediaPlayer mp;
        if (sound_button.isChecked()) {
            mp = MediaPlayer.create(getActivity(), R.raw.light_switch_off);
        } else {
            mp = MediaPlayer.create(getActivity(), R.raw.light_switch_on);
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }
}
