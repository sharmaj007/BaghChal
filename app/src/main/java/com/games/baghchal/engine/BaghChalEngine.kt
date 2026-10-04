package com.games.baghchal.engine

import kotlin.math.abs

enum class Piece { TIGER, GOAT }
enum class Turn { GOAT, TIGER }
enum class GamePhase { PLACEMENT, MOVEMENT }
enum class Difficulty { EASY, HARD }

data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val captureRow: Int? = null,
    val captureCol: Int? = null,
    val isPlacement: Boolean = false
)

data class BoardSnapshot(
    val board: Array<Array<Piece?>>,
    val tigers: List<Pair<Int, Int>>,
    val goatsLeft: Int,
    val capturedCount: Int,
    val phase: GamePhase,
    val turn: Turn,
    val winner: Turn?,
    val isDraw: Boolean,
    val isGameOver: Boolean,
    val lastCPUMove: Move?
)

class BaghChalEngine {
    var board: Array<Array<Piece?>> = Array(5) { Array(5) { null } }
    var tigers = mutableListOf(Pair(0, 0), Pair(0, 4), Pair(4, 0), Pair(4, 4))
    var goatsLeft = 20
    var capturedCount = 0
    var phase = GamePhase.PLACEMENT
    var turn = Turn.GOAT
    var winner: Turn? = null
    var isDraw = false
    var isGameOver = false
    var lastCPUMove: Move? = null

    private val moveHistory = mutableListOf<BoardSnapshot>()
    private val boardRepetitionHistory = mutableMapOf<String, Int>()

    init {
        resetGame()
    }

    fun resetGame() {
        board = Array(5) { Array(5) { null } }
        board[0][0] = Piece.TIGER
        board[0][4] = Piece.TIGER
        board[4][0] = Piece.TIGER
        board[4][4] = Piece.TIGER
        tigers = mutableListOf(Pair(0, 0), Pair(0, 4), Pair(4, 0), Pair(4, 4))
        goatsLeft = 20
        capturedCount = 0
        phase = GamePhase.PLACEMENT
        turn = Turn.GOAT
        winner = null
        isDraw = false
        isGameOver = false
        lastCPUMove = null
        moveHistory.clear()
        boardRepetitionHistory.clear()
        recordRepetition()
    }

    fun saveSnapshot() {
        moveHistory.add(
            BoardSnapshot(
                board = Array(5) { r -> Array(5) { c -> board[r][c] } },
                tigers = tigers.toList(),
                goatsLeft = goatsLeft,
                capturedCount = capturedCount,
                phase = phase,
                turn = turn,
                winner = winner,
                isDraw = isDraw,
                isGameOver = isGameOver,
                lastCPUMove = lastCPUMove
            )
        )
    }

    fun canUndo(): Boolean = moveHistory.isNotEmpty() && !isGameOver

    fun undoMove(): Boolean {
        if (moveHistory.isEmpty()) return false
        val snapshot = moveHistory.removeAt(moveHistory.size - 1)
        board = snapshot.board
        tigers = snapshot.tigers.toMutableList()
        goatsLeft = snapshot.goatsLeft
        capturedCount = snapshot.capturedCount
        phase = snapshot.phase
        turn = snapshot.turn
        winner = snapshot.winner
        isDraw = snapshot.isDraw
        isGameOver = snapshot.isGameOver
        lastCPUMove = snapshot.lastCPUMove
        return true
    }

    fun hasDiagonal(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val pairs = arrayOf(
            intArrayOf(0, 0, 1, 1), intArrayOf(1, 1, 2, 2), intArrayOf(2, 2, 3, 3), intArrayOf(3, 3, 4, 4),
            intArrayOf(0, 4, 1, 3), intArrayOf(1, 3, 2, 2), intArrayOf(2, 2, 3, 1), intArrayOf(3, 1, 4, 0),
            intArrayOf(0, 2, 1, 1), intArrayOf(0, 2, 1, 3), intArrayOf(2, 0, 1, 1), intArrayOf(2, 4, 1, 3),
            intArrayOf(2, 0, 3, 1), intArrayOf(4, 0, 3, 1), intArrayOf(2, 4, 3, 3), intArrayOf(4, 4, 3, 3),
            intArrayOf(4, 2, 3, 1), intArrayOf(4, 2, 3, 3), intArrayOf(2, 2, 1, 1), intArrayOf(2, 2, 1, 3),
            intArrayOf(2, 2, 3, 1), intArrayOf(2, 2, 3, 3)
        )
        return pairs.any { p ->
            (p[0] == r1 && p[1] == c1 && p[2] == r2 && p[3] == c2) ||
            (p[0] == r2 && p[1] == c2 && p[2] == r1 && p[3] == c1)
        }
    }

    fun isAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val dr = abs(r1 - r2)
        val dc = abs(c1 - c2)
        if (dr == 0 && dc == 1) return true
        if (dr == 1 && dc == 0) return true
        if (dr == 1 && dc == 1) return hasDiagonal(r1, c1, r2, c2)
        return false
    }

    fun getValidMoves(r: Int, c: Int): List<Move> {
        val moves = mutableListOf<Move>()
        val piece = board[r][c] ?: return moves

        if (piece == Piece.TIGER) {
            for (nr in 0..4) {
                for (nc in 0..4) {
                    if (board[nr][nc] == null && isAdjacent(r, c, nr, nc)) {
                        moves.add(Move(fromRow = r, fromCol = c, toRow = nr, toCol = nc))
                    }
                    if (board[nr][nc] == Piece.GOAT && isAdjacent(r, c, nr, nc)) {
                        val jr = nr + (nr - r)
                        val jc = nc + (nc - c)
                        if (jr in 0..4 && jc in 0..4 && board[jr][jc] == null && isAdjacent(nr, nc, jr, jc)) {
                            moves.add(
                                Move(
                                    fromRow = r, fromCol = c, toRow = jr, toCol = jc,
                                    captureRow = nr, captureCol = nc
                                )
                            )
                        }
                    }
                }
            }
        } else if (piece == Piece.GOAT && phase == GamePhase.MOVEMENT) {
            for (nr in 0..4) {
                for (nc in 0..4) {
                    if (board[nr][nc] == null && isAdjacent(r, c, nr, nc)) {
                        moves.add(Move(fromRow = r, fromCol = c, toRow = nr, toCol = nc))
                    }
                }
            }
        }
        return moves
    }

    fun placeGoat(r: Int, c: Int): Boolean {
        if (isGameOver || turn != Turn.GOAT || phase != GamePhase.PLACEMENT || board[r][c] != null) return false
        saveSnapshot()
        board[r][c] = Piece.GOAT
        goatsLeft--
        if (goatsLeft == 0) phase = GamePhase.MOVEMENT
        turn = Turn.TIGER
        checkStateAndWin()
        return true
    }

    fun executeMove(move: Move): Boolean {
        if (isGameOver) return false
        saveSnapshot()
        if (move.captureRow != null && move.captureCol != null) {
            board[move.captureRow][move.captureCol] = null
            capturedCount++
        }
        board[move.fromRow][move.fromCol] = null
        val piece = if (turn == Turn.TIGER) Piece.TIGER else Piece.GOAT
        board[move.toRow][move.toCol] = piece

        if (piece == Piece.TIGER) {
            val idx = tigers.indexOfFirst { it.first == move.fromRow && it.second == move.fromCol }
            if (idx >= 0) tigers[idx] = Pair(move.toRow, move.toCol)
            turn = Turn.GOAT
        } else {
            turn = Turn.TIGER
        }

        checkStateAndWin()
        return true
    }

    fun areTigersTrapped(): Boolean {
        return tigers.all { (tr, tc) -> getValidMoves(tr, tc).isEmpty() }
    }

    private fun recordRepetition() {
        val key = board.joinToString("") { row -> row.map { it?.name ?: "." }.joinToString("") } + turn.name + phase.name
        val count = (boardRepetitionHistory[key] ?: 0) + 1
        boardRepetitionHistory[key] = count
        if (count >= 5) {
            isDraw = true
            isGameOver = true
        }
    }

    fun checkStateAndWin() {
        recordRepetition()
        if (isDraw) return

        if (capturedCount >= 5) {
            winner = Turn.TIGER
            isGameOver = true
            return
        }

        if (areTigersTrapped()) {
            winner = Turn.GOAT
            isGameOver = true
            return
        }

        if (turn == Turn.GOAT && phase == GamePhase.MOVEMENT) {
            var goatHasMove = false
            for (r in 0..4) {
                for (c in 0..4) {
                    if (board[r][c] == Piece.GOAT && getValidMoves(r, c).isNotEmpty()) {
                        goatHasMove = true
                        break
                    }
                }
            }
            if (!goatHasMove) {
                winner = Turn.TIGER
                isGameOver = true
            }
        }
    }
}
