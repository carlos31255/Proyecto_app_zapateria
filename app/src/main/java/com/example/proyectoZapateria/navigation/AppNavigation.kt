package com.example.proyectoZapateria.navigation


import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navArgument
import com.example.proyectoZapateria.ui.components.AppDrawer
import com.example.proyectoZapateria.ui.components.AppTopBar
import com.example.proyectoZapateria.ui.components.publicDrawerItems
import com.example.proyectoZapateria.ui.screen.HomeScreen
import com.example.proyectoZapateria.ui.screen.LoginScreenVm
import com.example.proyectoZapateria.ui.screen.RegisterScreenVm
import com.example.proyectoZapateria.ui.screen.admin.AdminHomeScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminAgregarProductoScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminInventarioScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminClientesScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminUsuariosScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminPerfilScreen
import com.example.proyectoZapateria.ui.screen.admin.AdminVentasScreen
import com.example.proyectoZapateria.ui.screen.admin.VentaDetalleScreen
import com.example.proyectoZapateria.ui.screen.admin.ClienteDetalleScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClienteHomeScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClienteCatalogoScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClienteProductoDetailScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClienteCartScreen
import com.example.proyectoZapateria.ui.screen.transportista.TransportistaEntregasScreen
import com.example.proyectoZapateria.ui.screen.transportista.ConfirmarEntregaScreen
import com.example.proyectoZapateria.ui.screen.transportista.TransportistaHomeScreen
import com.example.proyectoZapateria.ui.screen.transportista.TransportistaPerfilScreen
import com.example.proyectoZapateria.ui.components.AuthenticatedDrawerHeader
import com.example.proyectoZapateria.ui.components.PublicDrawerHeader
import com.example.proyectoZapateria.ui.screen.transportista.transportistaDrawerItems
import com.example.proyectoZapateria.ui.screen.cliente.clienteDrawerItems
import com.example.proyectoZapateria.ui.screen.admin.adminDrawerItems
import com.example.proyectoZapateria.ui.screen.cliente.ClientePerfilScreen
import com.example.proyectoZapateria.ui.screen.cliente.ClientePedidosScreen
import kotlinx.coroutines.launch


@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val authViewModel: com.example.proyectoZapateria.viewmodel.AuthViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isRestoring by authViewModel.isRestoringSession.collectAsStateWithLifecycle()
    val startupError by authViewModel.startupError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Mostrar error global de inicio (por ejemplo: microservicio inaccesible)
    LaunchedEffect(startupError) {
        if (!startupError.isNullOrBlank()) {
            Toast.makeText(context, startupError, Toast.LENGTH_LONG).show()
            authViewModel.clearStartupError()
        }
    }


    // Redireccionar automáticamente si hay sesión guardada
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val isOnAuthScreen = currentRoute == Route.Login.path ||
                                currentRoute == Route.Register.path ||
                                currentRoute == Route.Home.path

            if (isOnAuthScreen) {
                // Redireccionar al home correspondiente según el rol
                val destination = when(currentUser?.idRol) {
                    1L -> Route.AdminHome.path       // Administrador
                    2L -> Route.TransportistaHome.path // Transportista
                    3L -> Route.ClienteHome.path      // Cliente
                    else -> Route.Home.path
                }

                navController.navigate(destination) {
                    popUpTo(Route.Home.path) { inclusive = true }
                }
            }
        }
    }

    // Helpers de navegación
    val goHome: () -> Unit = {
        // Si hay usuario autenticado, verificar si ya está en su home
        if (currentUser != null) {
            val destination = when(currentUser?.idRol) {
                1L -> Route.AdminHome.path       // Administrador
                2L -> Route.TransportistaHome.path // Transportista
                3L -> Route.ClienteHome.path      // Cliente
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
            val currentRoute by navController.currentBackStackEntryAsState()
            val routeString = currentRoute?.destination?.route ?: Route.Home.path


            // Decidimos qué menú mostrar
            when (currentUser?.nombreRol) {
                // --- CASO 1: ADMINISTRADOR ---
                "Administrador" -> {
                    AppDrawer(
                        currentRoute = routeString,
                        items = adminDrawerItems(
                            onAgregarProducto = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminAgregarProducto.path)
                            },
                            onVentas = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminVentas.path)
                            },
                            onClientes = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminClientes.path)
                            },
                            onInventario = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminInventario.path)
                            },
                            onUsuarios = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminUsuarios.path)
                            },
                            onReportes = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminReportes.path)
                            },
                            onPerfil = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.AdminPerfil.path)
                            },
                            onLogout = {
                                scope.launch { drawerState.close() }
                                authViewModel.logout()
                                navController.navigate(Route.Login.path) { popUpTo(0) { inclusive = true } }
                            }
                        ),
                        header = {
                            AuthenticatedDrawerHeader(
                                username = currentUser?.nombre ?: "Usuario",
                                role = currentUser?.nombreRol ?: "Administrador"
                            )
                        }
                    )
                }
                // --- CASO 2: CLIENTE ---
                "Cliente" -> {
                    AppDrawer(
                        currentRoute = routeString,
                        items = clienteDrawerItems(
                            onCatalogo = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.ClienteCatalogo.path)
                            },
                            onPedidos = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.ClientePedidos.path)
                            },
                            onPerfil = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.ClientePerfil.path)
                            },
                            onLogout = {
                                scope.launch { drawerState.close() }
                                authViewModel.logout()
                                navController.navigate(Route.Login.path) { popUpTo(0) { inclusive = true } }
                            },
                            onCart = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Route.ClienteCart.path)
                            }
                        ),
                        header = {
                            AuthenticatedDrawerHeader(
                                username = currentUser?.nombre ?: "Usuario",
                                role = currentUser?.nombreRol ?: "Cliente"
                            )
                        }
                    )
                }
                // --- CASO 4: TRANSPORTISTA ---
                "Transportista" -> {
                     AppDrawer(
                         currentRoute = routeString,
                         items = transportistaDrawerItems(
                             onEntregas = {
                                 scope.launch { drawerState.close() }
                                 navController.navigate(Route.TransportistaListaEntregas.path)
                             },
                             onPerfil = {
                                 scope.launch { drawerState.close() }
                                 navController.navigate(Route.TransportistaPerfil.path)
                             },
                             onLogout = {
                                 scope.launch { drawerState.close() }
                                 authViewModel.logout()
                                 navController.navigate(Route.Login.path) {
                                     popUpTo(0) { inclusive = true }
                                 }
                             }
                         ),
                         header = {
                             AuthenticatedDrawerHeader(
                                 username = currentUser?.nombre ?: "Usuario",
                                 role = currentUser?.nombreRol ?: "Transportista"
                             )
                         }
                     )
                 }
                else -> {
                    AppDrawer(
                        currentRoute = routeString,
                        items = publicDrawerItems(
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
                        ),
                        header = {
                            PublicDrawerHeader()
                        }
                    )
                }
            }
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
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Route.Home.path,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
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

                    composable(Route.AdminClientes.path) {
                        AdminClientesScreen(
                            navController = navController
                        )
                    }

                    composable(Route.AdminUsuarios.path) {
                        AdminUsuariosScreen(
                            navController = navController
                        )
                    }

                    composable(Route.AdminReportes.path,
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        com.example.proyectoZapateria.ui.screen.admin.ReportesScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    composable(Route.AdminPerfil.path) {
                        AdminPerfilScreen(
                            navController = navController
                        )
                    }

//                composable(Route.AdminVentas.path) {
//                    AdminVentasScreen(
//                        navController = navController
//                    )
//                }

                    composable(
                        route = Route.VentaDetalle.path,
                        arguments = listOf(navArgument("idBoleta") { type = NavType.LongType })
                    ) { backStackEntry ->
                        VentaDetalleScreen(
                            navController = navController
                        )
                    }

                    composable(
                        route = Route.ClienteDetalle.path,
                        arguments = listOf(navArgument("idCliente") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val idCliente = backStackEntry.arguments?.getLong("idCliente") ?: 0L
                        ClienteDetalleScreen(
                            navController = navController,
                            idCliente = idCliente
                        )
                    }



                    // Rutas del cliente
                    composable(Route.ClienteHome.path) {
                        ClienteHomeScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                    composable(Route.ClienteCatalogo.path) {
                        ClienteCatalogoScreen(
                            navController = navController,
                            viewModel = hiltViewModel()
                        )
                    }
                    composable(Route.ClientePedidos.path) {
                        ClientePedidosScreen(navController = navController)
                    }
                    composable(Route.ClienteCart.path) {
                        ClienteCartScreen(navController = navController)
                    }
                    composable(
                        route = Route.ClienteProductoDetail.path,
                        arguments = listOf(navArgument("idModelo") { type = NavType.LongType })
                    ) {
                        ClienteProductoDetailScreen(navController = navController)
                    }
                    // Rutas del transportista
                    composable(Route.TransportistaHome.path) {
                        TransportistaHomeScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                    // TRANSPORTISTA - Lista de entregas
                    composable(
                        route = Route.TransportistaListaEntregas.path
                    ) {
                        TransportistaEntregasScreen(
                            navController = navController
                        )
                    }

                    // TRANSPORTISTA - Confirmar/Completar entrega
                    composable(
                        route = Route.TransportistaConfirmarEntrega.path,
                        arguments = listOf(navArgument("idEntrega") { type = NavType.LongType })
                    ) {
                        ConfirmarEntregaScreen(navController = navController)
                    }

                    composable(route = Route.TransportistaPerfil.path) {
                        TransportistaPerfilScreen(navController = navController)
                    }
                    composable(route = Route.ClientePerfil.path) {
                        ClientePerfilScreen(navController = navController)
                    }
                }
                // Overlay de carga mientras restauramos la sesión (z-index alto)
                if (isRestoring) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Restaurando sesión...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
             }
         }
     }
 }
