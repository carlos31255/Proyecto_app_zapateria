package com.example.proyectoZapateria.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectoZapateria.ui.components.AppDrawer
import com.example.proyectoZapateria.ui.components.AppTopBar
import com.example.proyectoZapateria.ui.components.defaultDrawerItems
import com.example.proyectoZapateria.ui.screen.HomeScreen
import com.example.proyectoZapateria.ui.screen.LoginScreenVm
import com.example.proyectoZapateria.ui.screen.RegisterScreenVm
import com.example.proyectoZapateria.ui.screen.admin.AdminHomeScreen

import com.example.proyectoZapateria.ui.screen.admin.AdminAgregarProductoScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminInventarioScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClienteHomeScreen
import com.example.proyectoZapateria.ui.screen.transportista.TransportistaEntregasScreen

import com.example.proyectoZapateria.ui.screen.transportista.TransportistaHomeScreen
import com.example.proyectoZapateria.ui.screen.vendedor.VendedorHomeScreen
import com.example.proyectoZapateria.ui.screen.transportista.DetalleEntregaScreen
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch


@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val authViewModel: com.example.proyectoZapateria.viewmodel.AuthViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Helpers de navegación
    val goHome: () -> Unit = {
        // Si hay usuario autenticado, verificar si ya está en su home
        if (currentUser != null) {
            val destination = when(currentUser?.idRol) {
                1 -> Route.AdminHome.path
                2 -> Route.VendedorHome.path
                3 -> Route.TransportistaHome.path
                4 -> Route.ClienteHome.path
                else -> Route.Home.path
            }

            // Verificar si ya está en la ruta de destino
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == destination) {
                // Ya está en home, mostrar mensaje
                Toast.makeText(context, "Ya estás en la pantalla principal", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Ya estás en la pantalla principal", Toast.LENGTH_SHORT).show()
            } else {
                navController.navigate(Route.Home.path)
            }
        }
    }

    val goLogin: () -> Unit = {
        // Si ya está autenticado, mostrar mensaje
        if (currentUser != null) {
            Toast.makeText(context, "Ya tienes una sesión activa", Toast.LENGTH_SHORT).show()
        } else {
            navController.navigate(Route.Login.path)
        }
    }

    val goRegister: () -> Unit = {
        // Si ya está autenticado, mostrar mensaje
        if (currentUser != null) {
            Toast.makeText(context, "Ya tienes una sesión activa", Toast.LENGTH_SHORT).show()
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
            "Cliente" -> Route.ClienteHome.path
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
            }
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

                composable(Route.AdminAgregarProducto.path) {
                    AdminAgregarProductoScreen(
                        navController = navController
                    )
                }

                composable(Route.AdminInventario.path) {
                    AdminInventarioScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }



                // Rutas del cliente
                composable(Route.ClienteHome.path) {
                    ClienteHomeScreen(
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
                composable(
                    route = Route.TransportistaEntregas.path
                ) { backStackEntry ->
                    TransportistaEntregasScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        backStackEntry = backStackEntry
                    )
                }

                composable(
                    // Esta es la ruta base: "transportista/entregas/detalle/{idEntrega}"
                    route = Route.TransportistaEntregaDetalle.path,

                    // Define el argumento que esperamos
                    arguments = listOf(navArgument("idEntrega") { type = NavType.IntType })
                ) {
                    // Llama a la pantalla de detalle real.
                    // El DetalleEntregaViewModel (inyectado con hiltViewModel())
                    // leerá automáticamente el "idEntrega" de la ruta.
                    DetalleEntregaScreen(navController = navController)
                }
                }
            }
        }
    }
