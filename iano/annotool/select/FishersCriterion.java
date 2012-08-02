package annotool.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;



/**
 *   This class uses FishersCriterion to select features.
 */
public class FishersCriterion implements FeatureSelector
{
    /*
     *  Fisher's Criterion formula:
     *
     *            ( mean_j(Class_1) - mean_j(Class_2)^2
     *  FC_j = ---------------------------------------------
     *         ( variance_j(Class_1) + variance_j(Class_2) )
     *
     *  //j is a feature
     *
     *
     *  Variance formula:
     *
     *                                  ( SUM[i=1..n] X_i )^2
     *          SUM[i=1..n] (X_i^2)  -  ---------------------
     *                                            n
     *  s^2 = --------------------------------------------------
     *                            n - 1
     *
     *  //sum of squares minus sum squared over n, over n - 1
     *
     */
    //
    //variables
    private float[][] features; //data, [sample][feature]
    private int[] classes;      //a.k.a. targets, parallel to features.
    private int input_number_of_features = 0; //features per sample
    private int number_of_classes;  //number of unique entries in classes[]
    //
    //The following four variables can be thought of as 2d arrays,
    //with the arrangement [class][feature].
    //
    //The number of classes is not known in advance,
    //and the class labels do not necessarily begin with 0,
    //so I am using HashMaps.
    //The key is the class id.  The value is an array,
    //where each element corresponds to a feature number.
    //
    //sum of every feature in every class
    private HashMap<Integer, float[]> sums = new HashMap<Integer, float[]>();
    //sum of squares of every feature in every class
    private HashMap<Integer, float[]> sumsSq = new HashMap<Integer, float[]>();
    //mean (average) of every feature in every class
    private HashMap<Integer, float[]> means = new HashMap<Integer, float[]>();
    //variance of every feature in every class
    private HashMap<Integer, float[]> variances = new HashMap<Integer, float[]>();
    //number of samples in every class
    private HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
    //
    //fisher criterion for each feature
    private double[] fisher;
    //
    //PARAMETERS
    private String multiclass_comparison_method;//"ONE_VS_OTHERS" or "PAIRWISE"
    int output_number_of_features;              //number of features to select
    int[] selected_indices;                     //selected features

    
    /**
     * Default constructor
     */    
    //CONSTRUCTORS
    //
    //This constructor only processes the parameters.
    //It is meant to be used in the "Horizontal" approach,
    //where the data (features[][] and classes[]
    //will later be added with addData() method,
    //possibly more than once.
    //=============IGNORE============
    public FishersCriterion()
    {
    }
    
    /**
     * Sets algorithm parameters from para
     * 
     * @param   para  Each element of the hashmap holds a parameter name
     *                for its key and a its value is that of the parameter.
     *                The parameters should be the same as those in the 
     *                algorithms.xml file.
     */
    public void setParameters(HashMap<String, String> para)
    {
        if (para.containsKey("MULTICLASS COMPARISON METHOD")) {
            if (para.get("MULTICLASS COMPARISON METHOD").equals("PAIRWISE")) {
                this.multiclass_comparison_method = "PAIRWISE";
            }
            else {
                this.multiclass_comparison_method = "ONE VS OTHERS";
            }
        }
        else {
            this.multiclass_comparison_method = "ONE VS OTHERS";
            //use ONE_VS_OTHERS if not specified
        }

        if (para.containsKey("NUMBER OF FEATURES")) {
            this.output_number_of_features =
                    Integer.parseInt((String) para.get("NUMBER OF FEATURES"));

            System.out.println("NUMBER OF FEATURES = " + this.output_number_of_features);
        }
    }

    /**
     * Constructor that sets algorithm parameters from para
     * 
     * @param   para  Each element of the hashmap holds a parameter name
     *                for its key and a its value is that of the parameter.
     *                The parameters should be the same as those in the 
     *                algorithms.xml file.
     */
    public FishersCriterion(HashMap<String, String> para) {
        if (para.containsKey("MULTICLASS COMPARISON METHOD")) {
            if (para.get("MULTICLASS COMPARISON METHOD").equals("PAIRWISE")) {
                this.multiclass_comparison_method = "PAIRWISE";
            }
            else {
                this.multiclass_comparison_method = "ONE VS OTHERS";
            }
        }
        else {
            this.multiclass_comparison_method = "ONE VS OTHERS";
            //use ONE_VS_OTHERS if not specified
        }

        if (para.containsKey("NUMBER OF FEATURES")) {
            this.output_number_of_features =
                    Integer.parseInt((String) para.get("NUMBER OF FEATURES"));

            System.out.println("NUMBER OF FEATURES = " + this.output_number_of_features);
        }
         
        /*
            if (this.output_number_of_features > this.input_number_of_features) {
                this.output_number_of_features = this.input_number_of_features;
                //print a diagnostic message?
            }
        }
        else {
            //if unspecified
            //select top 20% percent, or at least one
            this.output_number_of_features =
                    (this.input_number_of_features * 20) / 100;
            if (this.output_number_of_features == 0) {
                this.output_number_of_features = 1;
            }
        }*/
    }
    ///////////////  IGNORE THAT CONSTRUCTOR !!!!!!!!!s

    /**
     * Sets algorithm parameters from para and copies features and classes to instance variables.
     * Also calculates a couple instance variables and calls process_parameters.
     * 
     * @param   features  The extracted features of the image
     * @param   classes   The classes(targets) of the image
     * @param   para      Each element of the hashmap holds a parameter name
     *                    for its key and a its value is that of the parameter.
     *                    The parameters should be the same as those in the 
     *                    algorithms.xml file.
     */
    //This constructor is meant to be used in the "Vertical" approach,
    //where the data (features[][] and classes[]) are to be added only once.
    public FishersCriterion(float[][] features, int[] classes, HashMap parameters) {
        //process parameters using the other constructor
        //this(parameters);

        this.features = features;
        this.classes = classes;

        this.input_number_of_features = features[0].length;
        this.fisher = new double[input_number_of_features];

        //throw exception of n < 2

        process_parameters(parameters);
    }

    private void process_parameters(HashMap parameters) {
        if (parameters.containsKey("MULTICLASS COMPARISON METHOD")) {
            if (parameters.get("MULTICLASS COMPARISON METHOD").equals("PAIRWISE")) {
                this.multiclass_comparison_method = "PAIRWISE";
            }
            else {
                this.multiclass_comparison_method = "ONE VS OTHERS";
            }
        }
        else {
            this.multiclass_comparison_method = "ONE VS OTHERS";
            //use ONE_VS_OTHERS if not specified
        }

        if (parameters.containsKey("NUMBER OF FEATURES")) {
            this.output_number_of_features =
                    Integer.parseInt((String) parameters.get("NUMBER OF FEATURES"));

         if (this.output_number_of_features > this.input_number_of_features) {
                this.output_number_of_features = this.input_number_of_features;
                //print a diagnostic message?
            }
        }
        /*
        else {
            //if unspecified
            //select top 20% percent, or at least one
            this.output_number_of_features =
                    (this.input_number_of_features * 20) / 100;
            if (this.output_number_of_features == 0) {
                this.output_number_of_features = 1;
            }
        }*/
    }

    private void calculate_sums_and_sumsSq() {
        //this function calculates the sum of every feature, in every class,
        //and sum of every feature squared, in every class.
        //
        //I am using a compensated summation algorithm
        //(a.k.a. Kahan summation algorithm)
        //to avoid rounding errors.
        //http://en.wikipedia.org/wiki/Compensated_summation
        //
        //the compensated summation pseudocode:
        //function KahanSum(input)
        //    var sum = 0.0
        //    var c = 0.0             //A running compensation for lost low-order bits.
        //    for i = 1 to input.length do
        //        y = input[i] - c    //So far, so good: c is zero.
        //        t = sum + y         //Alas, sum is big, y small, so low-order digits of y are lost.
        //        c = (t - sum) - y   //(t - sum) recovers the high-order part of y; subtracting y recovers -(low part of y)
        //        sum = t             //Algebraically, c should always be zero. Beware eagerly optimising compilers!
        //        //Next time around, the lost low part will be added to y in a fresh attempt.
        //    return sum

        //c is specific to every feature in every class

        //these are for sums
        HashMap<Integer, float[]> c_sum = new HashMap<Integer, float[]>();
        float y_sum = 0;
        float t_sum = 0;

        //these are for sums of squares
        HashMap<Integer, float[]> c_sumSq = new HashMap<Integer, float[]>();
        float y_sumSq = 0;
        float t_sumSq = 0;

        //for each sample, for each feature
        for (int i = 0; i < features.length; i++) {
            for (int j = 0; j < input_number_of_features; j++) {
                //create new k/v pairs if it's the first appearance of that class
                if (!sums.containsKey(classes[i])) {
                    sums.put(classes[i], new float[input_number_of_features]);
                    sumsSq.put(classes[i], new float[input_number_of_features]);
                    counts.put(classes[i], 0);

                    //also create new c, y, and t
                    c_sum.put(classes[i], new float[input_number_of_features]);
                    //y_sum.put(classes[i], new float[input_number_of_features]);
                    //t_sum.put(classes[i], new float[input_number_of_features]);

                    c_sumSq.put(classes[i], new float[input_number_of_features]);
                    //y_sumSq.put(classes[i], new float[input_number_of_features]);
                    //t_sumSq.put(classes[i], new float[input_number_of_features]);
                }

                y_sum = features[i][j] - c_sum.get(classes[i])[j];
                t_sum = sums.get(classes[i])[j] + y_sum;
                c_sum.get(classes[i])[j] = (t_sum - sums.get(classes[i])[j]) - y_sum;
                sums.get(classes[i])[j] = t_sum;

                y_sumSq = features[i][j] * features[i][j] - c_sumSq.get(classes[i])[j];
                t_sumSq = sumsSq.get(classes[i])[j] + y_sumSq;
                c_sumSq.get(classes[i])[j] = (t_sumSq - sumsSq.get(classes[i])[j]) - y_sumSq;
                sumsSq.get(classes[i])[j] = t_sumSq;
            }
            counts.put(classes[i], counts.get(classes[i]) + 1);
            //essentially, count_for_that_class++
        }
    }

    private int[] get_class_ids() {
        //classes are identified with integer id's.
        //the id's do not necessarily begin with 0.
        //at times, it is useful to get an array of the id's.
        //this function does just that
        int[] class_indices = new int[counts.size()];
        Object[] keys = counts.keySet().toArray();
        for (int i = 0; i < class_indices.length; i++) {
            class_indices[i] = (Integer) keys[i];
        }
        return class_indices;
    }

    private void calculate_means_and_variances() {
        //this function calculates the means and variances of every feature,
        //in every class,

        //create means and variances arrays for each class
        for (int c : counts.keySet()) {
            means.put(c, new float[input_number_of_features]);
            variances.put(c, new float[input_number_of_features]);
        }

        //foreach class
        for (int c : counts.keySet()) {
            //foreach feature
            for (int f = 0; f < input_number_of_features; f++) {
                //calculate mean and variance
                means.get(c)[f] = sums.get(c)[f] / counts.get(c);
                variances.get(c)[f] =
                        (sumsSq.get(c)[f] - sums.get(c)[f] * means.get(c)[f])
                        / (counts.get(c) - 1);
            }
        }
    }

    private void calculate_fishers_criterion() throws Exception {
        //calculate sums and sums of squares for each feature
        calculate_sums_and_sumsSq();

        number_of_classes = counts.size();

        if (number_of_classes < 2) {
            //Fisher's Criterion requires AT LEAST 2 classes to work
            throw new Exception("Number of classes less than 2");
        }
        else if (number_of_classes == 2) {
            calculate_fishers_criterion_two_classes();
        }
        else if (multiclass_comparison_method.equals("ONE_VS_OTHERS")) {
            calculate_fishers_criterion_one_vs_others();
        }
        else { //PAIRWISE
            calculate_fishers_criterion_pairwise();
        }
    }

    private void calculate_fishers_criterion_two_classes() {
        calculate_means_and_variances();

        //there are only two classes -- get their Integer id's
        int[] class_ids = get_class_ids();
        int class1 = class_ids[0];
        int class2 = class_ids[1];

        //calculate fisher's criterion for each feature
        for (int f = 0; f < input_number_of_features; f++) {
            fisher[f] =
                    ((means.get(class1)[f] - means.get(class2)[f])
                    * (means.get(class1)[f] - means.get(class2)[f]))
                    / (variances.get(class1)[f] + variances.get(class2)[f]);
        }
    }

    private void calculate_fishers_criterion_pairwise() {
        calculate_means_and_variances();

        int[] class_ids = get_class_ids();

        //match each pair of classes, without duplication of pairs
        //e.g., given 4 classes, pairs are 1-2, 1-3, 1-4, 2-3, 2-4, 3-4
        for (int c1 = 0; c1 < class_ids.length - 1; c1++) {
            for (int c2 = c1 + 1; c2 < class_ids.length; c2++) {
                //for each feture
                for (int f = 0; f < input_number_of_features; f++) {
                    fisher[f] +=
                            ((means.get(class_ids[c1])[f] - means.get(class_ids[c2])[f])
                            * (means.get(class_ids[c1])[f] - means.get(class_ids[c2])[f]))
                            / (variances.get(class_ids[c1])[f] + variances.get(class_ids[c2])[f]);
                }
            }
        }
    }

    private void calculate_fishers_criterion_one_vs_others() {
        calculate_means_and_variances();
        //the above call is useful for each ONE class in each ONE-vs-OTHERS combination.
        //the means and variances of the other classes, combined,
        //have to be calculated separately

        float others_sum = 0;   //OTHERS
        float others_sumSq = 0;
        float others_mean = 0;
        float others_var = 0;
        int others_count = 0;

        //get class indexes
        int[] class_ids = get_class_ids();

        //for compensated summation
        float c_sum = 0, y_sum = 0, t_sum = 0;
        float c_sumSq = 0, y_sumSq = 0, t_sumSq = 0;
        //compensated summation is not as critical here as it is in
        //calculating sums and sumsSq.  But, it doesn't hurt, and
        //for a large number of classes, it could improve things

        //for each feature f
        for (int f = 0; f < input_number_of_features; f++) {
            //for each class c
            for (int one_class = 0; one_class < class_ids.length; one_class++) {
                //calc sum, sumSq, and count for each of the other classes, combined
                //this means add sums and sumsSq of these classes together.
                for (int other_class = 0; other_class < class_ids.length; other_class++) {
                    if (other_class == one_class) {
                        continue; //skip the ONE class in the combination
                    }
                    //sum
                    y_sum = sums.get(class_ids[other_class])[f] - c_sum;
                    t_sum = others_sum + y_sum;
                    c_sum = (t_sum - others_sum) - y_sum;
                    others_sum = t_sum;
                    //sumSq
                    y_sumSq = sumsSq.get(class_ids[other_class])[f] - c_sumSq;
                    t_sumSq = others_sumSq + y_sumSq;
                    c_sumSq = (t_sumSq - others_sumSq) - y_sumSq;
                    others_sumSq = t_sumSq;

                    others_count += counts.get(class_ids[other_class]);
                }

                //calc mean and var of other classes, combined
                others_mean = others_sum / others_count;
                others_var = (others_sumSq - others_sum * others_mean) / (others_count - 1);

                //calc fisher's criterion for this one_vs_others combination
                //and add to total fisher for this feature
                fisher[f] +=
                        ((means.get(class_ids[one_class])[f] - others_mean)
                        * (means.get(class_ids[one_class])[f] - others_mean))
                        / (variances.get(class_ids[one_class])[f] + others_var);

                //reset others' variables for next run
                others_sum = 0;
                others_sumSq = 0;
                others_mean = 0;
                others_var = 0;
                others_count = 0;

                //reset compensated summation
                c_sum = 0;
                c_sumSq = 0;
            }
        }
    }

    //Selects the number of features specified in the NUMBER_OF_FEATURES
    //parameter with the highest Fisher's Criterion score
    private void select_best_features() {

        ArrayList<FeatureFisherIndex> all = new ArrayList<FeatureFisherIndex>();
        //fill it up with the values/indices
        for (int i = 0; i < this.fisher.length; i++) {
            all.add(new FeatureFisherIndex(fisher[i], i));
        }

        //DEBUG
//        System.out.println("=== all features: ===");
//        for (int i = 0; i < all.size(); i++) {
//            System.out.println("Fisher: " + all.get(i).fisher_value + " Index: " + all.get(i).feature_index);
//        }

        //sort
        Collections.sort(all); //, new FeatureFisherIndex_Compare());

        //DEBUG
//        System.out.println("=== all features SORTED: ===");
//        for (int i = 0; i < all.size(); i++) {
//            System.out.println("Fisher: " + all.get(i).fisher_value + " Index: " + all.get(i).feature_index);
//        }

        //get best ones
        this.selected_indices = new int[this.output_number_of_features];
        System.out.println("selected_indices length = " + this.selected_indices.length);

        for (int i = 0; i < this.selected_indices.length; i++) {
            this.selected_indices[i] = all.get(i).feature_index;
        }

        //DEBUG
        System.out.println("selected_indices");
        for (int i = 0; i < this.selected_indices.length; i++) {
            System.out.print(this.selected_indices[i] + " ");
        }
        System.out.println();


//        this.selected_indices = new int[this.output_number_of_features];
//
//        Double[] sorted_fisher = new Double[this.fisher.length];
//        for (int i = 0; i < this.fisher.length; i++) {
//            sorted_fisher[i] = this.fisher[i];
//        }
//        Arrays.sort(sorted_fisher, Collections.reverseOrder());

        //DEBUG
//        System.out.println("SORTED:");
//        for (int i = 0; i < sorted_fisher.length; i++) {
//            System.out.print(sorted_fisher[i] + " ");
//        }
//        System.out.println();

//        for (int i = 0; i < this.output_number_of_features; i++) {
//            double val = (Double) sorted_fisher[i];
//            for (int j = 0; j < this.fisher.length; j++) {
//                if (fisher[j] == val) {
//                    this.selected_indices[i] = j;
//                    break;
//                }
//            }
//        }
    }

    /**
     * Sets and calculates instance variables based on the parameters and calls selectFeatures
     * 
     * @param   features  Two-dimensional array of extracted image data
     * @param   targets   Array of the targets for the image
     * @return            Two-dimensional array of features that are selected
     */
    // 08/06/2011 implementing modified FeatureSelector interface  
    // so the data don't need to be supplied in constructor
    public float[][] selectFeatures(float[][] features, int[] targets)
    {
        this.features = features;
        this.classes = targets;

        this.input_number_of_features = features[0].length;
        this.fisher = new double[input_number_of_features];

        //check the validity of output_number_of_features againgn input
         if (this.output_number_of_features > this.input_number_of_features) 
         {
           this.output_number_of_features = this.input_number_of_features;
           //print a diagnostic message
           System.out.println("Number of outputfeatures is set as "+output_number_of_features);
        }
        
        return selectFeatures();
    }
    
    /**
     * Calls methods calculate_fishers_criterion, select_best_features, and selectFeaturesGivenIndices
     * 
     * @return  Two-dimensional array of features that are selected
     */
    public float[][] selectFeatures(){
        try {    	
        	calculate_fishers_criterion();
        }
        catch(Exception ex) {
        	ex.printStackTrace();
        }
        select_best_features();

        return selectFeaturesGivenIndices(this.selected_indices);
    }

    /**
     * Returns the indices of the selected features 
     * (an index is between 0 and one less than the number of features).
     * 
     * @return  The indices of the selected features.
     */
    public int[] getIndices(){
        if (selected_indices == null) {
            selectFeatures();
        }
        return selected_indices;
    }

    /**
     * Selects features using indices and returns the selected features.
     * 
     * @param   indices  Array of indices to the data columns
     * @return           Two-dimensional array of features that are selected   
     */
    public float[][] selectFeaturesGivenIndices(int[] indices) {
        float[][] selectedFeatures = new float[this.features.length][indices.length];

        for (int i = 0; i < this.features.length; i++) {
            for (int j = 0; j < indices.length; j++) {
                selectedFeatures[i][j] = this.features[i][indices[j]];
            }
        }
        return selectedFeatures;
    }

    /**
     * Selects features using indices and returns the selected features.
     * 
     * @param   data     Two-dimensional array of extracted image data
     * @param   indices  Array of indices to the data columns
     * @return           Two-dimensional array of features that are selected   
     */
    public float[][] selectFeaturesGivenIndices(float[][] data, int[] indices) {
        float[][] selectedFeatures = new float[data.length][indices.length];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < indices.length; j++) {
                selectedFeatures[i][j] = data[i][indices[j]];
            }
        }
        return selectedFeatures;
    }

    //////////////////////////////////////
    protected void test() throws Exception {
        //Scanner scanner = new Scanner(System.in);
        //System.out.print("Type 'AAA': ");
        //String input = scanner.nextLine();

//        System.out.println("Entering test()");

//       float t1, t2;
//        t1 = System.currentTimeMillis();

        calculate_fishers_criterion();
        select_best_features();

//        System.out.println("After calculate_fishers_criterion()");

//        t2 = System.currentTimeMillis();
//        System.out.println("TIME: " + (t2 - t1));
        /////////////

//        System.out.println("SUMS:");
//        for (int k : sums.keySet()) {
//            for (int j = 0; j < input_number_of_features; j++) {
//                System.out.printf("%12.6f\t", sums.get(k)[j]);
//            }
//            System.out.println();
//        }

//        System.out.println("SUMS SQ:");
//        for (int k : sums.keySet()) {
//            for (int j = 0; j < input_number_of_features; j++) {
//                System.out.printf("%12.6f\t", sumsSq.get(k)[j]);
//            }
//            System.out.println();
//        }

        System.out.println("FISHER:");
        for (int i = 0; i < fisher.length; i++) {
            System.out.printf("%12.6f\t", fisher[i]);
        }
        System.out.println();

//        System.out.println("SELECTED INDICES:");
//        for (int i = 0; i < selected_indices.length; i++) {
//            System.out.printf("%12.6f\t", selected_indices[i]);
//        }
//        System.out.println();



//        select_best_features();
//        System.out.println("\t\t BEST FEATURES:");
//        for (int i = 0; i < this.selected_indices.length; i++) {
//            System.out.printf("==%12.6f\taa", selected_indices[i]);
//        }
//        System.out.println();

    }
}

class FeatureFisherIndex  implements Comparable
{
    public double fisher_value;
    public int feature_index;

    /**
     * Constructor that copies parameters to instance variables.
     * 
     * @param   data     Two-dimensional array of extracted image data
     * @param   indices  Array of indices to the data columns
     * @return           Two-dimensional array of features that are selected   
     */    
    public FeatureFisherIndex(double fisher_value, int feature_index) {
        this.fisher_value = fisher_value;
        this.feature_index = feature_index;
    }
    
    /**
     * Compares the indices of instance object and a separate object
     * 
     * @param   p2  Second object to be compared
     * @return      1 if this<p2 , -1 if this1>p2, 0 if this==p2   
     */ 
    public int compareTo(Object p2) {
        if (this.fisher_value < ((FeatureFisherIndex)p2).fisher_value) {
            return 1;
        }
        else if (this.fisher_value > ((FeatureFisherIndex)p2).fisher_value) {
            return -1;
        }
        else {
            return 0;
        }
    }
}

class FeatureFisherIndex_Compare implements Comparator
{
   /**
    * Compares the indices of two separate objects.
    * 
    * @param   p1  First object to be compared
    * @param   p2  Second object to be compared
    * @return      1 if p1<p2 , -1 if p1>p2, 0 if p1==p2   
    */
    public int compare(Object p1, Object p2) {
        if (((FeatureFisherIndex) p1).fisher_value < ((FeatureFisherIndex) p2).fisher_value) {
            return 1;
        }
        else if (((FeatureFisherIndex) p1).fisher_value > ((FeatureFisherIndex) p2).fisher_value) {
            return -1;
        }
        else {
            return 0;
        }
    }
}

