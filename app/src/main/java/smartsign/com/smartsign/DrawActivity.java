package smartsign.com.smartsign;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.suyati.telvin.drawingboard.DrawingBoard;

import java.io.File;
import java.io.IOException;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";

    private DrawingBoard drawingBoard;
    private Button saveButton;
    private Button refreshButton;

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
                    File outFile = File.createTempFile("drawing_", ".png", DrawActivity.this.getCacheDir());

                    drawingBoard.setBaseFilePath(DrawActivity.this.getCacheDir().getPath());
                    drawingBoard.saveAsImageFile(outFile.getName(), false);

                    Log.i(TAG, String.format("Saved to file %s", outFile.getAbsolutePath()));

                } catch (IOException e) {
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
