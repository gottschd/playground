package org.gottschd;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

public class AlchemicalReductionBenchmark {
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
	@Fork(1)
	@State(Scope.Thread)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public static abstract class AbstractBenchmark {

		AlchemicalReductionable reducer;

		@Setup
		public void setup() {
			reducer = getImpl();
		};

		protected abstract AlchemicalReductionable getImpl();

		@Benchmark
	    @Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
	    @Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
		public void reduce(Blackhole bh) {
			String result = reducer.reduce("dabAcCaCBAcCcaDA");
			bh.consume(result);
		}

	}

	public static class Benchmark001 extends AbstractBenchmark {
		@Override
		protected AlchemicalReductionable getImpl() {
			return new AlchemicalReductionVersion001();
		}
	}

	public static class Benchmark002 extends AbstractBenchmark {
		@Override
		protected AlchemicalReductionable getImpl() {
			return new AlchemicalReductionVersion002();
		}
	}

	public static class Benchmark003 extends AbstractBenchmark {
		@Override
		protected AlchemicalReductionable getImpl() {
			return new AlchemicalReductionVersion003();
		}
	}

	public static class Benchmark004 extends AbstractBenchmark {
		@Override
		protected AlchemicalReductionable getImpl() {
			return new AlchemicalReductionVersion004();
		}
	}
}
