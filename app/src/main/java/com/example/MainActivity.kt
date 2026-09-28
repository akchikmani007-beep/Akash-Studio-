package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BillDetailScreen
import com.example.ui.screens.CompaniesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NewBillScreen
import com.example.ui.screens.PhotographerDetailScreen
import com.example.ui.screens.PhotographersScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BillingViewModel

sealed class Screen {
    data object Dashboard : Screen()
    data object Companies : Screen()
    data object Photographers : Screen()
    data object NewBill : Screen()
    data object Reminders : Screen()
    data object Reports : Screen()
    data class PhotographerDetail(val photographerId: Long) : Screen()
    data class BillDetail(val billId: Long) : Screen()
}

enum class MainTab(val title: String, val icon: ImageVector, val screen: Screen) {
    DASHBOARD("Home", Icons.Default.Dashboard, Screen.Dashboard),
    COMPANIES("Studios", Icons.Default.Business, Screen.Companies),
    PHOTOGRAPHERS("Photographers", Icons.Default.CameraAlt, Screen.Photographers),
    NEW_BILL("New Bill", Icons.Default.ReceiptLong, Screen.NewBill),
    REMINDERS("Reminders", Icons.Default.Notifications, Screen.Reminders),
    REPORTS("Reports", Icons.Default.Assessment, Screen.Reports)
}

class MainActivity : ComponentActivity() {

    private val viewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudioApp(viewModel: BillingViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }

    val currentScreen = screenStack.lastOrNull() ?: Screen.Dashboard
    val pendingReminders by viewModel.pendingReminders.collectAsState()

    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
        } else if (currentTab != MainTab.DASHBOARD) {
            currentTab = MainTab.DASHBOARD
            screenStack.clear()
            screenStack.add(Screen.Dashboard)
        }
    }

    fun switchTab(tab: MainTab) {
        currentTab = tab
        screenStack.clear()
        screenStack.add(tab.screen)
    }

    BackHandler(enabled = screenStack.size > 1 || currentTab != MainTab.DASHBOARD) {
        navigateBack()
    }

    val isDetailScreen = currentScreen is Screen.PhotographerDetail || currentScreen is Screen.BillDetail

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isDetailScreen) {
                NavigationBar(modifier = Modifier.testTag("main_navigation_bar")) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab && screenStack.size == 1
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { switchTab(tab) },
                            icon = {
                                if (tab == MainTab.REMINDERS && pendingReminders.isNotEmpty()) {
                                    BadgedBox(badge = {
                                        Badge { Text("${pendingReminders.size}") }
                                    }) {
                                        Icon(imageVector = tab.icon, contentDescription = tab.title)
                                    }
                                } else {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToNewBill = { switchTab(MainTab.NEW_BILL) },
                    onNavigateToCompanies = { switchTab(MainTab.COMPANIES) },
                    onNavigateToReminders = { switchTab(MainTab.REMINDERS) },
                    onBillClick = { billId -> navigateTo(Screen.BillDetail(billId)) },
                    onSeeAllBills = { switchTab(MainTab.PHOTOGRAPHERS) }
                )
                is Screen.Companies -> CompaniesScreen(
                    viewModel = viewModel,
                    onViewCompanyBills = { companyId ->
                        viewModel.setCompanyFilter(companyId)
                        switchTab(MainTab.DASHBOARD)
                    }
                )
                is Screen.Photographers -> PhotographersScreen(
                    viewModel = viewModel,
                    onPhotographerClick = { photogId ->
                        navigateTo(Screen.PhotographerDetail(photogId))
                    }
                )
                is Screen.NewBill -> NewBillScreen(
                    viewModel = viewModel,
                    onBillCreated = { newBillId ->
                        navigateTo(Screen.BillDetail(newBillId))
                    }
                )
                is Screen.Reminders -> RemindersScreen(
                    viewModel = viewModel,
                    onBillClick = { billId -> navigateTo(Screen.BillDetail(billId)) }
                )
                is Screen.Reports -> ReportsScreen(
                    viewModel = viewModel
                )
                is Screen.PhotographerDetail -> PhotographerDetailScreen(
                    photographerId = screen.photographerId,
                    viewModel = viewModel,
                    onBack = { navigateBack() },
                    onBillClick = { billId -> navigateTo(Screen.BillDetail(billId)) },
                    onNewBillForPhotog = { photogId ->
                        viewModel.updateSelectedPhotographer(photogId)
                        switchTab(MainTab.NEW_BILL)
                    }
                )
                is Screen.BillDetail -> BillDetailScreen(
                    billId = screen.billId,
                    viewModel = viewModel,
                    onBack = { navigateBack() }
                )
            }
        }
    }
}
