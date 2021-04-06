package org.gottschd.languageutils;

import java.util.Locale;
import java.util.stream.Stream;

public class LanguageUtils {
    public static boolean isValid(String language) {
		return language != null && language.length() == 2
				&& Stream.of(Locale.getISOLanguages()).anyMatch(language::equals);
	}
}
