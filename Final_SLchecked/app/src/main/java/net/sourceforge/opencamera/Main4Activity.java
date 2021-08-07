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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.core.Core.BORDER_REPLICATE;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;


public class Main4Activity extends Activity {

    ArrayList<byte[]> x = new ArrayList<>();
    private static final String TAG = "Main4Activity";
    Bitmap k;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        Intent ss = getIntent();
        if (ss.getExtras() != null) {
            Log.d(TAG, "images passed to Main4Activity");
            x.add(ss.getByteArrayExtra("b1"));
            x.add(ss.getByteArrayExtra("b2"));
            x.add(ss.getByteArrayExtra("b3"));

            if (x.size() == 3) {
                Log.d(TAG, "Main4Activity image size is 3");
                Mat mat1 = new Mat();
                Mat mat2 = new Mat();
                Mat mat3 = new Mat();

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

                Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(mat3, mat3, Imgproc.COLOR_RGBA2RGB);


                List<Mat> images = new ArrayList<Mat>();
                images.add(mat1);
                images.add(mat2);
                images.add(mat3);
                //AlignMTB alignMTB = createAlignMTB();
                //alignMTB.process(images, images);
                Mat resultImage = exposureFusion(images, 1, 1, 1, 1);

                Bitmap bmp = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultImage, bmp);

                k = bmp;

                ImageView img1 = findViewById(R.id.i1);
                img1.setImageBitmap(bmp);
                img1.setRotation(90);
                img1.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                img1.setAdjustViewBounds(true);
                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

        }

    }

    public void next(View view) {
        Intent nextt = new Intent(this, Main5Activity.class);
        nextt.putExtra("b1", x.get(0));
        nextt.putExtra("b2", x.get(1));
        nextt.putExtra("b3", x.get(2));
        Log.d(TAG, "images passsed to main activity5");
        startActivity(nextt);
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
        } catch (Exception e) {
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

    private Mat exposureFusion(List<Mat> images, double w_c, double w_s, double w_e, int depth) {
        if (images.size() < 2) {
            //print("Input has to be a list of at least two images");
            return null;
        }

        Size size = images.get(0).size();
        for (int i = 1; i < images.size(); i++) {
            if (!images.get(i).size().equals(size)) {
                //print("Input images have to be of the same size");
                return null;
            }
        }
        int r = (int) size.height;
        int c = (int) size.width;
        int N = images.size();


        // COMPUTE WEIGHT MAPS
        List<Mat> weights = computeWeights(images, w_c, w_s, w_e);


        // COMBINE WEIGHT MAP WITH PYRAMIDS
        List<List<Mat>> lps = new ArrayList<List<Mat>>();
        List<List<Mat>> gps = new ArrayList<List<Mat>>();

        for (int i = 0; i < N; i++) {
            lps.add(laplacianPyramid(images.get(i), depth));
            gps.add(gaussianPyramid(weights.get(i), depth));
        }

        //System.out.println("gaussianPyramid length is "+gps.get(0).size());
        //System.out.println("laplacianPyramid length is "+lps.get(0).size());


        List<Mat> pyramid = new ArrayList<Mat>();

        for (int lev = 0; lev < depth; lev++) {
            Mat ls = Mat.zeros(lps.get(0).get(lev).size(), CV_8UC3);
            for (int i = 0; i < N; i++) {
                Mat lp = lps.get(i).get(lev);
                lp.convertTo(lp, CV_32FC3);
                Mat gpsFloat = new Mat(gps.get(i).get(lev).size(), CV_32F);
                gps.get(i).get(lev).convertTo(gpsFloat, CV_32FC1, (1.0 / 255.0));
                List<Mat> gpList = new ArrayList<Mat>();
                Mat gpsOnes = Mat.ones(gps.get(i).get(lev).size(), CV_32FC1);
                Mat gpsValues = new Mat(gps.get(i).get(lev).size(), CV_32FC1);
                Core.multiply(gpsOnes, Scalar.all(0.5), gpsValues);
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                Mat gp = new Mat(gpsFloat.size(), CV_32FC3);
                Core.merge(gpList, gp);
                Mat lp_gp = new Mat(lp.size(), CV_8UC3);
                Core.multiply(lp, gp, lp_gp, 255, CV_8UC3);
                Core.add(ls, lp_gp, ls);
            }
            pyramid.add(ls);
        }


        // COLLAPSE PYRAMID
        Mat resultImage = pyramid.get(depth - 1);

        for (int i = depth - 2; i >= 0; i--) {
            //Imgproc.pyrUp(resultImage, resultImage, new Size(pyramid.get(i).width(), pyramid.get(i).height()));
            //Core.add(resultImage,pyramid.get(i),resultImage);
            //image_expand(resultImage, new Size(pyramid.get(i).width(), pyramid.get(i).height()));
            Core.add(image_expand(resultImage, new Size(pyramid.get(i).width(), pyramid.get(i).height())), pyramid.get(i), resultImage);
        }



        /*
        // COMPUTE RESULTS DIRECTLY WITHOUT PYRAMIDS
        Mat resultImage = Mat.zeros(r,c,CV_32FC4);
        for(int i = 0; i < N; i++)
        {
            Mat img = images.get(i);
            Mat imgFloat = new Mat(r,c,CV_32F);
            img.convertTo(imgFloat,CV_32F,(1.0/255.0));
            Mat weighted = new Mat(r,c,CV_32F);
            List<Mat> rgbImages = new ArrayList<Mat>();
            Core.split(imgFloat,rgbImages);
            Mat rChannel = rgbImages.get(0);
            Mat gChannel = rgbImages.get(1);
            Mat bChannel = rgbImages.get(2);
            Core.multiply(weights.get(i),rChannel,rChannel);
            Core.multiply(weights.get(i),gChannel,gChannel);
            Core.multiply(weights.get(i),bChannel,bChannel);
            rgbImages.set(0,rChannel);
            rgbImages.set(1,gChannel);
            rgbImages.set(2,bChannel);
            Core.merge(rgbImages,weighted);
            Core.add(resultImage,weighted,resultImage);
        }
        resultImage.convertTo(resultImage,CV_8U,255);
        */


        return resultImage;
    }

    private Mat getGaussianKernel() {
        /*
        double[] kernel = new double[5];
        kernel[0]= 0.0625;
        kernel[1]=0.25;
        kernel[2]=0.375;
        kernel[3]=0.25;
        kernel[4]=0.0625;
        Mat mat = new Mat(5,1,CV_32FC1);
        mat.put(0,0,kernel);
        return mat;
        */
        return Imgproc.getGaussianKernel(5, 0.4);
    }

    private Mat image_reduce(Mat image) {
        Mat kernel = getGaussianKernel();
        Mat result = new Mat(image.size(), CV_32FC3);
        //Mat temp = new Mat(1,5,CV_32FC1);
        //Core.transpose(kernel, temp);
        //Imgproc.sepFilter2D(image, result, -1, kernel, temp, new Point(-1,-1), 0, BORDER_REFLECT);
        Imgproc.filter2D(image, result, -1, kernel);
        Imgproc.resize(result, result, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
        //result.convertTo(result,CV_8UC3);
        return result;
    }

    private Mat image_expand(Mat image, Size size) {
        Mat kernel = getGaussianKernel();
        Mat clone = image.clone();
        Mat result = new Mat(size, CV_32FC3);
        Imgproc.resize(clone, clone, size);
        //Mat temp = new Mat(1,5,CV_32FC1);
        //Core.transpose(kernel, temp);
        //Imgproc.sepFilter2D(clone, result, -1, kernel, temp, new Point(-1,-1), 0, BORDER_REFLECT);
        Imgproc.filter2D(clone, result, -1, kernel);
        //result.convertTo(result,CV_8UC3);
        return result;
    }

    private List<Mat> gaussianPyramid(Mat mat, int depth) {
        Mat copy = mat.clone();
        List<Mat> pyramid = new ArrayList<Mat>();
        pyramid.add(copy);
        for (int i = 0; i < depth; i++) {
            //Mat m = new Mat((mat.rows() + 1) / 2, (mat.cols() + 1) / 2, mat.type());
            //Imgproc.pyrDown(mat, m);
            copy = image_reduce(copy);
            pyramid.add(copy);
            //mat = m;
        }
        return pyramid;
    }

    private List<Mat> laplacianPyramid(Mat mat, int depth) {
        List<Mat> pyramid = new ArrayList<Mat>();
        List<Mat> gPyramid = gaussianPyramid(mat, depth + 1);
        pyramid.add(gPyramid.get(depth - 1));
        for (int i = depth - 1; i >= 1; i--) {
            Mat m = gPyramid.get(i);
            //Mat dst = new Mat();//new Mat(gPyramid.get(i-1).rows(),gPyramid.get(i-1).cols(),CV_32FC1);
            Mat dst = image_expand(m, new Size(gPyramid.get(i - 1).width(), gPyramid.get(i - 1).height()));
            //Imgproc.pyrUp(m, dst, new Size(gPyramid.get(i-1).width(), gPyramid.get(i-1).height()));
            System.out.println(dst.rows() + " " + dst.cols());
            System.out.println(gPyramid.get(i - 1).rows() + " " + gPyramid.get(i - 1).cols());
            Core.subtract(gPyramid.get(i - 1), dst, dst);
            pyramid.add(0, dst);
        }
        return pyramid;
    }


    private List<Mat> computeWeights(List<Mat> images, double w_c, double w_s, double w_e) {

        int r = images.get(0).rows();
        int c = images.get(0).cols();
        double regularization = 1.0;

        List<Mat> weights = new ArrayList<Mat>();
        Mat weightsSum = Mat.zeros(images.get(0).size(), CV_32FC1);

        for (int i = 0; i < images.size(); i++) {
            Mat img = images.get(i);    //8UC3 right now

            Mat imgFloat = new Mat(r, c, CV_32FC3);
            img.convertTo(imgFloat, CV_32FC3, (1.0 / 255.0));

            Mat grayScale = new Mat(r, c, CV_8UC1);
            Imgproc.cvtColor(img, grayScale, Imgproc.COLOR_BGR2GRAY);


            Mat weight = Mat.ones(r, c, CV_32FC1);

            //CONTRAST
            Mat imgGray = new Mat(r, c, CV_32FC1); //Float version
            grayScale.convertTo(imgGray, CV_32FC1, (1.0 / 255.0));
            Mat imgLaplacian = new Mat(r, c, CV_32FC1);
            Imgproc.Laplacian(imgGray, imgLaplacian, CV_32FC1, 1, 1, 0, BORDER_REPLICATE);
            Mat contrastWeight = new Mat(r, c, CV_32FC1);
            Core.absdiff(imgLaplacian, Scalar.all(0), contrastWeight);
            Core.pow(contrastWeight, w_c, contrastWeight);
            Core.add(contrastWeight, Scalar.all(regularization), contrastWeight);
            Core.multiply(weight, contrastWeight, weight);

            //SATURATION
            Mat saturationWeight = new Mat(r, c, CV_32FC1);
            List<Mat> rgbChannels = new ArrayList<Mat>();
            Core.split(imgFloat, rgbChannels);
            Mat R = rgbChannels.get(0);
            Mat G = rgbChannels.get(1);
            Mat B = rgbChannels.get(2);
            Mat mean = Mat.zeros(r, c, CV_32FC1);
            Core.add(mean, R, mean);
            Core.add(mean, G, mean);
            Core.add(mean, B, mean);
            Core.divide(mean, Scalar.all(3), mean);
            Core.subtract(R, mean, R);
            Core.subtract(G, mean, G);
            Core.subtract(B, mean, B);
            Core.pow(R, 2, R);
            Core.pow(G, 2, G);
            Core.pow(B, 2, B);
            Mat std = Mat.zeros(r, c, CV_32FC1);
            Core.add(std, R, std);
            Core.add(std, G, std);
            Core.add(std, B, std);
            Core.divide(std, Scalar.all(3), std);
            Core.pow(std, 0.5, std);
            Core.pow(std, w_s, saturationWeight);
            Core.add(saturationWeight, Scalar.all(regularization), saturationWeight);
            Core.multiply(weight, saturationWeight, weight);

            //WELL-EXPOSEDNESS

            double sigma = 0.2;
            Mat gaussianCurve = imgFloat.clone();
            Core.subtract(gaussianCurve, Scalar.all(0.5), gaussianCurve);
            Core.pow(gaussianCurve, 2, gaussianCurve);
            //Core.divide(gaussianCurve,Scalar.all(-2 * sigma2),gaussianCurve);
            Core.divide(gaussianCurve, Scalar.all(-2 * sigma * sigma), gaussianCurve);
            Core.exp(gaussianCurve, gaussianCurve);
            List<Mat> rgbGaussianCurves = new ArrayList<Mat>();
            Core.split(gaussianCurve, rgbGaussianCurves);
            Mat exposednessWeight = Mat.ones(r, c, CV_32FC1);
            Core.multiply(exposednessWeight, rgbGaussianCurves.get(0), exposednessWeight);
            Core.multiply(exposednessWeight, rgbGaussianCurves.get(1), exposednessWeight);
            Core.multiply(exposednessWeight, rgbGaussianCurves.get(2), exposednessWeight);
            Core.pow(exposednessWeight, w_e, exposednessWeight);
            Core.add(exposednessWeight, Scalar.all(regularization), exposednessWeight);
            Core.multiply(weight, exposednessWeight, weight);

            Core.add(weightsSum, weight, weightsSum);
            weights.add(weight);
        }

        Core.add(weightsSum, Scalar.all(1e-12), weightsSum);

        for (int i = 0; i < weights.size(); i++) {
            Mat normWeight = new Mat(r, c, CV_32FC1);
            Core.divide(weights.get(i), weightsSum, normWeight);
            weights.set(i, normWeight);
        }

        return weights;

    }

    private Mat readImageFromResources(int data) {
        Mat img = null;
        try {
            img = Utils.loadResource(this, data);
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGR);
        } catch (IOException e) {
            e.printStackTrace();
            //Log.e(TAG,Log.getStackTraceString(e));
        }
        return img;
    }
}

    /*private Mat exposureFusion (List<Mat> images, double w_c, double w_s, double w_e, int depth)
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
        int N = images.size();*/



// COMPUTE WEIGHT MAPS
// weights are 32FC1 (0.0~1.0)
        /*List<Mat> weights = computeWeights(images,w_c,w_s,w_e);
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
            pyramid.add(new Mat(gps.get(0).get(i).size(), CV_32FC3, new Scalar(0)));
        }

        for(int i=0;i<N;i++)
        {
            for(int lev = 0 ;lev<depth; lev++){
                Mat gpsFloat = gps.get(i).get(lev).clone();
                List<Mat> gpList = new ArrayList<Mat>();
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                gpList.add(gpsFloat);
                Mat gp = new Mat(gpsFloat.size(),CV_32FC3);
                Core.merge(gpList,gp);

                Mat lp = lps.get(i).get(lev);
                ////32FC3
                //System.out.println("Before convertTo lp type is "+lp.type());
                // I have to convert it because matlab uses values between 0~1. The gaussian pyramid is already between 0~1.
                lp.convertTo(lp,CV_32FC3, (1.0/255.0));
                //32FC3
                //System.out.println("After convertTo lp type is "+lp.type());
                Mat lp_gp = new Mat(lp.size(),CV_32FC3);
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
            /*Core.add(image_expand(resultImage.clone(), odd, true), pyramid.get(i), resultImage);
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
//}

    /*private Mat getGaussianKernel1D(){
        double[] kernel = new double[5];
        kernel[0]= 0.0625f;
        kernel[1]=0.25f;
        kernel[2]=0.375f;
        kernel[3]=0.25f;
        kernel[4]=0.0625f;
        Mat mat = new Mat(5,1,CV_32FC1);
        mat.put(0,0,kernel);
        return mat;
    }

    private Mat getGaussianKernel(){
        return getGaussianKernel1D();
    }

    private Mat image_reduce(Mat image){
        if(image.type() == CV_8UC3){
            image.convertTo(image, CV_32FC3);
        }
        Mat kernelX = getGaussianKernel();
        Mat kernelY = new Mat();
        Core.transpose(kernelX, kernelY);
        //weight map일 때는 32FC1, laplacian pyramid일 때는 32FC3
        //System.out.println("image type is "+ image.type());
        Mat clone = image.clone();

        Mat result;
        //If this is for making the laplacian pyramid
        if(clone.type() == CV_32FC3) {
            result = new Mat(image.size(), CV_32FC3);
            Mat resultR = new Mat(image.size(), CV_32FC1);
            Mat resultG = new Mat(image.size(), CV_32FC1);
            Mat resultB = new Mat(image.size(), CV_32FC1);
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
            result = new Mat(image.size(), CV_32FC1);
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
        Mat result = new Mat(r, c, CV_32FC3);
        Mat resultR = new Mat(r, c ,CV_32FC1);
        Mat resultG = new Mat(r, c, CV_32FC1);
        Mat resultB = new Mat(r, c, CV_32FC1);
        UIntVer.convertTo(UIntVer, CV_32FC3);

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
            result.convertTo(result, CV_32FC3, (1.0/255.0));

        return result;


        //return rgbUIntVer.get(2);
    }


    private List<Mat> gaussianPyramid(Mat mat, int depth)
    {
        Mat copy = mat.clone();
        //나중에 라플라시안 피라미드 만들 때를 대비해서. 일단 weightmap일 때는 mat type가 32FC1임!
        if(mat.type() == CV_8UC3)
            copy.convertTo(copy, CV_32FC3);
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

    private List<Mat> laplacianPyramid(Mat mat, int depth)
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

            Mat weight = Mat.ones(r,c,CV_32FC1);

            //CONTRAST
            Mat imgGray = new Mat(r,c,CV_32FC1); //Float version
            grayScale.convertTo(imgGray, CV_32FC1, (1.0/255.0));
            //compare this to matlab!
            //if(i==1)
            //printMatrix(imgGray); // Looks ok actually

            Mat imgLaplacian = new Mat(r,c,CV_32FC1);
            //Mat laplacianFilter = getLaplacianFilter();
            //Imgproc.filter2D(imgGray, imgLaplacian, -1, laplacianFilter, new Point(-1, -1), 0, BORDER_REPLICATE);
            Imgproc.Laplacian(imgGray,imgLaplacian, CV_32FC1, 1, 1, 0, BORDER_REPLICATE);
            Mat contrastWeight = new Mat(r,c,CV_32FC1);
            Core.absdiff(imgLaplacian,new Scalar(0),contrastWeight);
            Core.pow(contrastWeight,w_c,contrastWeight);
            Core.multiply(weight,contrastWeight,weight);

            //SATURATION
            Mat imgFloat = new Mat(r,c,CV_32FC3);
            img.convertTo(imgFloat,CV_32FC3,(1.0/255.0));
            Mat saturationWeight = new Mat(r,c,CV_32FC1);
            List<Mat> rgbChannels = new ArrayList<Mat>();
            Core.split(imgFloat,rgbChannels);
            Mat R = rgbChannels.get(0);
            Mat G = rgbChannels.get(1);
            Mat B = rgbChannels.get(2);
            Mat mean = Mat.zeros(r,c,CV_32FC1);
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
            Mat std = Mat.zeros(r,c,CV_32FC1);
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

            Mat exposednessWeight = Mat.ones(r,c,CV_32FC1);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(0),exposednessWeight);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(1),exposednessWeight);
            Core.multiply(exposednessWeight,gaussianCurveRGB.get(2),exposednessWeight);
            Core.pow(exposednessWeight, w_e, exposednessWeight);
            Core.multiply(weight,exposednessWeight,weight);

            weights.add(weight);

        }

        for(int i=0;i<weights.size();i++)
        {
            Core.add(weights.get(i), new Scalar(1e-12), weights.get(i));
        }


        Mat weightsSum = new Mat(weights.get(1).size(), CV_32FC1, new Scalar(0));
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


}*/
