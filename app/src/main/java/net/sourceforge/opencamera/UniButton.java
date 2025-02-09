package net.sourceforge.opencamera;

import android.view.View;
import android.widget.ImageButton;

public class UniButton extends FuncButton {

    final private MainActivity mainActivity;

    private int uniClickListenerRes = FunctionsNamesRes[0];
    private int uniLongClickListenerRes = FunctionsNamesRes[0];
    private int uniOnPressListenerRes = FunctionsNamesRes[0];
    private int uniOnReleaseListenerRes = FunctionsNamesRes[0];


    public UniButton(ImageButton imageButton,MainActivity mainActivity){
        super(imageButton);
        this.mainActivity = mainActivity;
    }


    public void setOnClickListener(int funcResName) {
        uniClickListenerRes = funcResName;
        super.setOnClickListener((View v) ->
        {
            getFuncByResId(funcResName).run();
        });
    }
    public void setOnLongClickListener(int funcResName) {
        uniLongClickListenerRes = funcResName;
        super.setOnLongClickListener((View v) -> {
            getFuncByResId(funcResName).run();
            return true;
        });
    }
    public void setOnPressListener(int funcResName) {
        uniOnPressListenerRes = funcResName;
        super.setOnPressListener(() -> getFuncByResId(funcResName).run());
    }
    public void setOnReleaseListener(int funcResName) {
        uniOnReleaseListenerRes = funcResName;
        super.setOnReleaseListener(() -> getFuncByResId(funcResName).run());
    }


    public int getClickListenerResId(){
        return uniClickListenerRes;
    }
    public int getLongClickListenerResId(){
        return uniLongClickListenerRes;
    }
    public int getOnPressListenerResId(){
        return uniOnPressListenerRes;
    }
    public int getOnReleaseListenerResId(){
        return uniOnReleaseListenerRes;
    }




    private Runnable getFuncByResId(int resourceId){
        switch (resourceId){
            case R.string.take_photo_option:
                return () -> {
                    if(mainActivity.getPreview().isVideo())
                        mainActivity.clickedSwitchVideo(getButton());
                    else mainActivity.takePicture(false);
                };

            case R.string.take_video_option:
                return () -> {
                    if (!mainActivity.getPreview().isVideo())
                        mainActivity.clickedSwitchVideo(getButton());
                    else
                        mainActivity.takePicture(false);
                };

            case R.string.pause_video_option:
                return ()  -> mainActivity.clickedPauseVideo(getButton());

            case R.string.take_photo_when_video_recording_option:
                return () -> mainActivity.takePicture(true);

            case R.string.switch_camera_option:
                return () -> mainActivity.clickedSwitchCamera(null);

            default: return () -> {};
        }

    }

    public static final int[] FunctionsNamesRes = {
            R.string.nothing_option,
            R.string.take_photo_option,
            R.string.take_video_option,
            R.string.pause_video_option,
            R.string.take_photo_when_video_recording_option,
            R.string.switch_camera_option
    };
}
