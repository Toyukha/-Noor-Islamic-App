package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

enum class PaymentGatewayOption(val label: String, val badge: String) {
  PAYSTACK("Paystack", "Instant NGN / Cards"),
  GOOGLE_PAY("Google Pay", "1-Tap Express"),
  CARD("Credit/Debit", "Visa & Mastercard")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutBottomSheet(
  currency: String,
  isProcessing: Boolean,
  selectedGateway: String,
  onSelectGateway: (String) -> Unit,
  onConfirmPayment: (gateway: String, currency: String) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val priceFormatted = if (currency == "NGN") "₦1,000" else "$5.00"

  var cardNumber by remember { mutableStateOf("5399 •••• •••• 8421") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvv by remember { mutableStateOf("832") }
  var cardHolder by remember { mutableStateOf("Devout Subscriber") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = Modifier.testTag("checkout_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Noor Premium Subscription",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Monthly Recurring • Cancel Anytime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Price Tag Banner
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Total Due Today",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "$priceFormatted / month",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary
          ) {
            Text(
              text = "Billed Monthly",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Gateway Selectors
      Text(
        text = "Select Payment Method",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PaymentGatewayOption.values().forEach { option ->
          val isSelected = selectedGateway == option.name
          val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
          val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
              )
              .background(bgColor)
              .clickable { onSelectGateway(option.name) }
              .padding(vertical = 10.dp, horizontal = 6.dp)
              .testTag("gateway_option_${option.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = when (option) {
                PaymentGatewayOption.PAYSTACK -> Icons.Default.Payment
                PaymentGatewayOption.GOOGLE_PAY -> Icons.Default.Shield
                PaymentGatewayOption.CARD -> Icons.Default.CreditCard
              },
              contentDescription = option.label,
              tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = option.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
            Text(
              text = option.badge,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.outline,
              textAlign = TextAlign.Center,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Form details depending on Gateway
      when (selectedGateway) {
        PaymentGatewayOption.PAYSTACK.name -> {
          // Paystack Nigerian & African Fintech Flow
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF00C3F7).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C3F7).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C3F7)),
                  contentAlignment = Alignment.Center
                ) {
                  Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Secured by Paystack",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF007A99)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                  text = "CBN Licensed",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.outline
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Pay seamlessly with Nigerian Debit Cards (Mastercard, Visa, Verve), Bank Transfer, or USSD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }

        PaymentGatewayOption.GOOGLE_PAY.name -> {
          // Google Pay 1-Tap Experience
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = "GPay",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Google Pay Express Checkout",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Use your linked Google Account payment methods. Fingerprint or Face Unlock authentication.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }

        else -> {
          // Credit / Debit Card Form
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = cardNumber,
              onValueChange = { cardNumber = it },
              label = { Text("Card Number", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
              modifier = Modifier.fillMaxWidth().testTag("input_card_number")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = cardExpiry,
                onValueChange = { cardExpiry = it },
                label = { Text("Expiry (MM/YY)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_card_expiry")
              )
              OutlinedTextField(
                value = cardCvv,
                onValueChange = { cardCvv = it },
                label = { Text("CVV", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_card_cvv")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Security Reassurance Note
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Security",
          tint = MaterialTheme.colorScheme.outline,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "256-bit SSL encrypted • Sharia-compliant • Automatic renewals",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.outline
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Pay Button
      Button(
        onClick = { onConfirmPayment(selectedGateway, currency) },
        enabled = !isProcessing,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedGateway == PaymentGatewayOption.PAYSTACK.name) Color(0xFF00C3F7) else MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_subscription_payment_button")
      ) {
        if (isProcessing) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("Authorizing Payment...", fontWeight = FontWeight.Bold, color = Color.White)
        } else {
          val buttonTitle = when (selectedGateway) {
            PaymentGatewayOption.PAYSTACK.name -> "Pay $priceFormatted via Paystack"
            PaymentGatewayOption.GOOGLE_PAY.name -> "Pay $priceFormatted with Google Pay"
            else -> "Pay $priceFormatted / month"
          }
          Text(buttonTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
      }
    }
  }
}
