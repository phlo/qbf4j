package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.io.*;
import at.jku.fmv.qbf.pcnf.CNFEncoder;
import at.jku.fmv.qbf.pcnf.PG86;
import at.jku.fmv.qbf.pnf.ForAllUpExistsUp;
import at.jku.fmv.qbf.pnf.PrenexingStrategy;

public class QCIR2PNF {

	private static final Class<? extends PrenexingStrategy> defaultStrategy =
		ForAllUpExistsUp.class;

	private static final Class<? extends CNFEncoder> defaultEncoder =
		PG86.class;

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

	private static List<String> listClassesPath(URI uri, String pkg) {
		Path pkgPath = Paths.get(uri);
		Path classPath =
			Arrays.stream(pkg.split("\\."))
				.reduce(
					pkgPath,
					(p1, p2) -> p1.getParent(),
					(p1, p2) -> p1.getParent());

		try {
			return Files.walk(pkgPath)
				.filter(path -> path.toString().endsWith(".class"))
				.map(clazzPath -> classPath.relativize(clazzPath))
				.map(clazzPath -> clazzPath.toString()
					.replace("/", ".")
					.replace(".class", ""))
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<String> listClassesJAR(URI uri, String pkg) {
        try (ZipFile zip = new ZipFile(new File(uri))) {
			String pkgPath = pkg.replace(".", "/");
			return zip.stream()
				.filter(ze -> !ze.isDirectory())
				.map(ZipEntry::getName)
				.filter(f -> f.startsWith(pkgPath))
				.filter(f -> f.endsWith(".class"))
				.map(f -> f.replace("/", "."))
				.map(f -> f.replace(".class", ""))
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> List<Class<? extends T>> getImplementations(
		final Class<? extends T> type
	) {
		try {
			String pkg = type.getPackage().getName();

			Enumeration<URL> urls = Thread.currentThread()
				.getContextClassLoader()
				.getResources(pkg.replace('.', '/'));

			List<Class<? extends T>> implementations = new ArrayList<>();

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				(url.getProtocol().equals("jar")
					? listClassesJAR(new URI(url.getFile().split("!")[0]), pkg)
					: listClassesPath(url.toURI(), pkg))
					.stream()
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
					.forEach(implementations::add);
			}

			implementations.sort(
				(c1, c2) -> c1.getName().compareTo(c2.getName()));
			return implementations;

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
				+ clazz.getName()
				+ (clazz == defaultStrategy
					? " (default)"
					: ""))
			.collect(Collectors.joining("\n")) + "\n"
		+ "\n"
		+ "  -c [<class>], --cnf[=<class>]   transform to PCNF, where <class> is the fully\n"
		+ "                                  qualified name of a class implementing the\n"
		+ "                                  CNFEncoder interface, e.g.:\n"
		+ "\n"
		+ encodings.stream()
			.map(clazz ->
				"                                  "
				+ clazz.getName()
				+ (clazz == defaultEncoder
					? " (default)"
					: ""))
			.collect(Collectors.joining("\n")) + "\n"
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
				strategy = defaultStrategy.newInstance();

			if (encoder == null)
				encoder = defaultEncoder.newInstance();

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
