# Notes
contains some jmh benchmarks over various topics
 - a small jmh about "iso 2 letter code" checks to prove what performs better, map or list impl.
 - a small jmh about the consequences of how to initialize JAXBContext 
 - a small jmh about comparing scrypt hasing runtime of two different implementations

- mvn package
- >java -jar target/benchmarks.jar BenchmarkLanguageUtils
- >java -jar target/benchmarks.jar BenchmarkJAXBContext
- >java -jar target/benchmarks.jar BenchmarkPasswdHashing