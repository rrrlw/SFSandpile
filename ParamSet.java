import java.text.DecimalFormat;

public class ParamSet
{
	//all parameters required for a simulation (include default values?)
	private int width,					//positive
				height,					//positive
				equilSteps,				//non-negative
				simSteps,				//non-negative
				seed;					//any integer
	private boolean walled,				//
					stochThreshold,		//if false, sigma = 0.0
					ncRedist,			//if false, dnc = 1
					randRedist,			//
					randExtract;		//
	private double threshold,			//positive
				   sigma,				//positive
				   dnc,					//0 <= dnc <= 1
				   epsilon,				//positive
				   beta;				//0 <= beta <= 1
				   
	
	private static final double EPS = 1e-8;		//epsilon for comparing doubles
	private static DecimalFormat df = new DecimalFormat("0.0000000000"),	//print during exceptions
								 printer = new DecimalFormat("0.000000");		//print for output to file
				   
	//constructor - sets all to default (except width, height which are provided)
	public ParamSet(int setWidth, int setHeight)
	{
		width = setWidth;
		height= setHeight;
		
		//defaults
		equilSteps = 0;
		simSteps = 0;
		seed = 42;
		walled = true;
		stochThreshold = false;
		sigma = 0.0;
		ncRedist = false;
		dnc = 1.0;
		randRedist = false;
		randExtract= false;
		threshold = 1.0;
		epsilon = 1e-6;
		beta = 0.0;
	}
	
	//setter methods (include checks)
	public double getBeta()
	{
		return beta;
	}
	public void setBeta(double set)
	{
		if (set < -EPS || set > 1.0 + EPS)
			customExcept("dnc", df.format(set));
		else if (set < 0)	//close enough to 0 (passed if statement)
			set = 0.0;
		else if (set > 1)	//close enough to 1 (passed if statement)
			set = 1.0;
			
		beta = set;
	}
	public double getEpsilon()
	{
		return epsilon;
	}
	public void setEpsilon(double set)
	{
		if (set < EPS)
			customExcept("epsilon", df.format(set));
		
		epsilon = set;
	}
	public double getDNC()
	{
		return dnc;
	}
	public void setDNC(double set)
	{
		if (set < -EPS || set > 1.0 + EPS)
			customExcept("dnc", df.format(set));
		else if (set < 0)	//close enough to 0 (passed if statement)
			set = 0.0;
		else if (set > 1)	//close enough to 1 (passed if statement)
			set = 1.0;
		
		dnc = set;
	}
	public double getSigma()
	{
		return sigma;
	}
	public void setSigma(double set)
	{
		if (set < EPS)
			customExcept("sigma", df.format(set));
			
		sigma = set;
	}
	public double getThresh()
	{
		return threshold;
	}
	public void setThresh(double set)
	{
		if (set < EPS)
			customExcept("threshold", df.format(set));
			
		threshold = set;
	}
	public boolean getRandExtract()
	{
		return randExtract;
	}
	public void setRandExtract(boolean set)
	{
		randExtract = set;
	}
	public boolean getRandRedist()
	{
		return randRedist;
	} 
	public void setRandRedist(boolean set)
	{
		randRedist = set;
	}
	public boolean get NonConserveRedist()
	{
		return ncRedist;
	}
	public void setNonConserveRedist(boolean set)
	{
		ncRedist = set;
		
		if (!ncRedist)
			dnc = 1.0;
	}
	public boolean getStochThresh()
	{
		return stochThreshold;
	}
	public void setStochThresh(boolean set)
	{
		stochThreshold = set;
		
		if (!stochThreshold)
			sigma = 0.0;
	}
	public boolean getWalled()
	{
		return walled;
	}
	public void setWalled(boolean set)
	{
		walled = set;
	}
	public int getSeed()
	{
		return seed;
	}
	public void setSeed(int set)
	{
		seed = set;
	}
	public int getSimSteps()
	{
		return simSteps;
	}
	public void setSimSteps(int set)
	{
		if (set < 0)
			customExcept("simulation steps", Integer.toString(set));
			
		simSteps = set;
	}
	public int getEquilSteps()
	{
		return equilSteps;
	}
	public void setEquilSteps(int set)
	{
		if (set < 0)
			customExcept("equilibration steps", Integer.toString(set));
			
		equilSteps = set;
	}
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int set)
	{
		if (set <= 0)
			customExcept("width", Integer.toString(set));
			
		width = set;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int set)
	{
		if (set <= 0)
			customExcept("height", Integer.toString(set));
			
		height = set;
	}
	public int getNum()
	{
		return width * height;
	}
	
	//csv format i/o methods
	/*public void readCSV()
	{
		
	}
	public String toCSVString()
	{
		
	}*/
	
	//generic method to throw an exception
	private void customExcept(String paramName, String paramVal)
	{
		throw new IllegalArgumentException("Invalid " + paramName + " parameter: " + paramVal);
	}
}