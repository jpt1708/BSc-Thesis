package model;


/**
 * DUMMY Algorithm Class.
 */

public class Algorithm {
	 

	private String id;
	private String state;

	public Algorithm(String id) {
		this.id = id;
		this.state = "available";
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	

	

	public double[][] runAlgorithm (){
		double[][] retres = null;

		
				
		return retres;
	}
	
public static void waiting (int n){
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        
        while (t1 - t0 < n);
}

	



		
	}
	

