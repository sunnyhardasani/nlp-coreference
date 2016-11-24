/**
 * Created by sunny on 11/4/16.
 */
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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

    /**
     * will return all the noun phrases
     * todo: great variety of changes can be made in this function to get all the important
     * refer highlighted sections in the paper
     * @param parse
     * @return
     */
    private List<String> getNounPhrases(Tree parse) {
        List<String> result = new ArrayList<String>();
        TregexPattern pattern = TregexPattern.compile("(@NNP) | (@NNS) | (@NN) |(@NNPS) | (@PRP)");
        TregexMatcher matcher = pattern.matcher(parse);

        //while (matcher.find()) {
        while (matcher.findNextMatchingNode()) {
            Tree match = matcher.getMatch();

            if(Settings.getInstance().getNptype().equals(NPTYPE.BASE)) {
//                boolean isChildNP = false;
//                for(Tree child:match.children()){
//                    if(checkChildren(child)){
//                        isChildNP = true;
//                        break;
//                    }
//                }
//                if(false == isChildNP) {
                    List<Tree> leaves = match.getLeaves();
                    //System.out.println("leaves = " + leaves);
                    String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
                    result.add(nounPhrase);
//                }
            }
            else if( Settings.getInstance().getNptype().equals(NPTYPE.ALL)
                    || Settings.getInstance().getNptype().equals(NPTYPE.ELLEN) ) {

                List<Tree> leaves = match.getLeaves();
                String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
                result.add(nounPhrase);
            }

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
            Sentence sent = new Sentence(sentenceWithoutXML);
            //parse result of all the sentence and
            //add to the nplist of object
            List<String> results = getNounPhrases(sent.parse());
            UtilitySingleton utilitySingletonObj = UtilitySingleton.getInstance();
            for (String np : results) {
                String ID = utilitySingletonObj.getInstance().getGenerateNewKey();
                utilitySingletonObj.getInstance().updateMap(ID,np,this.startPosIndex);
                this.startPosIndex++;
            }
        }
    }
}
