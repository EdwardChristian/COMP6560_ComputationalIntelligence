import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Genetic algorithm for financial forecasting. COMP6560 Assignment 1
 * To use, compile GeneticAlgorithm.java and DataSet.java using "javac" (COMPILE DataSet.java first!!)
 * then run GeneticAlgorithm using "java"
 * 
 * I included the .class files, but it's a good idea to always recompile before use
 *
 * @author Edward-Christian Marin
 */
public class GeneticAlgorithm {
    /**
     * Number of bits of the individual encoding.
     */
    private static int BITS;

    /**
     * The population size.
     */
    private static final int POPULATION_SIZE = 100;

    /**
     * The number of generations.
     */
    private static final int MAX_GENERATION = 50;

    /**
     * The max weight
     */
    private static final double MAX_WEIGHT = 1.0;

    /**
     * The min weight
     */
    private static final double MIN_WEIGHT = -1.0;

    /**
     * Probability of the mutation operator.
     */
    private static final double MUTATION_PROBABILITY = 0.1;

    /**
     * Probability of the crossover operator.
     */
    private static final double CROSSOVER_PROBABILITY = 0.9;

    /**
     * Tournament selection size
     */
    private static final int TOURNAMENT_SIZE = 3;

    /**
     * Random number generation.
     */
    private Random random = new Random();

    /**
     * The current population.
     */
    private double[][] population;

    /**
     * Fitness values of each individual of the population.
     */
    private double[] fitness;

    /**
     * The input data object (default name is Dataset.csv)
     */
    private DataSet data = new DataSet("Dataset.csv");


    // Genetic Algorithm -----------------------------------------------//
    /**
     * Starts the execution of the GA.
     */
    private double[] run(List<boolean[]> data) {
        // set bits to number of technical indicator rules
        BITS = 6;

        //--------------------------------------------------------------//
        // initialises the population                                   //
        //--------------------------------------------------------------//
        initialise();

        //--------------------------------------------------------------//
        // evaluates the population                                    //
        //--------------------------------------------------------------//
        evaluate(data);

        for (int g = 0; g < MAX_GENERATION; g++) {
            //----------------------------------------------------------//
            // creates a new population                                 //
            //----------------------------------------------------------//

            double[][] newPopulation = new double[POPULATION_SIZE][BITS];
            // index of the current individual to be created
            int current = 0;

            while (current < POPULATION_SIZE) {
                double probability = random.nextDouble();

                // should we perform mutation?
                if (probability <= MUTATION_PROBABILITY || (POPULATION_SIZE - current) == 1) {
                    int parent = select();

                    double[] offspring = mutation(parent);
                    // copies the offspring to the new population
                    newPopulation[current] = offspring;
                    current += 1;
                }
                // otherwise we perform a crossover
                else {
                    int first = select();
                    int second = select();

                    double[][] offspring = crossover(first, second);
                    // copies the offspring to the new population
                    newPopulation[current] = offspring[0];
                    current += 1;
                    newPopulation[current] = offspring[1];
                    current += 1;
                }
            }

            population = newPopulation;

            //----------------------------------------------------------//
            // evaluates the new population                             //
            //----------------------------------------------------------//
            evaluate(data);
            // prints the value of the best individual
            int best = 0;

            for (int i = 1; i < POPULATION_SIZE; i++) {
                if (fitness[best] > fitness[i]) {
                    best = i;
                }
            }
            System.out.println("Fitness for gen " + g + ": " + fitness[best]);
            System.out.print("Best individual of this gen: [");

            for (int i = 0; i < BITS; i++) {
                System.out.print(" " + population[best][i]);
            }

            System.out.println(" ]");
            }

        // prints the value of the best individual
        int best = 0;

        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (fitness[best] > fitness[i]) {
                best = i;
            }
        }

        System.out.print("Best individual: [");

        for (int i = 0; i < BITS; i++) {
            System.out.print(" " + population[best][i]);
        }

        System.out.println(" ]");
        System.out.println("Fitness on training data: " +  fitness[best]);


        return population[best];
    }

    /*
     * Crossover function, one point
     */
    private double[][] crossover(int first, int second) {
        double[][] offspring = new double[2][BITS];
        int point = random.nextInt(BITS);
        
        for (int i = 0; i < BITS; i++) {
            if (i == point) {
                int k = first;
                first = second;
                second = k;
            }

            offspring[0][i] = population[first][i];
            offspring[1][i] = population[second][i];

        }
        
        return offspring;
    }

    /*
     * Mutation function, one point
     */
    private double[] mutation(int parent) {
        double[] offspring = new double[BITS];
        int point = random.nextInt(BITS);
        
        for (int i = 0; i < BITS; i++) {
            if (i == point) {
                offspring[i] = MIN_WEIGHT + (MAX_WEIGHT - MIN_WEIGHT) * random.nextDouble();
            }
            else {
                offspring[i] = population[parent][i];
            }
        }
        return offspring;
    }

    /*
     * Tournament selection with size set as final variable (default 5)
     */
    private int select() {
        // randomly select 5 individuals for the tournament
        ArrayList<Integer> individuals = new ArrayList<Integer>();
        // add all possible indexes to an array
        for(int i = 0;i < POPULATION_SIZE;i++) individuals.add(i);
        // shuffle the array
        Collections.shuffle(individuals);
        // select the first k individuals (it's been shuffled so it's random)
        int winner = individuals.get(0);
        for(int i = 1;i < TOURNAMENT_SIZE;i++) {
            if(fitness[winner] > fitness[individuals.get(i)]){
                winner = individuals.get(i);
            }
        };
        return winner;
    }

    /*
     * Fitness function for training data
     */
    private void evaluate(List<boolean[]> data) {
        double prediction;
        // for each individual
        for (int i = 0; i < POPULATION_SIZE; i++) {
            fitness[i] = 0.0; // initial fitness is 0.0
            // for each data point
            for(int j = 0; j < data.size(); j++){
                boolean[] dataPoint = data.get(j);
                prediction = predict(population[i],dataPoint);
                // fitness = (prediction - real_increase)^2
                fitness[i] += Math.pow((prediction - formatYFitness(dataPoint[dataPoint.length - 1])), 2);
            }            
        }
        
    }

    /*
     * Individual prediction function, use this to get a prediction from an individual.
     */
    private static double predict(double[] individual, boolean[] dataPoint){
        double predict = 0.0;
        double sum = 0.0;
        for(int i = 0; i < individual.length; i++){
            // sum of weight * rule
            sum += individual[i] * formatYSigmoid(dataPoint[i]);
        }
        // apply Sigmoid function to sum to get prediction (smoothed with a confidence factor in)
        predict = sigmoid(sum);
        return predict;
    }

    /**
     * Function that formats the input data. false becomes -1 and true becomes 1 (from boolean to integer)
     */
    private static int formatYSigmoid(boolean toFormat){
        if(toFormat == false){
            return -1;
        } else {
            return 1;
        }
    }
    /**
     * Function that formats the input data. false becomes 0 and true becomes 1 (from boolean to integer)
     */
    private static int formatYFitness(boolean toFormat){
        if(toFormat == false){
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Standard sigmoid function
     */
    private static double sigmoid(double x)
    {
        return 1 / (1 + Math.exp(-x));
    }

    private void initialise() {
        population = new double[POPULATION_SIZE][BITS];
        fitness = new double[POPULATION_SIZE];

        for (int i = 0; i < population.length; i++) {
            population[i] = new double[BITS];
            for (int j = 0; j < population[i].length; j++) {
                population[i][j] = MIN_WEIGHT + (MAX_WEIGHT - MIN_WEIGHT) * random.nextDouble();
            }
            System.out.print("Init individual: [");

            for (int j = 0; j < BITS; j++) {
                System.out.print(" " + population[i][j]);
            }
    
            System.out.println(" ]");
        }
    }

    public static void main(String[] args){
        // initialize the algorithm object
        GeneticAlgorithm ga = new GeneticAlgorithm();
        // read the data from csv
        List<boolean[]> dataArray = ga.data.readData();
        // separate about 70% of data as training data and 30% as evaluation data
        int trainingAmount = (dataArray.size()*70)/100;
        List<boolean[]> trainingData = dataArray.subList(0, trainingAmount);
        List<boolean[]> evaluationData = dataArray.subList(trainingAmount,dataArray.size());
        // run the GA to get the best performing rule on training data
        double[] champion = ga.run(trainingData);

        // evaluate this rule against evaluation data
        double championFitness = 0.0; // initial fitness is 0.0
        double prediction;
        boolean formattedPrediction;
        // for each data point
        for(int i = 0; i < evaluationData.size(); i++){
            // get our prediction for this data point for the current individual
            boolean[] dataPoint = evaluationData.get(i);
            prediction = predict(champion,dataPoint);
            // Step function to get a 0 or 1 prediction
            if(prediction >= 0.5){
                formattedPrediction = true;
            } else {
                formattedPrediction = false;
            }
            if(formattedPrediction != dataPoint[dataPoint.length - 1]){
                championFitness++;
            }
        }
        System.out.println("=========TESTING RESULTS=========");
        System.out.println("Incorrect predictions on evaluation data: " + championFitness + " out of " + evaluationData.size());
        // calculate percentage of correct predictions on training data
        double rateOfSuccess = ((evaluationData.size()-championFitness)/evaluationData.size())*100;
        System.out.println("Rate of success on evaluation data: " +  rateOfSuccess + "%");

    }
}
