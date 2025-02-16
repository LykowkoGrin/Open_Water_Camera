package net.sourceforge.opencamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.skydoves.colorpickerview.ColorPickerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaterDialogHelper {

    public interface DialogListener {
        void onApplyClicked();
        void onDeleteClicked();
    }

    static private int getSpinnerListId(int resourceId){
        for(int i = 0;i < UniButton.FunctionsNamesRes.length;i++){
            if(UniButton.FunctionsNamesRes[i] == resourceId)
                return i;
        }

        return 0;
    }


    public static AlertDialog showDialog(Context context,UniButton uniButton, DialogListener listener) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.water_dialog, null);

        // Инициализация кастомных Spinner'ов
        Spinner spinner1 = dialogView.findViewById(R.id.spinner1);
        Spinner spinner2 = dialogView.findViewById(R.id.spinner2);
        Spinner spinner3 = dialogView.findViewById(R.id.spinner3);
        Spinner spinner4 = dialogView.findViewById(R.id.spinner4);


        List<String> data = new ArrayList<>();
        for(int id : UniButton.FunctionsNamesRes){
            data.add(context.getString(id));
        }

        // Настройка адаптеров (обычный ArrayAdapter)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                data
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Применение настроек к Spinner'ам
        for (Spinner spinner : Arrays.asList(spinner1, spinner2, spinner3, spinner4)) {
            spinner.setAdapter(adapter);
        }


        EditText sizeInput = dialogView.findViewById(R.id.size_input);

        ColorPickerView colorPicker = dialogView.findViewById(R.id.color_picker_view);
        colorPicker.setInitialColor(uniButton.getFilterColor());

        sizeInput.setText(String.valueOf(uniButton.getButton().getWidth()));

        spinner1.setSelection(getSpinnerListId(uniButton.getClickListenerResId()));
        spinner2.setSelection(getSpinnerListId(uniButton.getLongClickListenerResId()));
        spinner3.setSelection(getSpinnerListId(uniButton.getOnPressListenerResId()));
        spinner4.setSelection(getSpinnerListId(uniButton.getOnReleaseListenerResId()));

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();


        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnAccept).setOnClickListener(v -> {
            listener.onApplyClicked();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            listener.onDeleteClicked();
            dialog.dismiss();
        });


        return dialog;
    }

    public static AlertDialog showMinimalDialog(Context context, int btnSize, DialogListener listener){
        if (!(context instanceof Activity)) {
            Log.e("DialogError", "Context is not an Activity!");
            return null;
        }

        // Создание View из layout-файла
        View dialogView = LayoutInflater.from(context).inflate(R.layout.minimal_water_dialog, null);

        // Создание диалога с правильной темой
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        // Проверка активности перед отображением
        if (!((Activity) context).isFinishing()) {
            dialog.show();
        } else {
            Log.e("DialogError", "Activity is finishing!");
            return null;
        }

        EditText sizeInput = dialogView.findViewById(R.id.size_input);
        sizeInput.setText(String.valueOf(btnSize));


        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            listener.onDeleteClicked();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnAccept).setOnClickListener(v -> {
            listener.onApplyClicked();
            dialog.dismiss();
        });


        return dialog;
    }

    public static AlertDialog showZoomDialog(Context context,int btnSize, DialogListener listener){
        if (!(context instanceof Activity)) {
            Log.e("DialogError", "Context is not an Activity!");
            return null;
        }

        // Создание View из layout-файла
        View dialogView = LayoutInflater.from(context).inflate(R.layout.zoom_water_dialog, null);

        EditText speedInput = dialogView.findViewById(R.id.speed_input);
        speedInput.setText(String.valueOf(UnderwaterInterface.getZoomSpeed()));

        // Создание диалога с правильной темой
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText sizeInput = dialogView.findViewById(R.id.size_input);
        sizeInput.setText(String.valueOf(btnSize));

        // Проверка активности перед отображением
        if (!((Activity) context).isFinishing()) {
            dialog.show();
        } else {
            Log.e("DialogError", "Activity is finishing!");
            return null;
        }


        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnAccept).setOnClickListener(v -> {
            listener.onApplyClicked();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            listener.onDeleteClicked();
            dialog.dismiss();
        });


        return dialog;
    }
}