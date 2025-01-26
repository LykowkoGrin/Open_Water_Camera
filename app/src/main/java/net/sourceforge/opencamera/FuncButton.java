package net.sourceforge.opencamera;

import android.view.View;
import android.widget.ImageButton;

public class FuncButton {
    private final ImageButton imageButton;
    private View.OnClickListener clickListener = null;

    //setDoubleClickListener,setHoldListener,setClickListener,setPressListener,setReleaseListener

    public FuncButton(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    public void setupListeners(){
        imageButton.setOnClickListener(clickListener);
        imageButton.setOnDragListener(null);
        imageButton.setOnTouchListener(null);
    }

    public void setOnClickListener(View.OnClickListener listener){
        clickListener = listener;
    }

    public ImageButton getButton() {
        return imageButton;
    }

}
