package net.sourceforge.opencamera;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class FuncButton {
    private final ImageButton imageButton;

    private View.OnClickListener clickListener = null;
    private View.OnLongClickListener longClickListener = null;
    private OnPressListener pressListener = null;
    private OnReleaseListener releaseListener = null;

    private String buttonName = "";


    public void setButtonName(String name){
        buttonName = name;
    }

    public String getButtonName(){
        return buttonName;
    }

    public FuncButton(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    public void setupListeners(){
        imageButton.setOnClickListener(clickListener);
        imageButton.setOnLongClickListener(longClickListener);

        imageButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (pressListener != null) {
                        pressListener.onPress(); // Вызов слушателя нажатия
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (releaseListener != null) {
                        releaseListener.onRelease(); // Вызов слушателя отпускания
                    }
                    break;
            }
            return false;
        });
    }

    public void setOnClickListener(View.OnClickListener listener){
        clickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        longClickListener = listener;
    }

    public void setOnPressListener(OnPressListener listener){
        pressListener = listener;
    }

    public void setOnReleaseListener(OnReleaseListener listener){
        releaseListener = listener;
    }

    static public interface OnPressListener{
        public void onPress();
    }
    static public interface OnReleaseListener{
        public void onRelease();
    }

    public ImageButton getButton() {
        return imageButton;
    }

}
