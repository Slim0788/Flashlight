package com.slim.flashlight;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FlashActivity extends AppCompatActivity {

    private int soundId;
    private SoundPool soundPool;

    private ToggleButton flashToggle;
    private ToggleButton blinkToggle;
    private ImageView flashlightImage;
    private CameraManager cameraManager;

    private boolean isBlinked = false;
    private boolean isCameraAvailable;

    private Thread blinkThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flashToggle = findViewById(R.id.flash_toggle);
        blinkToggle = findViewById(R.id.blink_toggle);
        flashlightImage = findViewById(R.id.flashlight_image);

        soundPool = createSoundPoolWithBuilder();
        soundId = soundPool.load(this, R.raw.click, 1);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        isCameraAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        flashToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playSound();
            if (isCameraAvailable) {
                toggleImage();
                toggleFlashLight(isChecked);
            } else {
                buttonView.setChecked(false);
                showCameraAlert();
            }
        });

        blinkToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playSound();
            if (isCameraAvailable) {
                toggleImage();
                if (isChecked) {
                    blinkThread = new Thread(createBlink());
                    blinkThread.start();
                } else {
                    isBlinked = false;
                }
            } else {
                buttonView.setChecked(false);
                showCameraAlert();
            }
        });
    }

    private void playSound() {
        soundPool.play(soundId, 1, 1, 0, 0, 1);
    }

    private void showCameraAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_text)
                .setPositiveButton(R.string.exit_message, (dialog, which) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected SoundPool createSoundPoolWithBuilder() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        return new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(1)
                .build();
    }

    private void toggleFlashLight(boolean isFlashEnabled) {
        try {
            cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], isFlashEnabled);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void toggleImage() {
        if (flashToggle.isChecked() || blinkToggle.isChecked()) {
            flashlightImage.setImageResource(R.drawable.img_flashlight_on);
        } else {
            flashlightImage.setImageResource(R.drawable.img_flashlight_off);
        }
    }

    private Runnable createBlink() {
        isBlinked = true;
        return () -> {
            boolean flash = true;
            while (isBlinked) {
                toggleFlashLight(flash);
                try {
                    Thread.sleep(500L);
                    flash = !flash;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            toggleFlashLight(false);
        };
    }
}