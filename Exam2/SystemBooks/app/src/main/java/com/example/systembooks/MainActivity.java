package com.example.systembooks;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.systembooks.firebase.FirebaseAuthRepository;
import com.example.systembooks.firebase.FirebaseLoginFragment;
import com.example.systembooks.firebase.FirebaseManager;
import com.example.systembooks.firebase.FirebaseRegisterFragment;
import com.example.systembooks.firebase.NotificationHelper;
import com.example.systembooks.fragment.AccessDeniedFragment;
import com.example.systembooks.fragment.LoginFragment;
import com.example.systembooks.fragment.ProfileFragment;
import com.example.systembooks.fragment.RegisterFragment;
import com.example.systembooks.fragment.UserManagementFragment;
import com.example.systembooks.fragments.AdminUserDataFragment;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.RoleManager;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private SessionManager sessionManager;
    private ApiRepository apiRepository;
    private RoleManager roleManager;
    private FirebaseAuthRepository firebaseAuthRepository;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar gestores
        sessionManager = new SessionManager(this);
        apiRepository = new ApiRepository(this);
        roleManager = new RoleManager(this);
        firebaseAuthRepository = new FirebaseAuthRepository(this);
        notificationHelper = new NotificationHelper(this);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Set up drawer layout
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Set up the hamburger icon to open/close drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // Mostrar menú según rol usando RoleManager
            String currentRole = roleManager.getCurrentRole();
            showMenuItemsByRole(currentRole);
            
            // Cargar el fragmento inicial según el rol
            if (roleManager.isAdmin()) {
                loadFragment(new UserManagementFragment());
                navigationView.setCheckedItem(R.id.nav_crud);
            } else if (roleManager.isUser()) {
                // Dependiendo del método de autenticación, cargar el fragmento correspondiente
                if (sessionManager.isFirebaseAuth()) {
                    loadFragment(new com.example.systembooks.firebase.FirebaseProfileFragment());
                } else {
                    loadFragment(new ProfileFragment());
                }
                navigationView.setCheckedItem(R.id.nav_profile);
            }
        } else {
            // Mostrar menú de invitado
            showMenuItemsByRole(RoleManager.ROLE_GUEST);
            
            // Cargar fragmento de login
            if (savedInstanceState == null) {
                loadFragment(new LoginFragment());
                navigationView.setCheckedItem(R.id.nav_login);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks here
        int id = item.getItemId();
        Fragment fragment = null;
        
        if (id == R.id.nav_login) {
            // Mostrar opción para elegir entre login normal o Firebase
            showLoginOptions();
            return true;
        } else if (id == R.id.nav_register) {
            // Mostrar opción para elegir entre registro normal o Firebase
            showRegisterOptions();
            return true;
        } else if (id == R.id.nav_firebase_login) {
            fragment = new FirebaseLoginFragment();
        } else if (id == R.id.nav_firebase_register) {
            fragment = new FirebaseRegisterFragment();
        } else if (id == R.id.nav_crud) {
            // Verificar si tiene permisos de administrador
            if (roleManager.isAdmin()) {
                fragment = new UserManagementFragment();
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_admin_user_data) {
            // Verify admin permissions for viewing user data
            if (roleManager.isAdmin()) {
                fragment = new AdminUserDataFragment();
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_notifications) {
            // Verify admin permissions for notification admin
            if (roleManager.isAdmin()) {
                fragment = new com.example.systembooks.firebase.NotificationAdminFragment();
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.admin_required, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_profile) {
            // Verificar si tiene permisos de usuario
            if (roleManager.isUser() || roleManager.isAdmin()) {
                // Dependiendo del método de autenticación, cargar el fragmento correspondiente
                if (sessionManager.isFirebaseAuth()) {
                    fragment = new com.example.systembooks.firebase.FirebaseProfileFragment();
                } else {
                    fragment = new ProfileFragment();
                }
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_books) {
            // Iniciar MainActivity_2 para la pantalla de libros
            Intent intent = new Intent(this, MainActivity_2.class);
            startActivity(intent);
            // No cargar ningún fragmento ya que estamos iniciando otra actividad
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_logout) {
            handleLogout();
            fragment = new LoginFragment();
        }
        
        // Cargar el fragment seleccionado
        if (fragment != null) {
            loadFragment(fragment);
        }
        
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void handleLogout() {
        // Verificar el tipo de autenticación
        if (sessionManager.isFirebaseAuth()) {
            // Logout de Firebase
            firebaseAuthRepository.signOut();
        } else {
            // Logout de API
            apiRepository.logout();
        }
        
        // Logout del SessionManager
        sessionManager.logout();
        
        // Actualizar UI
        showMenuItemsByRole(RoleManager.ROLE_GUEST);
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Updates the navigation menu based on user role
     * @param role The role of the user ("guest", "ROLE_ADMIN", or "ROLE_USER")
     */
    public void showMenuItemsByRole(String role) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        
        // Hide all menu groups first
        navigationView.getMenu().setGroupVisible(R.id.group_guest, false);
        navigationView.getMenu().setGroupVisible(R.id.group_admin, false);
        navigationView.getMenu().setGroupVisible(R.id.group_user, false);
        navigationView.getMenu().setGroupVisible(R.id.group_authenticated, false);
        
        // Show appropriate menu items based on role
        if (role.equals(RoleManager.ROLE_GUEST)) {
            navigationView.getMenu().setGroupVisible(R.id.group_guest, true);
            
            // Actualizar header con información de invitado
            TextView usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.header_username);
            TextView roleTextView = navigationView.getHeaderView(0).findViewById(R.id.header_role);
            
            usernameTextView.setText(R.string.not_logged_in);
            roleTextView.setText(R.string.guest);
            
        } else {
            navigationView.getMenu().setGroupVisible(R.id.group_authenticated, true);
            
            // Actualizar header con información del usuario
            TextView usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.header_username);
            TextView roleTextView = navigationView.getHeaderView(0).findViewById(R.id.header_role);
            
            // Obtener nombre de usuario desde la sesión
            String userName = sessionManager.getUserName() != null ? 
                    sessionManager.getUserName() : getString(R.string.default_username);
            usernameTextView.setText(userName);
            
            if (role.equals(RoleManager.ROLE_ADMIN)) {
                navigationView.getMenu().setGroupVisible(R.id.group_admin, true);
                navigationView.getMenu().setGroupVisible(R.id.group_user, true);
                roleTextView.setText(R.string.admin_role_name);
            } else if (role.equals(RoleManager.ROLE_USER)) {
                navigationView.getMenu().setGroupVisible(R.id.group_user, true);
                roleTextView.setText(R.string.user_role_name);
            }
        }
    }
    
    /**
     * Muestra un diálogo para elegir entre login normal o con Firebase
     */
    private void showLoginOptions() {
        String[] options = {
                getString(R.string.login_title),
                getString(R.string.firebase_login_title)
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.choose_login_method)
                .setItems(options, (dialog, which) -> {
                    Fragment fragment;
                    switch (which) {
                        case 0:
                            fragment = new LoginFragment();
                            break;
                        case 1:
                            fragment = new FirebaseLoginFragment();
                            break;
                        default:
                            fragment = new LoginFragment();
                    }
                    loadFragment(fragment);
                })
                .show();
    }
    
    /**
     * Muestra un diálogo para elegir entre registro normal o con Firebase
     */
    private void showRegisterOptions() {
        String[] options = {
                getString(R.string.register_title),
                getString(R.string.firebase_register_title)
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.choose_register_method)
                .setItems(options, (dialog, which) -> {
                    Fragment fragment;
                    switch (which) {
                        case 0:
                            fragment = new RegisterFragment();
                            break;
                        case 1:
                            fragment = new FirebaseRegisterFragment();
                            break;
                        default:
                            fragment = new RegisterFragment();
                    }
                    loadFragment(fragment);
                })
                .show();
    }
    
    /**
     * Carga el fragmento inicial después del login
     */
    public void loadHomeFragment() {
        // Cargar el fragmento inicial según el rol
        Fragment fragment;
        NavigationView navigationView = findViewById(R.id.nav_view);
        
        if (roleManager.isAdmin()) {
            fragment = new UserManagementFragment();
            navigationView.setCheckedItem(R.id.nav_crud);
        } else if (roleManager.isUser()) {
            // Dependiendo del método de autenticación, cargar el fragmento correspondiente
            if (sessionManager.isFirebaseAuth()) {
                fragment = new com.example.systembooks.firebase.FirebaseProfileFragment();
            } else {
                fragment = new ProfileFragment();
            }
            navigationView.setCheckedItem(R.id.nav_profile);
        } else {
            fragment = new AccessDeniedFragment();
        }
        
        loadFragment(fragment);
    }
}