package tom.model;

import ML.QLearnBelief;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.Request;
import model.ResourceMappingNF;
import model.Substrate;
import model.components.Link;
import model.components.Node;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.*;

import org.apache.commons.collections15.multimap.MultiHashMap;

public class AlgorithmMILPmax extends Algorithm {

    public AlgorithmMILPmax() {
        super("MILPmax");
    }


    @Override
    protected double[][] allocateRequest(Request request, ResourceMappingNF reqMap) {
        long solveStartTime = System.nanoTime();
        // analyze substrate into arrays for cplex
        Substrate substrateCopy = getSubstrates().get(0);
        Graph<Node, Link> sub = substrateCopy.getGraph();
        ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
        ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
        // writer.write("Request: " +req.getId()+ " \n");
        int subNodesNum = subNodesList.size();
        double[] subNodes = new double[subNodesNum];

        for (Node x : subNodesList) {
            // subNodes[x.getId()] = x.getAvailableCpu();
            double old = x.getAvailableCpu(); //< getAvailableAllocatedCpu
            double neww = x.getAvailableAllocatedCpu();
            subNodes[x.getId()] = neww;


            // writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " +
            // x.getAvailableCpu() + "\n");
        }
        // Adjacency Matrix Substrate
        double[][] subLinks = new double[subNodesNum][subNodesNum];
        for (Link y : subLinksList) {
            Pair<Node> tmp = sub.getEndpoints(y);
            subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()] = y.getBandwidth();
            // writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " +
            // tmp.getSecond().getId()+" BW: " +
            // y.getBandwidth()+"\n");
        }

        /*
         * System.out.println("subNodesNum: "+subNodesNum);
         * System.out.println("subCap: "+Arrays.toString(subNodes));
         * System.out.println("subLinks: "+Arrays.deepToString(subLinks));
         */

        // analyze request into arrays for cplex

        int sfcNodes_num = request.getGraph().getVertexCount();
        double[] sfcNodes = new double[sfcNodes_num];
        String[] sfcNodeFunctions = new String[sfcNodes_num];
        // estimate revenue for request
        double proc_revenue = 0;

        // writer1.write("Request: " +req.getId()+ " \n");
        ArrayList<Node> req_n = (ArrayList<Node>) getNodes(request.getGraph());
        for (Node node : req_n) {
            sfcNodes[node.getId()] = node.getAvailableCpu();
            sfcNodeFunctions[node.getId()] = node.getName();
            // writer1.write("Node: " + node.getId() +" CPU: " +
            // node.getAvailableCpu()+"\n");
            proc_revenue += node.getAvailableCpu();
        }

        double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; // replace witn max

        double bw_revenue = 0;
        ArrayList<Link> links = (ArrayList<Link>) getLinks(request.getGraph());
        for (Link y : links) {
            Pair<Node> tmp = request.getGraph().getEndpoints(y);
            sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()] = y.getBandwidth();
            // writer1.write("Link " + y.getId()+ " : "+ y.getId()+ " : "+
            // tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
            // y.getBandwidth()+"\n");
            bw_revenue += y.getBandwidth();
        }

        ArrayList<Node> sfc = request.getNodes(request.getGraph());
        // redundant double naming... to lazy to change
        int sizeOfSFC = sfc.size();
        int numNodesSFC = sfc.size();
        // set revenue to reqMap
        reqMap.setEmbeddingRevenue(bw_revenue + proc_revenue);

        System.out.println("sfcNodes: " + Arrays.toString(sfcNodes));
        System.out.println("sfcLinks: " + Arrays.toString(sfcLinks[0]));

        try {


            IloCplex cplex = new IloCplex();
            cplex.setParam(IloCplex.DoubleParam.TiLim, 600); // 10 mins
            // cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
            // cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);

            /*****************************
             * System Variables
             **************************************************/
            // x^i_u if instance j of NF i is installed on susbtrate node u then x^i_u=1 \
            IloNumVar[][] x = new IloNumVar[subNodesNum][];
            for (int u = 0; u < subNodesNum; u++) {
                x[u] = new IloNumVar[sizeOfSFC];
                for (int i = 0; i < sizeOfSFC; i++) {
                    x[u] = cplex.numVarArray(sizeOfSFC, 0, 1, IloNumVarType.Int);
                }
            }

            // f^ij_uv
            IloNumVar[][][][] fl = new IloNumVar[subNodesNum][subNodesNum][][];
            for (int u = 0; u < subNodesNum; u++) {
                fl[u] = new IloNumVar[subNodesNum][][];
                for (int v = 0; v < subNodesNum; v++) {
                    fl[u][v] = new IloNumVar[sizeOfSFC][];
                    for (int i = 0; i < sizeOfSFC; i++) {
                        fl[u][v][i] = new IloNumVar[sizeOfSFC];
                        for (int j = 0; j < sizeOfSFC; j++) {
                            fl[u][v][i] = cplex.numVarArray(sizeOfSFC, 0, 1000000000);
                        }
                    }
                }
            }

            /*****************************
             * Objective Function
             **************************************************/

            IloLinearNumExpr cost = cplex.linearNumExpr();
            ////////////////////////////////////////////////////////////////////////
            // It builds the first summation of the objective function//////////////
            //////////////////////////////////////////////////////////////////////
            /*
             * for (int u=0;u<subNodesNum;u++){ for(int i=0; i< sizeOfSFC; i++){ double cpu
             * = sfcNodes[i]; cost.addTerm(cpu, x[u][i]); } }
             */

            ////////////////////////////////////////////////////////////////////////
            // It builds the second summation of the objective function//////////////
            ///////////////////////////////////////////////////////////////////// \
            IloLinearNumExpr flows = cplex.linearNumExpr();

            // double demand =0;
            // double cpu_demand =0;
            // for(int j=0; j<numNodesSFC; j++){
            // cpu_demand += sfcNodes[j];
            // }
            //
            // //calculate normalization factor
            // for(int i=0;i< numNodesSFC; i++){
            // for(int j=0; j<numNodesSFC; j++){
            // demand +=sfcLinks[i][j];
            // }
            // }
            for (int i = 0; i < numNodesSFC; i++) {
                for (int j = 0; j < numNodesSFC; j++) {
                    // demand = demand+sfcLinks[f][k][m];
                    for (int u = 0; u < subNodesNum; u++) {
                        for (int v = 0; v < subNodesNum; v++) {
                            flows.addTerm(1, fl[u][v][i][j]);
                            // flows.addTerm(cpu_demand/demand, fl[u][v][i][j]);
                            // System.out.println(u + " " +v + " " +f + " " + k + " " +m);
                        }
                    }

                }
            }

            // create objective minimization
            IloNumExpr expr = cplex.sum(flows, cost);
            cplex.addMinimize(expr);

            /*****************************
             * Capacity Constraints
             **************************************************/

            // node capacity
            for (int u = 0; u < subNodesNum; u++) {
                IloLinearNumExpr cpuReq = cplex.linearNumExpr();
                for (int i = 0; i < numNodesSFC; i++) {
                    double cpuNF = sfcNodes[i];
                    cpuReq.addTerm(cpuNF, x[u][i]);
                }
                double cpu = subNodes[u];
                cplex.addLe(cpuReq, cpu);
            }

            // link capacity
            for (int u = 0; u < subNodesNum; u++) {
                for (int v = 0; v < subNodesNum; v++) {
                    IloLinearNumExpr bwReq = cplex.linearNumExpr();
                    for (int i = 0; i < numNodesSFC; i++) {
                        for (int j = 0; j < numNodesSFC; j++) {
                            bwReq.addTerm(1, fl[u][v][i][j]);
                        }
                    }
                    double cap = subLinks[u][v];
                    cplex.addLe(bwReq, cap);
                }
            }

            /*****************************
             * Placement and Assignment Constraints
             **************************************************/

            // nf instance can be mapped to at most 1 substrate node
            for (int i = 0; i < numNodesSFC; i++) {
                IloLinearNumExpr assignment1 = cplex.linearNumExpr();
                for (int u = 0; u < subNodesNum; u++) {
                    assignment1.addTerm(1, x[u][i]);
                }
                cplex.addEq(assignment1, 1);
            }

            /*****************************
             * Flow Constraints
             **************************************************/
            for (int u = 0; u < subNodesNum; u++) {
                for (int i = 0; i < numNodesSFC; i++) {
                    for (int j = 0; j < numNodesSFC; j++) {
                        IloLinearNumExpr flow2 = cplex.linearNumExpr();
                        IloLinearNumExpr x_var = cplex.linearNumExpr();
                        IloLinearNumExpr x_var1 = cplex.linearNumExpr();
                        IloNumExpr expr1 = cplex.numExpr();
                        double cap = sfcLinks[i][j];
                        x_var.addTerm(cap, x[u][i]);
                        x_var.addTerm(-1 * cap, x[u][j]);
                        // expr1 = cplex.sum(x_var,x_var1);
                        for (int v = 0; v < subNodesNum; v++) {
                            flow2.addTerm(1, fl[u][v][i][j]);
                            flow2.addTerm(-1, fl[v][u][i][j]);
                        }
                        cplex.addEq(flow2, x_var);
                    }
                }

            }

            /*******************************************************************************/

            cplex.exportModel("lpex_MILP.lp");
            // long solveStartTime = System.nanoTime();
            boolean solvedOK = cplex.solve();
            // long solveEndTime = System.nanoTime();
            // long solveTime = solveEndTime - solveStartTime;
            System.out.println("solvedOK: " + solvedOK);

            double[][] xVar = new double[subNodesNum][sizeOfSFC];

            request.print();
            if (solvedOK) {

                System.out.println("###################################");
                // System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() +
                // ", Time (msec): " + ((double) solveTime / 1000000.0));
                System.out.println("Found an answer! CPLEX status: " + cplex.getStatus());
                cplex.output().println("Solution value = " + cplex.getObjValue());
                System.out.println("###################################");
                // substrateCopy.print();

                // nodeMapping requested-real
                for (int u = 0; u < subNodesNum; u++) {
                    for (int i = 0; i < sizeOfSFC; i++) {
                        xVar[u][i] = cplex.getValue(x[u][i]);
                    }
                }
            }

            cplex.end();

            return xVar;

        } catch (IloException e) {
            throw new RuntimeException(e);
        }


    }
}
