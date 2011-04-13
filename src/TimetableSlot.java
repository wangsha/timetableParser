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
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class TimetableSlot {
	public static String typeLec = "LECTURE";
	public static String typeTut = "TUTORIAL";
	
	String type="";
	String slot="";
	String day="";
	String time_start="";
	String time_end="";
	String venue="";
	String frequency="";
	
	public Element getElement(Document doc) {
		Element tslot = doc.createElement("timetable_slot");
		
		//type
		Element typeN = doc.createElement("type");
		typeN.appendChild(doc.createTextNode(type));
		tslot.appendChild(typeN);
		
		//slot
		Element slotN = doc.createElement("slot");
		slotN.appendChild(doc.createTextNode(slot));
		tslot.appendChild(slotN);
		
		//day
		Element dayN = doc.createElement("day");
		dayN.appendChild(doc.createTextNode(day));
		tslot.appendChild(dayN);
		
		//time_start
		Element time_startN = doc.createElement("time_start");
		time_startN.appendChild(doc.createTextNode(time_start));
		tslot.appendChild(time_startN);
		
		//time_end
		Element time_endN = doc.createElement("time_end");
		time_endN.appendChild(doc.createTextNode(time_end));
		tslot.appendChild(time_endN);
		
		//venue
		Element venueN = doc.createElement("venue");
		venueN.appendChild(doc.createTextNode(venue));
		tslot.appendChild(venueN);
		
		//frequency
		Element frequencyN = doc.createElement("frequency");
		frequencyN.appendChild(doc.createTextNode(frequency));
		tslot.appendChild(frequencyN);
		
		return tslot;
	}
	
	public String toString() {
		return "type: "+type + "\n"+
		"slot: "+ slot + "\n"+
		"day: "+ day +"\n"+
		"time_start: "+ time_start +"\n"+
		"time_end: " + time_end +"\n"+
		"venue: " + venue +"\n"+
		"frequency: "+ frequency;
	}
	
	
}
