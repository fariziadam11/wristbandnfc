package com.gbs.wristbandnfc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbs.wristbandnfc.ui.screens.dashboard.DashboardScreen
import com.gbs.wristbandnfc.ui.screens.history.HistoryScreen
import com.gbs.wristbandnfc.ui.screens.history.TransactionHistoryScreen
import com.gbs.wristbandnfc.ui.screens.login.LoginScreen
import com.gbs.wristbandnfc.ui.screens.login.LoginViewModel
import com.gbs.wristbandnfc.ui.screens.payment.PaymentScreen
import com.gbs.wristbandnfc.ui.screens.payment.TopupScreen
import com.gbs.wristbandnfc.ui.screens.profile.ProfileScreen
import com.gbs.wristbandnfc.ui.screens.register.RegisterScreen
import com.gbs.wristbandnfc.ui.screens.register.RegisterWriteScreen
import com.gbs.wristbandnfc.ui.screens.scan.ScanScreen
import com.gbs.wristbandnfc.ui.screens.ticket.TicketScreen
import com.gbs.wristbandnfc.ui.screens.ticket.ValidateTicketScreen
import com.gbs.wristbandnfc.ui.screens.wallet.WalletScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LoginScreen(
                uiState = uiState,
                onLogin = viewModel::login,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToWallet = { uid, token ->
                    navController.navigate(Screen.Wallet.route + "?uid=$uid&token=$token")
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTicket = { navController.navigate(Screen.Ticket.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Scan.route
        ) {
            ScanScreen(
                onScanSuccess = { uid, token ->
                    navController.navigate(Screen.Wallet.route + "?uid=$uid&token=$token") {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Wallet.route + "?uid={uid}&token={token}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType; defaultValue = "" },
                navArgument("token") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""

            WalletScreen(
                uid = uid,
                token = token,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = {
                    navController.navigate(Screen.Payment.createRoute(uid, token))
                },
                onNavigateToTopup = {
                    navController.navigate(Screen.Topup.createRoute(uid, token))
                },
                onNavigateToValidateTicket = {
                    navController.navigate(Screen.ValidateTicket.createRoute(uid, token))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.TransactionHistory.route)
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""

            PaymentScreen(
                uid = uid,
                token = token,
                onPaymentSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Topup.route,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""

            TopupScreen(
                uid = uid,
                token = token,
                onTopupSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { uid ->
                    navController.navigate(Screen.RegisterWrite.createRoute(uid))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RegisterWrite.route,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            RegisterWriteScreen(
                uid = uid,
                onWriteSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Ticket.route) {
            TicketScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ValidateTicket.route,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""

            ValidateTicketScreen(
                uid = uid,
                token = token,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToTransactionHistory = { wristbandId ->
                    navController.navigate(Screen.TransactionHistory.createRoute(wristbandId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TransactionHistory.route,
            arguments = listOf(
                navArgument("wristbandId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val wristbandId = backStackEntry.arguments?.getString("wristbandId") ?: ""

            TransactionHistoryScreen(
                wristbandId = wristbandId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
