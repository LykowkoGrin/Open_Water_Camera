package net.sourceforge.opencamera;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewManager;
import android.widget.ImageButton;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import java.util.ArrayList;
import java.util.Set;


public class UnderwaterInterface {
    private MainActivity mainActivity;
    private Set<FuncButton> funcButtons = new LinkedHashSet<FuncButton>();

    private boolean inMode = false;
    private boolean inEditMode = false;

    private LinearLayout itemContainer;
    private View waterPopup;

    private Set<VideoListener> videoListeners = new LinkedHashSet<VideoListener>();
    private Set<PauseListener> pauseListeners = new LinkedHashSet<PauseListener>();
    private AlertDialog waterDialog = null;


    MovableButton.OnClickListener editClickListener = ((MovableButton v) -> {
        waterDialog = WaterDialogHelper.showMinimalDialog(mainActivity, new WaterDialogHelper.DialogListener() {
            @Override
            public void onApplyClicked() {

            }

            @Override
            public void onDeleteClicked() {
                ((ViewManager)v.getButton().getParent()).removeView(v.getButton());
                funcButtons.remove(v.getButton());
            }
        });
        mainActivity.getMainUI().layoutUI();

    });
    MovableButton.OnClickListener editUniClickListener = ((MovableButton v) -> {
        waterDialog = WaterDialogHelper.showDialog(mainActivity, new WaterDialogHelper.DialogListener() {
            @Override
            public void onApplyClicked() {
                Spinner clickSpinner = waterDialog.findViewById(R.id.spinner1);
                Spinner holdSpinner = waterDialog.findViewById(R.id.spinner2);
                Spinner pressSpinner = waterDialog.findViewById(R.id.spinner3);
                Spinner releaseSpinner = waterDialog.findViewById(R.id.spinner4);

                //clickSpinner.getSelectedItemId();
            }

            @Override
            public void onDeleteClicked() {
                ((ViewManager)v.getButton().getParent()).removeView(v.getButton());
                funcButtons.remove(v.getButton());
            }
        });
        mainActivity.getMainUI().layoutUI();

    });

    UnderwaterInterface(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        itemContainer = mainActivity.findViewById(R.id.water_item_container);
        waterPopup = mainActivity.findViewById(R.id.water_popup_container);
        addMenuItems();
        loadElements();

        mainActivity.findViewById(R.id.water_edit_mode).setOnClickListener(v -> startEditMode());
        mainActivity.findViewById(R.id.done_edit_mode).setOnClickListener(v -> stopEditMode());
        mainActivity.findViewById(R.id.add_button).setOnClickListener(v -> {
            if(!waterPopupIsOpen()) openWaterPopup();
            else closeWaterPopup();
        });

    }

    private void loadElements(){
        Map<String, String> elementsMap = new HashMap<>();
        File file = new File(mainActivity.getFilesDir(), FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                elementsMap = gson.fromJson(reader, type);
                if (elementsMap == null) {
                    elementsMap = new HashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            elementsMap = new HashMap<>();
        }


    }

    private void saveElements(){
        Map<String, String> elementsMap = new HashMap<>();

        File file = new File(mainActivity.getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new Gson();
            String json = gson.toJson(elementsMap);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean waterPopupIsOpen(){
        return waterPopup.getVisibility() == View.VISIBLE;
    }

    public View getWaterPopup(){
        return mainActivity.findViewById(R.id.water_popup_container);
    }

    public void openWaterPopup(){
        waterPopup.setVisibility(View.VISIBLE);
        mainActivity.getMainUI().layoutUI(true);
    }

    public void closeWaterPopup(){
        waterPopup.setVisibility(View.GONE);
    }


    public void startWaterMode(){
        inMode = true;

        for(FuncButton button : funcButtons){
            button.getButton().setVisibility(View.VISIBLE);
        }


        mainActivity.getMainUI().setImmersiveMode(true);
        mainActivity.findViewById(R.id.take_photo).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.water_edit_mode).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.pause_video).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.take_photo_when_video_recording).setVisibility(View.GONE);

        mainActivity.getMainUI().layoutUI();
    }

    public void stopWaterMode(){
        inMode = false;

        for(FuncButton button : funcButtons){
            button.getButton().setVisibility(View.GONE);
        }

        mainActivity.findViewById(R.id.take_photo).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.water_edit_mode).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.water_popup_container).setVisibility(View.GONE);
        mainActivity.getMainUI().setImmersiveMode(false);

        if(mainActivity.getPreview().isVideoRecording()){
            mainActivity.findViewById(R.id.pause_video).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.take_photo_when_video_recording).setVisibility(View.VISIBLE);
        }

        mainActivity.getMainUI().layoutUI();

    }

    public boolean getModeState(){
        return inMode;
    }

    public AlertDialog getWaterDialog(){
        return waterDialog;
    }


    private void startEditMode(){
        inEditMode = true;

        mainActivity.findViewById(R.id.done_edit_mode).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.add_button).setVisibility(View.VISIBLE);;
        mainActivity.findViewById(R.id.water_mode).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.water_edit_mode).setVisibility(View.GONE);

        mainActivity.getMainUI().layoutUI();



        for(FuncButton button : funcButtons){
            setupButtonEditMode(button);
        }


    }

    private void stopEditMode(){
        inEditMode = false;

        mainActivity.findViewById(R.id.add_button).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.done_edit_mode).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.water_mode).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.water_edit_mode).setVisibility(View.VISIBLE);

        mainActivity.getMainUI().layoutUI();

        for(FuncButton button : funcButtons){
            button.setupListeners();
        }

        closeWaterPopup();
    }


    public void startingVideo(){
        for(VideoListener listener : videoListeners) listener.run(true);
    }

    public void stoppingVideo(){
        for(VideoListener listener : videoListeners) listener.run(false);
    }

    public void pausingVideo(){
        for(PauseListener listener : pauseListeners) listener.run(true);
    }

    public void resumingVideo(){
        for(PauseListener listener : pauseListeners) listener.run(false);
    }

    public ArrayList<View> getViewElements() {

        ArrayList<View> imageButtons = new ArrayList<>(); // Копируем первый список
        for(FuncButton button : funcButtons){
            imageButtons.add(button.getButton());
        }

        return imageButtons;
    }

    private void setupButtonEditMode(FuncButton btn){

        MovableButton movBtn = new MovableButton(btn.getButton());

        if(btn.getButtonName().equals("universal_button_option"))
            movBtn.setOnClickListener(editUniClickListener);
        else
            movBtn.setOnClickListener(editClickListener);

        movBtn.setupListeners();

    }

    private boolean createButtonByName(String name){
        FuncButton newButton = null;
        switch (name){
            case "take_photo_option":
                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams takePhotoParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 100),
                        dpToPx(mainActivity, 100)
                );
                newButton.getButton().setLayoutParams(takePhotoParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.take_photo_selector);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> {
                    if(mainActivity.getPreview().isVideo())
                        mainActivity.clickedSwitchVideo(v);
                    else mainActivity.takePicture(false);
                });

                break;

            case "take_video_option":
                final ImageButton button = new ImageButton(mainActivity);
                newButton = new FuncButton(button);
                RelativeLayout.LayoutParams takeVideoParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 100),
                        dpToPx(mainActivity, 100)
                );
                newButton.getButton().setLayoutParams(takeVideoParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.take_video_selector);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> {
                    if(!mainActivity.getPreview().isVideo())
                        mainActivity.clickedSwitchVideo(v);
                    else
                        mainActivity.takePicture(false);
                });

                videoListeners.add(isVideoCapturing -> {
                    if(isVideoCapturing){
                        button.setImageResource(R.drawable.take_video_recording);
                        button.setContentDescription( mainActivity.getResources().getString(R.string.stop_video) );
                        button.setTag(R.drawable.take_video_recording); // for testing*/
                    }
                    else{
                        button.setImageResource(R.drawable.take_video_selector);
                        button.setContentDescription( mainActivity.getResources().getString(R.string.start_video) );
                        button.setTag(R.drawable.take_video_selector); // for testing
                    }

                });
                break;

            case "take_photo_when_video_recording_option":

                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams takeWhenPhotoParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 70),
                        dpToPx(mainActivity, 70)
                );
                newButton.getButton().setLayoutParams(takeWhenPhotoParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.take_photo_when_video_recording);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> mainActivity.takePicture(true));
                break;

            case "pause_video_option":

                final ImageButton buttonPause = new ImageButton(mainActivity);

                newButton = new FuncButton(buttonPause);
                RelativeLayout.LayoutParams pauseVideoParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 70),
                        dpToPx(mainActivity, 70)
                );
                newButton.getButton().setLayoutParams(pauseVideoParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                newButton.getButton().setBackgroundDrawable(null);

                newButton.setOnClickListener((View v) -> mainActivity.clickedPauseVideo(v));

                pauseListeners.add(isPause -> {
                    if(isPause)
                        buttonPause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                    else
                        buttonPause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                });

                break;

            case "switch_camera_option":

                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams switchCameraParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 60),
                        dpToPx(mainActivity, 60)
                );
                newButton.getButton().setLayoutParams(switchCameraParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 5),
                        dpToPx(mainActivity, 5),
                        dpToPx(mainActivity, 5),
                        dpToPx(mainActivity, 5));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.switch_camera);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> mainActivity.clickedSwitchCamera(v));
                break;

            case "universal_button_option":

                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams uniButtonParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 60),
                        dpToPx(mainActivity, 60)
                );
                newButton.getButton().setLayoutParams(uniButtonParams);

                newButton.getButton().setImageResource(R.drawable.white_background);
                newButton.getButton().setPadding(0, 0, 0, 0);

                break;
        }


        if(newButton == null) return false;

        newButton.setButtonName(name);

        RelativeLayout relativeLayout = mainActivity.findViewById(R.id.relative_layout);
        relativeLayout.addView(newButton.getButton());

        if(inEditMode) setupButtonEditMode(newButton);
        else newButton.setupListeners();

        funcButtons.add(newButton);

        return true;

    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private void addMenuItems() {

        int[] options = {
                R.string.take_photo_option,
                R.string.take_video_option,
                R.string.take_photo_when_video_recording_option,
                R.string.pause_video_option,
                R.string.switch_camera_option,
                R.string.universal_button_option
        };

        for (int option : options) {

            TextView menuItem = new TextView(mainActivity);
            menuItem.setText(mainActivity.getResources().getString(option));
            menuItem.setTextSize(18);
            menuItem.setPadding(16, 16, 16, 16);
            menuItem.setTextColor(mainActivity.getResources().getColor(android.R.color.white));
            menuItem.setBackgroundColor(Color.BLACK);
            menuItem.setAlpha(0.9f);
            mainActivity.getResources().getResourceName(R.string.take_photo_option);

            menuItem.setOnClickListener(v -> {
                createButtonByName(mainActivity.getResources().getResourceEntryName(option));
                mainActivity.getMainUI().layoutUI();
            });


            itemContainer.addView(menuItem);
        }
    }

    final static String FILE_NAME = "WaterInterface.json";

    private static interface VideoListener{
        void run(boolean isVideoCapturing);
    }
    private static interface PauseListener{
        void run(boolean isPause);
    }
}
