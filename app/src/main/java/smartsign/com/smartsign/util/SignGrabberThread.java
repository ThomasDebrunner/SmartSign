package smartsign.com.smartsign.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Class that uploads a pdf, and downloads a pdf.
 * Created by najiji on 17/09/16.
 */
public class SignGrabberThread extends Thread {

    private static final String POST_URL = "http://159.122.252.231:5000/upload";

    private File uploadFile;
    private File targetFile;
    private boolean corporate;

    private boolean finished = false;

    private static final String TAG = "SignGrabberThread";

    public SignGrabberThread(File uploadFile, File targetFile, boolean corporate) {
        this.uploadFile = uploadFile;
        this.targetFile = targetFile;
        this.corporate = corporate;
    }


    @Override
    public void run() {
        try {

            if (targetFile.exists()) {
                targetFile.delete();
            }

            MultipartUtility mpUtility = new MultipartUtility(POST_URL, "US-ASCII");
            mpUtility.addFilePart("file", uploadFile);
            if (corporate) {
                Log.d(TAG, "Set to TRUE");
                mpUtility.addFormField("cd", "true");
            }
            else {
                mpUtility.addFormField("cd", "false");
            }
            mpUtility.downloadResponse(targetFile);
            finished = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            finished = true;
        }
        finished = true;
    }

    /**
     * Method returns true, if the process is finished
     * @return
     */
    public boolean isFinished() {
        return finished;
    }

}
