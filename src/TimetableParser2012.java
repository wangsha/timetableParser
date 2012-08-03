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

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TimetableParser2012 {

	public static String outfile = "cors.xml";
	public static String moduleurl = "https://aces01.nus.edu.sg/cors/jsp/report/ModuleDetailedInfo.jsp?";
	NodeFilter tdFilter = new TagNameFilter("td");
	NodeFilter trFilter = new TagNameFilter("tr");
	TimetableModule parseModule(String moduleCode) {
		// debug
		//moduleCode = "AR1101";
		TimetableModule module = new TimetableModule();
		module.code = moduleCode;
		URL url;
		int tryAgain = 0;
		while (tryAgain < 3) {
			tryAgain++;
			try {
				System.out.println(moduleurl.concat(moduleCode.split(" ")[0]
					.trim()));
				url = new URL(moduleurl.concat(moduleCode.split(" ")[0]
				                             						.trim()));
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
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
						System.out.println("last update"+ module.last_updated);
					}

					if (line.contains("tableframe")) {
						reachTable = true;
					}
					if (line.contains("<td width=\"65%\"") && reachTable) {
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
					if (line.contains("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\"") && reachTable) {
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
				tds = parser.extractAllNodesThatMatch(trFilter);
				
				try{
					parseLecture(tds, module);
				}catch (Exception e){}

				// //
				// extract tutorial slot
				// //
				sb = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					if (line.contains("tableframe")) {
						reachTable = true;
					}
					if (line.contains("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\"") && reachTable) {
						ignore = false;
					}

					if (line.contains("Footer.jsp")) {
						ignore = true;
						break;
					}

					if (!ignore && line.trim().length() > 0) {
						sb.append(line);
						sb.append("\n");
					}

				}

				//System.out.println(sb.toString());
				parser = Parser.createParser(sb.toString(), "utf8");
				NodeList trs = parser.extractAllNodesThatMatch(trFilter);
				//printNodeList(trs);
				
				try{
					parseTutorial(trs, module);
				}catch (Exception e) {

				}
				
				
				System.out.println("succeed with "+tryAgain+ " try" + module.toString());
				
				return module;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("exception caught, try again "+tryAgain);
				e.printStackTrace();
			}
		}
		System.out.println("tried "+tryAgain+ " times, give up.");
		return null;
	}
	
	private void parseLecture(NodeList rows, TimetableModule module) {

		for(int i = 1; i<rows.size(); i++) {
			//printNodeList(rows.elementAt(i).getChildren());

			try {
				Parser parser = Parser.createParser(rows.elementAt(i).toHtml(), "utf8");
				NodeList tds = parser.extractAllNodesThatMatch(tdFilter);
				printNodeList2(tds);

				if(tds.size() == 7) {
					TimetableSlot slot = new TimetableSlot();
					slot.slot = tds.elementAt(0).getFirstChild().toHtml().trim();
					slot.type =  tds.elementAt(1).getFirstChild().toHtml().trim();

					slot.day = tds.elementAt(3).getFirstChild().toHtml().trim();

					slot.time_start = tds.elementAt(4).getFirstChild().toHtml().trim();
					slot.time_end = tds.elementAt(5).getFirstChild().toHtml().trim();

					slot.venue = tds.elementAt(6).getFirstChild().toHtml().trim();
				
					// line4:Week(s): EVERY WEEK.
					String[] tokens = tds.elementAt(2).getFirstChild().toHtml().trim()
							.split("&nbsp;");
					slot.frequency = tokens[0].concat(" WEEK");
					module.addSlot(slot);
				}
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//System.exit(0);
	}

	private void parseTutorial(NodeList rows, TimetableModule module) {
		//System.out.println("size"+rows.size());
		for(int i = 1; i<rows.size(); i++) {
			//printNodeList(rows.elementAt(i).getChildren());
			//System.out.println("i:"+i);
			try {
				Parser parser = Parser.createParser(rows.elementAt(i).toHtml(), "utf8");
				NodeList tds = parser.extractAllNodesThatMatch(tdFilter);
				//printNodeList2(tds);
				//System.out.println(tds.size());
				if(tds.size() >= 8) {
					TimetableSlot slot = new TimetableSlot();
					slot.slot = tds.elementAt(0).getFirstChild().toHtml().trim();
					slot.type =  tds.elementAt(1).getFirstChild().toHtml().trim();

					slot.day = tds.elementAt(3).getFirstChild().toHtml().trim();

					slot.time_start = tds.elementAt(4).getFirstChild().toHtml().trim();
					slot.time_end = tds.elementAt(5).getFirstChild().toHtml().trim();

					slot.venue = tds.elementAt(6).getFirstChild().toHtml().trim();
				
					// line4:Week(s): EVERY WEEK.
					String[] tokens = tds.elementAt(2).getFirstChild().toHtml().trim()
							.split("&nbsp;");
					slot.frequency = tokens[0].concat(" WEEK");
					module.addSlot(slot);
					//System.out.println("["+i+"]"+slot);
				}
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		//System.exit(0);
	}

	private void parseModuleInfo(NodeList data, TimetableModule module) {
		// module.code = data.elementAt(0).getFirstChild().toHtml().trim();

		// System.out.println("title "+module.title);
		//printNodeList(data);
		module.title = data.elementAt(3).getFirstChild().toHtml().trim();

		module.description = data.elementAt(5).getFirstChild().toHtml().trim();
		module.examinable = data.elementAt(7).getFirstChild().toHtml().trim();
		module.exam_date = data.elementAt(9).getFirstChild().toHtml().trim();
		module.mc = data.elementAt(11).getFirstChild().toHtml().trim();
		module.prereq = data.elementAt(13).getFirstChild().toHtml().trim();
		module.preclude = data.elementAt(15).getFirstChild().toHtml().trim();
		module.workload = data.elementAt(17).getFirstChild().toHtml().trim();
		module.remarks = data.elementAt(19).getFirstChild().toHtml().trim();

	}
	
	private void printNodeList2(NodeList data) {
		for (int i=0; i<data.size(); i++) {
			System.out.println("["+i+"]" + data.elementAt(i).getFirstChild().toHtml().trim());
		}
	}

	private void printNodeList(NodeList data) {
		for (int i=0; i<data.size(); i++) {
			System.out.println("["+i+"]" + data.elementAt(i).toHtml().trim());
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TimetableParser2012 tparser = new TimetableParser2012();

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

			// root
			Element root = document.createElement("modulesinfo");
			// last updated
			root.setAttribute("lastUpdated", Long.toString(System
					.currentTimeMillis() / 1000));

			if (args.length != 6) {
				System.out
						.println("Usage: java -jar ttParser.jar semester year "
								+ "weekOneDay weekOneMonth weekMidtermBreakAfter adacemic_year");
				System.out
						.println("Eg: java -jar parser.jar 1 2012 13 8 6 2012/2013");
				
				args = new String[6];
				args[0] = "1";
				args[1] = "2012";
				args[2] = "13";
				args[3] = "8";
				args[4] = "6";
				args[5] = "2012/2013";

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

			TimetableParser2012.moduleurl = TimetableParser2012.moduleurl.concat(
					"acad_y=").concat(args[5]).concat("&sem_c=")
					.concat(args[0]).concat("&mod_c=");
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
				System.out.println("total faculty modules: " + divs.size());

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
							if (module != null) {
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
