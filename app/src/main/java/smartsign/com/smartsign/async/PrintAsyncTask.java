package smartsign.com.smartsign.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sec.android.ngen.common.lib.ssp.CapabilitiesExceededException;
import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes.*;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributesCaps;
import com.sec.android.ngen.common.lib.ssp.printer.PrinterService;
import com.sec.android.ngen.common.lib.ssp.printer.PrintletAttributes;

import java.io.File;
import java.lang.ref.WeakReference;

import smartsign.com.smartsign.observer.PrintObserver;

/**
 * Async task to request printers capabilities and launch Print.
 */
public class PrintAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "PrintAsyncTask";

    /**
     * Observer to be used as callback
     */
    private final WeakReference<PrintObserver> observer;
    /**
     * Application Context
     */
    private final Context context;
    /**
     * Preferences to obtain Print Settings
     */
    private final SharedPreferences prefs;

    private int copies;

    private File file;
    /**
     * Error Message string to provide to the user
     */
    private String errorMsg = null;

    public PrintAsyncTask(final Context context, final PrintObserver observer, File file, int copies) {
        this.observer = new WeakReference<>(observer);
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.file = file;
        this.copies = copies;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        final Result result = new Result();
        final PrintAttributesCaps caps = PrinterService.getCapabilities(context, result);

        if (caps != null) {
            Log.d(TAG, "Received Caps as:" +
                    "AutoFit: " + caps.getAutoFitList() +
                    ", ColorMode: " + caps.getColorModeList() +
                    ", Max Copies: " + caps.getMaxCopies() +
                    ", Duplex: " + caps.getDuplexList());
        }


        final Resources res = context.getResources();

        if (null == caps) {
            errorMsg = "Not able to obtain printers capabilities";
            return null;
        }

        try {

            Log.i(TAG, "Selected path: " + file.getAbsolutePath());

            // Build PrintAttributes based on preferences values
            final PrintAttributes attributes =
                    new PrintAttributes.PrintFromStorageBuilder(Uri.fromFile(file))
                            .setColorMode(ColorMode.DEFAULT)
                            .setDuplex(Duplex.DEFAULT)
                            .setAutoFit(AutoFit.DEFAULT)
                            .setCopies(copies)
                            .build(caps);

            // Clean stored job id if any
            if (observer.get() != null) {
                // Reset old job id
                observer.get().setJobId(0);
            }

            final PrintletAttributes taskAttribs = new PrintletAttributes.Builder().setShowSettingsUi(false).build();

            // Submit the job
            PrinterService.submit(context, attributes, taskAttribs);
        } catch (final CapabilitiesExceededException e) {
            Log.e(TAG, "Caps were exceeded: ", e);
            errorMsg = "CapabilitiesExceededException: " + e.getMessage();
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument was provided: ", e);
            errorMsg = e.getMessage();
        }

        return null;
    }
}