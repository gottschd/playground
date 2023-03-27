package org.gottschd;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import com.lambdaworks.crypto.SCryptUtil;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 1)
@State(Scope.Benchmark)
public class BenchmarkPasswdHashing {

    private static final int PARALLIZATION = 1;
    private static final int MEMORY_COST = 8;
    private static final int CPU_COST = 16384;

    @Param({ "$g12345sasewegk??sdg/e8" })
    public String plainPasswd;

    SCryptPasswordEncoder staticEncoder;

    @Setup(Level.Trial)
    public void setupTrial() {
        staticEncoder = new SCryptPasswordEncoder(CPU_COST, MEMORY_COST,
                PARALLIZATION,
                32, 16);
    }

    @Benchmark
    public String encodeSpringCryptoSingletonCrypto() {
        return staticEncoder.encode(plainPasswd);
    }

    @Benchmark
    public String encodeLamdaworkds() {
        return SCryptUtil.scrypt(plainPasswd, CPU_COST, MEMORY_COST, PARALLIZATION);
    }

}