package smartsign.com.smartsign;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private Button standardButton;
    private Button corporateButton;

    private PrintObserver printObserver;

    private ProgressDialog progressDialog;

    private File inFile;
    private File outFile;


    private void refreshOutput(boolean corporate) {
        Log.d(TAG, "uploadAndContinue called. Try to upload stuff and get result");


        // upload
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        SignGrabberThread signGrabberThread = new SignGrabberThread(inFile, outFile, corporate);
        signGrabberThread.start();

        while(!signGrabberThread.isFinished());

        progressDialog.hide();
    }


    private void refreshView() {
        if(outFile.exists()) {
            pdfView.fromFile(outFile).pages(0).enableSwipe(false).load();
        }
        else {
            Log.d(TAG, String.format("PDF file %s does not exist", outFile.getAbsolutePath()));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        printObserver = new PrintObserver(new Handler(), PreviewActivity.this);
        setContentView(R.layout.activity_preview);

        pdfView = (PDFView)findViewById(R.id.pdfView);
        printButton = (Button)findViewById(R.id.printButton);
        standardButton = (Button)findViewById(R.id.standardButton);
        corporateButton = (Button)findViewById(R.id.corporateButton);

        standardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshOutput(false);
                refreshView();
            }
        });


        corporateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("BLAAA", "Setting to TRUE");
                refreshOutput(true);
                refreshView();
            }
        });

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!outFile.exists()) {
                    Toast.makeText(PreviewActivity.this, "There is nothing to print", Toast.LENGTH_SHORT);
                    return;
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
                builder.setTitle("Copies");

                final EditText input = new EditText(PreviewActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int copies = 1;
                        String inp = input.getText().toString();

                        try {
                            copies = Integer.parseInt(inp);
                        } catch(NumberFormatException e) {
                            copies = 1;
                        }

                        // save the enviromnent
                        if (copies > 5) {
                            copies = 5;
                        }


                        // start printing
                        new PrintAsyncTask(getApplicationContext(), printObserver, outFile, copies).execute();

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


            }
        });

        Intent intent = getIntent();
        inFile = new File(intent.getStringExtra("INFILE_NAME"));

        Log.d(TAG, inFile.getAbsolutePath());


        try {
            outFile = File.createTempFile("result_", ".pdf", Environment.getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }


        refreshOutput(false);
        refreshView();

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
