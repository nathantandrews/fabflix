package dev.wdal.importer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StarParser extends AbstractParser {
    private StarManager mgr;
    private String val;
    private Star star;

    public StarParser(StarManager mgr) {
        this.mgr = mgr;
        openLog("stars_errors.log");
    }

    public void parseDocument() {
        super.parseDocument("actors63.xml");
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        val = "";
        if (qName.equalsIgnoreCase("actor")) {
            star = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        val = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            StarKey key = star.getKey();
            if (mgr.hasStarKey(key)) {
                errorLog.println("- duplicate star detected, ignored: " + key.toString());
                errorLog.flush();
                return;
            }
            mgr.add(star);
        } else if (qName.equalsIgnoreCase("stagename")) {
            star.setName(val);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                star.setDob(Integer.parseInt(val));
            }
            catch (NumberFormatException nfe) {
            }
        }

    }

    public static int main(String [] args)
    {
        StarManager mgr = new StarManager();
        StarParser sp = new StarParser(mgr);
        sp.parseDocument();
        mgr.print();
        return 0;
    }
}
