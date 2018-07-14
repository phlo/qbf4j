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
import at.jku.fmv.qbf.io.QCIR;
import at.jku.fmv.qbf.io.util.ParserException;

@DisplayName("QCIR")
public class QCIRTest {

	static Path file;

	static List<String> lncs = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "exists(p)"
		, "output(ϕ)"
		, "ϕ0_4 = exists(t; ϕ0)"
		, "ϕ0_3 = forall(s; ϕ0_4"
		, "ϕ0_2 = exists(r; ϕ0_3"
		, "ϕ0_1 = forall(q; ϕ0_2"
		, "ϕ1_2 = exists(r'; ϕ1)"
		, "ϕ1_1 = forall(q'; ϕ1_2)"
		, "ϕ2_2 = exists(r''; ϕ2)"
		, "ϕ2_1 = forall(q''; ϕ2_2)"
		, "ϕ = and(ϕ0_1, ϕ1_1, -ϕ2_1)"
	});

	static List<String> lncsOut = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "exists(p)"
		, "output(13)"
		, "14 = exists(t; ϕ0)"
		, "15 = forall(s; 14)"
		, "16 = exists(r; 15)"
		, "17 = forall(q; 16)"
		, "18 = exists(r'; ϕ1)"
		, "19 = forall(q'; 18)"
		, "20 = exists(r''; ϕ2)"
		, "21 = forall(q''; 20)"
		, "13 = and(17, 19, -21)"
	});

	static List<String> lncsCleansed = Arrays.asList(new String[] {
		"#QCIR-G14 4"
		, "output(4)"
		, "4 = and(1, 2, -3)"
	});

	static List<String> g14 = Arrays.asList(new String[] {
		"#QCIR-G14"
		, "forall(z)"
		, "output(4)"
		, "5 = and(x1, x2, z)"
		, "6 = exists(x1, x2; 5)"
		, "4 = or(z, 6)"
	});

	static List<String> g14Cleansed = Arrays.asList(new String[] {
		"#QCIR-G14 6"
		, "forall(1)"
		, "output(4)"
		, "5 = and(2, 3, 1)"
		, "6 = exists(2, 3; 5)"
		, "4 = or(1, 6)"
	});

	static List<String> propositional = Arrays.asList(new String[] {
		"#QCIR-G14 7"
		, "output(5)"
		, "6 = or(2, -3)"
		, "7 = or(-2, 3)"
		, "5 = and(1, 6, 7, -4)"
	});

	@BeforeAll
	static void setup() throws IOException {
		Path tmpDir = Files.createTempDirectory("qbf");
		tmpDir.toFile().deleteOnExit();

		file = Paths.get(tmpDir.toString(), "formula.qcir");
		file.toFile().deleteOnExit();
	}

	@Test
	@DisplayName("read")
	void test_read() throws IOException {
		Files.write(file, g14);
		assertEquals(QBFTest.g14, QCIR.read(file));

		Files.write(file, g14Cleansed);
		assertEquals(QBFTest.g14.cleanse(), QCIR.read(file));

		Files.write(file, lncs);
		assertEquals(QBFTest.lncs, QCIR.read(file));

		Files.write(file, lncsCleansed);
		assertEquals(QBFTest.lncs.cleanse(), QCIR.read(file));

		Files.write(file, propositional);
		assertEquals(QDIMACSTest.propositional, QCIR.read(file));

		// error: empty input file
		Files.write(file, Collections.emptyList());
		assertEquals(
			file.toString() + ": error: file is empty",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		String illegal;

		// error: only format-id
		illegal = "#QCIR-G14\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": error: missing output",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: only prefix
		illegal = "#QCIR-G14\n"
			+ "exists(1)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": error: missing output",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal prefix (no variables)
		illegal = "#QCIR-G14\n"
			+ "exists()\n"
			+ "output(3)\n"
			+ "3 = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 2: error: missing operands",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal prefix (missing left parentheses)
		illegal = "#QCIR-G14\n"
			+ "exists1, 2)\n"
			+ "output(3)\n"
			+ "3 = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 2: error: missing operands",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// ignored: illegal prefix (missing right parentheses)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2\n"
			+ "output(3)\n"
			+ "3 = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals("∃1,2: (1 ∨ 2)", QCIR.read(file).toString());

		// error: illegal output (missing variable)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output()\n"
			+ "3 = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 3: error: illegal output",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (unknown type)
		illegal = "#QCIR-G14\n"
			+ "none(1, 2)\n"
			+ "output(3)\n"
			+ "3 = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 2: error: unknown gate type 'none'",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (no variables)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ "3 = or()\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: missing operands",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (missing assignment)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ "3 or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: illegal gate definition",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (missing lhs)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ " = or(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: illegal gate definition",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (missing rhs)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ "3 = \n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: illegal gate definition",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (missing operands)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ "3 = or()\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: missing operands",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());

		// error: illegal gate (unknown type)
		illegal = "#QCIR-G14\n"
			+ "exists(1, 2)\n"
			+ "output(3)\n"
			+ "3 = xor(1, 2)\n";
		Files.write(file, illegal.getBytes());
		assertEquals(
			file.toString() + ": 4: error: unknown gate type 'xor'",
			assertThrows(
				ParserException.class,
				() -> QCIR.read(file)).getMessage());
	}

	@Test
	@DisplayName("write")
	void test_write() throws IOException {
		QCIR.write(QBFTest.g14, file, false);
		assertEquals(g14, Files.readAllLines(file));

		QCIR.write(QBFTest.g14.cleanse(), file, true);
		assertEquals(g14Cleansed, Files.readAllLines(file));

		QCIR.write(QBFTest.lncs, file, false);
		assertEquals(lncsOut, Files.readAllLines(file));

		QCIR.write(QBFTest.lncs.cleanse(), file, true);
		assertEquals(lncsCleansed, Files.readAllLines(file));

		QCIR.write(QDIMACSTest.propositional, file, true);
		assertEquals(propositional, Files.readAllLines(file));
	}
}
