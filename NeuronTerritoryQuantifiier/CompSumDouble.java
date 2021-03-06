/**
 *
 * @author aleksey
 * Modified J Zhou
 */

/**  Compensated Summation
 *
 *  Same as CompSumFloat, but with doubles.
 */

public class CompSumDouble
{
    private double sum = 0.0F;
    private double comp = 0.0F; //A running compensation for lost low-order bits.
    double y, t;

    public CompSumDouble() {
        this.sum = this.comp = this.y = this.t = 0.0F;
        //happens by default anyway
    }

    public CompSumDouble(double newSum) {
        this.sum = newSum;
    }

    public void setSum(double newSum) {
        this.sum = newSum;
    }

    public double getSum() {
        return sum;
    }

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
