package smartsign.com.smartsign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.IOException;

import smartsign.com.smartsign.async.PrintAsyncTask;
import smartsign.com.smartsign.observer.PrintObserver;
import smartsign.com.smartsign.util.SignGrabberThread;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private PDFView pdfView;

    private Button printButton;
    private PrintObserver printObserver;

    private ProgressDialog progressDialog;

    private File inFile;
    private File outFile;


    private void refreshOutput(int style) {
        // upload
        try {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Processing");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();


            outFile = File.createTempFile("result_", ".pdf", Environment.getExternalStorageDirectory());

            SignGrabberThread signGrabberThread = new SignGrabberThread(inFile, outFile);
            signGrabberThread.start();

            while(!signGrabberThread.isFinished());

            progressDialog.hide();


        } catch (IOException e) {
            e.printStackTrace();

        }

        // set output to input, if we have no output
        if (outFile == null) {
            outFile = inFile;
        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        printObserver = new PrintObserver(new Handler(), PreviewActivity.this);


        setContentView(R.layout.activity_preview);

        pdfView = (PDFView)findViewById(R.id.pdfView);
        printButton = (Button)findViewById(R.id.printButton);


        Intent intent = getIntent();
        String fullFileName = intent.getStringExtra("RESULT_FILENAME");
        Log.d(TAG, fullFileName);

        outFile = new File(fullFileName);
        if(outFile.exists()) {
            pdfView.fromFile(outFile).pages(0).enableSwipe(false).load();
        }
        else {
            Log.d(TAG, String.format("PDF file %s does not exist", outFile.getAbsolutePath()));
        }


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass application context
                new PrintAsyncTask(getApplicationContext(), printObserver, outFile).execute();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        printObserver.register(getApplicationContext());
    }


    @Override
    protected void onPause() {
        super.onPause();
        printObserver.unregister(getApplicationContext());
    }
}
