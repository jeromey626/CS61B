package tablut;

import static java.lang.Math.*;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Jerome Chen
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move A = findMove();
        if (A == null) {
            return "";
        }
        _controller.reportMove(A);
        return A.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int alp = Integer.MIN_VALUE;
        int bet = Integer.MAX_VALUE;
        int depth = maxDepth(b);
        if (b.turn() == BLACK) {
            int v = findMove(b, depth, true, -1, alp, bet);
        } else if (b.turn() == WHITE) {
            int v = findMove(b, depth, true, 1, alp, bet);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0) {
            return staticScore(board);
        } else {
            int fondVal = 0;
            if (sense == 1) {
                fondVal = findMax(board, depth, alpha, beta);
                if (saveMove) {
                    _lastFoundMove = _foundMax;
                }
            } else if (sense == -1) {
                fondVal = findMin(board, depth, alpha, beta);
                if (saveMove) {
                    _lastFoundMove = _foundMin;
                }
            }
            return fondVal;
        }
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        if (board.moveCount() >= 10) {
            return 4;
        }
        return 2;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        if (board.kingPosition() == null) {
            return -1 * WINNING_VALUE;
        } else if (board.kingPosition().isEdge()) {
            return WINNING_VALUE;
        }
        int bwPieces = board.numPieces(WHITE) - board.numPieces(BLACK);
        int numMoves = board.legalMoves(WHITE).size()
                - board.legalMoves(BLACK).size();
        return bwPieces + numMoves;
    }


    /**Find min at level.
     * @return the int for simpleFindMin.
     * @param board the Board.
     * @param alpha the alpha.
     * @param beta the beta. **/
    private int simpleFindMin(Board board, int alpha, int beta) {
        if (board.winner() == WHITE) {
            return WINNING_VALUE;
        } else if (board.winner() == BLACK) {
            return -1 * WINNING_VALUE;
        }
        int bestscorenow = Integer.MAX_VALUE;
        Board C = new Board();
        for (Move M: board.legalMoves(BLACK)) {
            C.copy(board);
            C.makeMove(M);
            int nextV = staticScore(C);
            if (nextV <= bestscorenow) {
                bestscorenow = nextV;
                beta = min(beta, nextV);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestscorenow;
    }

    /** Simple find max.
     * @return the in for simpleFindMin.
     * @param board the Board.
     *@param alpha the alpha.
     *@param beta the beta.*/
    private int simpleFindMax(Board board, int alpha, int beta) {
        if (board.winner() == WHITE) {
            return WINNING_VALUE;
        } else if (board.winner() == BLACK) {
            return -1 * WINNING_VALUE;
        }
        int bestscorenow = Integer.MIN_VALUE;
        Board C = new Board();
        for (Move M: board.legalMoves(WHITE)) {
            C.copy(board);
            C.makeMove(M);
            int nextV = staticScore(C);
            if (nextV >= bestscorenow) {
                bestscorenow = nextV;
                alpha = max(alpha, nextV);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestscorenow;
    }

    /** Find max.
     * @param board the board
     * @param  alpha the alpha
     * @param  beta the beta
     * @param depth the depth.
     * @return the int from findMax. */
    private int findMax(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return simpleFindMax(board, alpha, beta);
        }
        int bestsofar = Integer.MIN_VALUE;
        Move last = null;
        Board C = new Board();
        for (Move M: board.legalMoves(WHITE)) {
            C.copy(board);
            C.makeMove(M);
            int respVal = findMin(C, depth - 1, alpha, beta);
            if (respVal >= bestsofar) {
                bestsofar = respVal;
                last = M;
                alpha = max(alpha, respVal);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        _lastFoundMove = last;
        return bestsofar;
    }

    /** Find min.
     * @param board the board
     * @param  alpha the alpha
     * @param  beta the beta
     * @param depth the depth.
     * @return the int from findMin. */
    private int findMin(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return simpleFindMin(board, alpha, beta);
        }
        int bestsofar = Integer.MAX_VALUE;
        Move last = null;
        Board C = new Board();
        for (Move M: board.legalMoves(BLACK)) {
            C.copy(board);
            C.makeMove(M);
            int respVal = findMax(C, depth - 1, alpha, beta);
            if (respVal <= bestsofar) {
                bestsofar = respVal;
                last = M;
                beta = min(beta, respVal);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        _lastFoundMove = last;
        return bestsofar;
    }

    /** a move variable holder for min. */
    private Move _foundMin = null;

    /** a move variable holder for max. */
    private Move _foundMax = null;
}
