package com.kriptic.app.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    secretPin: String = "1337",
    onUnlockSecretApp: () -> Unit
) {
    var displayText by remember { mutableStateOf("0") }
    var expressionText by remember { mutableStateOf("") }

    fun onButtonPress(btn: String) {
        when (btn) {
            "C" -> {
                displayText = "0"
                expressionText = ""
            }
            "=" -> {
                if (displayText == secretPin || expressionText == secretPin) {
                    // Unlock trigger!
                    onUnlockSecretApp()
                } else {
                    // Real calculation logic
                    try {
                        val result = evalSimple(expressionText.ifBlank { displayText })
                        displayText = result
                        expressionText = ""
                    } catch (e: Exception) {
                        displayText = "Error"
                    }
                }
            }
            "+", "-", "×", "÷" -> {
                expressionText = displayText + " " + btn + " "
                displayText = "0"
            }
            else -> {
                if (displayText == "0" || displayText == "Error") {
                    displayText = btn
                } else {
                    displayText += btn
                }
                expressionText += btn
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17171C))
            .padding(24.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Calculator Display Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expressionText,
                fontSize = 20.sp,
                color = Color(0xFF8E8E93),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayText,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculator Buttons Grid
        val buttons = listOf(
            listOf("C", "( )", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("+/-", "0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    val isOperator = label in listOf("÷", "×", "-", "+", "=")
                    val isAction = label in listOf("C", "( )", "%", "+/-")

                    val buttonBg = when {
                        label == "=" -> Color(0xFF4F7CFF)
                        isOperator -> Color(0xFF2C2C2E)
                        isAction -> Color(0xFF3A3A3C)
                        else -> Color(0xFF2C2C2E)
                    }

                    val textColor = when {
                        label == "=" -> Color.White
                        isOperator -> Color(0xFF4F7CFF)
                        isAction -> Color(0xFFE5E5EA)
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(CircleShape)
                            .background(buttonBg)
                            .clickable { onButtonPress(label) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

private fun evalSimple(expr: String): String {
    val tokens = expr.split(" ")
    if (tokens.size < 3) return expr
    val op1 = tokens[0].toDoubleOrNull() ?: return expr
    val op = tokens[1]
    val op2 = tokens[2].toDoubleOrNull() ?: return expr

    val res = when (op) {
        "+" -> op1 + op2
        "-" -> op1 - op2
        "×" -> op1 * op2
        "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
        else -> op1
    }
    return if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
}
