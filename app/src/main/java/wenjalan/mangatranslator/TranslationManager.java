package wenjalan.mangatranslator;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import static wenjalan.mangatranslator.Key.GOOGLE_API_KEY;

public class TranslationManager {

    private static final String TAG = "MT-TranslationManager";

    private static Translate translate = null;

    private static Translate getTranslate() {
//        Translate.Builder builder = new Translate.Builder(
//                new NetHttpTransport(),
//                new AndroidJsonFactory(),
//                null
//        );
//        builder.setTranslateRequestInitializer(
//                new TranslateRequestInitializer(GOOGLE_API_KEY)
//        );
//        Translate translate = builder.build();
        TranslateOptions options = TranslateOptions.newBuilder()
                .setApiKey(GOOGLE_API_KEY)
                .build();
        Translate translate = options.getService();
        return translate;
    }

    public static void translate(final String text) {
        if (translate == null) {
            translate = getTranslate();
        }
        Log.d(TAG, "Text to Translate:\n" + text);
        // translate the text
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage("en"));
                String englishText = translation.getTranslatedText();
                Log.d(TAG, "Translation:\n" + englishText);
            }
        });
    }

}
