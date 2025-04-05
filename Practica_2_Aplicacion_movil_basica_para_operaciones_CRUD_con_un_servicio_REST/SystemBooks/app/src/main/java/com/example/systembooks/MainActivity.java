package com.example.systembooks;

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

import com.example.systembooks.fragment.AccessDeniedFragment;
import com.example.systembooks.fragment.LoginFragment;
import com.example.systembooks.fragment.ProfileFragment;
import com.example.systembooks.fragment.RegisterFragment;
import com.example.systembooks.fragment.UserManagementFragment;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.RoleManager;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private SessionManager sessionManager;
    private ApiRepository apiRepository;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar gestores
        sessionManager = new SessionManager(this);
        apiRepository = new ApiRepository(this);
        roleManager = new RoleManager(this);
        
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
                loadFragment(new ProfileFragment());
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
            fragment = new LoginFragment();
        } else if (id == R.id.nav_register) {
            fragment = new RegisterFragment();
        } else if (id == R.id.nav_crud) {
            // Verificar si tiene permisos de administrador
            if (roleManager.isAdmin()) {
                fragment = new UserManagementFragment();
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_profile) {
            // Verificar si tiene permisos de usuario
            if (roleManager.isUser() || roleManager.isAdmin()) {
                fragment = new ProfileFragment();
            } else {
                fragment = new AccessDeniedFragment();
                Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            }
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
        apiRepository.logout();
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
        
        // First, hide all role-specific groups
        navigationView.getMenu().findItem(R.id.nav_crud).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_profile).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
        
        // Show appropriate menu items based on role
        if (role.equals(RoleManager.ROLE_GUEST)) {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_register).setVisible(true);
            
            // Actualizar header con información de invitado
            TextView usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.header_username);
            TextView roleTextView = navigationView.getHeaderView(0).findViewById(R.id.header_role);
            
            usernameTextView.setText(R.string.not_logged_in);
            roleTextView.setText(R.string.guest);
            
        } else {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_register).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            
            // Actualizar header con información del usuario
            TextView usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.header_username);
            TextView roleTextView = navigationView.getHeaderView(0).findViewById(R.id.header_role);
            
            // Obtener nombre de usuario desde la sesión
            String userName = sessionManager.getUserName() != null ? 
                    sessionManager.getUserName() : getString(R.string.default_username);
            usernameTextView.setText(userName);
            
            if (role.equals(RoleManager.ROLE_ADMIN)) {
                navigationView.getMenu().findItem(R.id.nav_crud).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_profile).setVisible(true);
                roleTextView.setText(R.string.admin_role_name);
            } else if (role.equals(RoleManager.ROLE_USER)) {
                navigationView.getMenu().findItem(R.id.nav_profile).setVisible(true);
                roleTextView.setText(R.string.user_role_name);
            }
        }
    }
}