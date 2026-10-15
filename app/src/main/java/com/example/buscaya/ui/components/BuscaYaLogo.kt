package com.example.buscaya.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscaya.ui.theme.BuscaYaWhite
import com.example.buscaya.ui.theme.PharmacyLogoAccent

@Composable
fun BuscaYaLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Busca",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BuscaYaWhite
        )
        Text(
            text = "Ya!",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PharmacyLogoAccent
        )
    }
}
