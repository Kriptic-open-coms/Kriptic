package com.kriptic.app.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DarkBackground
import com.kriptic.app.ui.theme.DarkSurface
import com.kriptic.app.ui.theme.DarkTextPrimary
import com.kriptic.app.ui.theme.DarkTextSecondary
import com.kriptic.app.ui.theme.Heading
import com.kriptic.app.ui.theme.Title

@Composable
fun ReferenceDetailScreen(
    article: ReferenceArticle,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("← Back to Reference Search", style = Caption, color = DarkTextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (article.category == "LEGAL") Accent else Color(0xFF3DAA6B))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = article.category,
                style = Caption.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = article.title, style = Title, color = DarkTextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = article.summary, style = Body, color = DarkTextSecondary)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Text(
                text = article.content,
                style = Body,
                color = DarkTextPrimary,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
