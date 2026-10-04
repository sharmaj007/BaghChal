package com.games.baghchal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.games.baghchal.engine.BaghChalEngine
import com.games.baghchal.engine.Move
import com.games.baghchal.engine.Piece
import com.games.baghchal.engine.Turn
import kotlin.math.hypot

@Composable
fun BaghChalBoardCanvas(
    engine: BaghChalEngine,
    selectedPoint: Pair<Int, Int>?,
    validMoves: List<Move>,
    playerSide: Turn,
    onPointTapped: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(selectedPoint, engine.turn, engine.isGameOver) {
                detectTapGestures { tapOffset ->
                    if (engine.isGameOver) return@detectTapGestures
                    val pad = size.width * 0.09f
                    val step = (size.width - pad * 2f) / 4f

                    var bestR = -1
                    var bestC = -1
                    var minDistance = Float.MAX_VALUE

                    for (r in 0..4) {
                        for (c in 0..4) {
                            val ptX = pad + c * step
                            val ptY = pad + r * step
                            val dist = hypot(ptX - tapOffset.x, ptY - tapOffset.y)
                            if (dist < minDistance) {
                                minDistance = dist
                                bestR = r
                                bestC = c
                            }
                        }
                    }

                    if (minDistance < step * 0.55f && bestR != -1 && bestC != -1) {
                        onPointTapped(bestR, bestC)
                    }
                }
            }
    ) {
        val pad = size.width * 0.09f
        val step = (size.width - pad * 2f) / 4f

        fun getPt(r: Int, c: Int) = Offset(pad + c * step, pad + r * step)

        val lineColor = Color(0xCC000000)
        val lineStroke = 4f

        // Horizontal & Vertical Grid Lines
        for (i in 0..4) {
            drawLine(lineColor, getPt(i, 0), getPt(i, 4), strokeWidth = lineStroke)
            drawLine(lineColor, getPt(0, i), getPt(4, i), strokeWidth = lineStroke)
        }

        // Diagonals
        val diagonalPairs = arrayOf(
            intArrayOf(0, 0, 4, 4), intArrayOf(0, 4, 4, 0),
            intArrayOf(0, 0, 2, 2), intArrayOf(2, 0, 0, 2),
            intArrayOf(2, 0, 4, 2), intArrayOf(4, 0, 2, 2),
            intArrayOf(0, 4, 2, 2), intArrayOf(2, 4, 0, 2),
            intArrayOf(2, 4, 4, 2), intArrayOf(4, 4, 2, 2),
            intArrayOf(0, 2, 2, 0), intArrayOf(0, 2, 2, 4),
            intArrayOf(4, 2, 2, 0), intArrayOf(4, 2, 2, 4)
        )

        for (p in diagonalPairs) {
            drawLine(lineColor, getPt(p[0], p[1]), getPt(p[2], p[3]), strokeWidth = lineStroke)
        }

        // Intersection dots
        for (r in 0..4) {
            for (c in 0..4) {
                drawCircle(lineColor, radius = 4f, center = getPt(r, c))
            }
        }

        // Highlight Last CPU Move
        engine.lastCPUMove?.let { lastMv ->
            if (!lastMv.isPlacement && lastMv.fromRow in 0..4 && lastMv.fromCol in 0..4 && lastMv.toRow in 0..4 && lastMv.toCol in 0..4) {
                val fromPt = getPt(lastMv.fromRow, lastMv.fromCol)
                val toPt = getPt(lastMv.toRow, lastMv.toCol)

                drawLine(
                    color = Color(0x99FFA500),
                    start = fromPt,
                    end = toPt,
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawCircle(Color(0x66FFA500), radius = step * 0.38f, center = fromPt, style = Stroke(width = 2f))
                drawCircle(Color(0xCCFFA500), radius = step * 0.40f, center = toPt, style = Stroke(width = 3f))
            } else if (lastMv.toRow in 0..4 && lastMv.toCol in 0..4) {
                val toPt = getPt(lastMv.toRow, lastMv.toCol)
                drawCircle(Color(0xCCFFA500), radius = step * 0.40f, center = toPt, style = Stroke(width = 3f))
            }
        }

        // Highlight Valid Target Moves
        if (selectedPoint != null) {
            for (mv in validMoves) {
                val targetPt = getPt(mv.toRow, mv.toCol)
                drawCircle(Color(0x593278FF), radius = step * 0.28f, center = targetPt)
                drawCircle(Color(0xCC3278FF), radius = step * 0.28f, center = targetPt, style = Stroke(width = 2f))
            }
        } else if (engine.turn == playerSide && engine.turn == Turn.GOAT && engine.phase == com.games.baghchal.engine.GamePhase.PLACEMENT) {
            for (r in 0..4) {
                for (c in 0..4) {
                    if (engine.board[r][c] == null) {
                        val pt = getPt(r, c)
                        drawCircle(Color(0x263278FF), radius = step * 0.20f, center = pt)
                        drawCircle(Color(0x663278FF), radius = step * 0.20f, center = pt, style = Stroke(width = 1.5f))
                    }
                }
            }
        }

        // Draw Pieces
        val tigerRadius = step * 0.35f
        val goatRadius = step * 0.30f

        for (r in 0..4) {
            for (c in 0..4) {
                val piece = engine.board[r][c] ?: continue
                val pt = getPt(r, c)
                val isSel = selectedPoint != null && selectedPoint.first == r && selectedPoint.second == c

                if (piece == Piece.TIGER) {
                    val bg = if (isSel) Color(0xFFE07000) else Color(0xFFC45A00)
                    drawCircle(bg, radius = tigerRadius, center = pt)
                    drawCircle(Color(0xFF7A3500), radius = tigerRadius, center = pt, style = Stroke(width = 3f))

                    val layoutResult = textMeasurer.measure(
                        text = "T",
                        style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(pt.x - layoutResult.size.width / 2f, pt.y - layoutResult.size.height / 2f)
                    )
                } else {
                    val bg = if (isSel) Color(0xFFB8D098) else Color(0xFFE8E0C8)
                    drawCircle(bg, radius = goatRadius, center = pt)
                    drawCircle(Color(0xFF6A7A4A), radius = goatRadius, center = pt, style = Stroke(width = 3f))

                    val layoutResult = textMeasurer.measure(
                        text = "G",
                        style = TextStyle(color = Color(0xFF3A5A1A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(pt.x - layoutResult.size.width / 2f, pt.y - layoutResult.size.height / 2f)
                    )
                }
            }
        }
    }
}
