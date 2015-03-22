/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RecipExperiment;

import java.util.ArrayList;

/**
 *
 * @author legodude
 */
public class RecipConstants {
//    id=0prob=0.400593435609818 w0 4.583213799933576 w1 3.5106459682597135 w2 3.3128815561461344
//id=1prob=0.29984153928925705 w0 4.689080166396826 w1 2.749919833304573 w2 1.937217603928093
//id=2prob=0.29956502510092486 w0 4.3605127397780485 w1 3.2512747502092254 w2 2.6529543110164826 
    
    public static final int numRounds = 20;
    public static final String LOG_SERVER = "bogglemac2.dyndns.org";
    
    public static final int minProposeTime = 20;
    public static final int maxProposeTime = 60;
    public static final int minRespondTime = 20;
    public static final int maxRespondTime = 60;
    
    public ArrayList<ArrayList<Double>> retroProbWeights = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> socialPrefProbWeights = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> NoRecipProbweights = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<ArrayList<Double>>> prospectiveMeritCoeffsProposer = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<ArrayList<Double>>> prospectiveMeritCoeffsResponder = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<Double>> prospectiveMeritProbWeights = new ArrayList<ArrayList<Double>>();

    public RecipConstants(){
        ArrayList<Double> weights;
        
        
        //---------------Begin retroProbWeights---------------------------
        weights = new ArrayList<Double>();
        weights.add( 0.400593435609818 ); //prob
        weights.add( 4.583213799933576 );
        weights.add( 3.5106459682597135 );
        weights.add( 3.3128815561461344 );
        
        retroProbWeights.add(weights);
        
        weights = new ArrayList<Double>();
        weights.add( 0.29984153928925705 ); //prob
        weights.add( 4.689080166396826 );
        weights.add( 2.749919833304573 );
        weights.add( 1.937217603928093 );
        
        retroProbWeights.add(weights);
        
        weights = new ArrayList<Double>();
        weights.add( 0.29956502510092486 ); //prob
        weights.add( 4.3605127397780485 );
        weights.add( 3.2512747502092254 );
        weights.add( 2.6529543110164826 );
        
        retroProbWeights.add(weights);
        
//        weights = new ArrayList<Double>();
//        weights.add( 0.400593435609818 );
//        weights.add( 4.583213799933576 );
//        weights.add( 0.0 );
//        weights.add( 3.3128815561461344 );
//        
//        retroProbWeights.add(weights);
//        
//        weights = new ArrayList<Double>();
//        weights.add( 0.29984153928925705 );
//        weights.add( 4.689080166396826 );
//        weights.add( 0.0 );
//        weights.add( 1.937217603928093 );
//        
//        retroProbWeights.add(weights);
//        
//        weights = new ArrayList<Double>();
//        weights.add( 0.29956502510092486 );
//        weights.add( 4.3605127397780485 );
//        weights.add( 0.0 );
//        weights.add( 2.6529543110164826 );
//        
//        retroProbWeights.add(weights);
        
        //---------------End retroProbWeights---------------------------
        
        //---------------Begin socialPrefProbWeights---------------------------
        
        //(0.36, 0.63) and feature weights were 
        //(3.00, 5.13, 4.61, 0.46) and (3.13, 4.95, 0.47, 3.30)
        
        weights = new ArrayList<Double>();
        weights.add( .36 ); //prob
        weights.add( 3.00 );
        weights.add( 5.13 );
        weights.add( 4.61 );
        weights.add( 0.46 );
        
        socialPrefProbWeights.add(weights);
        
        weights = new ArrayList<Double>();
        weights.add( .64 ); //prob
        weights.add( 3.13 );
        weights.add( 4.95 );
        weights.add( 0.47 );
        weights.add( 3.30 );
        
        socialPrefProbWeights.add(weights);
        
        //---------------End socialPrefProbWeights---------------------------
        


        //---------------Begin NoRecipWeights ---------------------------

        //(0.36, 0.63) and feature weights were
        //(3.00, 5.13, 4.61, 0.46) and (3.13, 4.95, 0.47, 3.30)

        weights = new ArrayList<Double>();
        weights.add( 1.0 ); //prob
        weights.add( 4.5 );
        weights.add( 5.13 );

        NoRecipProbweights.add(weights);



        //---------------End NoRecipWeights ---------------------------
        
        //---------------Begin prospective merit coeff---------------------------
//         Allocator
//  h1 co=17.6 -10.09  h2 co=46.17 51.24  h3 co=60.5 44.61  h4 co=85.27 107.31  h5 co=96.98 104.6
      
        ArrayList<ArrayList<Double>> probCoeffs;
        ArrayList<Double> coeffs;
        
        probCoeffs = new ArrayList<ArrayList<Double>>();
         
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 17.6  ); 
        coeffs.add( -10.09  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 46.17  ); 
        coeffs.add( 51.24  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 60.5  ); 
        coeffs.add( 44.61  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 85.27  ); 
        coeffs.add( 107.31  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 96.98  ); 
        coeffs.add( 104.6  ); 
        probCoeffs.add( coeffs );
        
        prospectiveMeritCoeffsProposer.add(probCoeffs);
        
 //Deliberator
//
//  h1 co=29.62 58.08  h2 co=43.93 47.4  h3 co=69.58 105.81  h4 co=81.59 99.16  h5 co=104.33 160.47
         
        
        probCoeffs = new ArrayList<ArrayList<Double>>();
        coeffs = new ArrayList<Double>();

        
        coeffs = new ArrayList<Double>();
        coeffs.add( 29.62  ); 
        coeffs.add( 58.08  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 43.93  ); 
        coeffs.add( 47.4  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 69.58  ); 
        coeffs.add( 105.81  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 81.59  ); 
        coeffs.add( 99.16  ); 
        probCoeffs.add( coeffs );
        
        coeffs = new ArrayList<Double>();
        coeffs.add( 104.33  ); 
        coeffs.add( 160.47  ); 
        probCoeffs.add( coeffs );
        
        prospectiveMeritCoeffsResponder.add(probCoeffs);
        

        weights = new ArrayList<Double>();
        weights.add( 1.0 );
        weights.add( 5.16 );
        weights.add( 4.48 );
        weights.add( 3.95);
        weights.add( 2.91);

        prospectiveMeritProbWeights.add(weights); 
        
        //---------------End prospective merit coeff---------------------------

        
    
    }
    
}
