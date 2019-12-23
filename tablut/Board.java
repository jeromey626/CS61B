package tablut;


import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Jerome Chen
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        nboard = model.nboard;
        init();
    }

    /** Clears the board to the initial position. */
    void init() {
        this.nboard.clear();
        this.put(KING, THRONE);
        for (int i = 0; i < INITIAL_DEFENDERS.length; i++) {
            this.put(WHITE, INITIAL_DEFENDERS[i]);
        }
        for (int i = 0; i < INITIAL_ATTACKERS.length; i++) {
            this.put(BLACK, INITIAL_ATTACKERS[i]);
        }
        for (Square sq : SQUARE_LIST) {
            if (nboard.get(sq) == null) {
                this.put(EMPTY, sq);
            }
        }
        _allStatesStr.clear();
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        _repeated = false;
        _allStates.push((HashMap) nboard.clone());
        _allStatesStr.add(encodedBoard().substring(1));
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n the move limit to be set. */
    void setMoveLimit(int n) {
        lim = n;
        if (2 * lim <= moveCount()) {
            throw new Error("bad limit");
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String currentSt = encodedBoard().substring(1);
        if (_allStatesStr.contains(currentSt)) {
            _winner = this.turn().opponent();
            _repeated = true;
        }
        HashMap cloner = (HashMap) this.nboard.clone();
        _allStates.push(cloner);
        _allStatesStr.push(currentSt);
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (Square sq : SQUARE_LIST) {
            if (this.get(sq) == KING) {
                return sq;
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return this.nboard.get(sq(col, row));
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        this.nboard.put(s, p);
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        this.nboard.put(s, p);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (!from.isRookMove(to)) {
            return false;
        }
        int c1 = from.col();
        int r1 = from.row();
        int c2 = to.col();
        int r2 = to.row();
        if (c1 == c2) {
            if (r2 - r1 > 0) {
                for (int a = r1 + 1; a <= r2; a++) {
                    if (this.get(sq(c1, a)) != EMPTY) {
                        return false;
                    }
                }
            } else if (r2 - r1 < 0) {
                for (int a = r1 - 1; a >= r2; a--) {
                    if (this.get(sq(c1, a)) != EMPTY) {
                        return false;
                    }
                }
            }
        } else if (r1 == r2) {
            if (c2 - c1 > 0) {
                for (int a = c1 + 1; a <= c2; a++) {
                    if (this.get(sq(a, r1)) != EMPTY) {
                        return false;
                    }
                }
            } else if (c2 - c1 < 0) {
                for (int a = c1 - 1; a >= c2; a--) {
                    if (this.get(sq(a, r1)) != EMPTY) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (this.get(from) == EMPTY
            || this.get(to) != EMPTY
            || (this.get(from) != turn() && this.get(from) != KING)
                || (this.get(from) == KING && turn() != WHITE)
                || !isUnblockedMove(from, to)
                || (to == THRONE && this.get(from) != KING)) {
            return false;
        }
        return true;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** helper hostile square checker.
     * @return a sqaure if there is a hostile square.
     * @param dir the direction of the square.
     * @param sq1 the reference square. **/
    private Square hostility(Square sq1, int dir) {
        Square oth = sq1.rookMove(dir, 2);
        if (oth == null) {
            return null;
        } else if (oth == THRONE) {
            return oth;
        }
        if (turn() == WHITE && (this.get(sq1) == WHITE
                || this.get(sq1) == KING)) {
            if (this.get(oth) == WHITE || this.get(oth) == KING) {
                return oth;
            }
        } else {
            if (this.get(sq1) == turn() && this.get(oth) == this.turn()) {
                return oth;
            }
        }
        return null;
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        if (lim != 0) {
            if (_moveCount / 2 >= lim) {
                _winner = this.turn().opponent();
            }
        }
        this.put(this.get(from), to);
        this.put(EMPTY, from);
        if (hostility(to, 1) != null) {
            capture(to, hostility(to, 1));
        }
        if (hostility(to, 3) != null) {
            capture(to, hostility(to, 3));
        }
        if (hostility(to, 0) != null) {
            capture(to, hostility(to, 0));
        }
        if (hostility(to, 2) != null) {
            capture(to, hostility(to, 2));
        }
        this.movesMade.push(mv(from, to));
        _moveCount += 1;
        checkRepeated();


        if (kingPosition() == null) {
            _winner = BLACK;
        }
        if (kingPosition().isEdge()) {
            _winner = WHITE;
        }
        if (!hasMove(_turn)) {
            _winner = this._turn.opponent();
        }
        changeturn();
        _moveCount += 1;

    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        HashSet<Square> adjthrone = new HashSet<>();
        adjthrone.add(THRONE);
        adjthrone.add(NTHRONE);
        adjthrone.add(ETHRONE);
        adjthrone.add(STHRONE);
        adjthrone.add(WTHRONE);
        int dir = sq0.direction(sq2);
        if (sq0.rookMove(dir, 2) != sq2) {
            return;
        }

        if (get(sq0) != get(sq2)
            && sq2 != THRONE) {
            return;
        }
        Square mid = sq0.between(sq2);
        if (get(mid) == KING && get(sq0) == BLACK) {
            if (adjthrone.contains(mid)) {
                boolean across = get(sq2) == BLACK || sq2 == THRONE;
                Square diaOne = sq0.diag1(sq2);
                Square diaTwo = sq0.diag2(sq2);
                boolean dOne = get(diaOne) == BLACK || diaOne == THRONE;
                boolean dTwo = get(diaTwo) == BLACK || diaTwo == THRONE;
                if (across && dOne && dTwo) {
                    put(EMPTY, mid);
                }
            }
            return;
        } else if (sq2 == THRONE && get(sq2) == KING) {
            int hostile = 0;
            for (Square sq : adjthrone) {
                if (get(sq) == BLACK && sq != mid) {
                    hostile += 1;
                }
            }
            if (hostile == 3) {
                if (get(sq0) == BLACK && get(mid) == WHITE) {
                    put(EMPTY, mid);
                }
            }
            return;
        }
        if (get(sq0) == WHITE && get(mid) == KING) {
            return;
        } else if (get(mid) == KING
                && mid == THRONE) {
            return;
        } else if (get(sq0) != get(mid)
                && get(mid) != EMPTY) {
            put(EMPTY, mid);
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            this.movesMade.pop();
            this._moveCount -= 1;
            changeturn();
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (!this._repeated || _allStates.size() > 1) {
            this._allStatesStr.remove(encodedBoard());
            this._allStates.pop();
            this.nboard = this._allStates.peek();
        }
        this._repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        this.movesMade.clear();
        this._moveCount = 0;
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> possiblemoves = new ArrayList<>();
        for (Square sq : SQUARE_LIST) {
            if (this.get(sq).side() == side) {
                for (Square sq2 : SQUARE_LIST) {
                    if (sq != sq2 && isLegal(sq, sq2)
                            && isUnblockedMove(sq, sq2)) {
                        possiblemoves.add(mv(sq, sq2));
                    }
                }
            }
        }
        return possiblemoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locations = new HashSet<>();
        for (Square sq : SQUARE_LIST) {
            if (this.get(sq).side() == side) {
                locations.add(sq);
            }
        }
        return locations;
    }

    /** Return the number of pieces on SIDE. */
    int numPieces(Piece side) {
        return pieceLocations(side).size();
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or null if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /** Creation of the actual game board. */
    private HashMap<Square, Piece> nboard = new HashMap<>(SIZE * SIZE);

    /**helper method to help change the player's turn indicator.**/
    private void changeturn() {
        this._turn = this._turn.opponent();
    }

    /** Stack of the moves made in the game so far. **/
    private Stack<Move> movesMade = new Stack<>();

    /** All the states that have occurred thus far in String form. **/
    private Stack<String> _allStatesStr = new Stack<String>();

    /** the move limit. **/
    private int lim;

    /** All the states that have occurred thus far. **/
    private Stack<HashMap<Square, Piece>> _allStates = new Stack<>();


}
