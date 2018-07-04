package at.jku.fmv.qbf.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.*;
import at.jku.fmv.qbf.io.QCIR;

@DisplayName("QCIR")
class QCIRTest {

	private static String examplesDir = "test/formulas/";

	private static Path lncsPath = Paths.get(examplesDir + "lncs.qcir");
	private static Path g14Path = Paths.get(examplesDir + "g14.qcir");
	private static Path g14CleansedPath = Paths.get(examplesDir + "g14-cleansed.qcir");

	@Test
	@DisplayName("read")
	void test_read() {
		QBF formula;

		try {

			formula = QCIR.read(g14Path);
			assertEquals(
				"∀z: (z ∨ ∃x1,x2: (x1 ∧ x2 ∧ z))",
				formula.toString());

			formula = QCIR.read(g14CleansedPath);
			assertEquals(
				"∀3: (3 ∨ ∃1,2: (1 ∧ 2 ∧ 3))",
				formula.toString());

			formula = QCIR.read(lncsPath);
			assertEquals(
				"∃p: (∀q: ∃r: ∀s: ∃t: ϕ0 ∧ ∀q1: ∃r1: ϕ1 ∧ -∀q2: ∃r2: ϕ2)",
				formula.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("write")
	void test_write() {
		Path tmpFile;
		QBF formula;

		String tmpDir = "/tmp/";

		try {
			formula = QCIR.read(g14Path);
			tmpFile = Paths.get(tmpDir + g14Path.getFileName().toString());
			QCIR.write(formula, tmpFile.toAbsolutePath().toString(), false);
			assertEquals(
				"#QCIR-G14\n"
				+ "forall(z)\n"
				+ "output(4)\n"
				+ "5 = and(x1, x2, z)\n"
				+ "6 = exists(x1, x2; 5)\n"
				+ "4 = or(z, 6)\n",
				Files.readAllLines(tmpFile).stream()
					.collect(Collectors.joining("\n")) + "\n");

			formula = QCIR.read(g14CleansedPath);
			tmpFile = Paths.get(tmpDir + g14CleansedPath.getFileName().toString());
			QCIR.write(formula, tmpFile.toAbsolutePath().toString(), true);
			assertEquals(
				"#QCIR-G14 6\n"
				+ "forall(3)\n"
				+ "output(4)\n"
				+ "5 = and(1, 2, 3)\n"
				+ "6 = exists(1, 2; 5)\n"
				+ "4 = or(3, 6)\n",
				Files.readAllLines(tmpFile).stream()
					.collect(Collectors.joining("\n")) + "\n");

			formula = QCIR.read(lncsPath);
			tmpFile = Paths.get(tmpDir + lncsPath.getFileName().toString());
			QCIR.write(formula, tmpFile.toAbsolutePath().toString(), true);
			assertEquals(
					"#QCIR-G14 21\n"
					+ "exists(p)\n"
					+ "output(13)\n"
					+ "14 = exists(t; ϕ0)\n"
					+ "15 = forall(s; 14)\n"
					+ "16 = exists(r; 15)\n"
					+ "17 = forall(q; 16)\n"
					+ "18 = exists(r1; ϕ1)\n"
					+ "19 = forall(q1; 18)\n"
					+ "20 = exists(r2; ϕ2)\n"
					+ "21 = forall(q2; 20)\n"
					+ "13 = and(17, 19, -21)\n",
				Files.readAllLines(tmpFile).stream()
					.collect(Collectors.joining("\n")) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}