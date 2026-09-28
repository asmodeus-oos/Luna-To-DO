package com.luna.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.AccountEntity
import com.luna.app.data.local.entity.TransactionEntity
import com.luna.app.ui.theme.LunaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTransactionSheet(
    accounts: List<AccountEntity>,
    editingTransaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        tags: List<String>,
        accountId: Long,
        currency: String,
        notes: String?,
        timestamp: Long
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            editingTransaction?.let {
                if (it.amount % 1.0 == 0.0) it.amount.toInt().toString() else it.amount.toString()
            } ?: ""
        )
    }
    var transactionType by remember { mutableStateOf(editingTransaction?.type ?: "EXPENSE") }
    var selectedCategory by remember { mutableStateOf(editingTransaction?.category ?: "General") }
    var selectedAccountId by remember {
        mutableStateOf(editingTransaction?.accountId ?: (accounts.firstOrNull()?.id ?: 1L))
    }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(editingTransaction?.tags ?: emptyList()) }
    var notes by remember { mutableStateOf(editingTransaction?.notes ?: "") }

    val amountFocusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories = listOf(
        "General", "Groceries", "Dining", "Coffee",
        "Shopping", "Tech", "Utilities", "Rent",
        "Transport", "Health", "Subscriptions", "Education", "Travel"
    )

    val incomeCategories = listOf(
        "General", "Salary", "Freelance", "Consultation",
        "Investments", "Bonus", "Cashback", "Side Project"
    )

    val currentCategories = if (transactionType == "INCOME") incomeCategories else expenseCategories

    LaunchedEffect(Unit) {
        if (editingTransaction == null && amountText.isEmpty()) {
            amountFocusRequester.requestFocus()
        }
    }

    val typeColor = when (transactionType) {
        "INCOME" -> Color(0xFF10B981)
        "TRANSFER" -> Color(0xFF3B82F6)
        else -> Color(0xFFEF4444)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isLight) Color(0xFFF9F9FA) else Color(0xFF18181E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(palette.textSecondary.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (editingTransaction == null) "New Transaction" else "Edit Transaction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                if (editingTransaction != null && onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .clickable {
                                onDelete(editingTransaction.id)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Toggle (Expense / Income / Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isLight) Color(0xFFEBEBEF) else Color(0xFF24242C))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    "EXPENSE" to "Expense",
                    "INCOME" to "Income",
                    "TRANSFER" to "Transfer"
                ).forEach { (typeKey, label) ->
                    val isSelected = transactionType == typeKey
                    val btnBg = if (isSelected) {
                        when (typeKey) {
                            "INCOME" -> Color(0xFF10B981)
                            "TRANSFER" -> Color(0xFF3B82F6)
                            else -> Color(0xFFEF4444)
                        }
                    } else Color.Transparent

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(btnBg)
                            .clickable { transactionType = typeKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else palette.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Amount Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isLight) Color.White else Color(0xFF202028))
                    .border(
                        width = 1.dp,
                        color = typeColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transactionType == "INCOME") "+$" else "-$",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = amountText,
                        onValueChange = { input ->
                            // Allow digits and up to 1 decimal point
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                amountText = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(amountFocusRequester),
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        cursorBrush = SolidColor(typeColor),
                        decorationBox = { innerTextField ->
                            if (amountText.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textSecondary.copy(alpha = 0.35f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title / Description
            Text(
                text = "Title / Description",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color.White else Color(0xFF202028))
                    .border(
                        1.dp,
                        if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = palette.textPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    cursorBrush = SolidColor(typeColor),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. Composite resin supplies",
                                fontSize = 15.sp,
                                color = palette.textSecondary.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account / Wallet Selector
            Text(
                text = "Account / Wallet",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { acc ->
                    val isAccSelected = selectedAccountId == acc.id
                    val accColor = try {
                        Color(android.graphics.Color.parseColor(acc.colorHex))
                    } catch (_: Exception) { Color(0xFF3B82F6) }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAccSelected) accColor.copy(alpha = 0.2f)
                                else if (isLight) Color.White else Color(0xFF202028)
                            )
                            .border(
                                width = if (isAccSelected) 1.5.dp else 1.dp,
                                color = if (isAccSelected) accColor else if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedAccountId = acc.id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = acc.name,
                                fontSize = 12.sp,
                                fontWeight = if (isAccSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAccSelected) palette.textPrimary else palette.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "Category",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentCategories.forEach { cat ->
                    val isCatSelected = selectedCategory.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCatSelected) typeColor.copy(alpha = 0.18f)
                                else if (isLight) Color.White else Color(0xFF202028)
                            )
                            .border(
                                width = if (isCatSelected) 1.2.dp else 0.8.dp,
                                color = if (isCatSelected) typeColor else if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isCatSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCatSelected) typeColor else palette.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Section
            Text(
                text = "Tags",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Active tags
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove tag",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { tags = tags.filter { it != tag } }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tag input box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLight) Color.White else Color(0xFF202028))
                    .border(
                        1.dp,
                        if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = palette.textPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val clean = tagInput.trim().removePrefix("#")
                            if (clean.isNotBlank() && !tags.contains(clean)) {
                                tags = tags + clean
                                tagInput = ""
                            }
                        }
                    ),
                    cursorBrush = SolidColor(typeColor),
                    decorationBox = { innerTextField ->
                        if (tagInput.isEmpty()) {
                            Text(
                                text = "Add tag (e.g. tax-deductible, clinic, implant)...",
                                fontSize = 13.sp,
                                color = palette.textSecondary.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )

                if (tagInput.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                            .clickable {
                                val clean = tagInput.trim().removePrefix("#")
                                if (clean.isNotBlank() && !tags.contains(clean)) {
                                    tags = tags + clean
                                    tagInput = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Tag",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            Text(
                text = "Notes (Optional)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color.White else Color(0xFF202028))
                    .border(
                        1.dp,
                        if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = palette.textPrimary
                    ),
                    maxLines = 3,
                    cursorBrush = SolidColor(typeColor),
                    decorationBox = { innerTextField ->
                        if (notes.isEmpty()) {
                            Text(
                                text = "Vendor name, invoice number, or client notes...",
                                fontSize = 13.sp,
                                color = palette.textSecondary.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            val canSave = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0.0
            val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (canSave) typeColor else typeColor.copy(alpha = 0.4f)
                    )
                    .clickable(enabled = canSave) {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.trim().ifEmpty { selectedCategory }
                        onSave(
                            finalTitle,
                            parsedAmount,
                            transactionType,
                            selectedCategory,
                            tags,
                            selectedAccountId,
                            selectedAccount?.currencyCode ?: "USD",
                            notes.trim().ifEmpty { null },
                            editingTransaction?.timestamp ?: System.currentTimeMillis()
                        )
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (editingTransaction == null) "Log Transaction" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
