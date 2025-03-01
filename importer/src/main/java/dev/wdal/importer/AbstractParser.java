package dev.wdal.importer;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AbstractParser extends DefaultHandler {
    protected PrintWriter errorLog;
    protected AbstractParser()
    {
        ImporterCleaner.getCleaner().register(this, this::cleanup);
    }
    public void openLog(String filename)
    {
        try
        {
            errorLog = new PrintWriter(new FileOutputStream(filename));
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    public void cleanup()
    {
        if (errorLog != null)
        {
            errorLog.flush();
            errorLog.close();
        }
    }
    public void parseDocument(String filename)
    {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try
        {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(filename, this);
        }
        catch (SAXException se)
        {
            se.printStackTrace();
        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
    }
}
