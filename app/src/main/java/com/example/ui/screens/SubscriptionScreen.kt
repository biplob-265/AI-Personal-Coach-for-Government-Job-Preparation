package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SubscriptionScreen(
    isPremium: Boolean,
    isProcessing: Boolean,
    successMessage: String?,
    onPurchasePremium: (String) -> Unit,
    onClearSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "প্রিমিয়াম",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )

            Text(
                text = "AI প্রিমিয়াম সাবস্ক্রিপশন",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "BCS এবং সরকারি চাকরি প্রস্তুতির জন্য সম্পূর্ণ গাইড",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic plan status display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আপনার বর্তমান সাবস্ক্রিপশন প্ল্যান:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPremium) "👑 প্রিমিয়ার মেম্বারশিপ (সক্রিয়)" else "🌟 ফ্রি মেম্বারশিপ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isPremium) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "অ্যাক্টিভ",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compare Plans
            Text(
                text = "প্ল্যান তুলনা",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompareRow(feature = "প্রতিদিন MCQ অনুশীলন সীমা", freeVal = "২০০ টি কুইজ/MCQ", premiumVal = "আনলিমিটেড ♾️", highlightPremium = true)
            CompareRow(feature = "প্রতিদিন AI মেন্টর মেসেজ", freeVal = "৫ টি বার্তা", premiumVal = "আনলিমিটেড ♾️", highlightPremium = true)
            CompareRow(feature = "AI স্টাডি প্ল্যানার ও উইকলি রুটিন", freeVal = "🔒 বন্ধ", premiumVal = "খোলা ✅", highlightPremium = true)
            CompareRow(feature = "স্মার্ট উইকনেস অ্যানালাইসিস", freeVal = "🔓 আংশিক", premiumVal = "উন্নত ✅", highlightPremium = true)

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Methods Panel
            if (!isPremium) {
                Text(
                    text = "সহজ বিকাশ, নগদ ও রকেট পেমেন্ট",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "যেকোনো একটি গেটওয়ে সিলেক্ট করে সাবস্ক্রাইব করুন। মাত্র ২৯৯ টাকা/মাস মাত্র।",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                )

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "নিরাপদ পেমেন্ট ট্রানজ্যাকশন প্রসেস হচ্ছে...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // bKash Button
                    Button(
                        onClick = { onPurchasePremium("bKash (বিকাশ)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(52.dp)
                            .testTag("pay_bkash_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2125B)), // bKash Pink
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ব", color = Color(0xFFE2125B), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("বিকাশ দিয়ে পে করুন (২৯৯/- টাকা)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Nagad Button
                    Button(
                        onClick = { onPurchasePremium("Nagad (নগদ)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(52.dp)
                            .testTag("pay_nagad_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA5A1E)), // Nagad Orange
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ন", color = Color(0xFFFA5A1E), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("নগদ দিয়ে পে করুন (২৯৯/- টাকা)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Rocket Button
                    Button(
                        onClick = { onPurchasePremium("Rocket (রকেট)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(52.dp)
                            .testTag("pay_rocket_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D40)), // Rocket Maroon
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("র", color = Color(0xFF8C1D40), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("রকেট দিয়ে পে করুন (২৯৯/- টাকা)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "সফল",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "আপনার মেন্টরশিপ অ্যাকাউন্টে প্রিমিয়াম লাইসেন্স সফলভাবে সংযুক্ত আছে। আমাদের সকল সার্ভিস আপনি আনলিমিটেড ব্যবহার করতে পারবেন। ধন্যবাদ!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        // Celebrate Success Dialog Modal
        if (successMessage != null) {
            Dialog(onDismissRequest = onClearSuccess) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "সফল",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "পেমেন্ট সফল হয়েছে!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = successMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onClearSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("premium_success_ok_button")
                        ) {
                            Text("শুরু করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompareRow(
    feature: String,
    freeVal: String,
    premiumVal: String,
    highlightPremium: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = feature,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ফ্রি: $freeVal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "প্রিমিয়ার: $premiumVal",
                    fontSize = 13.sp,
                    fontWeight = if (highlightPremium) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (highlightPremium) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
