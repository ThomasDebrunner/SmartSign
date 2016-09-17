package smartsign.com.smartsign.async;

/**
 * Created by najiji on 17/09/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.sec.android.ngen.common.lib.ssp.CapabilitiesExceededException;
import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.scanner.ScanAttributes;
import com.sec.android.ngen.common.lib.ssp.scanner.ScanAttributesCaps;
import com.sec.android.ngen.common.lib.ssp.scanner.ScanletAttributes;
import com.sec.android.ngen.common.lib.ssp.scanner.ScannerService;

/**
 * Builds attributes and executes Scan To Me launch
 */
public class ScanAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ScanAsyncTask";

    /** Application Context */
    private final Context context;

    /** Preferences to obtain Print Settings */
    private final SharedPreferences prefs;

    /** Error Message string to provide to the user */
    private String mErrorMsg = null;

    public ScanAsyncTask(final Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Override
    protected Void doInBackground(final Void... params) {
        // Obtain Caps to build Scan Attributes
        final Result result = new Result();
        final ScanAttributesCaps caps = ScannerService.getCapabilities(context, result);

        if (caps != null) {
            Log.d(TAG,
                    "Received Caps as:" +
                            " Destination:" + caps.getDestinationList() +
                            ",ColorMode:" + caps.getColorModeList().toString() +
                            ",Duplex:" + caps.getDuplexList().toString() +
                            ",DocFormat(Me):" + caps.getDocumentFormatList(ScanAttributes.Destination.ME).toString() +
                            ",DocFormat(Email):" + caps.getDocumentFormatList(ScanAttributes.Destination.EMAIL).toString() +
                            ",Orientation:" + caps.getOrientationList() +
                            ",ScanSize:" + caps.getScanSizeList());
        }

        if (null == caps) {
            mErrorMsg = "Not able to obtain printers capabilities";
            return null;
        }

        try {

            // Build ScanAttributes based on preferences values
            final ScanAttributes.MeBuilder builder = new ScanAttributes.MeBuilder();

            final ScanAttributes attributes = builder.setColorMode(ScanAttributes.ColorMode.DEFAULT)
                    .setDuplex(ScanAttributes.Duplex.DEFAULT)
                    .setDocumentFormat(ScanAttributes.DocumentFormat.DEFAULT)
                    .setScanSize(ScanAttributes.ScanSize.A4_LANDSCAPE)
                    .setResolution(ScanAttributes.Resolution.DEFAULT)
                    .setOrientation(ScanAttributes.Orientation.DEFAULT)
                    .setMultiPage(ScanAttributes.MultiPage.FALSE)
                    .setSingleScan(ScanAttributes.SingleScan.TRUE)
                    .build(caps);

            final ScanletAttributes taskAttribs = new ScanletAttributes.Builder().setShowSettingsUi(false).build();
            // Submit the job
            final String rid = ScannerService.submit(context, attributes, taskAttribs);

            Log.d(TAG, "rid: " + rid);
        } catch (CapabilitiesExceededException e) {
            Log.e(TAG, "Caps were exceeded: ", e);
            mErrorMsg = "CapabilitiesExceededException: " + e.getMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);

        if (mErrorMsg != null) {
            Log.d(TAG, mErrorMsg);
            Toast.makeText(context, mErrorMsg, Toast.LENGTH_SHORT).show();
        }
    }
}