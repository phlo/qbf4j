package main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.io.QCIRTest;
import at.jku.fmv.qbf.io.QDIMACSTest;
import main.QCIR2PNF;

@DisplayName("qcir2pnf")
class QCIR2PNFTest {

	static Path inputLNCS, inputG14;
	static Path output;

	static int exitCode;

	static PrintStream stdout = System.out;
	static PrintStream stderr = System.err;

	// ∃p,q'': ∀q,q',r'': ∃r,r': ∀s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)
	public static List<String> lncsAUEU = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "exists(p, q'')"
		, "forall(q, r'', q')"
		, "exists(r, r')"
		, "forall(s)"
		, "exists(t)"
		, "output(13)"
		, "13 = and(ϕ0, ϕ1, -ϕ2)"
	});

	// ∃p: ∀q: ∃q'',r: ∀q',r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)
	public static List<String> lncsADED = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "exists(p)"
		, "forall(q)"
		, "exists(q'', r)"
		, "forall(s, r'', q')"
		, "exists(t, r')"
		, "output(13)"
		, "13 = and(ϕ0, ϕ1, -ϕ2)"
	});

	// ϕ0 ∧ ϕ1 ∧ -ϕ2
	public static List<String> lncsQDIMACS = Arrays.asList(new String[] {
		"p cnf 3 3"
		, "1 0"
		, "2 0"
		, "-3 0"
	});

	// ∀z: (z ∨ ∃x1, x2: (x1 ∧ x2 ∧ z))
	public static List<String> g14 = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "forall(z)"
		, "output(4)"
		, "5 = and(x1, x2, z)"
		, "6 = exists(x1, x2; 5)"
		, "4 = or(z, 6)"
	});

	// ∀z: ∃x1, x2: (z ∨ (x1 ∧ x2 ∧ z))
	public static List<String> g14PNF = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "forall(z)"
		, "exists(x1, x2)"
		, "output(4)"
		, "5 = and(x1, x2, z)"
		, "4 = or(z, 5)"
	});

	// ∀z: ∃x1, x2: (z ∨ (x1 ∧ x2 ∧ z))
	public static List<String> g14PNFCleansed = Arrays.asList(new String[] {
		"#QCIR-G14 5"
		, "forall(1)"
		, "exists(2, 3)"
		, "output(4)"
		, "5 = and(2, 3, 1)"
		, "4 = or(1, 5)"
	});

	// ∀z: ∃x1,x2:
	//   (pg0
	//   ∧ (-_pg0 ∨ z ∨ _pg1)
	//   ∧ (-_pg1 ∨ x1)
	//   ∧ (-_pg1 ∨ x2)
	//   ∧ (-_pg1 ∨ z))
	public static List<String> g14PCNF = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "forall(z)"
		, "exists(x1, x2)"
		, "output(6)"
		, "7 = or(-_pg0, z, _pg1)"
		, "8 = or(-_pg1, x1)"
		, "9 = or(-_pg1, x2)"
		, "10 = or(-_pg1, z)"
		, "6 = and(_pg0, 7, 8, 9, 10)"
	});

	// ∀2: ∃3,4:
	//   (1
	//   ∧ (-1 ∨ 2 ∨ 3)
	//   ∧ (-3 ∨ 4)
	//   ∧ (-3 ∨ 5)
	//   ∧ (-3 ∨ 2))
	public static List<String> g14PCNFCleansed = Arrays.asList(new String[] {
		"#QCIR-G14 10"
		, "forall(2)"
		, "exists(4, 5)"
		, "output(6)"
		, "7 = or(-1, 2, 3)"
		, "8 = or(-3, 4)"
		, "9 = or(-3, 5)"
		, "10 = or(-3, 2)"
		, "6 = and(1, 7, 8, 9, 10)"
	});

	@BeforeAll
	static void setup() throws IOException {
		Path tmpDir = Files.createTempDirectory("qcir2pnf");
		tmpDir.toFile().deleteOnExit();

		// lncs example
		inputLNCS = Paths.get(tmpDir.toString(), "lncs.qcir");
		inputLNCS.toFile().deleteOnExit();

		Files.write(
			inputLNCS,
			QCIRTest.lncs.stream()
				.collect(Collectors.joining("\n"))
				.getBytes());

		// g14 example
		inputG14 = Paths.get(tmpDir.toString(), "g14.qcir");
		inputG14.toFile().deleteOnExit();

		Files.write(
			inputG14,
			QCIRTest.g14.stream()
				.collect(Collectors.joining("\n"))
				.getBytes());

		// output file
		output = Paths.get(tmpDir.toString(), "output");
		output.toFile().deleteOnExit();

		// catch calls to System::exit
		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {

				exitCode = status;

				if (status != 0)
					throw new SecurityException(Integer.toString(status));
			}

			@Override
			public void checkPermission(Permission perm) {}
		});

		// prevent output to stdin and stderr
		PrintStream devNull = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {}
		});

		System.setOut(devNull);
		System.setErr(devNull);
	}

	@AfterAll
	static void cleanup() {
		System.setOut(stdout);
		System.setErr(stderr);
	}

	@Test
	@DisplayName("default")
	void test_default() throws IOException {
		QCIR2PNF.main(new String[] {
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsAUEU, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PNF, Files.readAllLines(output));
	}

	@Test
	@DisplayName("prenexing strategy")
	void test_strategy() throws IOException {
		QCIR2PNF.main(new String[] {
			"-s",
			"at.jku.fmv.qbf.pnf.ForAllDownExistsDown",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsADED, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"-s",
			"at.jku.fmv.qbf.pnf.ForAllDownExistsDown",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PNF, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--strategy=at.jku.fmv.qbf.pnf.ForAllDownExistsDown",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsADED, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--strategy=at.jku.fmv.qbf.pnf.ForAllDownExistsDown",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PNF, Files.readAllLines(output));
	}

	@Test
	@DisplayName("cleanse")
	void test_cleanse() throws IOException {
		QCIR2PNF.main(new String[] {
			"--cleanse",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(QCIRTest.lncsCleansed, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--cleanse",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PNFCleansed, Files.readAllLines(output));
	}

	@Test
	@DisplayName("cnf")
	void test_cnf() throws IOException {
		QCIR2PNF.main(new String[] {
			"-c",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsAUEU, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"-c",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PCNF, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--cnf",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsAUEU, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--cnf",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PCNF, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"-c",
			"at.jku.fmv.qbf.pcnf.PG86",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsAUEU, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"-c",
			"at.jku.fmv.qbf.pcnf.PG86",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PCNF, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--cnf=at.jku.fmv.qbf.pcnf.PG86",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsAUEU, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--cnf=at.jku.fmv.qbf.pcnf.PG86",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(g14PCNF, Files.readAllLines(output));
	}

	@Test
	@DisplayName("qdimacs")
	void test_qdimacs() throws IOException {
		QCIR2PNF.main(new String[] {
			"--qdimacs",
			inputLNCS.toString(),
			output.toString()
		});
		assertEquals(lncsQDIMACS, Files.readAllLines(output));

		QCIR2PNF.main(new String[] {
			"--qdimacs",
			inputG14.toString(),
			output.toString()
		});
		assertEquals(QDIMACSTest.g14QDIMACS, Files.readAllLines(output));
	}

	@Test
	@DisplayName("combinations")
	void test_combinations() throws IOException {
		String[][] strategyOpts = {
			{ "-s", "at.jku.fmv.qbf.pnf.ForAllDownExistsDown" },
			{ "--strategy=at.jku.fmv.qbf.pnf.ForAllDownExistsDown" },
		};

		String[][] cnfOpts = {
			{ "-c" },
			{ "-c", "at.jku.fmv.qbf.pcnf.PG86" },
			{ "--cnf" },
			{ "--cnf=at.jku.fmv.qbf.pcnf.PG86" },
		};

		Function<List<String>, String[]> buildArgs = args ->
			Stream.concat(
				args.stream(),
				Stream.of(
					inputG14.toString(),
					output.toString()))
				.toArray(String[]::new);

		for (int i = 0; i < strategyOpts.length; i++) {
			for (int j = 0; j < cnfOpts.length; j++) {
				List<String> args =
					Stream.concat(
						Arrays.stream(strategyOpts[i]),
						Arrays.stream(cnfOpts[j]))
					.collect(Collectors.toList());

				QCIR2PNF.main(buildArgs.apply(args));
				assertEquals(g14PCNF, Files.readAllLines(output));

				args.add("--cleanse");
				QCIR2PNF.main(buildArgs.apply(args));
				assertEquals(g14PCNFCleansed, Files.readAllLines(output));

				args.add("--qdimacs");
				QCIR2PNF.main(buildArgs.apply(args));
				assertEquals(
					QDIMACSTest.g14QDIMACS,
					Files.readAllLines(output));
			}
		}
	}

	@Test
	@DisplayName("illegal arguments")
	void test_illegalArguments() throws IOException {
		// no arguments
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {}));
		assertEquals(1, exitCode);

		// single argument
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] { inputLNCS.toString() }));
		assertEquals(1, exitCode);

		// non-existing input file
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] { "nonexisting.qcir" }));
		assertEquals(1, exitCode);

		// non-existing output file location
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] { "nonexisting/output.qcir" }));
		assertEquals(1, exitCode);

		// illegal option
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"--illegal",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// -s: missing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"-s",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"-s",
				"--cleanse",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// -s: missing in-/output file argument
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"-s",
				output.toString()
			}));
		assertEquals(1, exitCode);

		// -s: non-existing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"-s",
				"non.existing.Strategy",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// --strategy: missing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"--strategy",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"--strategy=",
				"--cleanse",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// -s: non-existing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"--strategy=non.existing.Strategy",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// -c: non-existing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"-c",
				"non.existing.CNFEncoder",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);

		// --cnf: non-existing class
		assertThrows(
			SecurityException.class,
			() -> QCIR2PNF.main(new String[] {
				"--cnf=non.existing.CNFEncoder",
				inputLNCS.toString(),
				output.toString()
			}));
		assertEquals(1, exitCode);
	}
}
