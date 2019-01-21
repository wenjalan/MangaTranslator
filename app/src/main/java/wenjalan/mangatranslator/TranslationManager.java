package wenjalan.mangatranslator;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;

import static wenjalan.mangatranslator.Key.GOOGLE_API_KEY;

public class TranslationManager {

    private static Translate translate;

    public static Translate getTranslate() {
        TranslateOptions options = TranslateOptions.newBuilder()
                .setApiKey(GOOGLE_API_KEY)
                .build();
        return options.getService();
    }

    // translates a string of text (japanese) into english
    public static String translate(String text) {
        if (translate == null) {
            translate = getTranslate();
        }
        return "hello";
    }

}
