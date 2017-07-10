import java.util.LinkedList;
import java.util.Random;
import java.util.Arrays;
import java.text.DecimalFormat;
import java.io.PrintWriter;

public class Sandpile
{
    //member variables
    private int num;	//width * height
    private double[] a, b,
                     slope;
    private LinkedList<IntDbl> changes;
    private int[][] connect;
    private LinkedList<Integer> unstable;
    private double threshold,
                   sigma,
                   dnc;
    private boolean randRedist,
                    randExtract,
                    stochThresh,
                    noncons;
    private int seed;
    private Random generator;
                    
    //constructor
    /*double[] initialA, int setNum,
                    boolean[][] setConnect, double setThresh,
                    double setSigma, double setDNC,
                    boolean setRedist, boolean setExtract,
                    boolean setSig, boolean setNoncons,
                    int setSeed*/
    public Sandpile(ParamSet params)
    {
    	//make vars same as constructor below
    	double[] initialA = new double[params.getWidth() * params.getHeight()];
    	int setNum = params.getWidth() * params.getHeight(),
    		setSeed = params.getSeed();
    	boolean[][] setConnect;
    	if (!params.getWalled())
			setConnect = ConnGenerator.wattsStrogatz(params.getHeight(), params.getWidth(), false, params.getBeta());
		else
			setConnect = ConnGenerator.wattsStrogatz(params.getHeight(), params.getWidth(), true, params.getBeta());
    	double setThresh = params.getThresh(),
    		   setDNC = params.getDNC(),
    		   setSigma = params.getSigma();
    	boolean setRedist = params.getRandRedist(),
    			setExtract= params.getRandExtract(),
    			setSig = params.getStochThresh(),
    			setNoncons = params.getNonConserveRedist();
    	
    	//everything below here should be the exact same as the constructor below
    	//set basic variables
        num = setNum;
        threshold = setThresh;
        sigma = setSigma;
        dnc = setDNC;
        randRedist = setRedist;
        randExtract = setExtract;
        seed = setSeed;
        generator = new Random(seed);
        stochThresh = setSig;
        noncons = setNoncons;
        
        //instantiate currently unused variables - empty or filled with zeros
        changes = new LinkedList<IntDbl>();
        unstable = new LinkedList<Integer>();
        a = new double[num];
        for (int i = 0; i < num; i++)
            a[i] = initialA[i];
        slope = new double[num];
        
        //convert adjacency matrix into adjacency list
        int[] lengths = new int[num];
        int i, j;
        for (i = 0; i < num; i++)
            for (j = 0; j < num; j++)
                if (setConnect[i][j])
                    lengths[i]++;
        
        connect = new int[num][];
        int counter;
        for (i = 0; i < num; i++)
        {
            counter = 0;
            connect[i] = new int[lengths[i]];
            for (j = 0; j < num; j++)
                if (setConnect[i][j])
                    connect[i][counter++] = j;
        }
        for (i = 0; i < num; i++)
        {
            slope[i] = slopeAt(i);
            if (slope[i] > nextThresh(i))
                unstable.add(i);
        }
        
        //DEBUG
        /*for (i = 0; i < connect.length; i++)
            System.out.println(Arrays.toString(connect[i]));*/
    }
    public Sandpile(double[] initialA, int setNum,
                    boolean[][] setConnect, double setThresh,
                    double setSigma, double setDNC,
                    boolean setRedist, boolean setExtract,
                    boolean setSig, boolean setNoncons,
                    int setSeed)
    {
        //set basic variables
        num = setNum;
        threshold = setThresh;
        sigma = setSigma;
        dnc = setDNC;
        randRedist = setRedist;
        randExtract = setExtract;
        seed = setSeed;
        generator = new Random(seed);
        stochThresh = setSig;
        noncons = setNoncons;
        
        //instantiate currently unused variables - empty or filled with zeros
        changes = new LinkedList<IntDbl>();
        unstable = new LinkedList<Integer>();
        a = new double[num];
        for (int i = 0; i < num; i++)
            a[i] = initialA[i];
        slope = new double[num];
        
        //convert adjacency matrix into adjacency list
        int[] lengths = new int[num];
        int i, j;
        for (i = 0; i < num; i++)
            for (j = 0; j < num; j++)
                if (setConnect[i][j])
                    lengths[i]++;
        
        connect = new int[num][];
        int counter;
        for (i = 0; i < num; i++)
        {
            counter = 0;
            connect[i] = new int[lengths[i]];
            for (j = 0; j < num; j++)
                if (setConnect[i][j])
                    connect[i][counter++] = j;
        }
        for (i = 0; i < num; i++)
        {
            slope[i] = slopeAt(i);
            if (slope[i] > nextThresh(i))
                unstable.add(i);
        }
        
        //DEBUG
        /*for (i = 0; i < connect.length; i++)
            System.out.println(Arrays.toString(connect[i]));*/
    }
    
    //returns total energy in the system at current time
    public double totalEnergy()
    {
        double counter = 0.0;
        for (double i : a)
            counter +=  i * i;
        return counter;
    }
    
    //returns number of connections that a given node has
    private int numConnections(int i)
    {
        //System.out.println("NUM CONNECT: "+i);
        return connect[i].length;
    }
    
    //calculate slope (arbitrary number of connections)
    private double slopeAt(int i)
    {
        int totalConnect = numConnections(i);
        double counter = a[i],
               multiplier = 1.0/totalConnect;
               
        for (int j = 0; j < totalConnect; j++)
            counter -= multiplier * a[connect[i][j]];
            
        return counter;
    }
    
    //get next threshold value
    //NEED TO REWRITE THIS METHOD (RANDOM EXTRACTION NOT WORKING)
    private double nextThresh(int i)
    {
        //neither random extraction nor stochastic threshold
        if (!randExtract && !stochThresh)
        {
            return threshold;
        }
        //only a stochastic threshold
        else if (!randExtract && stochThresh)
        {
            return threshold + generator.nextGaussian() * sigma * sigma;
        }
        //only random extraction
        else if (randExtract && !stochThresh)
        {
            if (Math.abs(slope[i]) < threshold)
                return threshold;
            double minVal = Math.abs(slope[i]) - threshold,
                   maxVal = threshold;
            if (maxVal < minVal)
            {
                //EXCEPTION WAS THROWN
                //throw new RuntimeException("Error 1 because max="+maxVal+" but minVal="+minVal);
                return minVal;
            }
            double randVal = generator.nextDouble() * (maxVal - minVal) + minVal;
            return randVal;
        }
        //both stochastic threshold and random extraction
        else
        {
            double thresh = threshold;
            thresh += generator.nextGaussian() * sigma * sigma;
            if (Math.abs(slope[i]) < thresh)
                return thresh;
            double minVal = Math.abs(slope[i]) - thresh,
                   maxVal = thresh;
            if (maxVal < minVal)
                //EXCEPTION WAS THROWN
                //throw new RuntimeException("Error 2 because max="+maxVal+" but minVal="+minVal);
                return minVal;
            double randVal = generator.nextDouble() * (maxVal - minVal) + minVal;
            return randVal;
        }
        /*double ans = threshold;
        if (!randExtract && !stochThresh)
            return ans;
        else if (randExtract)
            return generator.nextDouble() * Math.abs(Math.abs(threshold) - (slope[i] - threshold)) + Math.abs(Math.abs(slope[i]) - threshold);
        else if (stochThresh)
            ans += generator.nextGaussian() * sigma * sigma;    //check this line (one sigma or two?)
        return ans;*/
    }
    
    //convert from changes[] to a[]
    private void implementChanges()
    {
        for (IntDbl curr : changes)
            a[curr.sq] += curr.amt;
        changes.clear();
        unstable.clear();   //check this line
        for (int i = 0; i < num; i++)
        {
            slope[i] = slopeAt(i);
            if (slope[i] > nextThresh(i))
                unstable.add(i);
        }
    }
    
    public void detDriveStep(double eps, PrintWriter fout)
    {
        if (unstable.size() == 0)
        {
            numCalled++;
            eps += 1.0;
            for (int i = 0; i < num; i++)
            {
                a[i] *= eps;
                slope[i] *= eps;
                if (slope[i] > nextThresh(i))
                    unstable.add(i);
            }
        }
        else
        {
            for (int i : unstable)
                redistribute(i);
            implementChanges();
        }
        
        if (fout != null)
        {
            fout.println(totalEnergy());
        }
    }
    
    protected static int NUM_STEPS = 125000;
    private double EPS_SIZE = 0.87;
    protected static int numCalled = 0;
    public void initEquilibrium(int steps, PrintWriter fout)
    {
        for (int i = 0; i < steps; i++)
        {
            //if (i % 1000 == 0)
                //System.out.println(i);
            //System.out.println(EPS_SIZE+" ");
            initEquilibriumStep(EPS_SIZE, fout);
        }
    }
    
    private void initEquilibriumStep(double eps, PrintWriter fout)
    {
        if (unstable.size() == 0)
        {
            //numCalled++;
            //System.out.println("CALLED: "+numCalled);
            int sq = generator.nextInt(num);
            //System.out.println("BEFORE: "+a[sq]+" "+eps);
            a[sq] += eps;
            //System.out.println("AFTER:  "+a[sq]+" "+eps);
            slope[sq] += eps;
            if (slope[sq] > nextThresh(sq))
                unstable.add(sq);
        }
        else
        {
            for (int i : unstable)
                redistribute(i);
            implementChanges();
        }
        if (fout != null)
        {
            //System.out.println(totalEnergy()+" "+numCalled);
            //System.out.println(this.toString());
            //System.exit(0);
            fout.println(totalEnergy());
        }
    }
    
    private boolean validRand(double[] rando, int i, double thresh)
    {
        double startingEnergy = a[i] * a[i];
        for (int j = 0; j < connect[i].length; j++)
            startingEnergy += a[connect[i][j]] * a[connect[i][j]];
        double endingEnergy = (a[i] + rando[0]) * (a[i] + rando[0]);
        double curr;
        for (int j = 0; j < connect[i].length; j++)
        {
            curr = a[connect[i][j]] + rando[j+1] * thresh;
            endingEnergy += curr * curr;
        }
        return (endingEnergy < startingEnergy);
    }
    
    private double[] genNormalizedRand(int len)
    {
        double[] ans = new double[len];
        double counter = 0.0;
        for (int j = 1; j < ans.length; j++)
        {
            ans[j] = generator.nextDouble();
            counter += ans[j];
        }
        for (int j = 1; j < ans.length; j++)
            ans[j] /= counter;
        //System.out.println(Arrays.toString(ans));
        return ans;
    }
    
    private void redistribute(int i)
    {
        double currThresh = nextThresh(i);
        if (randRedist)
        {
            double[] almostRando = null;
            boolean did = false;
            double currNC = 1.0;
            for (int run = 0; !did && run < 20; run++)
            {
                almostRando = genNormalizedRand(numConnections(i)+1);
                almostRando[0] = 0.0 - currThresh;
                //System.out.println(Arrays.toString(almostRando));
                
                //valid set of random numbers chosen
                double curr;
                if (validRand(almostRando, i, currThresh))
                {
                    did = true;
                    
                    //subtract from current
                    changes.add(new IntDbl(i, almostRando[0]));
                    
                    //add to neighbors (based on random proportions)
                    for (int j = 1; j < almostRando.length; j++)
                    {
                        if (noncons)
                            currNC = generator.nextDouble() * (1.0 - dnc) + dnc;
                        changes.add(new IntDbl(connect[i][j-1],almostRando[j] * currThresh * currNC));
                    }
                }
            }
            if (!did)
            {
                System.err.println("Couldn't find valid random redistribution.");
                
                //add anyway
                changes.add(new IntDbl(i, almostRando[0]));
                for (int j = 1; j < almostRando.length; j++)
                {
                    if (noncons)
                        currNC = generator.nextDouble() * (1.0 - dnc) + dnc;
                    changes.add(new IntDbl(connect[i][j-1], almostRando[j] * currThresh * currNC));
                }
            }
        }
        else
        {
            //subtract from current
            changes.add(new IntDbl(i, 0.0-currThresh));
            
            //add to neighbors (threshold/# neighbors)
            double nChange = currThresh/numConnections(i);
            double currNC;
            for (int j = 0; j < connect[i].length; j++)
            {
                currNC = generator.nextDouble() * (1.0 - dnc) + dnc;
                changes.add(new IntDbl(connect[i][j], nChange * currNC));
            }
        }
    }
    
    public String toString(int num)
    {
        String val = "0.";
        for (int i = 0; i < num; i++)
            val += "0";
        DecimalFormat df = new DecimalFormat(val);
        String ans = df.format(a[0]);
        for (int i = 1; i < a.length; i++)
            ans += "," + df.format(a[i]);
        return ans;
    }
    public String toString()
    {
        return toString(4);
    }
}