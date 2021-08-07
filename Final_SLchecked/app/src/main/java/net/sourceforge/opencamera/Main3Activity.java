package net.sourceforge.opencamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.BORDER_REPLICATE;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class Main3Activity extends Activity {
    ArrayList<byte []> x = new ArrayList<>();
    private static final String TAG = "Main3Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Intent ss = getIntent();
        if (ss.getExtras() != null) {
            Log.d(TAG, "images passed to Main3Activity");
            x.add(ss.getByteArrayExtra("b1"));
            x.add(ss.getByteArrayExtra("b2"));
            x.add(ss.getByteArrayExtra("b3"));


            if (x.size()==3) {
                Log.d(TAG, "Main3Activity image size is 5");
                Mat mat1= new Mat();
                Mat mat2= new Mat();
                Mat mat3= new Mat();


                ArrayList<Bitmap> xx = new ArrayList<>();
                xx.add(BitmapFactory.decodeByteArray(x.get(0), 0, x.get(0).length));
                xx.add(BitmapFactory.decodeByteArray(x.get(1), 0, x.get(1).length));
                xx.add(BitmapFactory.decodeByteArray(x.get(2), 0, x.get(2).length));


                Bitmap bmp1 = xx.get(0).copy(Bitmap.Config.ARGB_8888, true);
                Bitmap bmp2 = xx.get(1).copy(Bitmap.Config.ARGB_8888, true);
                Bitmap bmp3 = xx.get(2).copy(Bitmap.Config.ARGB_8888, true);


                Utils.bitmapToMat(bmp1, mat1);
                Utils.bitmapToMat(bmp2, mat2);
                //8UC4, RGBA format
                Utils.bitmapToMat(bmp3, mat3);


                Imgproc.cvtColor(mat1,mat1,Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(mat2,mat2,Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(mat3,mat3,Imgproc.COLOR_RGBA2RGB);



                List<Mat> images = new ArrayList<Mat>();
                images.add(mat1);
                images.add(mat2);
                images.add(mat3);

               // Mat resultImage = exposureFusion(images, 1,1,1,3);

                //Bitmap bmp = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                //Utils.matToBitmap(resultImage, bmp);

                ImageView img1 = findViewById(R.id.i1);
                img1.setImageBitmap(bmp2);
                img1.setRotation(90);
                img1.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.setAdjustViewBounds(true);
                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);

            }

        }


    }

    public void next(View view) {
        Intent next = new Intent(this, Main4Activity.class);
        next.putExtra("b1", x.get(0));
        next.putExtra("b2", x.get(1));
        next.putExtra("b3", x.get(2));

        startActivity(next);
    }
}
