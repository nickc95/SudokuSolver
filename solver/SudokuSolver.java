package sudoku;
import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random ;

/**
 * Place for your code.
 */
public class SudokuSolver {

    // Arc class
    // Represents link between 2 cells
    public class Arc {
        int current;
        int next;

        public Arc(int x, int y) {
            current = x;
            next = y;
        }
    }

    /**
     * @return names of the authors and their student IDs (1 per line).
     */
    public String authors() {
        return "Name: Nicholas Chim   Student Id: 48250138";
    }

    /**
     * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
     *
     * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
     * @return the solved Sudoku board
     */
    public int[][] solve(int[][] board) {

        // sudoku board array indices
        // 00 01 02  03 04 05  06 07 08
        // 09 10 11  12 13 14  15 16 17
        // 18 19 20  21 22 23  24 25 26
        //
        // 27 28 29  30 31 32  33 34 35
        // 36 37 38  39 40 41  42 43 44
        // 45 46 47  48 49 50  51 52 53
        //
        // 54 55 56  57 58 59  60 61 62
        // 63 64 65  66 67 68  69 70 71
        // 72 73 74  75 76 77  78 79 80

        int[] solution = new int[81];
        ArrayList<Integer>[] domains = new ArrayList[81];
        int index = 0;

        // convert game board into 1d array
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++ ) {
                solution[index++] = board[x][y];
            }
        }

        // initialise domain list
        for (int i = 0; i < 81; i++) {
            domains[i] = new ArrayList<Integer>();
        }

        // initialise all domains (each variable's possible numbers)
        for (int x = 0; x < solution.length; x++) {
            // variable is empty (zero), all domains possible
            if (solution[x] == 0) {
                for (int y = 1; y <= 9; y++) {
                    domains[x].add(y);
                }
            }
            // variable already set
            else {
                domains[x].add(solution[x]);
            }
        }

        // perform arc consistency with domain splitting
        solution = gac(solution, domains);

        // convert solution array back to board array
        if (solution != null) {
            index = 0;
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 9; y++) {
                    board[x][y] = solution[index++];
                }
            }
        }
        return board;
    }

    // performs arc consistency with domain splitting
    // parameters: sol, the 1d array representing the game board.
    //             doms, the 2d array list, containing domains of each cell.
    // returns: a solved sudoku board, null otherwise
    private int[] gac(int[] solution, ArrayList<Integer>[] domains) {
        ArrayList<Integer>[] leftDomain = new ArrayList[81];
        ArrayList<Integer>[] rightDomain = new ArrayList[81];
        int[] solutionA = new int[81];
        int[] solutionB = new int[81];
        int domainToSplit = 0;
        int mid = 0;

        // initialise variables
        for (int i = 0; i < 81; i++) {
            leftDomain[i] = new ArrayList<Integer>();
            rightDomain[i] = new ArrayList<Integer>();
            solutionA[i] = solution[i];
            solutionB[i] = solution[i];
        }

        // initialise left and right domains (split)
        for (int i = 0; i < 81; i++) {
            copyArray(domains[i], leftDomain[i]);
            copyArray(domains[i], rightDomain[i]);
        }

        // select domain to split
        for (int x = 0; x < 81; x++) {
            if (domains[x].size() > 1){
                domainToSplit = x;
            } else if (domains[x].size() == 0) {
                return null;
            }
        }

        // if no domain needs to be split,
        // check if solution is correct and return board
        if (domainToSplit == 0) {
            ac(solution, domains);

            for (int x = 0; x < 81; x++) {
                if ((solution[x] == 0) && (domains[x].size() == 1)) {
                    solution[x] = domains[x].get(0);
                }
            }
            return solution;
        }

        // find middle index to split
        mid = (domains[domainToSplit].size() / 2);

        // first half of domain given to leftDomain
        for (int i = (leftDomain[domainToSplit].size() - 1); leftDomain[domainToSplit].size() > mid; i--) {
            leftDomain[domainToSplit].remove(i);
        }

        // rest of domain given to rightDomain
        for (int i = mid; i > 0; i--){
            rightDomain[domainToSplit].remove(0);
        }

        // perform ac on left and right domains
        ac(solutionA, leftDomain);
        ac(solutionB, rightDomain);

        // repeat domain split on each half of domain
        solutionA = gac(solutionA, leftDomain);
        solutionB = gac(solutionB, rightDomain);

        // return solution
        if (solutionA != null) {
            return solutionA;
        } else { // solutionB either null (no solution) or not null
            return solutionB;
        }
    }

    // arc consistency algorithm
    // parameters: sol, the 1d array representing game board.
    //             doms, the 2d array representing domains of each cell in board
    // returns: an arc consistent sudoku solution
    private void ac(int[] solution, ArrayList<Integer>[] domains) {
        LinkedList<Arc> TDA = new LinkedList<Arc>();
        ArrayList<Integer> consistent; // a list of consistent domains
        Iterator<Integer> currentDomains;
        Iterator<Integer> nextDomains;
        Arc currentArc;
        Integer d1;
        Integer d2;
        int current;
        int next;

        // initialise ToDoArcs
        for (int i = 0; i < 81; i++) {
            getTDA(TDA, i);
        }

        // perform loop as long as there is an arc in todo
        while (TDA.size() > 0) {
            // retrieve an arc from TDA
            currentArc = TDA.remove();
            current = currentArc.current;
            next = currentArc.next;
            consistent = new ArrayList<Integer>();
            // get domains from current cell
            currentDomains = domains[current].iterator();

            // iterate through all domains in current cell
            while (currentDomains.hasNext()) {
                d1 = currentDomains.next();
                // get domains from next cell
                nextDomains = domains[next].iterator();


                // while next cell still has a domain
                while (nextDomains.hasNext()) {
                    d2 = nextDomains.next();
                    // if there is no match in domains
                    if (!d1.equals(d2)) {
                        // add domain to list of consistent domains
                        consistent.add(d1);
                        break;
                    }
                }
            }

            // if a domain was removed, we need to recheck consistency
            if(consistent.size() != domains[current].size()) {
                getTDA(TDA, current);
                // update domain
                copyArray(consistent, domains[current]);
            }
        }
    }

    // get ToDoArcs
    // parameters: TDA, a linked list of all arcs that we have to check
    //             p, the position of the cell to be added into TDA
    // returns: TDA with all arcs of all cells
    private void getTDA(LinkedList<Arc> TDA, int p) {
        // get row and column values for p
        int r = getRow(p);
        int c = getCol(p);
        int col_block = (c / 3);
        int row_block = (r / 3);

        for (int i = 0; i < 9 ; i++) {
            // add all other cells in same row as p
            if (i != c) {
                TDA.add(new Arc(p, getPosition(r, i)));
            }

            // add all other cells in same column as p
            if (i != r) {
                TDA.add(new Arc(p, getPosition(i, c)));
            }
        }

        // add all arcs within block p is located in
        for (int i = (col_block * 3); i < (col_block * 3) + 3; i++) {
            for (int j = (row_block * 3); j < (row_block * 3) + 3; j++) {
                if (i != c && j != r) {
                    TDA.add(new Arc(p, getPosition(j, i)));
                }
            }
        }
    }

    // gets the row of position p
    // parameters: p is cell position
    // returns: row number of p
    private int getRow(int p) {
        return (int) Math.floor(p / 9);
    }


    // gets the column of position p
    // parameters: p is cell position
    // returns: column number of p
    private int getCol(int p) {
        return (p % 9);
    }

    // gets position of coordinate (x,y)
    // parameters: x is row, y is column
    // returns: position of (x,y) in 1d array
    private int getPosition(int x, int y) {
        int pos = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (x == i && y == j) {
                    return pos;
                } else {
                    pos++;
                }
            }
        }
        return pos;
    }

    // perform copy on array
    // parameters: array, the array to be copied
    //             target, the target array to be copied to
    private void copyArray(ArrayList<Integer> array, ArrayList<Integer> target) {
        Iterator<Integer> itr = array.iterator();
        Integer i = 0;

        target.clear();

        while (itr.hasNext()) {
            i = itr.next();
            target.add(i);
        }
    }
}