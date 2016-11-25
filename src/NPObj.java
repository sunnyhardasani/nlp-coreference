/**
 * Created by sunny on 11/4/16.
 */
public class NPObj {
    String strNP;
    String ID = "";
    String REF = "";
    int pos = -1;
    int maxDifference = -1;
    FeaturesObj featuresObj;

    public NPObj(String strNP, String ID, String REF, int pos){
        this.strNP = strNP;
        this.ID = ID;
        this.REF = REF;
        this.pos = pos;
    }

    public void loadFeatures(){
        this.featuresObj = new FeaturesObj(this);

        //todo temp for printing
        //System.out.println("\nRule 1 - All Words:");
//        for(String word: this.featuresObj.getRuleOne_allWords()){
//            System.out.println(word);
//        }

        //System.out.println("\nRule 2 - Pronoun Type:");
        //System.out.println(this.featuresObj.getRuleTwo_PronounType());

        //System.out.println("\nRule 3 - Article");
        //System.out.println(this.featuresObj.getRuleThree_article());

//        System.out.println("\nRule 4 - Appositive");
//        System.out.println(this.featuresObj.getRuleFour_appositive());
//
//        System.out.println("\nRule 5 - Number");
//        System.out.println(this.featuresObj.getRuleFive_number());
//
//        System.out.println("\nRule 6 - Proper Name");
//        System.out.println(this.featuresObj.isRuleSix_properName());
//
//        System.out.println("\nRule 7 - Head Noun");
//        System.out.println(this.featuresObj.getRuleSeven_headNoun());
//
//        System.out.println("\nRule 8- Gender");
//        System.out.println(this.featuresObj.getRuleEight_gender());
//
//        System.out.println("\nRule 9 - Animacy");
//        System.out.println(this.featuresObj.getRuleNine_animacy());
//
//        System.out.println("\nRule 10 - Semantic");
//        System.out.println(this.featuresObj.getRuleTen_semanticClass());
//
//        System.out.println("\nRule 11 - Position");
//        System.out.println(this.featuresObj.getRuleEleven_position());
    }

    public FeaturesObj getFeaturesObj() {
        return featuresObj;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getREF() {
        return REF;
    }

    public String getID() {
        return ID;
    }

    public String getStrNP() {
        return strNP.trim().toLowerCase();
    }

    public void setMaxDifference(int maxDifference) {
        this.maxDifference = maxDifference;
    }

    public int getMaxDifference() {
        return maxDifference;
    }

    public int compareTo(NPObj compareNPObj) {

        int compareID = Integer.parseInt(compareNPObj.getID());

        //ascending order
        return Integer.parseInt(this.getID()) - compareID;
    }
}
