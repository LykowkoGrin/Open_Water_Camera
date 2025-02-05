package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MovableButton {
    private final ImageButton imageButton;
    private final PointF lastTouchPoint = new PointF();

    private boolean isDragging = false;
    private float touchStartX;
    private float touchStartY;
    private static final int TOUCH_SLOP = 10; // Порог для определения перетаскивания

    private View.OnClickListener clickListener;


    public MovableButton(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    public void setupListeners() {
        imageButton.setOnClickListener(clickListener);
        imageButton.setOnDragListener(null);

        imageButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Сохраняем начальные координаты
                    lastTouchPoint.set(event.getRawX(), event.getRawY());
                    touchStartX = event.getRawX();
                    touchStartY = event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        // Проверяем, превысило ли движение порог
                        float deltaX = event.getRawX() - touchStartX;
                        float deltaY = event.getRawY() - touchStartY;
                        if (Math.hypot(deltaX, deltaY) > TOUCH_SLOP) {
                            isDragging = true;
                        }
                    }

                    if (isDragging) {
                        float dx = event.getRawX() - lastTouchPoint.x;
                        float dy = event.getRawY() - lastTouchPoint.y;
                        updateMargins((int) dx, (int) dy);
                        lastTouchPoint.set(event.getRawX(), event.getRawY());
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        // Выполняем действие при клике
                        imageButton.performClick();//handleClick();
                    }
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    return true;

                default:
                    return false;
            }
        });
    }

    public void setOnClickListener(View.OnClickListener listener){
        clickListener = listener;
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
