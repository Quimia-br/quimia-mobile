package com.quimia.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bottomSheet.QuimiaBottomSheet
import bottomSheet.QuimiaBottomSheetContent
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.quimia.android.presentation.system.HideSystemNavigationBar
import input.QuimiaInput
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

@Composable
internal fun BirthDateField(value: String, onValueChange: (String) -> Unit, error: String? = null) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    fun open() { keyboard?.hide(); expanded = true }
    Box(Modifier.fillMaxWidth().clearAndSetSemantics {
        role = Role.Button
        contentDescription = "Data de nascimento, ${value.ifBlank { "não selecionada" }}${error?.let { ". $it" }.orEmpty()}"
        onClick("Selecionar data de nascimento") { open(); true }
    }) {
        QuimiaInput(value, {}, label = "Data de nascimento", placeholder = "DD/MM/AAAA", enabled = false,
            trailingIcon = Lucide.Calendar, trailingIconSize = 16.dp, showTrailingIconWhenEmpty = true,
            onTrailingIconClick = ::open, error = error)
        Box(Modifier.matchParentSize().clickable(onClick = ::open))
    }
    if (expanded) BirthDateSheet(value, { expanded = false }, { onValueChange(it); expanded = false })
}

@Composable
internal fun BirthDateSheet(value: String, onDismiss: () -> Unit, onSave: (String) -> Unit,
    today: BirthDate = BirthDate.today()) {
    val parsed = BirthDate.parse(value)?.takeIf { it <= today }
    var year by rememberSaveable { mutableIntStateOf(parsed?.year ?: today.year) }
    var month by rememberSaveable { mutableIntStateOf(parsed?.month ?: if (isLegacyBirthDate(value)) value.takeLast(2).toInt() else today.month) }
    var selected by rememberSaveable { mutableStateOf(parsed?.toString()) }
    val content: @Composable ColumnScope.() -> Unit = {
        HideSystemNavigationBar()
        val tokens = quimiaColorTokens()
        Column(Modifier.fillMaxWidth().heightIn(min = 529.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Selecionar data\nde nascimento", Modifier.weight(1f), style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                QuimiaIconButton(icon = Lucide.X, containerColor = tokens.secondary, onClick = onDismiss, contentDescription = "Fechar calendário")
            }
            BirthCalendar(year, month, selected?.let { BirthDate.parse(it) }, today,
                onMonth = { if (month != it) { month = it; selected = null } },
                onYear = { if (year != it) { year = it; selected = null } },
                onDay = { selected = BirthDate(year, month, it).toString() })
            QuimiaButton(text = "Salvar", onClick = { selected?.let(onSave) }, enabled = selected != null,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), iconSize = 0, espacamento = 0,
                textStyle = QuimiaTextStyles.medium16, textColor = LightTokens.foregroundPrimary)
        }
    }
    val shape = RoundedCornerShape(40.dp)
    if (LocalInspectionMode.current) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.widthIn(max = 360.dp).fillMaxWidth().padding(start = 17.dp, end = 17.dp, bottom = 11.dp),
            color = quimiaColorTokens().surfaceBase, shape = shape, shadowElevation = 12.dp, border = quimiaContrastBorder()) {
            QuimiaBottomSheetContent(title = null, showDragHandle = false, showCloseButton = false,
                contentHeight = null, content = content)
        }
    } else QuimiaBottomSheet(isVisible = true, onDismissRequest = onDismiss, title = null,
        showDragHandle = false, showCloseButton = false, sheetMaxWidth = 360.dp, contentHeight = null,
        modifier = Modifier.statusBarsPadding().padding(start = 17.dp, end = 17.dp, bottom = 11.dp),
        shape = shape, scrimColor = Color.Transparent, content = content)
}

private val BirthMonths = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

@Composable
private fun BirthCalendar(year: Int, month: Int, selected: BirthDate?, today: BirthDate,
    onMonth: (Int) -> Unit, onYear: (Int) -> Unit, onDay: (Int) -> Unit) {
    val tokens = quimiaColorTokens()
    var monthMenu by remember { mutableStateOf(false) }
    var yearMenu by remember { mutableStateOf(false) }
    Surface(color = tokens.surfaceBackground, shape = RoundedCornerShape(24.dp), border = quimiaContrastBorder(),
        modifier = Modifier.fillMaxWidth().height(322.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth().height(32.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    CalendarMenuLabel(BirthMonths[month - 1], "Selecionar mês", { monthMenu = true }, fillWidth = true)
                    DropdownMenu(monthMenu, { monthMenu = false }, containerColor = tokens.surfaceBase) {
                        BirthMonths.forEachIndexed { index, name -> DropdownMenuItem(text = { Text(name, style = QuimiaTextStyles.medium14) },
                            onClick = { onMonth(index + 1); monthMenu = false }) }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box {
                    CalendarMenuLabel(year.toString(), "Selecionar ano", { yearMenu = true })
                    DropdownMenu(yearMenu, { yearMenu = false }, containerColor = tokens.surfaceBase) {
                        Box(Modifier.size(112.dp, 240.dp)) {
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = (today.year - year).coerceAtLeast(0))
                            LazyColumn(state = listState) {
                                items((today.year downTo 1).toList()) { item -> DropdownMenuItem(text = { Text(item.toString(), style = QuimiaTextStyles.medium14) },
                                    onClick = { onYear(item); yearMenu = false }) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().height(32.dp)) {
                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(it, style = QuimiaTextStyles.medium12.copy(fontSize = 12.857.sp, lineHeight = 16.071.sp, fontWeight = FontWeight.Normal),
                            color = tokens.foregroundPrimary.copy(alpha = .9f))
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Column(Modifier.weight(1f).selectableGroup(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val days = birthCalendarDays(year, month)
                val firstDayIndex = days.indexOf(1)
                days.chunked(7).forEachIndexed { weekIndex, week ->
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        week.forEachIndexed { columnIndex, day ->
                            val date = day?.let { BirthDate(year, month, it) }
                            val enabled = date != null && date <= today
                            val chosen = date != null && date == selected
                            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                if (day != null) Box(Modifier.size(32.dp)
                                    .background(if (chosen) tokens.primary else Color.Transparent, CircleShape)
                                    .clickable(enabled = enabled, role = Role.RadioButton) { onDay(day) }
                                    .semantics { this.selected = chosen; contentDescription = date.toString() }, contentAlignment = Alignment.Center) {
                                    Text(day.toString(), style = QuimiaTextStyles.medium12.copy(fontSize = 12.857.sp, lineHeight = 16.071.sp, fontWeight = FontWeight.Normal),
                                        color = if (chosen) LightTokens.foregroundPrimary else tokens.foregroundPrimary.copy(alpha = if (enabled) 1f else .35f))
                                }
                                else {
                                    val index = weekIndex * 7 + columnIndex
                                    val previousMonth = if (month == 1) 12 else month - 1
                                    val adjacentDay = if (index < firstDayIndex) daysInMonth(if (month == 1) year - 1 else year, previousMonth) - firstDayIndex + index + 1
                                        else index - firstDayIndex - daysInMonth(year, month) + 1
                                    Text(adjacentDay.toString(), style = QuimiaTextStyles.medium12.copy(fontSize = 12.857.sp, lineHeight = 16.071.sp, fontWeight = FontWeight.Normal),
                                        color = tokens.foregroundSubtle.copy(alpha = .35f), modifier = Modifier.clearAndSetSemantics { })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMenuLabel(text: String, description: String, onClick: () -> Unit, fillWidth: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)) {
        Text(text, modifier = if (fillWidth) Modifier.weight(1f, fill = false) else Modifier,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = QuimiaTextStyles.medium14.copy(fontSize = 14.464.sp, lineHeight = 19.286.sp), color = quimiaColorTokens().foregroundPrimary)
        QuimiaIconButton(icon = Lucide.ChevronDown, onClick = onClick, size = QuimiaIconButtonSize.Small, iconSize = 20.dp,
            containerColor = quimiaColorTokens().surfaceBase, contentDescription = description)
    }
}
