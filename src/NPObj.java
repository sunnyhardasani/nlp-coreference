import edu.stanford.nlp.coref.data.Dictionaries;

/**
 * Created by sunny on 11/4/16.
 */
public class NPObj {
    String strNP;
    String ID = "";
    String REF = "";
    int pos = -1;
    int maxDifference = -1;
    boolean isEndingWithComma = false;
    boolean isPerson = false;
    Dictionaries.Gender gender = Dictionaries.Gender.UNKNOWN;
    FeaturesObj featuresObj;


    public NPObj(String strNP, String ID, String REF, int pos, boolean isPerson){
        this.strNP = strNP;
        this.ID = ID;
        this.REF = REF;
        this.pos = pos;
        this.isPerson = isPerson;
    }

    public void setREF(String REF) {
        this.REF = REF;
    }

    public void loadFeatures(){
        this.featuresObj = new FeaturesObj(this);
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

    public boolean isEndingWithComma() {
        return isEndingWithComma;
    }

    public void setEndingWithComma(boolean endingWithComma) {
        isEndingWithComma = endingWithComma;
    }

    public Dictionaries.Gender getGender() {
        return gender;
    }

    public void setGender(Dictionaries.Gender gender) {
        this.gender = gender;
    }

    public boolean isPerson() {
        return isPerson;
    }

    public void setPerson(boolean person) {
        isPerson = person;
    }
}
