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
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class TimetableModule {
	String code="";
	String title="";
	String description="";
	String examinable="";
	String open_book ="Unknown";
	String exam_date="";
	String mc="";
	String prereq="";
	String preclude="";
	String workload="";
	String remarks="";
	String last_updated="";
	LinkedList<TimetableSlot> slots;
	
	public TimetableModule() {
		slots = new LinkedList<TimetableSlot>();
	}
	
	public void addSlot(TimetableSlot title) {
		slots.add(title);
	}
	
	public Element getElement(Document doc) {
		Element module = doc.createElement("module");
		//code
		Element codeN = doc.createElement("code");
		codeN.appendChild(doc.createTextNode(code));
		module.appendChild(codeN);
		
		//title
		Element titleN = doc.createElement("title");
		titleN.appendChild(doc.createTextNode(title));
		module.appendChild(titleN);
		
		//description
		Element descriptionN = doc.createElement("description");
		descriptionN.appendChild(doc.createTextNode(description));
		module.appendChild(descriptionN);
		
		//examinable
		Element examinableN = doc.createElement("examinable");
		examinableN.appendChild(doc.createTextNode(examinable));
		module.appendChild(examinableN);
		
		//open_book
		Element open_bookN = doc.createElement("open_book");
		open_bookN.appendChild(doc.createTextNode(open_book));
		module.appendChild(open_bookN);
		
		//exam_date
		Element exam_dateN = doc.createElement("exam_date");
		exam_dateN.appendChild(doc.createTextNode(exam_date));
		module.appendChild(exam_dateN);
		
		//mc
		Element mcN = doc.createElement("mc");
		mcN.appendChild(doc.createTextNode(mc));
		module.appendChild(mcN);
		
		//prereq
		Element prereqN = doc.createElement("prereq");
		prereqN.appendChild(doc.createTextNode(prereq));
		module.appendChild(prereqN);
		
		//preclude
		Element precludeN = doc.createElement("preclude");
		precludeN.appendChild(doc.createTextNode(preclude));
		module.appendChild(precludeN);
		
		//workload
		Element workloadN = doc.createElement("workload");
		workloadN.appendChild(doc.createTextNode(workload));
		module.appendChild(workloadN);
		
		//remarks
		Element remarksN = doc.createElement("remarks");
		remarksN.appendChild(doc.createTextNode(remarks));
		module.appendChild(remarksN);
		
		//last_updated
		Element last_updatedN = doc.createElement("last_updated");
		last_updatedN.appendChild(doc.createTextNode(last_updated));
		module.appendChild(last_updatedN);
		
		
		for (int i=0; i<slots.size(); i++) {
			module.appendChild(slots.get(i).getElement(doc));
		}
		return module;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder( 
		"code: "+code + "\n"+
		"title: "+ title + "\n"+
		"description: "+ description +"\n"+
		"examinable: "+ examinable +"\n"+
		"open_book: " + open_book +"\n"+
		"exam_date: " + exam_date +"\n"+
		
		"mc: "+ mc +"\n"+
		"prereq: "+ prereq +"\n"+
		"workload: " + workload +"\n"+
		"remarks: " + remarks +"\n"+
		"last_update: "+ last_updated + "\ntutorial slots:");
		
		for(int i=0; i<slots.size(); i++) {
			sb.append(slots.get(i).toString());
		}
		
		return sb.toString();
	}
}
