package com.example.proyectoZapateria.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyectoZapateria.ui.components.AppDrawer
import com.example.proyectoZapateria.ui.components.AppTopBar
import com.example.proyectoZapateria.ui.components.defaultDrawerItems
import com.example.proyectoZapateria.ui.screen.HomeScreen
import com.example.proyectoZapateria.ui.screen.LoginScreenVm
import com.example.proyectoZapateria.ui.screen.RegisterScreenVm
import com.example.proyectoZapateria.ui.screen.admin.AdminHomeScreen
import com.example.proyectoZapateria.ui.screen.transportista.TransportistaHomeScreen
import com.example.proyectoZapateria.ui.screen.vendedor.VendedorHomeScreen
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Helpers de navegación
    val goHome: () -> Unit = {
        // Si hay usuario autenticado, verificar si ya está en su home
        if (currentUser != null) {
            val destination = when(currentUser?.idRol) {
                1 -> Route.AdminHome.path
                2 -> Route.VendedorHome.path
                3 -> Route.TransportistaHome.path
                else -> Route.Home.path
            }

            // Verificar si ya está en la ruta de destino
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == destination) {
                // Ya está en home, mostrar mensaje
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ya estás en la pantalla principal",
                        duration = SnackbarDuration.Short
                    )
                }
            } else {
                // Navegar a su home
                navController.navigate(destination) {
                    popUpTo(Route.Home.path) { inclusive = true }
                }
            }
        } else {
            // Usuario no autenticado
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Route.Home.path) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ya estás en la pantalla principal",
                        duration = SnackbarDuration.Short
                    )
                }
            } else {
                navController.navigate(Route.Home.path)
            }
        }
    }

    val goLogin: () -> Unit = {
        // Si ya está autenticado, mostrar mensaje
        if (currentUser != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Ya tienes una sesión activa",
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            navController.navigate(Route.Login.path)
        }
    }

    val goRegister: () -> Unit = {
        // Si ya está autenticado, mostrar mensaje
        if (currentUser != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Ya tienes una sesión activa",
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            navController.navigate(Route.Register.path)
        }
    }

    // Redirigir según rol
    val redirectByRole: (String) -> Unit = { role ->
        val destination = when (role) {
            "Administrador" -> Route.AdminHome.path
            "Vendedor" -> Route.VendedorHome.path
            "Transportista" -> Route.TransportistaHome.path
            else -> Route.Home.path
        }
        navController.navigate(destination) {
            popUpTo(Route.Login.path) { inclusive = true }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null,
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        goLogin()
                    },
                    onRegister = {
                        scope.launch { drawerState.close() }
                        goRegister()
                    },
                    isAuthenticated = currentUser != null
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onLogin = goLogin,
                    onRegister = goRegister,
                    isAuthenticated = currentUser != null
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Rutas publicas
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        authViewModel = authViewModel,
                        onLoginSuccess = redirectByRole,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        authViewModel = authViewModel,
                        onRegisterOkGoLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                // Rutas del vendedor
                composable(Route.VendedorHome.path) {
                    VendedorHomeScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                // Rutas del administrador
                composable(Route.AdminHome.path) {
                    AdminHomeScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                // Rutas del transportista
                composable(Route.TransportistaHome.path) {
                    TransportistaHomeScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
                // pantallas pendientes de implementar
                composable(Route.VendedorVentas.path) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Pantalla de Ventas - Próximamente")
                    }
                }

                composable(Route.VendedorClientes.path) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Pantalla de Clientes - Próximamente")
                    }
                }

                composable(Route.VendedorInventario.path) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Pantalla de Inventario - Próximamente")
                    }
                }

                composable(Route.VendedorPerfil.path) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Pantalla de Perfil - Próximamente")
                    }
                }
            }
        }
    }
}
