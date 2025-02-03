package net.sourceforge.opencamera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

public class WaterDialogHelper {

    public interface DialogListener {
        void onApplyClicked(String[] selectedItems);
        void onDeleteClicked();
    }

    public static AlertDialog showDialog(Context context, DialogListener listener) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.water_dialog, null);

        // Инициализация кастомных Spinner'ов
        Spinner spinner1 = dialogView.findViewById(R.id.spinner1);
        Spinner spinner2 = dialogView.findViewById(R.id.spinner2);
        Spinner spinner3 = dialogView.findViewById(R.id.spinner3);
        Spinner spinner4 = dialogView.findViewById(R.id.spinner4);


        List<String> data = Arrays.asList("Item 1", "Item 2", "Item 3");

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

        // Настройка диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Обработчики кнопок
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnAccept).setOnClickListener(v -> {
            // Логика принятия
            dialog.dismiss();
        });

        return dialog;
    }

    private static float calculateScaleFactor(float rotationDegrees) {

        double radians = Math.toRadians(rotationDegrees % 90);
        return (float) (1f / Math.cos(radians));
    }
}