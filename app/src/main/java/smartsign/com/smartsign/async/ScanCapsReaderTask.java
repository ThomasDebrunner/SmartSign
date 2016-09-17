package smartsign.com.smartsign.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.scanner.ScanAttributes;
import com.sec.android.ngen.common.lib.ssp.scanner.ScanAttributesCaps;
import com.sec.android.ngen.common.lib.ssp.scanner.ScannerService;

import java.lang.ref.WeakReference;

import smartsign.com.smartsign.MainActivity;

/**
 * Created by najiji on 17/09/16.
 */
public class ScanCapsReaderTask extends AsyncTask<Void, Void, ScanAttributesCaps> {

    private MainActivity mainActivity;

    private final WeakReference<Context> contextRef;
    private static final String TAG = "ScanCapsReaderTask";


    public ScanCapsReaderTask(final Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        contextRef = new WeakReference<Context>(context);
    }

    @Override
    protected ScanAttributesCaps doInBackground(final Void... params) {
        ScanAttributesCaps scanAttributeCaps = null;
        if (contextRef.get() != null) {
            // read the scan attributes capabilities from the machine
            Result result = new Result();
            scanAttributeCaps = ScannerService.getCapabilities(contextRef.get(), result);

            if (scanAttributeCaps == null) {
                Log.e(TAG, "getCapabilities returned null. Result Code = " + result.mCode + " Cause = " + result.mCause);
            }
        }
        return scanAttributeCaps;
    }

    @Override
    protected void onPostExecute(final ScanAttributesCaps result) {
        super.onPostExecute(result);
        if (result != null) {
            // result received successfully, update the textView to show the result
            Log.d(TAG, "Successful response received from MFP");
            final String docFormats = result.getDocumentFormatList(ScanAttributes.Destination.ME).toString();
            final String documentFormatsStr = String.format("Document formats supported by ScanToME are %s ", docFormats);
            mainActivity.scannerPropsTextView.setText(documentFormatsStr);
        }
        else {
            Log.e(TAG, "Response from MFP is null");
        }
    }
}
