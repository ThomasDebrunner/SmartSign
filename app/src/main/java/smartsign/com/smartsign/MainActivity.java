package smartsign.com.smartsign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.sec.android.ngen.common.lib.ssp.DeviceNotReadyException;
import com.sec.android.ngen.common.lib.ssp.Ssp;

import java.io.File;
import java.io.IOException;

import smartsign.com.smartsign.async.PrintAsyncTask;
import smartsign.com.smartsign.async.ScanAsyncTask;
import smartsign.com.smartsign.observer.PrintObserver;
import smartsign.com.smartsign.observer.ScanObserver;
import smartsign.com.smartsign.util.SignGrabberThread;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ScanObserver scanObserver;

    private Button scanButton;
    private Button drawButton;

    private ProgressDialog progressDialog;

    private SharedPreferences mPrefs = null;

    public interface ScanFinishedHandler {
        void scanFinished(String fileName);
    }

    private void uploadAndContinue(String fileName) {
        Log.d(TAG, "uploadAndContinue called. Try to upload stuff and get result");

        String fullFileName = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
        File inputFile = new File(fullFileName);

        // switch activity.
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("INFILE_NAME", inputFile.getAbsolutePath());

        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Initialize Smart UX SDK
        // On mobile context, it's recommended to execute it AsyncTask, since mobile connection might take long
        try {
            Ssp.getInstance().initialize(getApplicationContext());
        }
        catch (final SsdkUnsupportedException e) {
            Log.e(TAG, "SmartUX SDK is not supported on this device or the device has invalid SmartUX Services installed, finish"); finish();
        }
        catch (final SecurityException e) {
            Log.e(TAG, "Necessary permissions are not specified, finish"); finish();
        }
        catch (final DeviceNotReadyException e) { Log.e(TAG, "Device not ready yet, finish"); finish();
        }

        scanObserver.register(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanObserver = new ScanObserver(new Handler(), MainActivity.this, new ScanFinishedHandler() {
            @Override
            public void scanFinished(String fileName) {
                uploadAndContinue(fileName);
            }
        });

        setContentView(R.layout.activity_main);

        scanButton = (Button)findViewById(R.id.scanButton);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ScanAsyncTask(getApplicationContext()).execute();
            }
        });

        drawButton = (Button)findViewById(R.id.drawButton);

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
            }
        });



    }


    @Override
    protected void onPause() {
        super.onPause();
        scanObserver.unregister(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
