/**
 * Created by vinodism on 11/5/16.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import edu.stanford.nlp.simple.*;

public class Rules {


	List<String> nominativeList = Arrays.asList("they","i", "he", "she", "we");
	List<String> accusativeList = Arrays.asList("me", "them","him", "her", "us", "whom");
	List<String> possessiveList = Arrays.asList("my",  "his", "our", "your", "your", "their", "mine", "yours", "his", "hers", "ours", "yours", "theirs");
	List<String> reflexiveList  = Arrays.asList("yourself", "ourselves", "himself", "herself", "itself",  "themselves", "myself");
	List<String> ambiguousList  = Arrays.asList("you", "it");
	List<String> pluralPronoun  = Arrays.asList("we","they","us","them","our","ours");
	List<String> malesPronounList = Arrays.asList("he", "his", "himself", "him");
	List<String> femalesPronounList = Arrays.asList("she", "her", "herself", "hers");
	List<String> maleNamesList = new ArrayList<String>();
	List<String> femaleNamesList = new ArrayList<String>();

	private static Rules ourInstance = new Rules();
	public static Rules getInstance() {
		return ourInstance;
	}
	private Rules() {

		//files are downloaded from the following link
		//male and female http://www.cs.cmu.edu/Groups/AI/util/areas/nlp/corpora/names/
		try {

			BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/male.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				maleNamesList.add(line.toLowerCase());
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("ERROR: MALE FILE ERROR");
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/female.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				femaleNamesList.add(line.toLowerCase());
			}
		}
		catch(Exception e){
			System.out.println("ERROR: FEMALE FILE ERROR");
		}
	}


	boolean matchPronounType(List<String> list, List<String> words){
		for(String word: words){
			word = word.toLowerCase();
			if(list.contains(word)){
				return true;
			}
		}
		return false;
	}

	boolean isItemInList(List<String> list, String item){
		item = item.toLowerCase();
		return list.contains(item);
	}

	/**
	 *
	 * @param npObj
	 * @return
	 */
	List<String> ruleOneGetALlWords(NPObj npObj){
		Sentence sent = new Sentence(npObj.strNP);
		return sent.words();
	}

	/**
	 *
	 * @param words
	 * @return
	 */
	String ruleTwoGetPronounType(List<String> words) {
		String pronounType = "NONE";

		if(matchPronounType(nominativeList,words)){
			pronounType = "NOM";
		}
		else if(matchPronounType(accusativeList,words)){
			pronounType = "ACC";
		}
		else if(matchPronounType(possessiveList,words)){
			pronounType = "POS";
		}
		//todo check this reflexive whether its required or not
		else if(matchPronounType(reflexiveList,words)){
			pronounType = "REF";
		}
		else if(matchPronounType(ambiguousList,words)){
			pronounType = "AMB";
		}

		return pronounType;
	}

	/**
	 *
	 * @param allWords
	 * @return
	 */
	String ruleThreeGetArticle(List<String> allWords) {
		String article = "NONE";
		for(String word: allWords){
			String trimLowerCaseWord = word.trim();
			if(trimLowerCaseWord.equals("a")){
				article = "INDEF";
			}
			else if(trimLowerCaseWord.equals("an")){
				article = "INDEF";
			}
			else if(trimLowerCaseWord.equals("the")) {
				article = "DEF";
			}
		}

		return article;
	}

	/**
	 * Rule 4
	 * @param npObj
	 * @return
	 */
	String ruleFourGetAppositive(NPObj npObj){
		return "NO";
	}

	/**
	 *
	 * @param allWords
	 * @return
	 */
	String ruleFiveGetNumber(List<String> allWords){
		String number = "SING";

		int wordsSize = allWords.size();
		if(wordsSize > 0) {
			String lastWord = allWords.get(wordsSize-1).toLowerCase();
			if(lastWord.endsWith("s")){
				number = "PLURAL";
				return number;
			}
		}

		//two way to check this case
		for(String pronoun: pluralPronoun){
			if(allWords.contains(pronoun)){
				number = "PLURAL";
				break;
			}
		}

		return number;
	}

	/**
	 *
	 * @param npObj
	 * @return
	 */
	boolean ruleSixGetProperNames(NPObj npObj){
		Sentence sent = new Sentence(npObj.strNP);
		for(String tag:sent.nerTags()){
			if(tag.equals("PERSON")){
				return true;
			}
		}
		return false;
	}

	String ruleSevenHeadNoun(List<String> words){
		int wordsSize = words.size();
		if(wordsSize > 0) {
			return words.get(wordsSize - 1);
		}
		return "";
	}

	//todo imporove this feature
	//todo by applying pos tagger
	String ruleEightGender(List<String> words){
		for(String word : words){
			word = word.toLowerCase();
			if(isItemInList(malesPronounList,word)){
				return "MASC";
			}
			else if(isItemInList(maleNamesList,word)){
				return "MASC";
			}
			else if(isItemInList(femalesPronounList,word)){
				return "FEM";
			}
			else if(isItemInList(femaleNamesList,word)){
				return "FEM";
			}
			else if(word.equals("it")){
				return "EITHER";
			}
		}

		return "NEUTER";
	}

	/**
	 * Nin Animacy
	 * @param words
	 * @param npObj
	 * @return
	 */
	String ruleNineAnimacy(List<String> words, NPObj npObj){
		String gender = ruleEightGender(words);
		boolean isProperName = ruleSixGetProperNames(npObj);
		if(isProperName){
			return "ANIM";
		}
		else if(gender.equals("MAS") || gender.equals("FEM")){
			return "ANIM";
		}
		return "INANIM";
	}

	/**
	 * Ten semantic class
	 * @param headNoun
	 * @param isPerson
	 * @param gender
	 * @param animacy
	 * @return
	 */
	String ruleTenSemanticClass(String headNoun, boolean isPerson, String gender, String animacy){
		if(isPerson){
			return "HUMAN";
		}
		else if(animacy.equals("ANIM")){
			return "HUMAN";
		}

		Sentence sent = new Sentence(headNoun);
		List<String> tags = sent.nerTags();
		if(tags.size() > 0) {
			if((!tags.get(0).equals("O"))) {
				return tags.get(0);
			}
		}
		return "OBJECT";
	}
//	//features extracted from the parsing the corpus.
//	void ExtractFeatures(NPObj nPhrases) {
//		{
//			Features featureObj = nPhrases.fObj;
//			featureObj.setPosition(nPhrases.fPos);
//			//if(Utilities.getInstance().ifPronoun(nPhrases.strNP){
//			//featureObj.setPronounType(PronounType.);
//			//}
//			String[] words = nPhrases.strNP.split("\\s");
//			featureObj.setHeadNoun(words[words.length - 1]);
//			featureObj.setWords(nPhrases.strNP);
//			for (int i = 0; i < words.length; i++) {
//				if ((words[i].toLowerCase().equals("a")) || (words[i].toLowerCase().equals("an"))) {
//					featureObj.setArticle("Indefinite");
//				} else if (words[i].toLowerCase().equals("the")) {
//					featureObj.setArticle("Indefinite");
//				}
//			}
//		}
//	}
//	//checks if two NP are appositives or not.
/*	private boolean isAppositive(NPObj a, NPObj b) {
    	Features afeatureObj = a.fObj;
		Features bfeatureObj = b.fObj;
		{
					if (afeatureObj.getIsProperNoun() && (!bfeatureObj.getIsProperNoun())) {
						return true;
					} else if (!afeatureObj.getIsProperNoun() && (bfeatureObj.getIsProperNoun())) {
						return true;
					} else {
						return false;
					}
				} else {
					if (Utilities.getInstance().isInteger(a.SentenceArray[index_a + 1]))
						return true;
				}

			}
		}
		return false;
	}*/
}