package com.games.baghchal.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.games.baghchal.audio.SoundManager
import com.games.baghchal.engine.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppScreen { LANG_SELECT, SIDE_SELECT, GAME }
enum class Lang { EN, NP }

data class LocalizedStrings(
    val title: String,
    val sub: String,
    val goatName: String,
    val goatDesc: String,
    val tigerName: String,
    val tigerDesc: String,
    val btnStart: String,
    val btnChangeLang: String,
    val btnChangeSide: String,
    val btnBack: String,
    val yourTurn: String,
    val cpuTurn: String,
    val youWin: String,
    val cpuWin: String,
    val capLabel: String,
    val leftLabel: String,
    val trayLabel: String,
    val placeGoat: String,
    val selectGoat: String,
    val moveGoat: String,
    val selectTiger: String,
    val moveTiger: String,
    val cpuThinking: String,
    val tigerWinMsg: String,
    val goatWinMsg: String,
    val statGoat: String,
    val statTiger: String,
    val drawMsg: String,
    val drawTitle: String,
    val diffTitle: String,
    val diffEasy: String,
    val diffHard: String
)

val stringsEN = LocalizedStrings(
    title = "Choose your side", sub = "Pick a piece — computer plays the other side",
    goatName = "Goat", goatDesc = "Place & surround tigers. Win by trapping all 4.",
    tigerName = "Tiger", tigerDesc = "Jump & capture goats. Win by catching 5.",
    btnStart = "Start game", btnChangeLang = "Change language", btnChangeSide = "Change Side", btnBack = "Play Again",
    yourTurn = "Your turn", cpuTurn = "Thinking...", youWin = "YOU WIN!", cpuWin = "COMPUTER WINS",
    capLabel = "Goats captured", leftLabel = "Goats to place", trayLabel = "Goat tray",
    placeGoat = "Tap any empty point to place a goat",
    selectGoat = "Tap your goat to move it", moveGoat = "Tap a highlighted point to move",
    selectTiger = "Tap a tiger to move it", moveTiger = "Tap a highlighted point to move or capture",
    cpuThinking = "Thinking...",
    tigerWinMsg = "Tigers captured 5 goats — tigers win!", goatWinMsg = "All tigers trapped — goats win!",
    statGoat = "Goat Wins", statTiger = "Tiger Wins", drawMsg = "Game drawn — too many repetitions", drawTitle = "DRAW!",
    diffTitle = "Select Difficulty", diffEasy = "Easy", diffHard = "Hard"
)

val stringsNP = LocalizedStrings(
    title = "आफ्नो पक्ष छान्नुहोस्", sub = "एउटा टोकन छान्नुहोस् — कम्प्युटरले अर्को खेल्छ",
    goatName = "बाख्रा", goatDesc = "बाघलाई घेर्नुहोस्। चारै बाघ थुन्यो भने जित्नुहुन्छ।",
    tigerName = "बाघ", tigerDesc = "बाख्रा उफ्रेर खानुहोस्। ५ बाख्रा खायो भने जित्नुहुन्छ।",
    btnStart = "खेल सुरु गर्नुहोस्", btnChangeLang = "भाषा फेर्नुहोस्", btnChangeSide = "पक्ष फेर्नुहोस्", btnBack = "फेरि खेल्नुहोस्",
    yourTurn = "तपाईंको पालो", cpuTurn = "सोच्दैछ...", youWin = "तपाईं जित्नुभयो!", cpuWin = "कम्प्युटर जित्यो",
    capLabel = "खाइएका बाख्रा", leftLabel = "राख्न बाँकी बाख्रा", trayLabel = "बाख्रा ट्रे",
    placeGoat = "खाली ठाउँमा थिचेर बाख्रा राख्नुहोस्",
    selectGoat = "सार्न बाख्रामा थिच्नुहोस्", moveGoat = "हाइलाइट भएको ठाउँमा थिच्नुहोस्",
    selectTiger = "सार्न बाघमा थिच्नुहोस्", moveTiger = "हाइलाइट भएको ठाउँमा थिच्नुहोस् वा खानुहोस्",
    cpuThinking = "सोच्दैछ...",
    tigerWinMsg = "बाघले ५ बाख्रा खायो — बाघ जित्यो!", goatWinMsg = "सबै बाघ थुनियो — बाख्रा जित्यो!",
    statGoat = "बाख्राको जित", statTiger = "बाघको जित", drawMsg = "खेल बराबरी भयो — धेरै पटक दोहोरियो", drawTitle = "बराबरी!",
    diffTitle = "कठिनाई छान्नुहोस्", diffEasy = "सजिलो", diffHard = "गाह्रो"
)

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-1600918757938052/4322851012"
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx: Context ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun BaghChalApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.LANG_SELECT) }
    var lang by remember { mutableStateOf(Lang.EN) }
    var playerSide by remember { mutableStateOf(Turn.GOAT) }
    var difficulty by remember { mutableStateOf(Difficulty.HARD) }
    var soundEnabled by remember { mutableStateOf(true) }

    val engine = remember { BaghChalEngine() }
    val ai = remember { MinimaxAI(engine) }
    val scope = rememberCoroutineScope()

    var selectedPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var validMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var statusMsg by remember { mutableStateOf("") }
    var isCpuThinking by remember { mutableStateOf(false) }
    var gameTrigger by remember { mutableIntStateOf(0) }

    val str = if (lang == Lang.EN) stringsEN else stringsNP

    fun runCpuTurn() {
        if (engine.isGameOver || isCpuThinking || engine.turn == playerSide) return
        isCpuThinking = true
        statusMsg = str.cpuThinking

        scope.launch {
            delay(450)
            val prevCap = engine.capturedCount
            if (engine.turn == Turn.TIGER) {
                val mv = ai.findBestTigerMove(difficulty)
                if (mv != null) {
                    engine.executeMove(mv)
                    engine.lastCPUMove = mv
                }
            } else {
                if (engine.phase == GamePhase.PLACEMENT && engine.goatsLeft > 0) {
                    val pt = ai.findBestGoatMove(difficulty)
                    if (pt != null) {
                        engine.placeGoat(pt.first, pt.second)
                        engine.lastCPUMove = Move(
                            fromRow = -1, fromCol = -1, toRow = pt.first, toCol = pt.second, isPlacement = true
                        )
                    }
                } else {
                    val mv = ai.findBestGoatMoveMovement(difficulty)
                    if (mv != null) {
                        engine.executeMove(mv)
                        engine.lastCPUMove = mv
                    }
                }
            }

            if (engine.capturedCount > prevCap) {
                SoundManager.playCaptureSound()
            } else {
                SoundManager.playMoveSound()
            }

            isCpuThinking = false
            selectedPoint = null
            validMoves = emptyList()
            gameTrigger++

            if (engine.isGameOver) {
                if (engine.winner == playerSide) {
                    SoundManager.playWinSound()
                    statusMsg = str.youWin
                } else {
                    SoundManager.playLoseSound()
                    statusMsg = if (engine.isDraw) str.drawMsg else str.cpuWin
                }
            } else {
                statusMsg = if (engine.turn == playerSide) str.yourTurn else str.cpuTurn
            }
        }
    }

    fun restartGame() {
        isCpuThinking = false
        engine.resetGame()
        selectedPoint = null
        validMoves = emptyList()
        statusMsg = if (playerSide == Turn.GOAT) str.yourTurn else str.cpuTurn
        gameTrigger++
        SoundManager.playMoveSound()
        if (playerSide == Turn.TIGER) runCpuTurn()
    }

    fun handlePointTapped(r: Int, c: Int) {
        if (engine.isGameOver || isCpuThinking || engine.turn != playerSide) return

        fun updateGameStatusAfterPlayerMove() {
            selectedPoint = null
            validMoves = emptyList()
            gameTrigger++
            if (engine.isGameOver) {
                if (engine.winner == playerSide) {
                    SoundManager.playWinSound()
                    statusMsg = str.youWin
                } else {
                    SoundManager.playLoseSound()
                    statusMsg = if (engine.isDraw) str.drawMsg else str.cpuWin
                }
            } else {
                statusMsg = str.cpuTurn
                runCpuTurn()
            }
        }

        if (playerSide == Turn.GOAT && engine.phase == GamePhase.PLACEMENT) {
            if (engine.placeGoat(r, c)) {
                SoundManager.playMoveSound()
                updateGameStatusAfterPlayerMove()
            }
        } else {
            val sel = selectedPoint
            if (sel != null) {
                val mv = validMoves.find { it.toRow == r && it.toCol == c }
                if (mv != null) {
                    val prevCap = engine.capturedCount
                    engine.executeMove(mv)
                    if (engine.capturedCount > prevCap) {
                        SoundManager.playCaptureSound()
                    } else {
                        SoundManager.playMoveSound()
                    }
                    updateGameStatusAfterPlayerMove()
                } else {
                    val clickedPiece = engine.board[r][c]
                    val expectedPiece = if (playerSide == Turn.GOAT) Piece.GOAT else Piece.TIGER
                    if (clickedPiece == expectedPiece) {
                        selectedPoint = Pair(r, c)
                        validMoves = engine.getValidMoves(r, c)
                    } else {
                        selectedPoint = null
                        validMoves = emptyList()
                    }
                }
            } else {
                val clickedPiece = engine.board[r][c]
                val expectedPiece = if (playerSide == Turn.GOAT) Piece.GOAT else Piece.TIGER
                if (clickedPiece == expectedPiece) {
                    selectedPoint = Pair(r, c)
                    validMoves = engine.getValidMoves(r, c)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 440.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trigger re-composition when gameTrigger changes
                    key(gameTrigger) {
                        when (currentScreen) {
                            AppScreen.LANG_SELECT -> {
                                Text("बाघ चाल", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1B))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Choose your language / भाषा छान्नुहोस्", fontSize = 14.sp, color = Color(0xFF606770))
                                Spacer(modifier = Modifier.height(24.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { lang = Lang.EN }
                                            .border(
                                                width = if (lang == Lang.EN) 2.dp else 0.5.dp,
                                                color = if (lang == Lang.EN) Color(0xFF378ADD) else Color(0xFF378ADD),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("EN", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1B))
                                            Text("English", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { lang = Lang.NP }
                                            .border(
                                                width = if (lang == Lang.NP) 2.dp else 0.5.dp,
                                                color = if (lang == Lang.NP) Color(0xFF378ADD) else Color(0xFF378ADD),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("ने", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1B))
                                            Text("नेपाली", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { currentScreen = AppScreen.SIDE_SELECT },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (lang == Lang.EN) "Continue" else "सुरु गरौं", fontSize = 16.sp)
                                }
                            }

                            AppScreen.SIDE_SELECT -> {
                                Text(str.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(str.sub, fontSize = 13.sp, color = Color(0xFF606770))
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { playerSide = Turn.GOAT }
                                            .border(
                                                width = if (playerSide == Turn.GOAT) 2.dp else 0.5.dp,
                                                color = if (playerSide == Turn.GOAT) Color(0xFF378ADD) else Color(0xFF378ADD),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier.size(44.dp).background(Color(0xFFE8F0DC), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) { Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B5A1A)) }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(str.goatName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(str.goatDesc, fontSize = 11.sp, color = Color(0xFF606770), textAlign = TextAlign.Center)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { playerSide = Turn.TIGER }
                                            .border(
                                                width = if (playerSide == Turn.TIGER) 2.dp else 0.5.dp,
                                                color = if (playerSide == Turn.TIGER) Color(0xFFC45A00) else Color(0xFFC45A00),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier.size(44.dp).background(Color(0xFFF5E0C8), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) { Text("T", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A3500)) }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(str.tigerName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(str.tigerDesc, fontSize = 11.sp, color = Color(0xFF606770), textAlign = TextAlign.Center)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(str.diffTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { difficulty = Difficulty.EASY },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (difficulty == Difficulty.EASY) Color(0xFFE8F0DC) else Color.Transparent
                                        )
                                    ) { Text(str.diffEasy) }

                                    OutlinedButton(
                                        onClick = { difficulty = Difficulty.HARD },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (difficulty == Difficulty.HARD) Color(0xFFF5E0C8) else Color.Transparent
                                        )
                                    ) { Text(str.diffHard) }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        restartGame()
                                        currentScreen = AppScreen.GAME
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text(str.btnStart, fontSize = 16.sp) }

                                TextButton(onClick = { currentScreen = AppScreen.LANG_SELECT }) { Text(str.btnChangeLang) }
                            }

                            AppScreen.GAME -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val pillText: String
                                    val pillBg: Color
                                    val pillTxt: Color

                                    if (engine.isGameOver) {
                                        if (engine.isDraw) {
                                            pillText = str.drawTitle
                                            pillBg = Color(0xFFD32F2F)
                                            pillTxt = Color.White
                                        } else if (engine.winner == playerSide) {
                                            pillText = str.youWin
                                            pillBg = Color(0xFF2E7D32)
                                            pillTxt = Color.White
                                        } else {
                                            pillText = str.cpuWin
                                            pillBg = Color(0xFFD32F2F)
                                            pillTxt = Color.White
                                        }
                                    } else if (engine.turn == playerSide) {
                                        pillText = str.yourTurn
                                        pillBg = Color(0xFFE8F0DC)
                                        pillTxt = Color(0xFF27500A)
                                    } else {
                                        pillText = str.cpuTurn
                                        pillBg = Color(0xFFF5E0C8)
                                        pillTxt = Color(0xFF7A3500)
                                    }

                                    Text(
                                        text = pillText,
                                        modifier = Modifier
                                            .background(pillBg, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pillTxt
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(onClick = {
                                            soundEnabled = !soundEnabled
                                            SoundManager.isMuted = !soundEnabled
                                        }) {
                                            Text(if (soundEnabled) "🔊" else "🔇", fontSize = 18.sp)
                                        }
                                        OutlinedButton(onClick = { currentScreen = AppScreen.SIDE_SELECT }) {
                                            Text(str.btnChangeSide, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5))) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(str.capLabel, fontSize = 10.sp, color = Color(0xFF606770))
                                            Text("${engine.capturedCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5))) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(str.leftLabel, fontSize = 10.sp, color = Color(0xFF606770))
                                            Text("${engine.goatsLeft}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                BaghChalBoardCanvas(
                                    engine = engine,
                                    selectedPoint = selectedPoint,
                                    validMoves = validMoves,
                                    playerSide = playerSide,
                                    onPointTapped = { r, c -> handlePointTapped(r, c) }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Goat Tray with Player Side Indicator Badge
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF0F2F5), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(str.trayLabel, fontSize = 11.sp, color = Color(0xFF606770))
                                        val sideText = if (lang == Lang.EN) {
                                            if (playerSide == Turn.GOAT) "You: Goat (G)" else "You: Tiger (T)"
                                        } else {
                                            if (playerSide == Turn.GOAT) "तपाईं: बाख्रा (G)" else "तपाईं: बाघ (T)"
                                        }
                                        val sideColor = if (playerSide == Turn.GOAT) Color(0xFF3B5A1A) else Color(0xFF7A3500)
                                        Text(
                                            text = sideText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sideColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        for (i in 0 until 20) {
                                            val isUsed = i >= engine.goatsLeft
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(
                                                        color = if (isUsed) Color(0x33CCD0D5) else Color(0xFFE8E0C8),
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isUsed) Color(0x33CCD0D5) else Color(0xFF8A7A5A),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Prominent Big Centered Game Over Banner Card
                                if (engine.isGameOver) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (engine.winner == playerSide) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        ),
                                        border = BorderStroke(
                                            2.dp,
                                            if (engine.winner == playerSide) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val titleText = when {
                                                engine.isDraw -> str.drawTitle
                                                engine.winner == playerSide -> str.youWin
                                                else -> str.cpuWin
                                            }
                                            val subText = when {
                                                engine.isDraw -> str.drawMsg
                                                engine.winner == Turn.TIGER -> str.tigerWinMsg
                                                else -> str.goatWinMsg
                                            }

                                            Text(
                                                text = titleText,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (engine.winner == playerSide) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = subText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1A1A1B),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    restartGame()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (engine.winner == playerSide) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(str.btnBack, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                } else {
                                    Text(statusMsg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1B))
                                }
                            }
                        }
                    }
                }
            }
        }

        // AdMob Banner Section at the bottom
        AdMobBanner(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}
