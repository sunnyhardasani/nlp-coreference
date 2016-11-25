import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Created by sunny on 11/7/16.
 */
public class PrintXML {
    List<List<NPObj>> clustersToCheckForRef;
    List<String> listOfConsideredIDs;
    List<String> listNegIDs;

    public PrintXML() {
        this.clustersToCheckForRef = new ArrayList<List<NPObj>>();
    }


    public void print(Document xml) throws Exception {
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            //tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(xml), new StreamResult(out));
//            System.out.println(out.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public String getReferenceID(String ID){

        boolean flag = false;
        int smallestPos = Integer.MAX_VALUE;
        NPObj smallestPosNPObj = new NPObj("", "", "", 0);
        List<NPObj> saveCluster = new ArrayList<NPObj>();


        for(List<NPObj> cluster : this.clustersToCheckForRef){
            if(cluster.size() > 1)
                for(NPObj npObj: cluster){
                    if(npObj.getID().equals(ID)){
                        flag = true;
                        saveCluster = cluster;
                        break;
                    }
                }
            if (flag) break;
        }

        if(flag){

            for(NPObj npObj: saveCluster){

                if(this.listOfConsideredIDs.contains(npObj.getID())
                        && npObj.getPos() < smallestPos
                        && !npObj.getID().equals(ID)) {

                    smallestPos = npObj.getPos();
                    smallestPosNPObj = npObj;
                }
            }
        }

        return smallestPosNPObj.getID();
    }


    public Document print(List<List<NPObj>> clusters, String sentenceWithXML){

        Document doc;
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

        doc = builder.newDocument();

        sentenceWithXML = sentenceWithXML.replaceAll("<SENT>","");
        sentenceWithXML = sentenceWithXML.replaceAll("</SENT>","");
//        sentenceWithXML = sentenceWithXML.replaceAll("<TXT>","");
//        sentenceWithXML = sentenceWithXML.replaceAll("</TXT>","");

        this.listOfConsideredIDs = new ArrayList<String>();
        NPObj arrNPObj[] = new NPObj[clusters.size()+1];

        //System.out.println("------------------------------");
        //System.out.println(clusters.size());


        //if cluster size is greater
        for(List<NPObj> cluster: clusters){
            if(cluster.size() > 1){
                //check whether we have to consider this cluster
                //if required by the document
                boolean flag = false;
                for(NPObj npobj: cluster){
                    if(Integer.parseInt(npobj.getID()) > 0){
                        flag = true;

                        if(!this.clustersToCheckForRef.contains(cluster))
                            this.clustersToCheckForRef.add(cluster);

                        break;
                    }
                }
                if(flag) {
                    for(NPObj npobj: cluster){
                        if(Integer.parseInt(npobj.getID()) < 0) {
                            arrNPObj[npobj.getPos()] = npobj;
                        }
                    }
                }
            }
        }


        //add all the positive id
        for(List<NPObj> cluster : this.clustersToCheckForRef){
            for(NPObj npObj: cluster){
                if(Integer.parseInt(npObj.getID()) > 0) {
                    if (!listOfConsideredIDs.contains(npObj.getID())) {
                        this.listOfConsideredIDs.add(npObj.getID());
                    }
                }
            }
        }

        String newXML = "";
        String remainingXML = sentenceWithXML;
        for(int arrIndex = 0; arrIndex < arrNPObj.length ; arrIndex++){
            NPObj npObj = arrNPObj[arrIndex];
            if(npObj != null){

                int startLoc = remainingXML.toLowerCase().indexOf(npObj.getStrNP().toLowerCase());
                if(startLoc >= 0) {
                    int endloc = startLoc + npObj.getStrNP().length();
                    String xml = remainingXML.substring(0, endloc);

                    if( getSubtringCount(xml,"COREF") % 2 == 0 ) {

                        remainingXML = remainingXML.substring(endloc);
                        String corefXML = xml.substring(0,startLoc)
                                        + "<COREF ID=\"" + npObj.getID()
                                        + "\">"
                                        + xml.substring(startLoc, startLoc + npObj.getStrNP().length())
                                        + "</COREF>"
                                        + xml.substring(endloc);

                        newXML += corefXML;

                        listOfConsideredIDs.add(npObj.getID());
                    }
                }
            }
        }
        newXML += remainingXML;

        try{
            PrintWriter writer = new PrintWriter("check.txt", "UTF-8");
            writer.println(newXML);
            writer.close();
        } catch (Exception e) {
            // do something
            e.printStackTrace();
        }

        try {
            //now add attribute to xml
            InputSource is = new InputSource(new StringReader(newXML));
            builder = factory.newDocumentBuilder();
            doc = builder.parse(is);

            NodeList nList = doc.getElementsByTagName("COREF");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String ID = eElement.getAttribute("ID");
                    String REF = getReferenceID(ID);

                    if(!REF.equals("") && !REF.equals(ID)) {

                        eElement.setAttribute("REF", REF);
                    }
                }
            }

            print(doc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public int getSubtringCount(String str, String findStr){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }
}