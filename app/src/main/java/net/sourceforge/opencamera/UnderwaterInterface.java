package net.sourceforge.opencamera;

import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.ImageButton;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skydoves.colorpickerview.ColorPickerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Set;


public class UnderwaterInterface {
    private MainActivity mainActivity;
    //btn = {FuncButton@25457}
    private Set<FuncButton> funcButtons = new LinkedHashSet<FuncButton>();

    private boolean inMode = false;
    private boolean inEditMode = false;

    private LinearLayout itemContainer;
    private View waterPopup;

    private Set<VideoListener> videoListeners = new LinkedHashSet<VideoListener>();
    private Set<PauseListener> pauseListeners = new LinkedHashSet<PauseListener>();
    private AlertDialog waterDialog = null;




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

    private void loadElements() {


        File file = new File(mainActivity.getFilesDir(), FILE_NAME);
        List<Map<String, List<String>>> elementsList = new ArrayList<>();

        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();

            Type type = new TypeToken<List<Map<String, List<String>>>>(){}.getType();
            elementsList = gson.fromJson(reader, type);

            if (elementsList == null) elementsList = new ArrayList<>();

        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        for(Map<String, List<String>> elementMap : elementsList) {

            if (elementMap == null) continue;

            try {

                Map.Entry<String, List<String>> entry = elementMap.entrySet().iterator().next();

                FuncButton funcButton = createButtonByName(entry.getKey());

                List<String> paramsStr = entry.getValue();

                if (paramsStr.size() < 4) continue;

                if (entry.getKey().equals("universal_button_option")) {
                    if (paramsStr.size() < 9)
                        continue;
                    List<String> listenersFuncs = new ArrayList<>(paramsStr.subList(4, paramsStr.size()));

                    ((UniButton) funcButton).setListenersByNames(listenersFuncs);
                    ((UniButton) funcButton).setFilerColor(Integer.parseInt(paramsStr.get(8)));
                }

                funcButton.getButton().post(() -> {
                    funcButton.getButton().setX(Float.parseFloat(paramsStr.get(0)));
                    funcButton.getButton().setY(Float.parseFloat(paramsStr.get(1)));

                });

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) funcButton.getButton().getLayoutParams();

                params.width = (int) Float.parseFloat(paramsStr.get(2));
                params.height = (int) Float.parseFloat(paramsStr.get(3));

                funcButton.getButton().setLayoutParams(params);


            } catch (Exception e) {
                System.err.println("Ошибка загрузки данных: " + e.toString());
            }
        }
    }

    private void saveElements(){
        List<Map<String, List<String>>> elementsList = new ArrayList<>();

        for (FuncButton btn : funcButtons) {
            // Создаем список параметров для кнопки
            List<String> btnParams = new ArrayList<>();

            String btnSaveName = btn.getButtonName();

            btnParams.add(String.valueOf(btn.getButton().getX()));
            btnParams.add(String.valueOf(btn.getButton().getY()));
            btnParams.add(String.valueOf(btn.getButton().getWidth()));
            btnParams.add(String.valueOf(btn.getButton().getHeight()));

            if ("universal_button_option".equals(btnSaveName) && btn instanceof UniButton) {
                UniButton uniBtn = (UniButton) btn;
                btnParams.addAll(uniBtn.getListenersNames());
                btnParams.add(String.valueOf(uniBtn.getFilterColor()));
            }

            elementsList.add(Collections.singletonMap(btnSaveName, btnParams));
        }


        File file = new File(mainActivity.getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new Gson();
            String json = gson.toJson(elementsList);
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

        saveElements();
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

        MovableButton.OnClickListener editClickListener = (() -> {
            waterDialog = WaterDialogHelper.showMinimalDialog(mainActivity, new WaterDialogHelper.DialogListener() {
                @Override
                public void onApplyClicked() {

                }

                @Override
                public void onDeleteClicked() {
                    ((ViewManager)btn.getButton().getParent()).removeView(btn.getButton());
                    funcButtons.remove(btn);

                }
            });
            mainActivity.getMainUI().layoutUI();

        });
        MovableButton.OnClickListener editUniClickListener = (() -> {
            waterDialog = WaterDialogHelper.showDialog(mainActivity,(UniButton)btn, new WaterDialogHelper.DialogListener() {
                @Override
                public void onApplyClicked() {
                    Spinner clickSpinner = waterDialog.findViewById(R.id.spinner1);
                    Spinner holdSpinner = waterDialog.findViewById(R.id.spinner2);
                    Spinner pressSpinner = waterDialog.findViewById(R.id.spinner3);
                    Spinner releaseSpinner = waterDialog.findViewById(R.id.spinner4);

                    ((UniButton)btn).setOnClickListener(UniButton.FunctionsNamesRes[(int) clickSpinner.getSelectedItemId()]);
                    ((UniButton)btn).setOnLongClickListener(UniButton.FunctionsNamesRes[(int) holdSpinner.getSelectedItemId()]);
                    ((UniButton)btn).setOnPressListener(UniButton.FunctionsNamesRes[(int) pressSpinner.getSelectedItemId()]);
                    ((UniButton)btn).setOnReleaseListener(UniButton.FunctionsNamesRes[(int) releaseSpinner.getSelectedItemId()]);


                    EditText sizeInput = waterDialog.findViewById(R.id.size_input);
                    String inputText = sizeInput.getText().toString().trim();
                    int size = btn.getButton().getWidth();

                    if (!inputText.isEmpty()) {
                        try {
                            size = Integer.parseInt(inputText);
                        } catch (NumberFormatException e) {
                            Log.e("TAG", "Некорректный формат числа");
                        }
                    }

                    if(size < UniButton.minButtonSize) size = UniButton.minButtonSize;

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btn.getButton().getLayoutParams();
                    params.width = size;
                    params.height = size;
                    btn.getButton().setLayoutParams(params);

                    ColorPickerView colorPicker = waterDialog.findViewById(R.id.color_picker_view);
                    ((UniButton)btn).setFilerColor(colorPicker.getColor());

                }

                @Override
                public void onDeleteClicked() {
                    ((ViewManager)btn.getButton().getParent()).removeView(btn.getButton());
                    funcButtons.remove(btn);
                }
            });
            mainActivity.getMainUI().layoutUI();

        });
        MovableButton.OnClickListener editZoomClickListener = (() -> {

            waterDialog = WaterDialogHelper.showZoomDialog(mainActivity, new WaterDialogHelper.DialogListener() {
                @Override
                public void onApplyClicked() {
                    EditText speedInput = waterDialog.findViewById(R.id.speed_input);
                    zoomSpeed = Integer.parseInt(speedInput.getText().toString().trim());
                }

                @Override
                public void onDeleteClicked() {
                    ((ViewManager)btn.getButton().getParent()).removeView(btn.getButton());
                    funcButtons.remove(btn);

                }
            });
            mainActivity.getMainUI().layoutUI();

        });

        if(btn.getButtonName().equals("universal_button_option"))
            movBtn.setOnClickListener(editUniClickListener);
        else if(btn.getButtonName().equals("zoom_plus_option") || btn.getButtonName().equals("zoom_minus_option"))
            movBtn.setOnClickListener(editZoomClickListener);
        else
            movBtn.setOnClickListener(editClickListener);

        movBtn.setupListeners();

    }

    static public int getZoomSpeed(){
        return zoomSpeed;
    }

    private FuncButton createButtonByName(String name){
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
                    if (!mainActivity.getPreview().isVideo())
                        mainActivity.clickedSwitchVideo(null);
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

                newButton.setOnClickListener((View v) -> mainActivity.clickedPauseVideo(null));

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
                newButton.setOnClickListener((View v) -> mainActivity.clickedSwitchCamera(null));
                break;

            case "universal_button_option":

                newButton = new UniButton(new ImageButton(mainActivity),mainActivity);
                RelativeLayout.LayoutParams uniButtonParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 60),
                        dpToPx(mainActivity, 60)
                );
                newButton.getButton().setLayoutParams(uniButtonParams);

                newButton.getButton().setImageResource(R.drawable.white_background);
                newButton.getButton().setPadding(0, 0, 0, 0);

                break;

            case "zoom_plus_option":
                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams zoomPlusCameraParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 100),
                        dpToPx(mainActivity, 100)
                );
                newButton.getButton().setLayoutParams(zoomPlusCameraParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                //newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.baseline_add_circle_outline_72);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> {
                    int zoomProgress = mainActivity.getPreview().getCameraController().getZoom();
                    int maxZoom = mainActivity.getPreview().getMaxZoom();

                    int newZoom = zoomProgress + zoomSpeed;
                    zoomProgress = Math.min(newZoom, maxZoom);
                    mainActivity.getPreview().zoomTo(zoomProgress);
                });
                break;

            case "zoom_minus_option":
                newButton = new FuncButton(new ImageButton(mainActivity));
                RelativeLayout.LayoutParams zoomMinusCameraParams = new RelativeLayout.LayoutParams(
                        dpToPx(mainActivity, 100),
                        dpToPx(mainActivity, 100)
                );
                newButton.getButton().setLayoutParams(zoomMinusCameraParams);
                newButton.getButton().setPadding(
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10),
                        dpToPx(mainActivity, 10));
                newButton.getButton().setScaleType(ImageView.ScaleType.FIT_CENTER);
                //newButton.getButton().setContentDescription(mainActivity.getString(R.string.take_photo));
                newButton.getButton().setImageResource(R.drawable.baseline_remove_circle_outline_72);
                newButton.getButton().setBackgroundDrawable(null);
                newButton.setOnClickListener((View v) -> {
                    int zoomProgress = mainActivity.getPreview().getCameraController().getZoom();

                    int newZoom = zoomProgress - zoomSpeed;
                    zoomProgress = Math.max(newZoom, 0);
                    mainActivity.getPreview().zoomTo(zoomProgress);
                });
                break;
        }


        if(newButton == null) return null;

        newButton.setButtonName(name);

        RelativeLayout relativeLayout = mainActivity.findViewById(R.id.relative_layout);
        relativeLayout.addView(newButton.getButton());

        if(inEditMode) setupButtonEditMode(newButton);
        else {
            newButton.setupListeners();
            newButton.getButton().setVisibility(View.GONE);
        }

        funcButtons.add(newButton);

        return newButton;

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
                R.string.universal_button_option,
                R.string.zoom_plus_option,
                R.string.zoom_minus_option
        };

        for (int option : options) {

            TextView menuItem = new TextView(mainActivity);
            menuItem.setText(mainActivity.getResources().getString(option));
            menuItem.setTextSize(18);
            menuItem.setPadding(16, 16, 16, 16);
            menuItem.setTextColor(mainActivity.getResources().getColor(android.R.color.white));
            menuItem.setBackgroundColor(Color.BLACK);
            menuItem.setAlpha(0.9f);
            //mainActivity.getResources().getResourceName(R.string.take_photo_option);

            menuItem.setOnClickListener(v -> {
                createButtonByName(mainActivity.getResources().getResourceEntryName(option));
                mainActivity.getMainUI().layoutUI();
            });


            itemContainer.addView(menuItem);
        }
    }

    private static int zoomSpeed = 2;


    final static String FILE_NAME = "WaterInterface.json";

    private static interface VideoListener{
        void run(boolean isVideoCapturing);
    }
    private static interface PauseListener{
        void run(boolean isPause);
    }
}
