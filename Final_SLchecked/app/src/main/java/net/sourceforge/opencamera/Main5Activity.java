package net.sourceforge.opencamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.core.Core.BORDER_REPLICATE;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.core.Core.BORDER_REPLICATE;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_64FC3;
import static org.opencv.core.CvType.CV_8SC1;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;


public class Main5Activity extends Activity {

    ArrayList<byte []> x = new ArrayList<>();
    private static final String TAG = "Main5Activity";
    Bitmap k;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        Intent ss = getIntent();
        if (ss.getExtras() != null) {
            Log.d(TAG, "images passed to Main5Activity");
            x.add(ss.getByteArrayExtra("b1"));
            x.add(ss.getByteArrayExtra("b2"));
            x.add(ss.getByteArrayExtra("b3"));


            if (x.size()==3) {
                Log.d(TAG, "Main5Activity image size is 3");
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

                cvtColor(mat1,mat1,Imgproc.COLOR_RGBA2RGB);
                cvtColor(mat2,mat2,Imgproc.COLOR_RGBA2RGB);
                cvtColor(mat3,mat3,Imgproc.COLOR_RGBA2RGB);

                double alpha = 0.9;
                double beta = -40;
                double gamma = 0.9;
                // System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
                Mat source1 = mat3;
                Mat destination1 = new Mat(source1.rows(), source1.cols(), source1.type());
                //applying brightness enhacement
                source1.convertTo(destination1, -1, alpha, beta);
                //Bitmap bmpp = Bitmap.createBitmap(destination1.cols(), destination1.rows(), Bitmap.Config.ARGB_8888);
                //Utils.matToBitmap(destination1, bmpp);

                Mat lut = new Mat(1, 256, CvType.CV_8UC1);
                lut.setTo(new Scalar(0));
                for (int i = 0; i < 256; i++) {
                    lut.put(0, i, Math.pow((double)(1.0 * i/255), 1/gamma) * 255);
                }
                Core.LUT(destination1, lut, destination1);

                alpha = 1.1;
                beta = 40;
                gamma = 1.1;
                Mat source2 = mat1;
                Mat destination2 = new Mat(source2.rows(), source2.cols(), source2.type());
                // applying brightness enhacement
                source2.convertTo(destination2, -1, alpha, beta);
                Mat lut2 = new Mat(1, 256, CvType.CV_8UC1);
                lut2.setTo(new Scalar(0));
                for (int i = 0; i < 256; i++) {
                    lut2.put(0, i, Math.pow((double)(1.0 * i/255), 1/gamma) * 255);
                }
                Core.LUT(destination2, lut2, destination2);


                List<Mat> images = new ArrayList<Mat>();
                images.add(mat1);
                images.add(mat2);
                images.add(mat3);
                images.add(destination1);
                images.add(destination2);
                //AlignMTB alignMTB = createAlignMTB();
                //alignMTB.process(images, images);

                //Mat one = modify(mat1);
                //Mat two = modify(mat2);
                //Mat three = modify(mat3);
                //List<Mat> iimages = new ArrayList<Mat>();
                //iimages.add(one);
                //iimages.add(two);
                //iimages.add(three);


                //WEIGHT PARAMETERS FOR EXPOSURE FUSION
                double w_c = 1.0, w_s = 1.0, w_e = 1.0;
                int depth = 8;
                Mat rgbImg = images.get(1);
                int r = rgbImg.rows();
                int c = rgbImg.cols();
                Mat hsvImg = new Mat(r,c,CV_8UC4);
                cvtColor(rgbImg,hsvImg,Imgproc.COLOR_RGB2HSV);
                //mean value range 0.0 ~ 255.0
                double mean_hue = Core.mean(hsvImg).val[0];
                double mean_sat = Core.mean(hsvImg).val[1];
                double mean_val = Core.mean(hsvImg).val[2];
                w_s = Math.log(mean_sat) / Math.log(255.0);
                w_e = Math.log(mean_val) / Math.log(255.0);

                Mat edges = new Mat(r,c,CV_8UC4);
                Imgproc.Canny(rgbImg,edges,10,100);
                double e1 = Core.mean(edges).val[0];
                //depth = (int)Math.round(e1/12.0);

                // PROCESS EXPOSURE FUSION
                // w_c, w_s, w_e : weights for contrast, saturation, well-exposedness
                // depth : pyramid depth
                Mat resultImage = exposureFusion(images,w_c,w_s,w_e,depth);


                //Mat resultImage = exposureFusion(images, 1,1,1,8);



                //Mat dest = resultImage.clone();
                //Imgproc.cvtColor(resultImage, dest, Imgproc.COLOR_RGB2YUV);
                //List<Mat> temp = new ArrayList<Mat>();
                //Core.split(dest, temp);
                //Imgproc.equalizeHist(temp.get(0),temp.get(0));
                //Core.merge(temp, dest);
                //Imgproc.cvtColor(dest, dest, Imgproc.COLOR_YUV2RGB);

                Bitmap bmp = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultImage, bmp);

                //Bitmap bmpp = Bitmap.createBitmap(dest.cols(), dest.rows(), Bitmap.Config.ARGB_8888);
                //Utils.matToBitmap(dest, bmpp);

                ImageView img1 = findViewById(R.id.i1);
                img1.setImageBitmap(bmp);
                img1.setRotation(90);
                img1.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.setAdjustViewBounds(true);
                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                k = bmp;



            }

        }
    }

    Mat modify(Mat inputimg) {
        if (inputimg.rows() > 1000)
        {
            do
            {
                Size sz = new Size(inputimg.cols()*0.5, inputimg.rows()*0.5);
                if ((int)(inputimg.cols()*0.5) % 24 == 0 || (int)(inputimg.rows()*0.5) % 32 == 0)
                    sz = new Size(inputimg.cols()*0.5 + 1, inputimg.cols()*0.5 + 1);
                resize(inputimg, inputimg, sz);
            }while (inputimg.rows() > 1000);
        }
        Mat temp = new Mat(inputimg.size(), CV_8UC3);
        cvtColor(inputimg, temp, Imgproc.COLOR_BGR2Lab);

        Mat LadjustmentImage;
        ArrayList<Mat> Lab = new ArrayList<Mat>();
        ArrayList<Mat> correctedLab = new ArrayList<Mat>();
        ;
        split(temp, Lab);


        LadjustmentImage = ExposureAdjustment(Lab.get(0));
        Mat a = Lab.get(1).adjustROI(0, 0,LadjustmentImage.cols(), LadjustmentImage.rows());
        Mat b = Lab.get(2).adjustROI(0, 0,LadjustmentImage.cols(), LadjustmentImage.rows());

        correctedLab.add(LadjustmentImage);
        correctedLab.add(a);
        correctedLab.add(b);


        merge(correctedLab, temp);
        cvtColor(temp,temp,Imgproc.COLOR_Lab2RGB);

        return temp;
    }

    Mat ExposureAdjustment(Mat gimg) {
        Block[] imgblcks = ImageSegmentation(gimg);
        Mat exposureadjustmentimage = ExposureCorrection(imgblcks, gimg);
        return exposureadjustmentimage;

    }
    Block[] ImageSegmentation(Mat img) {
        int rows= img.rows();
        int cols= img.cols();


        int nrows = rows/32;
        int ncols = cols/24;
        int totalblcks = nrows*ncols;
        Block imgblcks[] = new Block[totalblcks];
        for (int i = 0; i < totalblcks; i++) {
            imgblcks[i] = new Block();
        }

        for (int nblck = 0; nblck < totalblcks; nblck++) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 24; x++) {
                    imgblcks[nblck].block[y][x] = (char) (img.get(32*(nblck/ncols)+ y, 24*(nblck%ncols)+x)[0]);
                }
            }
            imgblcks[nblck].num = nblck;
            imgblcks[nblck].adjustFlag = 3;
        }

        int[][] blckhist = new int[totalblcks][256];
        for (int nblck = 0; nblck < totalblcks; nblck++) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 24; x++) {
                    blckhist[nblck][imgblcks[nblck].block[y][x]]++;
                }
            }
        }

        int maxidx = 0, maxbin = 0;
        int blackCnt = 0, whiteCnt = 0;
        for (int nblck = 0; nblck < totalblcks; nblck++) {
            maxbin = 0;
            maxidx = 0;
            for (int i = 0; i < 256; i++) {
                if (blckhist[nblck][i] > maxbin) {
                    maxbin = blckhist[nblck][i];
                    maxidx = i;
                }
            }

            if (maxidx > 230) {
                imgblcks[nblck].adjustFlag = 1; //bright region
            }
            else if (maxidx < 30 ) {
                imgblcks[nblck].adjustFlag = 2; //dark region
            }
            imgblcks[nblck].intensity = maxidx;
        }

        Mat laplacianimg;
        double[] k = { 0.1667, 0.6667, 0.1667, 0.6667, -3.3333, 0.6667,	0.1667, 0.6667, 0.1667 };	// laplacian kernel
        int margin = 1;
        int suminfo = 0;
        int totalsuminfo = 0;
        float tempinfo = 0;
        int correctBlckCnt = 0;
        for (int nblck = 0; nblck < totalblcks; nblck++) {
            if (imgblcks[nblck].adjustFlag == 1 || imgblcks[nblck].adjustFlag == 2) {
                suminfo = 0;
                for (int y = margin; y < 32+margin-1; y++) {
                    for (int x = margin; x < 24+margin-1; x++) {
                        tempinfo = 0;
                        for (int r = -margin; r <= margin; r++) {
                            for (int c = -margin; c <= margin; c++) {
                                double[] data = img.get(32*(nblck / ncols) + y + r, 24*(nblck%ncols)+x+c);
                                if (data != null) {
                                    tempinfo += data[0]*k[(r + margin) * 3 + (c + margin)];
                                }
                                else {
                                    System.out.println("segment image data null");
                                    break;
                                }

                                //tempinfo += (img.get(32*(nblck / ncols) + y + r, 24*(nblck%ncols)+x+c)[0])*k[(r + margin) * 3 + (c + margin)];


                            }
                        }
                        suminfo += Math.abs(tempinfo);
                    }
                }
                imgblcks[nblck].informative = suminfo;
                totalsuminfo += suminfo;
                correctBlckCnt++;
            }
        }

        int nfrm = 0;
        nfrm++;
        double avginfo = (double) totalsuminfo / correctBlckCnt;
        for (int nblck = 0; nblck < totalblcks; nblck++) {
            if (imgblcks[nblck].adjustFlag ==1 || imgblcks[nblck].adjustFlag == 2) {
                if (imgblcks[nblck].informative > (int) avginfo) {
                    imgblcks[nblck].informativeFlag = 1;
                }
                else {
                    imgblcks[nblck].informativeFlag = 0;
                }
            }
        }

        return imgblcks;

    }

    public void save(View view) {
        if (k != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap re = Bitmap.createBitmap(k, 0, 0, k.getWidth(), k.getHeight(), matrix, true);
            saveImageToExternalStorage(re);
            Toast.makeText(getApplicationContext(), "Saved successfully, Check gallery", Toast.LENGTH_SHORT).show();
        }

    }

    Mat ExposureCorrection(Block[] imgblcks, Mat srcimg) {
        int rows = srcimg.rows();
        int cols = srcimg.cols();

        double[] DWF = { 1,	0.951229424500714,	0.904837418035960,	0.860707976425058,	0.818730753077982,	0.778800783071405,	0.740818220681718,	0.704688089718713	,0.670320046035639	,0.637628151621773	,0.606530659712633	,0.576949810380487	,0.548811636094027	,0.522045776761016	,0.496585303791410	,0.472366552741015	,0.449328964117222	,0.427414931948727	,0.406569659740599	,0.386741023454501	,0.367879441171442	,0.349937749111155	,0.332871083698080	,0.316636769379053	,0.301194211912202	,0.286504796860190	,0.272531793034013	,0.259240260645892	,0.246596963941607	,0.234570288093798	,0.223130160148430	,0.212247973826743	,0.201896517994655	,0.192049908620754	,0.182683524052735	,0.173773943450445	,0.165298888221587	,0.157237166313628	,0.149568619222635	,0.142274071586514	,0.135335283236613	,0.128734903587804	,0.122456428252982	,0.116484157773497	,0.110803158362334	,0.105399224561864	,0.100258843722804	,0.0953691622155496	,0.0907179532894125	,0.0862935864993705	,0.0820849986238988	,0.0780816660011532	,0.0742735782143339	,0.0706512130604296	,0.0672055127397498	,0.0639278612067076	,0.0608100626252180	,0.0578443208748385	,0.0550232200564072	,0.0523397059484324	,0.0497870683678639	,0.0473589243911409	,0.0450492023935578	,0.0428521268670402	,0.0407622039783662	,0.0387742078317220	,0.0368831674012400	,0.0350843541008450	,0.0333732699603261	,0.0317456363780679	,0.0301973834223185	,0.0287246396542394	,0.0273237224472926	,0.0259911287787553	,0.0247235264703394	,0.0235177458560091	,0.0223707718561656	,0.0212797364383772	,0.0202419114458044	,
                0.0192547017753869	,0.0183156388887342	,0.0174223746394935	,0.0165726754017613	,0.0157644164848545	,0.0149955768204777	,0.0142642339089993	,0.0135685590122009	,0.0129068125804799	,0.0122773399030684	,0.0116785669703954	,0.0111089965382423	,0.0105672043838527	,0.0100518357446336	,0.00956160193054351	,0.00909527710169582	,0.00865169520312063	,0.00822974704902003	,0.00782837754922577	,0.00744658307092434	,0.00708340892905212	,0.00673794699908547 };

        int BLOCKROWS = 32;
        int BLOCKCOLS = 24;

        int nrows = rows / 32;
        int ncols = cols / 24;

        int totalblcks = nrows*ncols;
        Block[] tempblcks = new Block[totalblcks];
        for (int i = 0; i < totalblcks; i++) {
            tempblcks[i] = new Block();
        }

        rows = nrows * 32;
        cols = ncols * 24;

        Mat correctedimg = new Mat(cols, rows, CV_8UC1);
        Mat noncorrectedimg = new Mat(cols, rows, CV_8UC1);
        Mat tempimg = new Mat(cols, rows, CV_8UC1);

        double blckpix = 0;
        double currentpix = 0;
        double pix = 0;
        double w = 0;
        double ws = 0;
        double ed = 0;
        double sigma = 20.0;
        double tpix = 0;
        int r0 = 0;
        int c0 = 0;
        int rend = 0;
        int cend = 0;
        int bcenterx = 0;
        int bcentery = 0;
        int currentx = 0;
        int currenty = 0;
        int dbflag = 0;
        int blckidx = 0;
        double gamma;

        int maxinten = 0;
        int maxinfo = 0;

        for (int nblck = 0; nblck < totalblcks; nblck++)
        {
            if (imgblcks[nblck].adjustFlag == 1)
            {
                if (imgblcks[nblck].intensity > maxinten)
                    maxinten = imgblcks[nblck].intensity;
            }

            if (imgblcks[nblck].informativeFlag == 1)
            {
                if (imgblcks[nblck].informative > maxinfo)
                    maxinfo = imgblcks[nblck].informative;
            }
        }

        maxinten = 255;

        for (int nblck = 0; nblck < totalblcks; nblck++)
        {
            if (nblck / ncols == 0)	// ∞°¿Â ¿≠¡Ÿ
            {
                r0 = 0; rend = 2;
                if (nblck%ncols == 0)
                {
                    c0 = 0; cend = 2;
                }
                else if (nblck%ncols == 1)
                {
                    c0 = -1; cend = 2;
                    dbflag = 2;
                }
                else if (nblck%ncols == ncols - 1)
                {
                    c0 = -2; cend = 0;
                }
                else if (nblck%ncols == ncols - 2)
                {
                    c0 = -2; cend = 1;
                }
                else
                {
                    c0 = -2; cend = 2;
                }

            }
            else if (nblck / ncols == 1)	// ¿ßø°º≠ µŒπ¯¬∞ ¡Ÿ
            {
                r0 = -1; rend = 2;
                if (nblck%ncols == 0)
                {
                    c0 = 0; cend = 2;
                }
                else if (nblck%ncols == 1)
                {
                    c0 = -1; cend = 2;
                }
                else if (nblck%ncols == ncols - 1)
                {
                    c0 = -2; cend = 0;
                }
                else if (nblck%ncols == ncols - 2)
                {
                    c0 = -2; cend = 1;
                }
                else
                {
                    c0 = -2; cend = 2;
                }
            }
            else if (nblck / ncols == nrows - 1)	// ∞°¿Â æ∆∑ß ¡Ÿ
            {
                r0 = -2; rend = 0;
                if (nblck%ncols == 0)
                {
                    c0 = 0; cend = 2;
                }
                else if (nblck%ncols == 1)
                {
                    c0 = -1; cend = 2;
                }
                else if (nblck%ncols == ncols - 1)
                {
                    c0 = -2; cend = 0;
                }
                else if (nblck%ncols == ncols - 2)
                {
                    c0 = -2; cend = 1;
                }
                else
                {
                    c0 = -2; cend = 2;
                }

            }
            else if (nblck / ncols == nrows - 2)	// æ∆∑°ø°º≠ µŒπ¯¬∞ ¡Ÿ
            {
                r0 = -2; rend = 1;
                if (nblck%ncols == 0)
                {
                    c0 = 0; cend = 2;
                }
                else if (nblck%ncols == 1)
                {
                    c0 = -1; cend = 2;
                }
                else if (nblck%ncols == ncols - 1)
                {
                    c0 = -2; cend = 0;
                }
                else if (nblck%ncols == ncols - 2)
                {
                    c0 = -2; cend = 1;
                }
                else
                {
                    c0 = -2; cend = 2;
                }
            }
            else if (nblck%ncols == 0)	 // ∞°¿Â øﬁ¬  ¡Ÿ
            {
                c0 = 0; cend = 2;
                if (nblck / ncols == 0)
                {
                    r0 = 0; rend = 2;
                }
                else if (nblck / ncols == 1)
                {
                    r0 = -1; rend = 2;
                }
                else if (nblck / ncols == nrows - 1)
                {
                    r0 = -2; rend = 0;
                }
                else if (nblck / ncols == nrows - 2)
                {
                    r0 = -2; rend = 1;
                }
                else
                {
                    r0 = -2; rend = 2;
                }
            }
            else if (nblck%ncols == 1)   // øﬁ¬ ø°º≠ µŒπ¯¬∞ ¡Ÿ
            {
                c0 = -1; cend = 2;
                if (nblck / ncols == 0)
                {
                    r0 = 0; rend = 2;
                }
                else if (nblck / ncols == 1)
                {
                    r0 = -1; rend = 2;
                }
                else if (nblck / ncols == nrows - 1)
                {
                    r0 = -2; rend = 0;
                }
                else if (nblck / ncols == nrows - 2)
                {
                    r0 = -2; rend = 1;
                }
                else
                {
                    r0 = -2; rend = 2;
                }
            }
            else if (nblck%ncols == ncols - 1)	// ∞°¿Â ø¿∏•¬  ¡Ÿ
            {
                c0 = -2; cend = 0;
                if (nblck / ncols == 0)
                {
                    r0 = 0; rend = 2;
                }
                else if (nblck / ncols == 1)
                {
                    r0 = -1; rend = 2;
                }
                else if (nblck / ncols == nrows - 1)
                {
                    r0 = -2; rend = 0;
                }
                else if (nblck / ncols == nrows - 2)
                {
                    r0 = -2; rend = 1;
                }
                else
                {
                    r0 = -2; rend = 2;
                }
            }
            else if (nblck%ncols == ncols - 2)	// ø¿∏•¬ ø°º≠ µŒπ¯¬∞ ¡Ÿ
            {
                c0 = -2; cend = 1;
                if (nblck / ncols == 0)
                {
                    r0 = 0; rend = 2;
                }
                else if (nblck / ncols == 1)
                {
                    r0 = -1; rend = 2;
                }
                else if (nblck / ncols == nrows - 1)
                {
                    r0 = -2; rend = 0;
                }
                else if (nblck / ncols == nrows - 2)
                {
                    r0 = -2; rend = 1;
                }
                else
                {
                    r0 = -2; rend = 2;
                }
            }
            else	// ±◊ ¿Ãø‹¿« ∫Ì∑œ
            {
                r0 = -2; rend = 2;
                c0 = -2; cend = 2;
            }

            // 1110 ¿Ã ∫Œ∫– √ﬂ∞°
            if (imgblcks[nblck].adjustFlag == 0)
            {
                for (int y = 0; y < 32; y++)
                    for (int x = 0; x < 24; x++)
                        correctedimg.get((nblck / ncols) * 32 + y, (nblck % ncols) * 24 + x)[0] = imgblcks[nblck].block[y][x];
            }
            else
            {
                for (int y = 0; y<32; y++)
                {
                    for (int x = 0; x<24; x++)
                    {
                        blckpix = 0;
                        w = 0;
                        ws = 0;
                        ed = 0;
                        blckidx = 0;

                        currentx = (nblck % ncols)*BLOCKCOLS + x;
                        currenty = (nblck / ncols)*BLOCKROWS + y;

                        for (int r = r0; r <= rend; r++)
                        {
                            for (int c = c0; c <= cend; c++)
                            {

                                blckidx = nblck + r*ncols + c;


                                // ∫Ì∑œ¿« π‡±‚∞™∏∏ ∞Ì∑¡
							/*gamma = 1.0;
							if (imgblcks[blckidx].adjustFlag == 1)
								gamma = 2.2;
							else if (imgblcks[blckidx].adjustFlag == 2)
								gamma = 0.4;*/

                                // ∫Ì∑œ π‡±‚∞™ + ¡§∫∏∑Æ ∞Ì∑¡
                                gamma = 1.0;
                                if (imgblcks[blckidx].adjustFlag == 1)		// π‡¿∏∏Èº≠ ≥Ù¿∫ ¡§∫∏∑Æ¿ª ∞Æ¥¬ ∫Ì∑œ
                                {
                                    gamma = 2.2;
                                    if(imgblcks[blckidx].informativeFlag == 1)
                                        gamma = 1.0 + ((double)imgblcks[blckidx].intensity / maxinten * 0.75) + ((double)imgblcks[blckidx].informative / maxinfo * 0.75);  	// √÷º“ 2.2¿ÃªÛ √÷¥Î 3.0¿Ã«œ
                                }
                                else if (imgblcks[blckidx].adjustFlag == 2)	// æÓµŒøÏ∏Èº≠ ≥Ù¿∫ ¡§∫∏∑Æ¿ª ∞Æ¥¬ ∫Ì∑œ
                                {
                                    gamma = 0.4;
                                    if(imgblcks[blckidx].informativeFlag == 1)
                                        gamma = 1.0 + (((double)255 - imgblcks[blckidx].intensity) / maxinten * -0.4) + ((double)imgblcks[blckidx].informative / maxinfo * -0.4);	// √÷º“ 0.4¿ÃªÛ √÷¥Î 0.1¿Ã«œ
                                }

                                bcenterx = (blckidx % ncols)*BLOCKCOLS + BLOCKCOLS / 2 - 1;
                                bcentery = (blckidx / ncols)*BLOCKROWS + BLOCKROWS / 2 - 1;
							/*currentx = (nblck % ncols)*BLOCKCOLS + x;
							currenty = (nblck / ncols)*BLOCKROWS + y;*/
                                ed = Math.sqrt((double)((bcenterx - currentx)*(bcenterx - currentx) + (bcentery - currenty)*(bcentery - currenty)));
                                w = DWF[(int)ed];

                                pix = 255. * Math.pow((imgblcks[nblck].block[y][x]) / 255., gamma);

                                blckpix += pix * w;
                                ws += w;
                            }
                        }

                        tpix = blckpix / ws;
                        if (correctedimg != null) {
                            System.out.println("img not null");
                        }
                        double[] data = correctedimg.get((nblck / ncols) * BLOCKROWS + y, (nblck % ncols) * BLOCKCOLS + x);
                        if (data != null) {
                            int ch = correctedimg.channels();
                            for (int j = 0; j < ch; j++) {

                                data[j] = (char) tpix;

                            }
                            correctedimg.put((nblck / ncols) * BLOCKROWS + y, (nblck % ncols) * BLOCKCOLS + x, data);
                        }
                        else {
                            System.out.println("data is null");
                            break;
                        }

                        //correctedimg.get((nblck / ncols) * BLOCKROWS + y, (nblck % ncols) * BLOCKCOLS + x)[0] = (char)tpix;

                    }
                }

            }
        }

        return srcimg;
    }

    private void saveImageToExternalStorage(Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images_1");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }



    private Mat exposureFusion (List<Mat> images, double w_c, double w_s, double w_e, int depth)
    {
        if(images.size()<2)
        {
            //print("Input has to be a list of at least two images");
            return null;
        }

        Size size = images.get(0).size();
        for(int i = 1; i < images.size(); i++)
        {
            if(!images.get(i).size().equals(size))
            {
                return null;
            }
        }
        int r = (int) size.height;
        int c = (int) size.width;
        int N = images.size();



        // COMPUTE WEIGHT MAPS
        // weights are 32FC1 (0.0~1.0)
        List<Mat> weights = computeWeights(images,w_c,w_s,w_e);
        //return weights.get(3);

        List<List<Mat>> gps = new ArrayList<List<Mat>>();
        List<List<Mat>> lps = new ArrayList<List<Mat>>();
        List<Mat> pyramid = new ArrayList<Mat>();
        for(int i=0;i<N;i++)
        {
            //weights are 32FC1 (21)
            gps.add(gaussianPyramid(weights.get(i),depth));
            //images are 8UC3 (16)
            lps.add(laplacianPyramid(images.get(i),depth));
        }

        //System.out.println(lps.get(2).get(3).size());
        //return gps.get(2).get(7);
        //return lps.get(2).get(0);
        //아무리 봐도 여기까진 맞는 것 같음;;


        //setting up the final pyramid.
        for(int i=0;i<depth;i++){
            pyramid.add(new Mat(gps.get(0).get(i).size(), CV_64FC3, new Scalar(0)));
        }

        for(int i=0;i<N;i++)
        {
            for(int lev = 0 ;lev<depth; lev++){
                Mat gpsFloat = gps.get(i).get(lev).clone();
                List<Mat> gpList = new ArrayList<Mat>();
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                Mat gp = new Mat(gpsFloat.size(),CV_64FC3);
                Core.merge(gpList,gp);

                Mat lp = lps.get(i).get(lev);
                ////32FC3
                //System.out.println("Before convertTo lp type is "+lp.type());
                // I have to convert it because matlab uses values between 0~1. The gaussian pyramid is already between 0~1.
                lp.convertTo(lp,CV_64FC3, (1.0/255.0));
                //32FC3
                //System.out.println("After convertTo lp type is "+lp.type());
                Mat lp_gp = new Mat(lp.size(),CV_64FC3);
                Core.multiply(gp,lp,lp_gp);
                Core.add(pyramid.get(lev), lp_gp, pyramid.get(lev));
            }
        }

        //return pyramid.get(6);




        // Pyramid is now from 0 to 1!!



        // COLLAPSE PYRAMID
        System.out.println("pyramid's top size is: "+pyramid.get(depth-1).size());
        Mat resultImage = pyramid.get(depth-1).clone();

        for(int i=depth-2;i>=0;i--)
        {
            //Imgproc.pyrUp(resultImage, resultImage, new Size(pyramid.get(i).width(), pyramid.get(i).height()));
            //Core.add(resultImage,pyramid.get(i),resultImage);
            //image_expand(resultImage, new Size(pyramid.get(i).width(), pyramid.get(i).height()));
            System.out.println("resultImage size is "+resultImage.size());
            Point odd = new Point(2*resultImage.size().height - pyramid.get(i).size().height, 2*resultImage.size().width - pyramid.get(i).size().width);

            /*
            if(i==depth-3) {
                System.out.println("Returned Early. resultImage type is "+resultImage.type());
                //Correct
                //return resultImage;
                //Suddenly really wrong. resultImage range is currently 0~1, 32FC3.
                return image_expand(resultImage.clone(), odd, true);
            }
            */
            Core.add(image_expand(resultImage.clone(), odd, true), pyramid.get(i), resultImage);
        }

        System.out.println("About final image :"+resultImage.size()+" "+resultImage.type());

        resultImage.convertTo(resultImage, CV_8UC3, 255);
        //return gps.get(2).get(0);
        return resultImage;



        /*
        //For debugging!
        //This pyramid is 8UC3 as a whole.
        Mat clone = images.get(0).clone();
        Mat temp = new Mat();
        Mat J = clone;
        for(int i=0;i<=0;i++)
        {
            clone = image_reduce(J);
            Point odd = new Point(clone.size().height*2 - J.height(), clone.size().width*2 - J.width());
            System.out.println("clone type is "+clone.type()+", J type is " + J.type());
            System.out.println("clone size is "+clone.size()+", J size is " + J.size());
            System.out.println("J.type is "+J.type());
            System.out.println("image_expand(clone, odd) is "+image_expand(clone, odd).type());
            Core.subtract(J, image_expand(clone, odd), temp);
            System.out.println("added size "+temp.size());
            J = clone;
        }
        System.out.println("added to the front has size "+J.size());
        return temp;
        */
    }

    private Mat getGaussianKernel1D(){
        double[] kernel = new double[5];
        kernel[0]= 0.0625f;
        kernel[1]=0.25f;
        kernel[2]=0.375f;
        kernel[3]=0.25f;
        kernel[4]=0.0625f;
        Mat mat = new Mat(5,1,CV_64FC1);
        mat.put(0,0,kernel);
        return mat;
    }

    private Mat getGaussianKernel(){
        return getGaussianKernel1D();
    }

    private Mat image_reduce(Mat image){
        if(image.type() == CV_8UC3){
            image.convertTo(image, CV_64FC3);
        }
        Mat kernelX = getGaussianKernel();
        Mat kernelY = new Mat();
        Core.transpose(kernelX, kernelY);
        //weight map일 때는 32FC1, laplacian pyramid일 때는 32FC3
        //System.out.println("image type is "+ image.type());
        Mat clone = image.clone();

        Mat result;
        //If this is for making the laplacian pyramid
        if(clone.type() == CV_64FC3) {
            result = new Mat(image.size(), CV_64FC3);
            Mat resultR = new Mat(image.size(), CV_64FC1);
            Mat resultG = new Mat(image.size(), CV_64FC1);
            Mat resultB = new Mat(image.size(), CV_64FC1);
            List<Mat> cloneRGB = new ArrayList<Mat>();
            Core.split(clone, cloneRGB);
            Imgproc.sepFilter2D(cloneRGB.get(0), resultR, -1, kernelX, kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
            Imgproc.sepFilter2D(cloneRGB.get(1), resultG, -1, kernelX, kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
            Imgproc.sepFilter2D(cloneRGB.get(2), resultB, -1, kernelX, kernelY, new Point(-1,-1), 0, BORDER_REFLECT);

            cloneRGB = new ArrayList<Mat>();
            cloneRGB.add(resultR);
            cloneRGB.add(resultG);
            cloneRGB.add(resultB);

            Core.merge(cloneRGB, result);
            //System.out.println("2. result type is "+result.type());
            Imgproc.resize(result, result, new Size((image.width()/2 + ((image.width()%2==1)?1:0)),(image.height()/2) + ((image.height()%2==1)?1:0)),0, 0, Imgproc.INTER_NEAREST);
        }
        //If this is for making the weight map's gaussian pyramid.
        else {
            result = new Mat(image.size(), CV_64FC1);
            Imgproc.sepFilter2D(clone, result, -1, kernelX, kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
            //System.out.println("2. result type is "+result.type());
            Imgproc.resize(result, result, new Size((image.width()/2 + ((image.width()%2==1)?1:0)),(image.height()/2) + ((image.height()%2==1)?1:0)),0, 0, Imgproc.INTER_NEAREST);
        }

        //System.out.println("1. result type is "+result.type());
        //Imgproc.filter2D(clone, result, -1 ,kernel);
        //System.out.println("3. result type is "+result.type()); //It's 21 (32FC3)
        //System.out.println("result size is "+result.size());
        return result;
    }

    private Mat image_expand(Mat image, Point odd, boolean between01){
        Mat imageClone = image.clone();
        copyMakeBorder(imageClone, imageClone, 1, 1, 1, 1, BORDER_REPLICATE);
        Mat kernelX = getGaussianKernel();
        Mat kernelY = new Mat();
        Core.transpose(kernelX, kernelY);

        Mat UIntVer = new Mat(imageClone.size(), CV_8UC3);

        if(between01==false)
            imageClone.convertTo(UIntVer, CV_8UC3);
        else
            imageClone.convertTo(UIntVer, CV_8UC3, 255);

        Imgproc.resize(UIntVer, UIntVer, new Size(imageClone.width()*2, imageClone.height()*2), 0, 0, Imgproc.INTER_NEAREST);

        //System.out.println("UIntVer type: "+UIntVer.type()); (16)

        Mat mask = new Mat(2,2, CV_8UC1);

        int[][] array = new int[2][2];
        array[0][0] = 255;
        array[1][0] = 0;
        array[0][1] = 0;
        array[1][1] = 0;
        for (int i=0; i<2; i++) {
            for (int j = 0; j < 2; j++) {
                mask.put(i, j, array[i][j]);
            }
        }
        //mask becomes twice the size of image Mat
        Mat biggerMask = new Mat();
        Core.repeat(mask, imageClone.height(), imageClone.width(), biggerMask);
        //UIntVer SHOULD be image size * 2;
        //System.out.println("UIntVer row: "+UIntVer.rows() + "\ncol: "+UIntVer.cols());
        //System.out.println("biggerMask row: "+biggerMask.rows() + "\ncol: "+biggerMask.cols());

        List<Mat> rgbUIntVer = new ArrayList<Mat>();
        //System.out.println("UIntVer type: "+UIntVer.type());
        Core.split(UIntVer,rgbUIntVer);


        //System.out.println("biggerMask size "+biggerMask.size()+". BiggerMask type: "+biggerMask.type());
        //System.out.println("rgbUIntVer size "+rgbUIntVer.get(0).size()+". rgbUIntVer type: "+rgbUIntVer.get(0).type());
        Core.bitwise_and(rgbUIntVer.get(0), biggerMask, rgbUIntVer.get(0));
        Core.bitwise_and(rgbUIntVer.get(1), biggerMask, rgbUIntVer.get(1));
        Core.bitwise_and(rgbUIntVer.get(2), biggerMask, rgbUIntVer.get(2));
        //Core.bitwise_and(UIntVer, biggerMask, UIntVer);
        Core.merge(rgbUIntVer, UIntVer);


        int r = imageClone.height()*2;
        int c = imageClone.width()*2;
        Mat result = new Mat(r, c, CV_64FC3);
        Mat resultR = new Mat(r, c ,CV_64FC1);
        Mat resultG = new Mat(r, c, CV_64FC1);
        Mat resultB = new Mat(r, c, CV_64FC1);
        UIntVer.convertTo(UIntVer, CV_64FC3);

        Scalar four = new Scalar(4);
        //Core.multiply(UIntVer, four, UIntVer);

        Core.split(UIntVer, rgbUIntVer);

        Core.multiply(rgbUIntVer.get(0), four, rgbUIntVer.get(0));
        Core.multiply(rgbUIntVer.get(1), four, rgbUIntVer.get(1));
        Core.multiply(rgbUIntVer.get(2), four, rgbUIntVer.get(2));

        //System.out.println("size: "+rgbUIntVer.size()); //Size is always 3.

        Imgproc.sepFilter2D(rgbUIntVer.get(0),resultR,-1,kernelX,kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
        Imgproc.sepFilter2D(rgbUIntVer.get(1),resultG,-1,kernelX,kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
        Imgproc.sepFilter2D(rgbUIntVer.get(2),resultB,-1,kernelX,kernelY, new Point(-1,-1), 0, BORDER_REFLECT);
        List<Mat> finalImageRGB = new ArrayList<Mat>();
        finalImageRGB.add(resultR);
        finalImageRGB.add(resultG);
        finalImageRGB.add(resultB);
        Core.merge(finalImageRGB, result);


        //Imgproc.sepFilter2D(UIntVer, result, -1, kernelX, kernelY, new Point(-1, -1), 0, BORDER_DEFAULT);


        System.out.println(r-4-(int)odd.x);
        System.out.println(c-4-(int)odd.y);
        Rect roi = new Rect(2, 2, c-4-(int)odd.y, r-4-(int)odd.x);
        System.out.println("result size is "+result.size());
        result = new Mat(result, roi);
        System.out.println("result size is "+result.size());
        System.out.println("result type is "+result.type());

        //Added this 11/11. For reconstructing laplacian pyramid at the end.
        if(between01 == true)
            result.convertTo(result, CV_64FC3, (1.0/255.0));

        return result;


        //return rgbUIntVer.get(2);
    }


    private List<Mat> gaussianPyramid(Mat mat,int depth)
    {
        Mat copy = mat.clone();
        //나중에 라플라시안 피라미드 만들 때를 대비해서. 일단 weightmap일 때는 mat type가 32FC1임!
        if(mat.type() == CV_8UC3)
            copy.convertTo(copy, CV_64FC3);
        List<Mat> pyramid = new ArrayList<Mat>();
        pyramid.add(copy);
        for(int i = 1;i<depth;i++)
        {
            //Mat m = new Mat((mat.rows() + 1) / 2, (mat.cols() + 1) / 2, mat.type());
            //Imgproc.pyrDown(mat, m);
            copy = image_reduce(copy);
            System.out.println("size of copy is "+copy.size());
            pyramid.add(copy);
            //mat = m;
        }
        return pyramid;
    }

    private List<Mat> laplacianPyramid(Mat mat,int depth)
    {
        //mat is 8UC3 (16). ALWAYS!
        List<Mat> pyramid = new ArrayList<Mat>();
        //This pyramid is 8UC3 as a whole.

        Mat clone = mat.clone();
        Mat J = clone;

        for(int i=0;i<=depth-2;i++)
        {
            clone = image_reduce(J);
            Mat temp = new Mat();
            Point odd = new Point(clone.size().height*2 - J.height(), clone.size().width*2 - J.width());
            System.out.println("clone type is "+clone.type()+", J type is " + J.type());
            System.out.println("clone size is "+clone.size()+", J size is " + J.size());
            System.out.println("J.type is "+J.type());
            System.out.println("image_expand(clone, odd) is "+image_expand(clone, odd, false).type());
            Core.subtract(J, image_expand(clone, odd, false), temp);
            System.out.println("added size "+temp.size());
            pyramid.add(temp);
            J = clone;
        }
        System.out.println("added to the front has size "+J.size());
        pyramid.add(J);
        return pyramid;
    }


    private List<Mat> computeWeights (List<Mat> images, double w_c, double w_s, double w_e) {

        int r = images.get(0).rows();
        int c = images.get(0).cols();

        List<Mat> weights = new ArrayList<Mat>();

        for(int i = 0; i < images.size(); i++)
        {
            Mat img = images.get(i).clone();    //8UC3 right now

            Mat grayScale = new Mat(r,c,CV_8UC1);
            Imgproc.cvtColor(img,grayScale,Imgproc.COLOR_RGB2GRAY);

            Mat weight = Mat.ones(r,c,CV_64FC1);

            //CONTRAST
            Mat imgGray = new Mat(r,c,CV_64FC1); //Float version
            grayScale.convertTo(imgGray, CV_64FC1, (1.0/255.0));
            //compare this to matlab!
            //if(i==1)
            //printMatrix(imgGray); // Looks ok actually

            Mat imgLaplacian = new Mat(r,c,CV_64FC1);
            //Mat laplacianFilter = getLaplacianFilter();
            //Imgproc.filter2D(imgGray, imgLaplacian, -1, laplacianFilter, new Point(-1, -1), 0, BORDER_REPLICATE);
            Imgproc.Laplacian(imgGray,imgLaplacian, CV_64FC1, 1, 1, 0, BORDER_REPLICATE);
            Mat contrastWeight = new Mat(r,c,CV_64FC1);
            Core.absdiff(imgLaplacian,new Scalar(0),contrastWeight);
            Core.pow(contrastWeight,w_c,contrastWeight);
            Core.multiply(weight,contrastWeight,weight);

            //SATURATION
            Mat imgFloat = new Mat(r,c,CV_64FC3);
            img.convertTo(imgFloat,CV_64FC3,(1.0/255.0));
            Mat saturationWeight = new Mat(r,c,CV_64FC1);
            List<Mat> rgbChannels = new ArrayList<Mat>();
            Core.split(imgFloat,rgbChannels);
            Mat R = rgbChannels.get(0);
            Mat G = rgbChannels.get(1);
            Mat B = rgbChannels.get(2);
            Mat mean = Mat.zeros(r,c,CV_64FC1);
            Core.add(mean,R,mean);
            Core.add(mean,G,mean);
            Core.add(mean,B,mean);
            Core.divide(mean,new Scalar(3),mean);
            Core.subtract(R,mean,R);
            Core.subtract(G,mean,G);
            Core.subtract(B,mean,B);
            Core.pow(R,2,R);
            Core.pow(G,2,G);
            Core.pow(B,2,B);
            Mat std = Mat.zeros(r,c,CV_64FC1);
            Core.add(std,R,std);
            Core.add(std,G,std);
            Core.add(std,B,std);
            Core.divide(std,new Scalar(3),std);
            Core.pow(std,0.5,std);
            Core.pow(std, w_s, saturationWeight);
            Core.multiply(weight,saturationWeight,weight);
            //WELL-EXPOSEDNESS

            double sigma = 0.2;
            Mat gaussianCurve = imgFloat.clone();
            List<Mat> gaussianCurveRGB = new ArrayList<Mat>();
            Core.split(gaussianCurve, gaussianCurveRGB);

            Core.subtract(gaussianCurveRGB.get(0), new Scalar(0.5),gaussianCurveRGB.get(0));
            Core.subtract(gaussianCurveRGB.get(1), new Scalar(0.5),gaussianCurveRGB.get(1));
            Core.subtract(gaussianCurveRGB.get(2), new Scalar(0.5),gaussianCurveRGB.get(2));

            Core.pow(gaussianCurveRGB.get(0),2, gaussianCurveRGB.get(0));
            Core.pow(gaussianCurveRGB.get(1),2, gaussianCurveRGB.get(1));
            Core.pow(gaussianCurveRGB.get(2),2, gaussianCurveRGB.get(2));

            Core.divide(gaussianCurveRGB.get(0),new Scalar(-2 * sigma * sigma),gaussianCurveRGB.get(0));
            Core.divide(gaussianCurveRGB.get(1),new Scalar(-2 * sigma * sigma),gaussianCurveRGB.get(1));
            Core.divide(gaussianCurveRGB.get(2),new Scalar(-2 * sigma * sigma),gaussianCurveRGB.get(2));

            Core.exp(gaussianCurveRGB.get(0), gaussianCurveRGB.get(0));
            Core.exp(gaussianCurveRGB.get(1), gaussianCurveRGB.get(1));
            Core.exp(gaussianCurveRGB.get(2), gaussianCurveRGB.get(2));

            /*
            Mat exposednessWeight = Mat.ones(r,c,CV_64FC1);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(0),exposednessWeight);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(1),exposednessWeight);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(2),exposednessWeight);
            Core.pow(exposednessWeight, w_e, exposednessWeight);
            Core.multiply(weight,exposednessWeight,weight);
            */

            //JND based saliency weight
            //grayScale is 0 to 255
            //i parameter is just for debugging. Delete it afterwards.
            Mat result = getJND(grayScale, i);
            System.out.println("result type is "+result.type());
            Core.multiply(result,new Scalar(10),result);
            Core.divide(result, new Scalar(255), result);
            Core.multiply(weight, result, weight);

            weights.add(weight);

        }

        for(int i=0;i<weights.size();i++)
        {
            Core.add(weights.get(i), new Scalar(1e-12), weights.get(i));
        }


        Mat weightsSum = new Mat(weights.get(1).size(), CV_64FC1, new Scalar(0));
        for(int i=0;i<weights.size();i++)
        {
            Core.add(weights.get(i), weightsSum, weightsSum);
        }

        for(int i = 0; i<weights.size(); i++)
        {
            Core.divide(weights.get(i),weightsSum,weights.get(i));
        }

        //printMatrix(weights.get(1));

        //return weights.get(1);
        return weights;
    }

    //From 0 to 255
    private Mat getFilterForI_bar(){
        byte[][] temp = {{1,1,1,1,1},{1,2,2,2,1},{1,2,0,2,1},{1,2,2,2,1},{1,1,1,1,1}};
        Mat filter = new Mat(5,5,CV_8UC1);
        for(int i=0;i<5;i++){
            filter.put(i,0,temp[i]);
        }
        //System.out.println("filterForI_bar is ");printMatrix(filter);
        return filter;
    }

    private Mat func_Gm(Mat image){
        Mat filter1 = new Mat(5,5,CV_8SC1);
        byte[][] G1 = {{0,0,0,0,0},{1,3,8,3,1},{0,0,0,0,0},{-1,-3,-8,-3,-1},{0,0,0,0,0}};
        for(int i=0;i<5;i++){
            filter1.put(i,0,G1[i]);
        }
        //System.out.println("filter1 is ");
        //printMatrix(filter1);
        Mat filter2 = new Mat(5,5,CV_8SC1);
        byte[][] G2 = {{0,0,1,0,0},{0,8,3,0,0},{1,3,0,-3,-1},{0,0,-3,-8,0},{0,0,-1,0,0}};
        for(int i=0;i<5;i++){
            filter2.put(i,0,G2[i]);
        }
        Mat filter3 = new Mat(5,5,CV_8SC1);
        byte[][] G3 = {{0,0,1,0,0},{0,0,3,8,0},{-1,-3,0,3,1},{0,-8,-3,0,0},{0,0,-1,0,0}};
        for(int i=0;i<5;i++){
            filter3.put(i,0,G3[i]);
        }
        Mat filter4 = new Mat(5,5,CV_8SC1);
        byte[][] G4 = {{0,1,0,-1,0},{0,3,0,-3,0},{0,8,0,-8,0},{0,3,0,-3,0},{0,1,0,-1,0}};
        for(int i=0;i<5;i++){
            filter4.put(i,0,G4[i]);
        }
        Mat result1 = image.clone();
        Mat result2 = image.clone();
        Mat result3 = image.clone();
        Mat result4 = image.clone();
        //Also fixed here!
        Imgproc.filter2D(image, result1, -1, filter1, new Point(-1,-1),0, BORDER_CONSTANT);
        //Core.divide(result1, new Scalar(16), result1);
        Core.absdiff(result1, new Scalar(0), result1);
        Imgproc.filter2D(image, result2, -1, filter2, new Point(-1,-1),0, BORDER_CONSTANT);
        //Core.divide(result2, new Scalar(16), result2);
        Core.absdiff(result2, new Scalar(0), result2);
        Imgproc.filter2D(image, result3, -1, filter3, new Point(-1,-1),0, BORDER_CONSTANT);
        //Core.divide(result3, new Scalar(16), result3);
        Core.absdiff(result3, new Scalar(0), result3);
        Imgproc.filter2D(image, result4, -1, filter4, new Point(-1,-1),0, BORDER_CONSTANT);
        //Core.divide(result4, new Scalar(16), result4);
        Core.absdiff(result4, new Scalar(0), result4);

        Mat finalresult = image.clone();
        Core.max(result1, result2, finalresult);
        Core.max(finalresult, result3, finalresult);
        Core.max(finalresult, result4, finalresult);

        /*
        //Unfinished. Don't know if this is necessary though.
        Mat img_edge = image.clone();
        Imgproc.Canny(image, img_edge, 0.5, 100);
        Mat se = Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(11,11));
        */


        return finalresult;
    }

    //grayScale is initially 8UC1 0~255
    private Mat getJND(Mat grayScale, int debug){
        System.out.println("debug is "+debug);
        Mat grayScale_clone = grayScale.clone();
        Mat Gm = func_Gm(grayScale);
        Gm.convertTo(Gm, CV_64FC1);

        //if(debug==3)
        //    printMatrix(Gm);

        Mat filter = getFilterForI_bar();
        Mat bg = grayScale_clone.clone();
        Imgproc.filter2D(grayScale_clone, bg, -1, filter, new Point(-1, -1),0,BORDER_CONSTANT);
        //Added this rn
        Core.divide(bg, new Scalar(32), bg);

        //byte[] temp = new byte[(int)grayScale_clone.total()];
        byte[] allPixels = new byte[(int)bg.total()];
        grayScale.get(0,0,allPixels);

        double[] allPixelsDouble = new double[(int)bg.total()];

        for(int i=0;i<allPixels.length;i++){
            int allPixels_i = (((int)allPixels[i])&0xFF);
            if(allPixels_i<=127)
                allPixelsDouble[i] = 17 * (1-Math.sqrt(((double)allPixels_i)/127))+3;
            else
                allPixelsDouble[i] = (3/(double)128)*(allPixels_i-127)+3;
            if(debug == 3) {
                //They're all NaN actually. Why?
                //Log.d("allPixelsDouble: ",allPixelsDouble[i] + " ");
            }
        }


        Mat JNDl = new Mat(grayScale.size(),CV_64FC1);
        JNDl.put(0,0, allPixelsDouble);

        /*
        //NaN here!
        if(debug==3)
            printMatrix(JNDl);
        */

        //Now we have to calculate JNDt!!!
        Mat alpha = new Mat(JNDl.size(), CV_64FC1);
        if(bg.type() == CV_8UC1) {
            System.out.println("bg converted!");
            bg.convertTo(bg, CV_64FC1); // Originally it was CV_8UC1
        }
        /*
        //I fixed this!
        Core.multiply(bg, new Scalar(0.01), alpha);
        Core.add(alpha, new Scalar(11.5), alpha);
        if(debug==1) {
            Log.d("Here", "alpha: ");
            printMatrix(alpha);
        }
        Core.multiply(Gm, new Scalar(0.01), Gm);
        Core.subtract(Gm, new Scalar(1), Gm);
        Core.absdiff(Gm, new Scalar(0),Gm);

       // if(debug==3){
       //     Log.d("Here", "Gm: ");
       //     printMatrix(Gm);
       // }

        Mat JNDt = new Mat(JNDl.size(), CV_64FC1);
        Core.multiply(Gm, alpha, JNDt);
        Core.subtract(JNDt, new Scalar(12), JNDt);
        */

        Core.multiply(bg, new Scalar(0.0001), alpha);
        Core.add(alpha, new Scalar(0.115), alpha);

        Mat LAMBDA = new Mat(JNDl.size(), CV_64FC1, new Scalar(0.5));
        Mat beta = bg.clone();
        Core.multiply(bg, new Scalar(0.01), beta);
        Core.subtract(LAMBDA, beta, beta);

        Mat JNDt = new Mat(JNDl.size(), CV_64FC1);
        System.out.println("Gm.type is "+Gm.type());
        System.out.println("alpha.type is "+alpha.type());
        Core.multiply(Gm, alpha, JNDt);
        Core.add(JNDt, beta, JNDt);


        /*
        if(debug==3)
            printMatrix(JNDt);
        */

        Mat JND = new Mat(JNDl.size(), CV_64FC1);
        Core.add(JNDl, JNDt, JND);
        Mat temp = new Mat(JNDl.size(), CV_64FC1);
        Core.min(JNDl, JNDt, temp);
        Core.multiply(temp, new Scalar(0.3), temp);
        Core.subtract(JND,temp,JND);

        /*
        if(debug==3) {
            printMatrix(JND);
        }
        */

        return JND;
    }

}