
/**
 * Created by sunny on 11/4/16.
 */

import java.util.*;
import java.io.File;
import com.google.common.io.Files;
import java.nio.charset.Charset;
import java.lang.String;
import java.util.List;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import javax.xml.parsers.*;
import java.io.StringReader;
import edu.stanford.nlp.simple.*;

public class FileObj {

    List<SentenceObj> sentencesObjList  = new ArrayList<SentenceObj>();
    List<NPObj> npObjList               = new ArrayList<NPObj>();
    List<String> allSentenceExceptCoref = new ArrayList<String>();
    String sentenceWithXML;
    Document document;
    /**
     *
     * @param fileName
     * @throws Exception
     */
    public FileObj(String fileName) throws Exception{

        // read all the map
        generateMapOfNPToID(fileName);

        // read all the sentences of the file
        // and store in the sentences data structure
        parse();

        //read all the sentence from the map
        //create the list of NPObjs
        populateNPList();

        //todo remove this function
        for(NPObj npObj: this.npObjList){
            npObj.setMaxDifference(this.npObjList.size());
            npObj.loadFeatures();
        }
        ClusteringAlgorithm clusteringAlgorithmObj = new ClusteringAlgorithm();
        clusteringAlgorithmObj.loadData(this.npObjList);
        List<List<NPObj>> setOfCluster = clusteringAlgorithmObj.run(Settings.getInstance().getRadius());

        this.document = new PrintXML().print(setOfCluster,this.sentenceWithXML);

        UtilitySingleton.getInstance().clearMap();
    }

    /**
     * this will populate the np list
     */
    void populateNPList(){

        Map<Integer,Integer> localTempMap = new HashMap<Integer, Integer>();
        // Getting a Set of Key-value pairs
        Set entrySet = UtilitySingleton.getInstance().getMap().entrySet();

        // Obtaining an iterator for the entry set
        Iterator it = entrySet.iterator();

        // Get the sorted list of all pos
        List<Integer>  listOfAllCurrentPos = new ArrayList<Integer>();
        while(it.hasNext()){
            Map.Entry me = (Map.Entry)it.next();
            listOfAllCurrentPos.add(((NPObj) me.getValue()).getPos());
        }
        Collections.sort(listOfAllCurrentPos);

        // Iterate through all the pos
        Integer localPos = 1;
        for(Integer currentPos: listOfAllCurrentPos){
            localTempMap.put(currentPos,localPos++);
        }

        //now update the object with new pos and
        //add to the np object list
        it = entrySet.iterator();
        while(it.hasNext()){
            Map.Entry me = (Map.Entry)it.next();
            NPObj npObj = ((NPObj) me.getValue());
            int currentPos = npObj.getPos();
            int newPos = localTempMap.get(currentPos);
            npObj.setPos(newPos);

            //this will add all the np object in the list
            if(Settings.getInstance().getNptype().equals(NPTYPE.ELLEN)) {
                if (Integer.parseInt(npObj.getID()) > 0) {
                    this.npObjList.add(npObj);
                }
            }
            else{
                this.npObjList.add(npObj);
            }
        }
    }

    public Document getDocument(){
        return this.document;
    }

    void generateMapOfNPToID(String fileName)  throws Exception{

        // read some text from the file..
        File inputFile = new File(fileName);
        this.sentenceWithXML = Files.toString(inputFile, Charset.forName("UTF-8"));
        //this.sentenceWithXML = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
//        this.sentenceWithXML = this.sentenceWithXML.replaceAll("'s", " ");
//        this.sentenceWithXML = this.sentenceWithXML.replaceAll("'S", " ");
//        this.sentenceWithXML = this.sentenceWithXML.replaceAll("(\\r|\\n|\\r\\n)+", " ");
//        this.sentenceWithXML = this.sentenceWithXML.replaceAll("[ ]{2,}", " ");


        //this will modify the files
        //this is the temporary work around solution to read
        //sentence which are not marked with
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("<TXT>","<TXT><SENT>");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("</TXT>","</SENT></TXT>");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("<COREF","</SENT><COREF");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("</COREF>","</COREF><SENT>");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(this.sentenceWithXML));
            Document doc = builder.parse(is);

            NodeList nList2 = doc.getElementsByTagName("SENT");
            for (int temp = 0; temp < nList2.getLength(); temp++) {
                Node nNode = nList2.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String textContent = eElement.getTextContent();
                    textContent = textContent.replaceAll("\u00A0"," ");
                    this.allSentenceExceptCoref.add(textContent);
                }
            }


            NodeList nList = doc.getElementsByTagName("COREF");
            int pos = 0;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String textContent = eElement.getTextContent();
                    String ID = eElement.getAttribute("ID");
                    UtilitySingleton.getInstance().updateMap(ID, textContent, pos);
                    pos += 50;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this function would be responsible
     * for the parsing of file into sentences
     */
    void parse(){

        int posStartIndex = 1;
        for(String sent : this.allSentenceExceptCoref ) {
            //System.out.println(sent);
            edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(sent);
            // will iterate over all the sentences in the text
            for (Sentence nlpSentence : doc.sentences()) {
                SentenceObj sentenceObj = new SentenceObj(nlpSentence,posStartIndex);
                this.sentencesObjList.add(sentenceObj);
                posStartIndex += 50;
            }
        }
    }
}
