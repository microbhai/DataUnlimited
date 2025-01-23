package akhil.DataUnlimited.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import akhil.DataUnlimited.model.types.Types;

public class FormatConversion {

	private FormatConversion() {
	}

	private static final Logger lo = LogManager.getLogger(FormatConversion.class.getName());

	public static String jsonToXML(String json) {
		try {
			return prettyPrint("<root>" + XML.toString(new JSONObject(json.trim())) + "</root>", false);
		} catch (Exception e) {
			String msg = "ERROR: Could not convert to XML.";
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg + LogStackTrace.get(e));
			else
				lo.error(msg + LogStackTrace.get(e));
			return null;
		}
	}

	public static boolean isJSONValid(String json) {
		try {
			new JSONObject(json);
			String msg = "Valid JSON Found...";
			if (Types.getInstance().getIsUI())
				DULogger.log(400, msg);
			else
				lo.info(msg);
			return true;
		} catch (JSONException ex) {
			try {
				new JSONArray(json);
				String msg = "Valid JSON Array Found... need a JSON, not array.";
				if (Types.getInstance().getIsUI())
					DULogger.log(400, msg);
				else
					lo.info(msg);
				return false;

			} catch (JSONException ex1) {
				String msg = "Invalid JSON Found...";// + LogStackTrace.get(ex1) + LogStackTrace.get(ex);
				if (Types.getInstance().getIsUI())
					DULogger.log(400, msg);
				else
					lo.info(msg);
				return false;
			}

		}
	}

	public static final boolean isValidXML(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		class SimpleErrorHandler implements ErrorHandler {

			@Override
			public void warning(SAXParseException e) throws SAXException {
				String msg = "Handler -> XML Parsing Error..." + LogStackTrace.get(e);
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);

			}

			@Override
			public void error(SAXParseException e) throws SAXException {
				String msg = "Handler -> XML Parsing Error..." + LogStackTrace.get(e);
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);

			}

			@Override
			public void fatalError(SAXParseException e) throws SAXException {
				String msg = "Handler -> XML Parsing Error..." + LogStackTrace.get(e);
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);
			}

		}

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new SimpleErrorHandler());
			builder.parse(new InputSource(new StringReader(xml)));
			String msg = "Valid XML Found...";
			if (Types.getInstance().getIsUI())
				DULogger.log(400, msg);
			else
				lo.info(msg);
			return true;
		} catch (Exception e) {
			String msg = "XML Parsing Error..." + LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			return false;
		}
	}

	public static final String prettyPrint(String xml) {
		xml = xml.replaceAll("(?m)^[ \t]*\r?\n", "");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xml)));

			TransformerFactory factory = TransformerFactory.newInstance();

			Transformer tf = factory.newTransformer();
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			StringWriter out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			return out.getBuffer().toString();
		} catch (Exception e) {
			String msg = "Pretty Print Exception. Invalid XML.";
			if (Types.getInstance().getIsUI())
				DULogger.log(300, msg);
			else
				lo.warn(msg);
			return xml;
		}

	}

	public static final String prettyPrint(String xml, boolean isJson) {
		if (isJson) {
			try {
				return new JSONObject(xml.trim()).toString(4);
			} catch (JSONException ex) {
				try {
					new JSONArray(xml);
					String msg = "Valid JSON Array Found...";
					if (Types.getInstance().getIsUI())
						DULogger.log(400, msg);
					else
						lo.info(msg);
				} catch (JSONException ex1) {
					String msg = "Invalid JSON Found..." + LogStackTrace.get(ex1) + LogStackTrace.get(ex);
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
				}
				return xml;
			}

		} else {
			xml = xml.replaceAll("(?m)^[ \t]*\r?\n", "");
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(xml)));

				TransformerFactory factory = TransformerFactory.newInstance();

				Transformer tf = factory.newTransformer();
				tf.setOutputProperty(OutputKeys.METHOD, "xml");
				tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				tf.setOutputProperty(OutputKeys.INDENT, "yes");
				tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				StringWriter out = new StringWriter();
				tf.transform(new DOMSource(doc), new StreamResult(out));
				return out.getBuffer().toString();
			} catch (Exception e) {
				String msg = "Pretty Print Exception. Invalid XML.";
				if (Types.getInstance().getIsUI())
					DULogger.log(300, msg);
				else
					lo.warn(msg);
				return xml;
			}
		}
	}

}
