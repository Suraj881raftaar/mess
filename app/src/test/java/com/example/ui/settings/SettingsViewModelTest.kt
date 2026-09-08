package com.example.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.repository.BackupRepository
import com.example.data.repository.DataManagementRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var settingsRepository: SettingsRepository
  private lateinit var backupRepository: BackupRepository
  private lateinit var dataManagementRepository: DataManagementRepository
  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    settingsRepository = SettingsRepository(db.messSettingDao())
    backupRepository = BackupRepository(context, db)
    dataManagementRepository = DataManagementRepository(db)

    viewModel = SettingsViewModel(
      settingsRepository,
      backupRepository,
      dataManagementRepository
    )
  }

  @After
  fun teardown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun testProfileConfiguration() = runBlocking {
    viewModel.updateMessProfileSync("Corporate Mess", "Suresh Kumar", "9876543210")

    val state = viewModel.uiState.first { it.messName == "Corporate Mess" }
    assertEquals("Corporate Mess", state.messName)
    assertEquals("Suresh Kumar", state.managerName)
    assertEquals("9876543210", state.managerPhone)
  }

  @Test
  fun testReminderToggle() = runBlocking {
    viewModel.toggleRemindAttendanceSync(false)
    val state = viewModel.uiState.first { !it.remindAttendance }
    assertFalse(state.remindAttendance)

    viewModel.toggleRemindMenuSync(false)
    val state2 = viewModel.uiState.first { !it.remindMenu }
    assertFalse(state2.remindMenu)
  }

  @Test
  fun testThemeModeUpdate() = runBlocking {
    viewModel.updateThemeModeSync("DARK")
    val state = viewModel.uiState.first { it.themeMode == "DARK" }
    assertEquals("DARK", state.themeMode)
  }
}
