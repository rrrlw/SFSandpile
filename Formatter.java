import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class Formatter
{
    //static variables
    private static final DecimalFormat df;
    private static final String DELIMITER;
    
    //static initialization
    static
    {
        df = new DecimalFormat("0.000000");
        DELIMITER = ",";
    }
    
    //convert total lattice energy (A^2) data to T,P,E data
    protected static void analyzeA2(String inputFile, String outputFile) throws IOException
    {
        //setup input and output files
        File in = new File(inputFile),
             out= new File(outputFile);
             
        //setup input and output filestreams
        Scanner fin = new Scanner(in);
        PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(out)));
        
        //output header row
        fout.println("Step.Num,T,E,P");
        
        //declare and initialize variables for use later
        double last,                    //total energy at last iteration
               current;                 //total energy at current iteration
        int currAvalanche = 0;          //# of steps that current avalanche has gone on for
        double startAvalanche = 0.0,    //total energy at start of current avalanche
               peakEnergy = 0.0;        //peak energy loss in a single step during this avalanche
               
        //make sure file isn't empty
        if (fin.hasNextDouble())
            last = fin.nextDouble();
        else return ;
        
        //loop through input lines to find avalanches (when total energy decreases between iterations)
        for (int currStep = 1; fin.hasNextDouble(); currStep++)
        {
            //set current (to compare with last)
            current = fin.nextDouble();
            
            //if there's no avalanche going on right now
            if (currAvalanche == 0)
            {
                //if the start of an avalanche happens here (otherwise no action necessary)
                if (current < last)
                {
                    currAvalanche = 1;
                    peakEnergy = last - current;
                    startAvalanche = last;
                }
            }
            
            //if there is an avalanche going on right now
            else
            {
                //avalanche is still going
                if (current < last)
                {
                    currAvalanche++;
                    peakEnergy = Math.max(peakEnergy, last - current);
                }
                
                //avalanche ended
                else
                {
                    //StepNum, T, E, P (in that order)
                    fout.println((currStep - currAvalanche) + "," + currAvalanche + "," + df.format(Math.abs(startAvalanche - last)) + "," + peakEnergy);
                    
                    //reset variables
                    startAvalanche = 0.0;
                    peakEnergy = 0.0;
                    currAvalanche = 0;
                }
            }
            
            //update last variable (current will be read in next loop)
            last = current;
        }
        
        //close input and output filestreams
        fin.close();
        fout.close();
    }
    //take in input files and print out waiting time columns
    private static final int DATA_LINE_PARTS = 4;
    protected static void getWaitTimes(String inputFile, String outputFile) throws IOException
    {
        //setup IO files
        File in = new File(inputFile),
             out= new File(outputFile);
             
        //setup IO streams
        Scanner fin = new Scanner(in);
        PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(out)));
        
        //read header row (and output)
        fout.println(fin.nextLine() +",WaitT,WaitE,waitP");
        
        //read through data and line-by-line add waiting times
        ArrayList<Integer> step = new ArrayList<Integer>(),
                              t = new ArrayList<Integer>();
        ArrayList<Double> e = new ArrayList<Double>(),
                          p = new ArrayList<Double>();
        String line;
        String[] parts;
        int waitT,
            waitE,
            waitP,
            maxT = 0;
        double maxE = 0.0,
               maxP = 0.0;
        int lineNum, backCount;
        for (lineNum = 0; fin.hasNextLine(); lineNum++)
        {
            //read in data for line
            line = fin.nextLine();
            parts = line.split(DELIMITER);
            
            //check data validity
            if (parts.length != DATA_LINE_PARTS)
            {
                System.err.println("Incorrect number of arguments for waiting time calculation; skipping line.");
                continue;
            }
            
            //store data
            step.add(Integer.parseInt(parts[0]));
            t.add(Integer.parseInt(parts[1]));
            e.add(Double.parseDouble(parts[2]));
            p.add(Double.parseDouble(parts[3]));
            
            //add waiting time for T
            waitT = 0;
            if (t.get(lineNum) > maxT)
                waitT = -1;
            else
            {
                for (backCount = lineNum - 1; backCount >= 0 && t.get(backCount) < t.get(lineNum); backCount--);
                waitT = step.get(lineNum) - step.get(backCount);
            }
            
            //add waiting time for E
            waitE = 0;
            if (e.get(lineNum) > maxE)
                waitE = -1;
            else
            {
                for (backCount = lineNum - 1; backCount >= 0 && e.get(backCount) < e.get(lineNum); backCount--);
                waitE = step.get(lineNum) - step.get(backCount);
            }
            
            //add waiting time for P
            waitP = 0;
            if (p.get(lineNum) > maxP)
                waitP = -1;
            else
            {
                for (backCount = lineNum - 1; backCount >= 0 && p.get(backCount) < p.get(lineNum); backCount--);
                waitP = step.get(lineNum) - step.get(backCount);
            }
            
            //print out waiting times
            fout.println(step.get(lineNum) + 
                      "," + t.get(lineNum) +
                      "," + e.get(lineNum) +
                      "," + p.get(lineNum) +
                      "," + waitT +
                      "," + waitE +
                      "," + waitP);
            
            //update maximum counters
            maxT = Math.max(maxT, t.get(lineNum));
            maxE = Math.max(maxE, e.get(lineNum));
            maxP = Math.max(maxP, p.get(lineNum));
        }
        
        //close IO streams
        fin.close();
        fout.close();
    }
    //take in input files and print out power law values
    protected static void getAlphas(String inputFile, String outputFile) throws IOException
    {
        //setup IO files
        File in = new File(inputFile),
             out= new File(outputFile);
        
        //setup I stream
        Scanner fin = new Scanner(in);
        
        //read header row
        fin.nextLine();
        
        //read through data
        ArrayList<Integer> t = new ArrayList<Integer>();
        ArrayList<Double> e = new ArrayList<Double>(),
                          p = new ArrayList<Double>();
        String line;
        String[] parts;
        while (fin.hasNextLine())
        {
            line = fin.nextLine();
            parts = line.split(DELIMITER);
            
            if (parts.length != DATA_LINE_PARTS)
            {
                System.err.println("Incorrect number of arguments for alpha calculation; skipping line.");
                continue;
            }
            
            t.add(Integer.parseInt(parts[1]));
            e.add(Double.parseDouble(parts[2]));
            p.add(Double.parseDouble(parts[3]));
        }
        
        //close I stream & setup O stream
        fin.close();
        PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(out)));
        
        double alphaT = alphaMLEDisc(t),
               alphaE = alphaMLECont(e),
               alphaP = alphaMLECont(p);
               
        fout.println("Alpha.T,Alpha.E,Alpha.P");
        fout.println(alphaT + "," + alphaE + "," + alphaP);
        
        //close O stream
        fout.close();
    }
    //maximum likelihood estimation (MLE) of continuous data
    private static double alphaMLECont(Collection<Double> nums)
    {
        double minVal = Collections.min(nums),
               tempSum = 0.0;
               
        //make sure minVal is positive (otherwise we have an error)
        if (minVal <= 0)
            throw new IllegalArgumentException("Cannot calculate power law exponent of negative continuous parameters.");
        
        int n = nums.size();
        for (double curr : nums)
            tempSum += Math.log(curr / minVal);
        
        return (1.0 + (double) n / tempSum);
    }
    //maximum likelihood estimation (MLE) of discrete data
    private static double alphaMLEDisc(Collection<Integer> nums)
    {
        double minVal = Collections.min(nums) - 0.5,    //subtract 0.5 for approximation to discrete MLE (see Clauset et al. for more info)
               tempSum= 0.0;
               
        //make sure minVal is positive (otherwise we have an error)
        if (minVal <= 0)
            throw new IllegalArgumentException("Cannot calculate power law exponent of negative discrete parameters.");
        
        int n = nums.size();
        for (int curr : nums)
            tempSum += Math.log((double)curr / minVal);
            
        return (1.0 + (double) n / tempSum);
    }
}