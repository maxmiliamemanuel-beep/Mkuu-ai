package com.mkuu.ai.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Dark = darkColorScheme(primary=Color(0xFFB9A0FF),secondary=Color(0xFF70D6C2),surface=Color(0xFF121219),surfaceVariant=Color(0xFF22222E))
private val Light = lightColorScheme(primary=Color(0xFF5D3AB3),secondary=Color(0xFF006C5A))
@Composable fun MkuuTheme(dark:Boolean=true,content:@Composable()->Unit)=MaterialTheme(colorScheme=if(dark) Dark else Light,typography=Typography(),content=content)
