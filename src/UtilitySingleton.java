import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunny on 11/5/16.
 */
public class UtilitySingleton {
    private static UtilitySingleton ourInstance = new UtilitySingleton();
    public static UtilitySingleton getInstance() {
        return ourInstance;
    }
    private UtilitySingleton() {
    }

    //todo tackle to add more than one unique key
    int index = 0;

    Map<String,NPObj> mapOfNPToMarkedID = new HashMap();

    public String getGenerateNewKey(){
        return Integer.toString(--index);
    }

//    public void setMap( Map mapOfNPToMarkedID ){
//        this.mapOfNPToMarkedID = mapOfNPToMarkedID;
//    }

    public Map getMap(){
        return this.mapOfNPToMarkedID;
    }

    public void updateMap(String key,String value, int pos){
        this.mapOfNPToMarkedID.put(key,new NPObj(value,key,"",pos));
    }

    public void clearMap(){
        index = 0;
        this.mapOfNPToMarkedID.clear();
    }

    public boolean removeKey(String key){
        if(null == this.mapOfNPToMarkedID.remove(key)){
            return false;
        }
        return true;
    }
}
