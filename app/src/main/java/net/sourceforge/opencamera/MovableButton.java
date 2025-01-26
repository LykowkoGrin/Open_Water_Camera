package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MovableButton {
    private final ImageButton imageButton;
    private final PointF lastTouchPoint = new PointF();


    public MovableButton(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    public void setupListeners(){
        // Обработчик касаний для перемещения

        imageButton.setOnClickListener(null);
        imageButton.setOnDragListener(null);

        imageButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Сохраняем начальную точку касания
                    lastTouchPoint.set(event.getRawX(), event.getRawY());
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Вычисляем смещение
                    float dx = event.getRawX() - lastTouchPoint.x;
                    float dy = event.getRawY() - lastTouchPoint.y;

                    // Обновляем отступы
                    updateMargins((int) dx, (int) dy);

                    // Обновляем точку последнего касания
                    lastTouchPoint.set(event.getRawX(), event.getRawY());
                    return true;
            }
            return false;
        });
    }

    public void returnOldListeners(){

    }

    private void updateMargins(int dx, int dy) {
        // Проверяем, что у кнопки есть LayoutParams
        if (imageButton.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageButton.getLayoutParams();
            params.leftMargin += dx;
            params.topMargin += dy;
            imageButton.setLayoutParams(params);
        }
    }

    public ImageButton getButton() {
        return imageButton;
    }
}
