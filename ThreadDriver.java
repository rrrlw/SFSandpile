import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class ThreadDriver
{
    //constants
    protected static final String INPUT_FILE = "SimRun.csv",
                                  FOLDER_BASE= "Simulation X",
                                  FOLDER_REPL= "X",
                                  LATTICE_FILE="A2.txt",
                                  LOT_DATA = "Data.csv",
                                  LOT_2_DATA="Data2.csv",
                                  ALPHA_DATA = "Alphas.csv",
                                  PARAM_FILE = "Params.csv";
    protected static final int NUM_PARAM = 15,
                             UPDATE_GAP= 100000;
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private static int numThreadsRunning = 0;
    protected static Scanner paramIn;
    protected static int simCounter = getSimCounter();
    protected static String headerRow;
    
    //driver method
    public static void main(String[] args) throws IOException
    {
        //open input stream
        try
        {
            paramIn = new Scanner(new File(INPUT_FILE));
        }
        catch (IOException e)
        {
            System.out.println("Issue with input file: "+e+"\nPROGRAM TERMINATED.");
            return ;
        }
        
        //DEBUG message
        System.out.println("Input stream setup complete.");
        
        //read in header row
        headerRow = paramIn.nextLine();
        
        //DEBUG message
        System.out.println("Parameter row read.");
        
        //declare variables for use in each row
        Sandpile s;
        double[] initA;
        int width,
            height,
            eqSteps,
            seed;
        boolean wall,
                randRedist,
                randExtract,
                stochThresh,
                ncRedist;
        double threshold,
               sigma,
               dnc,
               epsilon;
        int i,
            numSim = 0,
            numSteps,
            simStartNum = simCounter,
            simEndNum;
        boolean[][] adjMatrix;
        
        //DEBUG message
        System.out.println("Found simulation counter value: "+simCounter);
            
        String folderName,
               outputFileName,
               lotDataFile,
               lotDataFile2,
               alphaFile,
               paramFile,
               paramLine;
        String[] parameters;
        PrintWriter fout,
                    paramOut;
        File makeFolder,
             del;
        
        //DEBUG message
        System.out.println("Variable declaration complete");
        
        ThreadSP[] t = new ThreadSP[1];
        t[0] = new ThreadSP(1);
        for (int tNum = 0; tNum < t.length; tNum++)
            t[tNum].start();
        
        //run a single simulation in each loop
        /*while (paramIn.hasNextLine())
        {
            //update message
            System.out.println("Starting Simulation #"+(simCounter));
            
            //update simulation counter
            numSim++;
            
            //figure out file/folder names for simulation
            folderName = FOLDER_BASE.replace(FOLDER_REPL,Integer.toString(simCounter++));
            outputFileName = folderName + "/" + LATTICE_FILE;
            lotDataFile = folderName + "/" + LOT_DATA;
            alphaFile = folderName + "/" + ALPHA_DATA;
            lotDataFile2 = folderName + "/" + LOT_2_DATA;
            
            //create directory
            new File(folderName).mkdir();
            
            //setup output file stream
            fout = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
            
            //read in parameters
            paramLine = paramIn.nextLine();
            parameters = paramLine.split(",");
            
            //store parameters in folder for future use (replication)
            paramFile = folderName + "/" + PARAM_FILE;
            paramOut = new PrintWriter(new FileWriter(paramFile));
            paramOut.println(headerRow);
            paramOut.println(paramLine);
            paramOut.close();
            
            //skip line if incorrect number of parameters
            if (parameters.length != NUM_PARAM)
            {
                //DEBUG message
                System.out.println("Incorrect # of parameters: "+parameters.length);
                
                continue;
            }
            
            //convert parameters to proper types
            width = Integer.parseInt(parameters[0]);
            height= Integer.parseInt(parameters[1]);
            wall = (Integer.parseInt(parameters[2]) == 1);
            threshold = Double.parseDouble(parameters[3]);
            stochThresh = (Integer.parseInt(parameters[4]) == 1);
            sigma = Double.parseDouble(parameters[5]);
            ncRedist = (Integer.parseInt(parameters[6]) == 1);
            dnc = Double.parseDouble(parameters[7]);
            randRedist = (Integer.parseInt(parameters[8]) == 1);
            randExtract= (Integer.parseInt(parameters[9]) == 1);
            eqSteps = Integer.parseInt(parameters[10]);
            numSteps =Integer.parseInt(parameters[11]);
            epsilon = Double.parseDouble(parameters[12]);
            seed = Integer.parseInt(parameters[13]);
            
            //get values of other variables based on inputed parameters
            initA = new double[width * height];
            
            if (!wall)
            {
                adjMatrix = ConnGenerator.allNoWall(height, width);
            }
            else
            {
                adjMatrix = ConnGenerator.allWall(height, width);
            }
            
            //initialize sandpile object
            s = new Sandpile(initA,                 //initial grid state
                             width * height,        //size of the grid
                             adjMatrix,             //adjacency matrix
                             threshold,             //threshold
                             sigma,                 //sigma
                             dnc,                   //dnc
                             randRedist,            //random redistribution
                             randExtract,           //random extraction
                             stochThresh,           //stochastic threshold
                             ncRedist,              //nonconservative redistribution
                             seed);                 //seed for pseudorandom number generator
            
            //equilibriate
            /*s.initEquilibrium(eqSteps, fout);
            //System.out.println(s.toString(1));
            
            //DEBUG message
            System.out.println("System equilibration complete.");
            
            //run model
            for (i = 0; i < numSteps; i++)
            {
                //print update
                if (i % UPDATE_GAP == 0)
                    System.out.println("AT STEP #"+i);
                    
                //drive system
                s.detDriveStep(epsilon, fout);
            }
            
            //DEBUG message
            System.out.println("System driving complete.");
            
            //close any open output streams
            fout.close();
            
            //print output to folder
            Formatter.analyzeA2(outputFileName, lotDataFile);
            
            //DEBUG message
            System.out.println("Data formatting step complete.");
            
            //get power law values
            Formatter.getAlphas(lotDataFile, alphaFile);
            
            //DEBUG message
            System.out.println("Power laws exponent estimation complete.");
            
            //get waiting time values
            Formatter.getWaitTimes(lotDataFile, lotDataFile2);
            
            //DEBUG message
            System.out.println("Waiting time distributions calculated.");
            
            //delete A2 file & Data.csv file (not needed b/c Data2.csv contains everything)
            del = new File(outputFileName);
            if (del.exists())
                del.delete();
            del = new File(lotDataFile);
            if (del.exists())
                del.delete();
            
            //DEBUG message
            System.out.println("Data cleanup complete.");
            
            ThreadSP a = new ThreadSP(s, fout,
                                    eqSteps, numSteps, epsilon,
                                    simCounter-1, folderName,
                                    outputFileName, lotDataFile,
                                    alphaFile, lotDataFile2);
            a.start();
            
            //print update
            System.out.println("Done with Simulation #"+(simCounter-1));
        }
        simEndNum = simCounter - 1;*/
        
        //Begin data formatting step (for individual simulations)
        /*String inputFile,
               outputFile;
        for (i = simStartNum; i <= simEndNum; i++)
        {
            //inputFile = 
        }*/
        
        //Begin data aggregation step (for individual models)
    }
    
    private static void keepStartingUntil(int upperLimThreads)
    {
        
    }
    
    //start a new thread with a simulation
    private static void startSim(Sandpile s, int eqSteps, int numSteps, double epsilon, PrintWriter fout)
    {
        
    }
    
    //find valid file name based on historical simulation counter
    private static int getSimCounter()
    {
        //declare variables for use later
        int count = 0;
        String currName = FOLDER_BASE.replace(FOLDER_REPL,Integer.toString(count++));
        
        //keep adding to count until valid file name is found
        File temp = new File(currName);
        
        //DEBUG message
        //System.out.println("Exists: "+currName+" "+temp.exists());
        
        while (temp.exists())
        {
            currName = FOLDER_BASE.replace(FOLDER_REPL,Integer.toString(count++));
            temp = new File(currName);
            
            //DEBUG message
            //System.out.println("Exists: "+currName+" "+temp.exists());
        }
        
        //return valid simulation number for file name
        return count-1;
    }
}