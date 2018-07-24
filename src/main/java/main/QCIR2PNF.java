package main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.io.*;
import at.jku.fmv.qbf.pcnf.CNFEncoder;
import at.jku.fmv.qbf.pnf.PrenexingStrategy;

public class QCIR2PNF {

	private static void error(int status, String msg) {
		System.err.println("error: " + msg);
		System.out.println(usage);
		System.exit(status);
	}

	public static <T> T instanceOf(
		final String name,
		final Class<? extends T> type
	) throws
		ClassNotFoundException,
		InvocationTargetException,
		InstantiationException,
		NoSuchMethodException,
		IllegalAccessException
	{
		return Class.forName(name).asSubclass(type).newInstance();
	}

	private static <T> List<Class<? extends T>> getImplementations(
		final Class<? extends T> type
	) {
		try {
			Enumeration<URL> urls = Thread.currentThread()
				.getContextClassLoader()
				.getResources(type.getPackage().getName()
					.replace(PrenexingStrategy.class.getSimpleName(), "")
					.replace('.', '/'));

			List<Class<? extends T>> classes = new ArrayList<>();

			while (urls.hasMoreElements()) {
				Path pkgPath = Paths.get(urls.nextElement().toURI());

				Path classPath =
					Arrays.stream(type.getPackage().getName().split("\\."))
						.reduce(
							pkgPath,
							(p1, p2) -> p1.getParent(),
							(p1, p2) -> p1.getParent());

				Files.walk(pkgPath)
					.filter(path -> path.toString().endsWith(".class"))
					.map(clazzPath -> classPath.relativize(clazzPath))
					.map(clazzPath -> clazzPath.toString()
						.replace("/", ".")
						.replaceAll(".class$", ""))
					.map(clazzName -> {
						Class<? extends T> clazz;
						try {
							clazz = Class.forName(clazzName).asSubclass(type);
						} catch (Exception e) {
							clazz = null;
						}
						return Optional.ofNullable(clazz);
					})
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(clazz ->
						!clazz.isInterface()
						&& !clazz.isLocalClass()
						&& !Modifier.isAbstract(clazz.getModifiers()))
					.forEach(classes::add);
			}

			return classes;

		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Class<? extends PrenexingStrategy>> strategies =
		getImplementations(PrenexingStrategy.class);

	private static List<Class<? extends CNFEncoder>> encodings =
		getImplementations(CNFEncoder.class);

	private static String usage =
		"Usage: qcir2pnf [OPTION]... <input-file> <output-file>\n"
		+ "\n"
		+ "  -s <class>, --strategy=<class>  prenexing strategy to apply, where <class>\n"
		+ "                                  is the fully qualified name of a class\n"
		+ "                                  implementing the PrenexingStrategy interface,\n"
		+ "                                  e.g.:\n"
		+ "\n"
		+ strategies.stream()
			.map(clazz ->
				"                                  "
				+ clazz.getName())
			.collect(Collectors.joining("\n"))
			+ " (default)\n"
		+ "\n"
		+ "  -c [<class>], --cnf[=<class>]   transform to PCNF, where <class> is the fully\n"
		+ "                                  qualified name of a class implementing the\n"
		+ "                                  CNFEncoder interface, e.g.:\n"
		+ "\n"
		+ encodings.stream()
			.map(clazz ->
				"                                  "
				+ clazz.getName())
			.collect(Collectors.joining("\n"))
			+ " (default)\n"
		+ "\n"
		+ "  --cleanse                       cleanse formula\n"
		+ "\n"
		+ "  --qdimacs                       output formula in QDIMACS format\n";

	public static void main(String[] args) {
		if (args.length < 2)
			error(1, "missing arguments");

		try {
			PrenexingStrategy strategy = null;
			CNFEncoder encoder = null;

			boolean toPCNF = false;
			boolean toQDIMACS = false;
			boolean cleanse = false;

			for (int i = 0; i < args.length - 2; i++) {
				String[] opt = args[i].split("=");
				switch(opt[0]) {
				case "--strategy":
					if (opt.length < 2)
						error(1, opt[0] + " missing prenexing strategy class");
				case "-s":
					strategy = opt.length > 1
						? instanceOf(opt[1], PrenexingStrategy.class)
						: instanceOf(args[++i], PrenexingStrategy.class);
					break;

				case "-c":
				case "--cnf":
					toPCNF = true;
					if (opt.length > 1)
						encoder = instanceOf(opt[1], CNFEncoder.class);
					else if (i < args.length - 3 && !args[i + 1].startsWith("-"))
						encoder = instanceOf(args[++i], CNFEncoder.class);
					break;

				case "--qdimacs":
					toQDIMACS = true;
				case "--cleanse":
					cleanse = true;
					break;
				default:
					error(1, "unknown argument " + opt[0]);
				}
			}

			if (strategy == null)
				strategy = strategies.get(strategies.size() - 1).newInstance();

			if (encoder == null)
				encoder = encodings.get(encodings.size() - 1).newInstance();

			Path inFile = Paths.get(args[args.length - 2]);
			Path outFile = Paths.get(args[args.length - 1]);

			QBF formula = QCIR.read(inFile);

			formula = toPCNF || toQDIMACS
				? formula.toPCNF(strategy, encoder)
				: formula.toPNF(strategy);

			if (cleanse)
				formula = formula.cleanse();

			if (toQDIMACS)
				QDIMACS.write(formula, outFile);
			else
				QCIR.write(formula, outFile, cleanse);

		} catch (IOException e) {
			error(1, e.getMessage() + " file not found");
		} catch (IllegalAccessException | ClassNotFoundException e) {
			error(1, e.getMessage() + " class not found");
		} catch (
			InstantiationException
			| InvocationTargetException
			| NoSuchMethodException
			e
		) {
			error(1, "unable to instantiate " + e.getMessage());
		}
	}
}
