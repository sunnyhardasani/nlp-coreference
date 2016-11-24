import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sunny on 11/6/16.
 */
public class ClusteringAlgorithm {
//    private static ClusteringAlgorithm ourInstance = new ClusteringAlgorithm();
//
//    public static ClusteringAlgorithm getInstance() {
//        return ourInstance;
//    }
    List<List<NPObj>>  listOfListOfNPObjs;

    //reference
    //http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    //reference
    //http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public ClusteringAlgorithm() {
        this.listOfListOfNPObjs = new ArrayList<List<NPObj>>();
    }

    public void loadData(List<NPObj> listNPObjs){
        for(NPObj npObj: listNPObjs){
            List<NPObj> listOfNPObj = new ArrayList<NPObj>();
            listOfNPObj.add(npObj);

            System.out.println("string :" + npObj.getStrNP() + " id:" + npObj.getID());
            this.listOfListOfNPObjs.add(listOfNPObj);
        }
    }


    public boolean allNPCompatible(List<NPObj> Ci,List<NPObj> Cj){
        Distance distanceObj = Distance.getInstance();
        Double maxValue = 1000000.0;
        for(NPObj NPa: Cj){
            for(NPObj NPb: Ci){
                if(distanceObj.calculate(NPa,NPb) >= maxValue){
                    return false;
                }
            }
        }
        return true;
    }
    public List<List<NPObj>>  run(double radius){
        Distance distanceObj = Distance.getInstance();
        distanceObj.setRadius(radius);

        int size = this.listOfListOfNPObjs.size();
        for(int j = size-1 ; j >= 0; j--){
            for(int i = j - 1; i >= 0; i--){

                List<NPObj> Cj = this.listOfListOfNPObjs.get(j);
                NPObj NPj = Cj.get(0);

                List<NPObj> Ci = this.listOfListOfNPObjs.get(i);
                NPObj NPi = Ci.get(0);

                Double distance = distanceObj.calculate(NPi,NPj);
                if(distance < radius && allNPCompatible(Cj, Ci)){

                    Cj = union(Ci,Cj);
                    this.listOfListOfNPObjs.set(j,Cj);
                }
            }
        }

        int index = 0;
        for(List<NPObj> listNPObjs: this.listOfListOfNPObjs){
            index++;
            if(listNPObjs.size() > 1) {
                for(NPObj npObj: listNPObjs){
                    //System.out.print(" ," + npObj.getID());
                    System.out.print(" ," + npObj.getStrNP());
                }
            }

        }

        return this.listOfListOfNPObjs;
    }
}
