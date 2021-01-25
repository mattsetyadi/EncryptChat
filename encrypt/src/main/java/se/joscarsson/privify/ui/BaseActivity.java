package se.joscarsson.privify.ui;

import android.content.Intent;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import se.joscarsson.privify.R;

public abstract class BaseActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener, DrawerLayout.DrawerListener {
    protected ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int selectedMenuId;

    protected abstract int getMenuItemId();

    @Override
    protected void onResume() {
        super.onResume();
        this.selectedMenuId = getMenuItemId();

        if (this.selectedMenuId == -1) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            this.navigationView.setCheckedItem(this.selectedMenuId);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        this.drawerLayout = findViewById(R.id.drawer_layout);
        this.drawerLayout.addDrawerListener(this);

        this.drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        this.drawerToggle.setDrawerIndicatorEnabled(true);

        this.navigationView = findViewById(R.id.navigation_view);
        this.navigationView.getMenu().getItem(0).setOnMenuItemClickListener(this);
       // this.navigationView.getMenu().getItem(1).setOnMenuItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        this.selectedMenuId = item.getItemId();
        this.drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (this.selectedMenuId == getMenuItemId()) return;

        if (this.selectedMenuId == R.id.settings_menu_item) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            startActivity(new Intent(this, MainActivityPri.class));
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
