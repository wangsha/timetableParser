/**
 * Copyright (c) 2011-2012
 * Wang Sha
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * 1. The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * 2. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.htmlparser.*;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

public class TimetableParser {

	public static String outfile = "cors.xml";
	public static String moduleurl = "https://aces01.nus.edu.sg/cors/jsp/report/ModuleDetailedInfo.jsp?";

	TimetableModule parseModule(String moduleCode) {
		// debug
		TimetableModule module = new TimetableModule();
		module.code = moduleCode;
		URL url;
		try {
			url = new URL(moduleurl.concat(moduleCode.split(" ")[0]));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line;
			StringBuffer sb = new StringBuffer();
			boolean reachTable = false;
			boolean ignore = true;

			// //
			// extract module description
			// //
			while ((line = reader.readLine()) != null) {
				if (line.contains("Correct as at")) {
					int len = line.trim().length();
					module.last_updated = line.trim().substring(4, len - 5);
				}

				if (line.contains("tableframe")) {
					reachTable = true;
				}
				if (line.contains("<td width=\"70%\"") && reachTable) {
					ignore = false;
				}

				if (line.contains("</table>") && !ignore) {
					ignore = true;
					reachTable = false;
					break;
				}

				if (!ignore && line.trim().length() > 0) {
					sb.append(line);
					sb.append("\n");
				}

			}

			Parser parser = Parser.createParser(sb.toString(), "utf8");
			NodeFilter tdFilter = new TagNameFilter("td");
			NodeList tds = parser.extractAllNodesThatMatch(tdFilter);
			parseModuleInfo(tds, module);
			// //
			// extract lecture slot
			// //
			sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {

				if (line.contains("tableframe")) {
					reachTable = true;
				}
				if (line.contains("<table width=\"80%\"") && reachTable) {
					ignore = false;
				}

				if (line.contains("</div>")) {
					ignore = true;
					break;
				}

				if (!ignore && line.trim().length() > 0) {
					sb.append(line);
					sb.append("\n");
				}

			}
			parser = Parser.createParser(sb.toString(), "utf8");
			tds = parser.extractAllNodesThatMatch(tdFilter);
			TimetableSlot slot = new TimetableSlot();
			for (int i = 0; i < tds.size(); i++) {
				// handle multiple session case
				Node temp = tds.elementAt(i);
				int offset = 0;
				while (temp.getChildren().size() > offset) {
					slot = parseSlot(temp, offset);
					if (slot != null) {
						module.addSlot(slot);
					}
					offset += 4;

				}
			}

			// //
			// extract tutorial slot
			// //
			sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				if (line.contains("tableframe")) {
					reachTable = true;
				}
				if (line.contains("<table width=\"80%\"") && reachTable) {
					ignore = false;
				}

				if (line.contains("</div>")) {
					ignore = true;
					break;
				}

				if (!ignore && line.trim().length() > 0) {
					sb.append(line);
					sb.append("\n");
				}

			}

			parser = Parser.createParser(sb.toString(), "utf8");
			tds = parser.extractAllNodesThatMatch(tdFilter);
			for (int i = 0; i < tds.size(); i++) {
				// handle multiple session case
				Node temp = tds.elementAt(i);
				int offset = 0;
				while (temp.getChildren().size() > offset) {
					slot = parseSlot(temp, offset);
					if (slot != null) {
						module.addSlot(slot);
					}
					offset += 4;

				}
			}
			return module;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("exceptin caught");
			e.printStackTrace();
			return null;
		}
		//return module;
	}

	private void parseModuleInfo(NodeList data, TimetableModule module) {
		// module.code = data.elementAt(0).getFirstChild().toHtml().trim();

		module.title = data.elementAt(2).getFirstChild().toHtml().trim();
		module.description = data.elementAt(4).getFirstChild().toHtml().trim();
		module.examinable = data.elementAt(6).getFirstChild().toHtml().trim();
		module.exam_date = data.elementAt(8).getFirstChild().toHtml().trim();
		module.mc = data.elementAt(10).getFirstChild().toHtml().trim();
		module.prereq = data.elementAt(12).getFirstChild().toHtml().trim();
		module.preclude = data.elementAt(14).getFirstChild().toHtml().trim();
		module.workload = data.elementAt(16).getFirstChild().toHtml().trim();
		module.remarks = data.elementAt(18).getFirstChild().toHtml().trim();

	}

	private TimetableSlot parseSlot(Node data, int offset) {
		TimetableSlot slot = new TimetableSlot();
		try {

			// line 1:
			// case1: LECTURE Class [SL1]
			// case2: TUTORIAL Class [T10]
			// case3: SECTIONAL TEACHING Class [S34]
			// case4: LABORATORY Class [B01]
			String[] tokens = data.getChildren().elementAt(0).toHtml().trim()
					.split("Class");
			slot.type = tokens[0].trim();
			String temp = tokens[1].trim();
			slot.slot = temp.substring(1, temp.length() - 1);

			// line 2:MONDAY From 1200 hrs to 1400 hrs in LT25,
			tokens = data.getChildren().elementAt(offset + 2).toHtml().trim()
					.split(" ");
			slot.day = tokens[0];
			slot.time_start = tokens[2];
			slot.time_end = tokens[5];

			slot.venue = tokens[8].substring(0, tokens[8].length() - 1);

			// line4:Week(s): EVERY WEEK.
			tokens = data.getChildren().elementAt(offset + 4).toHtml().trim()
					.split(" ");
			slot.frequency = tokens[1].concat(" WEEK");
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
		return slot;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TimetableParser tparser = new TimetableParser();

		boolean debug = false;
		String baseurl = "https://aces01.nus.edu.sg/cors/jsp/report/ModuleInfoListing.jsp?fac_c=";
		String[] urls = { "31", "34", "36", "82", "35", "20", "48", "39", "47",
				"32", "10", "3B", "43", "3D" };
		String[] facNames = { "ARTS &amp; SOCIAL SCIENCES", "DENTISTRY",
				"ENGINEERING", "JOINT MULTI-DISCIPLINARY PROGRAMMES", "LAW",
				"NON-FACULTY-BASED DEPARTMENTS", "SCHOOL OF BUSINESS",
				"SCHOOL OF COMPUTING", "SCHOOL OF DESIGN AND ENVIRONMENT",
				"SCIENCE", "UNIVERSITY ADMINISTRATION",
				"UNIVERSITY SCHOLARS PROGRAMME",
				"YONG LOO LIN SCHOOL OF MEDICINE",
				"YONG SIEW TOH CONSERVATORY OF MUSIC" };

		try {
			// write module
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.INDENT, "yes");

			//root
			Element root = document.createElement("modulesinfo");
			//last updated
			root.setAttribute("lastUpdated", Long.toString(System
					.currentTimeMillis()/1000));
			
			if (args.length != 5) {
				System.out.println("Usage: java -jar ttParser.jar semester year " +
						"weekOneDay weekOneMonth weekMidtermBreakAfter adacemic_year");
				System.out.println("Eg: java -jar parser.jar 2 2011 10 1 6 2010/2011");
			}
			
			/*
			 * update these settings accordingly
			 */
			root.setAttribute("semester", args[0]);
			root.setAttribute("year", args[1]);
			root.setAttribute("weekOneDay", args[2]);
			root.setAttribute("weekOneMonth", args[3]);
			root.setAttribute("weekMidtermBreakAfter", args[4]);
			StringBuffer sb = new StringBuffer();
			String line;
			Element faculty;
			
			TimetableParser.moduleurl = TimetableParser.moduleurl
				.concat("acad_y=").concat(args[5])
				.concat("&sem_c=").concat(args[0])
				.concat("&mod_c=");
			for (int i = 0; i < urls.length; i++) {
				faculty = document.createElement("faculty");
				faculty.setAttribute("faculty_id", urls[i]);
				faculty.setAttribute("name", facNames[i]);
				
				URL url = new URL(baseurl.concat(urls[i]));
				System.out.println(url.toString());
				sb = new StringBuffer();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				boolean ignore = true;
				boolean reachTable = false;

				while ((line = reader.readLine()) != null) {
					if (line.contains("tableframe")) {
						reachTable = true;
					}
					if (line.contains("valign=\"top\"") && reachTable) {
						ignore = false;
					}

					if (line.contains("<!-- Footer.jsp -->")) {
						ignore = true;
					}

					if (!ignore && line.trim().length() > 0) {
						sb.append(line);
						sb.append("\n");
					}
				}
				reader.close();

				Parser parser = Parser.createParser(sb.toString(), "utf8");
				NodeFilter divFilter = new TagNameFilter("div");
				NodeList divs = parser.extractAllNodesThatMatch(divFilter);
				System.out.println("totalsize" + divs.size());

				for (int j = 0; j < divs.size(); j++) {
					Node node = divs.elementAt(j);
					switch (j % 9) {
					case 1:// module code
						// TODO get module description
						String moduleCode = node.getChildren().elementAt(1)
								.getFirstChild().toHtml().trim();

						if (moduleCode.length() > 0) {
							System.out.println(moduleCode);
							TimetableModule module = tparser
									.parseModule(moduleCode);
							if(module != null) {
							// System.out.println(module.toString());
							Element moduleN = module.getElement(document);
							faculty.appendChild(moduleN);
							}
						}
						break;
					default:
						;// ignore
					}
				}
				root.appendChild(faculty);
			}
			document.appendChild(root);
			PrintWriter pw = new PrintWriter(new FileOutputStream("out.xml"));
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(pw);
			tf.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
