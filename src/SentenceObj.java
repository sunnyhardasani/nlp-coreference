/**
 * Created by sunny on 11/4/16.
 */
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.simple.*;

import java.util.*;

public class SentenceObj {
    String sentenceWithXML;
    String sentenceWithoutXML;
    List<NPObj> npList = new ArrayList<NPObj>();
    Sentence nlpParsedObj;
    int startPosIndex = 0;

    public SentenceObj(Sentence nlpParsedObj,int startPosIndex){

        this.nlpParsedObj = nlpParsedObj;
        this.sentenceWithXML = nlpParsedObj.toString();
        this.sentenceWithoutXML = this.sentenceWithXML.replaceAll("<[^>]+>", "");
        this.startPosIndex = startPosIndex;

        parse(this.sentenceWithoutXML);
    }




    private boolean checkChildren(Tree parse) {
        TregexPattern pattern = TregexPattern.compile("@NNP");
        TregexMatcher matcher = pattern.matcher(parse);
        return matcher.find();
    }
    TregexPattern tPattern = TregexPattern.compile("(@NP)");

//    public void printNP(String line){s
//        //while ((line = br.readLine()) != null) {
//        System.out.println("line = " + line);
//        if(line.trim().length() > 0) {
//            Tree t = Tree.valueOf(line);
//            if( t != null ) {
//                TregexMatcher tMatcher = tPattern.matcher(t);
//                while (tMatcher.find()) {
//                    System.out.println(tMatcher.getMatch());
//                }
//            }
//        }
//        //}
//    }

    public String removeExtraSpace(String npWithSpaces, String sent){
        //apply the brtue force approach to remove the space from sentence
        for(int n = 0; n < npWithSpaces.length() ; n++){
            if(npWithSpaces.charAt(n) == ' ') {
                StringBuilder sb = new StringBuilder(npWithSpaces);
                sb.deleteCharAt(n);
                String s = sb.toString();
                if(sent.indexOf(s) >= 0){
                    return s;
                }
            }
        }
        //or send the same npWithSpace as you have to no choice
        return npWithSpaces;
    }
    /**
     * will return all the noun phrases
     * todo: great variety of changes can be made in this function to get all the important
     * refer highlighted sections in the paper
     * @param parse
     * @return
     */
    private List<String> getNounPhrases(Tree parse, String sentenceWithoutXML) {
        List<String> result = new ArrayList<String>();
        TregexPattern pattern = TregexPattern.compile("(@NNP) | (@NNS) | (@NN) |(@NNPS) | (@PRP)");
        TregexMatcher matcher = pattern.matcher(parse);

        //while (matcher.find()) {
        while (matcher.findNextMatchingNode()) {
            Tree match = matcher.getMatch();
//
//            if(Settings.getInstance().getNptype().equals(NPTYPE.BASE)) {
////                boolean isChildNP = false;
////                for(Tree child:match.children()){
////                    if(checkChildren(child)){
////                        isChildNP = true;
////                        break;
////                    }
////                }
////                if(false == isChildNP) {
////                    List<Tree> leaves = match.getLeaves();
////                    //System.out.println("leaves = " + leaves);
////                    String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
////                    result.add(nounPhrase);
////                }

                String sent = sentenceWithoutXML;
                String np = SentenceUtils.listToString(match.yield());

                //check if there are any extra space
                //if not then remove all the extra space
                if(sent.indexOf(np) < 0) {
                    np = removeExtraSpace(np, sent);
                }

                System.out.println("np = " + np);
                //this will add the np by removing all the unnecessary spaces
                result.add(np);
//            }
//            else if( Settings.getInstance().getNptype().equals(NPTYPE.ALL)
//                    || Settings.getInstance().getNptype().equals(NPTYPE.ELLEN) ) {
//
//                List<Tree> leaves = match.getLeaves();
//                String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
//                result.add(nounPhrase);
//            }

        }
        return result;
    }

    /**
     * call the function to parse the sentence
     * to fetch all the noun phrases
     * @param sentenceWithoutXML
     * @return
     */
    void parse(String sentenceWithoutXML){
        if(sentenceWithoutXML.length() > 0) {
            //printNP(sentenceWithoutXML);

            Sentence sent = new Sentence(sentenceWithoutXML);

            //parse result of all the sentence and
            //add to the nplist of object
            List<String> results = getNounPhrases(sent.parse(), sentenceWithoutXML);
            UtilitySingleton utilitySingletonObj = UtilitySingleton.getInstance();
            for (String np : results) {

                String ID = utilitySingletonObj.getInstance().getGenerateNewKey();
                utilitySingletonObj.getInstance().updateMap(ID,np,this.startPosIndex);
                this.startPosIndex++;
            }
        }
    }
}
