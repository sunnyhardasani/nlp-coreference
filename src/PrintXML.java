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
    //kept the map for future use
    Map mapIdToNPObj = new HashMap();

    public PrintXML() {
        this.clustersToCheckForRef = new ArrayList<List<NPObj>>();
    }


    public void print(Document xml) throws Exception {
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(xml), new StreamResult(out));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public String getReferenceID(String ID){

        boolean flag = false;

        List<NPObj> saveLargestCluster = new ArrayList<NPObj>();
        int largestClusterSize = Integer.MIN_VALUE;

        int smallestPos = Integer.MAX_VALUE;
        NPObj smallestPosNPObj = new NPObj("", "", "", 0);

        int smallestNeg = Integer.MAX_VALUE;
        NPObj smallestNegNPObj = new NPObj("", "", "", 0);

        int closestDistance = Integer.MAX_VALUE;
        NPObj closestNPObj = new NPObj("", "", "", 0);


        int tempDis = Integer.MAX_VALUE;
        NPObj tempNPObj = new NPObj("","","",0);

        NPObj npIDObj = (NPObj) mapIdToNPObj.get(ID);


        //if already assigned then return ref
        if(!npIDObj.getREF().equals("")){
            if(this.listOfConsideredIDs.contains(npIDObj.getREF())) {
                return npIDObj.getREF();
            }
            else{
                System.out.println("NOT FOUND = " + npIDObj.getREF());
            }
        }


        for(List<NPObj> cluster : this.clustersToCheckForRef){
            if(cluster.size() > 1) {
                for (NPObj npObj : cluster) {
                    if (npObj.getID().equals(ID)) {
                        flag = true;
//
                        if( largestClusterSize < cluster.size()){
                            largestClusterSize = cluster.size();
                            saveLargestCluster = cluster;
                        }
//                        break;
                    }
                }
//                if(flag) {
//
//                    for (NPObj npObj : cluster) {
//                        int distance = Math.abs(npIDObj.getPos() - npObj.getPos());
//
//                        if (distance != 0
//                                && closestDistance > distance
//                                && listOfConsideredIDs.contains(npObj.getID())) {
//
//                            closestDistance = distance;
//                            closestNPObj = npObj;
//                        }
//                    }
//                    flag = false;
//                }
//            }
        }

        if(flag){


            for(NPObj npObj: saveLargestCluster){

                int distance = Math.abs(npIDObj.getPos() - npObj.getPos());

                if(distance != 0
                        && tempDis > distance
                        && listOfConsideredIDs.contains(npObj.getID())
                        && Integer.parseInt(npObj.getID()) > 0
                        && Integer.parseInt(npObj.getID()) < Integer.parseInt(npIDObj.getID())){
                    tempDis = distance;
                    tempNPObj = npObj;
                }

                if(distance != 0 && closestDistance > distance && listOfConsideredIDs.contains(npObj.getID())){
                    closestDistance = distance;
                    closestNPObj = npObj;
                }

                if(!tempNPObj.getID().equals("") ){
                    tempNPObj = closestNPObj;
                }



//                if(this.listOfConsideredIDs.contains(npObj.getID())
//                        && npObj.getPos() < smallestPos
//                        && !npObj.getID().equals(ID)
//                        && Integer.parseInt(npObj.getID()) > 0 ) {
//
//                    smallestPos = npObj.getPos();
//                    smallestPosNPObj = npObj;
//                }
//
//                if(this.listOfConsideredIDs.contains(npObj.getID())
//                        && npObj.getPos() < smallestNeg
//                        && !npObj.getID().equals(ID)) {
//
//                    smallestNeg = npObj.getPos();
//                    smallestNegNPObj = npObj;
                }
            }
        }

        if(smallestPos == Integer.MAX_VALUE){
            return smallestNegNPObj.getID();
        }
        return smallestPosNPObj.getID();
//        return closestNPObj.getID();
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

        for(List<NPObj> cluster: clusters){
            for(NPObj npobj: cluster){
                mapIdToNPObj.put(npobj.getID(),npobj);
            }
        }

        //iterate all the elements
        for(List<NPObj> cluster: clusters){
            for(NPObj npobj: cluster){
                mapIdToNPObj.put(npobj.getID(),npobj);

                if(!npobj.getREF().equals("")){
                    String idNP = npobj.getREF();
                    NPObj idNPObj = (NPObj) mapIdToNPObj.get(idNP);

                    // this is the location which i have to mark
                    arrNPObj[idNPObj.getPos()] = idNPObj;
                }
            }
        }




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

                        String REF = npObj.getID();//.replaceAll("-","X");
                        remainingXML = remainingXML.substring(endloc);
                        String corefXML = xml.substring(0,startLoc)
                                        + "<COREF ID=\"" + REF
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

                        //REF = REF.replaceAll("-","X");
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

    public Document printWithNewAlgo(List<NPObj> clusters, String sentenceWithXML){

        Document doc;
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;

        this.listOfConsideredIDs = new ArrayList<String>();

        //this will iterate over the cluster
        //and set the id to ref id map
        Map mapIdToNPObj = new HashMap();
        for(int i = 0 ; i < clusters.size() ; i++){
            NPObj npobj = clusters.get(i);
            mapIdToNPObj.put(npobj.getID(),npobj);
        }



        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

        doc = builder.newDocument();

        sentenceWithXML = sentenceWithXML.replaceAll("<SENT>","");
        sentenceWithXML = sentenceWithXML.replaceAll("</SENT>","");

        NPObj arrNPObj[] = new NPObj[clusters.size()+1];
        for(int i = 0 ; i < clusters.size() ; i++){
            NPObj npobj = clusters.get(i);
            if( !npobj.getREF().equals("") && Integer.parseInt(npobj.getREF()) < 0 ) {
                NPObj npIDObj = (NPObj) mapIdToNPObj.get(npobj.getREF());

                arrNPObj[npIDObj.getPos()] = npIDObj;
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

                        //listOfConsideredIDs.add(npObj.getID());
                    }
                }
            }
        }
        newXML += remainingXML;

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
                    String REF = ((NPObj)mapIdToNPObj.get(ID)).getREF();

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