package org.gottschd.languageutils;

import java.util.Locale;
import java.util.Set;

public class LanguageUtils2 {
	private static final Set<String> LANGS = Set.of(Locale.getISOLanguages());

	public static boolean isValid(String language) {
		return language != null && language.length() == 2 && LANGS.contains(language);
	}
}
