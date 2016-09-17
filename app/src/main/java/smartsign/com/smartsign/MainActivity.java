package smartsign.com.smartsign;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.sec.android.ngen.common.lib.ssp.DeviceNotReadyException;
import com.sec.android.ngen.common.lib.ssp.Ssp;

import smartsign.com.smartsign.async.PrintAsyncTask;
import smartsign.com.smartsign.async.ScanAsyncTask;
import smartsign.com.smartsign.async.ScanCapsReaderTask;
import smartsign.com.smartsign.observer.PrintObserver;
import smartsign.com.smartsign.observer.ScanObserver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PrintObserver printObserver;
    private ScanObserver scanObserver;

    private Button scannerPropsButton;
    private Button printButton;
    private Button scanButton;
    public TextView scannerPropsTextView;

    private SharedPreferences mPrefs = null;


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

        printObserver.register(getApplicationContext());
        scanObserver.register(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        printObserver = new PrintObserver(new Handler(), MainActivity.this);
        scanObserver = new ScanObserver(new Handler(), MainActivity.this);

        setContentView(R.layout.activity_main);

        scannerPropsTextView = (TextView)findViewById(R.id.scannerPropsTextView);
        scannerPropsButton = (Button)findViewById(R.id.scannerPropsButton);
        printButton = (Button)findViewById(R.id.printButton);
        scanButton = (Button)findViewById(R.id.scanButton);

        scannerPropsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ScanCapsReaderTask(MainActivity.this,  MainActivity.this).execute((Void)null);
            }
        });


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass application context
                new PrintAsyncTask(getApplicationContext(), printObserver).execute();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ScanAsyncTask(getApplicationContext()).execute();
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        printObserver.unregister(getApplicationContext());
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
