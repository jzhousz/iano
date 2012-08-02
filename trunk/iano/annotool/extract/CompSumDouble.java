package annotool.extract;

/**  Compensated Summation
 *
 *  Same as CompSumFloat, but with doubles.
 */

public class CompSumDouble
{
    private double sum = 0.0F;
    private double comp = 0.0F; //A running compensation for lost low-order bits.
    double y, t;

    /**
     * Default constructor
     */
    public CompSumDouble() {
        this.sum = this.comp = this.y = this.t = 0.0F;
        //happens by default anyway
    }

    /**
     * Constructor that sets the value of the sum instance variable
     * 
     * @param newSum  Value to be assigned to sum
     */
    public CompSumDouble(double newSum) {
        this.sum = newSum;
    }

    /**
     * Set the value of the sum instance variable
     * 
     * @param newSum  Value to be assigned to sum
     */
    public void setSum(double newSum) {
        this.sum = newSum;
    }

    /**
     * Returns the value of the sum instance variable
     * 
     * @return  The value of sum
     */
    public double getSum() {
        return sum;
    }

    /**
     * Adds the passed in number to the sum instance variable
     * 
     * @param i  Value to add to sum
     */
    public void Add(double i) {
        y = i - comp;       //So far, so good: c is zero.
        t = sum + y;        //Alas, sum is big, y small, so low-order digits
        //                  //of y are lost.
        comp = (t - sum) - y; //(t - sum) recovers the high-order part of y;
        //                  //subtracting y recovers -(low part of y)
        sum = t;            //Algebraically, c should always be zero.
        //                  //Beware eagerly optimising compilers!
        //                  //Next time around, the lost low part will be
        //                  //added to y in a fresh attempt.
    }
}
