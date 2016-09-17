package smartsign.com.smartsign;

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

import smartsign.com.smartsign.async.PrintAsyncTask;
import smartsign.com.smartsign.observer.PrintObserver;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private PDFView pdfView;

    private Button printButton;
    private PrintObserver printObserver;

    private File pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        printObserver = new PrintObserver(new Handler(), PreviewActivity.this);


        setContentView(R.layout.activity_preview);

        pdfView = (PDFView)findViewById(R.id.pdfView);
        printButton = (Button)findViewById(R.id.printButton);


        Intent intent = getIntent();
        String fullFileName = Environment.getExternalStorageDirectory().getPath() + "/" + intent.getStringExtra("RESULT_FILENAME");
        Log.d(TAG, fullFileName);

        pdfFile = new File(fullFileName);
        if(pdfFile.exists()) {
            pdfView.fromFile(pdfFile).pages(0).enableSwipe(false).load();
        }
        else {
            Log.d(TAG, String.format("PDF file %s does not exist", pdfFile.getAbsolutePath()));
        }


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass application context
                new PrintAsyncTask(getApplicationContext(), printObserver, pdfFile).execute();
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
