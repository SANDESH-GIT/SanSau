/**
 * Thread which handles all requests for MotionBlur transform.
 * Parameters: Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile
 * Sends a message to the handler in Library after processing the image using its Messenger object to
 * let the library know that the processed Bitmap output image is ready.
 */

package edu.asu.msrs.artcelerationlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class MotionBlur implements Runnable {
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private MemoryFile memoryFile;
    private int intArgs[];

    MotionBlur(Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile, int[] intArgs) {
        this.messenger = messenger;
        this.input = input;
        this.requestNo = requestNo;
        this.memoryFile = memoryFile;
        this.intArgs =intArgs;
    }

    static {
        System.loadLibrary("MotionBlurLib");
    }
    /**
     * Run method includes all the transform logic for processing the input Bitmap image.
     * Writes the output image to the Memory File and shares the file descriptor of this file
     * with the library.
     */
    @Override
    public void run() {
        Log.d("fd", "Motion Blur!");

        // a0=0-> Horizontal motion blur; a0=1 -> Vertical motion blur
        //int a0 = 0;
        int a0 = intArgs[0];
        // radius
        //int a1 = 8;
        int a1=intArgs[1];

        //int size = 2*a1+1;

        // Image size, w-> width & h->height
        int w = input.getWidth();
        int h = input.getHeight();

        // Creating bitmap to be returned as a modified (mutable output bitmap)
         Bitmap output = Bitmap.createBitmap(w,h,input.getConfig());

/*
        int[][] Pr = new int[w][h];
        int[][] Pg = new int[w][h];
        int[][] Pb = new int[w][h];

        int[][] r = new int[w][h]; // Red
        int[][] g = new int[w][h]; // Green
        int[][] b = new int[w][h]; // Blue
        int pix;

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                pix = input.getPixel(i,j);
                r[i][j] = Color.red(pix);
                g[i][j] = Color.green(pix);
                b[i][j] = Color.blue(pix);
            }
        }


        //Log.d("fd", "ret: "+op);

        if (a0==0) {
            for(int j = 0; j < h; j++){
                for(int i =0; i < w; i++){
                    for (int k = 0; k < size; k++) {
                        int xval = i - a1 + k;
                        if (!(xval < 0 || xval >= w)) {
                            Pr[i][j] += r[xval][j];
                            Pg[i][j] += g[xval][j];
                            Pb[i][j] += b[xval][j];
                        }
                    }
                    Pr[i][j] /= size;
                    Pg[i][j] /= size;
                    Pb[i][j] /= size;

                    output.setPixel(i, j, Color.argb(255, Pr[i][j], Pg[i][j], Pb[i][j]));
                }
            }
        }else if (a0==1){
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    for (int k = 0; k < size; k++) {
                        int yval = j - a1 + k;
                        if (!(yval < 0 || yval >= h)) {
                            Pr[i][j] += r[i][yval];
                            Pg[i][j] += g[i][yval];
                            Pb[i][j] += b[i][yval];
                        }
                    }
                    Pr[i][j] /= size;
                    Pg[i][j] /= size;
                    Pb[i][j] /= size;

                    output.setPixel(i, j, Color.argb(255, Pr[i][j], Pg[i][j], Pb[i][j]));
                }
            }
        }
*/


        getMotionBlur(a0, a1, input, output);
        Log.d("Motion Blur","Done processing...!!!");

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            output.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] oparray = outputStream.toByteArray();
            memoryFile.getOutputStream().write(oparray);

            Bundle bundle = new Bundle();
            Message message = Message.obtain(null, 10, oparray.length, requestNo);
            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            bundle.putParcelable("ClassPFD", pfd);
            message.setData(bundle);

            messenger.send(message);
            //Thread.sleep(1000,0);
            memoryFile.allowPurging(true);
            memoryFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public native static void getMotionBlur(int a0, int a1, Bitmap input,Bitmap output);

}

