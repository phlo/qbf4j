package at.jku.fmv.qbf.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.*;
import at.jku.fmv.qbf.io.QCIR;

@DisplayName("QCIR")
public class QCIRTest {

	static Path lncsFile;
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

	static Path g14File;
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

	@BeforeAll
	static void setup() throws IOException {
		Path tmpDir = Files.createTempDirectory("qbf");
		tmpDir.toFile().deleteOnExit();

		lncsFile = Paths.get(tmpDir.toString(), "lncs.qcir");
		lncsFile.toFile().deleteOnExit();

		g14File = Paths.get(tmpDir.toString(), "g14.qcir");
		g14File.toFile().deleteOnExit();
	}

	@Test
	@DisplayName("read")
	void test_read() throws IOException {
		Files.write(g14File, g14);
		assertEquals(QBFTest.g14, QCIR.read(g14File));

		Files.write(g14File, g14Cleansed);
		assertEquals(QBFTest.g14.cleanse(), QCIR.read(g14File));

		Files.write(lncsFile, lncs);
		assertEquals(QBFTest.lncs, QCIR.read(lncsFile));

		Files.write(lncsFile, lncsCleansed);
		assertEquals(QBFTest.lncs.cleanse(), QCIR.read(lncsFile));
	}

	@Test
	@DisplayName("write")
	void test_write() throws IOException {
		QCIR.write(QBFTest.g14, g14File, false);
		assertEquals(g14, Files.readAllLines(g14File));

		QCIR.write(QBFTest.g14.cleanse(), g14File, true);
		assertEquals(g14Cleansed, Files.readAllLines(g14File));

		QCIR.write(QBFTest.lncs, lncsFile, false);
		assertEquals(lncsOut, Files.readAllLines(lncsFile));

		QCIR.write(QBFTest.lncs.cleanse(), lncsFile, true);
		assertEquals(lncsCleansed, Files.readAllLines(lncsFile));
	}
}