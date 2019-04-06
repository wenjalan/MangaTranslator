package wenjalan.mangatranslator.cloud;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import wenjalan.mangatranslator.MainActivity;

public class VisionManager {

    private static String API_KEY = "AIzaSyCHm_E5aK3YXOxkiLuFtOMzpqGSJu0glNo";

    private static final String TAG = "MT-VisionManager";

    private static Vision vision = null;

    public static Vision getVision() {
        Vision.Builder builder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null
        );
        builder.setVisionRequestInitializer(
                new VisionRequestInitializer(API_KEY)
        );
        Vision vision = builder.build();
        return vision;
    }

    public static void findText(final String filepath) {
        if (vision == null) {
            vision = getVision();
        }
        // Log.d(TAG, "Finding text...");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // convert photo to byte array
                    FileInputStream fis = new FileInputStream(new File(filepath));
                    byte[] data = IOUtils.toByteArray(fis);
                    fis.close();

                    // create the image
                    Image inputImage = new Image();
                    inputImage.encodeContent(data);

                    // make the request
                    Feature feature = new Feature();
                    feature.setType("TEXT_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(feature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    // get response
                    BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();
                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();

                    // Log it
                    // Log.d(TAG, "Text found: " + text.getText());

                    // send back to Main
                    if (text != null) {
                        MainActivity.onVisionManagerComplete(text.getText());
                    }

                } catch (Exception e) {
                    Log.d(TAG, "Error finding text");
                    e.printStackTrace();
                    MainActivity.get().setTranslatedText("No text found");
                    MainActivity.onError();
                }
            }
        });
    }

}
