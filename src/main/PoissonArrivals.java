package main;

import java.util.Random;

import cern.jet.random.Exponential;
import cern.jet.random.Uniform;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomGenerator;
import cern.jet.random.engine.RandomEngine;
import java.net.URL;
import java.net.URLClassLoader;
public class PoissonArrivals {
	
	
	
	
	public static void main(String[] args) {
		
	
		ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
        	System.out.print(url.getFile()+":");
        }

		int  n = (int) Math.random();
		MersenneTwister generator = new MersenneTwister(n);
		Uniform myUniformDist = new Uniform(generator);
		Exponential exp_arr = new Exponential(0.04,generator);
		int max =0;
		int min=10000;
		for (int i=0;i<5000;i++) {
		double startDate = myUniformDist.staticNextDoubleFromTo(0, 1);
		if (startDate<0.05)
		System.out.println((double)startDate);
		
		}
	}
	
/*	public double getNext() {
	    return  Math.log(1-rand.nextDouble())/(-lambda);
	}*/
}
