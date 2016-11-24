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
    List<String> allRefIds;

    //private static PrintXML ourInstance = new PrintXML();
//    public static PrintXML getInstance() {
//        return ourInstance;
//    }
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
            int smallestPos = Integer.MAX_VALUE;
            NPObj smallestPosNPObj = new NPObj("", "", "", 0);
            int smallestPositivePos = Integer.MAX_VALUE;
            NPObj smallestPositivePosNPObj = new NPObj("", "", "", 0);
            int count = 0;
            for(NPObj npObj: saveCluster){
                if(this.allRefIds.contains(npObj.getID()) && npObj.getPos() < smallestPos){
                    smallestPos = npObj.getPos();
                    smallestPosNPObj = npObj;
                }
//                if(npObj.getPos() < smallestPositivePos && Integer.parseInt(npObj.getID()) > 0 ){
//                    smallestPositivePos = npObj.getPos();
//                    smallestPositivePosNPObj = npObj;
//                    count++;
//                }
            }

//            if(smallestPositivePos  != Integer.MAX_VALUE && count > 1)
//                return smallestPositivePosNPObj.getID();

            return smallestPosNPObj.getID();
        }
        return "";
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

        allRefIds = new ArrayList<String>();

        //add all the positive id
        for(List<NPObj> cluster : this.clustersToCheckForRef){
            for(NPObj npObj: cluster){
                if(Integer.parseInt(npObj.getID()) > 0) {
                    if (!allRefIds.contains(npObj.getID())) {
                        this.allRefIds.add(npObj.getID());
                    }
                }
            }
        }

        String newXML = "";
        String remainingXML = sentenceWithXML;
        for(int arrIndex = 0; arrIndex < arrNPObj.length ; arrIndex++){
            NPObj npObj = arrNPObj[arrIndex];
            if(npObj != null){

                int startLoc = remainingXML.indexOf(npObj.getStrNP());
                if(startLoc > 0) {
                    int endloc = startLoc + npObj.getStrNP().length();
                    String xml = remainingXML.substring(0, endloc);

                    if(getSubtringCount(xml,"COREF") % 2 == 0 && !allRefIds.contains(npObj.getID())) {
                        allRefIds.remove(npObj.getID());

                        remainingXML = remainingXML.substring(endloc + 1);
                        xml = xml.replaceFirst(npObj.getStrNP(),
                                "<COREF ID=\""
                                        + npObj.getID()
                                        + "\">"
                                        + npObj.getStrNP()
                                        + "</COREF>");
                        newXML += xml;

                        //todo hack
                        allRefIds.add(npObj.getID());
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

            Set<String> hs = new HashSet<String>();
            hs.addAll(allRefIds);
            allRefIds.clear();
            allRefIds.addAll(hs);

            NodeList nList = doc.getElementsByTagName("COREF");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String ID = eElement.getAttribute("ID");
                    String REF = getReferenceID(ID);

                    if(!REF.equals("") && !REF.equals(ID) /*&& (allRefIds.contains(REF))*/) {
                        /*allRefIds.remove(REF);*/
                        //System.out.println("CHECK" + (allRefIds.contains(REF)));
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
