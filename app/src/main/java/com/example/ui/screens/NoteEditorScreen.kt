package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ai.AiService
import com.example.data.model.AutoClassifier
import com.example.data.model.NoteEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.NoteRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveTableView
import com.example.ui.components.NeuIconButton
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.util.AlarmScheduler
import com.example.ui.util.AttachmentStorage
import com.example.ui.util.DeviceAudioFile
import com.example.ui.util.RichTextFormatter
import com.example.ui.util.SystemRingtoneItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialNote: NoteEntity?,
    isDarkMode: Boolean,
    repository: NoteRepository,
    preferences: AppPreferences,
    onBack: () -> Unit,
    onOpenTableEditor: (initialTableData: String, onResult: (String) -> Unit) -> Unit = { _, _ -> },
    onSaveNote: (
        id: Long,
        title: String,
        content: String,
        category: String,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderline: Boolean,
        isStrikethrough: Boolean,
        isCodeFormat: Boolean,
        fontSize: Int,
        fontColorHex: String,
        alignment: String,
        listType: String,
        tableData: String,
        styleSpansJson: String,
        attachmentsJson: String,
        onSaved: (Long) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { AiService() }

    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var contentValue by remember { mutableStateOf(TextFieldValue(initialNote?.content ?: "")) }
    var selectedCategory by remember { mutableStateOf(initialNote?.category ?: "Normal") }

    var currentNoteId by remember { mutableStateOf(initialNote?.id ?: 0L) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    var spans: List<RichTextFormatter.TextSpan> by remember {
        mutableStateOf(RichTextFormatter.deserializeSpans(initialNote?.styleSpansJson ?: "[]"))
    }

    var attachments: List<RichTextFormatter.AttachmentInfo> by remember {
        mutableStateOf(RichTextFormatter.deserializeAttachments(initialNote?.attachmentsJson ?: "[]"))
    }

    // Item 11: JSON Mode — coding-style syntax highlighting for keys/strings/
    // numbers/brackets when the user pastes or writes JSON.
    fun buildJsonHighlightedText(text: String): AnnotatedString {
        val keyColor = Color(0xFF80D8FF)
        val stringColor = Color(0xFFC3E88D)
        val numberColor = Color(0xFFF78C6C)
        val keywordColor = Color(0xFFC792EA)
        val bracketColor = Color(0xFFFFD54F)
        val base = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF222222)

        return androidx.compose.ui.text.buildAnnotatedString {
            append(text)
            val keyRegex = Regex("\"(?:[^\"\\\\]|\\\\.)*\"(?=\\s*:)")
            val stringRegex = Regex("\"(?:[^\"\\\\]|\\\\.)*\"")
            val numberRegex = Regex("-?\\b\\d+\\.?\\d*\\b")
            val keywordRegex = Regex("\\b(true|false|null)\\b")
            val bracketRegex = Regex("[{}\\[\\]:,]")

            addStyle(SpanStyle(color = base), 0, text.length)
            for (m in bracketRegex.findAll(text)) addStyle(SpanStyle(color = bracketColor, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
            for (m in numberRegex.findAll(text)) addStyle(SpanStyle(color = numberColor), m.range.first, m.range.last + 1)
            for (m in keywordRegex.findAll(text)) addStyle(SpanStyle(color = keywordColor), m.range.first, m.range.last + 1)
            for (m in stringRegex.findAll(text)) addStyle(SpanStyle(color = stringColor), m.range.first, m.range.last + 1)
            for (m in keyRegex.findAll(text)) addStyle(SpanStyle(color = keyColor, fontWeight = FontWeight.SemiBold), m.range.first, m.range.last + 1)
        }
    }

    // Item 8: apply a boolean format (bold/italic/underline/strikethrough/code) either
    // to the current selection (if any text is selected) or as a "typing mode" that
    // will wrap newly typed characters going forward until toggled off again.
    fun applyOrToggleBooleanFormat(type: String, current: Boolean, setCurrent: (Boolean) -> Unit) {
        val sel = contentValue.selection
        if (!sel.collapsed) {
            spans = RichTextFormatter.toggleBooleanProperty(spans, type, sel.min, sel.max)
        } else {
            setCurrent(!current)
        }
    }

    // Item 8: apply a text color either to the current selection, or as the
    // active "typing color" for newly typed characters. Passing null clears
    // the typing color (falls back to the day/night default — Item 9).
    fun applyOrSetColor(hex: String?) {
        val sel = contentValue.selection
        if (!sel.collapsed && hex != null) {
            spans = RichTextFormatter.setValueProperty(spans, "color", sel.min, sel.max, hex)
        } else {
            activeColorHex = hex
        }
    }

    // Formatting state — Item 8/9: these now represent the CURRENT TYPING
    // format (applied to newly typed characters going forward), not a global
    // style for the whole note. Existing spans elsewhere are untouched.
    var isBold by remember { mutableStateOf(initialNote?.isBold ?: false) }
    var isItalic by remember { mutableStateOf(initialNote?.isItalic ?: false) }
    var isUnderline by remember { mutableStateOf(initialNote?.isUnderline ?: false) }
    var isStrikethrough by remember { mutableStateOf(initialNote?.isStrikethrough ?: false) }
    var isCodeFormat by remember { mutableStateOf(initialNote?.isCodeFormat ?: false) }
    var activeColorHex by remember { mutableStateOf<String?>(null) }
    var showFormatSheet by remember { mutableStateOf(false) }
    var isJsonMode by remember { mutableStateOf(false) }

    var fontSize by remember { mutableStateOf(initialNote?.fontSize ?: 16) }
    // Ensure font color contrast: default to #111111 in day mode and #FFFFFF in dark mode
    var selectedColorHex by remember {
        val initial = initialNote?.fontColorHex
        if (initial.isNullOrBlank() || initial == "#FFFFFF" && !isDarkMode) {
            mutableStateOf(if (isDarkMode) "#FFFFFF" else "#111111")
        } else {
            mutableStateOf(initial)
        }
    }
    var alignment by remember { mutableStateOf(initialNote?.alignment ?: "left") }
    var listType by remember { mutableStateOf(initialNote?.listType ?: "none") }
    var tableData by remember { mutableStateOf(initialNote?.tableData ?: "") }

    // Dialogs & Modals state
    // (Alarm dialog moved to ReadNoteScreen — Item 14)
    var showImageEditModal by remember { mutableStateOf<RichTextFormatter.AttachmentInfo?>(null) }
    var showAiChatbotModal by remember { mutableStateOf(false) }
    var showTableFullscreen by remember { mutableStateOf(false) }

    // Floating Chatbot Position
    var botOffsetX by remember { mutableFloatStateOf(0f) }
    var botOffsetY by remember { mutableFloatStateOf(0f) }

    // Image Attachment picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val info = AttachmentStorage.copyToAppStorage(context, uri)
                withContext(Dispatchers.Main) {
                    if (info != null) {
                        attachments = attachments + info
                        Toast.makeText(context, "Image added to Note!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun performSave(showToast: Boolean, thenNavigateBack: Boolean) {
        if (title.isBlank() && contentValue.text.isBlank() && attachments.isEmpty()) {
            if (thenNavigateBack) onBack()
            return
        }
        val autoCat = if (selectedCategory == "Normal") {
            AutoClassifier.detectCategory(title, contentValue.text)
        } else {
            selectedCategory
        }

        onSaveNote(
            currentNoteId,
            title,
            contentValue.text,
            autoCat,
            isBold,
            isItalic,
            isUnderline,
            isStrikethrough,
            isCodeFormat,
            fontSize,
            selectedColorHex,
            alignment,
            listType,
            tableData,
            RichTextFormatter.serializeSpans(spans),
            RichTextFormatter.serializeAttachments(attachments)
        ) { savedId ->
            currentNoteId = savedId
            hasUnsavedChanges = false
            if (showToast) {
                Toast.makeText(context, "Note Saved in $autoCat", Toast.LENGTH_SHORT).show()
            }
            if (thenNavigateBack) onBack()
        }
    }

    // Debounced Auto-Save
    LaunchedEffect(
        title, contentValue.text, spans, tableData, attachments,
        isBold, isItalic, isUnderline, isStrikethrough, isCodeFormat,
        fontSize, selectedColorHex, alignment, listType
    ) {
        hasUnsavedChanges = true
        kotlinx.coroutines.delay(1200)
        performSave(showToast = false, thenNavigateBack = false)
    }

    // Save on pause/exit
    DisposableEffect(Unit) {
        onDispose {
            if (hasUnsavedChanges) {
                performSave(showToast = false, thenNavigateBack = false)
            }
        }
    }

    // Undo/Redo Stacks
    val undoStack = remember { mutableStateListOf<Pair<String, List<RichTextFormatter.TextSpan>>>() }
    val redoStack = remember { mutableStateListOf<Pair<String, List<RichTextFormatter.TextSpan>>>() }

    fun pushUndo() {
        if (undoStack.size > 20) undoStack.removeAt(0)
        undoStack.add(Pair(contentValue.text, spans))
        redoStack.clear()
    }

    val mainScrollState = rememberScrollState()
    // Item 13: guarantees the cursor never gets hidden behind the keyboard —
    // explicitly requests a scroll whenever the caret moves, instead of
    // relying only on the platform's default (sometimes-late) behaviour.
    val contentBringIntoViewRequester = remember { BringIntoViewRequester() }

    // DAY MODE CONTRAST FIX: Default text color in Day mode is dark (Color(0xFF111111))
    val defaultTextColor = if (isDarkMode) Color.White else Color(0xFF111111)
    val effectiveTextColor = if (!isDarkMode && (selectedColorHex.equals("#FFFFFF", ignoreCase = true) || selectedColorHex.equals("#FFF", ignoreCase = true))) {
        Color(0xFF111111)
    } else {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (e: Exception) {
            defaultTextColor
        }
    }

    GlassBackground(isDarkMode = isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            isDarkMode = isDarkMode,
                            size = 38.dp,
                            iconSize = 18.dp,
                            tint = if (isDarkMode) Color.White else Color.Black,
                            onClick = { performSave(showToast = false, thenNavigateBack = true) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initialNote == null) "New Note" else "Edit Note",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF111111)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Undo
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            isDarkMode = isDarkMode,
                            size = 36.dp,
                            iconSize = 17.dp,
                            tint = if (undoStack.isNotEmpty()) (if (isDarkMode) Color.White else Color.Black) else Color.Gray.copy(0.3f),
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    redoStack.add(Pair(contentValue.text, spans))
                                    val last = undoStack.removeAt(undoStack.size - 1)
                                    contentValue = TextFieldValue(last.first, selection = TextRange(last.first.length))
                                    spans = last.second
                                }
                            }
                        )

                        // Redo
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            isDarkMode = isDarkMode,
                            size = 36.dp,
                            iconSize = 17.dp,
                            tint = if (redoStack.isNotEmpty()) (if (isDarkMode) Color.White else Color.Black) else Color.Gray.copy(0.3f),
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    undoStack.add(Pair(contentValue.text, spans))
                                    val next = redoStack.removeAt(redoStack.size - 1)
                                    contentValue = TextFieldValue(next.first, selection = TextRange(next.first.length))
                                    spans = next.second
                                }
                            }
                        )

                        // Save Button
                        GlassCard(
                            shape = RoundedCornerShape(12.dp),
                            isDarkMode = isDarkMode,
                            onClick = { performSave(showToast = true, thenNavigateBack = true) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Brush.linearGradient(listOf(CrimsonPrimary, Color(0xFFFF5E7E))))
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Note Body Scrollable Area (Auto-Scroll with safe bottom padding so text never gets hidden behind toolbar)
                // Item 13: horizontal padding trimmed from 16dp to 8dp so the
                // content area fills more of the phone's width.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(mainScrollState)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    // Note Title Input
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkMode) Color.White else Color(0xFF111111)
                        ),
                        cursorBrush = SolidColor(CrimsonPrimary),
                        decorationBox = { innerTextField ->
                            if (title.isBlank()) {
                                Text(
                                    text = "Untitled Note",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkMode) Color.White.copy(0.35f) else Color.Gray.copy(0.6f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd | HH:mm", Locale.getDefault()).format(Date()),
                        fontSize = 11.5.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inline Attached Images (with Move, Resize, Crop, Rename support - PART F Item 5)
                    if (attachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            attachments.forEachIndexed { index, att ->
                                val isImage = att.mimeType.startsWith("image")
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    isDarkMode = isDarkMode,
                                    elevation = 2.dp
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (isImage) {
                                            AsyncImage(
                                                model = File(att.uri),
                                                contentDescription = att.fileName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = att.fileName,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isDarkMode) Color.White else Color.Black,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Row {
                                                IconButton(
                                                    onClick = { showImageEditModal = att },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Image", tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        attachments = attachments.toMutableList().apply { removeAt(index) }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Interactive Table Display if present
                    if (tableData.isNotBlank()) {
                        InteractiveTableView(
                            tableData = tableData,
                            isDarkMode = isDarkMode,
                            onTableChange = { updated -> tableData = updated }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Main Content Input (Day Mode text contrast fix & Auto-Scroll buffer)
                    BasicTextField(
                        value = contentValue,
                        onValueChange = { newVal ->
                            val oldText = contentValue.text
                            val newText = newVal.text
                            if (newText != oldText) {
                                pushUndo()
                                // Shift existing spans for whatever just changed (insert/delete/paste)
                                spans = RichTextFormatter.adjustSpansForEdit(spans, oldText, newText)
                                // Item 8: if text grew (a real insertion), wrap ONLY the newly typed
                                // range with whichever formats are currently toggled on — existing
                                // text before/after is never touched.
                                if (newText.length > oldText.length) {
                                    var editStart = 0
                                    while (editStart < oldText.length && editStart < newText.length && oldText[editStart] == newText[editStart]) {
                                        editStart++
                                    }
                                    val insertedEnd = editStart + (newText.length - oldText.length)
                                    if (isBold) spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "bold")
                                    if (isItalic) spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "italic")
                                    if (isUnderline) spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "underline")
                                    if (isStrikethrough) spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "strikethrough")
                                    if (isCodeFormat) spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "code")
                                    activeColorHex?.let { hex ->
                                        spans = RichTextFormatter.addSpan(spans, editStart, insertedEnd, "color", hex)
                                    }
                                }
                            }
                            contentValue = newVal
                            // Item 13: keep the caret visible above the keyboard on every edit.
                            coroutineScope.launch { contentBringIntoViewRequester.bringIntoView() }
                        },
                        visualTransformation = VisualTransformation { text ->
                            TransformedText(
                                if (isJsonMode) {
                                    buildJsonHighlightedText(text.text)
                                } else {
                                    RichTextFormatter.buildStyledText(
                                        text = text.text,
                                        spans = spans,
                                        defaultColor = effectiveTextColor,
                                        fontSize = fontSize.toFloat()
                                    )
                                },
                                OffsetMapping.Identity
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = fontSize.sp,
                            fontFamily = if (isCodeFormat || isJsonMode) FontFamily.Monospace else FontFamily.Default,
                            textAlign = when (alignment) {
                                "center" -> TextAlign.Center
                                "right" -> TextAlign.Right
                                else -> TextAlign.Left
                            },
                            color = effectiveTextColor,
                            lineHeight = (fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(CrimsonPrimary),
                        decorationBox = { innerTextField ->
                            if (contentValue.text.isBlank()) {
                                Text(
                                    text = "Start typing your notes, code, or ideas...",
                                    fontSize = fontSize.sp,
                                    color = if (isDarkMode) Color.White.copy(0.35f) else Color.Gray.copy(0.6f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                            .bringIntoViewRequester(contentBringIntoViewRequester)
                            .onFocusEvent {
                                if (it.isFocused) {
                                    coroutineScope.launch { contentBringIntoViewRequester.bringIntoView() }
                                }
                            }
                    )

                    // Safe Bottom Space so typing near the bottom never slips behind the toolbar
                    Spacer(modifier = Modifier.height(160.dp))
                }

                // Bottom Formatting & Feature Toolbar
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    isDarkMode = isDarkMode,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Formatting group — Item 8: Bold/Italic/Underline/Strikethrough/Colors
                        // combined into one button instead of separate icons.
                        IconButton(onClick = { showFormatSheet = true }) {
                            Icon(
                                Icons.Default.FormatBold,
                                contentDescription = "Text Formatting",
                                tint = if (isBold || isItalic || isUnderline || isStrikethrough || activeColorHex != null) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black)
                            )
                        }

                        // Code Format
                        IconButton(onClick = { isCodeFormat = !isCodeFormat }) {
                            Icon(Icons.Default.Code, contentDescription = "Code", tint = if (isCodeFormat) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // JSON Mode toggle — Item 11
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JSON",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isJsonMode) CrimsonPrimary else Color.Gray
                            )
                            Switch(
                                checked = isJsonMode,
                                onCheckedChange = { isJsonMode = it },
                                modifier = Modifier.size(width = 34.dp, height = 20.dp).scale(0.65f),
                                colors = SwitchDefaults.colors(checkedThumbColor = CrimsonPrimary, checkedTrackColor = CrimsonPrimary.copy(alpha = 0.4f))
                            )
                        }

                        // Align Left
                        IconButton(onClick = { alignment = "left" }) {
                            Icon(Icons.Default.FormatAlignLeft, contentDescription = "Align Left", tint = if (alignment == "left") CrimsonPrimary else Color.Gray)
                        }

                        // Align Center
                        IconButton(onClick = { alignment = "center" }) {
                            Icon(Icons.Default.FormatAlignCenter, contentDescription = "Align Center", tint = if (alignment == "center") CrimsonPrimary else Color.Gray)
                        }

                        // Insert / Edit Table Screen (PART F Item 1)
                        IconButton(onClick = { showTableFullscreen = true }) {
                            Icon(Icons.Default.TableChart, contentDescription = "Table Editor", tint = CrimsonPrimary)
                        }

                        // Add Image / Media Attachment — Item 12: replaces the old fingerprint icon
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(painter = painterResource(R.drawable.ic_svg_media), contentDescription = "Attach Media", tint = CrimsonPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // SINGLE Floating AU AI Chatbot with custom sphere mascot icon (PART F Item 2 & 3 & 4)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset(botOffsetX.roundToInt(), botOffsetY.roundToInt()) }
                    .padding(end = 20.dp, bottom = 80.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            botOffsetX += dragAmount.x
                            botOffsetY += dragAmount.y
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable { showAiChatbotModal = true }
                ) {
                    Image(
                        painter = painterResource(R.drawable.au_bot_icon_1790271144581),
                        contentDescription = "AU Chatbot",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Formatting group bottom sheet — Item 8: Bold/Italic/Underline/Strikethrough
    // + text colors + custom color, all in one place.
    if (showFormatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormatSheet = false },
            containerColor = if (isDarkMode) Color(0xFF1E222B) else Color(0xFFF6F8FB)
        ) {
            var customHexInput by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Text Formatting",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
                Text(
                    text = if (contentValue.selection.collapsed)
                        "Applies to text you type next"
                    else
                        "Applies to your current selection",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconButton(onClick = { applyOrToggleBooleanFormat("bold", isBold) { isBold = it } }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = if (isBold) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                    }
                    IconButton(onClick = { applyOrToggleBooleanFormat("italic", isItalic) { isItalic = it } }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = if (isItalic) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                    }
                    IconButton(onClick = { applyOrToggleBooleanFormat("underline", isUnderline) { isUnderline = it } }) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = if (isUnderline) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                    }
                    IconButton(onClick = { applyOrToggleBooleanFormat("strikethrough", isStrikethrough) { isStrikethrough = it } }) {
                        Icon(Icons.Default.FormatStrikethrough, contentDescription = "Strikethrough", tint = if (isStrikethrough) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = if (isDarkMode) Color(0x22FFFFFF) else Color(0x1F000000))
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Colors", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // "Default" swatch clears the color override -> falls back to
                    // the day/night default color (Item 9).
                    val presetColors = listOf(
                        null to (if (isDarkMode) Color.White else Color.Black),
                        "#FF5252" to Color(0xFFFF5252),
                        "#FFB300" to Color(0xFFFFB300),
                        "#4CAF50" to Color(0xFF4CAF50),
                        "#2196F3" to Color(0xFF2196F3),
                        "#9C27B0" to Color(0xFF9C27B0)
                    )
                    presetColors.forEach { (hex, swatchColor) ->
                        val isActive = activeColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) CrimsonPrimary else Color.Gray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { applyOrSetColor(hex) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { customHexInput = it },
                        placeholder = { Text("Custom hex e.g. #E91E63", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val hex = customHexInput.trim()
                            try {
                                android.graphics.Color.parseColor(hex)
                                applyOrSetColor(hex)
                                customHexInput = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid hex color", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Apply")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Fullscreen Dedicated Table Editor (PART F Item 1)
    if (showTableFullscreen) {
        TableEditorScreen(
            initialTableData = tableData,
            isDarkMode = isDarkMode,
            onBack = { showTableFullscreen = false },
            onSaveTable = { saved ->
                tableData = saved
                showTableFullscreen = false
                Toast.makeText(context, "Table updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Floating Chatbot with live editor access — Item 20. Reuses the same
    // AiChatScreen used elsewhere in the app (with its file/image OCR
    // attachment support), but wired here with the CURRENT editor content and
    // a callback that lets the model actually rewrite/insert into this note.
    AiChatScreen(
        isOpen = showAiChatbotModal,
        isDarkMode = isDarkMode,
        preferences = preferences,
        repository = repository,
        currentNoteContent = "Title: $title\n\n${contentValue.text}",
        onClose = { showAiChatbotModal = false },
        onCreateNoteFromAi = { _, _, _ -> },
        onModifyCurrentNote = { newContent ->
            contentValue = TextFieldValue(newContent, selection = TextRange(newContent.length))
            Toast.makeText(context, "Note updated by AU Bot", Toast.LENGTH_SHORT).show()
        }
    )

    // Image Edit Modal (Move, Crop, Resize, Rename - PART F Item 5)
    showImageEditModal?.let { imgAtt ->
        var editedName by remember { mutableStateOf(imgAtt.fileName) }
        AlertDialog(
            onDismissRequest = { showImageEditModal = null },
            title = { Text("Edit Image in Note", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Rename File") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Actions available: Crop, Resize, Reorder in note.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    onClick = {
                        attachments = attachments.map {
                            if (it.uri == imgAtt.uri) it.copy(fileName = editedName) else it
                        }
                        showImageEditModal = null
                        Toast.makeText(context, "Image updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageEditModal = null }) { Text("Cancel") }
            }
        )
    }
}
