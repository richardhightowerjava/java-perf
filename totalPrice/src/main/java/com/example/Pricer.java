package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This code was written to show what not to do.
 * Essentially this code reads a very large DOM file into memory thus consuming a lot of memory.
 * @author Rick Hightower
 *
 */
@WebServlet("/pricer")
public class Pricer extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			InputStream stream = request.getInputStream();
			PrintWriter writer = response.getWriter();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
			 db.setErrorHandler(
			            new PricerErrorHandler(writer, response));
			    

			Document doc = db.parse(new InputSource(stream));
			Node pricesNode = doc.getFirstChild();
			NodeList childNodes = pricesNode.getChildNodes();
			float total = 0.0f;
			
			for (int index = 0; index < childNodes.getLength(); index++) {
				Node item = childNodes.item(index);
				if (item.getNodeName() == "price") {
					Node priceNode = item;
					NodeList priceChildNodes = priceNode.getChildNodes();
					int qty = 0;
					float price = 0.0f;
					for (int pindex = 0; pindex < priceChildNodes.getLength(); pindex++) {
						Node pitem = priceChildNodes.item(pindex);
						if (pitem.getNodeName()=="qty") {
							qty = Integer.parseInt(pitem.getTextContent());
							
						} else if (pitem.getNodeName()=="price") {
							price = Float.parseFloat(pitem.getTextContent());
						}
						total += (qty * price);
					}
				}
			}
			
			writer.printf("Your total is %2.2f\n", total);
			System.out.printf("Your total is %2.2f\n", total);
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

	}

	private static class PricerErrorHandler implements ErrorHandler {

		private PrintWriter out;
		private HttpServletResponse response;

		PricerErrorHandler(PrintWriter out, HttpServletResponse response) {
			this.out = out;
			this.response = response;
		}

		private String getParseExceptionInfo(SAXParseException spe) {
			String systemId = spe.getSystemId();
			
			if (systemId == null) {
				systemId = "null";
			}
			String info = "URI=" + systemId + " Line=" + spe.getLineNumber()
					+ ": " + spe.getMessage();
			return info;
		}

		public void warning(SAXParseException spe) throws SAXException {
			out.println("Warning: " + getParseExceptionInfo(spe));
			spe.printStackTrace(out);
		}

		public void error(SAXParseException spe) throws SAXException {
			String message = "Error: " + getParseExceptionInfo(spe);
			out.println(message);
			spe.printStackTrace(out);
		}

		public void fatalError(SAXParseException spe) throws SAXException {
			String message = "Fatal Error: " + getParseExceptionInfo(spe);
			out.println(message);
			spe.printStackTrace(out);
			response.setStatus(501);
		}
	}

}
