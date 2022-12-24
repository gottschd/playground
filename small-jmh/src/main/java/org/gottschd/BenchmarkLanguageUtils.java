package org.gottschd;

import org.gottschd.languageutils.LanguageUtils;
import org.gottschd.languageutils.LanguageUtils2;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchmarkLanguageUtils {

    @Param({"de", "en", "öö"})
    public String lang;

    @Benchmark
    public boolean usingSteamContains() {
        return LanguageUtils.isValid(lang);
    }

    @Benchmark
    public boolean usingSetContains() {
        return LanguageUtils2.isValid(lang);
    }

}