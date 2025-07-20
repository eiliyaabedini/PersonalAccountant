package ir.act.personalAccountant.data.repository

import android.util.Log
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.DeleteSheetRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridProperties
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.TextFormat
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.SyncProgress
import ir.act.personalAccountant.domain.model.SyncStep
import ir.act.personalAccountant.domain.repository.GoogleSheetsRepository
import ir.act.personalAccountant.domain.repository.SettingsRepository
import ir.act.personalAccountant.domain.repository.SyncStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsRepositoryImpl @Inject constructor(
    private val googleAuthRepository: GoogleAuthRepository,
    private val settingsRepository: SettingsRepository,
    private val syncStateRepository: SyncStateRepository
) : GoogleSheetsRepository {

    companion object {
        private const val TAG = "GoogleSheetsRepository"
        private const val SPREADSHEET_ID_KEY = "google_sheets_spreadsheet_id"
    }

    override suspend fun createPersonalAccountantSpreadsheet(
        title: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()
                ?: return@withContext Result.failure(Exception("User not signed in"))

            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Create spreadsheet
            val spreadsheet = Spreadsheet().apply {
                properties = SpreadsheetProperties().apply {
                    this.title = title
                }
            }

            val createdSpreadsheet = sheetsService.spreadsheets()
                .create(spreadsheet)
                .execute()

            val spreadsheetId = createdSpreadsheet.spreadsheetId
            Log.d(TAG, "Created spreadsheet with ID: $spreadsheetId")

            // Create initial monthly sheet for current month
            val currentMonth = getCurrentMonth()
            createMonthlySheet(spreadsheetId, currentMonth)

            // Delete the default "Sheet1" that Google creates automatically
            deleteDefaultSheet(spreadsheetId)

            // Save spreadsheet ID
            saveSpreadsheetId(spreadsheetId)

            Result.success(spreadsheetId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating spreadsheet", e)
            Result.failure(e)
        }
    }

    override suspend fun createMonthlySheet(
        spreadsheetId: String,
        monthYear: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()
                ?: return@withContext Result.failure(Exception("User not signed in"))

            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Check if sheet already exists
            if (sheetExists(spreadsheetId, monthYear)) {
                Log.d(TAG, "Sheet $monthYear already exists")
                return@withContext Result.success(Unit)
            }

            // Add new sheet
            val addSheetRequest = AddSheetRequest().apply {
                properties = SheetProperties().apply {
                    title = monthYear
                }
            }

            val batchUpdateRequest = BatchUpdateSpreadsheetRequest().apply {
                requests = listOf(
                    Request().apply {
                        this.addSheet = addSheetRequest
                    }
                )
            }

            sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute()

            Log.d(TAG, "Created monthly sheet: $monthYear")

            // Set headers
            setMonthlySheetHeaders(spreadsheetId, monthYear)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating monthly sheet", e)
            Result.failure(e)
        }
    }

    private suspend fun sheetExists(
        spreadsheetId: String,
        sheetTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount() ?: return@withContext false
            val sheetsService = googleAuthRepository.createSheetsService(account)

            val spreadsheet = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()

            val existingSheets = spreadsheet.sheets?.map { it.properties.title } ?: emptyList()
            return@withContext sheetTitle in existingSheets
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if sheet exists", e)
            false
        }
    }

    private suspend fun setMonthlySheetHeaders(
        spreadsheetId: String,
        sheetTitle: String
    ) = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()!!
            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Set header values
            val range = "$sheetTitle!A1:G1"
            val headers = ValueRange().apply {
                setValues(
                    listOf(
                        listOf(
                            "ID",
                            "Date",
                            "Amount",
                            "Tag",
                            "Image",
                            "Destination Amount",
                            "Destination Currency"
                        )
                    )
                )
            }

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, headers)
                .setValueInputOption("USER_ENTERED")
                .execute()

            // Format headers with table styling
            formatSheetAsTable(spreadsheetId, sheetTitle)

            Log.d(TAG, "Set headers and formatting for sheet: $sheetTitle")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting headers", e)
        }
    }

    private suspend fun formatSheetAsTable(
        spreadsheetId: String,
        sheetTitle: String
    ) = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()!!
            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Get sheet ID
            val spreadsheet = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()

            val sheet = spreadsheet.sheets?.find { it.properties?.title == sheetTitle }
            val sheetId = sheet?.properties?.sheetId ?: return@withContext

            // Create formatting requests
            val requests = mutableListOf<Request>()

            // 1. Format header row (row 1) with bold and background color
            requests.add(Request().apply {
                repeatCell = RepeatCellRequest().apply {
                    range = GridRange().apply {
                        this.sheetId = sheetId
                        startRowIndex = 0
                        endRowIndex = 1
                        startColumnIndex = 0
                        endColumnIndex = 7
                    }
                    cell = CellData().apply {
                        userEnteredFormat = CellFormat().apply {
                            backgroundColor = Color().apply {
                                red = 0.2f
                                green = 0.4f
                                blue = 0.8f
                                alpha = 1.0f
                            }
                            textFormat = TextFormat().apply {
                                bold = true
                                foregroundColor = Color().apply {
                                    red = 1.0f
                                    green = 1.0f
                                    blue = 1.0f
                                    alpha = 1.0f
                                }
                            }
                            horizontalAlignment = "CENTER"
                        }
                    }
                    fields = "userEnteredFormat(backgroundColor,textFormat,horizontalAlignment)"
                }
            })

            // 2. Auto-resize columns
            requests.add(Request().apply {
                autoResizeDimensions = AutoResizeDimensionsRequest().apply {
                    dimensions = DimensionRange().apply {
                        this.sheetId = sheetId
                        dimension = "COLUMNS"
                        startIndex = 0
                        endIndex = 7
                    }
                }
            })

            // 3. Freeze header row
            requests.add(Request().apply {
                updateSheetProperties = UpdateSheetPropertiesRequest().apply {
                    properties = SheetProperties().apply {
                        this.sheetId = sheetId
                        gridProperties = GridProperties().apply {
                            frozenRowCount = 1
                        }
                    }
                    fields = "gridProperties.frozenRowCount"
                }
            })

            // Execute all formatting requests
            if (requests.isNotEmpty()) {
                val batchUpdateRequest = BatchUpdateSpreadsheetRequest().apply {
                    this.requests = requests
                }

                sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error formatting sheet as table", e)
        }
    }

    private suspend fun deleteDefaultSheet(spreadsheetId: String) = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()!!
            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Get spreadsheet to find the default sheet ID
            val spreadsheet = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()

            // Find the sheet with title "Sheet1" (default sheet)
            val defaultSheet = spreadsheet.sheets?.find { sheet ->
                sheet.properties?.title == "Sheet1"
            }

            if (defaultSheet != null) {
                val sheetId = defaultSheet.properties.sheetId

                // Create delete sheet request
                val deleteSheetRequest = DeleteSheetRequest().apply {
                    this.sheetId = sheetId
                }

                val batchUpdateRequest = BatchUpdateSpreadsheetRequest().apply {
                    requests = listOf(
                        Request().apply {
                            this.deleteSheet = deleteSheetRequest
                        }
                    )
                }

                sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute()

                Log.d(TAG, "Deleted default Sheet1")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting default sheet", e)
            // Don't fail the whole process if we can't delete the default sheet
        }
    }

    override suspend fun syncExpenses(
        expenses: List<Expense>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()
                ?: return@withContext Result.failure(Exception("User not signed in"))

            val spreadsheetId = getSpreadsheetId()
                ?: return@withContext Result.failure(Exception("No spreadsheet configured"))

            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Group expenses by month
            val expensesByMonth = expenses.groupBy { expense ->
                getMonthFromTimestamp(expense.timestamp)
            }

            expensesByMonth.forEach { (monthYear, monthExpenses) ->
                // Ensure monthly sheet exists
                createMonthlySheet(spreadsheetId, monthYear)

                // Convert expenses to rows
                val rows = monthExpenses.map { expense ->
                    listOf(
                        expense.id.toString(),
                        formatDate(expense.timestamp),
                        expense.amount.toString(),
                        expense.tag,
                        expense.imagePath ?: "",
                        expense.destinationAmount?.toString() ?: "",
                        expense.destinationCurrency ?: ""
                    )
                }

                // Clear existing data and append new data
                clearSheetData(spreadsheetId, monthYear)

                // Append to sheet
                val range = "$monthYear!A2:G"
                val valueRange = ValueRange().apply {
                    setValues(rows)
                }

                sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute()

                Log.d(TAG, "Synced ${monthExpenses.size} expenses for $monthYear")
            }

            // Save the last sync timestamp
            syncStateRepository.saveLastSyncTimestamp(System.currentTimeMillis())

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing expenses", e)
            Result.failure(e)
        }
    }

    override suspend fun syncExpensesWithProgress(
        expenses: List<Expense>
    ): Flow<SyncProgress> = flow {
        try {
            emit(SyncProgress(currentStep = SyncStep.CONNECTING, currentItem = "Authenticating..."))

            val account = googleAuthRepository.getLastSignedInAccount()
                ?: throw Exception("User not signed in")

            val spreadsheetId = getSpreadsheetId()
                ?: throw Exception("No spreadsheet configured")

            val sheetsService = googleAuthRepository.createSheetsService(account)
            val driveService = googleAuthRepository.createDriveService(account)

            // Check if spreadsheet still exists
            emit(
                SyncProgress(
                    currentStep = SyncStep.CONNECTING,
                    currentItem = "Verifying spreadsheet exists..."
                )
            )

            val spreadsheetExists = try {
                sheetsService.spreadsheets().get(spreadsheetId).execute()
                true
            } catch (e: Exception) {
                Log.w(TAG, "Spreadsheet not found: $spreadsheetId", e)
                false
            }

            if (!spreadsheetExists) {
                // Clear the invalid spreadsheet ID and sync state
                clearSpreadsheetId()
                emit(
                    SyncProgress(
                        currentStep = SyncStep.ERROR,
                        currentItem = "Spreadsheet was deleted from Google Drive",
                        error = "Please reconnect to Google Sheets to create a new spreadsheet"
                    )
                )
                return@flow
            }

            emit(
                SyncProgress(
                    currentStep = SyncStep.FETCHING_EXPENSES,
                    currentItem = "Preparing expenses..."
                )
            )

            // Group expenses by month
            val expensesByMonth = expenses.groupBy { expense ->
                getMonthFromTimestamp(expense.timestamp)
            }

            val totalExpenses = expenses.size
            var completedExpenses = 0

            emit(
                SyncProgress(
                    totalItems = totalExpenses,
                    completedItems = completedExpenses,
                    currentStep = SyncStep.CREATING_MONTHLY_SHEETS,
                    currentItem = "Creating monthly sheets..."
                )
            )

            // Create monthly sheets
            expensesByMonth.keys.forEach { monthYear ->
                createMonthlySheet(spreadsheetId, monthYear)
                emit(
                    SyncProgress(
                        totalItems = totalExpenses,
                        completedItems = completedExpenses,
                        currentStep = SyncStep.CREATING_MONTHLY_SHEETS,
                        currentItem = "Created sheet for $monthYear"
                    )
                )
            }

            // Process expenses by month
            expensesByMonth.forEach { (monthYear, monthExpenses) ->
                emit(
                    SyncProgress(
                        totalItems = totalExpenses,
                        completedItems = completedExpenses,
                        currentStep = SyncStep.UPLOADING_IMAGES,
                        currentItem = "Checking existing data for $monthYear..."
                    )
                )

                // Get existing data from the actual Google Sheet
                val existingSheetData = getExistingSheetData(spreadsheetId, monthYear)
                val existingIds = existingSheetData.map { it["ID"] }.toSet()

                emit(
                    SyncProgress(
                        totalItems = totalExpenses,
                        completedItems = completedExpenses,
                        currentStep = SyncStep.UPLOADING_IMAGES,
                        currentItem = "Found ${existingIds.size} existing records in $monthYear..."
                    )
                )

                // Smart sync: only process expenses that are missing from the sheet or have changed
                val processedExpenses = monthExpenses.map { expense ->
                    val expenseIdStr = expense.id.toString()
                    val existingRecord = existingSheetData.find { it["ID"] == expenseIdStr }

                    // Check if expense exists in sheet and if data matches
                    val needsSync = existingRecord == null ||
                            existingRecord["Date"] != formatDate(expense.timestamp) ||
                            existingRecord["Amount"] != expense.amount.toString() ||
                            existingRecord["Tag"] != expense.tag ||
                            existingRecord["Destination Amount"] != (expense.destinationAmount?.toString()
                        ?: "") ||
                            existingRecord["Destination Currency"] != (expense.destinationCurrency
                        ?: "")

                    if (needsSync) {
                        emit(
                            SyncProgress(
                                totalItems = totalExpenses,
                                completedItems = completedExpenses,
                                currentStep = SyncStep.UPLOADING_IMAGES,
                                currentItem = "Processing expense ${expense.id}..."
                            )
                        )

                        // Handle image upload if needed
                        val updatedExpense = if (expense.imagePath != null &&
                            expense.imagePath.startsWith("/") &&
                            java.io.File(expense.imagePath).exists()
                        ) {

                            // Check if image is already in the sheet
                            val existingImageUrl = existingRecord?.get("Image")
                            val hasExistingImage = !existingImageUrl.isNullOrBlank() &&
                                    existingImageUrl.startsWith("https://drive.google.com")

                            if (!hasExistingImage) {
                                try {
                                    emit(
                                        SyncProgress(
                                            totalItems = totalExpenses,
                                            completedItems = completedExpenses,
                                            currentStep = SyncStep.UPLOADING_IMAGES,
                                            currentItem = "Uploading image for expense ${expense.id}..."
                                        )
                                    )

                                    val imageFile = java.io.File(expense.imagePath)
                                    val imageBytes = imageFile.readBytes()
                                    val fileName =
                                        "expense_${expense.id}_${System.currentTimeMillis()}.jpg"
                                    val uploadResult = uploadImageToDrive(imageBytes, fileName)

                                    if (uploadResult.isSuccess) {
                                        val imageUrl = uploadResult.getOrNull()!!
                                        expense.copy(imagePath = imageUrl)
                                    } else {
                                        expense
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error uploading image for expense ${expense.id}", e)
                                    expense
                                }
                            } else {
                                // Use existing image URL from sheet
                                expense.copy(imagePath = existingImageUrl)
                            }
                        } else {
                            expense
                        }

                        updatedExpense
                    } else {
                        // Expense already exists in sheet and hasn't changed
                        emit(
                            SyncProgress(
                                totalItems = totalExpenses,
                                completedItems = completedExpenses,
                                currentStep = SyncStep.UPLOADING_IMAGES,
                                currentItem = "Skipping unchanged expense ${expense.id}..."
                            )
                        )
                        // Use existing image URL from sheet if available
                        val existingImageUrl = existingRecord?.get("Image")
                        expense.copy(imagePath = existingImageUrl)
                    }
                }

                emit(
                    SyncProgress(
                        totalItems = totalExpenses,
                        completedItems = completedExpenses,
                        currentStep = SyncStep.SYNCING_EXPENSES,
                        currentItem = "Syncing expenses for $monthYear..."
                    )
                )

                // Convert expenses to rows
                val rows = processedExpenses.map { expense ->
                    listOf(
                        expense.id.toString(),
                        formatDate(expense.timestamp),
                        expense.amount.toString(),
                        expense.tag,
                        expense.imagePath ?: "",
                        expense.destinationAmount?.toString() ?: "",
                        expense.destinationCurrency ?: ""
                    )
                }

                // Only update sheet if there are new/changed expenses
                val hasChanges = processedExpenses.any { expense ->
                    val expenseIdStr = expense.id.toString()
                    val existingRecord = existingSheetData.find { it["ID"] == expenseIdStr }

                    // Check if expense exists in sheet and if data matches
                    existingRecord == null ||
                            existingRecord["Date"] != formatDate(expense.timestamp) ||
                            existingRecord["Amount"] != expense.amount.toString() ||
                            existingRecord["Tag"] != expense.tag ||
                            existingRecord["Image"] != (expense.imagePath ?: "") ||
                            existingRecord["Destination Amount"] != (expense.destinationAmount?.toString()
                        ?: "") ||
                            existingRecord["Destination Currency"] != (expense.destinationCurrency
                        ?: "")
                }

                if (hasChanges) {
                    emit(
                        SyncProgress(
                            totalItems = totalExpenses,
                            completedItems = completedExpenses,
                            currentStep = SyncStep.SYNCING_EXPENSES,
                            currentItem = "Updating sheet for $monthYear..."
                        )
                    )

                    // Clear existing data and append new data
                    clearSheetData(spreadsheetId, monthYear)

                    // Append to sheet
                    val range = "$monthYear!A2:G"
                    val valueRange = ValueRange().apply {
                        setValues(rows)
                    }

                    sheetsService.spreadsheets().values()
                        .update(spreadsheetId, range, valueRange)
                        .setValueInputOption("USER_ENTERED")
                        .execute()

                    Log.d(
                        TAG,
                        "Updated sheet for $monthYear with ${processedExpenses.size} expenses"
                    )
                } else {
                    emit(
                        SyncProgress(
                            totalItems = totalExpenses,
                            completedItems = completedExpenses,
                            currentStep = SyncStep.SYNCING_EXPENSES,
                            currentItem = "No changes detected for $monthYear, skipping update..."
                        )
                    )
                }

                // Update progress for each expense in this month
                processedExpenses.forEach { expense ->
                    completedExpenses++
                    emit(
                        SyncProgress(
                            totalItems = totalExpenses,
                            completedItems = completedExpenses,
                            currentStep = SyncStep.SYNCING_EXPENSES,
                            currentItem = "Processed expense: ${expense.tag} - ${expense.amount}"
                        )
                    )
                }

                Log.d(TAG, "Synced ${processedExpenses.size} expenses for $monthYear")
            }

            // Sync completed - all data is now verified against actual Google Sheets content
            // Save the last sync timestamp
            syncStateRepository.saveLastSyncTimestamp(System.currentTimeMillis())

            emit(
                SyncProgress(
                    totalItems = totalExpenses,
                    completedItems = completedExpenses,
                    currentStep = SyncStep.COMPLETED,
                    currentItem = "Sync completed successfully!"
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing expenses with progress", e)
            emit(
                SyncProgress(
                    currentStep = SyncStep.ERROR,
                    currentItem = "Sync failed",
                    error = e.message
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun clearSheetData(
        spreadsheetId: String,
        sheetTitle: String
    ) = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()!!
            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Clear data starting from row 2 (keep headers)
            val range = "$sheetTitle!A2:G1000"
            val valueRange = ValueRange().apply {
                setValues(listOf(listOf("", "", "", "", "", "", "")))
            }

            sheetsService.spreadsheets().values()
                .clear(spreadsheetId, range, ClearValuesRequest())
                .execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing sheet data", e)
        }
    }

    override suspend fun uploadImageToDrive(
        imageBytes: ByteArray,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()
                ?: return@withContext Result.failure(Exception("User not signed in"))

            val driveService = googleAuthRepository.createDriveService(account)

            // Extract month from filename (expect format: expense_id_timestamp.jpg)
            val monthYear = try {
                val timestamp =
                    fileName.split("_").getOrNull(2)?.split(".")?.firstOrNull()?.toLong()
                        ?: System.currentTimeMillis()
                getMonthFromTimestamp(timestamp)
            } catch (e: Exception) {
                getCurrentMonth()
            }

            // Create or get PersonalAccountant folder
            val personalAccountantFolderId =
                getOrCreateFolder(driveService, "PersonalAccountant", null)

            // Create or get month folder inside PersonalAccountant
            val monthFolderId = getOrCreateFolder(
                driveService,
                monthYear.replace("-", "_"),
                personalAccountantFolderId
            )

            // Create file metadata with parent folder
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                mimeType = "image/jpeg"
                parents = listOf(monthFolderId)
            }

            // Upload file
            val mediaContent = ByteArrayContent("image/jpeg", imageBytes)

            val file = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            // Make file publicly viewable
            val permission = Permission().apply {
                type = "anyone"
                role = "reader"
            }

            driveService.permissions()
                .create(file.id, permission)
                .execute()

            val imageUrl = "https://drive.google.com/uc?id=${file.id}"
            Log.d(TAG, "Uploaded image to $monthYear folder: $imageUrl")
            Result.success(imageUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateFolder(
        driveService: com.google.api.services.drive.Drive,
        folderName: String,
        parentFolderId: String?
    ): String = withContext(Dispatchers.IO) {
        try {
            // Search for existing folder
            val query = if (parentFolderId != null) {
                "name='$folderName' and mimeType='application/vnd.google-apps.folder' and '$parentFolderId' in parents and trashed=false"
            } else {
                "name='$folderName' and mimeType='application/vnd.google-apps.folder' and trashed=false"
            }

            val result = driveService.files()
                .list()
                .setQ(query)
                .setSpaces("drive")
                .execute()

            // If folder exists, return its ID
            if (result.files.isNotEmpty()) {
                val folderId = result.files[0].id
                Log.d(TAG, "Found existing folder: $folderName with ID: $folderId")
                return@withContext folderId
            }

            // Create new folder
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                if (parentFolderId != null) {
                    parents = listOf(parentFolderId)
                }
            }

            val folder = driveService.files()
                .create(folderMetadata)
                .setFields("id")
                .execute()

            Log.d(TAG, "Created new folder: $folderName with ID: ${folder.id}")
            folder.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/getting folder: $folderName", e)
            throw e
        }
    }

    override suspend fun isConnected(): Boolean {
        return try {
            val isSignedIn = googleAuthRepository.isSignedIn()
            val spreadsheetId = getSpreadsheetId()
            val hasSpreadsheetId = !spreadsheetId.isNullOrEmpty()

            // Basic checks: signed in and has spreadsheet ID
            if (isSignedIn && hasSpreadsheetId) {
                val account = googleAuthRepository.getLastSignedInAccount()
                if (account != null && account.email != null) {
                    // For faster response, we'll do a lightweight check
                    // The spreadsheet existence will be verified during actual sync operations
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connection status", e)
            // On error, fall back to basic checks
            try {
                googleAuthRepository.isSignedIn() && !getSpreadsheetId().isNullOrEmpty()
            } catch (fallbackError: Exception) {
                false
            }
        }
    }

    override suspend fun getSpreadsheetId(): String? {
        return settingsRepository.getStringSetting(SPREADSHEET_ID_KEY)
    }

    override suspend fun saveSpreadsheetId(spreadsheetId: String) {
        settingsRepository.saveStringSetting(SPREADSHEET_ID_KEY, spreadsheetId)
    }

    private suspend fun clearSpreadsheetId() {
        settingsRepository.saveStringSetting(SPREADSHEET_ID_KEY, "")
    }

    override suspend fun clearSyncState(expenseId: Long) {
        syncStateRepository.clearSyncState(expenseId)
    }

    override suspend fun getLastSyncTimestamp(): Long? {
        return syncStateRepository.getLastSyncTimestamp()
    }

    private suspend fun getExistingSheetData(
        spreadsheetId: String,
        sheetTitle: String
    ): List<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthRepository.getLastSignedInAccount()
                ?: return@withContext emptyList()

            val sheetsService = googleAuthRepository.createSheetsService(account)

            // Check if spreadsheet exists first
            val spreadsheet = try {
                sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .execute()
            } catch (e: Exception) {
                Log.w(TAG, "Spreadsheet not found: $spreadsheetId", e)
                return@withContext emptyList()
            }

            // Check if the specific sheet exists
            val sheetExists = spreadsheet.sheets?.any { it.properties?.title == sheetTitle } == true
            if (!sheetExists) {
                Log.d(TAG, "Sheet $sheetTitle does not exist in spreadsheet")
                return@withContext emptyList()
            }

            // Get the data from the sheet
            val range = "$sheetTitle!A1:G1000" // Get up to 1000 rows
            val response = try {
                sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read sheet data: $sheetTitle", e)
                return@withContext emptyList()
            }

            val values = response.getValues() ?: return@withContext emptyList()

            if (values.isEmpty()) {
                return@withContext emptyList()
            }

            // First row should be headers: ID, Date, Amount, Tag, Image, Destination Amount, Destination Currency
            val headers = values[0]?.map { it.toString() } ?: return@withContext emptyList()

            // Convert remaining rows to maps
            val dataRows = values.drop(1) // Skip header row
            val result = mutableListOf<Map<String, String>>()

            for (row in dataRows) {
                if (row.isNotEmpty()) {
                    val rowMap = mutableMapOf<String, String>()
                    headers.forEachIndexed { index, header ->
                        val value = if (index < row.size) row[index]?.toString() ?: "" else ""
                        rowMap[header] = value
                    }
                    // Only include rows that have an ID (first column)
                    if (rowMap["ID"]?.isNotBlank() == true) {
                        result.add(rowMap)
                    }
                }
            }

            Log.d(TAG, "Found ${result.size} existing records in sheet $sheetTitle")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Error reading existing sheet data", e)
            emptyList()
        }
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "%04d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1
        )
    }

    private fun getMonthFromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return String.format(
            "%04d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1
        )
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}