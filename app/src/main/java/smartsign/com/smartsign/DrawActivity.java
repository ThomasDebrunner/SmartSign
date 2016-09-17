package smartsign.com.smartsign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.suyati.telvin.drawingboard.DrawingBoard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import smartsign.com.smartsign.util.SignGrabberThread;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";

    private DrawingBoard drawingBoard;
    private Button saveButton;
    private Button refreshButton;


    private ProgressDialog progressDialog;


    private void uploadAndContinue(File inputFile) {
        Log.d(TAG, "uploadAndContinue called. Try to upload stuff and get result");


        File outputFile = null;

        // upload
        try {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Processing");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();


            outputFile = File.createTempFile("result_", ".pdf", Environment.getExternalStorageDirectory());

            SignGrabberThread signGrabberThread = new SignGrabberThread(inputFile, outputFile);
            signGrabberThread.start();

            while(!signGrabberThread.isFinished());

            progressDialog.hide();


        } catch (IOException e) {
            e.printStackTrace();

        }

        // set output to input, if we have no output
        if (outputFile == null) {
            outputFile = inputFile;
        }


        // switch activity.
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("RESULT_FILENAME", outputFile.getAbsolutePath());
        intent.putExtra("INPUT_FILENAME", inputFile.getAbsolutePath());
        startActivity(intent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);


        drawingBoard = (DrawingBoard) findViewById(R.id.drawingBoard);

        drawingBoard.setPenColor(R.color.colorPrimaryDark);
        drawingBoard.setPenWidth(10f);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    File outImg = File.createTempFile("drawing_", ".png", Environment.getExternalStorageDirectory());
                    File outPdf = File.createTempFile("drawing_", ".pdf", Environment.getExternalStorageDirectory());

                    drawingBoard.setBaseFilePath("/");
                    drawingBoard.saveAsImageFile(outImg.getName(), false);

                    Log.i(TAG, String.format("Saved image to file %s", outImg.getAbsolutePath()));

                    Thread.sleep(500);

                    // create pdf
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(outPdf));
                    document.setPageSize(PageSize.A4.rotate());

                    document.open();

                    Image image = Image.getInstance(outImg.getAbsolutePath());

                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / image.getWidth()) * 100;

                    image.scalePercent(scaler);
                    document.add(image);


                    document.close();

                    Log.i(TAG, String.format("Saved pdf to file %s", outPdf.getAbsolutePath()));


                    uploadAndContinue(outPdf);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                drawingBoard.clearBoard();
            }
        });
    }
}
