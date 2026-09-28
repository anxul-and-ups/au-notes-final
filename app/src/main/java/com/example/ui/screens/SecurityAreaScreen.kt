package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.NoteEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.NoteRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.NeuIconButton
import com.example.ui.theme.CrimsonPrimary
import kotlinx.coroutines.launch

@Composable
fun SecurityAreaScreen(
    repository: NoteRepository,
    preferences: AppPreferences,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onOpenNote: (NoteEntity) -> Unit,
    vaultUnlockedExternal: Boolean = false,
    onVaultUnlockedChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allNotes by repository.allActiveNotes.collectAsState(initial = emptyList())
    val storedPin by preferences.lockPin.collectAsState()
    val secQuestion by preferences.securityQuestion.collectAsState()
    val secAnswer by preferences.securityAnswer.collectAsState()
    val lockedFolders by preferences.lockedFolders.collectAsState()
    val customFolders by preferences.customFolders.collectAsState()
    val hiddenFolders by preferences.hiddenFolders.collectAsState()
    val hasCustomPin by preferences.hasCustomPin.collectAsState()
    val autoLockMode by preferences.securityAutoLockMode.collectAsState()
    var showHideFolderPicker by remember { mutableStateOf(false) }

    // Local mirror of the unlocked state. For "Immediately" / "When leaving
    // Security Area" modes we always force this back to false the moment this
    // screen is (re)composed, i.e. hard auto-lock on every exit (Item 1 / 4 / 40).
    // For "When app is closed" / "When screen is locked off" modes we instead
    // trust vaultUnlockedExternal, which AuNotesApp only clears on the matching
    // lifecycle event, so the vault can stay open across a Settings<->Security
    // round-trip within the same app session.
    var isVaultUnlocked by remember { mutableStateOf(vaultUnlockedExternal) }
    var showAuthPrompt by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (autoLockMode == AppPreferences.AUTO_LOCK_IMMEDIATE || autoLockMode == AppPreferences.AUTO_LOCK_ON_LEAVE_AREA) {
            isVaultUnlocked = false
            onVaultUnlockedChange(false)
        }
    }

    if (showAuthPrompt && !isVaultUnlocked) {
        com.example.ui.components.PinLockDialog(
            correctPin = storedPin,
            title = "Security Vault Locked",
            subtitle = "Enter 4-digit PIN or use Fingerprint to unlock",
            securityQuestion = secQuestion,
            securityAnswer = secAnswer,
            isDarkMode = isDarkMode,
            requireSetup = !hasCustomPin,
            onDismiss = {
                showAuthPrompt = false
                if (!isVaultUnlocked) {
                    onBack()
                }
            },
            onUnlocked = {
                isVaultUnlocked = true
                onVaultUnlockedChange(true)
                showAuthPrompt = false
            },
            onSetupComplete = { pin, question, answer ->
                preferences.setSecurityDetails(pin, question, answer)
                Toast.makeText(context, "Security PIN created successfully!", Toast.LENGTH_SHORT).show()
            },
            onPinReset = { newPin ->
                preferences.setLockPin(newPin)
                isVaultUnlocked = true
                onVaultUnlockedChange(true)
                showAuthPrompt = false
                Toast.makeText(context, "PIN Reset Successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val hiddenNotes = remember(allNotes) {
        allNotes.filter { it.isTrash == false && it.isLocked } // Secure locked notes stored here
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hidden Notes, 1: Hidden/Locked Folders, 2: Change Lock

    // Change Lock fields
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var newSecQuestionInput by remember { mutableStateOf(secQuestion) }
    var newSecAnswerInput by remember { mutableStateOf("") }
    var isOldPinVerified by remember { mutableStateOf(false) }

    GlassBackground(isDarkMode = isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            onClick = onBack
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Security Area",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color(0xFF111111)
                            )
                            Text(
                                text = "PIN & Biometric Protected Vault",
                                fontSize = 11.sp,
                                color = CrimsonPrimary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isVaultUnlocked) {
                    // Vault Locked Placeholder UI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Security Vault is Locked",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Authentication required to access protected files",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassCard(
                                modifier = Modifier.padding(horizontal = 32.dp),
                                shape = RoundedCornerShape(14.dp),
                                isDarkMode = isDarkMode,
                                onClick = { showAuthPrompt = true }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Brush.linearGradient(listOf(CrimsonPrimary, Color(0xFFFF5E7E))))
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Unlock with PIN / Biometrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Tab Selector: Hidden Notes | Folders | Change Lock
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = CrimsonPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = CrimsonPrimary
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Hidden Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Protected Folders", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Hidden Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Change Lock", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab 0: Hidden Notes List
                if (selectedTab == 0) {
                    if (hiddenNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No locked or hidden notes found.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Long-press any note on Home Screen and select 'Lock'.",
                                    color = CrimsonPrimary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(hiddenNotes) { note ->
                                var isUnlockingAnim by remember { mutableStateOf(false) }
                                val lockRotation by animateFloatAsState(
                                    targetValue = if (isUnlockingAnim) -35f else 0f,
                                    animationSpec = tween(350),
                                    label = "lockAnim"
                                )

                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    isDarkMode = isDarkMode,
                                    elevation = 3.dp,
                                    onClick = { onOpenNote(note) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isUnlockingAnim) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = CrimsonPrimary,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .rotate(lockRotation)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = note.title,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkMode) Color.White else Color(0xFF111111),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = note.content.take(60),
                                                fontSize = 12.sp,
                                                color = if (isDarkMode) Color.White.copy(0.6f) else Color.DarkGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Unhide / Unlock Button with smooth animation
                                            GlassCard(
                                                shape = RoundedCornerShape(10.dp),
                                                isDarkMode = isDarkMode,
                                                onClick = {
                                                    isUnlockingAnim = true
                                                    coroutineScope.launch {
                                                        kotlinx.coroutines.delay(250)
                                                        repository.toggleLock(note.id, true)
                                                        Toast.makeText(context, "Note Unlocked & Restored to ${note.folder}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_security_unlock),
                                                        contentDescription = "Unlock",
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Unhide",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4CAF50)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 1: Protected / Locked Folders
                else if (selectedTab == 1) {
                    val allFolders = listOf("All Notes", "Favorites", "APIs Keys", "Code", "Media", "Personal")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allFolders) { folder ->
                            val isFolderLocked = lockedFolders.contains(folder)
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                isDarkMode = isDarkMode,
                                elevation = 2.dp,
                                onClick = {
                                    preferences.toggleFolderLock(folder)
                                    Toast.makeText(
                                        context,
                                        if (!isFolderLocked) "Locked $folder" else "Unlocked $folder",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isFolderLocked) CrimsonPrimary else Color(0xFFFFCA28),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = folder,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkMode) Color.White else Color.Black
                                            )
                                            Text(
                                                text = if (isFolderLocked) "Protected with PIN" else "Normal folder",
                                                fontSize = 11.sp,
                                                color = if (isFolderLocked) CrimsonPrimary else Color.Gray
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isFolderLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (isFolderLocked) CrimsonPrimary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab: Hidden Folder — Item 3. Folders hidden from the main
                // screen via the folder long-press menu land here; their notes
                // stay untouched and reappear on the main screen when unhidden.
                else if (selectedTab == 2) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hidden Folders",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            IconButton(onClick = { showHideFolderPicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Hide a folder",
                                    tint = CrimsonPrimary
                                )
                            }
                        }

                        if (hiddenFolders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No hidden folders. Tap + to move a folder here.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(hiddenFolders.toList()) { folderName ->
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        isDarkMode = isDarkMode,
                                        elevation = 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_custom_folder),
                                                    contentDescription = null,
                                                    tint = CrimsonPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = folderName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isDarkMode) Color.White else Color.Black
                                                )
                                            }
                                            TextButton(onClick = {
                                                preferences.unhideFolder(folderName)
                                                Toast.makeText(context, "Folder restored to main screen", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Text("Unhide", color = CrimsonPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Picker: choose a currently-visible custom folder to hide
                    if (showHideFolderPicker) {
                        val hidableFolders = customFolders.filter { it !in hiddenFolders }
                        Dialog(onDismissRequest = { showHideFolderPicker = false }) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                isDarkMode = isDarkMode,
                                strong = true
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Hide a Folder",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color.White else Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (hidableFolders.isEmpty()) {
                                        Text(
                                            text = "No custom folders available. Create one from the main screen's \"Add Folder\" first.",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        hidableFolders.forEach { folderName ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        preferences.hideFolder(folderName)
                                                        showHideFolderPicker = false
                                                    }
                                                    .padding(vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_custom_folder),
                                                    contentDescription = null,
                                                    tint = CrimsonPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(folderName, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 2: Change Lock with Strict Existing PIN Guard (Fixes Part I Item 1)
                else if (selectedTab == 3) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (!isOldPinVerified) {
                            Text(
                                text = "Verify Existing Passcode First",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Text(
                                text = "You must enter your current 4-digit PIN before setting a new passcode.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = oldPinInput,
                                onValueChange = { if (it.length <= 4) oldPinInput = it },
                                label = { Text("Current 4-Digit PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    focusedLabelColor = CrimsonPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isDarkMode = isDarkMode,
                                onClick = {
                                    if (oldPinInput == storedPin) {
                                        isOldPinVerified = true
                                        Toast.makeText(context, "Old PIN verified! Enter new details below.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Incorrect current PIN!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(listOf(CrimsonPrimary, Color(0xFFFF5E7E))))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Verify & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Set New Security Credentials",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = { if (it.length <= 4) newPinInput = it },
                                label = { Text("New 4-Digit PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    focusedLabelColor = CrimsonPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newSecQuestionInput,
                                onValueChange = { newSecQuestionInput = it },
                                label = { Text("Security Recovery Question") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    focusedLabelColor = CrimsonPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newSecAnswerInput,
                                onValueChange = { newSecAnswerInput = it },
                                label = { Text("Security Answer") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    focusedLabelColor = CrimsonPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isDarkMode = isDarkMode,
                                onClick = {
                                    if (newPinInput.length == 4 && newSecAnswerInput.isNotBlank()) {
                                        preferences.setSecurityDetails(newPinInput, newSecQuestionInput, newSecAnswerInput)
                                        Toast.makeText(context, "Passcode & Security Question Updated!", Toast.LENGTH_SHORT).show()
                                        isOldPinVerified = false
                                        oldPinInput = ""
                                        newPinInput = ""
                                        newSecAnswerInput = ""
                                    } else {
                                        Toast.makeText(context, "Please enter 4 digits PIN and an answer.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Save New Security Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
