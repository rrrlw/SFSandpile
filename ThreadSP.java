import java.io.*;
import java.util.*;

public class ThreadSP extends Thread
{
    //members
    private Sandpile s;
    private PrintWriter fout;
    private int eqSteps,
                numSteps,
                id,
                numSims;
    private double epsilon;
    private String folderName,
                   outputFileName,
                   lotDataFile,
                   alphaFile,
                   lotDataFile2;
    
    //static variables
    private static final int UPDATE_GAP = 10000;
    
    //constructor
    public ThreadSP(int setNumSims)
    {
        numSims = setNumSims;
    }
    
    //static method
    private static String nextReadingLine()
    {
        if (ThreadDriver.paramIn.hasNextLine())
            return ThreadDriver.paramIn.nextLine();
        else
            return "";
    }
    
    public void run()
    {
        String paramLine, paramFile;
        String[] parameters;
        PrintWriter paramOut;
        for (int currRun = 0; currRun < numSims; currRun++)
        {
            paramLine = nextReadingLine();
            if (paramLine.equals(""))
            {
                System.err.println("QUIT");
                break;
            }
            
            //update message
            id = ThreadDriver.simCounter++;
            System.out.println("Starting Simulation #"+id);
            
            //file/folder names for simulation
            folderName = ThreadDriver.FOLDER_BASE.replace(ThreadDriver.FOLDER_REPL,
                                                          Integer.toString(id));
            outputFileName = folderName + "/" + ThreadDriver.LATTICE_FILE;
            lotDataFile = folderName + "/" + ThreadDriver.LOT_DATA;
            alphaFile = folderName + "/" + ThreadDriver.ALPHA_DATA;
            lotDataFile2= folderName + "/" + ThreadDriver.LOT_2_DATA;
            
            //create directory
            new File(folderName).mkdir();
            
            //setup output file stream
            try {
                fout = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
                
                //read in parameters
                parameters = paramLine.split(",");
                
                //store parameters in folder for future use (replication)
                paramFile = folderName + "/" + ThreadDriver.PARAM_FILE;
                paramOut = new PrintWriter(new BufferedWriter(new FileWriter(paramFile)));
                paramOut.println(ThreadDriver.headerRow);
                paramOut.println(paramLine);
                paramOut.close();
                
                //skip line if incorrect number of parameters
                if (parameters.length != ThreadDriver.NUM_PARAM)
                {
                    //DEBUG message
                    System.out.println("Incorrect # of parameters: "+parameters.length);
                
                    continue;
                }
                
                //convert parameters to proper types
                int width = Integer.parseInt(parameters[0]);
                int height= Integer.parseInt(parameters[1]);
                boolean wall = (Integer.parseInt(parameters[2]) == 1);
                double threshold = Double.parseDouble(parameters[3]);
                boolean stochThresh = (Integer.parseInt(parameters[4]) == 1);
                double sigma = Double.parseDouble(parameters[5]);
                boolean ncRedist = (Integer.parseInt(parameters[6]) == 1);
                double dnc = Double.parseDouble(parameters[7]);
                boolean randRedist = (Integer.parseInt(parameters[8]) == 1);
                boolean randExtract= (Integer.parseInt(parameters[9]) == 1);
                eqSteps = Integer.parseInt(parameters[10]);
                numSteps =Integer.parseInt(parameters[11]);
                epsilon = Double.parseDouble(parameters[12]);
                int seed = Integer.parseInt(parameters[13]);
                double beta = Double.parseDouble(parameters[14]);
                
                double[] initA = new double[width * height];
                boolean[][] adjMatrix;
                if (!wall)
                    adjMatrix = ConnGenerator.wattsStrogatz(height, width, false, beta);
                else
                    adjMatrix = ConnGenerator.wattsStrogatz(height, width, true, beta);
                    
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
                
                runSimulation();
            } catch (Exception e) {
                System.err.println("Thread failed.");
            }
            
        }
    }
    
    //run current simulation
    public void runSimulation()
    {
        //start message
        System.out.println("Starting simulation in Thread #"+id);
        
        //equilibrium
        s.initEquilibrium(eqSteps, fout);
        System.out.println("System "+id+" equilibration complete (Thread "+id+").");
        
        //run model
        for (int i = 0; i < numSteps; i++)
        {
            //print update
            if (i % UPDATE_GAP == 0)
                System.out.println("AT STEP #"+i+" in Thread #"+id);
            
            //drive system
            s.detDriveStep(epsilon, fout);
        }
        
        //close stuff up
        fout.close();
        System.out.println("System driving complete (Thread "+id+").");
        
        //print output to folder
        try
        {
            Formatter.analyzeA2(outputFileName, lotDataFile);
            System.out.println("Data formatting step complete (Thread "+id+").");
            
            //get power law values
            Formatter.getAlphas(lotDataFile, alphaFile);
            System.out.println("Power laws exponent estimation complete (Thread "+id+").");
            
            //get waiting time values
            Formatter.getWaitTimes(lotDataFile, lotDataFile2);
            System.out.println("Waiting time distributions calculated (Thread "+id+").");
            
            //delete A2 file & Data.csv file
            File del = new File(outputFileName);
            if (del.exists())
                del.delete();
            del = new File(lotDataFile);
            if (del.exists())
                del.delete();
            System.out.println("Data cleanup complete (Thread "+id+").");
            
            System.out.println("Done with simulation in Thread #"+id);
        }
        catch (Exception e)
        {
            System.err.println("Had an error in Thread #"+id+". "+e);
        }
    }
}