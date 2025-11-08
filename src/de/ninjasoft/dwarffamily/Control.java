package de.ninjasoft.dwarffamily;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.SAXException;

public class Control {

    public static ArrayList<String> Races;
    public static String importedFilename;
    public static String activeRaceName;
    public static DebugWindow debugWindow;
    
    public static ArrayList<Dwarf> reimportWithRace(String desiredRace)
    {
        return ImportXML(importedFilename, debugWindow, desiredRace);
    }
    
    public static ArrayList<Dwarf> ImportXML(String filename, DebugWindow debug)
    {
        return ImportXML(filename, debug, "DWARF");
    }
    
    public static ArrayList<Dwarf> ImportXML(String filename, DebugWindow debug, String desiredRace)
    {
        debugWindow = debug;
        debug.progressBar.setIndeterminate(false);

        ArrayList<Dwarf> dwarfs = new ArrayList<Dwarf>();
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            
            // Setup a new eventReader
            InputStream in = new FileInputStream(filename);
            //XMLEventReader eventReader = inputFactory.createXMLEventReader(in, "ISO-8859-6");            
            //FIXED: filter invalid XML characters
            FilteredInputStream fin = new FilteredInputStream(in);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(fin);
            
            int c = 0;
            debug.progressBar.setIndeterminate(true);
            while(eventReader.hasNext())
            {
                eventReader.next();
                c++;
            }
            //System.out.println(c + " top level elements in total.");
            
            eventReader.close();
            in.close();
            fin.close();
            
            Races = new ArrayList<String>();
            
            debug.progressBar.setIndeterminate(false);
            debug.progressBar.setMaximum(c);
            
            in = new FileInputStream(filename);
            //eventReader = inputFactory.createXMLEventReader(in, "ISO-8859-6");            
            //FIXED: filter invalid XML characters
            fin = new FilteredInputStream(in);
            eventReader = inputFactory.createXMLEventReader(fin);
            
            // read the XML document
            Dwarf dwarf = null;
            ArrayList<Integer> children = null;
            c = 0;
            while (eventReader.hasNext()) {
                debug.progressBar.setValue(c++);
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement() && event.asStartElement().getName().getLocalPart() == "historical_figure") {
                    dwarf = new Dwarf();
                    children = new ArrayList<Integer>();
                    Boolean histfig = true;
                    while (histfig) {
                        event = eventReader.nextEvent();
                        if (event.isStartElement()) {
                            if (event.asStartElement().getName().getLocalPart().equals("race")) {
                                event = eventReader.nextEvent();
                                
                                if(!Races.contains(event.asCharacters().getData()))
                                {
                                    Races.add(event.asCharacters().getData());
                                }
                                
                                if (event.asCharacters().getData().contentEquals(desiredRace.toUpperCase())) {
                                    //System.out.println(event.asCharacters().getData());
                                    continue;
                                } else {
                                    break;
                                }
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("name")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setName(event.asCharacters().getData());
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("caste")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setGender(event.asCharacters().getData());
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("birth_seconds72")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setBirthday(secondsToDate(Integer.valueOf(event.asCharacters().getData())));
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("death_seconds72")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setDeathday(secondsToDate(Integer.valueOf(event.asCharacters().getData())));
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("id")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                if (Integer.valueOf(event.asCharacters().getData()) >= 0) {
                                    dwarf.setId(Integer.valueOf(event.asCharacters().getData()));
                                    continue;
                                } else {
                                    break;
                                }

                            }
                            if (event.asStartElement().getName().getLocalPart().equals("death_year")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setDeathyear(event.asCharacters().getData());
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("birth_year")) {
                                event = eventReader.nextEvent();
                                //System.out.println(event.asCharacters().getData());
                                dwarf.setBirthyear(event.asCharacters().getData());
                                continue;
                            }
                            if (event.asStartElement().getName().getLocalPart().equals("link_type")) {
                                event = eventReader.nextEvent();
                                if (event.asCharacters().getData().contentEquals("mother")) {
                                    do {
                                        event = eventReader.nextEvent();
                                    } while (!event.isStartElement());
                                    event = eventReader.nextEvent();
                                    dwarf.setMotherid(Integer.valueOf(event.asCharacters().getData()));
                                    continue;
                                }
                                if (event.asCharacters().getData().contentEquals("father")) {
                                    do {
                                        event = eventReader.nextEvent();
                                    } while (!event.isStartElement());
                                    event = eventReader.nextEvent();
                                    dwarf.setFatherid(Integer.valueOf(event.asCharacters().getData()));
                                    continue;
                                }
                                if (event.asCharacters().getData().contentEquals("spouse")) {
                                    do {
                                        event = eventReader.nextEvent();
                                    } while (!event.isStartElement());
                                    event = eventReader.nextEvent();
                                    dwarf.setSpouseid(Integer.valueOf(event.asCharacters().getData()));
                                    continue;
                                }
                                if (event.asCharacters().getData().contentEquals("child")) {
                                    do {
                                        event = eventReader.nextEvent();
                                    } while (!event.isStartElement());
                                    event = eventReader.nextEvent();
                                    children.add(Integer.valueOf(event.asCharacters().getData()));
                                    continue;
                                }

                            }
                        } else if (event.isEndElement()) {
                            if (event.asEndElement().getName().getLocalPart() == "historical_figure") {
                                histfig = false;
                                dwarf.setChildrenids(children);
                                dwarfs.add(dwarf);
                            }
                        }
                    }

                }

            }
            eventReader.close();
            in.close();
            
            debug.progressBar.setIndeterminate(true);

            for (int i = 0; i < dwarfs.size(); i++) {
                if (dwarfs.get(i).getMotherid() != null) {
                    for (int j = 0; j < dwarfs.size(); j++) {
                        if (dwarfs.get(j).getId().equals(dwarfs.get(i).getMotherid())) {
                            dwarfs.get(i).setMother(dwarfs.get(j));
                            //System.out.println("mother linked");
                            break;
                        }
                    }
                }
                if (dwarfs.get(i).getFatherid() != null) {
                    for (int j = 0; j < dwarfs.size(); j++) {
                        if (dwarfs.get(j).getId().equals(dwarfs.get(i).getFatherid())) {
                            dwarfs.get(i).setFather(dwarfs.get(j));
                            //System.out.println("father linked");
                            break;
                        }
                    }
                }
                if (dwarfs.get(i).getSpouseid() != null) {
                    for (int j = 0; j < dwarfs.size(); j++) {
                        if (dwarfs.get(j).getId().equals(dwarfs.get(i).getSpouseid())) {
                            dwarfs.get(i).setSpouse(dwarfs.get(j));
                            //System.out.println("spouse linked");
                            break;
                        }
                    }
                }
                if (dwarfs.get(i).getChildrenids() != null) {
                    ArrayList<Dwarf> dwarfchildren = new ArrayList<Dwarf>();
                    for (int j = 0; j < dwarfs.size(); j++) {
                        for (int h = 0; h < dwarfs.get(i).getChildrenids().size(); h++) {
                            if (dwarfs.get(j).getId().equals(dwarfs.get(i).getChildrenids().get(h))) {
                                dwarfchildren.add(dwarfs.get(j));
                                //System.out.println("child linked");
                                break;
                            }
                        }
                    }
                    dwarfs.get(i).setChildren(dwarfchildren);
                }
            }
            
            debug.progressBar.setIndeterminate(false);
            debug.progressBar.setValue(0);
        } catch (Exception E) {
            E.printStackTrace();
            JOptionPane.showMessageDialog(null, E.getMessage().toString());
            
            debug.progressBar.setIndeterminate(false);
            debug.progressBar.setValue(0);
            return null;
        }
        
        Collections.sort(Races);
        for(int i=0; i<Races.size(); i++)
        {
            String s = Races.get(i);
            char[] raw = s.toCharArray();
                            
            for(int j=0; j<raw.length; j++)
            {
                if(j==0 || Character.isWhitespace(raw[j-1]))
                {
                    raw[j] = Character.toUpperCase(raw[j]);
                }
                else
                {
                    raw[j] = Character.toLowerCase(raw[j]);
                }
            }

            Races.set(i, String.valueOf(raw));
        }
        importedFilename = filename.substring(0, filename.lastIndexOf('.'));
        activeRaceName = desiredRace;
        
        return dwarfs;
    }

    public static void GedExportTest(ArrayList<Dwarf> dwarfs) throws SAXException, IOException {
        PrintWriter writer = new PrintWriter(importedFilename + ".ged", "UTF-8");
        writer.println("0 HEAD");
		writer.println("1 GEDC");
		writer.println("2 VERS 5.5.1");
		writer.println("2 FORM LINEAGE-LINKED");
		writer.println("1 CHAR UTF-8");
		writer.println("1 LANG English");
		writer.println("1 SOUR MYDWARF");
		writer.println("2 NAME MyDwarf Family Tree Builder");
		writer.println("2 VERS 5.5.1");
		writer.println("2 _RTLSAVE RTL");
		for (int i = 0; i < dwarfs.size(); i++) {
			dwarfs.get(i).getName();
            writer.println("0 @I" + dwarfs.get(i).getId() + "@ INDI");
            writer.println("1 NAME " + dwarfs.get(i).getNewName());
			writer.println("2 GIVN " + dwarfs.get(i).getGIVName());
			writer.println("2 SURN " + dwarfs.get(i).getSURName());
            writer.println("1 SEX " + dwarfs.get(i).getGender().substring(0, 1));
            writer.println("1 BIRT");
            writer.println("2 DATE " + dwarfs.get(i).getBirthday() + " " + dwarfs.get(i).getBirthyear());
            writer.println("1 DEAT");
            writer.println("2 DATE " + dwarfs.get(i).getDeathday() + " " + dwarfs.get(i).getDeathyear());
            writer.println("");
        }
		for (int i = 0; i < dwarfs.size(); i++) {
            if (dwarfs.get(i).getGender().contentEquals("MALE")) {
                if (dwarfs.get(i).getSpouse() != null) {
                    writer.println("0 @F" + dwarfs.get(i).getId() + "@ FAM");
                    writer.println("1 HUSB @I" + dwarfs.get(i).getId() + "@");
                    writer.println("1 WIFE @I" + dwarfs.get(i).getSpouse().getId() + "@");
                    for (int j = 0; j < dwarfs.get(i).getChildren().size(); j++) {
                        writer.println("1 CHIL @I" + dwarfs.get(i).getChildren().get(j).getId() + "@");
                    }
                    writer.println("");
                }
            }
        }
		writer.println("0 TRLR");
        writer.close();

    }

    public static String secondsToDate(int count) {
        String dateString = Integer.toString((count % 33600) / 1200 + 1) + " ";
        int month = count / 33600 + 1;

        switch (month) {
            case 1: {
                dateString += "Granite";
                break;
            }
            case 2: {
                dateString += "Slate";
                break;
            }
            case 3: {
                dateString += "Felsite";
                break;
            }
            case 4: {
                dateString += "Hematite";
                break;
            }
            case 5: {
                dateString += "Malachite";
                break;
            }
            case 6: {
                dateString += "Galena";
                break;
            }
            case 7: {
                dateString += "Limestone";
                break;
            }
            case 8: {
                dateString += "Sandstone";
                break;
            }
            case 9: {
                dateString += "Timber";
                break;
            }
            case 10: {
                dateString += "Moonstone";
                break;
            }
            case 11: {
                dateString += "Opal";
                break;
            }
            case 12: {
                dateString += "Obsidian";
                break;
            }
        }

        return dateString;
    }
}