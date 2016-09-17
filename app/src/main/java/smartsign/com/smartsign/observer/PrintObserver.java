package smartsign.com.smartsign.observer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.SpsCause;
import com.sec.android.ngen.common.lib.ssp.printer.PrinterService;
import com.sec.android.ngen.common.lib.ssp.printer.Printlet;

import java.util.List;

/**
 * Observer for Print result and progress.
 * Created by najiji on 17/09/16.
 */
public class PrintObserver extends PrinterService.AbstractPrintletObserver {

    private Activity activity;

    private static final String TAG = "PrintObserver";

    private int jobId = 0;


    /**
     * Constructor
     * @param handler
     */
    public PrintObserver(final Handler handler, Activity activity) {
        super(handler);
        this.activity = activity;
    }

    private void showToast(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onCancel(final String rid) {
        Log.d(TAG, "Received Print Cancel");
        showToast("Print cancelled!");
    }

    @Override
    public void onComplete(final String rid, final Bundle bundle) {
        Log.d(TAG, "Received Print Complete");
        showToast("Print completed!");

        Log.d(TAG, "onComplete: with data \n" +
                "  KEY_IMAGE_COUNT: " + bundle.getInt(Printlet.Keys.KEY_IMAGE_COUNT, 0) + "\n" +
                "  KEY_SET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SET_COUNT, 0) + "\n" +
                "  KEY_SHEET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SHEET_COUNT, 0));
    }

    @Override
    public void onFail(final String rid, final Result result) {
        Log.e(TAG, "Received Print Fail, Result " + result.mCode);
        showToast("Print failed! " + result);

        if (result.mCode == Result.RESULT_WS_FAILURE) {
            final Result.WSCause cause = Result.getWSCause(result);

            if (cause != null) {
                Log.w(TAG, cause.toString());
            } else {
                Log.w(TAG, "Failed without any cause");
            }

            showToast( "Web service cause: " + cause);
        } else {
            final List<SpsCause> causes = result.getSpsCause();

            showToast("Sps results: " + causes);
        }
    }

    @Override
    public void onProgress(final String rid, final Bundle bundle) {
        if (bundle.containsKey(Printlet.Keys.KEY_JOBID)) {
            jobId = bundle.getInt(Printlet.Keys.KEY_JOBID);
            Log.d(TAG, "onProgress: Received jobID as " + jobId);
            showToast("Job ID is " + jobId);

            Log.d(TAG, "onProgress: with data \n" +
                    "  KEY_IMAGE_COUNT: " + bundle.getInt(Printlet.Keys.KEY_IMAGE_COUNT, 0) + "\n" +
                    "  KEY_SET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SET_COUNT, 0) + "\n" +
                    "  KEY_SHEET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SHEET_COUNT, 0));
        }
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
