package com.games.baghchal.engine

import kotlin.math.max
import kotlin.math.min

class MinimaxAI(private val engine: BaghChalEngine) {

    fun findBestTigerMove(difficulty: Difficulty): Move? {
        val depth = if (difficulty == Difficulty.HARD) 3 else 1

        val tigerMoves = mutableListOf<Move>()
        for ((tr, tc) in engine.tigers) {
            tigerMoves.addAll(engine.getValidMoves(tr, tc))
        }

        if (tigerMoves.isEmpty()) return null
        tigerMoves.shuffle()

        // Prioritize capture moves first
        tigerMoves.sortByDescending { if (it.captureRow != null) 1 else 0 }

        val candidates = mutableListOf<Pair<Move, Int>>()
        var bestScore = -99999

        for (mv in tigerMoves) {
            val simulated = simulateTigerMove(engine, mv)
            val score = minimax(
                simulated,
                depth - 1,
                alpha = -99999,
                beta = 99999,
                isMaximizing = false
            )
            if (score > bestScore) {
                bestScore = score
            }
            candidates.add(Pair(mv, score))
        }

        val margin = if (difficulty == Difficulty.EASY) 15 else 0
        val topCandidates = candidates.filter { it.second >= bestScore - margin }
        return topCandidates.randomOrNull()?.first ?: candidates.maxByOrNull { it.second }?.first
    }

    fun findBestGoatMove(difficulty: Difficulty): Pair<Int, Int>? {
        val depth = if (difficulty == Difficulty.HARD) 2 else 1

        if (engine.phase == GamePhase.PLACEMENT && engine.goatsLeft > 0) {
            val emptySpots = mutableListOf<Pair<Int, Int>>()
            for (r in 0..4) {
                for (c in 0..4) {
                    if (engine.board[r][c] == null) {
                        emptySpots.add(Pair(r, c))
                    }
                }
            }
            if (emptySpots.isEmpty()) return null
            emptySpots.shuffle()

            val candidates = mutableListOf<Pair<Pair<Int, Int>, Int>>()
            var bestScore = 99999

            for (pt in emptySpots) {
                val simulated = simulateGoatPlacement(engine, pt.first, pt.second)
                val score = minimax(
                    simulated,
                    depth - 1,
                    alpha = -99999,
                    beta = 99999,
                    isMaximizing = true
                )
                if (score < bestScore) {
                    bestScore = score
                }
                candidates.add(Pair(pt, score))
            }

            val margin = if (difficulty == Difficulty.EASY) 15 else 0
            val topCandidates = candidates.filter { it.second <= bestScore + margin }
            return topCandidates.randomOrNull()?.first ?: candidates.minByOrNull { it.second }?.first
        }
        return null
    }

    fun findBestGoatMoveMovement(difficulty: Difficulty): Move? {
        val depth = if (difficulty == Difficulty.HARD) 2 else 1

        val goatMoves = mutableListOf<Move>()
        for (r in 0..4) {
            for (c in 0..4) {
                if (engine.board[r][c] == Piece.GOAT) {
                    goatMoves.addAll(engine.getValidMoves(r, c))
                }
            }
        }

        if (goatMoves.isEmpty()) return null
        goatMoves.shuffle()

        val candidates = mutableListOf<Pair<Move, Int>>()
        var bestScore = 99999

        for (mv in goatMoves) {
            val simulated = simulateGoatMove(engine, mv)
            val score = minimax(
                simulated,
                depth - 1,
                alpha = -99999,
                beta = 99999,
                isMaximizing = true
            )
            if (score < bestScore) {
                bestScore = score
            }
            candidates.add(Pair(mv, score))
        }

        val margin = if (difficulty == Difficulty.EASY) 15 else 0
        val topCandidates = candidates.filter { it.second <= bestScore + margin }
        return topCandidates.randomOrNull()?.first ?: candidates.minByOrNull { it.second }?.first
    }

    private fun evaluateScore(engineState: BaghChalEngine): Int {
        if (engineState.capturedCount >= 5) return 10000
        if (engineState.areTigersTrapped()) return -10000

        val trappedCount = engineState.tigers.count { (tr, tc) -> engineState.getValidMoves(tr, tc).isEmpty() }
        return engineState.capturedCount * 50 - trappedCount * 40 - engineState.goatsLeft * 2
    }

    private fun minimax(
        simEngine: BaghChalEngine,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta

        if (simEngine.capturedCount >= 5) return 10000
        if (simEngine.areTigersTrapped()) return -10000
        if (depth == 0) return evaluateScore(simEngine)

        if (isMaximizing) {
            var maxEval = -99999
            val tMoves = mutableListOf<Move>()
            for ((tr, tc) in simEngine.tigers) {
                tMoves.addAll(simEngine.getValidMoves(tr, tc))
            }
            if (tMoves.isEmpty()) return -10000

            for (mv in tMoves) {
                val nextSim = simulateTigerMove(simEngine, mv)
                val eval = minimax(nextSim, depth - 1, currentAlpha, currentBeta, false)
                maxEval = max(maxEval, eval)
                currentAlpha = max(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = 99999
            if (simEngine.phase == GamePhase.PLACEMENT && simEngine.goatsLeft > 0) {
                for (r in 0..4) {
                    for (c in 0..4) {
                        if (simEngine.board[r][c] == null) {
                            val nextSim = simulateGoatPlacement(simEngine, r, c)
                            val eval = minimax(nextSim, depth - 1, currentAlpha, currentBeta, true)
                            minEval = min(minEval, eval)
                            currentBeta = min(currentBeta, eval)
                            if (currentBeta <= currentAlpha) break
                        }
                    }
                }
            } else {
                val gMoves = mutableListOf<Move>()
                for (r in 0..4) {
                    for (c in 0..4) {
                        if (simEngine.board[r][c] == Piece.GOAT) {
                            gMoves.addAll(simEngine.getValidMoves(r, c))
                        }
                    }
                }
                if (gMoves.isEmpty()) return 10000

                for (mv in gMoves) {
                    val nextSim = simulateGoatMove(simEngine, mv)
                    val eval = minimax(nextSim, depth - 1, currentAlpha, currentBeta, true)
                    minEval = min(minEval, eval)
                    currentBeta = min(currentBeta, eval)
                    if (currentBeta <= currentAlpha) break
                }
            }
            return minEval
        }
    }

    private fun copyEngine(src: BaghChalEngine): BaghChalEngine {
        val copy = BaghChalEngine()
        copy.board = Array(5) { r -> Array(5) { c -> src.board[r][c] } }
        copy.tigers = src.tigers.map { Pair(it.first, it.second) }.toMutableList()
        copy.goatsLeft = src.goatsLeft
        copy.capturedCount = src.capturedCount
        copy.phase = src.phase
        copy.turn = src.turn
        copy.winner = src.winner
        copy.isGameOver = src.isGameOver
        return copy
    }

    private fun simulateTigerMove(src: BaghChalEngine, mv: Move): BaghChalEngine {
        val copy = copyEngine(src)
        copy.board[mv.fromRow][mv.fromCol] = null
        copy.board[mv.toRow][mv.toCol] = Piece.TIGER
        val idx = copy.tigers.indexOfFirst { it.first == mv.fromRow && it.second == mv.fromCol }
        if (idx >= 0) copy.tigers[idx] = Pair(mv.toRow, mv.toCol)

        if (mv.captureRow != null && mv.captureCol != null) {
            copy.board[mv.captureRow][mv.captureCol] = null
            copy.capturedCount++
        }
        copy.turn = Turn.GOAT
        return copy
    }

    private fun simulateGoatPlacement(src: BaghChalEngine, r: Int, c: Int): BaghChalEngine {
        val copy = copyEngine(src)
        copy.board[r][c] = Piece.GOAT
        copy.goatsLeft--
        if (copy.goatsLeft == 0) copy.phase = GamePhase.MOVEMENT
        copy.turn = Turn.TIGER
        return copy
    }

    private fun simulateGoatMove(src: BaghChalEngine, mv: Move): BaghChalEngine {
        val copy = copyEngine(src)
        copy.board[mv.fromRow][mv.fromCol] = null
        copy.board[mv.toRow][mv.toCol] = Piece.GOAT
        copy.turn = Turn.TIGER
        return copy
    }
}
