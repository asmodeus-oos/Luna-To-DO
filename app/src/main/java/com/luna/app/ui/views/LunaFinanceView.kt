package com.luna.app.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.SouthWest
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.AccountEntity
import com.luna.app.data.local.entity.BudgetEntity
import com.luna.app.data.local.entity.FinancialGoalEntity
import com.luna.app.data.local.entity.TransactionEntity
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LunaFinanceView(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    budgets: List<BudgetEntity>,
    financialGoals: List<FinancialGoalEntity>,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onQuickAddTransaction: (title: String, amount: Double, type: String, tags: List<String>) -> Unit,
    onAddAccount: (name: String, accountType: String, balance: Double, colorHex: String) -> Unit,
    onTransferFunds: (fromAccountId: Long, toAccountId: Long, amount: Double, notes: String?) -> Unit,
    onAddBudget: (categoryName: String, limitAmount: Double, alertThresholdPercent: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddFinancialGoal: (title: String, targetAmount: Double, currentAmount: Double, colorHex: String) -> Unit,
    onDeleteFinancialGoal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }

    // Dialog state controllers
    var isAddAccountDialogOpen by remember { mutableStateOf(false) }
    var isTransferDialogOpen by remember { mutableStateOf(false) }
    var isAddBudgetDialogOpen by remember { mutableStateOf(false) }
    var isAddGoalDialogOpen by remember { mutableStateOf(false) }

    // Filter states
    var selectedFilterChip by remember { mutableStateOf("ALL") }
    var selectedAccountFilterId by remember { mutableStateOf<Long?>(null) }
    var quickInputText by remember { mutableStateOf("") }

    // Summary calculations - remembered for 120fps smooth performance
    val totalNetWorth = remember(accounts) { accounts.sumOf { it.balance } }

    val startOfMonthMillis = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val (monthIncome, monthExpense, monthNetSavings, savingsRatePercent) = remember(transactions, startOfMonthMillis) {
        val monthTxs = transactions.filter { it.timestamp >= startOfMonthMillis }
        val income = monthTxs
            .filter { it.type.equals("INCOME", ignoreCase = true) }
            .sumOf { it.amount }
        val expense = monthTxs
            .filter { it.type.equals("EXPENSE", ignoreCase = true) }
            .sumOf { it.amount }
        val net = income - expense
        val rate = if (income > 0) ((net / income) * 100).toInt() else 0
        listOf(income, expense, net, rate.toDouble())
    }

    // Filter transactions - remembered
    val filteredTransactions = remember(transactions, selectedAccountFilterId, selectedFilterChip, startOfMonthMillis) {
        transactions.filter { tx ->
            val matchesAccount = selectedAccountFilterId == null || tx.accountId == selectedAccountFilterId
            val matchesChip = when (selectedFilterChip) {
                "EXPENSES" -> tx.type.equals("EXPENSE", ignoreCase = true)
                "INCOME" -> tx.type.equals("INCOME", ignoreCase = true)
                "THIS_MONTH" -> tx.timestamp >= startOfMonthMillis
                "BILLS" -> {
                    val billKeys = listOf("bill", "utility", "rent", "subscription", "electric", "internet", "phone")
                    billKeys.any { tx.title.contains(it, ignoreCase = true) || tx.category.contains(it, ignoreCase = true) || tx.tags.any { tag -> tag.contains(it, ignoreCase = true) } }
                }
                "TAX" -> tx.isTaxDeductible || tx.tags.any { it.contains("tax", ignoreCase = true) }
                else -> true
            }
            matchesAccount && matchesChip
        }
    }

    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD9242429).copy(alpha = 0.82f)
    val glassBorderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
        } else {
            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.10f), Color.Transparent)
        }
    )

    // Virtualized LazyColumn with zero nested horizontalScroll to completely eliminate page stickiness
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO NET WORTH & CASH FLOW CARD (Liquid Glass with Gradient Glow)
        item(key = "finance_hero_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = if (isLight) Color(0x18000000) else Color(0x60000000),
                        spotColor = if (isLight) Color(0x20000000) else Color(0x60000000)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(glassFill)
                    .border(width = 1.2.dp, brush = glassBorderBrush, shape = RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TOTAL NET WORTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textSecondary.copy(alpha = 0.75f),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${currencyFormatter.format(totalNetWorth)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary
                            )
                        }

                        val savingsInt = savingsRatePercent.toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (savingsInt >= 0) "Savings: +$savingsInt%" else "Savings: $savingsInt%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (savingsInt >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Monthly Cash Flow Metrics Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isLight) Color(0xFFF1F1F5) else Color(0xFF1B1B22))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inflow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = "Inflow",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Income",
                                    fontSize = 10.sp,
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = "+$${currencyFormatter.format(monthIncome)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        // Outflow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = "Outflow",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Expenses",
                                    fontSize = 10.sp,
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = "-$${currencyFormatter.format(monthExpense)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinanceActionButton(
                            label = "+ Log Entry",
                            icon = Icons.Rounded.Add,
                            backgroundColor = Color(0xFF3B82F6),
                            contentColor = Color.White,
                            onClick = onAddTransactionClick,
                            modifier = Modifier.weight(1.3f)
                        )

                        FinanceActionButton(
                            label = "Transfer",
                            icon = Icons.Rounded.SwapHoriz,
                            backgroundColor = if (isLight) Color(0xFFE8E8ED) else Color(0xFF2C2C36),
                            contentColor = palette.textPrimary,
                            onClick = { isTransferDialogOpen = true },
                            modifier = Modifier.weight(1f)
                        )

                        FinanceActionButton(
                            label = "Budget",
                            icon = Icons.Rounded.PieChart,
                            backgroundColor = if (isLight) Color(0xFFE8E8ED) else Color(0xFF2C2C36),
                            contentColor = palette.textPrimary,
                            onClick = { isAddBudgetDialogOpen = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. NATURAL LANGUAGE QUICK-CAPTURE BAR
        item(key = "finance_quick_capture") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isLight) Color.White else Color(0xFF22222A))
                    .border(
                        width = 1.dp,
                        color = if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Quick capture",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = quickInputText,
                        onValueChange = { quickInputText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = palette.textPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (quickInputText.isNotBlank()) {
                                    parseAndExecuteQuickTransaction(quickInputText, onQuickAddTransaction)
                                    quickInputText = ""
                                }
                            }
                        ),
                        cursorBrush = SolidColor(Color(0xFF3B82F6)),
                        decorationBox = { innerTextField ->
                            if (quickInputText.isEmpty()) {
                                Text(
                                    text = "Quick log: e.g. $45 Groceries #food or +2500 Salary",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (quickInputText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                                .clickable {
                                    parseAndExecuteQuickTransaction(quickInputText, onQuickAddTransaction)
                                    quickInputText = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. WALLETS & ACCOUNTS GRID (FlowRow - Non-sticky, smooth swiping)
        item(key = "finance_accounts_section") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WALLETS & ACCOUNTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAddAccountDialogOpen = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Wallet",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FlowRow wrapping accounts naturally without horizontal scroll
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "All Wallets" pill
                    val isAllSelected = selectedAccountFilterId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isAllSelected) Color(0xFF3B82F6).copy(alpha = 0.2f)
                                else if (isLight) Color.White else Color(0xFF22222A)
                            )
                            .border(
                                width = if (isAllSelected) 1.5.dp else 1.dp,
                                color = if (isAllSelected) Color(0xFF3B82F6) else if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedAccountFilterId = null }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text(
                                text = "All Wallets",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllSelected) Color(0xFF3B82F6) else palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${currencyFormatter.format(totalNetWorth)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary
                            )
                        }
                    }

                    // Individual Accounts
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountFilterId == acc.id
                        val accColor = try {
                            Color(android.graphics.Color.parseColor(acc.colorHex))
                        } catch (_: Exception) { Color(0xFF3B82F6) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) accColor.copy(alpha = 0.2f)
                                    else if (isLight) Color.White else Color(0xFF22222A)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accColor else if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedAccountFilterId = if (isSelected) null else acc.id
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Column {
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
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${currencyFormatter.format(acc.balance)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. MONTHLY BUDGET ENVELOPES
        item(key = "finance_budgets_section") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MONTHLY BUDGET ENVELOPES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${budgets.size} Active",
                        fontSize = 11.sp,
                        color = palette.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (budgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color.White else Color(0xFF202028))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { isAddBudgetDialogOpen = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Create your first budget (e.g. Groceries $500/mo)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3B82F6)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val monthTxs = remember(transactions, startOfMonthMillis) {
                            transactions.filter { it.timestamp >= startOfMonthMillis }
                        }

                        budgets.forEach { budget ->
                            val spentOnCategory = remember(monthTxs, budget.categoryName) {
                                monthTxs
                                    .filter { it.type.equals("EXPENSE", ignoreCase = true) && it.category.equals(budget.categoryName, ignoreCase = true) }
                                    .sumOf { it.amount }
                            }
                            val progress = if (budget.limitAmount > 0) (spentOnCategory / budget.limitAmount).toFloat() else 0f
                            val isExceeded = spentOnCategory > budget.limitAmount
                            val isWarning = spentOnCategory >= (budget.limitAmount * (budget.alertThresholdPercent / 100.0))

                            val barColor = when {
                                isExceeded -> Color(0xFFEF4444)
                                isWarning -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isLight) Color.White else Color(0xFF202028))
                                    .border(1.dp, if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = budget.categoryName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = palette.textPrimary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$${currencyFormatter.format(spentOnCategory)} / $${currencyFormatter.format(budget.limitAmount)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExceeded) Color(0xFFEF4444) else palette.textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Delete Budget",
                                                tint = palette.textSecondary.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { onDeleteBudget(budget.id) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = barColor,
                                        trackColor = if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. SAVINGS GOALS (FlowRow - Non-sticky, smooth swiping)
        if (financialGoals.isNotEmpty()) {
            item(key = "finance_goals_section") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SAVINGS GOALS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textSecondary,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isAddGoalDialogOpen = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "New Goal",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "New",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        financialGoals.forEach { goal ->
                            val goalProgress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                            val goalColor = try {
                                Color(android.graphics.Color.parseColor(goal.colorHex))
                            } catch (_: Exception) { Color(0xFF10B981) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isLight) Color.White else Color(0xFF202028))
                                    .border(1.dp, if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = goal.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.textPrimary,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Delete",
                                            tint = palette.textSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { onDeleteFinancialGoal(goal.id) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$${currencyFormatter.format(goal.currentAmount)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = goalColor
                                    )
                                    Text(
                                        text = "Target: $${currencyFormatter.format(goal.targetAmount)}",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { goalProgress.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.5.dp)),
                                        color = goalColor,
                                        trackColor = if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. TRANSACTIONS SECTION HEADER & FILTER CHIPS (FlowRow - Non-sticky, smooth swiping)
        item(key = "finance_transactions_header") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RECENT TRANSACTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${filteredTransactions.size} items",
                            fontSize = 11.sp,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAddTransactionClick() }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Entry",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Entry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FlowRow wrapping filter chips naturally with zero horizontal drag contention
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All",
                        "EXPENSES" to "Expenses",
                        "INCOME" to "Income",
                        "THIS_MONTH" to "This Month",
                        "BILLS" to "Bills & Rent",
                        "TAX" to "Tax Deductible"
                    ).forEach { (filterKey, label) ->
                        val isSelected = selectedFilterChip == filterKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f)
                                    else if (isLight) Color.White else Color(0xFF202028)
                                )
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) Color(0xFF3B82F6) else if (isLight) Color(0xFFE5E5EA) else Color(0xFF2E2E38),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedFilterChip = filterKey }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF3B82F6) else palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 7. TRANSACTION ITEMS (Virtualized - Instant 60/120fps Rendering)
        if (filteredTransactions.isEmpty()) {
            item(key = "finance_empty_transactions") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Wallet,
                            contentDescription = "No transactions",
                            tint = palette.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No transactions found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Log Entry' or use the quick capture bar above.",
                            fontSize = 12.sp,
                            color = palette.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredTransactions,
                key = { it.id }
            ) { tx ->
                val isIncome = tx.type.equals("INCOME", ignoreCase = true)
                val isTransfer = tx.type.equals("TRANSFER", ignoreCase = true)
                val txColor = when {
                    isIncome -> Color(0xFF10B981)
                    isTransfer -> Color(0xFF3B82F6)
                    else -> Color(0xFFEF4444)
                }

                val dateStr = remember(tx.timestamp) {
                    dateFormatter.format(Date(tx.timestamp))
                }
                val accountName = remember(accounts, tx.accountId) {
                    accounts.firstOrNull { it.id == tx.accountId }?.name ?: "Wallet"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLight) Color.White else Color(0xFF202028))
                        .border(
                            1.dp,
                            if (isLight) Color(0xFFEBEBEF) else Color(0xFF2C2C36),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onEditTransaction(tx) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(txColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isIncome -> Icons.Rounded.NorthEast
                                        isTransfer -> Icons.Rounded.SwapHoriz
                                        else -> Icons.Rounded.SouthWest
                                    },
                                    contentDescription = tx.category,
                                    tint = txColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = accountName,
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                    Text(
                                        text = " • $dateStr",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary.copy(alpha = 0.7f)
                                    )
                                }

                                if (tx.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        tx.tags.take(3).forEach { tag ->
                                            Text(
                                                text = "#$tag",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF3B82F6)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isIncome) "+$${currencyFormatter.format(tx.amount)}" else "-$${currencyFormatter.format(tx.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = txColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tx.category,
                                fontSize = 10.sp,
                                color = palette.textSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // Add Account Dialog
    if (isAddAccountDialogOpen) {
        var accName by remember { mutableStateOf("") }
        var accBalanceText by remember { mutableStateOf("") }
        var accType by remember { mutableStateOf("CASH") }
        var accColorHex by remember { mutableStateOf("#3B82F6") }

        AlertDialog(
            onDismissRequest = { isAddAccountDialogOpen = false },
            title = { Text("Add Wallet / Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account Name", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp)
                    )

                    Text("Initial Balance", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = accBalanceText,
                        onValueChange = { accBalanceText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text("Color", fontSize = 12.sp, color = palette.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#3B82F6", "#10B981", "#8B5CF6", "#F59E0B", "#EF4444").forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (accColorHex == hex) 2.dp else 0.dp,
                                        color = if (accColorHex == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { accColorHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bal = accBalanceText.toDoubleOrNull() ?: 0.0
                        if (accName.isNotBlank()) {
                            onAddAccount(accName.trim(), accType, bal, accColorHex)
                            isAddAccountDialogOpen = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddAccountDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Transfer Funds Dialog
    if (isTransferDialogOpen && accounts.size >= 2) {
        var fromId by remember { mutableStateOf(accounts[0].id) }
        var toId by remember { mutableStateOf(accounts[1].id) }
        var transferAmountText by remember { mutableStateOf("") }
        var transferNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isTransferDialogOpen = false },
            title = { Text("Transfer Between Accounts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("From Account", fontSize = 12.sp, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.forEach { acc ->
                            val isSel = fromId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.2f))
                                    .clickable { fromId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(acc.name, fontSize = 11.sp, color = if (isSel) Color.White else palette.textPrimary)
                            }
                        }
                    }

                    Text("To Account", fontSize = 12.sp, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.filter { it.id != fromId }.forEach { acc ->
                            val isSel = toId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.2f))
                                    .clickable { toId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(acc.name, fontSize = 11.sp, color = if (isSel) Color.White else palette.textPrimary)
                            }
                        }
                    }

                    Text("Amount", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = transferAmountText,
                        onValueChange = { transferAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = transferAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && fromId != toId) {
                            onTransferFunds(fromId, toId, amount, transferNotes.ifEmpty { null })
                            isTransferDialogOpen = false
                        }
                    }
                ) {
                    Text("Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { isTransferDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Budget Dialog
    if (isAddBudgetDialogOpen) {
        var catName by remember { mutableStateOf("") }
        var limitText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isAddBudgetDialogOpen = false },
            title = { Text("New Monthly Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Category Name", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp)
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Groceries", "Rent & Utilities", "Dining", "Tech", "Shopping", "Transport").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Gray.copy(alpha = 0.15f))
                                    .clickable { catName = preset }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(preset, fontSize = 10.sp, color = palette.textPrimary)
                            }
                        }
                    }

                    Text("Monthly Limit ($)", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        if (catName.isNotBlank() && limit > 0) {
                            onAddBudget(catName.trim(), limit, 80.0)
                            isAddBudgetDialogOpen = false
                        }
                    }
                ) {
                    Text("Set Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddBudgetDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Financial Goal Dialog
    if (isAddGoalDialogOpen) {
        var goalTitle by remember { mutableStateOf("") }
        var targetAmountText by remember { mutableStateOf("") }
        var currentAmountText by remember { mutableStateOf("") }
        var goalColorHex by remember { mutableStateOf("#10B981") }

        AlertDialog(
            onDismissRequest = { isAddGoalDialogOpen = false },
            title = { Text("New Savings Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Goal Title", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp)
                    )

                    Text("Target Amount ($)", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text("Current Saved ($)", fontSize = 12.sp, color = palette.textSecondary)
                    BasicTextField(
                        value = currentAmountText,
                        onValueChange = { currentAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = targetAmountText.toDoubleOrNull() ?: 0.0
                        val current = currentAmountText.toDoubleOrNull() ?: 0.0
                        if (goalTitle.isNotBlank() && target > 0) {
                            onAddFinancialGoal(goalTitle.trim(), target, current, goalColorHex)
                            isAddGoalDialogOpen = false
                        }
                    }
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddGoalDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Backward-compatible alias for LunaFinanceView
 */
@Composable
fun LunaWealthView(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    budgets: List<BudgetEntity>,
    financialGoals: List<FinancialGoalEntity>,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onQuickAddTransaction: (title: String, amount: Double, type: String, tags: List<String>) -> Unit,
    onAddAccount: (name: String, accountType: String, balance: Double, colorHex: String) -> Unit,
    onTransferFunds: (fromAccountId: Long, toAccountId: Long, amount: Double, notes: String?) -> Unit,
    onAddBudget: (categoryName: String, limitAmount: Double, alertThresholdPercent: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddFinancialGoal: (title: String, targetAmount: Double, currentAmount: Double, colorHex: String) -> Unit,
    onDeleteFinancialGoal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LunaFinanceView(
        transactions = transactions,
        accounts = accounts,
        budgets = budgets,
        financialGoals = financialGoals,
        onAddTransactionClick = onAddTransactionClick,
        onEditTransaction = onEditTransaction,
        onDeleteTransaction = onDeleteTransaction,
        onQuickAddTransaction = onQuickAddTransaction,
        onAddAccount = onAddAccount,
        onTransferFunds = onTransferFunds,
        onAddBudget = onAddBudget,
        onDeleteBudget = onDeleteBudget,
        onAddFinancialGoal = onAddFinancialGoal,
        onDeleteFinancialGoal = onDeleteFinancialGoal,
        modifier = modifier
    )
}

/**
 * Natural language helper to parse quick entries like:
 * "$45 Groceries #supplies"
 * "+1800 Project milestone #income"
 */
private fun parseAndExecuteQuickTransaction(
    text: String,
    onExecute: (title: String, amount: Double, type: String, tags: List<String>) -> Unit
) {
    val clean = text.trim()
    val isIncome = clean.startsWith("+")
    val amountRegex = Regex("""[+-]?\$?(\d+(?:\.\d{1,2})?)""")
    val match = amountRegex.find(clean)

    val amount = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    if (amount <= 0.0) return

    val type = if (isIncome) "INCOME" else "EXPENSE"

    // Extract tags
    val tags = Regex("""#(\w+)""").findAll(clean).map { it.groupValues[1] }.toList()

    // Title is everything minus amount and tags
    val title = clean
        .replace(Regex("""[+-]?\$?\d+(?:\.\d{1,2})?"""), "")
        .replace(Regex("""#\w+"""), "")
        .trim()
        .ifEmpty { if (isIncome) "Quick Income" else "Quick Expense" }

    onExecute(title, amount, type, tags)
}

@Composable
private fun FinanceActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
