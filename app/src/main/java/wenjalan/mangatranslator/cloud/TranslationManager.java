package wenjalan.mangatranslator.cloud;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.util.Collections;

import wenjalan.mangatranslator.MainActivity;

import static wenjalan.mangatranslator.cloud.Key.GOOGLE_API_KEY;

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
        // Log.d(TAG, "Text to Translate:\n" + text);
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
                    String translated = response.getTranslations().get(0).getTranslatedText();
                    // Log.d(TAG, "Translated text:\n" + translated);

                    // reformat it to replace escape codes for ' and others
                    String formatted = Html.fromHtml(translated).toString();

                    // return it to main
                    MainActivity.onTranslationComplete(formatted);

                } catch (Exception e) {
                    Log.d(TAG, "Error translating text");
                    e.printStackTrace();
                }
            }
        });
    }

}
