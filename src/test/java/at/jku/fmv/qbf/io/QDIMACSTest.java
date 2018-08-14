package at.jku.fmv.qbf.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.*;
import at.jku.fmv.qbf.QBF.*;
import at.jku.fmv.qbf.io.util.ParserException;

@DisplayName("QDIMACS")
public class QDIMACSTest {

	static Path file;

	// ∃p (∀q ∃r ∀s ∃t ϕ0 ∧ ∀q' ∃r' ϕ1 ∧ ¬∀q'' ∃r'' ϕ2)
	// ∃4 (∀5 ∃6 ∀7 ∃8  1 ∧ ∀9  ∃10  2 ∧ ¬∀11  ∃12   3)
	// ∃p,q'': ∀q,q',r'': ∃r,r': ∀s: ∃t: (1 ∧ 2 ∧ 3)
	// ∃4,11:  ∀5,9,12:   ∃6,10: ∀7: ∃8: (1 ∧ 2 ∧ 3)
	public static QBF lncs =
		new Exists(
			new ForAll(
				new Exists(
					new ForAll(
						new Exists(
							new And(
								new Variable("1"),
								new Variable("2"),
								new Variable("3")),
							"8"),
						"7"),
					"6", "10"),
				"5", "9", "12"),
			"4", "11");
	public static List<String> lncsQDIMACS = Arrays.asList(new String[] {
		"p cnf 12 3"
		, "e 11 4 0"
		, "a 12 5 9 0"
		, "e 6 10 0"
		, "a 7 0"
		, "e 8 0"
		, "1 0"
		, "2 0"
		, "3 0"
	});

	// ∀z: ∃x1,x2: (pg0 ∧ (-_pg0 ∨ z ∨ _pg1) ∧ (-_pg1 ∨ x1) ∧ (-_pg1 ∨ x2) ∧ (-_pg1 ∨ z))
	// ∀2: ∃4,5: (1 ∧ (-1 ∨ 2 ∨ 3) ∧ (-3 ∨ 4) ∧ (-3 ∨ 5) ∧ (-3 ∨ 2))
	public static QBF g14 =
		new ForAll(
			new Exists(
				new And(
					new Variable("1"),
					new Or(
						new Not(new Variable("1")),
						new Variable("2"),
						new Variable("3")),
					new Or(
						new Not(new Variable("3")),
						new Variable("4")),
					new Or(
						new Not(new Variable("3")),
						new Variable("5")),
					new Or(
						new Not(new Variable("3")),
						new Variable("2"))),
				"4", "5"),
			"2");
	public static List<String> g14QDIMACS = Arrays.asList(new String[] {
		"p cnf 5 5"
		, "a 2 0"
		, "e 4 5 0"
		, "1 0"
		, "-1 2 3 0"
		, "-3 4 0"
		, "-3 5 0"
		, "-3 2 0"
	});

	// propositional formula
	public static QBF propositional =
		new And(
			new Variable("1"),
			new Or(new Variable("2"), new Not(new Variable("3"))),
			new Or(new Not(new Variable("2")), new Variable("3")),
			new Not(new Variable("4"))
		);
	public static List<String> propositionalQDIMACS =
		Arrays.asList(new String[] {
			"p cnf 4 4"
			, "1 0"
			, "2 -3 0"
			, "-2 3 0"
			, "-4 0"
		});

	@BeforeAll
	static void setup() throws IOException {
		Path tmpDir = Files.createTempDirectory("qbf");
		tmpDir.toFile().deleteOnExit();

		file = Paths.get(tmpDir.toString(), "formula.qdimacs");
		file.toFile().deleteOnExit();
	}

	@Test
	@DisplayName("read")
	void test_read() throws IOException {
		Files.write(file, lncsQDIMACS);
		assertEquals(lncs, QDIMACS.read(file));

		Files.write(file, g14QDIMACS);
		assertEquals(g14, QDIMACS.read(file));

		Files.write(file, propositionalQDIMACS);
		assertEquals(propositional, QDIMACS.read(file));

		// error: empty input file
		Files.write(file, Collections.emptyList());
		assertEquals(
			file.toString() + ": error: file is empty",
			assertThrows(
				ParserException.class,
				() -> QDIMACS.read(file)).getMessage());

		String illegal;

		// error: only problem line
		illegal = "p cnf 0 0\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": error: missing clauses",
			assertThrows(
				ParserException.class,
				() -> QDIMACS.read(file)).getMessage());

		// error: only prefix
		illegal = "p cnf 1 0\n"
				+ "e 1 0\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": error: missing clauses",
			assertThrows(
				ParserException.class,
				() -> QDIMACS.read(file)).getMessage());

		// error: illegal prefix (no variables)
		illegal = "p cnf 1 1\n"
				+ "e 0\n"
				+ "1 0\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 2: error: missing variables",
			assertThrows(
				ParserException.class,
				() -> QDIMACS.read(file)).getMessage());

		// error: illegal clause (no variables)
		illegal = "p cnf 1 1\n"
				+ "0\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 2: error: missing variables",
			assertThrows(
				ParserException.class,
				() -> QDIMACS.read(file)).getMessage());
	}

	@Test
	@DisplayName("write")
	void test_write() throws IOException {
		QDIMACS.write(lncs, file);
		assertEquals(lncsQDIMACS, Files.readAllLines(file));

		QDIMACS.write(g14, file);
		assertEquals(g14QDIMACS, Files.readAllLines(file));

		QDIMACS.write(propositional, file);
		assertEquals(propositionalQDIMACS, Files.readAllLines(file));

		// error: not in PNF
		assertEquals(
			"skeleton not in CNF",
			assertThrows(
				IllegalArgumentException.class,
				() -> QDIMACS.write(QBFTest.lncs, file)).getMessage());
	}
}
