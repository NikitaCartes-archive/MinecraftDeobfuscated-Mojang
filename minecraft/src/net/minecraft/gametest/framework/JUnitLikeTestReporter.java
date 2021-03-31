package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitLikeTestReporter implements TestReporter {
	private final Document document;
	private final Element testSuite;
	private final Stopwatch stopwatch;
	private final File destination;

	public JUnitLikeTestReporter(File file) throws ParserConfigurationException {
		this.destination = file;
		this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		this.testSuite = this.document.createElement("testsuite");
		Element element = this.document.createElement("testsuite");
		element.appendChild(this.testSuite);
		this.document.appendChild(element);
		this.testSuite.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
		this.stopwatch = Stopwatch.createStarted();
	}

	private Element createTestCase(GameTestInfo gameTestInfo, String string) {
		Element element = this.document.createElement("testcase");
		element.setAttribute("name", string);
		element.setAttribute("classname", gameTestInfo.getStructureName());
		element.setAttribute("time", String.valueOf((double)gameTestInfo.getRunTime() / 1000.0));
		this.testSuite.appendChild(element);
		return element;
	}

	@Override
	public void onTestFailed(GameTestInfo gameTestInfo) {
		String string = gameTestInfo.getTestName();
		String string2 = gameTestInfo.getError().getMessage();
		Element element;
		if (gameTestInfo.isRequired()) {
			element = this.document.createElement("failure");
			element.setAttribute("message", string2);
		} else {
			element = this.document.createElement("skipped");
			element.setAttribute("message", string2);
		}

		Element element2 = this.createTestCase(gameTestInfo, string);
		element2.appendChild(element);
	}

	@Override
	public void onTestSuccess(GameTestInfo gameTestInfo) {
		String string = gameTestInfo.getTestName();
		this.createTestCase(gameTestInfo, string);
	}

	@Override
	public void finish() {
		this.stopwatch.stop();
		this.testSuite.setAttribute("time", String.valueOf((double)this.stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0));

		try {
			this.save(this.destination);
		} catch (TransformerException var2) {
			throw new Error("Couldn't save test report", var2);
		}
	}

	public void save(File file) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource dOMSource = new DOMSource(this.document);
		StreamResult streamResult = new StreamResult(file);
		transformer.transform(dOMSource, streamResult);
	}
}
