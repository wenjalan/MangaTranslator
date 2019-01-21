package wenjalan.mangatranslator;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.util.Collections;

import static wenjalan.mangatranslator.Key.GOOGLE_API_KEY;

public class TranslationManager {

    private static final String TAG = "MT-TranslationManager";

    private static Translate translate = null;

    private static Translate getTranslate() {
        Translate.Builder builder = new Translate.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null
        );
        builder.setTranslateRequestInitializer(
                new TranslateRequestInitializer(GOOGLE_API_KEY)
        );
        Translate translate = builder.build();
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
                try {
                    Translate.Translations.List list = translate.new Translations().list(
                            Collections.singletonList(text), "en");
                    list.setKey(GOOGLE_API_KEY);
                    list.setSource("ja");
                    final TranslationsListResponse response = list.execute();
                    Log.d(TAG, "Translated text:\n" + response.getTranslations().get(0).getTranslatedText());
                } catch (Exception e) {
                    Log.d(TAG, "Error translating text");
                    e.printStackTrace();
                }

            }
        });
    }

}
